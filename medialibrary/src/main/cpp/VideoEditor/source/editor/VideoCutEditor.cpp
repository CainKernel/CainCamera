//
// Created by CainHuang on 2019/2/26.
//

#include "VideoCutEditor.h"

VideoCutEditor::VideoCutEditor(const char *srcUrl, const char *videoUrl, MessageHandle *messageHandle)
                : Editor(messageHandle), srcUrl(srcUrl), dstUrl(videoUrl),
                start(0), duration(0), speed(1.0) {
    mMutex = new Mutex();
}

VideoCutEditor::~VideoCutEditor() {
    if (mMutex) {
        delete mMutex;
        mMutex = nullptr;
    }
}

void VideoCutEditor::setDuration(long start, long duration) {
    this->start = start;
    this->duration = duration;
}

void VideoCutEditor::setSpeed(float speed) {
    this->speed = speed;
}

int VideoCutEditor::process() {
    // 输入
    AVFormatContext *ifmt_ctx = NULL;
    AVStream *in_audio_stream = NULL;
    AVStream *in_video_stream = NULL;
    AVCodecContext *audio_dec_ctx = NULL;
    AVCodec *input_dec = NULL;
    int audio_input_idx = -1;
    int video_input_idx = -1;


    // 输出
    AVOutputFormat *ofmt = NULL;
    AVFormatContext *ofmt_ctx = NULL;
    AVStream *out_audio_stream = NULL;
    AVStream *out_video_stream = NULL;
    AVCodec *audio_enc = NULL;              // 音频编码器对象
    AVCodecContext *audio_enc_ctx = NULL;   // 音频编码上下文

    // 用于转码操作
    SwrContext *resample_context = NULL;    // 音频重采样

    double frameRate = 0;
    int packetCount = 0;
    // 音视频裁剪完成标志
    bool audioFinish = true;
    bool videoFinish = true;



    AVFrame *frame;
    AVPacket pkt;
    int got_frame;
    int64_t startPos;
    int seekFlag = 0;
    int ret, i;
    double pts = 0;

    av_register_all();

    // 打开输入文件
    if ((ret = avformat_open_input(&ifmt_ctx, srcUrl, 0, 0)) < 0) {
        LOGE("Could not open input file '%s'", srcUrl);
        goto end;
    }

    // 查找媒体流信息
    if ((ret = avformat_find_stream_info(ifmt_ctx, 0)) < 0) {
        LOGE("Failed to retrieve input stream information");
        goto end;
    }

    // 打印输入文件信息
    av_dump_format(ifmt_ctx, 0, srcUrl, 0);

    // 打开输出文件
    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, dstUrl);
    if (!ofmt_ctx) {
        LOGE("Could not create output context\n");
        ret = AVERROR_UNKNOWN;
        goto end;
    }

    // 获取输出格式
    ofmt = ofmt_ctx->oformat;

    // 利用输入文件的媒体流，
    for (i = 0; i < ifmt_ctx->nb_streams; i++) {
        AVStream *out_stream;
        // 设置裁剪完成标志
        if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoFinish = false;
            video_input_idx = i;
            in_video_stream = ifmt_ctx->streams[i];

            // 创建输出媒体流对象
            out_stream = avformat_new_stream(ofmt_ctx, NULL);
            if (!out_stream) {
                LOGE("Failed allocating output stream\n");
                ret = AVERROR_UNKNOWN;
                goto end;
            }

            // 将输入的媒体流参数复制到输出媒体流中
            ret = avcodec_parameters_copy(out_stream->codecpar, in_video_stream->codecpar);
            if (ret < 0) {
                LOGE("Failed to copy codec parameters\n");
                goto end;
            }

            // 需要设置旋转角度，fmt_ctx中不一定有旋转角度的信息
            AVDictionaryEntry *entry = av_dict_get(in_video_stream->metadata, "rotate", NULL,
                                                   AV_DICT_MATCH_CASE);
            if (entry && entry->value) {
                av_dict_set(&out_stream->metadata, "rotate", entry->value, 0);
            } else {
                av_dict_set(&out_stream->metadata, "rotate", "0", 0);
            }
            out_stream->codecpar->codec_tag = 0;
            out_video_stream = out_stream;

        } else if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioFinish = false;
            audio_input_idx = i;
            in_audio_stream = ifmt_ctx->streams[i];

            // 创建音频解码上下文
            audio_dec_ctx = avcodec_alloc_context3(NULL);
            if (!audio_dec_ctx) {
                LOGE("Could not allocate a decoding context.");
                goto end;
            }

            // 复制解码参数到解码上下文中
            ret = avcodec_parameters_to_context(audio_dec_ctx, in_audio_stream->codecpar);
            if (ret < 0) {
                LOGE("Could not copy parameters to audio decoder context.");
                goto end;
            }

            // 复制时基到解码上下文中
            av_codec_set_pkt_timebase(audio_dec_ctx, in_audio_stream->time_base);

            // 查找音频解码器
            if (!(input_dec = avcodec_find_decoder(in_audio_stream->codecpar->codec_id))) {
                LOGE("Could not find input audio codec.");
                goto end;
            }
            audio_dec_ctx->codec_id = input_dec->id;

            // 打开音频解码器
            if ((ret = avcodec_open2(audio_dec_ctx, input_dec, NULL)) < 0) {
                LOGE("Could not open input codec (error '%s')", av_err2str(ret));
                goto end;
            }

            // 创建输出媒体流对象
            out_stream = avformat_new_stream(ofmt_ctx, NULL);
            if (!out_stream) {
                LOGE("Failed allocating output stream\n");
                ret = AVERROR_UNKNOWN;
                goto end;
            }

            // 将输入的媒体流参数复制到输出媒体流中
            ret = avcodec_parameters_copy(out_stream->codecpar, in_audio_stream->codecpar);
            if (ret < 0) {
                LOGE("Failed to copy codec parameters\n");
                goto end;
            }
            out_stream->codecpar->codec_tag = 0;
            out_audio_stream = out_stream;
        }
    }

    // 打印输出文件的信息
    av_dump_format(ofmt_ctx, 0, dstUrl, 1);

    // 判断如果是文件，则用avio_open打开输出文件
    if (!(ofmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, dstUrl, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGE("Could not open output file '%s'", dstUrl);
            goto end;
        }
    }
    // 复制文件名
    av_strlcpy((*ofmt_ctx).filename, dstUrl, sizeof((*ofmt_ctx).filename));

    // 音频需要做转码处理，方便后面的操作
    if (audio_dec_ctx != NULL && in_audio_stream != NULL && out_audio_stream != NULL) {
        // 创建编码上下文
        audio_enc_ctx = avcodec_alloc_context3(NULL);
        if (!audio_enc_ctx) {
            LOGE("Could not allocate an encoding context");
            goto end;
        }

        // 指定音频编码参数
        audio_enc_ctx->channels = output_channel;
        audio_enc_ctx->channel_layout = (uint64_t)av_get_default_channel_layout(output_channel);
        audio_enc_ctx->sample_rate = output_sample_rate;
        audio_enc_ctx->sample_fmt = output_sample_fmt;
        audio_enc_ctx->bit_rate = output_bit_rate;
        // 允许实验性的AAC编码器
        audio_enc_ctx->strict_std_compliance = FF_COMPLIANCE_EXPERIMENTAL;
        out_audio_stream->time_base.den = output_sample_rate;
        out_audio_stream->time_base.num = 1;

        // 查找编码器
        audio_enc = avcodec_find_encoder(AV_CODEC_ID_AAC);
        if (!audio_enc) {
            LOGE("Could not find an AAC encoder.");
            goto end;
        }

        // 打开编码器
        if ((ret = avcodec_open2(audio_enc_ctx, audio_enc, NULL)) < 0) {
            LOGE("Could not open output codec (error '%s')", av_err2str(ret));
            goto end;
        }

        // 初始化音频重采样器
        ret = initResampler(audio_dec_ctx, audio_enc_ctx, &resample_context);
        if (ret < 0) {
            LOGE("Could not init resample context");
            goto end;
        }

    }

    // 写入文件头部信息
    ret = avformat_write_header(ofmt_ctx, NULL);
    if (ret < 0) {
        LOGE("Error occurred when opening output file\n");
        goto end;
    }

    // 设置视频帧率
    if (in_video_stream != NULL) {
        // 计算帧率
        if (in_video_stream->r_frame_rate.den > 0) {
            frameRate = (float)in_video_stream->r_frame_rate.num / (float)in_video_stream->r_frame_rate.den;
        } else if (in_video_stream->codec->framerate.den > 0) {
            frameRate = (float)in_video_stream->codec->framerate.num / (float)in_video_stream->codec->framerate.den;
        }
        frameRate = frameRate * speed;
        LOGD("frame rate = %f", frameRate);
    }

    // 定位到起始位置
    startPos = av_rescale(start, AV_TIME_BASE, 1000);
    // 这里用av_seek_frame有可能出现没数据的情况，就是裁剪GOP较大的时候，裁剪区间落在其中，无法裁到正确的数据
    seekFlag &= ~AVSEEK_FLAG_BYTE;
    ret = avformat_seek_file(ifmt_ctx, -1, INT64_MIN, startPos, INT64_MAX, seekFlag);
    if (ret < 0) {
        LOGE("Could not seek to position - '%ld'", start);
        goto end;
    }

    frame = av_frame_alloc();
    while (1) {

        // 裁剪超范围则直接退出
        if (audioFinish && videoFinish) {
            break;
        }

        // 不断地从输入文件中读取数据包
        ret = av_read_frame(ifmt_ctx, &pkt);
        if (ret < 0) {
            break;
        }

        if (video_input_idx == pkt.stream_index) {

            // 判断输出的时长是否超出了范围
            AVRational *time_base = &ifmt_ctx->streams[pkt.stream_index]->time_base;
            pts = atof(av_ts2timestr(pkt.pts, time_base));
            if (pts * 1000L > start + duration) {
                videoFinish = true;
                av_packet_unref(&pkt);
                continue;
            }

            // 根据输入流的时基，将pts、dts、duration等信息转换到输出媒体流的值。
            // 由于裁剪是连续的，这里并不需要做转码就可以报数据包存放到输出媒体流中了，对应ffmpeg命令行的 -vcodec copy
            // 另外就是，我们可以通过改变dts/pts实现视频数据包帧率的控制，而音频则需要做倍速转码操作。
            pkt.pts = av_rescale_q_rnd(pkt.pts, in_video_stream->time_base, out_video_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
            pkt.dts = av_rescale_q_rnd(pkt.dts, in_video_stream->time_base, out_video_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
//            // 重新设置dts和pts,用于调整帧率，这个要配合音频变速的代码进行处理
//            pkt.pts = pkt.dts = (int64_t)(packetCount * out_video_stream->time_base.den / out_video_stream->time_base.num / frameRate);
//            packetCount++;
            pkt.duration = av_rescale_q(pkt.duration, in_video_stream->time_base, out_video_stream->time_base);
            pkt.pos = -1;
        } else if (audio_input_idx == pkt.stream_index) {
            // 判断输出的时长是否超出了范围
            AVRational *time_base = &ifmt_ctx->streams[pkt.stream_index]->time_base;
            pts = atof(av_ts2timestr(pkt.pts, time_base));
            if (pts * 1000L > start + duration) {
                audioFinish = true;
                av_packet_unref(&pkt);
                continue;
            }

            // TODO 音频倍速变换有些问题，暂时先不做处理
//            // 解码音频数据包
//            if ((ret = avcodec_decode_audio4(audio_dec_ctx, frame, &got_frame, &pkt)) < 0) {
//                LOGE("Could not decode audio frame (error '%s')", av_err2str(ret));
//                goto end;
//            }
//
//            if (got_frame) {
//                uint8_t **converted_samples = NULL;
//
//                // 初始化转码内存
//                if (initConvertedSamples(&converted_samples, audio_enc_ctx,
//                                         frame->nb_samples)) {
//                    if (converted_samples) {
//                        av_freep(&converted_samples[0]);
//                        free(converted_samples);
//                    }
//                    goto end;
//                }
//
//                // 音频重采样
//                if ((ret = swr_convert(resample_context, converted_samples, frame->nb_samples,
//                                       (const uint8_t**)frame->extended_data, frame->nb_samples)) < 0) {
//                    LOGE("Could not convert input samples (error '%s')\n", av_err2str(ret));
//                    if (converted_samples) {
//                        av_freep(&converted_samples[0]);
//                        free(converted_samples);
//                    }
//                    goto end;
//                }
//
//
//                // TODO 变速并重新编码，这里还没想好怎么处理才不会出现杂音的问题，倍速后续再弄吧
//                // TODO 等弄完编辑合成页面的功能再回来弄这边
//
//                // 释放转码后的数据
//                if (converted_samples) {
//                    av_freep(&converted_samples[0]);
//                    free(converted_samples);
//                }
//            }

            // 根据输入流的时基，计算出输出数据包的pts、dts、duration等实际数据
            pkt.pts = av_rescale_q_rnd(pkt.pts, in_audio_stream->time_base, out_audio_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
            pkt.dts = av_rescale_q_rnd(pkt.dts, in_audio_stream->time_base, out_audio_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
            pkt.duration = av_rescale_q(pkt.duration, in_audio_stream->time_base, out_audio_stream->time_base);
            pkt.pos = -1;

        } else {
            av_packet_unref(&pkt);
            continue;
        }

        // 写入复用器中
        ret = av_interleaved_write_frame(ofmt_ctx, &pkt);
        av_packet_unref(&pkt);
        if (ret < 0) {
            LOGE("Error muxing packet\n");
            break;
        }
    }

    // 写入文件尾部
    av_write_trailer(ofmt_ctx);
    ret = 0;

    end:
    av_frame_free(&frame);
    av_packet_unref(&pkt);

    // 关闭解复用上下文
    avformat_close_input(&ifmt_ctx);

    // 释放音频解码上下文
    if (audio_dec_ctx) {
        avcodec_close(audio_dec_ctx);
    }

    // 释放重采样上下文
    if (resample_context) {
        swr_free(&resample_context);
    }

    // 释放音频编码上下文
    if (audio_enc_ctx) {
        avcodec_free_context(&audio_enc_ctx);
        audio_enc_ctx = NULL;
    }

    // 关闭输出文件
    if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE)) {
        avio_closep(&ofmt_ctx->pb);
    }
    avformat_free_context(ofmt_ctx);
    ofmt_ctx = NULL;

    // 返回倍速裁剪的结果
    if (ret < 0 && ret != AVERROR_EOF) {
        LOGE("Error occurred: %s\n", av_err2str(ret));
        return -1;
    }

    return 0;
}

int VideoCutEditor::initResampler(AVCodecContext *input_codec_context,
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
    if ((ret = swr_init((*resample_context))) < 0) {
        LOGE("Could not open resample context\n");
        swr_free(resample_context);
        return ret;
    }
    return 0;
}


int VideoCutEditor::initConvertedSamples(uint8_t ***converted_input_samples,
                                         AVCodecContext *output_codec_context, int frame_size) {
    int ret;
    if (!(*converted_input_samples = (uint8_t **)calloc(output_codec_context->channels,
                                                        sizeof(**converted_input_samples)))) {
        LOGE("Could not allocate converted input sample pointers\n");
        return AVERROR(ENOMEM);
    }
    if ((ret = av_samples_alloc(*converted_input_samples, NULL, output_codec_context->channels,
                                frame_size, output_codec_context->sample_fmt, 0)) < 0) {
        LOGE("Could not allocate converted input samples (error '%s')\n", av_err2str(ret));
        av_freep(&(*converted_input_samples)[0]);
        free(*converted_input_samples);
        return ret;
    }
    return 0;
}
