//
// Created by CainHuang on 2020-02-24.
//

#ifndef AVMEDIAPLAYER_H
#define AVMEDIAPLAYER_H

#include <SafetyQueue.h>
#include <android/native_window.h>
#include <player/AudioStreamPlayer.h>
#include <player/VideoStreamPlayer.h>
#include <MessageQueue.h>
#include <player/OnPlayListener.h>
#include <player/Timestamp.h>

/**
 * 播放器事件类型
 */
enum media_player_event_type {
    MEDIA_NOP               = 0,
    MEDIA_PREPARED          = 1,
    MEDIA_STARTED           = 2,
    MEDIA_PLAYBACK_COMPLETE = 3,
    MEDIA_SEEK_COMPLETE     = 4,
    MEDIA_BUFFERING_UPDATE  = 5,
    MEDIA_SET_VIDEO_SIZE    = 6,
    MEDIA_ERROR             = 100,
    MEDIA_INFO              = 200,
    MEDIA_CURRENT           = 300,

    MEDIA_SET_VIDEO_SAR = 10001
};

/**
 * 播放出错类型
 */
enum media_player_error_type {
    // 0xx
    MEDIA_ERROR_UNKNOWN = 1,
    // 1xx
    MEDIA_ERROR_SERVER_DIED = 100,
    // 2xx
    MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200,
    // 3xx
};

/**
 * 媒体播放器信息类型
 */
enum media_player_info_type {
    // 0xx
    MEDIA_INFO_UNKNOWN = 1,
    // The player was started because it was used as the next player for another
    // player, which just completed playback
    MEDIA_INFO_STARTED_AS_NEXT = 2,
    // The player just pushed the very first video frame for rendering
    MEDIA_INFO_RENDERING_START = 3,
    // 7xx
    // The video is too complex for the decoder: it can't decode frames fast
    // enough. Possibly only the audio plays fine at this stage.
    MEDIA_INFO_VIDEO_TRACK_LAGGING = 700,
    // MediaPlayer is temporarily pausing playback internally in order to
    // buffer more data.
    MEDIA_INFO_BUFFERING_START = 701,
    // MediaPlayer is resuming playback after filling buffers.
    MEDIA_INFO_BUFFERING_END = 702,
    // Bandwidth in recent past
    MEDIA_INFO_NETWORK_BANDWIDTH = 703,

    // 8xx
    // Bad interleaving means that a media has been improperly interleaved or not
    // interleaved at all, e.g has all the video samples first then all the audio
    // ones. Video is playing but a lot of disk seek may be happening.
    MEDIA_INFO_BAD_INTERLEAVING = 800,
    // The media is not seekable (e.g live stream).
    MEDIA_INFO_NOT_SEEKABLE = 801,
    // New media metadata is available.
    MEDIA_INFO_METADATA_UPDATE = 802,
    // Audio can not be played.
    MEDIA_INFO_PLAY_AUDIO_ERROR = 804,
    // Video can not be played.
    MEDIA_INFO_PLAY_VIDEO_ERROR = 805,

    //9xx
    MEDIA_INFO_TIMED_TEXT_ERROR = 900,
};

/**
 * 媒体播放器
 */
class AVMediaPlayer : Runnable {
public:
    AVMediaPlayer();

    virtual ~AVMediaPlayer();

    // 初始化
    void init();

    void release();

    void setOnPlayingListener(std::shared_ptr<OnPlayListener> listener);

    status_t setDataSource(const char *url, int64_t offset = 0, const char *headers = nullptr);

    status_t setAudioDecoder(const char *decoder);

    status_t setVideoDecoder(const char *decoder);

#if defined(__ANDROID__)
    status_t setVideoSurface(ANativeWindow *window);
#endif

    status_t setSpeed(float speed);

    status_t setLooping(bool looping);

    status_t setRange(float start, float end);

    status_t setVolume(float leftVolume, float rightVolume);

    status_t setMute(bool mute);

    status_t prepare();

    status_t start();

    status_t pause();

    status_t stop();

    status_t setDecodeOnPause(bool decodeOnPause);

    status_t seekTo(float timeMs);

    long getCurrentPosition();

    float getDuration();

    int getRotate();

    int getVideoWidth();

    int getVideoHeight();

    bool isLooping();

    bool isPlaying();

    void onPlaying(float pts);

    void onSeekComplete(AVMediaType type);

    void onCompletion(AVMediaType type);

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
    std::shared_ptr<OnPlayListener> mPlayListener;
    std::shared_ptr<StreamPlayListener> mStreamPlayListener;
    std::shared_ptr<AudioStreamPlayer> mAudioPlayer;
    std::shared_ptr<VideoStreamPlayer> mVideoPlayer;
    std::unique_ptr<MessageQueue> mMessageQueue;
    std::shared_ptr<Timestamp> mTimestamp;
};

/**
 * 媒体流播放监听器
 */
class MediaPlayerListener : public StreamPlayListener {
public:
    MediaPlayerListener(AVMediaPlayer *player);

    virtual ~MediaPlayerListener();

    void onPrepared(AVMediaType type) override;

    void onPlaying(AVMediaType type, float pts) override;

    void onSeekComplete(AVMediaType type) override;

    void onCompletion(AVMediaType type) override;

    void onError(AVMediaType type, int errorCode, const char *msg) override;

private:
    AVMediaPlayer *player;
};


#endif //AVMEDIAPLAYER_H
