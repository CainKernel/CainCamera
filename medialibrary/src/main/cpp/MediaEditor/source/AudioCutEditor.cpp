//
// Created by CainHuang on 2019/3/10.
//

#include "AudioCutEditor.h"

AudioCutEditor::AudioCutEditor(const char *srcUrl, const char *dstUrl)
           : MediaEditor(), mStart(0), mDuration(0), mSpeed(1.0) {
    this->srcUrl = av_strdup(srcUrl);
    this->dstUrl = av_strdup(dstUrl);
}

AudioCutEditor::~AudioCutEditor() {
    if (srcUrl != nullptr) {
        av_freep(&srcUrl);
        srcUrl = nullptr;
    }
    if (dstUrl != nullptr) {
        av_freep(&dstUrl);
        dstUrl = nullptr;
    }
}

void AudioCutEditor::setDuration(long start, long duration) {
    this->mStart = start;
    this->mDuration = duration;
}

void AudioCutEditor::setSpeed(float speed) {
    this->mSpeed = speed;
}

int AudioCutEditor::process() {
    int ret;
    AVPacket packet;
    AVFrame *frame = NULL;
    int64_t startPos;
    bool audioFinish = false;    // 音频裁剪结束
    int audio_stream_idx = -1;

    AVFormatContext *ifmt_ctx = NULL;        // 解复用上下文
    AVCodecContext *audio_dec_ctx = NULL;   // 音频解码上下文

    SwrContext *resample_context = NULL;    // 音频重采样
    AVAudioFifo *fifo = NULL;               // 音频FIFO缓冲区

    // 音频AAC文件输出
    AVFormatContext *audio_ofmt_ctx = NULL;       // 音频复用上下文
    AVCodecContext *audio_enc_ctx = NULL;   // 音频编码上下文

    if (!srcUrl || !dstUrl) {
        LOGE("input file or output file is null.");
        return -1;
    }

    av_register_all();

    // 打开输入文件
    ret = openInputFile(srcUrl, &ifmt_ctx, &audio_stream_idx, &audio_dec_ctx);
    if (ret < 0) {
        goto end;
    }

    if (audio_stream_idx == -1) {
        ret = -1;
        goto end;
    }

    // 如果不存在音频或视频，则表示已经结束了
    if (audio_stream_idx == -1) {
        audioFinish = true;
    }
    // 创建音频AAC文件所需要的复用上下文、媒体流、编码器等
    if (audio_stream_idx >= 0) {
        // 打开音频输出文件
        ret = openAACOutputFile(dstUrl, &audio_ofmt_ctx, audio_dec_ctx, &audio_enc_ctx);
        if (ret < 0) {
            goto end;
        }

        // 初始化重采样器
        ret = initResampler(audio_dec_ctx, audio_enc_ctx, &resample_context);
        if (ret < 0) {
            goto end;
        }

        // 初始化FIFO缓冲区
        if ((ret = initAudioFifo(&fifo, audio_enc_ctx)) < 0) {
            goto end;
        }

        // 写入文件头部信息
        if ((ret = avformat_write_header(audio_ofmt_ctx, NULL)) < 0) {
            LOGE("Could not write output file header (error '%s')\n", av_err2str(ret));
            goto end;
        }
    }

    // 定位到起始位置
    startPos = av_rescale(mStart, AV_TIME_BASE, 1000);

    // 定位到附近的关键帧，优先使用视频帧，这里主要是为了定位到关键帧，以免解码得到的H264文件开头花屏
    if (audio_stream_idx >= 0) {
        ret = av_seek_frame(ifmt_ctx, audio_stream_idx, startPos, AVSEEK_FLAG_BACKWARD);
    } else {
        ret = -1;
    }
    if (ret < 0) {
        goto end;
    }

    av_init_packet(&packet);
    frame = av_frame_alloc();

    // 开始解码
    while (true) {

        // 音频和视频均提取完成，直接跳出循环
        if (audioFinish) {
            break;
        }

        // 读取数据包
        if ((ret = av_read_frame(ifmt_ctx, &packet)) < 0) {
            LOGE("Failed to read packet - '%s'.", av_err2str(ret));
            break;
        }

        // 音频重采样输出
        if (audio_stream_idx >= 0 && !audioFinish && packet.stream_index == audio_stream_idx) {

            // 判断音频时间戳是否超出裁剪的时钟，如果是，则直接继续下一轮。
            if (packet.dts != AV_NOPTS_VALUE || packet.pts != AV_NOPTS_VALUE) {
                if (packet.dts != AV_NOPTS_VALUE && packet.dts * av_q2d(ifmt_ctx->streams[audio_stream_idx]->time_base) * 1000L > mStart + mDuration) {
                    audioFinish = true;
                }
                if (packet.pts != AV_NOPTS_VALUE && packet.pts * av_q2d(ifmt_ctx->streams[audio_stream_idx]->time_base) * 1000L > mStart + mDuration) {
                    audioFinish = true;
                }
            }

            // 直接释放数据包的数据
            if (audioFinish) {
                av_packet_unref(&packet);
                break;
            }

            // 填充音频数据到fifo缓冲区中
            if (av_audio_fifo_size(fifo) < audio_enc_ctx->frame_size) {
                if ((ret = decodeAndWriteToFifo(fifo, audio_dec_ctx, audio_enc_ctx,
                                                resample_context, &packet)) < 0) {
                    goto end;
                }
            }

            // 如果填充完，则编码写入文件中
            while (av_audio_fifo_size(fifo) >= audio_enc_ctx->frame_size) {
                if ((ret = encodeAudioAndWrite(fifo, audio_ofmt_ctx, audio_enc_ctx)) < 0) {
                    goto end;
                }
            }
        }

        // 释放内存，防止内存泄漏
        av_frame_unref(frame);
        av_packet_unref(&packet);
    }

    // 音频AAC写入文件尾部信息
    if (audio_stream_idx >= 0 && dstUrl != NULL) {

        // 将缓冲区中剩余的音频帧刷出
        while (av_audio_fifo_size(fifo) > 0) {
            if ((ret = encodeAudioAndWrite(fifo, audio_ofmt_ctx, audio_enc_ctx)) < 0) {
                goto end;
            }
        }
        // 写入一个空的音频帧用作结束，也就是flush encoder
        int data_written;
        do {
            if ((ret = encodeAudioFrame(NULL, audio_ofmt_ctx, audio_enc_ctx, &data_written)) < 0) {
                goto end;
            }
        } while (data_written);

        // 写入文件尾
        if ((ret = av_write_trailer(audio_ofmt_ctx)) < 0) {
            LOGE("Could not write output file trailer (error '%s')\n", av_err2str(ret));
            goto end;
        }
    }
    ret = 0;

    // 退出释放内存处理
    end:
    av_packet_unref(&packet);
    av_frame_free(&frame);

    if (resample_context) {
        swr_free(&resample_context);
    }

    if (fifo) {
        av_audio_fifo_free(fifo);
    }

    if (audio_enc_ctx) {
        avcodec_close(audio_enc_ctx);
        avcodec_free_context(&audio_enc_ctx);
    }

    if (audio_ofmt_ctx) {
        avio_closep(&audio_ofmt_ctx->pb);
        avformat_free_context(audio_ofmt_ctx);
    }

    if (audio_dec_ctx) {
        avcodec_close(audio_dec_ctx);
        avcodec_free_context(&audio_dec_ctx);
    }

    if (ifmt_ctx) {
        avformat_close_input(&ifmt_ctx);
    }

    if (ret != 0) {
        if (mEditListener != nullptr) {
            mEditListener->onFailed(av_err2str(ret));
        }
    } else {
        if (mEditListener != nullptr) {
            mEditListener->onSuccess();
        }
    }

    return ret;
}

