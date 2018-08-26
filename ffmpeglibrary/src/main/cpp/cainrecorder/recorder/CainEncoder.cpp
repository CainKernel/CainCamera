/**
 *
 * Created by cain on 2018/1/6.
 */

#include "CainEncoder.h"


CainEncoder::CainEncoder()
        : isInited(false),
          fmt(NULL),
          fmt_ctx(NULL),
          audio_codec(NULL),
          video_codec(NULL),
          enableAudio(false),
          have_audio(false),
          have_video(false),
          video_pkt(),
          audio_pkt() {
    memset(&audio_st, 0, sizeof(OutputStream));
    memset(&video_st, 0, sizeof(OutputStream));
}

CainEncoder::~CainEncoder() {
    release();
}

/**
 * 设置输出路径
 * @param path
 */
void CainEncoder::setOutputFile(const char *path) {
    strncpy(mOutputFile, path, sizeof(mOutputFile));
}

/**
 * 设置视频大小
 * @param width
 * @param height
 */
void CainEncoder::setVideoSize(int width, int height) {
    mWidth = width;
    mHeight = height;
}

/**
 * 设置视频帧率
 * @param frameRate
 */
void CainEncoder::setVideoFrameRate(int frameRate) {
    mFrameRate = frameRate;
}

/**
 * 设置视频码率
 * @param bitRate
 */
void CainEncoder::setVideoBitRate(long long bitRate) {
    mBitRate = bitRate;
}

/**
 * 设置视频颜色格式
 * @param fmt
 */
void CainEncoder::setVideoColorFormat(OMX_COLOR_FORMATTYPE fmt) {
    mColor = fmt;
    if (mColor == OMX_COLOR_FormatYUV420SemiPlanar) {
        mPixFmt = AV_PIX_FMT_NV21;
    } else if (mColor == OMX_COLOR_FormatYUV420Planar) {
        mPixFmt = AV_PIX_FMT_YUV420P;
    }
}

/**
 * 设置是否允许音频编码
 * @param enable
 */
void CainEncoder::setEnableAudioEncode(bool enable) {
    enableAudio = enable;
}

/**
 * 设置饮片采样频率
 * @param sampleRate
 */
void CainEncoder::setAudioSampleRate(int sampleRate) {
    mAudioSampleRate = sampleRate;
}

/**
 * 设置音频码率
 * @param bitRate
 */
void CainEncoder::setAudioBitRate(int bitRate) {
    mAudioBitRate = bitRate;
}

/**
 * 将ffmpeg的log打印到文件或者能够
 * @param ptr
 * @param level
 * @param fmt
 * @param vl
 */
void ffmpeg_log(void *ptr, int level, const char *fmt, va_list vl) {
    FILE *fp = fopen("/storage/emulated/0/av_log.txt", "a+");
    if (fp) {
        vfprintf(fp, fmt, vl);
        fflush(fp);
        fclose(fp);
    }
}

/**
 * 初始化编码器
 * @return
 */
