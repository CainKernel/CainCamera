//
// Created by CainHuang on 2020/1/4.
//

#include <cstdlib>
#include "SonicAudioTranscoder.h"
#include "AVMediaHeader.h"

SonicAudioTranscoder::SonicAudioTranscoder(int sampleRate, int channelCount, float speed) {
    sonic = sonicCreateStream(sampleRate, channelCount);
    this->sampleRate = sampleRate;
    this->channelCount = channelCount;
    sonicSetSpeed(sonic, speed);
    sonicSetPitch(sonic, 1.0);
    sonicSetRate(sonic, 1.0);
    putBufferSize = 4096;
    putAudioBuffer = (short *) malloc(putBufferSize);
    isExit = false;
}

SonicAudioTranscoder::~SonicAudioTranscoder() {
    isExit = true;
    if (putAudioBuffer != nullptr) {
        free(putAudioBuffer);
        putAudioBuffer = nullptr;
    }
    sonicDestroyStream(sonic);
    sonic = nullptr;
}

/**
 * 设置速度
 * @param speed
 */
void SonicAudioTranscoder::setSpeed(float speed) {
    if (sonic != nullptr) {
        sonicSetSpeed(sonic, clamp(speed, MINIMUM_SPEED, MAXIMUM_SPEED));
    }
}

/**
 * 设置节拍
 * @param pitch
 */
void SonicAudioTranscoder::setPitch(float pitch) {
    if (sonic != nullptr) {
        sonicSetPitch(sonic, clamp(pitch, MINIMUM_PITCH, MAXIMUM_PITCH));
    }
}

/**
 * 刷新缓冲区
 */
void SonicAudioTranscoder::flush() {
    if (sonic != nullptr) {
        sonicFlushStream(sonic);
    }
}

/**
 * 转码音频数据
 * @param data 音频数据
 * @return 转码结果
 */
int SonicAudioTranscoder::transcode(AVMediaData *data) {
    if (data == nullptr || data->sample == nullptr || data->sample_size <= 0) {
        flush();
        return 0;
    }
    int size = data->sample_size;
    if (size > putBufferSize) {
        putAudioBuffer = (short *) realloc(putAudioBuffer, size);
        putBufferSize = size;
    }
    LOGE("putAudioBuffer == null ? %d, sample size：%d", (putAudioBuffer == nullptr), size);
    // 将uint8_t数据转成short类型
    for (int i = 0; i < (size / 2); i++) {
        putAudioBuffer[i] = (data->sample[i * 2] | (data->sample[i * 2 + 1] << 8));
    }
    putSample(putAudioBuffer, size);
    data->free();
    int availableSize = getSamplesAvailable();
    if (availableSize > 0) {
        auto outputBuffer = (short *) malloc(availableSize);
        int samplesReadSize = receiveSample(outputBuffer, availableSize);
        if (samplesReadSize > 0) {
            data->sample = (uint8 *)outputBuffer; // 转换回原来的uint8_t
            data->sample_size = samplesReadSize;
            return samplesReadSize;
        } else {
            free(outputBuffer);
        }
    }
    return 0;
}

/**
 * 将pcm数据送去转码
 * @param buffer
 * @param len 缓冲区长度
 */
void SonicAudioTranscoder::putSample(short *buffer, int len) {
    if (sonic != nullptr) {
        int samples = len / (sizeof(short) * sonicGetNumChannels(sonic));
        sonicWriteShortToStream(sonic, buffer, samples);
    }
}

/**
 * 获取转码后的PCM数据
 * @param buffer
 * @param len
 * @return 返回读取到的字节数
 */
int SonicAudioTranscoder::receiveSample(short *buffer, int len) {
    int available = getSamplesAvailable();

    if (len > available) {
        len = available;
    }
    int samplesRead = sonicReadShortFromStream(sonic, buffer,
                                               len / (sizeof(short) * sonicGetNumChannels(sonic)));
    int bytesRead = samplesRead * sizeof(short) * sonicGetNumChannels(sonic);
    return bytesRead;
}

/**
 * 获取采样之后可用的字节数
 * @return
 */
int SonicAudioTranscoder::getSamplesAvailable() {
    return sonicSamplesAvailable(sonic) * sizeof(short) * sonicGetNumChannels(sonic);
}

/**
 * 限定最大最小区间
 * @param value
 * @param min
 * @param max
 * @return
 */
float SonicAudioTranscoder::clamp(float value, float min, float max) {
    if (value < min) {
        return min;
    } else if (value > max) {
        return max;
    }
    return value;
}