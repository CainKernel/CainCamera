//
// Created by Administrator on 2018/3/23.
//

#ifndef CAINCAMERA_SYNCHRONIZER_H
#define CAINCAMERA_SYNCHRONIZER_H

#include "AudioDecoder.h"
#include "VideoDecoder.h"
#include "Clock.h"

// 时钟同步类型
enum {
    AV_SYNC_AUDIO_MASTER,   // 音频作为同步，默认以音频同步 /* default choice */
    AV_SYNC_VIDEO_MASTER,   // 视频作为同步
    AV_SYNC_EXTERNAL_CLOCK, // 外部时钟作为同步 /* synchronize to an external clock */
};

// 音频视频帧同步器
class Synchronizer {
public:
    Synchronizer(AudioDecoder *audioDecoder, VideoDecoder *videoDecoder);
    virtual ~Synchronizer();
    // 获取同步类型
    int getMasterSyncType();
    // 获取主时钟
    double getMasterClock();
    // 检查外部时钟速度
    void checkExternalClockSpeed();

private:
    AudioDecoder *mAudioDecoder;    // 音频解码器
    VideoDecoder *mVideoDecoder;    // 视频解码器
    Clock *mVideoClock;             // 视频时钟
    Clock *mAudioClock;             // 音频时钟
    Clock *mExternalClock;          // 外部时钟
    int mSyncType;                  // 同步类型
};


#endif //CAINCAMERA_SYNCHRONIZER_H
