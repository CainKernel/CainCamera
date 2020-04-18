//
// Created by CainHuang on 2020-01-09.
//

#include "NdkAudioEncoder.h"

NdkAudioEncoder::NdkAudioEncoder(const std::shared_ptr<AVMediaMuxer> &mediaMuxer) : NdkMediaEncoder(mediaMuxer) {
    mBufferSize = AUDIO_BUFFER_SIZE;
}

NdkAudioEncoder::~NdkAudioEncoder() {
    release();
}

/**
 * 设置音频参数
 * @param bitrate
 * @param sampleRate
 * @param channelCount
 */
void NdkAudioEncoder::setAudioParams(int bitrate, int sampleRate, int channelCount) {
    mBitrate = bitrate;
    mSampleRate = sampleRate;
    mChannelCount = channelCount;
}

/**
 * 设置缓冲区大小
 * @param size
 */
void NdkAudioEncoder::setBufferSize(int size) {
    mBufferSize = size;
}

/**
 * 准备编码器
 * @return
 */
int NdkAudioEncoder::openEncoder() {
    int ret;
    AMediaFormat *mediaFormat = AMediaFormat_new();
    AMediaFormat_setString(mediaFormat, AMEDIAFORMAT_KEY_MIME, AUDIO_MIME_TYPE);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_SAMPLE_RATE, mSampleRate);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_CHANNEL_COUNT, mChannelCount);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_AAC_PROFILE, AACObjectLC);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_BIT_RATE, mBitrate);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_MAX_INPUT_SIZE, mBufferSize);

    mMediaCodec = AMediaCodec_createEncoderByType(AUDIO_MIME_TYPE);
    // 配置编码器
    ret = AMediaCodec_configure(mMediaCodec, mediaFormat, nullptr, nullptr, AMEDIACODEC_CONFIGURE_FLAG_ENCODE);
    // 释放配置对象
    AMediaFormat_delete(mediaFormat);
    if (ret != AMEDIA_OK) {
        LOGE("NdkAudioEncoder - AMediaCodec_configure error: %d", ret);
        return ret;
    }
    // 打开编码器
    ret = AMediaCodec_start(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("NdkAudioEncoder - AMediaCodec_start error: %d", ret);
        return ret;
    }

    // 刷新缓冲区
    ret = AMediaCodec_flush(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("NdkAudioEncoder - AMediaCodec_start error: %d", ret);
        return ret;
    }

    mTotalBytesRead = 0;
    mPresentationTimeUs = 0;

    // 创建媒体流
    auto mediaMuxer = mWeakMuxer.lock();
    if (mediaMuxer != nullptr) {
        pStream = mediaMuxer->createStream(AV_CODEC_ID_AAC);
    }
    if (pStream != nullptr) {
        mStreamIndex = pStream->index;
        pStream->time_base = (AVRational){1, mSampleRate};
        pStream->codecpar->format = AV_SAMPLE_FMT_S16;
        pStream->codecpar->sample_rate = mSampleRate;
        pStream->codecpar->channels = mChannelCount;
        pStream->codecpar->channel_layout = (uint64_t)av_get_default_channel_layout(mChannelCount);
        pStream->codecpar->bit_rate = mBitrate;
        pStream->codecpar->codec_type = AVMEDIA_TYPE_AUDIO;
        pStream->codecpar->codec_id = AV_CODEC_ID_AAC;
        pStream->codecpar->codec_tag = 0;
    }

    return AMEDIA_OK;
}

/**
 * 关闭编码器
 * @return
 */
int NdkAudioEncoder::closeEncoder() {
    if (!mMediaCodec) {
        return 0;
    }
    int ret;
    ret = AMediaCodec_flush(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("NdkAudioEncoder - AMediaCodec_flush error: %d", ret);
        return ret;
    }

    ret = AMediaCodec_stop(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("NdkAudioEncoder - AMediaCodec_stop", ret);
        return ret;
    }

    ret = AMediaCodec_delete(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("NdkAudioEncoder - AMediaCodec_delete error: %d", ret);
        return ret;
    }
    mMediaCodec = nullptr;
    return 0;
}

void NdkAudioEncoder::release() {
    NdkMediaEncoder::release();
    closeEncoder();
}

/**
 * 编码媒体数据
 * @param mediaData
 * @param gotFrame
 * @return
 */
