//
// Created by admin on 2018/4/29.
//

#ifndef CAINPLAYER_AVVIDEODECODER_H
#define CAINPLAYER_AVVIDEODECODER_H

#include "AVDecoder.h"
#include "../common/MediaQueue.h"
#include "../common/MediaJniCall.h"
#include "../../common/AndroidLog.h"
#include "AVAudioDecoder.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/time.h>

#ifdef __cplusplus
};
#endif

class AVVideoDecoder : public AVDecoder {
public:
    AVVideoDecoder(MediaStatus *status, MediaJniCall *jniCall);

    virtual ~AVVideoDecoder();

    // 播放视频
    void start();

    // 释放资源
    void release();

    // 退出解码线程
    void exitDecodeThread();

    // 解码视频帧
    void decodeFrame();

    // 获取视频帧
    int getFrame(AVFrame *frame);

    // 清除裸数据队列
    void clearToKeyPacket();

    // 设置视频帧率
    void setVideoRate(int rate);

    // 获取视频帧率
    int getVideoRate() const;

    // 是否高帧率
    bool isBigFrameRate() const;

    // 设置是否高帧率视频
    void setBigFrameRate(bool bigFrameRate);

private:
    // 解码线程句柄
    static void *decodeThreadHandle(void *data);

    AVPacket *mPacket;

    pthread_t decodeThread;

    pthread_mutex_t mMutex;

    bool isExit;
    bool isExit2;
    int rate;
    bool bigFrameRate;
};

#endif //CAINPLAYER_AVVIDEODECODER_H
