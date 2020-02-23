//
// Created by CainHuang on 2020-01-14.
//

#ifndef AUDIOPLAYER_H
#define AUDIOPLAYER_H

#include "../AVMediaHeader.h"

class AudioProvider {
public:
    virtual ~AudioProvider() = default;

    virtual int onAudioProvide(short **buffer, int size) = 0;
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

    inline AVSampleFormat getSampleFormat() {
        return mSampleFmt;
    }

protected:
    std::weak_ptr<AudioProvider> mAudioProvider;
    int mSampleRate;                // 采样率
    uint8_t mChannels;              // 声道数
    AVSampleFormat mSampleFmt;      // 采样格式
};


#endif //AUDIOPLAYER_H
