//
// Created by CainHuang on 2020-01-02.
//

#include <memory.h>
#include "MediaCodecVideoEncoder.h"


MediaCodecVideoEncoder::MediaCodecVideoEncoder(int width, int height, int bitrate, int frameRate) {
    mWidth = width;
    mHeight = height;
    mBitrate = bitrate;
    mFrameRate = frameRate;
}

/**
 * 设置输出路径
 * @param path
 */
void MediaCodecVideoEncoder::setOutputPath(const char *path) {
    mOutputPath = strdup(path);
}

/**
 * 释放所有资源
 */
void MediaCodecVideoEncoder::release() {
    if (mMediaCodec != nullptr) {
        AMediaCodec_stop(mMediaCodec);
        AMediaCodec_delete(mMediaCodec);
        mMediaCodec = nullptr;
    }
    if (mMediaMuxer != nullptr) {
        if (mMuxerStarted) {
            AMediaMuxer_stop(mMediaMuxer);
        }
        AMediaMuxer_delete(mMediaMuxer);
        mMediaMuxer = nullptr;
    }
    if (mMediaFormat != nullptr) {
        AMediaFormat_delete(mMediaFormat);
        mMediaFormat = nullptr;
    }
}

/**
 * 准备编码器
 * @return
 */
int MediaCodecVideoEncoder::prepare() {
    mMediaCodec = AMediaCodec_createEncoderByType(VIDEO_MIME_TYPE);
    mMediaFormat = AMediaFormat_new();
    AMediaFormat_setString(mMediaFormat, AMEDIAFORMAT_KEY_MIME, VIDEO_MIME_TYPE);
    AMediaFormat_setInt32(mMediaFormat, AMEDIAFORMAT_KEY_WIDTH, mWidth);
    AMediaFormat_setInt32(mMediaFormat, AMEDIAFORMAT_KEY_HEIGHT, mHeight);
    AMediaFormat_setInt32(mMediaFormat, AMEDIAFORMAT_KEY_BIT_RATE, mBitrate);
    AMediaFormat_setInt32(mMediaFormat, "max-bitrate", mBitrate * 2);
    AMediaFormat_setInt32(mMediaFormat, "bitrate-mode", BITRATE_MODE_CBR);
    AMediaFormat_setInt32(mMediaFormat, AMEDIAFORMAT_KEY_FRAME_RATE, mFrameRate);
    AMediaFormat_setInt32(mMediaFormat, AMEDIAFORMAT_KEY_COLOR_FORMAT, 0x7F000789); // COLOR_FormatSurface
    AMediaFormat_setInt32(mMediaFormat, AMEDIAFORMAT_KEY_I_FRAME_INTERVAL, 1);  // GOP 1s
    int profile = 0;
    int level = 0;
    if (!strcmp("video/avc", VIDEO_MIME_TYPE)) {
        profile = 0x08; // AVCProfileHigh
        level = 0x100;  // AVCLevel30
        if (mWidth * mHeight >= 1280 * 720) {
            level = 0x200; //AVCLevel31
        }
        if (mWidth * mHeight >= 1920 * 1080) {
            level = 0x800; // AVCLevel40
        }
    } else if (!strcmp("video/hevc", VIDEO_MIME_TYPE)) {
        profile = 0x01; // HEVCProfileMain
        level = 0x80;   // HEVCHighTierLevel30
        if (mWidth * mHeight >= 1280 * 720) {
            level = 0x200; // HEVCHighTierLevel31
        }
        if (mWidth * mHeight >= 1920 * 1080) {
            level = 0x800;  // HEVCHighTierLevel40
        }
    }
    AMediaFormat_setInt32(mMediaFormat, "profile", profile);
    AMediaFormat_setInt32(mMediaFormat, "level", level);

    // 配置编码器
    media_status_t ret = AMediaCodec_configure(mMediaCodec, mMediaFormat, nullptr, nullptr,
            AMEDIACODEC_CONFIGURE_FLAG_ENCODE);
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

    FILE *fp = fopen(mOutputPath, "wb");
    if (fp != nullptr) {
        mFd = fileno(fp);
    } else {
        mFd = -1;
        LOGE("open file error: %s", mOutputPath);
        return -1;
    }
    mMediaMuxer = AMediaMuxer_new(mFd, AMEDIAMUXER_OUTPUT_FORMAT_MPEG_4);
    mMuxerStarted = false;
    mVideoTrackId = -1;
    fclose(fp);
    return AMEDIA_OK;
}