bool CainEncoder::initEncoder() {
    if (isInited) {
        return true;
    }
    do {
        AVDictionary *opt = NULL;
        int ret;
        av_log_set_callback(ffmpeg_log);
        // 注册
        av_register_all();
        // 分配输出媒体上下文
        avformat_alloc_output_context2(&fmt_ctx, NULL, NULL, mOutputFile);
        if (fmt_ctx == NULL) {
            ALOGE("fail to avformat_alloc_output_context2 for %s", mOutputFile);
            break;
        }
        // 获取AVOutputFormat
        fmt = fmt_ctx->oformat;

        // 使用默认格式编码器视频流，并初始化编码器
        if (fmt->video_codec != AV_CODEC_ID_NONE) {
            addStream(&video_st, fmt_ctx, &video_codec, fmt->video_codec);
            have_video = true;
        }
        // 使用默认格式编码器音频流，并初始化编码器
        if (fmt->audio_codec != AV_CODEC_ID_NONE && enableAudio) {
            addStream(&audio_st, fmt_ctx, &audio_codec, fmt->audio_codec);
            have_audio = true;
        }
        if(!have_video && !have_audio) {
            ALOGE("no audio or video codec found for the fmt!");
            break;
        }
        // 打开视频编码器
        if (have_video) {
            openVideo(video_codec, &video_st, opt);
        }
        // 打开音频编码器
        if (have_audio) {
            openAudio(audio_codec, &audio_st, opt);
        }
        // 打开输出文件
        ret = avio_open(&fmt_ctx->pb, mOutputFile, AVIO_FLAG_READ_WRITE);
        if (ret < 0) {
            ALOGE("Could not open '%s': %s", mOutputFile, av_err2str(ret));
            break;
        }
        // 写入文件头部信息
        ret = avformat_write_header(fmt_ctx, NULL);
        if (ret < 0) {
            ALOGE("Error occurred when opening output file: %s", av_err2str(ret));
            break;
        }
        isInited = true;
    } while (0);

    // 判断是否初始化成功，如果不成功，则需要重置所有状态
    if (!isInited) {
        reset();
    }

    return isInited;
}

/**
 * 添加码流
 * @param ost
 * @param oc
 * @param codec
 * @param codec_id
 * @return
 */
bool CainEncoder::addStream(OutputStream *ost, AVFormatContext *oc, AVCodec **codec,
                            enum AVCodecID codec_id) {

    AVCodecContext *context;
    // 查找编码器
    *codec = avcodec_find_encoder(codec_id);
    if (!(*codec)) {
        ALOGE("Could not find encoder for '%s'\n", avcodec_get_name(codec_id));
        return false;
    }
    // 创建输出码流
    ost->st = avformat_new_stream(oc, *codec);
    if (!ost->st) {
        ALOGE("Could not allocate stream\n");
        return false;
    }
    // 绑定码流的ID
    ost->st->id = oc->nb_streams - 1;
    // 创建编码上下文
    context = avcodec_alloc_context3(*codec);
    if (!context) {
        ALOGE("Could not alloc an encoding context\n");
        return false;
    }
    // 绑定编码上下文
    ost->enc = context;
    // 判断编码器的类型
    switch ((*codec)->type) {
        // 如果创建的是音频码流,则设置音频编码器的参数
        case AVMEDIA_TYPE_AUDIO:
            context->sample_fmt = (*codec)->sample_fmts
                                       ? (AVSampleFormat) (*codec)->sample_fmts[0]
                                       : AV_SAMPLE_FMT_S16;
            context->bit_rate = mAudioBitRate;
            context->sample_rate = mAudioSampleRate;
            // 判断支持的采样率
            if ((*codec)->supported_samplerates) {
                context->sample_rate = (*codec)->supported_samplerates[0];
                for (int i = 0; (*codec)->supported_samplerates[i]; i++) {
                    if((*codec)->supported_samplerates[i] == mAudioSampleRate) {
                        context->sample_rate = mAudioSampleRate;
                    }
                }
            }
            // 设定声道
            context->channels = av_get_channel_layout_nb_channels(context->channel_layout);
            context->channel_layout = AV_CH_LAYOUT_STEREO;
            if ((*codec)->channel_layouts) {
                context->channel_layout = (*codec)->channel_layouts[0];
                for (int i = 0; (*codec)->channel_layouts[i]; i++) {
                    if ((*codec)->channel_layouts[i] == AV_CH_LAYOUT_STEREO) {
                        context->channel_layout = AV_CH_LAYOUT_STEREO;
                    }
                }
            }
            // 重新设定声道
            context->channels = av_get_channel_layout_nb_channels(context->channel_layout);
            // 设定time_base
            ost->st->time_base = (AVRational) {1, context->sample_rate};


            break;

            //  如果创建的是视频码流，则设置视频编码器的参数
        case AVMEDIA_TYPE_VIDEO:
            context->codec_id = codec_id;
            context->bit_rate = mBitRate;
            context->width = mWidth;
            context->height = mHeight;
            ost->st->time_base = (AVRational) {1, mFrameRate};
            context->time_base = ost->st->time_base;
            context->gop_size = 12;
            context->pix_fmt = AV_PIX_FMT_YUV420P;
            context->thread_count = 12;
            context->qmin = 10;
            context->qmax = 51;
            context->max_b_frames = 3;
            break;

        default:
            break;
    }
    // 全局头部信息是否存在
    if (oc->oformat->flags & AVFMT_GLOBALHEADER) {
        context->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }
    return true;
}

