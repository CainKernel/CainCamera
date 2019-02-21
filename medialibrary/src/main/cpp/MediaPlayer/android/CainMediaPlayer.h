//
// Created by cain on 2019/2/1.
//

#ifndef CAINMEDIAPLAYER_H
#define CAINMEDIAPLAYER_H

#include <AndroidLog.h>
#include <Mutex.h>
#include <Condition.h>
#include <Thread.h>
#include <player/MediaPlayer.h>

enum media_event_type {
    MEDIA_NOP               = 0, // interface test message
    MEDIA_PREPARED          = 1,
    MEDIA_PLAYBACK_COMPLETE = 2,
    MEDIA_BUFFERING_UPDATE  = 3,
    MEDIA_SEEK_COMPLETE     = 4,
    MEDIA_SET_VIDEO_SIZE    = 5,
    MEDIA_STARTED           = 6,
    MEDIA_TIMED_TEXT        = 99,
    MEDIA_ERROR             = 100,
    MEDIA_INFO              = 200,

    MEDIA_SET_VIDEO_SAR = 10001
};

enum media_error_type {
    // 0xx
    MEDIA_ERROR_UNKNOWN = 1,
    // 1xx
    MEDIA_ERROR_SERVER_DIED = 100,
    // 2xx
    MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200,
    // 3xx
};

enum media_info_type {
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


class MediaPlayerListener {
public:
    virtual void notify(int msg, int ext1, int ext2, void *obj) {}
};

class CainMediaPlayer : public Runnable {
public:
    CainMediaPlayer();

    virtual ~CainMediaPlayer();

    void init();

    void disconnect();

    status_t setDataSource(const char *url, int64_t offset = 0, const char *headers = NULL);

    status_t setMetadataFilter(char *allow[], char *block[]);

    status_t getMetadata(bool update_only, bool apply_filter, AVDictionary **metadata);

    status_t setVideoSurface(ANativeWindow* native_window);

    status_t setListener(MediaPlayerListener *listener);

    status_t prepare();

    status_t prepareAsync();

    void start();

    void stop();

    void pause();

    void resume();

    bool isPlaying();

    int getVideoWidth();

    int getVideoHeight();

    status_t seekTo(float msec);

    long getCurrentPosition();

    long getDuration();

    status_t reset();

    status_t setAudioStreamType(int type);

    status_t setLooping(bool looping);

    bool isLooping();

    status_t setVolume(float leftVolume, float rightVolume);

    void setMute(bool mute);

    void setRate(float speed);

    void setPitch(float pitch);

    status_t setAudioSessionId(int sessionId);

    int getAudioSessionId();

    void setOption(int category, const char *type, const char *option);

    void setOption(int category, const char *type, int64_t option);

    void notify(int msg, int ext1, int ext2, void *obj = NULL, int len = 0);

protected:
    void run() override;

private:
    void postEvent(int what, int arg1, int arg2, void *obj = NULL);
    
private:
    Mutex mMutex;
    Condition mCondition;
    Thread *msgThread;
    bool abortRequest;
    GLESDevice *videoDevice;
    MediaPlayer *mediaPlayer;
    MediaPlayerListener *mListener;

    bool mSeeking;
    long mSeekingPosition;
    bool mPrepareSync;
    status_t mPrepareStatus;
    int mAudioSessionId;
};

#endif //CAINMEDIAPLAYER_H
