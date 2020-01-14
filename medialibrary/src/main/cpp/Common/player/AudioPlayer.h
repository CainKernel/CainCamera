//
// Created by CainHuang on 2020-01-14.
//

#ifndef AUDIOPLAYER_H
#define AUDIOPLAYER_H

#include "../AVMediaHeader.h"

class AudioProvider {
public:
    virtual void onAudioProvide(uint8_t *stream, int len) = 0;
};

class AudioPlayer {

public:
    AudioPlayer(const std::shared_ptr<AudioProvider> &audioProvider);

    virtual ~AudioPlayer();


    virtual int open(int sampleRate, int channels) = 0;

    virtual void start() = 0;

    virtual void stop() = 0;

    virtual void pause() = 0;

    virtual void resume() = 0;

    virtual void flush() = 0;

    virtual void setStereoVolume(float leftVolume, float rightVolume) = 0;

    inline int getSampleRate() {
        return mSampleRate;
    }

    inline int getChannels() {
        return mChannels;
    }

    inline uint32_t getBufferSize() {
        return mBufferSize;
    }

protected:
    std::weak_ptr<AudioProvider> mAudioProvider;
    int mSampleRate;                // 采样率
    uint8_t mChannels;              // 声道数
    uint32_t mBufferSize;           // 缓冲区大小
};


#endif //AUDIOPLAYER_H
