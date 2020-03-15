//
// Created by CainHuang on 2020-02-25.
//

#include "AVideoPlay.h"

AVideoPlay::AVideoPlay(const std::shared_ptr<VideoProvider> &videoProvider)
           : VideoPlay(videoProvider) {
    mWindow = nullptr;
    mVideoThread = nullptr;
    mAbortRequest = true;
    mPauseRequest = false;
    mForceRender = false;
    mRefreshRate = 30;
    mBuffer = nullptr;
}

void AVideoPlay::setOutputSurface(ANativeWindow *window) {
    if (mWindow != nullptr) {
        ANativeWindow_release(mWindow);
    }
    mWindow = window;
    requestRender();
}

void AVideoPlay::start() {
    LOGD("AVideoPlay::start()");
    mAbortRequest = false;
    mPauseRequest = false;
    mCondition.signal();
    if (!mVideoThread) {
        mVideoThread = new Thread(this, Priority_High);
    }
    if (!mVideoThread->isActive()) {
        mVideoThread->start();
    }
    LOGD("AVideoPlay::start() Success");
}

void AVideoPlay::stop() {
    LOGD("AVideoPlay::stop()");
    mAbortRequest = true;
    mCondition.signal();
    if (mVideoThread && mVideoThread->isActive()) {
        mVideoThread->join();
        delete mVideoThread;
        mVideoThread = nullptr;
    }
    LOGD("AVideoPlay::stopSuccess()");
}

void AVideoPlay::pause() {
    LOGD("AVideoPlay::pause()");
    mPauseRequest = true;
    mCondition.signal();
}

void AVideoPlay::resume() {
    LOGD("AVideoPlay::resume()");
    mPauseRequest = false;
    mCondition.signal();
}

void AVideoPlay::requestRender() {
    mForceRender = true;
    mCondition.signal();
}

void AVideoPlay::run() {
    videoPlay();
}

void AVideoPlay::release() {
    stop();
    if (mWindow != nullptr) {
        ANativeWindow_release(mWindow);
        mWindow = nullptr;
    }
}

void AVideoPlay::videoPlay() {
    // 绘制时的缓冲区
    ANativeWindow_Buffer outBuffer;
    int result = 0;
    while (true) {

        mMutex.lock();
        if (mAbortRequest) {
            LOGD("AVideoPlay::exiting...");
            mMutex.unlock();
            break;
        }

        if (mPauseRequest && !mForceRender) {
            LOGD("AVideoPlay::pause....");
            mCondition.wait(mMutex);
            mMutex.unlock();
            continue;
        }

        // 如果此时没有窗口，则等待10毫秒继续下一轮刷新
        if (mWidth <= 0 || mHeight <= 0) {
            mCondition.waitRelativeMs(mMutex, 10);
            mMutex.unlock();
            continue;
        }

        // 初始化缓冲区
        if (!mBuffer) {
            int buffer_size = av_image_get_buffer_size(AV_PIX_FMT_RGBA, mWidth, mHeight, 1);
            mBuffer = (uint8_t *) av_malloc(buffer_size * sizeof(uint8_t));
        }

        // 渲染刷新
        if (mVideoProvider.lock() != nullptr) {
            // 这里返回的是rgba的linesize
            result = mVideoProvider.lock()->onVideoProvide(mBuffer, mWidth, mHeight, AV_PIX_FMT_RGBA);
            if (result > 0 && mWindow != nullptr) {
                ANativeWindow_setBuffersGeometry(mWindow, mWidth, mHeight, WINDOW_FORMAT_RGBA_8888);
                ANativeWindow_lock(mWindow, &outBuffer, nullptr);
                // 对齐处理
                uint8_t *bits = (uint8_t *) outBuffer.bits;
                for (int h = 0; h < mHeight; h++) {
                    memcpy(bits + h * outBuffer.stride * 4,
                           mBuffer + h * result, result);
                }
                ANativeWindow_unlockAndPost(mWindow);
            }
        }

        // 按照mRefreshRate fps 的频率刷新
        if (mRefreshRate > 0 && !mForceRender) {
            mCondition.waitRelativeMs(mMutex, static_cast<nsecs_t>(1000.0f / mRefreshRate));
        } else if (!mForceRender) { // 否则10毫秒刷新一次
            mCondition.waitRelativeMs(mMutex, 10);
        }
        mForceRender = false;
        mMutex.unlock();
    }
    LOGD("video play thread exit!");
}
