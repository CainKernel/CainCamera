//
// Created by Administrator on 2018/1/4.
//

#include "cain_recorder.h"

/**
 * 构造方法
 * @param params
 */
CainRecorder::CainRecorder(EncoderParams * params) : params(params) {

}

void custom_log(void *ptr, int level, const char *fmt, va_list vl) {
    FILE *fp = fopen("/storage/emulated/0/av_log.txt", "a+");
    if (fp) {
        vfprintf(fp, fmt, vl);
        fflush(fp);
        fclose(fp);
    }
}

/**
 * 初始化录制器
 * @return
 */
int CainRecorder::initRecorder() {
    if (!params) {
        LOGE("error! EncoderParams is empty!");
        return -1;
    }

    // ------------------------------ 复用器初始化部分 ----------------------------------------------
    av_log_set_callback(custom_log);
    // 初始化
    av_register_all();

    // 打开输出上下文
    avformat_alloc_output_context2(&mFormatCtx, NULL, NULL, params->mediaPath);

    // 初始化视频编码器
    int ret = initVideoEncoder();
    if (ret < 0) {
        LOGE("failed to init video encoder!");
        return -1;
    }
    // 初始化音频编码器
    if (params->enableAudio) {
        ret = initAudioEncoder();
        if (ret) {
            LOGE("failed to init audio encoder!");
            return -1;
        }
    }

    // 打开输出文件
    ret = avio_open(&mFormatCtx->pb, params->mediaPath, AVIO_FLAG_READ_WRITE);
    if (ret < 0) {
        LOGE("avio_open error() error %d : Could not open %s", ret, params->mediaPath);
        return -1;
    }

    // 写入文件头
    avformat_write_header(mFormatCtx, NULL);

    return 0;

}

/*
 * 初始化视频编码器
 */
int CainRecorder::initVideoEncoder() {
    // 1、创建视频编码器
    mVideoCodec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (mVideoCodec == NULL) {
        LOGE("avcodec_find_encoder() error: video codec not found.");
        return -1;
    }

    // 2、创建编码上下文
    mVCodecCtx = avcodec_alloc_context3(mVideoCodec);
    if (!mVCodecCtx) {
        LOGE("Could not allocate video codec context\n");
        return -1;
    }

    // 3、设置视频编码上下文的相关参数
    mVCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
    mVCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;
    mVCodecCtx->width = params->previewWidth;
    mVCodecCtx->height = params->previewHeight;
    if (mFormatCtx->oformat->flags & AVFMT_GLOBALHEADER) {
        mVCodecCtx->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }
    mVCodecCtx->bit_rate = params->bitRate;
    mVCodecCtx->gop_size = 50;
    mVCodecCtx->thread_count = 12;
    mVCodecCtx->time_base.num = 1;
    mVCodecCtx->time_base.den = params->frameRate;
    mVCodecCtx->qmin = 10;
    mVCodecCtx->qmax = 51;
    mVCodecCtx->max_b_frames = 3;

    // 4、H.264参数设置
    AVDictionary *param = 0;
    if (mVCodecCtx->codec_id == AV_CODEC_ID_H264) {
        av_dict_set(&param, "preset", "ultrafast", 0);
        av_dict_set(&param, "tune", "zerolatency", 0);
        av_dict_set(&param, "profile", "baseline", 0);
    }

    // 5、打开视频编码器
    int ret = avcodec_open2(mVCodecCtx, mVideoCodec, &param);
    if (ret < 0) {
        LOGE("avcodec_open2() error %d: Could not open video codec.", ret);
        return -1;
    }

    // 6、创建视频码流并设置相关参数
    mVideoStream = avformat_new_stream(mFormatCtx, mVideoCodec);
    if (mVideoStream == NULL) {
        LOGE("avformat_new_stream() error: Could not allocate video stream!\n");
        return -1;
    }
    mVideoStream->time_base.num = 1;
    mVideoStream->time_base.den = params->frameRate;
    mVideoStream->codec = mVCodecCtx;

    // yuv的长度
    y_length = params->previewWidth * params->previewHeight;
    uv_length = y_length / 4;
    return 0;
}

/**
 * 初始化音频编码器
 * @return
 */
