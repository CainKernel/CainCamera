//
// Created by admin on 2018/4/29.
//

#ifndef CAINPLAYER_MEDIAQUEUE_H
#define CAINPLAYER_MEDIAQUEUE_H

#include "queue"
#include "MediaStatus.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <libavcodec/avcodec.h>
#include "pthread.h"

#ifdef __cplusplus
};
#endif

class MediaQueue {
public:
    MediaQueue(MediaStatus *status);
    ~MediaQueue();
    void putPacket(AVPacket *packet);
    int getPacket(AVPacket *packet);
    void clearPacket();
    void clearToKeyPacket();

    void putFrame(AVFrame *avFrame);
    int getFrame(AVFrame *frame);
    void clearFrame();

    void release();
    int getPacketSize();
    int getFrameSize();

    void notify();

private:
    std::queue<AVPacket*> queuePacket;
    std::queue<AVFrame*> queueFrame;
    pthread_mutex_t mutexFrame;
    pthread_cond_t condFrame;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;
    MediaStatus *mediaStatus;
};


#endif //CAINPLAYER_MEDIAQUEUE_H
