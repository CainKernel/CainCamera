//
// Created by Administrator on 2018/3/23.
//

#include "FrameQueue.h"

FrameQueue::FrameQueue() {
    mMutex = MutexCreate();
    mCondition = CondCreate();
    mAbortRequest = false;
}

FrameQueue::~FrameQueue() {
    flush();
    MutexDestroy(mMutex);
    CondDestroy(mCondition);
}

int FrameQueue::put(AVFrame *avFrame) {
    if (size() >= FRAME_QUEUE_SIZE) {
        return -1;
    }
    MutexLock(mMutex);
    mQueue.push(avFrame);
    CondSignal(mCondition);
    MutexUnlock(mMutex);
    return 0;
}

int FrameQueue::get(AVFrame *avframe) {
    MutexLock(mMutex);
    while (!mAbortRequest) {
        if (mQueue.size() > 0) {
            AVFrame *frame = mQueue.front();
            if (av_frame_ref(avframe, frame) == 0) {
                mQueue.pop();
            }
            av_frame_free(&frame);
            av_free(frame);
            frame = NULL;
        } else if (!mAbortRequest) {
            CondWait(mCondition, mMutex);
        }
    }
    MutexUnlock(mMutex);
    return 0;
}

int FrameQueue::flush() {
    MutexLock(mMutex);
    while (!mQueue.empty()) {
        AVFrame *frame = mQueue.front();
        mQueue.pop();
        av_frame_free(&frame);
        av_free(frame);
        frame = NULL;
    }
    CondSignal(mCondition);
    MutexUnlock(mMutex);
    return 0;
}

int FrameQueue::size() {
    int size = 0;
    MutexLock(mMutex);
    size = mQueue.size();
    MutexUnlock(mMutex);
    return size;
}

void FrameQueue::notify() {
    CondSignal(mCondition);
}

void FrameQueue::setAbort(bool abort) {
    mAbortRequest = abort;
}

bool FrameQueue::isAbort() {
    return mAbortRequest;
}