int CainRecorder::initAudioEncoder() {
    // 1、创建音频编码器
    mAudioCodec = avcodec_find_encoder(AV_CODEC_ID_AAC);
    if (mAudioCodec == NULL) {
        LOGE("avcodec_find_encoder() error: audio codec not found.");
        return -1;
    }

    // 2、创建音频编码上下文
    mACodecCtx = avcodec_alloc_context3(mAudioCodec);
    if (!mACodecCtx) {
        LOGE("Could not allocate video codec context\n");
        return -1;
    }

    // 3、设置音频编码上下文的相关参数
    mACodecCtx->codec_type = AVMEDIA_TYPE_AUDIO;
    mACodecCtx->sample_fmt = AV_SAMPLE_FMT_S16;
    mACodecCtx->sample_rate = params->audioSampleRate;
    mACodecCtx->channel_layout = AV_CH_LAYOUT_MONO;
    mACodecCtx->channels = av_get_channel_layout_nb_channels(mACodecCtx->channel_layout);
    mACodecCtx->bit_rate = params->audioBitRate;

    // 4、AAC参数设置
    AVDictionary *param = 0;
    if (mACodecCtx->codec_id == AV_CODEC_ID_AAC) {
        av_dict_set(&param, "profile", "aac_he", 0);
    }

    // 5、打开音频编码器
    int ret = avcodec_open2(mACodecCtx, mAudioCodec, &param);
    if (ret < 0) {
        LOGE("avcodec_open2() error %d: Could not open audio codec.", ret);
        return -1;
    }

    // 6、创建音频码流并设置相关参数
    mAudioStream = avformat_new_stream(mFormatCtx, mAudioCodec);
    if (mVideoStream == NULL) {
        LOGE("avformat_new_stream() error: Could not allocate audio stream!\n");
        return -1;
    }
    mVideoStream->time_base.num = 1;
    mVideoStream->time_base.den = params->audioSampleRate;
    mVideoStream->codec = mACodecCtx;

    // 采样大小
    mSampleSize = av_samples_get_buffer_size(NULL, mACodecCtx->channels,
                                             mACodecCtx->frame_size, mACodecCtx->sample_fmt, 1);
    return 0;
}

/**
 * 开始录制
 */
void CainRecorder::startRecord() {
    recorderState = RECORDER_STARTED;
    startTime = av_gettime();
}

/**
 * 录制结尾
 */
void CainRecorder::recordEndian() {
    recorderState = RECORDER_STOPPED;
    int ret = flushFrame(mFormatCtx, 0);
    if (ret < 0) {
        LOGE("flushing encoder failed!\n");
    }
    // 释放资源
    release();
}

/**
 * 刷出剩余编码帧
 * @return
 */
int CainRecorder::flushFrame(AVFormatContext *fmt_ctx, int streamIndex) {
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[streamIndex]->codec->codec->capabilities & CODEC_CAP_DELAY)) {
        return 0;
    }
    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_video2(fmt_ctx->streams[streamIndex]->codec, &enc_pkt,
                                    NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0) {
            break;
        }
        if (!got_frame) {
            ret = 0;
            break;
        }
        LOGI("flush Encoder: succeed to encode 1 frame video!\tsize:%5d\n", enc_pkt.size);
        AVRational time_base = mFormatCtx->streams[0]->time_base;//{ 1, 1000 };
        AVRational r_framerate1 = {60, 2};
        AVRational time_base_q = {1, AV_TIME_BASE};
        int64_t calc_duration = (double) (AV_TIME_BASE) * (1 / av_q2d(r_framerate1));    // 内部时间戳
        enc_pkt.pts = av_rescale_q(frameCount * calc_duration, time_base_q, time_base);
        enc_pkt.dts = enc_pkt.pts;
        enc_pkt.duration = av_rescale_q(calc_duration, time_base_q, time_base);
        enc_pkt.pos = -1;
        frameCount++;
        mFormatCtx->duration = enc_pkt.duration * frameCount;
        // 写入文件中
        ret = av_interleaved_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0) {
            break;
        }
    }
    // 释放AVPacket
    av_free_packet(&enc_pkt);
    // 写入文件尾
    av_write_trailer(mFormatCtx);
    return ret;
}

/**
 * h264编码
 * @param data yuv原始数据
 * @return
 */
