//
// Created by Administrator on 2018/3/23.
//

#include "PacketQueue.h"

PacketQueue::PacketQueue() {
    mMutex = MutexCreate();
    mCondition = CondCreate();
    mAbortRequest = false;
}

PacketQueue::~PacketQueue() {
    flush();
    MutexDestroy(mMutex);
    CondDestroy(mCondition);
}

int PacketQueue::put(AVPacket *avPacket) {
    MutexLock(mMutex);
    mDuration += avPacket->duration;
    mQueue.push(avPacket);
    CondSignal(mCondition);
    MutexUnlock(mMutex);
    return 0;
}

int PacketQueue::get(AVPacket *avPacket) {
    MutexLock(mMutex);
    while (!mAbortRequest) {
        if (mQueue.size() > 0) {
            AVPacket *pkt = mQueue.front();
            if (av_packet_ref(avPacket, pkt) == 0) {
                mQueue.pop();
            }
            av_packet_free(&pkt);
            av_free(pkt);
            pkt = NULL;
            mDuration -= avPacket->duration;
            break;
        } else if (!mAbortRequest) {
            CondWait(mCondition, mMutex);
        }
    }
    MutexUnlock(mMutex);
    return 0;
}

int PacketQueue::flush() {
    MutexLock(mMutex);
    while (!mQueue.empty()) {
        AVPacket *pkt = mQueue.front();
        mQueue.pop();
        av_packet_free(&pkt);
        av_free(pkt);
        pkt = NULL;
        mDuration = 0;
    }
    MutexUnlock(mMutex);
    return 0;
}

int PacketQueue::size() {
    int size = 0;
    MutexLock(mMutex);
    size = mQueue.size();
    MutexUnlock(mMutex);
    return size;
}

void PacketQueue::notify() {
    CondSignal(mCondition);
}

void PacketQueue::setAbort(bool abort) {
    mAbortRequest = abort;
}

bool PacketQueue::isAbort() {
    return mAbortRequest;
}