/**
 * 打开音频编码器
 * @param codec
 * @param ost
 * @param opt_arg
 * @return
 */
bool CainEncoder::openAudio(AVCodec *codec, OutputStream *ost, AVDictionary *opt_arg) {
    AVCodecContext * codecContext;
    int ret;
    AVDictionary *opt = NULL;
    // 获取音频编码上下文
    codecContext = ost->enc;
    // 复制信息
    av_dict_copy(&opt, opt_arg, 0);
    // 打开音频编码器
    ret = avcodec_open2(codecContext, codec, &opt);
    av_dict_free(&opt);
    if (ret < 0) {
        ALOGE("Could not open audio codec: %s", av_err2str(ret));
        return false;
    }
    // 创建音频编码的AVFrame
    ost->frame = allocAudioFrame(codecContext->channels, codecContext->sample_fmt,
                                 codecContext->channel_layout,
                                 codecContext->sample_rate, codecContext->frame_size);
    // 创建暂存的AVFrame
    ost->tmp_frame = allocAudioFrame(codecContext->channels, AV_SAMPLE_FMT_S16,
                                     codecContext->channel_layout,
                                     codecContext->sample_rate, codecContext->frame_size);

    // 将码流参数复制到复用器
    ret = avcodec_parameters_from_context(ost->st->codecpar, codecContext);
    if (ret < 0) {
        ALOGE("Could not copy the stream parameters\n");
        return false;
    }

    // 创建重采样上下文
    ost->swr_ctx = swr_alloc();
    if (!ost->swr_ctx) {
        ALOGE("Could not allocate resampler context");
        return false;
    }

    // 设定重采样信息
    av_opt_set_int(ost->swr_ctx, "in_channel_count", codecContext->channels, 0);
    av_opt_set_int(ost->swr_ctx, "in_sample_rate", codecContext->sample_rate, 0);
    av_opt_set_sample_fmt(ost->swr_ctx, "in_sample_fmt", AV_SAMPLE_FMT_S16, 0);
    av_opt_set_int(ost->swr_ctx, "out_channel_count", codecContext->channels, 0);
    av_opt_set_int(ost->swr_ctx, "out_sample_rate", codecContext->sample_rate, 0);
    av_opt_set_sample_fmt(ost->swr_ctx, "out_sample_fmt", codecContext->sample_fmt, 0);

    // 初始化音频重采样上下文
    ret = swr_init(ost->swr_ctx);
    if (ret < 0) {
        ALOGE("Failed to initialize the resampling context");
        return false;
    }

    return true;
}

/**
 * 创建音频编码帧
 * @param sample_fmt
 * @param channel_layout
 * @param sample_rate
 * @param frame_size
 * @return
 */
AVFrame* CainEncoder::allocAudioFrame(int channels, enum AVSampleFormat sample_fmt,
                                      uint64_t channel_layout, int sample_rate, int frame_size) {
    // 创建音频帧
    AVFrame *frame = av_frame_alloc();
    int ret;
    if (!frame) {
        ALOGE("Error allocating an audio frame");
        return NULL;
    }
    // 设定音频帧的格式
    frame->format = sample_fmt;
    frame->channel_layout = channel_layout;
    frame->sample_rate = sample_rate;
    frame->nb_samples = frame_size;
    // 设置采样的缓冲大小
    audioSampleSize = av_samples_get_buffer_size(NULL, channels, frame_size,
                                      sample_fmt, 1);
    // 创建缓冲区
    uint8_t *frame_buf = (uint8_t *) av_malloc(audioSampleSize);
    // 填充音频缓冲数据
    avcodec_fill_audio_frame(frame, channels, sample_fmt,
                             (const uint8_t *) frame_buf, audioSampleSize, 1);
    return frame;
}

