//
// Created by cain on 2017/12/27.
//

#include "h264_encoder.h"
#include "native_log.h"

AVCEncoder::AVCEncoder(EncoderParams *params) : MediaEncoder(params) {

}

/**
 * 结束编码是刷出还在编码器中的帧
 * @param fmt_ctx
 * @param stream_index
 * @return
 */
int AVCEncoder::flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index) {
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[stream_index]->codec->codec->capabilities &
          CODEC_CAP_DELAY)) {
        return 0;
    }

    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_video2(fmt_ctx->streams[stream_index]->codec,
                                    &enc_pkt, NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0) {
            break;
        }
        if (!got_frame) {
            ret = 0;
            break;
        }
        LOGI("flush encoder: success to encode 1 frame video!\t size:%5d", enc_pkt.size);

        // 写入到.h264文件中 TODO 发送给混合器
        ret = av_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0) {
            break;
        }
    }
    return ret;
}

/**
* 初始化编码器
* @return
*/
int AVCEncoder::init(EncoderMuxer *muxer) {
    mMuxer = muxer;
    if (mMuxer == NULL) {
        LOGE("error: Muxer is NULL, init failed!\n");
        return -1;
    }
    // 给复用器添加视频编码器
    // Error:(63) undefined reference to 'EncoderMuxer::addVideoEncoder(MediaEncoder*)'
    // 这里不能像Java那样相互引用调用方法的
//    muxer->addVideoEncoder(this);

    // 注册
    av_register_all();

    // 查找的编码器
    mCodec = avcodec_find_encoder(mCodecContext->codec_id);
    if (!mCodec) {
        LOGE("can not find encoder!\n");
        return -1;
    }

    // 创建新的视频流
    mMediaStream = avformat_new_stream(mMuxer->mFormatCtx, mCodec);
    if (mMediaStream == NULL) {
        LOGE("video_st is null!\n");
        return -1;
    }

    // 获取视频编码上下文并设置参数
    mCodecContext = mMediaStream->codec;
    mCodecContext->codec_id = AV_CODEC_ID_H264;
    mCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
    mCodecContext->pix_fmt = AV_PIX_FMT_YUV420P;

    mCodecContext->width = mEncoderParams->mVideoWidth;
    mCodecContext->height = mEncoderParams->mVideoHeight;

    mCodecContext->bit_rate = mEncoderParams->mBitRate;
    mCodecContext->gop_size = 30;
    mCodecContext->thread_count = 12;
    mCodecContext->time_base.num = 1;
    mCodecContext->time_base.den = mEncoderParams->mFrameRate;
    mCodecContext->qmin = 10;
    mCodecContext->qmax = 51;
    mCodecContext->max_b_frames = 0;

    AVDictionary *param = 0;
    if (mCodecContext->codec_id == AV_CODEC_ID_H264) {
        av_dict_set(&param, "tune", "zerolatency", 0);
        av_opt_set(mCodecContext->priv_data, "preset", "ultrafast", 0);
        av_dict_set(&param, "profile", "baseline", 0);
    }

    // 打开编码器
    if (avcodec_open2(mCodecContext, mCodec, &param) < 0) {
        LOGE("failed to open encoder!\n");
    }
    // 创建视频帧
    mFrame = av_frame_alloc();
    // 获取缓冲大小
    mBufferSize = avpicture_get_size(mCodecContext->pix_fmt, mCodecContext->width,
                                     mCodecContext->height);
    LOGI("picture buffer size: %d", mBufferSize);
    // 创建缓冲
    uint8_t *buffer = (uint8_t *) av_malloc(mBufferSize);
    avpicture_fill((AVPicture *) mFrame, buffer, mCodecContext->pix_fmt,
                   mCodecContext->width, mCodecContext->height);
    // 创建一个AVPacket包
    av_new_packet(&mAVPacket, mBufferSize);
    // 启动一个新线程
    pthread_t thread;
    pthread_create(&thread, NULL, AVCEncoder::startEncoder, this);
    return 0;
}

/**
* 开始编码线程
* @param obj
* @return
*/
void *AVCEncoder::startEncoder(void *obj) {
    AVCEncoder *avcEncoder = (AVCEncoder *) obj;
    while (!avcEncoder->isEnd || !avcEncoder->mFrameQueue.empty()) {
        // 释放资源 TODO 混合器
        if (avcEncoder->isRelease) {
            // 清除数据
            if (avcEncoder->mMediaStream) {
                avcodec_close(avcEncoder->mMediaStream->codec);
                av_free(avcEncoder->mFrame);
            }
            delete avcEncoder;
            return 0;
        }
        // 等待数据
        if (avcEncoder->mFrameQueue.empty()) {
            continue;
        }
        // 获取需要编码的数据
        uint8_t *pictureBuffer = *avcEncoder->mFrameQueue.wait_and_pop().get();
        // 设置PTS
        avcEncoder->mFrame->pts = avcEncoder->mFrameCount;
        avcEncoder->mFrameCount++;
        // 视频编码
        int ret = avcodec_encode_video2(avcEncoder->mCodecContext, &avcEncoder->mAVPacket,
                                        avcEncoder->mFrame, &avcEncoder->mGotFrame);
        if (ret < 0) {
            LOGE("fialed to encode !\n");
        }
        // 写入数据
        if (avcEncoder->mGotFrame == 1) {
            avcEncoder->mAVPacket.stream_index = avcEncoder->mMediaStream->index;
            // 使用复用器写入视频帧
            int ret = av_interleaved_write_frame(avcEncoder->mMuxer->mFormatCtx,
                                                 &avcEncoder->mAVPacket);
            if (ret < 0) {
                LOGE("av_interleaved_write_frame() "
                             "error: %d while writing interleaved audio frame", ret);
            }
            // 释放AVPacket
            av_free_packet(&avcEncoder->mAVPacket);
        }
        // 释放缓冲
        delete (pictureBuffer);
    }
    // 结束录制
    if (avcEncoder->isEnd) {
        avcEncoder->encoderEndian();
        delete avcEncoder;
    }
    return 0;
}


/**
* 结尾信息
* @return
*/
int AVCEncoder::encoderEndian() {
    int ret = flush_encoder(mMuxer->mFormatCtx, 0);
    if (ret < 0) {
        LOGE("flushing encoder failed!\n");
        return -1;
    }
    // 释放资源
    if (mMediaStream) {
        avcodec_close(mMediaStream->codec);
        av_free(mFrame);
    }
}