int AudioCutEditor::openInputFile(const char *filename, AVFormatContext **input_format_context,
                                  int *audio_stream_idx, AVCodecContext **input_audio_codec_context) {
    AVCodecContext *codec_ctx;
    AVCodec *input_codec;
    int ret;
    int i;
    int audioIndex = -1, videoIndex = -1;

    // 打开输入文件
    if ((ret = avformat_open_input(input_format_context, filename, NULL,
                                   NULL)) < 0) {
        LOGE("Could not open input file '%s' (error '%s')\n", filename, av_err2str(ret));
        *input_format_context = NULL;
        return ret;
    }

    // 查找媒体流信息
    if ((ret = avformat_find_stream_info(*input_format_context, NULL)) < 0) {
        LOGE("Could not open find stream info (error '%s')\n", av_err2str(ret));
        avformat_close_input(input_format_context);
        return ret;
    }

    // 查找媒体流
    for (i = 0; i < (*input_format_context)->nb_streams; ++i) {
        if ((*input_format_context)->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            if (audioIndex == -1) {
                audioIndex = i;
            }
        } else if ((*input_format_context)->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            if (videoIndex == -1) {
                videoIndex = i;
            }
        }
    }

    // 如果音频和视频索引都没找到直接退出
    if (audioIndex == -1 && videoIndex == -1) {
        LOGE("Could not find audio or video stream");
        return -1;
    }

    // 获取音频解码上下文
    if (audioIndex >= 0) {
        // 创建解码上下文
        codec_ctx = avcodec_alloc_context3(NULL);
        if (!codec_ctx) {
            LOGE("Could not allocate a decoding context\n");
            avformat_close_input(input_format_context);
            return AVERROR(ENOMEM);
        }

        // 复制音频解码参数到解码上下文中
        ret = avcodec_parameters_to_context(codec_ctx, (*input_format_context)->streams[audioIndex]->codecpar);
        if (ret < 0) {
            LOGE("Failed to copy parameters to audio decoder context.");
            avformat_close_input(input_format_context);
            avcodec_free_context(&codec_ctx);
            return ret;
        }

        // 设置时钟基准
        av_codec_set_pkt_timebase(codec_ctx, (*input_format_context)->streams[audioIndex]->time_base);

        // 查找音频解码器
        if (!(input_codec = avcodec_find_decoder((*input_format_context)->streams[audioIndex]->codecpar->codec_id))) {
            LOGE("Could not find input audio codec.");
            avcodec_free_context(&codec_ctx);
            avformat_close_input(input_format_context);
            return AVERROR_EXIT;
        }
        codec_ctx->codec_id = input_codec->id;

        //  打开音频解码器
        if ((ret = avcodec_open2(codec_ctx, input_codec, NULL)) < 0) {
            LOGE("Could not open input codec (error '%s')\n", av_err2str(ret));
            avcodec_free_context(&codec_ctx);
            avformat_close_input(input_format_context);
            return ret;
        }

        // 赋值
        *audio_stream_idx = audioIndex;
        *input_audio_codec_context = codec_ctx;
    }

    return 0;
}

