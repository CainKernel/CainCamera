//
// Created by CainHuang on 2020-01-30.
//

#ifndef MUSICPLAYER_H
#define MUSICPLAYER_H

#include <audio/AudioPlayer.h>
#include <audio/AudioSLPlayer.h>
#include <reader/AudioReader.h>
#include <SafetyQueue.h>
#include <SonicAudioTranscoder.h>
#include <Resampler.h>
#include "decoder/AudioDecodeThread.h"

class OnPlayListener {
public:
    virtual ~OnPlayListener() = default;

    virtual void onPlaying(float pts) = 0;

    virtual void onSeekComplete() = 0;

    virtual void onCompletion() = 0;

    virtual void onError(int errorCode, const char *msg) = 0;
};

/**
 * 音乐播放器
 */
class MusicPlayer {
public:
    MusicPlayer();

    virtual ~MusicPlayer();

    void setOnPlayingListener(std::shared_ptr<OnPlayListener> listener);

    void setDataSource(const char *path);

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
    std::shared_ptr<AudioPlayer> mAudioPlayer;
    std::shared_ptr<OnPlayListener> mPlayListener;
    SafetyQueue<AVMediaData *> *mAudioFrame;
    std::shared_ptr<SonicAudioTranscoder> mAudioTranscoder;

    float mSpeed;
    int mSampleRate;
    int mChannels;
    bool mLooping;
    bool mPrepared;
    bool mPlaying;
    int64_t mAudioPts;
};

class MusicAudioProvider : public AudioProvider {
public:
    MusicAudioProvider();

    virtual ~MusicAudioProvider();

    int onAudioProvide(short **buffer, int bufSize) override;

    void setPlayer(MusicPlayer *player);

private:
    MusicPlayer *player;
};

#endif //MUSICPLAYER_H
