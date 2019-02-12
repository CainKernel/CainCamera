//
// Created by cain on 2019/1/6.
//

#ifndef AUDIORESAMPLER_H
#define AUDIORESAMPLER_H

#include <player/PlayerState.h>
#include <sync/MediaSync.h>
#include <SoundTouchWrapper.h>
#include <device/AudioDevice.h>

/**
 * 音频参数
 */
typedef struct AudioParams {
    int freq;
    int channels;
    int64_t channel_layout;
    enum AVSampleFormat fmt;
    int frame_size;
    int bytes_per_sec;
} AudioParams;

/**
 * 音频重采样状态结构体
 */
typedef struct AudioState {
    double audioClock;                      // 音频时钟
    double audio_diff_cum;
    double audio_diff_avg_coef;
    double audio_diff_threshold;
    int audio_diff_avg_count;
    int audio_hw_buf_size;
    uint8_t *outputBuffer;                  // 输出缓冲大小
    uint8_t *resampleBuffer;                // 重采样大小
    short *soundTouchBuffer;                // SoundTouch缓冲
    unsigned int bufferSize;                // 缓冲大小
    unsigned int resampleSize;              // 重采样大小
    unsigned int soundTouchBufferSize;      // SoundTouch处理后的缓冲大小大小
    int bufferIndex;
    int writeBufferSize;                    // 写入大小
    SwrContext *swr_ctx;                    // 音频转码上下文
    int64_t audio_callback_time;            // 音频回调时间
    AudioParams audioParamsSrc;             // 音频原始参数
    AudioParams audioParamsTarget;          // 音频目标参数
} AudioState;

/**
 * 音频重采样器
 */
class AudioResampler {
public:
    AudioResampler(PlayerState *playerState, AudioDecoder *audioDecoder, MediaSync *mediaSync);

    virtual ~AudioResampler();

    int setResampleParams(AudioDeviceSpec *spec, int64_t wanted_channel_layout);

    void pcmQueueCallback(uint8_t *stream, int len);

private:
    int audioSynchronize(int nbSamples);

    int audioFrameResample();

private:
    PlayerState *playerState;
    MediaSync *mediaSync;

    AVFrame *frame;
    AudioDecoder *audioDecoder;             // 音频解码器
    AudioState *audioState;                 // 音频重采样状态
    SoundTouchWrapper *soundTouchWrapper;   // 变速变调处理
};


#endif //AUDIORESAMPLER_H