/**
 * 关闭编码器
 * @return
 */
int MediaCodecVideoEncoder::closeEncoder() {
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
}

/**
 * 编码一帧数据
 * @param data
 */
void MediaCodecVideoEncoder::encode(AVMediaData *data) {

}

/**
 * 编码一帧数据
 * @param eof
 */
void MediaCodecVideoEncoder::drainEncoder(bool eof) {
//    // API >= 26
//    if (eof) {
//        ssize_t ret = AMediaCodec_signalEndOfInputStream(mMediaCodec);
//    }

    while (true) {
        AMediaCodecBufferInfo bufferInfo;
        int encodeStatus = AMediaCodec_dequeueOutputBuffer(mMediaCodec, &bufferInfo, TIMEOUT);
        if (encodeStatus == AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
            if (!eof) {
                break;
            } else {
                LOGD("no output available, spinning to await EOS");
            }
        } else if (encodeStatus == AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED) {

        } else if (encodeStatus == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
            assert(!mMuxerStarted);
            if (mMuxerStarted) {
                LOGW("format changed twice");
            }
            if (mMediaFormat != nullptr) {
                AMediaFormat_delete(mMediaFormat);
                mMediaFormat = nullptr;
            }
            mMediaFormat = AMediaCodec_getOutputFormat(mMediaCodec);
            LOGD("encoder output format changed: %s", AMediaFormat_toString(mMediaFormat));
            mVideoTrackId = AMediaMuxer_addTrack(mMediaMuxer, mMediaFormat);
            AMediaMuxer_start(mMediaMuxer);
            mMuxerStarted = true;
        } else if (encodeStatus < 0) {
            LOGW("unexpected result from encoder.dequeueOutputBuffer: %d", encodeStatus);
        } else {
            uint8 *encodeData = AMediaCodec_getOutputBuffer(mMediaCodec, encodeStatus, nullptr/* out_size*/);
            assert(encodeData != nullptr);

            if ((bufferInfo.flags & AMEDIACODEC_BUFFER_FLAG_CODEC_CONFIG) != 0) {
                LOGD("ignoring BUFFER_FLAG_CODEC_CONFIG");
                bufferInfo.size = 0;
            }
            if (bufferInfo.size != 0) {
                assert(mMuxerStarted);
                // 计算编码时钟
                calculateTimeUs(bufferInfo);
                // 将编码数据写入复用器中
                AMediaMuxer_writeSampleData(mMediaMuxer, mVideoTrackId, encodeData, &bufferInfo);
                LOGD("sent %d bytes to muxer, ts= %f", bufferInfo.size, bufferInfo.presentationTimeUs);

            }

            AMediaCodec_releaseOutputBuffer(mMediaCodec, encodeStatus, false);

            if ((bufferInfo.flags & AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM) != 0) {
                if (!eof) {
                    LOGW("reached end of stream unexpectedly");
                } else {
                    LOGD("end of stream reached");
                }
                break; // out of while
            }
        }
    }
}

/**
 * 计算编码时间戳
 * @param bufferInfo
 */
void MediaCodecVideoEncoder::calculateTimeUs(AMediaCodecBufferInfo bufferInfo) {
    if (mStartTimeStamp == 0) {
        mStartTimeStamp = bufferInfo.presentationTimeUs;
    } else {
        mDuration = bufferInfo.presentationTimeUs - mStartTimeStamp;
    }
}