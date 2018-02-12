//
// Created by Administrator on 2018/2/12.
//

#include "FrameQueue.h"

FrameQueue::FrameQueue(PacketQueue *pktq, int max_size, int keep_last) {
    init(pktq, max_size, keep_last);
}

FrameQueue::~FrameQueue() {
    destroy();
}

void FrameQueue::unref_item(Frame *vp) {
    av_frame_unref(vp->frame);
}

int FrameQueue::init(PacketQueue *pktq, int max_size, int keep_last) {
    int i;
    mutex = MutexCreate();
    if (!mutex) {
        av_log(NULL, AV_LOG_FATAL, "MutexCreate(): %s\n", GetError());
        return AVERROR(ENOMEM);
    }
    cond = CondCreate();
    if (!cond) {
        av_log(NULL, AV_LOG_FATAL, "CondCreate(): %s\n", GetError());
        return AVERROR(ENOMEM);
    }

    this->pktq = pktq;
    this->max_size = FFMIN(max_size, FRAME_QUEUE_SIZE);
    this->keep_last = !!keep_last;

    for (i = 0; i < this->max_size; ++i) {
        if (!(queue[i].frame = av_frame_alloc())) {
            return AVERROR(ENOMEM);
        }
    }
    return 0;
}

void FrameQueue::destroy() {
    int i;
    for (i = 0; i < max_size; i++) {
        Frame *vp = &queue[i];
        unref_item(vp);
    }
    MutexDestroy(mutex);
    CondDestroy(cond);
}


void FrameQueue::signal() {
    MutexLock(mutex);
    CondSignal(cond);
    MutexUnlock(mutex);
}

Frame* FrameQueue::peek() {
    return &queue[(rindex + rindex_shown) % max_size];
}

Frame* FrameQueue::peekNext() {
    return &queue[(rindex + rindex_shown + 1) % max_size];
}

Frame* FrameQueue::peekLast() {
    return &queue[rindex];
}

Frame* FrameQueue::peekWritable() {
    MutexLock(mutex);
    while (size >= max_size && !pktq->isAbort()) {
        CondWait(cond, mutex);
    }
    MutexUnlock(mutex);

    if (pktq->isAbort()) {
        return NULL;
    }
    return &queue[windex];
}

Frame* FrameQueue::peekReadable() {
    MutexLock(mutex);
    while (size - rindex_shown <= 0 && !pktq->isAbort()) {
        CondWait(cond, mutex);
    }
    MutexUnlock(mutex);
    if (pktq->isAbort()) {
        return NULL;
    }
    return &queue[(rindex + rindex_shown) % max_size];
}

void FrameQueue::push() {
    if (++windex == max_size) {
        windex = 0;
    }
    MutexLock(mutex);
    size++;
    MutexUnlock(mutex);
}

void FrameQueue::next() {
    if (keep_last && !rindex_shown) {
        rindex_shown = 1;
        return;
    }
    MutexLock(mutex);
    size--;
    CondSignal(cond);
    MutexUnlock(mutex);
}

int FrameQueue::nbRemain() {
    return size - rindex_shown;
}

int64_t FrameQueue::lasePos() {
    Frame *vp = &queue[rindex];
    if (rindex_shown && vp->serial == pktq->getSerial()) {
        return vp->pos;
    }
    return -1;
}