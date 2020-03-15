//
// Created by CainHuang on 2020-02-24.
//

#ifndef FFVIDEOPLAYER_H
#define FFVIDEOPLAYER_H

#include <SafetyQueue.h>
#include <android/native_window.h>
#include <player/AudioStreamPlayer.h>
#include <player/VideoStreamPlayer.h>
#include <MessageQueue.h>

/**
 * 播放器操作类型
 */
enum player_operation_type {
    OPT_NOP = 0,    // 没有任何处理
    OPT_START = 1,  // 开始
    OPT_PAUSE = 2,  // 暂停
    OPT_STOP = 3,   // 停止
    OPT_SEEK = 4,   // 定位
};

class OnPlayListener {
public:
    virtual ~OnPlayListener() = default;

    virtual void onPlaying(float pts) = 0;

    virtual void onSeekComplete() = 0;

    virtual void onCompletion() = 0;

    virtual void onError(int errorCode, const char *msg) = 0;
};

class FFMediaPlayer : Runnable {
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

    void setVolume(float leftVolume, float rightVolume);

    void start();

    void pause();

    void stop();

    void setDecodeOnPause(bool decodeOnPause);

    void seekTo(float timeMs);

    float getDuration();

    int getVideoWidth();

    int getVideoHeight();

    bool isLooping();

    bool isPlaying();

    void release();

    std::shared_ptr<OnPlayListener> getPlayListener();

private:
    void run() override;

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mThread;
    std::shared_ptr<OnPlayListener> mPlayListener;
    std::shared_ptr<StreamPlayListener> mStreamPlayListener;
    std::shared_ptr<AudioStreamPlayer> mAudioPlayer;
    std::shared_ptr<VideoStreamPlayer> mVideoPlayer;
    std::unique_ptr<MessageQueue> mMessageQueue;
};

class MediaStreamPlayerListener : public StreamPlayListener {
public:
    MediaStreamPlayerListener(FFMediaPlayer *player);

    virtual ~MediaStreamPlayerListener();

    void onPlaying(AVMediaType type, float pts) override;

    void onSeekComplete(AVMediaType type) override;

    void onCompletion(AVMediaType type) override;

    void onError(AVMediaType type, int errorCode, const char *msg) override;

private:
    FFMediaPlayer *player;
};


#endif //FFVIDEOPLAYER_H
