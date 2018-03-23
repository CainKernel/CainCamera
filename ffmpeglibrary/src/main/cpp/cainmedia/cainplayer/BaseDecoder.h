//
// Created by Administrator on 2018/3/21.
//

#ifndef CAINCAMERA_BASEDECODER_H
#define CAINCAMERA_BASEDECODER_H



#ifdef __cplusplus
extern "C" {
#endif

#include "libavformat/avformat.h"

#ifdef __cplusplus
};
#endif

#include <vector>
#include "Mutex.h"
#include "Thread.h"
#include "PacketQueue.h"
#include "FrameQueue.h"

class BaseDecoder {

public:
    BaseDecoder();
    virtual ~BaseDecoder();
    // 设置媒体流
    void setAVStream(AVStream *stream, int streamIndex);
    // 打开媒体流
    int openStream();
    // 刷出剩余裸数据
    void packetFlush();
    // 裸数据包入队
    void put(AVPacket *packet);
    // 存放一个空的裸数据
    void putNullPacket(int streamIndex);
    // 开始解码
    void start();
    // 暂停解码
    void pause();
    // 停止解码
    void stop();
    // 裸数据队列包大小
    int packetSize();
    // 媒体流索引
    int getStreamIndex();
    // 通知解除条件锁
    void notify();
    // 解码
    virtual void decodeFrame() = 0;

private:
    // 解码线程
    static int decodeThread(void *arg);

protected:
    PacketQueue *mPacketQueue;  // 裸数据队列
    FrameQueue *mFrameQueue;    // 帧队列
    int mStreamIndex;           // 媒体流索引
    AVStream *mStream;          // 媒体流
    AVCodecContext *mCodecCtx;  // 解码上下文
    AVCodec *mCodec;            // 解码器
    Mutex *mMutex;              // 互斥锁
    Cond  *mCondition;          // 条件锁
    Thread *mThread;            // 解码线程
    bool mPrepared;             // 是否已经准备好
    bool mOpenSuccess;          // 媒体流是否成功打开
    bool mAbortRequest;         // 是否停止解码
    bool mPaused;               // 是否暂停解码
    bool mPacketPending;        // 是否处于挂起状态
    AVPacket packet;            // 裸数据包
    AVPacket pkt_temp;          // 缓存的裸数据包

};


#endif //CAINCAMERA_BASEDECODER_H