/**
 * 打开视频编码器
 * @param codec
 * @param ost
 * @param opt_arg
 * @return
 */
bool CainEncoder::openVideo(AVCodec *codec, OutputStream *ost, AVDictionary *opt_arg) {
    int ret;
    // 获取视频编码上下文
    AVCodecContext *codecContext = ost->enc;
    AVDictionary *opt = NULL;
    av_dict_copy(&opt, opt_arg, 0);
    // 设定H264编码参数，如果不设定，编码会有延迟
    if (ost->enc->codec_id == AV_CODEC_ID_H264) {
        av_dict_set(&opt, "tune", "zerolatency", 0);
        av_opt_set(codecContext->priv_data, "preset", "ultrafast", 0);
        av_dict_set(&opt, "profile", "baseline", 0);
    }
    // 打开视频编码器
    ret = avcodec_open2(codecContext, codec, &opt);
    av_dict_free(&opt);
    if (ret < 0) {
        ALOGE("Could not open video codec: %s", av_err2str(ret));
        return false;
    }
    // 分配并初始化一个可重用的帧
    ost->frame = allocVideoFrame(codecContext->pix_fmt,
                                 codecContext->width, codecContext->height);
    if (!ost->frame) {
        ALOGE("Could not allocate video frame");
        return false;
    }
    // 如果输出格式不是YUV420P，那么也需要临时的YUV420P图像。 然后将其转换为所需的输出格式
    ost->tmp_frame = NULL;
    if (codecContext->pix_fmt != mPixFmt) {
        ost->tmp_frame = allocVideoFrame(mPixFmt, codecContext->width, codecContext->height);
        if (!ost->tmp_frame) {
            ALOGE("Could not allocate temporary picture");
            return false;
        }
    }

    // 将码流参数复制到复用器
    ret = avcodec_parameters_from_context(ost->st->codecpar, codecContext);
    if (ret < 0) {
        ALOGE("Could not copy the stream parameters\n");
        return false;
    }

    return true;
}

/**
 * 创建视频帧
 * @param pix_fmt
 * @param width
 * @param height
 * @return
 */
AVFrame* CainEncoder::allocVideoFrame(enum AVPixelFormat pix_fmt, int width, int height) {
    AVFrame *picture;
    int ret;

    // 创建AVFrame
    picture = av_frame_alloc();
    if (!picture) {
        return NULL;
    }
    picture->format = pix_fmt;
    picture->width = width;
    picture->height = height;

    // 创建缓冲区
    int picture_size = avpicture_get_size(pix_fmt, width, height);
    uint8_t *buf = (uint8_t *) av_malloc(picture_size);
    avpicture_fill((AVPicture *) picture, buf, pix_fmt, width, height);

    return picture;
}

/**
 * 释放资源
 * @return
 */
bool CainEncoder::release() {
    if (!isInited) {
        return true;
    }
    // 重置所有资源
    reset();

    isInited = false;

    return true;
}

/**
 * 获取音频编码缓冲大小
 * @return
 */
int CainEncoder::getAudioEncodeSize() {
    return audioSampleSize;
}

/**
 * 重置所有资源
 */
void CainEncoder::reset() {
    // 关闭视频编码器码流
    if (have_video) {
        closeStream(fmt_ctx, &video_st);
    }
    // 关闭音频编码器码流
    if (have_audio) {
        closeStream(fmt_ctx, &audio_st);
    }
    // 关闭输出文件
    if (!(fmt_ctx->flags & AVFMT_NOFILE)) {
        avio_close(fmt_ctx->pb);
        fmt_ctx->pb = NULL;
    }
    // 关闭输出上下文
    if (fmt_ctx != NULL) {
        avformat_free_context(fmt_ctx);
        fmt_ctx = NULL;
    }
}

