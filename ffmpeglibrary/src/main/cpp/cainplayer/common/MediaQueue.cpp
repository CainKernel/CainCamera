//
// Created by admin on 2018/4/29.
//

#include "MediaQueue.h"

MediaQueue::MediaQueue(MediaStatus *status) {
    mediaStatus = status;
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);
    pthread_mutex_init(&mutexFrame, NULL);
    pthread_cond_init(&condFrame, NULL);
}

MediaQueue::~MediaQueue() {
    mediaStatus = NULL;
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);
    pthread_mutex_destroy(&mutexFrame);
    pthread_cond_destroy(&condFrame);
}

void MediaQueue::release() {
    notify();
    clearPacket();
    clearFrame();
}

void MediaQueue::putPacket(AVPacket *packet) {
    pthread_mutex_lock(&mutexPacket);
    queuePacket.push(packet);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

}

int MediaQueue::getPacket(AVPacket *packet) {
    pthread_mutex_lock(&mutexPacket);
    while (mediaStatus != NULL && !mediaStatus->isExit()) {
        if (queuePacket.size() > 0) {
            AVPacket *pkt = queuePacket.front();
            if (av_packet_ref(packet, pkt) == 0) {
                queuePacket.pop();
            }
            av_packet_free(&pkt);
            av_free(pkt);
            pkt = NULL;
            break;
        } else {
            if (!mediaStatus->isExit()) {
                pthread_cond_wait(&condPacket, &mutexPacket);
            }
        }
    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

void MediaQueue::clearPacket() {

    pthread_cond_signal(&condPacket);
    pthread_mutex_lock(&mutexPacket);
    while (!queuePacket.empty()) {
        AVPacket *pkt = queuePacket.front();
        queuePacket.pop();
        av_free(pkt->data);
        av_free(pkt->buf);
        av_free(pkt->side_data);
        pkt = NULL;
    }
    pthread_mutex_unlock(&mutexPacket);

}

int MediaQueue::getPacketSize() {
    int size = 0;
    pthread_mutex_lock(&mutexPacket);
    size = queuePacket.size();
    pthread_mutex_unlock(&mutexPacket);
    return size;
}

void MediaQueue::putFrame(AVFrame *avFrame) {
    pthread_mutex_lock(&mutexFrame);
    queueFrame.push(avFrame);
    pthread_cond_signal(&condFrame);
    pthread_mutex_unlock(&mutexFrame);
}

int MediaQueue::getFrame(AVFrame *frame) {
    pthread_mutex_lock(&mutexFrame);
    while (mediaStatus != NULL && !mediaStatus->isExit()) {
        if (queueFrame.size() > 0) {
            AVFrame *temp = queueFrame.front();
            if (av_frame_ref(frame, temp) == 0) {
                queueFrame.pop();
            }
            frame->format = temp->format;
            av_frame_free(&temp);
            av_free(temp);
            temp = NULL;
            break;
        } else {
            if (!mediaStatus->isExit()) {
                pthread_cond_wait(&condFrame, &mutexFrame);
            }
        }
    }
    pthread_mutex_unlock(&mutexFrame);
    return 0;
}

void MediaQueue::clearFrame() {
    pthread_cond_signal(&condFrame);
    pthread_mutex_lock(&mutexFrame);
    while (!queueFrame.empty()) {
        AVFrame *frame = queueFrame.front();
        queueFrame.pop();
        av_frame_free(&frame);
        av_free(frame);
        frame = NULL;
    }
    pthread_mutex_unlock(&mutexFrame);
}

int MediaQueue::getFrameSize() {
    int size = 0;
    pthread_mutex_lock(&mutexFrame);
    size = queueFrame.size();
    pthread_mutex_unlock(&mutexFrame);
    return size;
}

void MediaQueue::notify() {
    pthread_cond_signal(&condFrame);
    pthread_cond_signal(&condPacket);
}

void MediaQueue::clearToKeyPacket() {
    pthread_cond_signal(&condPacket);
    pthread_mutex_lock(&mutexPacket);
    while (!queuePacket.empty()) {
        AVPacket *pkt = queuePacket.front();
        if (pkt->flags != AV_PKT_FLAG_KEY) {
            queuePacket.pop();
            av_free(pkt->data);
            av_free(pkt->buf);
            av_free(pkt->side_data);
            pkt = NULL;
        } else {
            break;
        }
    }
    pthread_mutex_unlock(&mutexPacket);
}