int AudioCutEditor::openAACOutputFile(const char *filename, AVFormatContext **output_format_context,
                                      AVCodecContext *input_codec_context,
                                      AVCodecContext **output_codec_context) {
    AVCodecContext *enc_ctx          = NULL;
    AVIOContext *output_io_context = NULL;
    AVStream *stream               = NULL;
    AVCodec *output_codec          = NULL;
    int ret;

    // 打开输出文件
    if ((ret = avio_open(&output_io_context, filename, AVIO_FLAG_WRITE)) < 0) {
        LOGE("Could not open output file '%s' (error '%s')\n", filename, av_err2str(ret));
        return ret;
    }

    // 创建输出上下文
    if (!(*output_format_context = avformat_alloc_context())) {
        LOGE("Could not allocate output format context\n");
        return AVERROR(ENOMEM);
    }

    // 指定输出上下文
    (*output_format_context)->pb = output_io_context;

    // 猜测容器格式
    if (!((*output_format_context)->oformat = av_guess_format(NULL, filename, NULL))) {
        LOGE("Could not find output file format\n");
        goto cleanup;
    }

    // 复制文件名
    av_strlcpy((*output_format_context)->filename, filename,
               sizeof((*output_format_context)->filename));

    // 查找AAC/X264编码器
    if (!(output_codec = avcodec_find_encoder(AV_CODEC_ID_AAC))) {
        LOGE("Could not find an AAC encoder.\n");
        goto cleanup;
    }

    // 创建音频/视频流
    if (!(stream = avformat_new_stream(*output_format_context, NULL))) {
        LOGE("Could not create new stream\n");
        ret = AVERROR(ENOMEM);
        goto cleanup;
    }

    // 创建编码上下文
    enc_ctx = avcodec_alloc_context3(output_codec);
    if (!enc_ctx) {
        LOGE("Could not allocate an encoding context\n");
        ret = AVERROR(ENOMEM);
        goto cleanup;
    }

    // 指定音频编码参数
    enc_ctx->channels       = output_channel;
    enc_ctx->channel_layout = (uint64_t)av_get_default_channel_layout(output_channel);
    enc_ctx->sample_rate    = output_sample_rate;
    enc_ctx->sample_fmt     = output_sample_fmt;
    enc_ctx->bit_rate       = output_bit_rate;

    // 允许实验性的AAC编码器
    enc_ctx->strict_std_compliance = FF_COMPLIANCE_EXPERIMENTAL;

    // 使用输入文件相同的采样率
    stream->time_base.den = input_codec_context->sample_rate;
    stream->time_base.num = 1;

    // 设置全局头部信息
    if ((*output_format_context)->oformat->flags & AVFMT_GLOBALHEADER) {
        enc_ctx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }

    // 打开编码上下文
    if ((ret = avcodec_open2(enc_ctx, output_codec, NULL)) < 0) {
        LOGE("Could not open output codec (error '%s')\n", av_err2str(ret));
        goto cleanup;
    }

    // 复制编码上下文参数到媒体流
    ret = avcodec_parameters_from_context(stream->codecpar, enc_ctx);
    if (ret < 0) {
        LOGE("Could not initialize stream parameters\n");
        goto cleanup;
    }

    *output_codec_context = enc_ctx;
    return 0;

    cleanup:
    avcodec_free_context(&enc_ctx);
    avio_closep(&(*output_format_context)->pb);
    avformat_free_context(*output_format_context);
    *output_format_context = NULL;
    return ret < 0 ? ret : AVERROR_EXIT;
}

