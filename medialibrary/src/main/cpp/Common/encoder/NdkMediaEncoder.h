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
#include "writer/NdkCodecProfileLevel.h"
#include "muxer/AVMediaMuxer.h"

class NdkMediaEncoder {
public:
    NdkMediaEncoder(std::shared_ptr<AVMediaMuxer> mediaMuxer);

    virtual ~NdkMediaEncoder();

    // 打开编码器
    virtual int openEncoder();

    // 关闭编码器
    virtual int closeEncoder();

    // 释放资源
    virtual void release();

    // 编码媒体数据
    int encodeMediaData(AVMediaData *mediaData);

    // 编码一帧数据
    int encodeFrame(AVFrame *frame);

    // 编码媒体数据
    virtual int encodeMediaData(AVMediaData *mediaData, int *gotFrame) = 0;

    // 编码一帧数据
    virtual int encodeFrame(AVFrame *frame, int *gotFrame) = 0;

    // 将媒体数据送去编码
    virtual int sendFrame(AVMediaData *mediaData) = 0;

    // 接收编码后的数据包
    virtual int receiveEncodePacket(AVPacket *packet, int *gotFrame) = 0;

    //  将数据包写入文件中
    int writePacket(AVPacket *packet);

    // 获取媒体类型
    virtual AVMediaType getMediaType() = 0;

protected:
    std::weak_ptr<AVMediaMuxer> mWeakMuxer; // 复用器
    AVStream *pStream;          // 媒体流索引
    AMediaCodec *mMediaCodec;   // 编码器
    int mStreamIndex;           // 媒体流索引
    long mDuration;             // 时长
};

#endif /* defined(__ANDROID__) */

#endif //NDKMEDIAENCODER_H
