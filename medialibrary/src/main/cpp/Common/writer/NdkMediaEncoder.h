//
// Created by CainHuang on 2020/1/11.
//

#ifndef NDKMEDIAENCODER_H
#define NDKMEDIAENCODER_H

#if defined(__ANDROID__)

#include <media/NdkMediaFormat.h>
#include <media/NdkMediaMuxer.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaError.h>
#include <map>

#include "MediaEncoder.h"
#include "NdkCodecProfileLevel.h"
#include "NdkMediaCodecMuxer.h"

class NdkMediaEncoder {
public:
    NdkMediaEncoder(std::shared_ptr<NdkMediaCodecMuxer> mediaMuxer);

    virtual ~NdkMediaEncoder();

    // 打开编码器
    virtual int openEncoder();

    // 关闭编码器
    virtual int closeEncoder();

    // 释放资源
    virtual void release();

    // 编码媒体数据
    int encodeMediaData(AVMediaData *mediaData);

    // 编码媒体数据
    virtual int encodeMediaData(AVMediaData *mediaData, int *gotFrame);

protected:
    std::weak_ptr<NdkMediaCodecMuxer> mWeakMuxer; // 复用器
    AMediaCodec *mMediaCodec;   // 编码器
    int mStreamIndex;           // 媒体流索引
    long mDuration;             // 时长
};

#endif /* defined(__ANDROID__) */

#endif //NDKMEDIAENCODER_H
