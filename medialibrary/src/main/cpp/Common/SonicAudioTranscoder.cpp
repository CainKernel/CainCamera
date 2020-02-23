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
 * 获取转码速度
 * @return
 */
float SonicAudioTranscoder::getSpeed() {
    if (sonic != nullptr) {
        return sonicGetSpeed(sonic);
    }
    return 1.0f;
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
 * 获取节拍
 * @return
 */
float SonicAudioTranscoder::getPitch() {
    if (sonic != nullptr) {
        return sonicGetPitch(sonic);
    }
    return 1.0f;
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
 * 倍速转码音频数据
 * @param data      input audio data
 * @param buffer    output buffer
 * @param pts       current pts
 * @return          transcode sample size
 */
int SonicAudioTranscoder::transcode(AVMediaData *data, short **buffer, int bufSize, int64_t &pts) {
    if (!data || data->getType() != MediaAudio || !data->sample) {
        if (data) {
            delete data;
        }
        flush();
        return 0;
    }
    pts = data->getPts();
    int size = data->sample_size;
    if (size > putBufferSize) {
        putAudioBuffer = (short *) realloc(putAudioBuffer, size);
        putBufferSize = size;
    }
    memcpy(putAudioBuffer, data->sample, size);
    putSample(putAudioBuffer, size);
    data->free();
    delete data;
    int availableSize = getSamplesAvailable();
    if (availableSize > 0) {
        if (availableSize > bufSize) {
            *buffer = (short *) realloc(*buffer, availableSize);
        }
        return receiveSample(*buffer, availableSize);
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