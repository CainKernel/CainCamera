//
// Created by cain on 2018/11/25.
//

#include "AudioQueue.h"

AudioQueue::AudioQueue() {
    pthread_mutex_init(&mMutex, NULL);
    pthread_cond_init(&mCondition, NULL);
}

AudioQueue::~AudioQueue() {
    clear();
    pthread_mutex_destroy(&mMutex);
    pthread_cond_destroy(&mCondition);
}

int AudioQueue::putPacket(AVPacket *packet) {
    pthread_mutex_lock(&mMutex);
    packetQueue.push(packet);
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mMutex);
    return 0;
}

int AudioQueue::getPacket(AVPacket *packet) {
    pthread_mutex_lock(&mMutex);
    while (true) {
        if (packetQueue.size() > 0) {
            AVPacket *avPacket = packetQueue.front();
            if (av_packet_ref(packet, avPacket) == 0) {
                packetQueue.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            break;
        } else {
            pthread_cond_wait(&mCondition, &mMutex);
        }
    }
    pthread_mutex_unlock(&mMutex);
    return 0;
}

int AudioQueue::getPacketSize() {
    int size = 0;
    pthread_mutex_lock(&mMutex);
    size = packetQueue.size();
    pthread_mutex_unlock(&mMutex);
    return size;
}

void AudioQueue::clear() {
    pthread_cond_signal(&mCondition);
    pthread_mutex_lock(&mMutex);
    while (!packetQueue.empty()) {
        AVPacket *packet = packetQueue.front();
        packetQueue.pop();
        av_packet_free(&packet);
        av_free(packet);
        packet = NULL;
    }
    pthread_mutex_unlock(&mMutex);
}
