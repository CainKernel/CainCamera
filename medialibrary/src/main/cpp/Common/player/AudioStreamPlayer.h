//
// Created by CainHuang on 2020-02-27.
//

#ifndef AUDIOSTREAMPLAYER_H
#define AUDIOSTREAMPLAYER_H

#include <audio/AudioPlay.h>
#include <audio/AudioSLPlay.h>
#include <SafetyQueue.h>
#include <SonicAudioTranscoder.h>
#include <Resampler.h>
#include <decoder/AudioDecodeThread.h>
#include "StreamPlayListener.h"

/**
 * 音频流播放器
 */
class AudioStreamPlayer {
public:
    AudioStreamPlayer(const std::shared_ptr<StreamPlayListener> &listener = nullptr);

    virtual ~AudioStreamPlayer();

    void setOnPlayingListener(std::shared_ptr<StreamPlayListener> listener);

    void setDataSource(const char *path);

    void setDecoderName(const char *decoder);

    void setSpeed(float speed);

    void setLooping(bool looping);

    void setRange(float start, float end);

    void setVolume(float leftVolume, float rightVolume);

    void start();

    void pause();

    void stop();

    void seekTo(float timeMs);

    float getDuration();

    bool isLooping();

    bool isPlaying();

    int onAudioProvider(short **buffer, int bufSize);

    void release();

private:
    Mutex mMutex;
    AudioDecodeThread *mAudioThread;
    std::shared_ptr<AudioProvider> mAudioProvider;
    std::shared_ptr<AudioPlay> mAudioPlayer;
    std::shared_ptr<StreamPlayListener> mPlayListener;
    SafetyQueue<AVMediaData *> *mFrameQueue;
    std::shared_ptr<SonicAudioTranscoder> mAudioTranscoder;

    float mSpeed;
    int mSampleRate;
    int mChannels;
    bool mLooping;
    bool mPrepared;
    bool mPlaying;
    int64_t mCurrentPts;
};

class StreamAudioProvider : public AudioProvider {
public:
    StreamAudioProvider();

    virtual ~StreamAudioProvider();

    int onAudioProvide(short **buffer, int bufSize) override;

    void setPlayer(AudioStreamPlayer *player);

private:
    AudioStreamPlayer *player;
};

#endif //AUDIOSTREAMPLAYER_H
