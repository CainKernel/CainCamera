//
// Created by cain on 2018/11/25.
//

#ifndef CAINCAMERA_AUDIOQUEUE_H
#define CAINCAMERA_AUDIOQUEUE_H

#include <queue>
#include <pthread.h>

extern "C" {
#include <libavcodec/avcodec.h>
};

class AudioQueue {
public:
    AudioQueue();

    virtual ~AudioQueue();

    int putPacket(AVPacket *packet);

    int getPacket(AVPacket *packet);

    int getPacketSize();

    void clear();

private:
    std::queue<AVPacket *> packetQueue;
    pthread_mutex_t mMutex;
    pthread_cond_t mCondition;

};


#endif //CAINCAMERA_AUDIOQUEUE_H
