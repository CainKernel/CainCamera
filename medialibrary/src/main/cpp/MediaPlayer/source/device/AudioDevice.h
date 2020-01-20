//
// Created by cain on 2018/12/28.
//

#ifndef AUDIODEVICE_H
#define AUDIODEVICE_H

#include <player/PlayerState.h>

// 音频PCM填充回调
typedef void (*AudioPCMCallback) (void *userdata, uint8_t *stream, int len);

typedef struct AudioDeviceSpec {
    int freq;                   // 采样率
    AVSampleFormat format;      // 音频采样格式
    uint8_t channels;           // 声道
    uint16_t samples;           // 采样大小
    uint32_t size;              // 缓冲区大小
    AudioPCMCallback callback;  // 音频回调
    void *userdata;             // 音频上下文
} AudioDeviceSpec;

class AudioDevice : public Runnable {
public:
    AudioDevice();

    virtual ~AudioDevice();

    virtual int open(const AudioDeviceSpec *desired, AudioDeviceSpec *obtained);

    virtual void start();

    virtual void stop();

    virtual void pause();

    virtual void resume();

    virtual void flush();

    virtual void setStereoVolume(float left_volume, float right_volume);

    virtual void run();
};


#endif //AUDIODEVICE_H
