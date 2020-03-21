//
// Created by CainHuang on 2020-03-14.
//

#ifndef AVMEDIAPLAYER_H
#define AVMEDIAPLAYER_H

#if defined(_ANDROID__)
#include <android/native_window.h>
#endif

#include <SafetyQueue.h>
#include "StreamPlayListener.h"
#include "AudioStreamPlayer.h"
#include "VideoStreamPlayer.h"
#include <MessageQueue.h>
#include "OnPlayListener.h"

/**
 * 播放器操作类型
 */
enum player_operation_type {
    OPT_NOP = 0,    // 没有任何处理
    OPT_START = 1,  // 开始
    OPT_PAUSE = 2,  // 暂停
    OPT_STOP = 3,   // 停止
    OPT_SEEK = 4,   // 定位
};


/**
 * 媒体播放器
 */
class AVMediaPlayer : Runnable {
public:
    AVMediaPlayer();

    virtual ~AVMediaPlayer();

    void setDataSource(const char *path);

    void setAudioDecoder(const char *decoder);

    void setVideoDecoder(const char *decoder);

#if defined(__ANDROID__)
    void setVideoSurface(ANativeWindow *window);
#endif

    void setSpeed(float speed);

    void setLooping(bool looping);

    void setRange(float start, float end);

    void setVolume(float leftVolume, float rightVolume);

    void start();

    void pause();

    void stop();

    void seekTo(float timeMs);

    float getDuration();

    int getVideoWidth();

    int getVideoHeight();

    bool isLooping();

    bool isPlaying();

    void release();

private:
    void run() override;

    void startPlayer();

    void pausePlayer();

    void stopPlayer();

    void seekPlayer(float timeMs);

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mThread;
    std::shared_ptr<AudioStreamPlayer> mAudioPlayer;
    std::shared_ptr<VideoStreamPlayer> mVideoPlayer;
    std::unique_ptr<MessageQueue> mMessageQueue;
    bool mLooping;
};


#endif //AVMEDIAPLAYER_H