int AudioCutEditor::initResampler(AVCodecContext *input_codec_context,
                                  AVCodecContext *output_codec_context,
                                  SwrContext **resample_context) {
    int ret;
    *resample_context = swr_alloc_set_opts(NULL,
                                           av_get_default_channel_layout(output_codec_context->channels),
                                           output_codec_context->sample_fmt,
                                           output_codec_context->sample_rate,
                                           av_get_default_channel_layout(input_codec_context->channels),
                                           input_codec_context->sample_fmt,
                                           input_codec_context->sample_rate,
                                           0, NULL);
    if (!(*resample_context)) {
        LOGE("Could not allocate resample context\n");
        return AVERROR(ENOMEM);
    }

    // 初始化音频重采样器
    if ((ret = swr_init((*resample_context))) < 0) {
        LOGE("Could not open resample context\n");
        swr_free(resample_context);
        return ret;
    }
    return 0;
}

int AudioCutEditor::initConvertedSamples(uint8_t ***converted_input_samples,
                                         AVCodecContext *output_codec_context, int frame_size) {
    int ret;

    if (!(*converted_input_samples = (uint8_t **)calloc(output_codec_context->channels,
                                                        sizeof(**converted_input_samples)))) {
        LOGE("Could not allocate converted input sample pointers\n");
        return AVERROR(ENOMEM);
    }

    if ((ret = av_samples_alloc(*converted_input_samples, NULL,
                                output_codec_context->channels,
                                frame_size,
                                output_codec_context->sample_fmt, 0)) < 0) {
        LOGE("Could not allocate converted input samples (error '%s')\n",
             av_err2str(ret));
        av_freep(&(*converted_input_samples)[0]);
        free(*converted_input_samples);
        return ret;
    }
    return 0;
}

int AudioCutEditor::initAudioFifo(AVAudioFifo **fifo, AVCodecContext *output_codec_context) {
    if (!(*fifo = av_audio_fifo_alloc(output_codec_context->sample_fmt,
                                      output_codec_context->channels, 1))) {
        LOGE("Could not allocate FIFO\n");
        return AVERROR(ENOMEM);
    }
    return 0;
}

int AudioCutEditor::convertSamples(const uint8_t **input_data, uint8_t **converted_data,
                                   const int frame_size, SwrContext *resample_context) {
    int ret;

    if ((ret = swr_convert(resample_context, converted_data, frame_size,
                           input_data, frame_size)) < 0) {
        LOGE("Could not convert input samples (error '%s')\n", av_err2str(ret));
        return ret;
    }

    return 0;
}

int AudioCutEditor::addSamplesToFifo(AVAudioFifo *fifo, uint8_t **converted_input_samples,
                                     const int frame_size) {
    int ret;
    if ((ret = av_audio_fifo_realloc(fifo, av_audio_fifo_size(fifo) + frame_size)) < 0) {
        LOGE("Could not reallocate FIFO\n");
        return ret;
    }
    if (av_audio_fifo_write(fifo, (void **)converted_input_samples,
                            frame_size) < frame_size) {
        LOGE("Could not write data to FIFO\n");
        return AVERROR_EXIT;
    }
    return 0;
}

int AudioCutEditor::decodeAudioFrame(AVPacket *packet, AVFrame *frame,
                                     AVCodecContext *input_codec_context, int *data_present) {
    int ret;
    if (!packet) {
        return -1;
    }

    if ((ret = avcodec_decode_audio4(input_codec_context, frame, data_present, packet)) < 0) {
        LOGE("Could not decode frame (error '%s')\n", av_err2str(ret));
        return ret;
    }
    return 0;
}

