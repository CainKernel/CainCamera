//
// Created by CainHuang on 2020-02-25.
//

#ifndef AVIDEOPLAY_H
#define AVIDEOPLAY_H

#include "VideoPlay.h"

#if defined(__ANDROID__)

#include <android/native_window.h>

class AVideoPlay : public VideoPlay, public Runnable {
public:
    AVideoPlay(const std::shared_ptr<VideoProvider> &videoProvider);

    void setOutputSurface(ANativeWindow *window);

    void start() override;

    void stop() override;

    void pause() override;

    void resume() override;

    void requestRender() override;

    void run() override;

private:
    void release();

    void videoPlay();

private:
    ANativeWindow *mWindow; // Surface窗口

    uint8_t *mBuffer;

    Mutex mMutex;
    Condition mCondition;
    Thread *mVideoThread;   // 音频播放线程
    bool mAbortRequest;     // 终止标志
    bool mPauseRequest;     // 暂停标志
    bool mForceRender;      // 是否立即刷新
};

#endif /* defined(__ANDROID__) */

#endif //AVIDEOPLAY_H
