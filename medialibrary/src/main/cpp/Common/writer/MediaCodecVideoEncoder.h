//
// Created by CainHuang on 2020-01-02.
//

#ifndef MEDIACODECVIDEOENCODER_H
#define MEDIACODECVIDEOENCODER_H

#if defined(__ANDROID__)

#include <AVMediaHeader.h>
#include <AVMediaData.h>

#include <media/NdkMediaFormat.h>
#include <media/NdkMediaMuxer.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaError.h>

#include "MediaCodecEncoder.h"

#define VIDEO_MIME_TYPE "video/avc"
#define TIMEOUT 10000

/** Constant quality mode */
#define BITRATE_MODE_CQ 0
/** Variable bitrate mode */
#define BITRATE_MODE_VBR 1
/** Constant bitrate mode */
#define BITRATE_MODE_CBR 2


/**
 * 视频编码器
 */
class MediaCodecVideoEncoder : public MediaCodecEncoder {

public:
    MediaCodecVideoEncoder(int width, int height, int bitrate, int frameRate);

    // 设置输出路径
    void setOutputPath(const char *path) override;

    // 准备编码器
    int prepare() override;

    // 关闭编码器
    int closeEncoder() override;

    // 释放所有资源
    void release() override;

    // 编码一帧数据
    void encode(AVMediaData *data) override;

    // 编码一帧数据
    void drainEncoder(bool eof);

private:
    void calculateTimeUs(AMediaCodecBufferInfo bufferInfo);

private:
    AMediaFormat *mMediaFormat;
    AMediaCodec *mMediaCodec;
    AMediaMuxer *mMediaMuxer;
    ssize_t mVideoTrackId;
    bool mMuxerStarted;
    long mStartTimeStamp;
    long mDuration;

    const char *mOutputPath;
    int mFd;
    int mWidth;
    int mHeight;
    int mBitrate;
    int mFrameRate;
};

#endif /* defined(__ANDROID__) */

#endif //MEDIACODECVIDEOENCODER_H
