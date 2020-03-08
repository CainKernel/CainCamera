//
// Created by CainHuang on 2020-02-24.
//

#ifndef FFVIDEOPLAYER_H
#define FFVIDEOPLAYER_H

#include <SafetyQueue.h>
#include <android/native_window.h>
#include <player/AudioStreamPlayer.h>
#include <player/VideoStreamPlayer.h>

class OnPlayListener {
public:
    virtual ~OnPlayListener() = default;

    virtual void onPlaying(float pts) = 0;

    virtual void onSeekComplete() = 0;

    virtual void onCompletion() = 0;

    virtual void onError(int errorCode, const char *msg) = 0;
};

class FFMediaPlayer {
public:
    FFMediaPlayer();

    virtual ~FFMediaPlayer();

    void setVideoPlayListener(std::shared_ptr<OnPlayListener> listener);

    void setDataSource(const char *path);

    void setAudioDecoder(const char *decoder);

    void setVideoDecoder(const char *decoder);

    void setVideoSurface(ANativeWindow *window);

    void setSpeed(float speed);

    void setLooping(bool looping);

    void setRange(float start, float end);

    void start();

    void pause();

    void stop();

    void seekTo(float timeMs);

    float getDuration();

    bool isLooping();

    bool isPlaying();

    void release();

private:
    std::shared_ptr<OnPlayListener> mPlayListener;
    std::shared_ptr<StreamPlayListener> mStreamPlayListener;
    std::shared_ptr<AudioStreamPlayer> mAudioPlayer;
    std::shared_ptr<VideoStreamPlayer> mVideoPlayer;
};

class VideoStreamPlayerListener : public StreamPlayListener {
public:
    VideoStreamPlayerListener(FFMediaPlayer *player);

    virtual ~VideoStreamPlayerListener();

    void onPlaying(AVMediaType type, float pts) override;

    void onSeekComplete(AVMediaType type) override;

    void onCompletion(AVMediaType type) override;

    void onError(AVMediaType type, int errorCode, const char *msg) override;

private:
    FFMediaPlayer *player;
};


#endif //FFVIDEOPLAYER_H
