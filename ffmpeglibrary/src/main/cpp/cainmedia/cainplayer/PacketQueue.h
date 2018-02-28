//
// Created by cain on 2018/2/22.
//

#ifndef CAINCAMERA_PACKETQUEUE_H
#define CAINCAMERA_PACKETQUEUE_H

#include <pthread.h>

#ifdef __cplusplus
extern "C" {
#endif

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"

#ifdef __cplusplus
};
#endif

// 包结构体
typedef struct MyAVPacketList {
    AVPacket pkt;
    struct MyAVPacketList *next;
    int serial;
} MyAVPacketList;

class PacketQueue {
public:
    PacketQueue();
    virtual ~PacketQueue();
    // 刷出剩余裸数据
    void flush(void);
    // 入队数据
    int put(AVPacket *pkt);
    // 入队空数据
    int putNullPacket(int streamIndex);
    // 获取裸数据包
    int get(AVPacket *pkt, bool block, int *serial);
    // 获取队列大小
    int size(void);
    // 是否取消入队
    bool isAbort(void);
    // 取消入队
    void abort(void);
    // 队列是否为空
    bool isEmpty(void);
    // 获取时长
    int64_t getDuration(void);
    // 设置flush 的裸数据包
    void setFlushPacket(AVPacket *pkt);
    // 开始
    void start(void);

    int serial;

private:
    AVPacket *flush_pkt;
    int putPrivate(AVPacket *pkt);

    MyAVPacketList *mFirst;
    MyAVPacketList *mLast;
    int mPackets;
    int mSize;
    int64_t duration;
    bool mAbortRequest;

    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
};


#endif //CAINCAMERA_PACKETQUEUE_H
