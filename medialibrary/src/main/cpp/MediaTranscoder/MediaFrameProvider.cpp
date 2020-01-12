//
// Created by CainHuang on 2020/1/8.
//

#include "MediaFrameProvider.h"

MediaFrameProvider::MediaFrameProvider() {
    mMediaReader = new AVMediaReader();
    mMediaReader->setReadListener(this, false);
    mAudioFrameQueue = new SafetyQueue<AVMediaData *>();
    mVideoFrameQueue = new SafetyQueue<AVMediaData *>();
    mMaxFrameKeep = MAX_FRAME_KEEP;
    mStart = -1;
    mEnd = -1;
}

MediaFrameProvider::~MediaFrameProvider() {
    release();
}

/**
 * 设置媒体资源
 * @param path
 */
void MediaFrameProvider::setDataSource(const char *path) {
    mMediaReader->setDataSource(path);
}

/**
 * 设置起始位置
 * @param start
 */
void MediaFrameProvider::setStart(float start) {
    mStart = start;
}

/**
 * 设置结束为止
 * @param end
 */
void MediaFrameProvider::setEnd(float end) {
    mEnd = end;
}

/**
 * 准备
 */
int MediaFrameProvider::prepare() {
    return mMediaReader->openInputFile();
}

/**
 * 开始提取媒体帧
 */
void MediaFrameProvider::start() {
    if (mThread == nullptr) {
        mThread = new Thread(this);
    }
    if (!mThread->isActive()) {
        mThread->start();
    }
    abortRequest = false;
}

/**
 * 取消
 */
void MediaFrameProvider::cancel() {
    abortRequest = true;
    if (mThread != nullptr && mThread->isActive()) {
        mThread->join();
    }
    if (mThread != nullptr) {
        delete mThread;
        mThread = nullptr;
    }
}

/**
 * 释放所有资源
 */
void MediaFrameProvider::release() {
    abortRequest = true;
    if (mThread != nullptr) {
        mThread->join();
        delete mThread;
        mThread = nullptr;
    }
    if (mMediaReader != nullptr) {
        delete mMediaReader;
        mMediaReader = nullptr;
    }
}

/**
 * 获取音频帧队列
 * @return
 */
SafetyQueue<AVMediaData *> *MediaFrameProvider::getAudioQueue() {
    return mAudioFrameQueue;
}

/**
 * 获取视频帧队列
 * @return
 */
SafetyQueue<AVMediaData *>* MediaFrameProvider::getVideoQueue() {
    return mVideoFrameQueue;
}

/**
 * 不断提取媒体数据帧
 */
void MediaFrameProvider::run() {
    int ret = 0;

    if (!mMediaReader) {
        return;
    }

    // 定位到开始为止
    if (mStart != -1) {
        mMediaReader->seekTo(mStart);
    }

    while (true) {
        if (abortRequest) {
            break;
        }
        if (mMediaReader == nullptr) {
            break;
        }
        // 等待媒体帧队列消耗
        if (isDecodeWaiting()) {
            continue;
        }
        // 解码失败
        ret = mMediaReader->decodePacket();
        if (ret < 0) {
            if (ret != -1) {
                LOGE("Failed to decode packet!, %s", av_err2str(ret));
            }
            break;
        }
    }
}

/**
 * 回调解码数据
 * @param frame
 * @param type
 */
void MediaFrameProvider::onDecodedFrame(AVFrame *frame, AVMediaType type) {
    if (type == AVMEDIA_TYPE_AUDIO) {
        resampleAudio(frame);
    } else if (type == AVMEDIA_TYPE_VIDEO) {
        convertVideo(frame);
    } else {
        av_frame_unref(frame);
        av_frame_free(&frame);
    }
}

/**
 * 是否需要解码等待
 * @return
 */
bool MediaFrameProvider::isDecodeWaiting() {
    return (mVideoFrameQueue->size() > mMaxFrameKeep || mAudioFrameQueue->size() >= mMaxFrameKeep);
}

/**
 * 音频重采样处理
 * @param frame
 * @return
 */
int MediaFrameProvider::resampleAudio(AVFrame *frame) {
    if (!frame) {
        return -1;
    }
    // TODO 将AVFrame中的采样数据复制到AVMediaData对象中

    av_frame_unref(frame);
    av_frame_free(&frame);
    return 0;
}

/**
 * 视频转码
 * @param frame
 * @return
 */
int MediaFrameProvider::convertVideo(AVFrame *frame) {
    if (!frame) {
        return -1;
    }
    // 将AVFrame对象转码成Yuv对象
    auto yuv = convertToYuvData(frame);
    if (yuv) {
        auto data = new AVMediaData();
        fillVideoData(data, yuv, yuv->width, yuv->height);
        mVideoFrameQueue->push(data);
    }
    av_frame_unref(frame);
    av_frame_free(&frame);
    return 0;
}

/**
 * 将YuvData数据填充到AVMediaData中
 * @param mediaData 媒体数据对象
 * @param yuvData   yuv数据对象
 * @param width     宽度
 * @param height    高度
 */
void MediaFrameProvider::fillVideoData(AVMediaData *mediaData, YuvData *yuvData, int width, int height) {
    auto image = new uint8_t[width * height * 3 / 2];
    if (mediaData != nullptr) {
        mediaData->free();
    } else {
        mediaData = new AVMediaData();
    }
    mediaData->image = image;
    memcpy(mediaData->image, yuvData->dataY, (size_t) width * height);
    memcpy(mediaData->image + width * height, yuvData->dataU, (size_t) width * height / 4);
    memcpy(mediaData->image + width * height * 5 / 4, yuvData->dataV, (size_t) width * height / 4);
    mediaData->length = width * height * 3 / 2;
    mediaData->width = width;
    mediaData->height = height;
    mediaData->pixelFormat = PIXEL_FORMAT_YUV420P;
    mediaData->type = MediaVideo;
}
