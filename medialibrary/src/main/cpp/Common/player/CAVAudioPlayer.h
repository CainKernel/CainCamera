//
// Created by CainHuang on 2020-01-30.
//

#ifndef CAVAUDIOPLAYER_H
#define CAVAUDIOPLAYER_H

#include <audio/AudioPlay.h>
#include <audio/AudioSLPlay.h>
#include <SafetyQueue.h>
#include <SonicAudioTranscoder.h>
#include <decoder/DecodeAudioThread.h>
#include <player/AudioStreamPlayer.h>
#include <MessageQueue.h>
#include <player/OnPlayListener.h>

/**
 * 播放器事件类型
 */
enum audio_player_event_type {
    MEDIA_PREPARED          = 1,
    MEDIA_STARTED           = 2,
    MEDIA_PLAYBACK_COMPLETE = 3,
    MEDIA_SEEK_COMPLETE     = 4,
    MEDIA_ERROR             = 100,
    MEDIA_INFO              = 200,
    MEDIA_CURRENT           = 300,
};

/**
 * 播放出错类型
 */
enum audio_player_error_type {
    // 0xx
    MEDIA_ERROR_UNKNOWN = 1,
    // 1xx
    MEDIA_ERROR_SERVER_DIED = 100,
    // 2xx
    MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200,
    // 3xx
};

/**
 * 音乐播放器
 */
class CAVAudioPlayer : Runnable {
public:
    CAVAudioPlayer();

    virtual ~CAVAudioPlayer();

    // 初始化
    void init();

    // 释放资源
    void release();

    // 设置监听器
    void setOnPlayingListener(std::shared_ptr<OnPlayListener> listener);

    // 设置数据源
    status_t setDataSource(const char *path);

    // 设置播放速度
    status_t setSpeed(float speed);

    // 设置是否循环播放
    status_t setLooping(bool looping);

    // 设置播放区间
    status_t setRange(float start, float end);

    // 设置音量大小
    status_t setVolume(float leftVolume, float rightVolume);

    // 准备播放器
    status_t prepare();

    // 开始播放
    status_t start();

    // 暂停播放
    status_t pause();

    // 停止播放
    status_t stop();

    // 跳转播放
    status_t seekTo(float timeMs);

    // 获取时长
    float getDuration();

    // 是否循环播放器
    bool isLooping();

    // 是否正在播放
    bool isPlaying();

    // 通知回调
    void notify(int msg, int arg1 = -1, int arg2 = -1);

private:
    void run() override;

    void postEvent(int what, int arg1 = -1, int arg2 = -1, void *obj = nullptr);

    void preparePlayer();

    void startPlayer();

    void pausePlayer();

    void stopPlayer();

    void seekPlayer(float timeMs);

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mThread;
    bool mAbortRequest;
    std::shared_ptr<AudioStreamPlayer> mAudioPlayer;
    std::shared_ptr<StreamPlayListener> mStreamPlayListener;
    std::shared_ptr<OnPlayListener> mPlayListener;
    std::unique_ptr<MessageQueue> mMessageQueue;
};

/**
 * 音频流播放监听器
 */
class AudioPlayerListener : public StreamPlayListener {

public:
    AudioPlayerListener(CAVAudioPlayer *player);

    virtual ~AudioPlayerListener();

    void onPrepared(AVMediaType type) override;

    void onPlaying(AVMediaType type, float pts) override;

    void onSeekComplete(AVMediaType type) override;

    void onCompletion(AVMediaType type) override;

    void onError(AVMediaType type, int errorCode, const char *msg) override;

private:
    CAVAudioPlayer *player;
};

#endif //CAVAUDIOPLAYER_H
