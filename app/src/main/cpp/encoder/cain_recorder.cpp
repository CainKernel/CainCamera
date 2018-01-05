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

    // ----------------------------- 视频编码器初始化部分 -------------------------------------------
    // 创建视频编码器
    mVideoCodec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (mVideoCodec == NULL) {
        LOGE("avcodec_find_encoder() error: Video codec not found.");
        return -1;
    }

    // 创建编码上下文
    mVideoCodecContext = avcodec_alloc_context3(mVideoCodec);
    if (!mVideoCodecContext) {
        LOGE("Could not allocate video codec context\n");
        return -1;
    }

    // 获取编码上下文，并设置相关参数
    mVideoCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
    mVideoCodecContext->pix_fmt = AV_PIX_FMT_YUV420P;
    mVideoCodecContext->width = params->previewWidth;
    mVideoCodecContext->height = params->previewHeight;
    if (mFormatCtx->oformat->flags & AVFMT_GLOBALHEADER) {
        mVideoCodecContext->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }
    mVideoCodecContext->bit_rate = params->bitRate;
    mVideoCodecContext->gop_size = 50;
    mVideoCodecContext->thread_count = 12;
    mVideoCodecContext->time_base.num = 1;
    mVideoCodecContext->time_base.den = params->frameRate;
    mVideoCodecContext->qmin = 10;
    mVideoCodecContext->qmax = 51;
    mVideoCodecContext->max_b_frames = 3;

    // Set Option
    AVDictionary *param = 0;
    // H.264参数设置
    if (mVideoCodecContext->codec_id == AV_CODEC_ID_H264) {
        av_dict_set(&param, "preset", "ultrafast", 0);
        av_dict_set(&param, "tune", "zerolatency", 0);
        av_dict_set(&param, "profile", "baseline", 0);
    }

    // 打开视频编码器
    int ret = avcodec_open2(mVideoCodecContext, mVideoCodec, &param);
    if (ret < 0) {
        LOGE("avcodec_open2() error %d: Could not open video codec.", ret);
        return -1;
    }

    // 创建视频码流
    mVideoStream = avformat_new_stream(mFormatCtx, mVideoCodec);
    if (mVideoStream == NULL) {
        LOGE("avformat_new_stream() error: Could not allocate video stream!\n");
        return -1;
    }
    mVideoStream->time_base.num = 1;
    mVideoStream->time_base.den = params->frameRate;
    mVideoStream->codec = mVideoCodecContext;

    // TODO 音频编码初始化


    // 打开输出文件
    ret = avio_open(&mFormatCtx->pb, params->mediaPath, AVIO_FLAG_READ_WRITE);
    if (ret < 0) {
        LOGE("avio_open error() error %d : Could not open %s", ret, params->mediaPath);
        return -1;
    }

    // 写入文件头
    avformat_write_header(mFormatCtx, NULL);

    // yuv的长度
    y_length = params->previewWidth * params->previewHeight;
    uv_length = y_length / 4;
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
        if (ret < 0)
            break;
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
    uint8_t *out_buffer = (uint8_t *)av_malloc(
            avpicture_get_size(AV_PIX_FMT_YUV420P, mVideoCodecContext->width,
                               mVideoCodecContext->height));

    avpicture_fill((AVPicture *) mVideoFrame, out_buffer, AV_PIX_FMT_YUV420P,
                   mVideoCodecContext->width, mVideoCodecContext->height);

    // NV21 转成 Y420P
    memcpy(mVideoFrame->data[0], yuvData, y_length);
    for (int i = 0; i < uv_length; ++i) {
        *(mVideoFrame->data[2] + i) = *(yuvData + y_length + i * 2);
        *(mVideoFrame->data[1] + i) = *(yuvData + y_length + i * 2 + 1);
    }
    // 设置宽高
    mVideoFrame->format = AV_PIX_FMT_YUV420P;
    mVideoFrame->width = mVideoCodecContext->width;
    mVideoFrame->height = mVideoCodecContext->height;

    // -------------------------------------- 开始编码 ---------------------------------------------
    // 创建一个AVPacket对象，用于保存编码后的h264数据
    mVideoPacket.data = NULL;
    mVideoPacket.size = 0;
    av_init_packet(&mVideoPacket);
    // YUV编码为H264
    int got_packet;
    int ret = avcodec_encode_video2(mVideoCodecContext, &mVideoPacket,
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

    return 0;
}

/**
 * aac编码
 */
int CainRecorder::aacEncode(jbyte *pcmData, int len) {
    // 如果还没有开始录制，则跳过编码
    if (recorderState != RECORDER_STARTED) {
        return 0;
    }
    // TODO 音频编码

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
        av_free(mVideoFrame);
    }
    // 释放音频码流
    if (mAudioStream) {
        avcodec_close(mAudioStream->codec);
        av_free(mAudioStream);
    }
    // 关闭io
    avio_close(mFormatCtx->pb);
    // 释放上下文
    avformat_free_context(mFormatCtx);
}