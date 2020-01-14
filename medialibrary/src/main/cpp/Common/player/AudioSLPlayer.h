//
// Created by CainHuang on 2020-01-14.
//

#ifndef AUDIOSLPLAYER_H
#define AUDIOSLPLAYER_H

#if defined(__ANDROID__)

#include "AudioPlayer.h"

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#ifndef OPENSLES_BUFFERS
#define OPENSLES_BUFFERS 4 // 最大缓冲区数量
#endif

#ifndef OPENSLES_BUFLEN
#define OPENSLES_BUFLEN  10 // 缓冲区长度(毫秒)
#endif

class AudioSLPlayer : public AudioPlayer, public Runnable {
public:
    AudioSLPlayer(const std::shared_ptr<AudioProvider> &audioProvider);

    virtual ~AudioSLPlayer();

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

    int mBytesPerFrame;                 // 一帧占多少字节
    int mMillisPerBuffer;               // 一个缓冲区时长占多少
    int mFramePerBuffer;                // 一个缓冲区有多少帧
    int mBytesPerBuffer;                // 一个缓冲区的大小
    uint8_t *mBuffer;                   // 缓冲区

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

#endif //AUDIOSLPLAYER_H
