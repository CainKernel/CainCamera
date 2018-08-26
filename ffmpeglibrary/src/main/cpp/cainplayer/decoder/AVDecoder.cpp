//
// Created by admin on 2018/4/29.
//

#include "AVDecoder.h"

AVDecoder::AVDecoder(MediaStatus *status, MediaJniCall *jniCall) {
    clock = 0;
    current = 0;
    pCodecContext = NULL;
    queue = new MediaQueue(status);
    mediaStatus = status;
    mediaJniCall = jniCall;
}

AVDecoder::~AVDecoder() {

}

MediaQueue* AVDecoder::getQueue() {
    return queue;
}

void AVDecoder::setClock(int secds) {
    clock = secds;
}

int AVDecoder::getClock() {
    return clock;
}

int AVDecoder::getStreamIndex() const {
    return streamIndex;
}

void AVDecoder::setStreamIndex(int streamIndex) {
    this->streamIndex = streamIndex;
}

int AVDecoder::getDuration() const {
    return duration;
}

void AVDecoder::setDuration(int duration) {
    this->duration = duration;
}

double AVDecoder::getCurrent() const {
    return current;
}

void AVDecoder::setCurrent(double current) {
    this->current = current;
}

AVCodecContext *AVDecoder::getCodecContext() const {
    return pCodecContext;
}

void AVDecoder::setCodecContext(AVCodecContext *context) {
    this->pCodecContext = context;
}

const AVRational &AVDecoder::getTimeBase() const {
    return timeBase;
}

void AVDecoder::setTimeBase(const AVRational &timebase) {
    this->timeBase = timebase;
}

/**
 * 清除帧队列
 */
void AVDecoder::clearFrame() {
    if (queue) {
        queue->clearFrame();
    }
}

/**
 * 清除裸数据包队列
 */
void AVDecoder::clearPacket() {
    if (queue) {
        queue->clearPacket();
    }
}

/**
 * 入队裸数据包
 * @param pkt
 * @return
 */
int AVDecoder::putPacket(AVPacket *pkt) {
    if (queue) {
        queue->putPacket(pkt);
        return 0;
    }
    return -1;
}

/**
 * 出列裸数据包
 * @param pkt
 * @return
 */
int AVDecoder::getPacket(AVPacket *pkt) {
    if (queue) {
        return queue->getPacket(pkt);
    }
    return -1;
}

/**
 * 获取裸数据包队列大小
 * @return
 */
int AVDecoder::getPacketSize() {
    if (queue) {
        return queue->getPacketSize();
    }
    return 0;
}

/**
 * 获取帧队列大小
 * @return
 */
int AVDecoder::getFrameSize() {
    if (queue) {
        return queue->getFrameSize();
    }
    return 0;
}