int NdkAudioEncoder::encodeMediaData(AVMediaData *mediaData, int *gotFrame) {
    int ret = 0;
    if (!mediaData) {
        return 0;
    }
    int gotFrameLocal;
    if (!gotFrame) {
        gotFrame = &gotFrameLocal;
    }
    *gotFrame = 0;

    // 将媒体数据送去编码
    ret = sendFrame(mediaData);
    if (ret != 0) {
        return ret;
    }
    // 初始化一个数据包
    AVPacket packet;
    av_init_packet(&packet);
    // 接收编码后的数据包
    ret = receiveEncodePacket(&packet, gotFrame);
    if (ret >= 0 && *gotFrame == 1) {
        writePacket(&packet);
    }
    av_packet_unref(&packet);
    return 0;
}

/**
 * 编码一帧媒体数据
 * @param frame
 * @param gotFrame
 * @return
 */
int NdkAudioEncoder::encodeFrame(AVFrame *frame, int *gotFrame) {
    return 0;
}

/**
 * 将媒体数据送去编码
 * @param mediaData
 * @return
 */
int NdkAudioEncoder::sendFrame(AVMediaData *mediaData) {
    ssize_t inputIndex = AMediaCodec_dequeueInputBuffer(mMediaCodec, -1);
    if (inputIndex >= 0) {
        size_t bufSize;
        uint8_t *buffer = AMediaCodec_getInputBuffer(mMediaCodec, inputIndex, &bufSize);
        memset(buffer, 0, bufSize);
        if (mediaData->sample_size <= 0) {
            AMediaCodec_queueInputBuffer(mMediaCodec, inputIndex, 0, 0, (uint64_t)mPresentationTimeUs, 0);
        } else {
            mTotalBytesRead += mediaData->sample_size;
            memcpy(buffer, mediaData->sample, mediaData->sample_size);
            AMediaCodec_queueInputBuffer(mMediaCodec, inputIndex, 0, mediaData->sample_size, (uint64_t)mPresentationTimeUs, 0);
            mPresentationTimeUs = 1000000L * 1.0 * (mTotalBytesRead / mChannelCount / 2) / mSampleRate;
            LOGD("NdkAudioEncoder - encode pcm data: presentationTimeUs: %f, s: %f", mPresentationTimeUs, (mPresentationTimeUs / 1000000.0));
        }
    }
    return 0;
}

/**
 * 接收编码后的数据包
 * @param packet
 * @param gotFrame
 * @return
 */
int NdkAudioEncoder::receiveEncodePacket(AVPacket *packet, int *gotFrame) {
    int gotFrameLocal;
    if (!gotFrame) {
        gotFrame = &gotFrameLocal;
    }
    *gotFrame = 0;
    ssize_t encodeStatus = 0;
    while (encodeStatus != AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
        AMediaCodecBufferInfo bufferInfo;
        encodeStatus = AMediaCodec_dequeueOutputBuffer(mMediaCodec, &bufferInfo, 0);
        if (encodeStatus >= 0) {
            uint8_t *encodeData = AMediaCodec_getOutputBuffer(mMediaCodec, encodeStatus, nullptr/* out_size */);
            if (encodeData) {
                if ((bufferInfo.flags & AMEDIACODEC_BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // 创建并配置FFmpeg媒体流信息
                    if (pStream != nullptr) {
                        if (bufferInfo.size > 0) {
                            pStream->codecpar->extradata = (uint8_t *) av_mallocz(
                                    (size_t) (bufferInfo.size + FF_INPUT_BUFFER_PADDING_SIZE));
                            memcpy(pStream->codecpar->extradata, encodeData,
                                   (size_t) bufferInfo.size);
                            pStream->codecpar->extradata_size = bufferInfo.size;
                        }
                    }
                } else {
                    // 将编码数据写入复用器中
                    if (packet != nullptr && mStreamIndex >= 0) {
                        packet->stream_index = mStreamIndex;
                        packet->data = encodeData;
                        packet->size = bufferInfo.size;
                        packet->pts = rescalePts(bufferInfo.presentationTimeUs,
                                                (AVRational) {1, mSampleRate});
                        packet->dts = packet->pts;
                        packet->pos = -1;
                        // 计算编码后的pts
                        av_packet_rescale_ts(packet, (AVRational){1, mSampleRate}, pStream->time_base);
                    }
                    *gotFrame = 1;
                }
            } else {
                *gotFrame = 0;
            }
            AMediaCodec_releaseOutputBuffer(mMediaCodec, encodeStatus, false);
        } else if (encodeStatus == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
            LOGD("AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED");
        }
    }
    return 0;
}

AVMediaType NdkAudioEncoder::getMediaType() {
    return AVMEDIA_TYPE_AUDIO;
}
