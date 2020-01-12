//
// Created by CainHuang on 2020-01-09.
//

#include "NdkAudioEncoder.h"

NdkAudioEncoder::NdkAudioEncoder(const std::shared_ptr<NdkMediaCodecMuxer> &mediaMuxer) : NdkMediaEncoder(mediaMuxer) {
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

    ssize_t encodeStatus;
    while (encodeStatus != AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
        AMediaCodecBufferInfo bufferInfo;
        encodeStatus = AMediaCodec_dequeueOutputBuffer(mMediaCodec, &bufferInfo, 0);
        if (encodeStatus >= 0) {
            uint8_t *encodeData = AMediaCodec_getOutputBuffer(mMediaCodec, encodeStatus, nullptr/* out_size */);
            if (encodeData) {
                if ((bufferInfo.flags & AMEDIACODEC_BUFFER_FLAG_CODEC_CONFIG) != 0
                    && bufferInfo.size != 0) {
                    AMediaCodec_releaseOutputBuffer(mMediaCodec, encodeStatus, false);
                } else {
                    // 将数据包写入复用器中
                    auto mediaMuxer = mWeakMuxer.lock();
                    if (mediaMuxer != nullptr && mediaMuxer->isStart()) {
                        mediaMuxer->writeFrame(mStreamIndex, encodeData, &bufferInfo);
                    }
                    *gotFrame = 1;
                    AMediaCodec_releaseOutputBuffer(mMediaCodec, encodeStatus, false);
                }
            } else {
                *gotFrame = 0;
                AMediaCodec_releaseOutputBuffer(mMediaCodec, encodeStatus, false);
            }
        } else if (encodeStatus == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
            auto mediaMuxer = mWeakMuxer.lock();
            if (mediaMuxer != nullptr) {
                AMediaFormat *mediaFormat = AMediaCodec_getOutputFormat(mMediaCodec);
                mStreamIndex = mediaMuxer->addTrack(mediaFormat);
                mediaMuxer->start();
                LOGD("audio mStreamIndex: %d", mStreamIndex);
            }
        }
    }
    return 0;
}