//
// Created by cain on 2018/4/28.
//

#ifndef CAINPLAYER_AVSYNCHRONIZER_H
#define CAINPLAYER_AVSYNCHRONIZER_H

#include <pthread.h>
#include <android/native_window.h>
#include <Thread.h>
#include "../decoder/AVAudioDecoder.h"
#include "../decoder/AVVideoDecoder.h"
#include "../output/VideoOutputLooper.h"

/**
 * 音视频同步器，用于同步视频渲染输出
 */
class AVSynchronizer {

public:
    AVSynchronizer(AVAudioDecoder *audioDecoder, AVVideoDecoder *videoDecoder,
                   MediaStatus *status, MediaJniCall *jniCall);

    virtual ~AVSynchronizer();

    // 设置Surface
    void setSurface(ANativeWindow *window);

    // Surface大小发生变化
    void setSurfaceChanged(int width, int height);

    // 开始渲染
    void start();

    // 停止渲染
    void stop();

    // 渲染视频帧
    void renderFrame();

    // 释放资源
    void release();

private:

    // 视频同步
    double synchronize(AVFrame *srcFrame, double pts);

    // 获取延时时间
    double getDelayTime(double diff);

    // 渲染线程句柄
    static int renderThreadHandle(void *data);

    AVAudioDecoder *audioDecoder;
    AVVideoDecoder *videoDecoder;
    MediaStatus *mediaStatus;
    MediaJniCall *mediaJniCall;

    Thread *renderThread;
    pthread_mutex_t mMutex;

    bool isExit;
    double video_clock;
    double framePts;
    double clock;
    double delayTime;
    int playcount;

    ANativeWindow *nativeWindow;
    VideoOutputLooper *mVideoLooper;
};


#endif //CAINPLAYER_AVSYNCHRONIZER_H
