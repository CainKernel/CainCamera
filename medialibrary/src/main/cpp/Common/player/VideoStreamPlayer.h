//
// Created by CainHuang on 2020-02-25.
//

#ifndef VIDEOSTREAMPLAYER_H
#define VIDEOSTREAMPLAYER_H

#include <video/VideoPlay.h>
#include <video/AVideoPlay.h>
#include <SafetyQueue.h>
#include <decoder/DecodeVideoThread.h>
#include "StreamPlayListener.h"

/**
 * 视频流播放器
 */
class VideoStreamPlayer {
public:
    VideoStreamPlayer(const std::shared_ptr<StreamPlayListener> &listener = nullptr);

    virtual ~VideoStreamPlayer();

    void setDataSource(const char *path);

    void setDecoderName(const char *decoder);

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

    int onVideoProvide(uint8_t *buffer, int width, int height, AVPixelFormat format);

    void release();

    std::shared_ptr<VideoPlay> getPlayer();

private:
    void freeFrame(AVFrame *frame);

private:
    Mutex mMutex;
    std::shared_ptr<DecodeVideoThread> mVideoThread;
    std::shared_ptr<VideoProvider> mVideoProvider;
    std::shared_ptr<VideoPlay> mVideoPlayer;
    std::weak_ptr<StreamPlayListener> mPlayListener;
    SafetyQueue<Picture *> *mFrameQueue;

    SwsContext *pSwsContext;

    AVFrame *mCurrentFrame;
    AVFrame *mConvertFrame;
    float mSpeed;
    bool mLooping;
    bool mPrepared;
    bool mPlaying;

    float mCurrentPts;
};

class VideoStreamProvider : public VideoProvider {
public:
    VideoStreamProvider();

    virtual ~VideoStreamProvider();

    void setPlayer(VideoStreamPlayer *player);

    int onVideoProvide(uint8_t *buffer, int width, int height, AVPixelFormat format) override;

private:
    VideoStreamPlayer *player;
};

#endif //VIDEOSTREAMPLAYER_H
