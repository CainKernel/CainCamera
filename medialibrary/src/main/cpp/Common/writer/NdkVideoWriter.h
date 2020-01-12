//
// Created by CainHuang on 2020-01-02.
//

#ifndef NDKVIDEOWRITER_H
#define NDKVIDEOWRITER_H

#if defined(__ANDROID__)

#include <media/NdkMediaFormat.h>
#include <media/NdkMediaMuxer.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaError.h>

#include "MediaEncoder.h"
#include "NdkCodecProfileLevel.h"

/**
 * 视频编码器
 */
class NdkVideoWriter : public MediaEncoder {

public:
    NdkVideoWriter(int width, int height, int bitrate, int frameRate);

    virtual ~NdkVideoWriter();

    // 设置编码监听器
    void setOnEncodingListener(OnEncodingListener *listener) override;

    // 设置输出路径
    void setOutputPath(const char *path) override;

    // 准备编码器
    int prepare() override;

    // 关闭编码器
    int closeEncoder() override;

    // 释放所有资源
    void release() override;

    // 编码一帧数据(byte编码)
    void encode(AVMediaData *data) override;

    // 编码一帧数据(Surface编码的)
    void drainEncoder(bool eof);

    // 刷新缓冲区
    void flush();

private:
    // 计算时间戳
    void calculateTimeUs(AMediaCodecBufferInfo bufferInfo);

    // 计算当前时长
    uint64_t calculatePresentationTime();

    // 查找起始码
    int avcFindStartCode(const uint8 *data, int offset, int end);

    // 查找起始码
    int findStartCode(uint8 *data, int offset, int end);
private:
    AMediaCodec *mMediaCodec;   // 编码器
    AMediaMuxer *mMediaMuxer;   // 复用器
    ssize_t mVideoTrackId;      // 视频轨道
    bool mMuxerStarted;         // 复用器是否已经开启
    long mStartTimeStamp;       // 上一帧时间戳
    long mDuration;             // 时长

    const char *mMimeType;      // 媒体类型，默认为video/avc，即mp4
    const char *mOutputPath;    // 编码输出路径
    int mWidth;                 // 宽度
    int mHeight;                // 高度
    int mBitrate;               // 比特率
    int mFrameRate;             // 帧率
    int mFrameIndex;            // 帧索引

    // sps和pps相关信息
    uint8_t* mSps;              // sps
    int32_t mSpsLength;         // sps长度
    uint8_t* mPps;              // pps
    int32_t mPpsLength;         // pps长度

    char* mStartCode;           // 起始码
    int32_t mStartCodeLength;   // 起始码长度
    bool mFirstIFrame;          // 是否首个关键帧

    // 设备信息
    int mSDKInt;                // SDK 版本号
    char* mPhoneType;           // 手机型号
    char* mCpu;                 // cpu型号

    OnEncodingListener *mListener; // 编码监听器，需要用户手动删除
};

#endif /* defined(__ANDROID__) */

#endif //NDKVIDEOWRITER_H
