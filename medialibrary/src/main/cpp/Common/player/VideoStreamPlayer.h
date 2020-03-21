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

    void setDecodeOnPause(bool decodeOnPause);

    void seekTo(float timeMs);

    float getDuration();

    int getVideoWidth();

    int getVideoHeight();

    bool isLooping();

    bool isPlaying();

    int onVideoProvide(uint8_t *buffer, int width, int height, AVPixelFormat format);

    void release();

    std::shared_ptr<VideoPlay> getPlayer();

    // 解码开始回调
    void onDecodeStart();

    // 解码结束回调
    void onDecodeFinish();

    // seek结束回调
    void onSeekComplete(float seekTime);

    // seek出错回调
    void onSeekError(int ret);

private:
    // 清空缓冲队列
    void flushQueue();

    // 释放帧对象
    void freeFrame(AVFrame *frame);

    // 设置当前播放时间戳
    void setCurrentTimestamp(float timeStamp);

    // 获取当前播放时间戳
    float getCurrentTimestamp();

    // 设置seek 的时间
    float setSeekTime(float time);

    // 获取seek的时间
    float getSeekTime();

    // 渲染一帧图片
    int renderFrame(uint8_t *buffer, int width, int height, AVPixelFormat format);
private:
    Mutex mMutex;
    Condition mCondition;
    std::shared_ptr<OnDecodeListener> mDecodeListener;
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
    bool mExit;
    bool mForceRender;
    float mSeekTime;

    float mCurrentPts;
};

/**
 * 播放线程提供者
 */
class VideoStreamProvider : public VideoProvider {
public:
    VideoStreamProvider();

    virtual ~VideoStreamProvider();

    void setPlayer(VideoStreamPlayer *player);

    int onVideoProvide(uint8_t *buffer, int width, int height, AVPixelFormat format) override;

private:
    VideoStreamPlayer *player;
};

/**
 * 解码监听器
 */
class VideoDecodeListener : public OnDecodeListener {
public:
    VideoDecodeListener(VideoStreamPlayer *player);

    virtual ~VideoDecodeListener();

    void onDecodeStart(AVMediaType type) override;

    void onDecodeFinish(AVMediaType type) override;

    void onSeekComplete(AVMediaType type, float seekTime) override;

    void onSeekError(AVMediaType type, int ret) override;

private:
    VideoStreamPlayer *player;
};

#endif //VIDEOSTREAMPLAYER_H
