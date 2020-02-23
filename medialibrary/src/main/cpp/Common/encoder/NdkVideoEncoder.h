//
// Created by CainHuang on 2020-01-09.
//

#ifndef NDKVIDEOENCODER_H
#define NDKVIDEOENCODER_H

#if defined(__ANDROID__)

#include <media/NdkMediaFormat.h>
#include <media/NdkMediaMuxer.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaError.h>

#include <map>
#include <AVFormatter.h>

#include "MediaEncoder.h"
#include "writer/NdkCodecProfileLevel.h"
#include "muxer/AVMediaMuxer.h"
#include "NdkMediaEncoder.h"

class NdkVideoEncoder : public NdkMediaEncoder {
public:
    NdkVideoEncoder(const std::shared_ptr<NdkMediaCodecMuxer> &mediaMuxer);

    virtual ~NdkVideoEncoder();

    // 设置参数
    void setVideoParams(int width, int height, int bitrate, int frameRate);

    // 准备编码器
    int openEncoder() override;

    // 关闭编码器
    int closeEncoder() override;

    // 释放资源
    void release() override;

    // 编码媒体数据
    int encodeMediaData(AVMediaData *mediaData, int *gotFrame) override;

protected:
    uint64_t calculatePresentationTime();

    // 计算时钟
    void calculateTimeUs(AMediaCodecBufferInfo bufferInfo);

private:
    std::weak_ptr<NdkMediaCodecMuxer> mWeakMuxer;
    AMediaCodec *mMediaCodec;   // 编码器
    long mStartTimeStamp;       // 上一帧时间戳
    long mDuration;             // 时长

    const char *mMimeType;      // 媒体类型，默认为video/avc，即mp4
    int mWidth;                 // 宽度
    int mHeight;                // 高度
    int mBitrate;               // 比特率
    int mFrameRate;             // 帧率
    int mFrameIndex;            // 帧索引
    double mPresentationTimeUs; // 当前时长

    // 设备信息
    int mSDKInt;                // SDK 版本号
    char* mPhoneType;           // 手机型号
    char* mCpu;                 // cpu型号

};

#endif /* defined(__ANDROID__) */

#endif //NDKVIDEOENCODER_H
