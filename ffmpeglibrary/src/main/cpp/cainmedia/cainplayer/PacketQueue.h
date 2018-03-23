//
// Created by Administrator on 2018/3/23.
//

#ifndef CAINCAMERA_PACKETQUEUE_H
#define CAINCAMERA_PACKETQUEUE_H

#include <queue>
#include <pthread.h>
#include <Mutex.h>

#ifdef __cplusplus
extern "C" {
#endif

#include <libavcodec/avcodec.h>

#ifdef __cplusplus
};
#endif


class PacketQueue {
public:
    PacketQueue();
    virtual ~PacketQueue();
    int put(AVPacket *avPacket);
    int get(AVPacket *avPacket);
    int flush();
    int size();
    void notify();
    void setAbort(bool abort);
    bool isAbort();

private:
    std::queue<AVPacket *> mQueue;
    Mutex *mMutex;
    Cond *mCondition;
    bool mAbortRequest;
    int64_t mDuration;
};


#endif //CAINCAMERA_PACKETQUEUE_H