/**
 * 关闭编码器码流
 * @param oc
 * @param ost
 */
void CainEncoder::closeStream(AVFormatContext *oc, OutputStream *ost) {
    // 关闭编码上下文
    if (ost->enc != NULL) {
        avcodec_free_context(&ost->enc);
    }
    // 释放AVFrame
    if (ost->frame != NULL) {
        av_frame_free(&ost->frame);
        ost->frame = NULL;
    }
    if (ost->tmp_frame != NULL) {
        av_frame_free(&ost->tmp_frame);
        ost->tmp_frame = NULL;
    }
    // 释放视频格式缩放转换上下文
    if (ost->sws_ctx != NULL) {
        sws_freeContext(ost->sws_ctx);
        ost->sws_ctx = NULL;
    }
    // 释放重采样上下文
    if (ost->swr_ctx != NULL) {
        swr_free(&ost->swr_ctx);
        ost->swr_ctx = NULL;
    }
}

/**
 * 视频编码
 * @param data
 * @return
 */
status_t CainEncoder::videoEncode(uint8_t *data) {
    int ret;
    // 获取输出码流
    OutputStream *ost = &video_st;
    AVCodecContext *context;
    AVFrame *frame;
    int got_frame = 0;
    // 获取视频编码上下文
    context = ost->enc;
    // 根据格式复制数据
    if (mPixFmt == AV_PIX_FMT_NV21) {
        memcpy(ost->tmp_frame->data[0], data, context->width * context->height);
        memcpy(ost->tmp_frame->data[1], (char *) data + context->width * context->height,
               context->width * context->height / 2);
    } else if (mPixFmt == AV_PIX_FMT_YUV420P) { // YUV420P格式复制
        memcpy(ost->frame->data[0], data, context->width * context->height);
        memcpy(ost->frame->data[1], (char *) data + context->width * context->height,
               context->width * context->height / 4);
        memcpy(ost->frame->data[2], (char *) data + context->width * context->height * 5 / 4,
               context->width * context->height / 4);
    }

    // 判断格式是否相同，不相同时，必须进行转换
    if (context->pix_fmt != mPixFmt) {
        if (!ost->sws_ctx) {
            ost->sws_ctx = sws_getContext(context->width, context->height,
                                          mPixFmt,
                                          context->width, context->height,
                                          context->pix_fmt,
                                          SWS_BICUBIC, NULL, NULL, NULL);
            if (!ost->sws_ctx) {
                ALOGE("Could not initialize the conversion context");
                return UNKNOWN_ERROR;
            }
        }
        // 格式转换
        sws_scale(ost->sws_ctx,
                  (const uint8_t *const *) ost->tmp_frame->data, ost->tmp_frame->linesize,
                  0, context->height, ost->frame->data, ost->frame->linesize);
    }

    // 计算AVFrame的pts
    ost->frame->pts = av_rescale_q(ost->next_pts++,
                                   (AVRational) {1, mFrameRate}, ost->st->time_base);
    frame = ost->frame;
    // 初始化一个AVPacket
    AVPacket pkt = {0};
    av_init_packet(&pkt);
    // 对视频帧进行编码
    ret = avcodec_encode_video2(context, &pkt, frame, &got_frame);
    if (ret < 0) {
        ALOGE("Error encoding video frame: %s", av_err2str(ret));
        return UNKNOWN_ERROR;
    }
    ALOGI("encode video frame sucess! got frame = %d\n", got_frame);
    // 编码成功则将数据写入文件
    if (got_frame == 1) {
        ret = writeFrame(fmt_ctx, &context->time_base, ost->st, &pkt);
        // 释放AVPacket
        av_free_packet(&pkt);
        if (ret < 0) {
            ALOGE("Error write video frame: %s", av_err2str(ret));
            return UNKNOWN_ERROR;
        }
        ALOGI("write video frame sucess!\n");
    } else {
        ret = 0;
        // 释放AVPacket
        av_free_packet(&pkt);
    }

    // 判断是否写入成功
    if (ret < 0) {
        ALOGE("Error while writing video frame: %s", av_err2str(ret));
        return UNKNOWN_ERROR;
    }

    return OK;
}

