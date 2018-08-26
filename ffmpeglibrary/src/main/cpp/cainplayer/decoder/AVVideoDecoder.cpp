//
// Created by admin on 2018/4/29.
//

#include "AVVideoDecoder.h"

AVVideoDecoder::AVVideoDecoder(MediaStatus *status, MediaJniCall *jniCall)
        : AVDecoder(status, jniCall) {
    streamIndex = -1;
    clock = 0;
    isExit = true;
    isExit2 = true;
    pthread_mutex_init(&mMutex, NULL);
    mPacket = av_packet_alloc();
}

AVVideoDecoder::~AVVideoDecoder() {
    pthread_mutex_destroy(&mMutex);
    if (mPacket != NULL) {
        av_packet_free(&mPacket);
        av_free(mPacket);
        mPacket = NULL;
    }
}

void AVVideoDecoder::release() {

    if (mediaStatus != NULL) {
        mediaStatus->setExit(true);
    }

    if (queue != NULL) {
        queue->notify();
    }

    int count = 0;
    while (!isExit || !isExit2) {
        if (count > 1000) {
            isExit = true;
            isExit2 = true;
        }
        count++;
        av_usleep(1000 * 10);
    }

    if (queue != NULL) {
        queue->release();
        delete(queue);
        queue = NULL;
    }

    if (mediaJniCall != NULL) {
        mediaJniCall = NULL;
    }

    if (pCodecContext != NULL) {
        avcodec_close(pCodecContext);
        avcodec_free_context(&pCodecContext);
        pCodecContext = NULL;
    }

    if (mediaStatus != NULL) {
        mediaStatus = NULL;
    }
}

/**
 * 解码线程句柄
 * @param data
 * @return
 */
void *AVVideoDecoder::decodeThreadHandle(void *data) {
    AVVideoDecoder *decoder = (AVVideoDecoder *) data;
    decoder->decodeFrame();
    decoder->exitDecodeThread();
    return NULL;
}

/**
 * 退出解码线程
 */
void AVVideoDecoder::exitDecodeThread() {
    pthread_exit(&decodeThread);
}

/**
 * 解码得到视频帧
 */
void AVVideoDecoder::decodeFrame() {

    while (!mediaStatus->isExit()) {

        // 硬解码的时候，这里不做解码操作
        if (mediaStatus->isHardDecode()) {
            continue;
        }

        // 如果处于Seek状态，则不做解码
        if (mediaStatus->isSeek()) {
            continue;
        }

        isExit2 = false;

        // 如果AVFrame队列中的数据超过20个，不做解码
        if (queue->getFrameSize() > 20) {
            continue;
        }

        pthread_mutex_lock(&mMutex);
        // 从裸数据包队列中取出AVPacket
        if (queue->getPacket(mPacket) != 0) {
            ALOGD("get mPacket Failed!");
            av_packet_unref(mPacket);
            pthread_mutex_unlock(&mMutex);
            continue;
        }

        // 将裸数据包送去解码
        int ret = avcodec_send_packet(pCodecContext, mPacket);
        if (ret < 0 && ret != AVERROR(EAGAIN) && ret != AVERROR_EOF) {
            av_packet_unref(mPacket);
            pthread_mutex_unlock(&mMutex);
            continue;
        }

        // 获取解码后的数据
        AVFrame *frame = av_frame_alloc();
        ret = avcodec_receive_frame(pCodecContext, frame);
        if (ret < 0 && ret != AVERROR_EOF) {
            av_frame_free(&frame);
            av_free(frame);
            frame = NULL;
            av_packet_unref(mPacket);
            pthread_mutex_unlock(&mMutex);
            continue;
        }
        // 将解码后的AVFrame帧对象入队
        queue->putFrame(frame);

        av_packet_unref(mPacket);

        pthread_mutex_unlock(&mMutex);
    }
    isExit2 = true;
    av_packet_unref(mPacket);
}

void AVVideoDecoder::start() {
    // 创建解码线程
    pthread_create(&decodeThread, NULL, decodeThreadHandle, this);

}

/**
 * 获取视频帧
 * @param frame
 * @return 成功则为0，失败返回-1
 */
int AVVideoDecoder::getFrame(AVFrame *frame) {
    if (queue) {
        return queue->getFrame(frame);
    }
    return -1;
}

/**
 * 清除裸数据队列到关键帧
 */
void AVVideoDecoder::clearToKeyPacket() {
    if (queue) {
        queue->clearToKeyPacket();
    }
}


/**
 * 设置帧率(1000/fps)
 * @param rate
 */
void AVVideoDecoder::setVideoRate(int rate) {
    this->rate = rate;
}

/**
 * 设置是否高帧率视频
 * @param bigFrameRate
 */
void AVVideoDecoder::setBigFrameRate(bool bigFrameRate) {
    this->bigFrameRate = bigFrameRate;
}

/**
 * 获取视频帧率
 * @return
 */
int AVVideoDecoder::getVideoRate() const {
    return rate;
}

/**
 * 判断是否高帧率
 * @return
 */
bool AVVideoDecoder::isBigFrameRate() const {
    return bigFrameRate;
}
