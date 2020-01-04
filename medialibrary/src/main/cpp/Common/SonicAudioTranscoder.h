//
// Created by CainHuang on 2020/1/4.
//

#ifndef SONICAUDIOTRANSCODER_H
#define SONICAUDIOTRANSCODER_H

// 最大倍速
#include "AVMediaData.h"
#include "sonic.h"

#define MAXIMUM_SPEED 8.0

// 最小倍速
#define MINIMUM_SPEED 0.1

// 最大节拍
#define MAXIMUM_PITCH 8.0

// 最小节拍
#define MINIMUM_PITCH 0.1

/**
 * 基于Sonic的倍速转换器
 */
class SonicAudioTranscoder {
public:
    SonicAudioTranscoder(int sampleRate, int channelCount, float speed);

    virtual ~SonicAudioTranscoder();

    // 设置速度
    void setSpeed(float speed);

    // 设置节拍
    void setPitch(float pitch);

    // 刷新缓冲区
    void flush();

    // 音频转码
    int transcode(AVMediaData *data);

private:
    // 将采样数据送去转码
    void putSample(short *buffer, int len);

    // 获取转码后的采样数据
    int receiveSample(short *buffer, int len);

    // 获取可用的采样数据
    int getSamplesAvailable();

    // 限定区间
    float clamp(float value, float min, float max);

private:
    sonicStream sonic;
    short *putAudioBuffer;
    bool isExit;
    int putBufferSize;
    int sampleRate;
    int channelCount;
};


#endif //SONICAUDIOTRANSCODER_H