/**
 * 音频编码
 * @param data
 * @param len
 * @return
 */
status_t CainEncoder::audioEncode(uint8_t *data, int len) {
    AVCodecContext *context;
    AVFrame *frame = NULL;
    int ret;
    int got_frame;
    int dst_nb_samples;
    OutputStream *ost = &audio_st;
    // 获取源数据
    unsigned char *srcData = (unsigned char *) data;
    // 初始化AVPacket
    AVPacket pkt = {0};
    av_init_packet(&pkt);
    // 获取音频编码上下文
    context = ost->enc;
    // 获取暂存的编码帧
    frame = audio_st.tmp_frame;
    // 复制数据
    memcpy(frame->data[0], srcData, len);
    // 获取pts
    frame->pts = audio_st.next_pts;
    // 计算pts
    audio_st.next_pts += frame->nb_samples;
    ALOGI("nb_samples = %d", frame->nb_samples);
    // 如果音频编码帧存在，则进入音频编码阶段
    if (frame) {
        // TODO 计算输出的dst_nb_samples，否则没法输出声音
        // 这是因为新版本的FFmpeg音频编码格式已经变成了AV_SAMPLE_FMT_FLTP
        // 但输入的PCM数据依旧是AV_SAMPLE_FMT_S16
        // 转换为目标格式
        ret = swr_convert(ost->swr_ctx, ost->frame->data, dst_nb_samples,
                          (const uint8_t **) frame->data, frame->nb_samples);
        if (ret < 0) {
            ALOGE("Error while converting\n");
            return UNKNOWN_ERROR;
        }
        // 获得音频帧并设置pts等
        frame = ost->frame;
        frame->pts = av_rescale_q(ost->samples_count, (AVRational) {1, context->sample_rate},
                                  context->time_base);
        ost->samples_count += dst_nb_samples;
        ALOGI("dst_nb_samples = %d", dst_nb_samples);
    }
    // 音频编码
    ret = avcodec_encode_audio2(context, &pkt, frame, &got_frame);
    if (ret < 0) {
        ALOGE("Error encoding audio frame: %s\n", av_err2str(ret));
        return UNKNOWN_ERROR;
    }
    ALOGI("encode audio frame sucess! got frame = %d\n", got_frame);
    pkt.pts = frame->pts;
    // 如果编码成功，则写入文件
    if (got_frame) {
        ret = writeFrame(fmt_ctx, &context->time_base, ost->st, &pkt);
        // 释放资源
        av_free_packet(&pkt);
        if (ret < 0) {
            ALOGE("Error while writing audio frame: %s\n", av_err2str(ret));
            return UNKNOWN_ERROR;
        }
        ALOGI("writing audio frame sucess!\n");
    }
    // 释放资源
    av_free_packet(&pkt);
    return OK;
}

/**
 * 停止编码
 * @return
 */
status_t CainEncoder::stopEncode() {
    // 写入文件尾
    av_write_trailer(fmt_ctx);
    ALOGI("写入文件尾部");
    return OK;
}

/**
 * 写入文件
 * @param fmt_ctx
 * @param time_base
 * @param st
 * @param pkt
 * @return
 */
int CainEncoder::writeFrame(AVFormatContext *fmt_ctx, const AVRational *time_base, AVStream *st,
                            AVPacket *pkt) {
    pkt->stream_index = st->index;
    // 写入文件
    return av_interleaved_write_frame(fmt_ctx, pkt);
}