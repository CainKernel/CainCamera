//
// Created by CainHuang on 2019/9/15.
//

#ifndef WRITER_H
#define WRITER_H

#include "../AVMediaHeader.h"
#include "../AVMediaData.h"

class MediaWriter {
public:
    // 设置输出文件
    virtual void setOutputPath(const char *dstUrl) = 0;

    // 设置是否使用时间戳形式
    virtual void setUseTimeStamp(bool use) = 0;

    // 设置最大比特率
    virtual void setMaxBitRate(int maxBitRate) = 0;

    // 设置视频输出参数
    virtual void setOutputVideo(int width, int height, int frameRate, AVPixelFormat pixelFormat) = 0;

    // 设置音频输出参数
    virtual void setOutputAudio(int sampleRate, int channels, AVSampleFormat sampleFormat) = 0;

    // 准备编码器
    virtual int prepare() = 0;

    // 编码媒体数据
    virtual int encodeMediaData(AVMediaData *mediaData) = 0;

    // 编码媒体数据
    virtual int encodeMediaData(AVMediaData *mediaData, int *gotFrame) = 0;

    // 编码一帧数据
    virtual int encodeFrame(AVFrame *frame, AVMediaType type) = 0;

    // 编码一帧数据
    virtual int encodeFrame(AVFrame *frame, AVMediaType type, int *gotFrame) = 0;

    // 停止写入
    virtual int stop() = 0;

    // 释放资源
    virtual void release() = 0;

};


#endif //WRITER_H
