//
// Created by cain on 2018/12/27.
//

#include "MediaDecoder.h"

MediaDecoder::MediaDecoder(AVCodecContext *avctx, AVStream *stream, int streamIndex, PlayerState *playerState) {
    packetQueue = new PacketQueue();
    this->pCodecCtx = avctx;
    this->pStream = stream;
    this->streamIndex = streamIndex;
    this->playerState = playerState;
}

MediaDecoder::~MediaDecoder() {
    mMutex.lock();
    if (packetQueue) {
        packetQueue->flush();
        delete packetQueue;
        packetQueue = NULL;
    }
    if (pCodecCtx) {
        avcodec_close(pCodecCtx);
        avcodec_free_context(&pCodecCtx);
        pCodecCtx = NULL;
    }
    playerState = NULL;
    mMutex.unlock();
}

void MediaDecoder::start() {
    if (packetQueue) {
        packetQueue->start();
    }
    mMutex.lock();
    abortRequest = false;
    mCondition.signal();
    mMutex.unlock();
}

void MediaDecoder::stop() {
    mMutex.lock();
    abortRequest = true;
    mCondition.signal();
    mMutex.unlock();
    if (packetQueue) {
        packetQueue->abort();
    }
}

void MediaDecoder::flush() {
    if (packetQueue) {
        packetQueue->flush();
    }
    // 定位时，音视频均需要清空缓冲区
    playerState->mMutex.lock();
    avcodec_flush_buffers(getCodecContext());
    playerState->mMutex.unlock();
}

int MediaDecoder::pushPacket(AVPacket *pkt) {
    if (packetQueue) {
        return packetQueue->pushPacket(pkt);
    }
    return 0;
}

int MediaDecoder::getPacketSize() {
    return packetQueue ? packetQueue->getPacketSize() : 0;
}

int MediaDecoder::getStreamIndex() {
    return streamIndex;
}

AVStream *MediaDecoder::getStream() {
    return pStream;
}

AVCodecContext *MediaDecoder::getCodecContext() {
    return pCodecCtx;
}

int MediaDecoder::getMemorySize() {
    return packetQueue ? packetQueue->getSize() : 0;
}

int MediaDecoder::hasEnoughPackets() {
    Mutex::Autolock lock(mMutex);
    return (packetQueue == NULL) || (packetQueue->isAbort())
           || (pStream->disposition & AV_DISPOSITION_ATTACHED_PIC)
           || (packetQueue->getPacketSize() > MIN_FRAMES)
              && (!packetQueue->getDuration()
                  || av_q2d(pStream->time_base) * packetQueue->getDuration() > 1.0);
}

void MediaDecoder::run() {
    // do nothing
}

