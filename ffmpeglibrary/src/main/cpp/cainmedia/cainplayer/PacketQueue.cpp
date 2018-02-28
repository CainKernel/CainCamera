//
// Created by cain on 2018/2/22.
//

#include "PacketQueue.h"

PacketQueue::PacketQueue() {
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mCondition, NULL);
    mFirst = NULL;
    mLast = NULL;
    flush_pkt = NULL;
    mPackets = 0;
    mSize = 0;
    mAbortRequest = true;
}

PacketQueue::~PacketQueue() {
    flush();
    flush_pkt = NULL;
    pthread_mutex_destroy(&mLock);
    pthread_cond_destroy(&mCondition);
}

void PacketQueue::setFlushPacket(AVPacket *pkt) {
    pthread_mutex_lock(&mLock);
    putPrivate(pkt);
    pthread_mutex_unlock(&mLock);
}

int PacketQueue::size() {
    pthread_mutex_lock(&mLock);
    int size = mPackets;
    pthread_mutex_unlock(&mLock);
    return size;
}

void PacketQueue::flush() {
    MyAVPacketList *pkt, *pkt1;
    pthread_mutex_lock(&mLock);
    for (pkt = mFirst; pkt != NULL; pkt = pkt1) {
        pkt1 = pkt->next;
        av_packet_unref(&pkt->pkt);
        av_free(&pkt);
    }
    mLast = NULL;
    mFirst = NULL;
    mPackets = 0;
    mSize = 0;
    duration = 0;
    pthread_mutex_unlock(&mLock);
}

int PacketQueue::putPrivate(AVPacket *pkt) {
    MyAVPacketList *pkt1;
    if (isAbort()) {
        return -1;
    }
    pkt1 = (MyAVPacketList *) av_malloc(sizeof(MyAVPacketList));
    if (!pkt1) {
        return -1;
    }
    pkt1->pkt = *pkt;
    pkt1->next = NULL;
    // 如果时flush类型的裸数据包，则调整序列
    if (pkt == flush_pkt) {
        serial++;
    }
    pkt1->serial = serial;
    // 调整指针
    if (!mLast) {
        mFirst = pkt1;
    } else {
        mLast->next = pkt1;
    }
    mLast = pkt1;
    mPackets++;
    mSize += pkt1->pkt.size + sizeof(*pkt1);
    duration += pkt1->pkt.duration;
    pthread_cond_signal(&mCondition);
    return 0;
}

int PacketQueue::put(AVPacket *pkt) {
    int ret;
    pthread_mutex_lock(&mLock);
    ret = putPrivate(pkt);
    pthread_mutex_unlock(&mLock);

    if (pkt != flush_pkt && ret < 0) {
        av_packet_unref(pkt);
    }
    return ret;
}

int PacketQueue::putNullPacket(int streamIndex) {
    AVPacket pkt1, *pkt = &pkt1;
    av_init_packet(pkt);
    pkt->data = NULL;
    pkt->size = 0;
    pkt->stream_index = streamIndex;
    return put(pkt);
}

int PacketQueue::get(AVPacket *pkt, bool block, int *serial) {
    MyAVPacketList *pkt1;
    int ret;

    pthread_mutex_lock(&mLock);
    for(;;) {
        if (mAbortRequest) {
            ret = -1;
            break;
        }

        pkt1 = mFirst;
        if (pkt1) {
            mFirst = pkt1->next;
            if (!mFirst) {
                mLast = NULL;
            }
            mPackets--;
            mSize -= pkt1->pkt.size + sizeof(*pkt1);
            duration -= pkt1->pkt.duration;
            *pkt = pkt1->pkt;
            if (serial) {
                *serial = pkt1->serial;
            }
            av_free(pkt1);
            ret = 1;
            break;
        } else if (!block) {
            ret = 0;
            break;
        } else {
            pthread_cond_wait(&mCondition, &mLock);
        }
    }
    pthread_mutex_unlock(&mLock);
    return ret;
}

bool PacketQueue::isAbort() {
    pthread_mutex_lock(&mLock);
    bool abort = mAbortRequest;
    pthread_mutex_unlock(&mLock);
    return abort;
}

void PacketQueue::abort() {
    pthread_mutex_lock(&mLock);
    mAbortRequest = true;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}

void PacketQueue::start() {
    pthread_mutex_lock(&mLock);
    mAbortRequest = false;
    pthread_mutex_unlock(&mLock);
}

bool PacketQueue::isEmpty() {
    return mPackets == 0;
}

int64_t PacketQueue::getDuration() {
    return duration;
}