int AudioCutEditor::decodeAndWriteToFifo(AVAudioFifo *fifo, AVCodecContext *input_codec_context,
                                         AVCodecContext *output_codec_context,
                                         SwrContext *resampler_context, AVPacket *packet) {
    AVFrame *input_frame = NULL;
    uint8_t **converted_input_samples = NULL;
    int data_present;
    int ret = AVERROR_EXIT;

    if (!(input_frame = av_frame_alloc())) {
        LOGE("Could not allocate input frame\n");
        ret = AVERROR(ENOMEM);
        goto cleanup;
    }

    // 解码音频数据包
    if (decodeAudioFrame(packet, input_frame, input_codec_context, &data_present)) {
        goto cleanup;
    }

    if (data_present) {
        if (initConvertedSamples(&converted_input_samples, output_codec_context,
                                 input_frame->nb_samples)) {
            goto cleanup;
        }
        if (convertSamples((const uint8_t**)input_frame->extended_data, converted_input_samples,
                           input_frame->nb_samples, resampler_context)) {
            goto cleanup;
        }
        if (addSamplesToFifo(fifo, converted_input_samples, input_frame->nb_samples)) {
            goto cleanup;
        }
        ret = 0;
    }
    ret = 0;

    cleanup:
    if (converted_input_samples) {
        av_freep(&converted_input_samples[0]);
        free(converted_input_samples);
    }
    av_frame_free(&input_frame);

    return ret;
}

int AudioCutEditor::encodeAudioFrame(AVFrame *frame, AVFormatContext *output_format_context,
                                     AVCodecContext *output_codec_context, int *data_present) {
    AVPacket output_packet;
    int ret;

    av_init_packet(&output_packet);
    output_packet.data = NULL;
    output_packet.size = 0;

    if (frame) {
        frame->pts = audio_pts;
        audio_pts += frame->nb_samples;
    }

    // 编码音频帧
    if ((ret = avcodec_encode_audio2(output_codec_context, &output_packet,
                                     frame, data_present)) < 0) {
        LOGE("Could not encode frame (error '%s')\n", av_err2str(ret));
        av_packet_unref(&output_packet);
        return ret;
    }

    if (*data_present) {

        // 计算裁剪进度
        double timeStamp = output_packet.pts * av_q2d(output_codec_context->time_base);
        double percent = timeStamp * 1000 / mDuration;
        if (percent < 0) {
            percent = 0;
        } else if (percent > 1.0) {
            percent = 1.0;
        }
        LOGD("processing percent: %f", percent);
        if (mEditListener != nullptr) {
            mEditListener->onProcessing((int)(percent * 100));
        }

        if ((ret = av_write_frame(output_format_context, &output_packet)) < 0) {
            LOGE("Could not write frame (error '%s')\n", av_err2str(ret));
            av_packet_unref(&output_packet);
            return ret;
        }
        av_packet_unref(&output_packet);
    }

    return 0;
}

int AudioCutEditor::initAudioOutputFrame(AVFrame **frame, AVCodecContext *output_codec_context,
                                         int frame_size) {
    int ret;

    if (!(*frame = av_frame_alloc())) {
        LOGE("Could not allocate output frame\n");
        return AVERROR_EXIT;
    }

    // 设置参数
    (*frame)->nb_samples     = frame_size;
    (*frame)->channel_layout = output_codec_context->channel_layout;
    (*frame)->format         = output_codec_context->sample_fmt;
    (*frame)->sample_rate    = output_codec_context->sample_rate;

    // 创建采样数据空间
    if ((ret = av_frame_get_buffer(*frame, 0)) < 0) {
        LOGE("Could not allocate output frame samples (error '%s')\n", av_err2str(ret));
        av_frame_free(frame);
        return ret;
    }

    return 0;
}

int AudioCutEditor::encodeAudioAndWrite(AVAudioFifo *fifo, AVFormatContext *output_format_context,
                                        AVCodecContext *output_codec_context) {
    AVFrame *output_frame;
    const int frame_size = FFMIN(av_audio_fifo_size(fifo),
                                 output_codec_context->frame_size);
    int data_written;

    // 初始化输出的音频帧
    if (initAudioOutputFrame(&output_frame, output_codec_context, frame_size)) {
        return AVERROR_EXIT;
    }

    // 从FIFO中读取样本数据
    if (av_audio_fifo_read(fifo, (void **)output_frame->data, frame_size) < frame_size) {
        fprintf(stderr, "Could not read data from FIFO\n");
        av_frame_free(&output_frame);
        return AVERROR_EXIT;
    }

    // 编码数据
    if (encodeAudioFrame(output_frame, output_format_context, output_codec_context, &data_written)) {
        av_frame_free(&output_frame);
        return AVERROR_EXIT;
    }
    av_frame_free(&output_frame);
    return 0;
}