int CainRecorder::avcEncode(jbyte *yuvData) {
    // 如果还没有开始录制，则跳过编码
    if (recorderState != RECORDER_STARTED) {
        return 0;
    }
    // 输出流
    if (mVideoStream == NULL) {
        LOGE("No video output stream (Is imageWidth > 0 && imageHeight > 0 and has start() been called?)");
        return -1;
    }
    // ------------------------- 安卓摄像头数据是NV21的，需要转换成YUV420P ----------------------------
    // 创建AVFrame对象
    mVideoFrame = av_frame_alloc();
    // 获取输出缓冲
    uint8_t *videoBuffer = (uint8_t *)av_malloc(
            avpicture_get_size(AV_PIX_FMT_YUV420P, mVCodecCtx->width,
                               mVCodecCtx->height));

    avpicture_fill((AVPicture *) mVideoFrame, videoBuffer, AV_PIX_FMT_YUV420P,
                   mVCodecCtx->width, mVCodecCtx->height);

    // NV21 转成 Y420P
    memcpy(mVideoFrame->data[0], yuvData, y_length);
    for (int i = 0; i < uv_length; ++i) {
        *(mVideoFrame->data[2] + i) = *(yuvData + y_length + i * 2);
        *(mVideoFrame->data[1] + i) = *(yuvData + y_length + i * 2 + 1);
    }
    // 设置宽高
    mVideoFrame->format = AV_PIX_FMT_YUV420P;
    mVideoFrame->width = mVCodecCtx->width;
    mVideoFrame->height = mVCodecCtx->height;

    // -------------------------------------- 开始编码 ---------------------------------------------
    // 创建一个AVPacket对象，用于保存编码后的h264数据
    mVideoPacket.data = NULL;
    mVideoPacket.size = 0;
    av_init_packet(&mVideoPacket);
    // YUV编码为H264
    int got_packet;
    int ret = avcodec_encode_video2(mVCodecCtx, &mVideoPacket,
                                    mVideoFrame, &got_packet);
    if (ret < 0) {
        LOGE("Error encoding video frame: %s %d", av_err2str(ret), ret);
        return -1;
    }
    av_frame_free(&mVideoFrame);
    // 如果成功编码，则设置相关参数，并写入文件中
    if (got_packet == 1) {
        frameCount++;
        mVideoPacket.stream_index = mVideoStream->index;
        AVRational time_base = mFormatCtx->streams[0]->time_base;//{ 1, 1000 };
        AVRational r_framerate1 = {60, 2};//{ 50, 2 };
        AVRational time_base_q = {1, AV_TIME_BASE};
        //内部时间戳
        int64_t calc_duration = (double) (AV_TIME_BASE) * (1 / av_q2d(r_framerate1));
        // 设置参数
        mVideoPacket.pts = av_rescale_q(frameCount * calc_duration, time_base_q, time_base);
        mVideoPacket.dts = mVideoPacket.pts;
        mVideoPacket.duration = av_rescale_q(calc_duration, time_base_q, time_base);
        mVideoPacket.pos = -1;
        // 延时
        int64_t pts_time = av_rescale_q(mVideoPacket.dts, time_base, time_base_q);
        int64_t now_time = av_gettime() - startTime;
        if (pts_time > now_time) {
            av_usleep(pts_time - now_time);
        }
        // 写入文件
        ret = av_interleaved_write_frame(mFormatCtx, &mVideoPacket);
        // 释放AVPacket
        av_free_packet(&mVideoPacket);
        if (ret < 0) {
            LOGE("av_interleaved_write_frame() error %d while writing interleaved video frame.", ret);
            return -1;
        }
    }
    // 删除视频缓冲
    delete(videoBuffer);

    return 0;
}

/**
 * aac编码
 */
int CainRecorder::aacEncode(jbyte *pcmData) {
    // 如果还没有开始录制，则跳过编码
    if (recorderState != RECORDER_STARTED) {
        return 0;
    }
    // 输出流
    if (mAudioStream == NULL) {
        LOGE("No audio output stream (Is imageWidth > 0 && imageHeight > 0 and has start() been called?)");
        return -1;
    }

    // 创建AVFrame
    mAudioFrame = av_frame_alloc();
    mAudioFrame->nb_samples = mACodecCtx->frame_size;
    mAudioFrame->format = mACodecCtx->sample_fmt;
    uint8_t *audioBuffer = (uint8_t *) av_malloc(mSampleSize);
    avcodec_fill_audio_frame(mAudioFrame, mACodecCtx->channels, mACodecCtx->sample_fmt,
                             (const uint8_t *) audioBuffer, mSampleSize, 1);
    // 填充数据
    mAudioFrame->data[0] = (uint8_t *) pcmData;
    mAudioFrame->pts = frameCount;
    // 创建一个AVPacket对象，用于保存编码后的AAC数据
    mAudioPacket.data = NULL;
    mAudioPacket.size = 0;
    av_init_packet(&mAudioPacket);
    int got_frame = 0;
    // pcm编码为AAC
    int ret = avcodec_encode_audio2(mACodecCtx, &mAudioPacket, mAudioFrame, &got_frame);
    if (ret < 0) {
        LOGE("Error encoding audio frame: %s %d", av_err2str(ret), ret);
    }
    av_frame_free(&mVideoFrame);

    // 如果成功编码，则设置相关参数，并写入文件中
    if (got_frame == 1) {
        mAudioPacket.stream_index = mAudioStream->index;
        // 写入音频
        ret = av_interleaved_write_frame(mFormatCtx, &mAudioPacket);
        // 释放资源
        av_free_packet(&mAudioPacket);
        if (ret < 0) {
            LOGE("av_interleaved_write_frame() error %d while writing interleaved audio frame.", ret);
            return -1;
        }
    }
    // 删除音频缓冲
    delete(audioBuffer);

    return 0;
}


/**
 * 释放资源
 */
void CainRecorder::release() {
    recorderState = RECORDER_RELEASE;
    startTime = 0;
    // 释放视频码流
    if (mVideoStream) {
        avcodec_close(mVideoStream->codec);
//        av_free(&mVideoStream);
    }
    // 释放音频码流
    if (mAudioStream) {
        avcodec_close(mAudioStream->codec);
//        av_free(&mAudioStream);
    }
    // 关闭io
    avio_close(mFormatCtx->pb);
    // 释放上下文
    avformat_free_context(mFormatCtx);
}