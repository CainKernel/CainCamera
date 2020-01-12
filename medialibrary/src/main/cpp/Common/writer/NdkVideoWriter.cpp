//
// Created by CainHuang on 2020-01-02.
//

#include <memory.h>
#include "NdkVideoWriter.h"
#include <sys/system_properties.h>

NdkVideoWriter::NdkVideoWriter(int width, int height, int bitrate, int frameRate) {
    mWidth = width;
    mHeight = height;
    mBitrate = bitrate;
    mFrameRate = frameRate;
    mMimeType = VIDEO_MIME_AVC;
    mSps = nullptr;
    mSpsLength = 0;
    mPps = nullptr;
    mPpsLength = 0;
    mStartCode = "\0\0\0\1";
    mStartCodeLength = 4;
    mFirstIFrame = false;

    // SDK 版本号
    char sdk[10] = {0};
    __system_property_get("ro.build.version.sdk",sdk);
    mSDKInt = atoi(sdk);

    // 设备型号
    char phoneType[20] = {0};
    __system_property_get("ro.product.model",phoneType);
    mPhoneType = (char*)malloc(20);
    memmove(mPhoneType, phoneType, 20);

    // 获取CPU型号
    char cpu[20] = {0};
    __system_property_get("ro.hardware",cpu);
    mCpu = (char*)malloc(20);
    memmove(mCpu, cpu, 20);

    mListener = nullptr;
    LOGD("current devices message: phone: %s, cpu:%s, sdk version: %d", mPhoneType, mCpu, mSDKInt);
}

NdkVideoWriter::~NdkVideoWriter() {
    release();
}

void NdkVideoWriter::setOnEncodingListener(OnEncodingListener *listener) {
    mListener = listener;
}

/**
 * 设置输出路径
 * @param path
 */
void NdkVideoWriter::setOutputPath(const char *path) {
    mOutputPath = strdup(path);
}

/**
 * 释放所有资源
 */
void NdkVideoWriter::release() {
    if (mMediaCodec != nullptr) {
        AMediaCodec_stop(mMediaCodec);
        AMediaCodec_delete(mMediaCodec);
        mMediaCodec = nullptr;
    }
    if (mMediaMuxer != nullptr) {
        delete mMediaMuxer;
        mMediaMuxer = nullptr;
    }
    if (mSps) {
        free(mSps);
        mSps = nullptr;
    }
    if (mPps) {
        free(mPps);
        mPps = nullptr;
    }
    if (mPhoneType) {
        free(mPhoneType);
        mPhoneType = nullptr;
    }
    if (mCpu) {
        free(mCpu);
        mCpu = nullptr;
    }
    mListener = nullptr;
}

/**
 * 准备编码器
 * @return
 */
int NdkVideoWriter::prepare() {
    mMediaCodec = AMediaCodec_createEncoderByType(mMimeType);
    AMediaFormat *mediaFormat = AMediaFormat_new();
    AMediaFormat_setString(mediaFormat, AMEDIAFORMAT_KEY_MIME, mMimeType);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_WIDTH, mWidth);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_HEIGHT, mHeight);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_BIT_RATE, mBitrate);
    AMediaFormat_setInt32(mediaFormat, "max-bitrate", mBitrate * 2);
    AMediaFormat_setInt32(mediaFormat, "bitrate-mode", BITRATE_MODE_CBR);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_FRAME_RATE, mFrameRate);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_COLOR_FORMAT, COLOR_FormatSurface);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_I_FRAME_INTERVAL, 1);  // GOP 1s
    int profile = 0;
    int level = 0;
    if (!strcmp(VIDEO_MIME_AVC, mMimeType)) {
        profile = AVCProfileHigh;
        level = AVCLevel31;
        if (mWidth * mHeight >= 1920 * 1080) {
            level = AVCLevel4;
        }
    } else if (!strcmp(VIDEO_MIME_HEVC, mMimeType)) {
        profile = HEVCProfileMain;
        level = HEVCHighTierLevel31;
        if (mWidth * mHeight >= 1920 * 1080) {
            level = HEVCHighTierLevel4;
        }
    }
    AMediaFormat_setInt32(mediaFormat, "profile", profile);
    AMediaFormat_setInt32(mediaFormat, "level", level);

    // 配置编码器
    media_status_t ret = AMediaCodec_configure(mMediaCodec, mediaFormat, nullptr, nullptr,
            AMEDIACODEC_CONFIGURE_FLAG_ENCODE);

    // 释放AMediaFormat对象
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

    // 打开输出文件
    int mFd;
    FILE *fp = fopen(mOutputPath, "wb");
    if (fp != nullptr) {
        mFd = fileno(fp);
    } else {
        LOGE("open file error: %s", mOutputPath);
        return -1;
    }

    // 创建MediaMuxer合成器
    mMediaMuxer = AMediaMuxer_new(mFd, AMEDIAMUXER_OUTPUT_FORMAT_MPEG_4);
    mMuxerStarted = false;
    mVideoTrackId = -1;

    // 关闭文件
    fclose(fp);

    // 准备参数
    mFirstIFrame = false;
    mFrameIndex = 0;

    return AMEDIA_OK;
}

