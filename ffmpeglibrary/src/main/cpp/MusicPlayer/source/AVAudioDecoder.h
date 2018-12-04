//
// Created by cain on 2018/11/25.
//

#ifndef CAINCAMERA_AVAUDIODECODER_H
#define CAINCAMERA_AVAUDIODECODER_H

#include <stdio.h>
#include "AudioQueue.h"
#include "PlayerCallback.h"
#include "PlayerStatus.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <SoundTouch.h>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libswresample/swresample.h>
};

class AVAudioDecoder {
public:
    AVAudioDecoder();

    virtual ~AVAudioDecoder();

    // 设置播放状态对象
    void setPlayerStatus(PlayerStatus *playerStatus);

    // 设置采样率
    void setSampleRate(int sampleRate);

    // 设置播放状态
    void setPlayerCallback(PlayerCallback *playerCallback);

    // 解码开始
    void start();

    // 解码音频
    int decodeAudio(void **pcmbuf);

    void initOpenSLES();

    int getOpenSLESSampleRate(int sample_rate);

    void pause();

    void resume();

    void stop();

    void release();

    void setVolume(int percent);

    void setChannelType(int channel);

    int translatePCM();

    void setPitch(float pitch);

    void setSpeed(float speed);

    void setTempo(float tempo);

    void setSpeedChange(double speedChange);

    void setTempoChange(double tempoChange);

    void setPitchOctaves(double pitchOctaves);

    void setPitchSemiTones(double semiTones);

    // 计算音频增益
    int calculateVolumeDB(char *pcmData, size_t pcmsize);

    // 入队PCM数据
    void enqueuePCM();

    // 获取音频流索引
    int getStreamIndex() const;

    // 设置音频流索引
    void setStreamIndex(int streamIndex);

    // 获取解码参数
    AVCodecParameters *getCodecParameters() const;

    // 设置解码参数
    void setCodecParameters(AVCodecParameters *parameters);

    // 设置时钟基准
    void setTimeBase(const AVRational &timeBase);

    // 获取时长
    int getDuration() const;

    // 设置时长
    void setDuration(int duration);

    // 获取解码上下文
    AVCodecContext *getCodecContext() const;

    // 设置解码上下文
    void setCodecContext(AVCodecContext *avCodecContext);

    // 设置时钟
    void setClock(double clock);

    // 设置上一次的时钟
    void setLastTime(double lastTime);

    // 获取音频队列
    AudioQueue *getQueue() const;

private:
    int streamIndex = -1;
    AVCodecContext *pCodecContext = NULL;
    AVCodecParameters *pCodecParameters = NULL;
    AudioQueue *audioQueue = NULL;
    PlayerStatus *playerStatus = NULL;
    PlayerCallback *mCallback = NULL;

    pthread_t mInitThread;  // 初始化OpenSLES的线程
    AVPacket *mPacket = NULL;
    AVFrame *mFrame = NULL;
    SwrContext *swr_ctx; // 重采样上下文

    uint8_t *buffer = NULL; // PCM原始数据缓冲区
    int sampleRate = 0; // 采样率

    int duration = 0;
    AVRational timeBase;
    double clock;       // 时钟
    double currentTime; // 当前时间
    double lastTime;    // 上一次调用时间

    int volumePercent = 100;
    int channelType = 2;    // 声音类型，0表示右声道，1表示左声道，2表示立体声

    float speed = 1.0f;
    float pitch = 1.0f;
    float tempo = 1.0f;

    // 引擎接口
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine = NULL;

    // 混音器接口
    SLObjectItf outputMixObject = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    // pcm播放接口
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;
    SLVolumeItf pcmVolumePlay = NULL;
    SLMuteSoloItf  pcmMutePlay = NULL;

    //缓冲器队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue = NULL;

    // SoundTouch
    soundtouch::SoundTouch *soundTouch = NULL;
    // 重采样缓冲区
    soundtouch::SAMPLETYPE *sampleBuffer = NULL;
    bool finished = true;
    uint8_t *out_buffer = NULL;
    int resampleSize = 0;
    int num = 0;
};


#endif //CAINCAMERA_AVAUDIODECODER_H
