//
// Created by admin on 2018/4/29.
//

#ifndef CAINPLAYER_AVDECODER_H
#define CAINPLAYER_AVDECODER_H

#include "../common/MediaJniCall.h"
#include "../common/MediaStatus.h"
#include "../common/MediaQueue.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <libavcodec/avcodec.h>

#ifdef __cplusplus
};
#endif

class AVDecoder {
public:
    AVDecoder(MediaStatus *status, MediaJniCall *jniCall);

    virtual ~AVDecoder();

    MediaQueue *getQueue();

    // 设置时钟
    virtual void setClock(int secds);

    // 获取时钟
    virtual int getClock();

    // 获取媒体流索引
    int getStreamIndex() const;

    // 设置媒体流索引
    void setStreamIndex(int streamIndex);

    // 获取总时长
    int getDuration() const;

    // 设置总时长
    void setDuration(int duration);

    // 获取当前时长
    double getCurrent() const;

    // 设置当前时长
    void setCurrent(double current);

    // 获取解码上下文
    AVCodecContext *getCodecContext() const;

    // 设置解码上下文
    void setCodecContext(AVCodecContext *context);

    // 获取时钟基准
    const AVRational &getTimeBase() const;

    // 设置时钟基准
    void setTimeBase(const AVRational &timebase);

    // 清空帧队列
    void clearFrame();

    // 清空裸数据包队列
    void clearPacket();

    // 入队裸数据包
    int putPacket(AVPacket *pkt);

    // 出列裸数据包
    int getPacket(AVPacket *pkt);

    // 获取裸数据包队列大小
    int getPacketSize();

    // 获取帧队列大小
    int getFrameSize();

protected:
    MediaQueue *queue;              // 数据包队列
    MediaStatus *mediaStatus;       // 播放器状态
    MediaJniCall *mediaJniCall;     // jni回调
    int streamIndex;                // 媒体流索引
    int duration;                   // 总时长
    double clock;                   // 时钟
    double current;                 // 当前时间
    AVCodecContext *pCodecContext;  // 解码上下文
    AVRational timeBase;            // 媒体流时钟基准
};

#endif //CAINPLAYER_AVDECODER_H
