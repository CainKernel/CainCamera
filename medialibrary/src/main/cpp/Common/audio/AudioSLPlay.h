//
// Created by CainHuang on 2020-01-14.
//

#ifndef AUDIOSLPLAY_H
#define AUDIOSLPLAY_H

#if defined(__ANDROID__)

#include "AudioPlay.h"

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <SafetyQueue.h>

#ifndef OPENSLES_BUFLEN
#define OPENSLES_BUFLEN  10 // 缓冲区长度(毫秒)
#endif

class AudioSLPlay : public AudioPlay, public Runnable {
public:
    AudioSLPlay(const std::shared_ptr<AudioProvider> &audioProvider);

    virtual ~AudioSLPlay();

    int open(int sampleRate, int channels) override;

    void start() override;

    void stop() override;

    void pause() override;

    void resume() override;

    void flush() override;

    void setStereoVolume(float leftVolume, float rightVolume) override;

    void run() override;

private:
    void reset();

    void release();

    // 创建引擎
    int createEngine();

    // 音频播放
    void audioPlay();

    // 引擎暂停
    void enginePause();

    // 引擎停止
    void engineStop();

    // 引擎播放
    void enginePlay();

    // 引擎刷新缓冲区
    void engineFlush();

    // 引擎是否处于播放状态
    bool isEnginePlaying();

    // 设置音量
    void engineSetVolume();

    // 转换成SL采样率
    SLuint32 getSLSampleRate(int sampleRate);

    // 获取SLES音量
    SLmillibel getAmplificationLevel(float volumeLevel);

private:
    // 引擎接口
    SLObjectItf slObject;
    SLEngineItf slEngine;

    // 混音器
    SLObjectItf slOutputMixObject;

    // 播放器对象
    SLObjectItf slPlayerObject;
    SLPlayItf slPlayItf;
    SLVolumeItf slVolumeItf;

    // 缓冲器队列接口
    SLAndroidSimpleBufferQueueItf slBufferQueueItf;

    bool mInited;                       // SL初始化完成标志

    int mMaxBufferSize;                 // 最大缓冲区
    int mBufferNumber;                  // 缓冲区数量
    SafetyQueue<short *> *mBufferQueue; // 队列缓冲区

    Mutex mMutex;
    Condition mCondition;
    Thread *mAudioThread;               // 音频播放线程
    bool mAbortRequest;                 // 终止标志
    bool mPauseRequest;                 // 暂停标志
    bool mFlushRequest;                 // 刷新标志
    bool mUpdateVolume;                 // 更新音量
    float mLeftVolume;                  // 左音量
    float mRightVolume;                 // 右音量
};

#endif /* defined(__ANDROID__)  */

#endif //AUDIOSLPLAY_H
