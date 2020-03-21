//
// Created by CainHuang on 2020-01-30.
//

#ifndef MUSICPLAYER_H
#define MUSICPLAYER_H

#include <audio/AudioPlay.h>
#include <audio/AudioSLPlay.h>
#include <SafetyQueue.h>
#include <SonicAudioTranscoder.h>
#include <Resampler.h>
#include <decoder/DecodeAudioThread.h>
#include <player/AudioStreamPlayer.h>

/**
 * 音乐播放器
 */
class MusicPlayer {
public:
    MusicPlayer();

    virtual ~MusicPlayer();

    void setOnPlayingListener(std::shared_ptr<StreamPlayListener> listener);

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

    void release();

private:
    std::shared_ptr<AudioStreamPlayer> mAudioPlayer;
};

#endif //MUSICPLAYER_H
