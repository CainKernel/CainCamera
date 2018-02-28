//
// Created by cain on 2018/2/25.
//

#include "FrameQueue.h"

FrameQueue::FrameQueue(PacketQueue *pktq, int max_size, int keep_last) {
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mCondition, NULL);
    this->pktq = pktq;
    this->maxSize = FFMIN(max_size, MAX_FRAME_QUEUE_SIZE);
    this->keepLast = !!keep_last;
    for (int i = 0; i < maxSize; ++i) {
        if (!(queue[i].frame = av_frame_alloc())) {
            break;
        }
    }
}

FrameQueue::~FrameQueue() {
    // 销毁队列中的AVFrame
    for (int i = 0; i < maxSize; ++i) {
        Frame *vp = &queue[i];
        unref(vp);
        av_frame_free(&vp->frame);
    }
    pktq = NULL;
    pthread_mutex_destroy(&mLock);
    pthread_cond_destroy(&mCondition);
}

void FrameQueue::unref(Frame *vp) {
    av_frame_unref(vp->frame);
    avsubtitle_free(&vp->sub);
}

void FrameQueue::signal() {
    pthread_mutex_lock(&mLock);
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}

Frame* FrameQueue::peek() {
    return &queue[(rindex + rindexShown) % maxSize];
}

Frame* FrameQueue::peekNext() {
    return &queue[(rindex + rindexShown + 1) % maxSize];
}

Frame* FrameQueue::peekLast() {
    return &queue[rindex];
}

Frame* FrameQueue::peekWritable() {
    pthread_mutex_lock(&mLock);
    // 如果存放的数据太大，则等待销毁完毕
    while (mSize >= maxSize && !pktq->isAbort()) {
        pthread_cond_wait(&mCondition, &mLock);
    }
    pthread_mutex_unlock(&mLock);
    if (pktq->isAbort()) {
        return NULL;
    }
    return &queue[windex];
}

Frame* FrameQueue::peekReadable() {
    pthread_mutex_lock(&mLock);
    // 如果没有读出数据，则一直等待
    while (mSize - rindexShown <= 0 && !pktq->isAbort()) {
        pthread_cond_wait(&mCondition, &mLock);
    }
    pthread_mutex_unlock(&mLock);
    if (pktq->isAbort()) {
        return NULL;
    }
    return &queue[(rindex + rindexShown) % maxSize];
}

void FrameQueue::push() {
    if (++windex == maxSize) {
        windex = 0;
    }
    pthread_mutex_lock(&mLock);
    mSize++;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}

void FrameQueue::next() {
    if (keepLast && !rindexShown) {
        rindexShown = 1;
        return;
    }
    // 释放帧
    unref(&queue[rindex]);
    if (++rindex == maxSize) {
        rindex = 0;
    }
    pthread_mutex_lock(&mLock);
    mSize--;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}

int FrameQueue::nbRemaining() {
    return mSize - rindexShown;
}

int64_t FrameQueue::lastPos() {
    Frame *fp = &queue[rindex];
    if (rindexShown && fp->serial == pktq->serial) {
        return fp->pos;
    } else {
        return -1;
    }
}

void FrameQueue::lock() {
    pthread_mutex_lock(&mLock);
}

void FrameQueue::unlock() {
    pthread_mutex_unlock(&mLock);
}