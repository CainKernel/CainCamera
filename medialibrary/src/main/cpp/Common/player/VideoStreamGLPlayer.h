//
// Created by CainHuang on 2020-04-18.
//

#ifndef VIDEOSTREAMGLPLAYER_H
#define VIDEOSTREAMGLPLAYER_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <node/DisplayRenderNode.h>
#include <node/InputRenderNode.h>
#include <node/RenderNodeList.h>

#include <SafetyQueue.h>
#include <decoder/DecodeVideoThread.h>
#include <video/GLVideoRender.h>
#include "StreamPlayListener.h"
#include "Timestamp.h"

/**
 * 基于OpenGLES的视频流播放器
 */
class VideoStreamGLPlayer : public Runnable {
public:
    VideoStreamGLPlayer(const std::shared_ptr<StreamPlayListener> &listener = nullptr);

    virtual ~VideoStreamGLPlayer();

    void setTimestamp(std::shared_ptr<Timestamp> timestamp);

    void setDataSource(const char *path);

    void setDecoderName(const char *decoder);

    void setSpeed(float speed);

    void setLooping(bool looping);

    void setRange(float start, float end);

    void prepare();

    void start();

    void pause();

    void stop();

    void setDecodeOnPause(bool decodeOnPause);

    void seekTo(float timeMs);

    float getDuration();

    int getRotate();

    int getVideoWidth();

    int getVideoHeight();

    bool isLooping();

    bool isPlaying();

    bool hasVideo();

    void release();

    void surfaceCreated(ANativeWindow *window);

    void surfaceChange(int width, int height);

    // 切换滤镜
    void changeFilter(RenderNodeType type, const char *filterName);

    // 切换滤镜
    void changeFilter(RenderNodeType type, const int id);

    void run() override;

public:

    // 解码开始回调
    void onDecodeStart();

    // 解码结束回调
    void onDecodeFinish();

    // seek结束回调
    void onSeekComplete(float seekTime);

    // seek出错回调
    void onSeekError(int ret);

private:
    // 刷新并渲染视频帧
    void refreshRenderFrame();

    // 上载纹理
    void renderFrame(AVFrame *frame);

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

    // 获取倍速
    float getSpeed();

private:
    Mutex mMutex;
    Condition mCondition;
    // 同步线程
    Thread *mRenderThread;

    std::shared_ptr<OnDecodeListener> mDecodeListener;
    std::shared_ptr<DecodeVideoThread> mVideoThread;
    std::weak_ptr<StreamPlayListener> mPlayListener;
    SafetyQueue<Picture *> *mFrameQueue;
    std::weak_ptr<Timestamp> mTimestamp;

    // 渲染器
    std::shared_ptr<GLVideoRender> mVideoRender;

    SwsContext *pSwsContext;

    AVFrame *mCurrentFrame;
    AVFrame *mConvertFrame;
    uint8_t *mBuffer;
    float mRefreshRate;     // 刷新频率, 默认30fps
    float mSpeed;
    bool mLooping;
    bool mPrepared;
    bool mAbortRequest;     // 终止标志
    bool mPauseRequest;     // 暂停标志
    bool mForceRender;      // 是否立即刷新
    bool mExit;
    float mSeekTime;

    float mCurrentPts;
    float mNextFramePts;
};

/**
 * 解码监听器
 */
class VideoStreamDecodeListener : public OnDecodeListener {
public:
    VideoStreamDecodeListener(VideoStreamGLPlayer *player);

    virtual ~VideoStreamDecodeListener();

    void onDecodeStart(AVMediaType type) override;

    void onDecodeFinish(AVMediaType type) override;

    void onSeekComplete(AVMediaType type, float seekTime) override;

    void onSeekError(AVMediaType type, int ret) override;

private:
    VideoStreamGLPlayer *player;
};

#endif //VIDEOSTREAMGLPLAYER_H
