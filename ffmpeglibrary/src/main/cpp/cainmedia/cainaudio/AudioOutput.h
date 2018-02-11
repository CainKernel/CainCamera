//
// Created by cain on 2018/2/11.
//

#ifndef CAINCAMERA_AUDIOOUTPUT_H
#define CAINCAMERA_AUDIOOUTPUT_H

#include <unistd.h>
#include "SLESContext.h"

#define DEFAULT_AUDIO_BUFFER_DURATION_IN_SECS 0.03

typedef int(*audioCallback)(unsigned char* , size_t, void* ctx);

#define PLAYING_STATE_STOPPED (0x00000001)
#define PLAYING_STATE_PLAYING (0x00000002)

class AudioOutput {

public:
    AudioOutput();
    virtual ~AudioOutput();

    int playingState;
    // 初始化声音
    SLresult initSoundTrack(int channels, int accompanySampleRate, audioCallback, void* ctx);
    // 开始
    SLresult start();
    // 播放
    SLresult play();
    // 暂停
    SLresult pause();
    // 停止
    SLresult stop();
    // 是否处于播放状态
    bool isPlaying();
    // 获取当前时间
    long getCurrentTimeMills();
    // 销毁上下文
    void destroyContext();
    audioCallback produceDataCallback;
    void* ctx;

private:
    SLEngineItf engineEngine;
    SLObjectItf outputMixObject;
    SLObjectItf audioPlayerObject;
    SLAndroidSimpleBufferQueueItf audioPlayerBufferQueue;
    SLPlayItf audioPlayerPlay;

    unsigned char* buffer;
    size_t bufferSize;
    // 初始化buffer
    void initPlayerBuffer();
    // 释放buffer
    void freePlayerBuffer();
    // 实例化一个对象
    SLresult realizeObject(SLObjectItf object);
    // 销毁这个对象
    void destroyObject(SLObjectItf& object);
    // 创建输出对象
    SLresult createOutputMix();
    // 创建AudioPlayer对象
    SLresult createAudioPlayer(int channels, int accompanySampleRate);
    // 获取AudioPlayer对象的bufferQueue的接口
    SLresult getAudioPlayerBufferQueueInterface();
    // 获得AudioPlayer对象的play的接口
    SLresult getAudioPlayerPlayInterface();
    // 设置播放器的状态为播放状态
    SLresult setAudioPlayerStatePlaying();
    // 设置播放器的状态为暂停状态
    SLresult setAudioPlayerStatePaused();
    // 当OpenSL播放完毕回调
    SLresult registerPlayerCallback();
    // 播放完毕回调
    static void playerCallback(SLAndroidSimpleBufferQueueItf bq, void *context);
    // 处理包数据
    void producePacket();
};


#endif //CAINCAMERA_AUDIOOUTPUT_H