/**
 * 关闭编码器
 * @return
 */
int NdkVideoWriter::closeEncoder() {
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
    mFrameIndex = 0;
    return 0;
}

/**
 * 编码一帧数据
 * @param data
 */
void NdkVideoWriter::encode(AVMediaData *data) {
    if (data->getType() != MediaVideo || data->image == nullptr || data->length <= 0) {
        return;
    }

    int length = data->length;
    ssize_t inputIndex = AMediaCodec_dequeueInputBuffer(mMediaCodec, -1);
    size_t inputSize = 0;
    if (inputIndex >= 0) {
        uint8_t* inputBuffer = AMediaCodec_getInputBuffer(mMediaCodec, inputIndex, &inputSize);
        if (inputBuffer != nullptr && inputSize >= length) {
            memmove(inputBuffer, data->image, length);
            AMediaCodec_queueInputBuffer(mMediaCodec, inputIndex, 0, length, calculatePresentationTime(), 0);
        } else {
            LOGE("ERROR_CODE_INPUT_BUFFER_FAILURE inputSize/data.length : %d/%d", inputIndex, length);
        }
    } else {
        LOGE("ERROR_CODE_INPUT_BUFFER_FAILURE inputIndex : %d", inputIndex);
    }

    AMediaCodecBufferInfo bufferInfo;
    ssize_t outputIndex = AMediaCodec_dequeueOutputBuffer(mMediaCodec, &bufferInfo, VIDEO_ENCODE_TIMEOUT);
    size_t outputSize = 0;
    LOGI("outputIndex : %d",outputIndex);
    if (outputIndex != AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
        if (outputIndex <= -20000) {
            LOGE("AMEDIA_DRM_ERROR_BASE");
            return;
        }
        if (outputIndex <= -10000) {
            LOGE("AMEDIA_ERROR_BASE");
            return;
        }

        if (outputIndex == AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED) {
            // outputBuffers = codec.getOutputBuffers();
        } else if (outputIndex == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {

            assert(!mMuxerStarted);
            if (mMuxerStarted) {
                LOGW("format changed twice");
            }
            AMediaFormat *mediaFormat = AMediaCodec_getOutputFormat(mMediaCodec);
            LOGD("encoder output format changed: %s", AMediaFormat_toString(mediaFormat));
            mVideoTrackId = AMediaMuxer_addTrack(mMediaMuxer, mediaFormat);
            AMediaMuxer_start(mMediaMuxer);
            mMuxerStarted = true;

        } else if (outputIndex >= 0) {
            LOGD("bufferInfo.size=%d bufferInfo.offset=%d", bufferInfo.size, bufferInfo.offset);
            uint8_t* outputBuffer = AMediaCodec_getOutputBuffer(mMediaCodec, outputIndex, &outputSize);
            if (outputBuffer) {
                if ((bufferInfo.flags & AMEDIACODEC_BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    LOGD("ignoring BUFFER_FLAG_CODEC_CONFIG");
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0) {
                    // 计算编码时钟
                    calculateTimeUs(bufferInfo);
                    // 将编码数据写入复用器中
                    AMediaMuxer_writeSampleData(mMediaMuxer, mVideoTrackId, outputBuffer, &bufferInfo);
                    LOGD("sent %d bytes to muxer, ts= %f", bufferInfo.size, bufferInfo.presentationTimeUs);
                    // 编码时长回调
                    if (mListener != nullptr) {
                        mListener->onEncoding(mDuration);
                    }
                }
            }
            // 释放编码缓冲区
            AMediaCodec_releaseOutputBuffer(mMediaCodec, outputIndex, false);
        }
    }
}

/**
 * 编码一帧数据
 * @param eof 结束标志
 */
void NdkVideoWriter::drainEncoder(bool eof) {
    while (true) {
        AMediaCodecBufferInfo bufferInfo;
        int encodeStatus = AMediaCodec_dequeueOutputBuffer(mMediaCodec, &bufferInfo, VIDEO_ENCODE_TIMEOUT);
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
            AMediaFormat *mediaFormat = AMediaCodec_getOutputFormat(mMediaCodec);
            LOGD("encoder output format changed: %s", AMediaFormat_toString(mediaFormat));
            mVideoTrackId = AMediaMuxer_addTrack(mMediaMuxer, mediaFormat);
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
                // 计算编码时钟
                calculateTimeUs(bufferInfo);
                // 将编码数据写入复用器中
                AMediaMuxer_writeSampleData(mMediaMuxer, mVideoTrackId, encodeData, &bufferInfo);
                LOGD("sent %d bytes to muxer, ts= %f", bufferInfo.size, bufferInfo.presentationTimeUs);

                // 录制时长回调
                if (mListener != nullptr) {
                    mListener->onEncoding(mDuration);
                }
            }

            // 释放编码数据
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
 * 刷新缓冲区
 */
void NdkVideoWriter::flush() {
    if (mMediaCodec != nullptr) {
        int status = AMediaCodec_flush(mMediaCodec);
        if (status != AMEDIA_OK) {
            LOGE("AMediaCodec_flush error");
        }
    } else {
        LOGD("media encoder is  null");
    }
}

/**
 * 计算编码时间戳
 * @param bufferInfo
 */
void NdkVideoWriter::calculateTimeUs(AMediaCodecBufferInfo bufferInfo) {
    if (mStartTimeStamp == 0) {
        mStartTimeStamp = bufferInfo.presentationTimeUs;
    } else {
        mDuration = bufferInfo.presentationTimeUs - mStartTimeStamp;
    }
}

/**
 * 计算时间戳
 * @return
 */
uint64_t NdkVideoWriter::calculatePresentationTime() {
    mFrameIndex++;
    return (uint64_t)(mFrameIndex * 1000000L / mFrameRate + 132L);
}

/**
 * 查找avc的起始码
 * @param data
 * @param offset
 * @param end
 * @return
 */
int NdkVideoWriter::avcFindStartCode(const uint8 *data, int offset, int end) {

    int a = offset + 4 - (offset & 3);

    for (end -= 3; offset < a && offset < end; offset++) {
        if (data[offset] == 0 && data[offset + 1] == 0 && data[offset + 2] == 1){
            return offset;
        }
    }

    for (end -= 3; offset < end; offset += 4) {
        int x = ((data[offset] << 8 | data[offset + 1]) << 8 | data[offset + 2]) << 8 | data[offset + 3];
        // if ((x - 0x01000100) & (~x) & 0x80008000) // little endian
        // if ((x - 0x00010001) & (~x) & 0x00800080) // big endian
        if (((x - 0x01010101) & (~x) & 0x80808080) != 0) { // generic
            if (data[offset + 1] == 0) {
                if (data[offset] == 0 && data[offset + 2] == 1)
                    return offset;
                if (data[offset + 2] == 0 && data[offset + 3] == 1)
                    return offset + 1;
            }
            if (data[offset + 3] == 0) {
                if (data[offset + 2] == 0 && data[offset + 4] == 1)
                    return offset + 2;
                if (data[offset + 4] == 0 && data[offset + 5] == 1)
                    return offset + 3;
            }
        }
    }

    for (end += 3; offset < end; offset++) {
        if (data[offset] == 0 && data[offset + 1] == 0 && data[offset + 2] == 1){
            return offset;
        }
    }

    return end + 3;
}

/**
 * 查找起始码
 * @param data
 * @param offset
 * @param end
 * @return
 */
int NdkVideoWriter::findStartCode(uint8 *data, int offset, int end) {
    int out = avcFindStartCode(data, offset, end);
    if (offset < out && out < end && data[out - 1] == 0){
        out--;
    }
    return out;
}
