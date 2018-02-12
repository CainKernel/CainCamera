//
// Created by Administrator on 2018/2/12.
//

#include "PacketQueue.h"

PacketQueue::PacketQueue() {
    init();
}

PacketQueue::~PacketQueue() {
    destroy();
}

/**
 * 入队
 * @param pkt
 * @param flush_PKt
 * @return
 */
int PacketQueue::put(AVPacket *pkt, AVPacket flush_PKt) {
    int ret;
    MutexLock(mutex);

    MyAVPacketList *pkt1;
    if (abort_request) {
        ret = -1;
        goto end;
    }
    pkt1 = (MyAVPacketList *) malloc(sizeof(MyAVPacketList));
    if (!pkt1) {
        ret = -1;
        goto end;
    }
    pkt1->pkt = *pkt;
    pkt1->next = NULL;
    if (pkt == &flush_PKt) {
        serial++;
    } else {
        last_pkt->next = pkt1;
    }
    last_pkt = pkt1;
    nb_packets++;
    size += pkt1->pkt.size + sizeof(*pkt1);
    duration += pkt1->pkt.duration;
end:
    MutexUnlock(mutex);
    if (pkt != &flush_PKt && ret < 0) {
        av_packet_unref(pkt);
    }

    return ret;
}

/**
 * 入队一个空包
 * @param stream_index
 * @param flush_pkt
 * @return
 */
int PacketQueue::putNullPacket(int stream_index, AVPacket flush_pkt) {
    AVPacket pkt1, *pkt = &pkt1;
    av_init_packet(pkt);
    pkt->data = NULL;
    pkt->size = 0;
    pkt->stream_index = stream_index;
    put(pkt, flush_pkt);
}


int PacketQueue::init() {
    mutex = MutexCreate();
    if (!mutex) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateMutex(): %s\n", GetError());
        return AVERROR(ENOMEM);
    }
    cond = CondCreate();
    if (!cond) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateCond(): %s\n", GetError());
        return AVERROR(ENOMEM);
    }

    abort_request = 1;
    return 0;
}


void PacketQueue::flush() {
    MyAVPacketList *pkt, *pkt1;
    MutexLock(mutex);
    for (pkt = first_pkt; pkt; pkt = pkt1) {
        pkt1 = pkt->next;
        av_packet_unref(&pkt->pkt);
        av_free(&pkt);
    }
    last_pkt = NULL;
    first_pkt = NULL;
    nb_packets = 0;
    size = 0;
    duration = 0;
    MutexUnlock(mutex);
}

void PacketQueue::destroy() {
    flush();
    MutexDestroy(mutex);
    CondDestroy(cond);
}


void PacketQueue::abort() {
    MutexLock(mutex);
    abort_request = 1;
    CondSignal(cond);
    MutexUnlock(mutex);
}


void PacketQueue::start(AVPacket flush_pkt) {
    MutexLock(mutex);
    abort_request = 0;
    put(&flush_pkt, flush_pkt);
    MutexUnlock(mutex);
}

int PacketQueue::get(AVPacket *pkt, int block, int *serial) {
    MyAVPacketList *pkt1;
    int ret;
    MutexLock(mutex);

    for (;;) {
        if (abort_request) {
            break;
        }

        pkt1 = first_pkt;
        if (pkt1) {
            first_pkt = pkt1->next;
            if (!first_pkt) {
                last_pkt = NULL;
            }
            nb_packets--;
            size -= pkt1->pkt.size + sizeof(*pkt1);
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
            CondWait(cond, mutex);
        }
    }

    MutexUnlock(mutex);
}

int PacketQueue::isAbort() {
    return abort_request;
}

int PacketQueue::getSerial() {
    return serial;
}