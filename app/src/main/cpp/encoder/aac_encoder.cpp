//
// Created by cain on 2017/12/31.
//

#include "aac_encoder.h"
#include "native_log.h"
#include "media_muxer.h"

AACEncoder::AACEncoder(EncoderParams *params) : MediaEncoder(params) {

}

/**
* 刷新剩余帧
* @param fmt_ctx
* @param stream_index
* @return
*/
int AACEncoder::flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index) {
    int result;
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
        result = avcodec_encode_audio2(fmt_ctx->streams[stream_index]->codec, &enc_pkt,
                                       NULL, &got_frame);
        av_frame_free(NULL);
        if (result < 0) {
            break;
        }
        if (!got_frame) {
            result = 0;
            break;
        }
        LOGI("Flush Encoder: Succeed to encode 1 frame!\tsize:%5d\n", enc_pkt.size);
        // 将编码后的AAC数据写入到.aac文件中 TODO 发送到混合队列
        result = av_write_frame(fmt_ctx, &enc_pkt);
        if (result < 0)
            break;
    }
    return result;
}


/**
* 初始化编码器
* @return
*/
int AACEncoder::init(EncoderMuxer * muxer) {
    mMuxer = muxer;
    if (mMuxer == NULL) {
        LOGE("error: Muxer is NULL, init failed!\n");
        return -1;
    }

    // 注册
    av_register_all();

    // 查找AAC编码器
    mCodec = avcodec_find_encoder(AV_CODEC_ID_AAC);
    if (!mCodec) {
        LOGE("can not find encoder!\n");
        return -1;
    }

    // 设置输出格式的音频编码器
    mMuxer->mOutputFormat->audio_codec = mCodec->id;

    // 创建音频码流
    mMediaStream = avformat_new_stream(mMuxer->mFormatCtx, mCodec);
    if (mMediaStream == NULL) {
        LOGE("avformat_new_stream() error: failed to create AVStream!\n");
        return -1;
    }
    // 获取并设置音频编码上下文
    mCodecContext = mMediaStream->codec;
    mCodecContext->codec_id = AV_CODEC_ID_AAC;
    mCodecContext->codec_type = AVMEDIA_TYPE_AUDIO;
    mCodecContext->sample_fmt = AV_SAMPLE_FMT_S16;
    mCodecContext->sample_rate = mEncoderParams->mAudioSampleRate;
    mCodecContext->channel_layout = AV_CH_LAYOUT_MONO;
    mCodecContext->channels = av_get_channel_layout_nb_channels(mCodecContext->channel_layout);
    mCodecContext->bit_rate = mEncoderParams->mAudioBitRate;
    // 获取声道
    int channel = av_get_channel_layout_nb_channels(mCodecContext->channel_layout);
    LOGI("channels:%d", channel);

    // 打开音频编码器
    int state = avcodec_open2(mCodecContext, mCodec, NULL);
    if (state < 0) {
        LOGE("failed to open encoder!---%d", state);
        return -1;
    }

    // 初始化编码帧
    mFrame = av_frame_alloc();
    mFrame->nb_samples = mCodecContext->frame_size;
    mFrame->format = mCodecContext->sample_fmt;
    // 设置采样的缓冲大小
    mBufferSize = av_samples_get_buffer_size(NULL, mCodecContext->channels,
                                             mCodecContext->frame_size,
                                             mCodecContext->sample_fmt, 1);

    // 创建缓冲区
    uint8_t *frame_buf = (uint8_t *) av_malloc(mBufferSize);
    // 填充音频缓冲数据
    avcodec_fill_audio_frame(mFrame, mCodecContext->channels, mCodecContext->sample_fmt,
                             (const uint8_t *) frame_buf, mBufferSize, 1);
    // 创建新的AVPacket
    av_new_packet(&mAVPacket, mBufferSize);
    isEnd = 0;
    isRelease = 0;
    // 创建新线程
    pthread_t thread;
    pthread_create(&thread, NULL, AACEncoder::startEncoder, this);
    return 0;
}

/**
* 编码结束操作
* @return
*/
int AACEncoder::encoderEndian() {
    int result = flush_encoder(mMuxer->mFormatCtx, 0);
    if (result < 0) {
        LOGE("flush encoder failed\n");
        return -1;
    }

    // 关闭编码器，释放视频帧
    if (mMediaStream) {
        avcodec_close(mMediaStream->codec);
        av_free(mFrame);
    }

    return 0;

}

/**
 * 线程函数
 * @param obj
 * @return
 */
void *AACEncoder::startEncoder(void *obj) {
    AACEncoder *aacEncoder = (AACEncoder *) obj;
    while (!aacEncoder->isEnd || !aacEncoder->mFrameQueue.empty()) {

        // 是否释放
        if (aacEncoder->isRelease) {
            // 关闭编码器
            if (aacEncoder->mMediaStream) {
                avcodec_close(aacEncoder->mMediaStream->codec);
                av_free(aacEncoder->mFrame);
            }
            delete aacEncoder;
            return 0;
        }
        // 等待音频输入
        if (aacEncoder->mFrameQueue.empty()) {
            continue;
        }
        // 编码数据缓冲
        uint8_t *frame_buf = *aacEncoder->mFrameQueue.wait_and_pop().get();
        // 编码数据
        aacEncoder->mFrame->data[0] = frame_buf;
        // 写入PTS（这里可能跟Video那边不同步）
        aacEncoder->mFrame->pts = aacEncoder->mFrameCount;
        aacEncoder->mFrameCount++;
        aacEncoder->mGotFrame = 0;
        // PCM编码成
        int result = avcodec_encode_audio2(aacEncoder->mCodecContext, &aacEncoder->mAVPacket,
                                           aacEncoder->mFrame, &aacEncoder->mGotFrame);
        // 判断是否编码成功
        if (result < 0) {
            LOGE("failed to encode!\n");
        }
        // 完成一帧编码
        if (aacEncoder->mGotFrame == 1) {
            aacEncoder->mAVPacket.stream_index = aacEncoder->mMediaStream->index;
            // 将编码后的音频帧写入文件中
            int ret = av_interleaved_write_frame(aacEncoder->mMuxer->mFormatCtx,
                                                 &aacEncoder->mAVPacket);
            if (ret < 0) {
                LOGE("av_interleaved_write_frame() "
                             "error: %d while writing interleaved audio frame", ret);
            }
            // 释放资源
            av_free_packet(&aacEncoder->mAVPacket);
        }
        // 删除缓存
        delete (frame_buf);
    }
    // 结束编码
    if (aacEncoder->isEnd) {
        aacEncoder->encoderEndian();
        delete aacEncoder;
    }
    return 0;
}