//
// Created by CainHuang on 2020-01-02.
//

#include <memory.h>
#include "NdkAudioWriter.h"

NdkAudioWriter::NdkAudioWriter(int bitrate, int sampleRate, int channelCount) {
    mBitrate = bitrate;
    mSampleRate = sampleRate;
    mChannelCount = channelCount;
    mOutputPath = nullptr;
    mBufferSize = AUDIO_BUFFER_SIZE;
}

/**
 * 设置编码监听器
 * @param listener
 */
void NdkAudioWriter::setOnEncodingListener(OnEncodingListener *listener) {

}

/**
 * 设置输出路径
 * @param path
 */
void NdkAudioWriter::setOutputPath(const char *path) {
    mOutputPath = strdup(path);
}

/**
 * 设置缓冲区大小
 * @param size
 */
void NdkAudioWriter::setBufferSize(int size) {
    mBufferSize = size;
}

/**
 * 准备编码器
 * @return
 */
int NdkAudioWriter::prepare() {
    assert(mOutputPath != nullptr);
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
    AMediaFormat_delete(mediaFormat);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_configure error: %d", ret);
        return ret;
    }
    // 打开编码器
    ret = AMediaCodec_start(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_start error: %d", ret);
        return ret;
    }

    // 刷新缓冲区
    ret = AMediaCodec_flush(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_start error: %d", ret);
        return ret;
    }

    // 创建Muxer
    int mFd;
    FILE *fp = fopen(mOutputPath, "wb");
    if (fp != nullptr) {
        mFd = fileno(fp);
    } else {
        LOGE("open file error: %s", mOutputPath);
        return -1;
    }
    mMediaMuxer = AMediaMuxer_new(mFd, AMEDIAMUXER_OUTPUT_FORMAT_MPEG_4);
    mTotalBytesRead = 0;
    mPresentationTimeUs = 0;
    mMuxerStarted = false;
    mAudioTrackId = -1;
    fclose(fp);
    return AMEDIA_OK;
}

/**
 * 关闭编码器
 * @return
 */
int NdkAudioWriter::closeEncoder() {
    if (!mMediaCodec) {
        return 0;
    }
    int ret;
    ret = AMediaCodec_flush(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_flush error: %d", ret);
        return ret;
    }

    ret = AMediaCodec_stop(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_stop", ret);
        return ret;
    }

    ret = AMediaCodec_delete(mMediaCodec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_delete error: %d", ret);
        return ret;
    }
    mMediaCodec = nullptr;
    // 关闭复用器
    if (mMediaMuxer != nullptr) {
        if (mMuxerStarted) {
            AMediaMuxer_stop(mMediaMuxer);
            mMuxerStarted = false;
        }
        AMediaMuxer_delete(mMediaMuxer);
        mMediaMuxer = nullptr;
    }
    return 0;
}

/**
 * 释放数据
 */
void NdkAudioWriter::release() {
    closeEncoder();
}

/**
 * 编码一帧数据
 * @param data
 */
void NdkAudioWriter::encode(AVMediaData *data) {
    if (data->getType() != MediaAudio) {
        return;
    }
    ssize_t inputIndex = AMediaCodec_dequeueInputBuffer(mMediaCodec, -1);
    if (inputIndex >= 0) {
        size_t bufSize;
        uint8_t *buffer = AMediaCodec_getInputBuffer(mMediaCodec, inputIndex, &bufSize);
        memset(buffer, 0, bufSize);
        if (data->sample_size < 0) {
            AMediaCodec_queueInputBuffer(mMediaCodec, inputIndex, 0, 0, (uint64_t)mPresentationTimeUs, 0);
        } else {
            mTotalBytesRead += data->sample_size;
            memcpy(buffer, data->sample, data->sample_size);
            AMediaCodec_queueInputBuffer(mMediaCodec, inputIndex, 0, data->sample_size, (uint64_t)mPresentationTimeUs, 0);
            mPresentationTimeUs = 1000000L * 1.0 * (mTotalBytesRead / mChannelCount / 2) / mSampleRate;
            LOGD("encode pcm: presentationTimeUs: %f, s: %f", mPresentationTimeUs, (mPresentationTimeUs / 1000000.0));
        }
    }

    ssize_t encodeStatus;
    while (encodeStatus != AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
        AMediaCodecBufferInfo bufferInfo;
        encodeStatus = AMediaCodec_dequeueOutputBuffer(mMediaCodec, &bufferInfo, 0);
        if (encodeStatus >= 0) {
            uint8_t *encodeData = AMediaCodec_getOutputBuffer(mMediaCodec, encodeStatus, nullptr/* out_size */);
            assert(encodeData != nullptr);
            if ((bufferInfo.flags & AMEDIACODEC_BUFFER_FLAG_CODEC_CONFIG) != 0 && bufferInfo.size != 0) {
                AMediaCodec_releaseOutputBuffer(mMediaCodec, encodeStatus, false);
            } else {
                AMediaMuxer_writeSampleData(mMediaMuxer, mAudioTrackId, encodeData, &bufferInfo);
                AMediaCodec_releaseOutputBuffer(mMediaCodec, encodeStatus, false);
            }
        } else if (encodeStatus == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
            assert(!mMuxerStarted);
            AMediaFormat *mediaFormat = AMediaCodec_getOutputFormat(mMediaCodec);
            mAudioTrackId = (int)AMediaMuxer_addTrack(mMediaMuxer, mediaFormat);
            AMediaMuxer_start(mMediaMuxer);
            mMuxerStarted = true;
        }
    }
}