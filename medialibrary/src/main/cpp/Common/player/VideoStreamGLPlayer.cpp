//
// Created by CainHuang on 2020-04-18.
//

#include "VideoStreamGLPlayer.h"

#define DEBUG 0

VideoStreamGLPlayer::VideoStreamGLPlayer(const std::shared_ptr<StreamPlayListener> &listener) {
    LOGD("VideoStreamGLPlayer::constructor()");
    mDecodeListener = std::make_shared<VideoStreamDecodeListener>(this);
    mFrameQueue = new SafetyQueue<Picture *>();
    // 解码线程
    mVideoThread = std::make_shared<DecodeVideoThread>();
    mVideoThread->setDecodeFrameQueue(mFrameQueue);
    mVideoThread->setOnDecodeListener(mDecodeListener);
    // 视频渲染器
    mVideoRender = std::make_shared<GLVideoRender>();
    mRenderThread = nullptr;

    pSwsContext = nullptr;

    mCurrentFrame = nullptr;
    mConvertFrame = nullptr;
    mBuffer = nullptr;
    mRefreshRate = 30;
    mSpeed = 1.0f;
    mLooping = false;
    mPrepared = false;
    mAbortRequest = true;
    mPauseRequest = false;
    mExit = true;
    mPlayListener = listener;
    mCurrentPts = -1;
    mForceRender = false;
    mSeekTime = -1;

}

VideoStreamGLPlayer::~VideoStreamGLPlayer() {
    release();
    LOGD("VideoStreamGLPlayer::destructor()");
}

void VideoStreamGLPlayer::release() {
    LOGD("VideoStreamGLPlayer::release()");
    stop();
    if (mVideoThread != nullptr) {
        mVideoThread->stop();
        mVideoThread.reset();
        mVideoThread = nullptr;
    }
    if (mDecodeListener != nullptr) {
        mDecodeListener.reset();
        mDecodeListener = nullptr;
    }
    if (pSwsContext != nullptr) {
        sws_freeContext(pSwsContext);
        pSwsContext = nullptr;
    }
    if (mConvertFrame != nullptr) {
        freeFrame(mConvertFrame);
        mConvertFrame = nullptr;
    }
    if (mCurrentFrame != nullptr) {
        freeFrame(mCurrentFrame);
        mCurrentFrame = nullptr;
    }
    if (mFrameQueue != nullptr) {
        flushQueue();
        delete(mFrameQueue);
        mFrameQueue = nullptr;
    }
    if (mBuffer != nullptr) {
        av_freep(&mBuffer);
        mBuffer = nullptr;
    }
}

void VideoStreamGLPlayer::setAutoAspectFit(bool autoFit) {
    if (mVideoRender != nullptr) {
        mVideoRender->setAutoAspectFit(autoFit);
    }
}

void VideoStreamGLPlayer::setTimestamp(std::shared_ptr<Timestamp> timestamp) {
    mTimestamp = timestamp;
    mCondition.signal();
}

void VideoStreamGLPlayer::setDataSource(const char *path) {
    if (mVideoThread != nullptr) {
        mVideoThread->setDataSource(path);
    }
}

void VideoStreamGLPlayer::setDecoderName(const char *decoder) {
    if (mVideoThread != nullptr) {
        mVideoThread->setDecodeName(decoder);
    }
}

void VideoStreamGLPlayer::setSpeed(float speed) {
    mSpeed = speed;
    mCondition.signal();
}

void VideoStreamGLPlayer::setLooping(bool looping) {
    mLooping = looping;
    if (mVideoThread != nullptr) {
        mVideoThread->setLooping(looping);
    }
}

void VideoStreamGLPlayer::setRange(float start, float end) {
    if (mVideoThread != nullptr) {
        mVideoThread->setRange(start, end);
    }
}

void VideoStreamGLPlayer::prepare() {
    LOGD("VideoStreamGLPlayer::prepare()");
    if (!mVideoThread) {
        return;
    }
    if (!mPrepared) {
        int ret = mVideoThread->prepare();
        if (ret < 0) {
            return;
        }
        mPrepared = true;
    }
    // 准备完成回调
    if (mPlayListener.lock() != nullptr) {
        mPlayListener.lock()->onPrepared(AVMEDIA_TYPE_VIDEO);
    }
}

void VideoStreamGLPlayer::start() {
    LOGD("VideoStreamGLPlayer::start()");
    mAbortRequest = false;
    mPauseRequest = false;
    mCondition.signal();

    // 打开解码线程
    mVideoThread->start();
    LOGD("width: %d, height: %d, rotation: %d", mVideoThread->getWidth(), mVideoThread->getHeight(),
         mVideoThread->getRotate());

    // 打开渲染线程
    if (mRenderThread == nullptr) {
        mRenderThread = new Thread(this);
    }
    if (!mRenderThread->isActive()) {
        mRenderThread->start();
    }
}

void VideoStreamGLPlayer::pause() {
    LOGD("VideoStreamGLPlayer::pause()");
    mPauseRequest = true;
    mCondition.signal();

    // 暂停解码线程
    if (mVideoThread != nullptr) {
        mVideoThread->pause();
    }
}

void VideoStreamGLPlayer::stop() {
    LOGD("VideoStreamGLPlayer::stop()");
    mAbortRequest = true;
    mCondition.signal();

    // 停止解码线程
    if (mVideoThread != nullptr) {
        mVideoThread->stop();
    }

    mMutex.lock();
    while (!mExit) {
        mCondition.wait(mMutex);
    }
    mMutex.unlock();

    flushQueue();

    // 退出渲染线程
    if (mRenderThread != nullptr) {
        mRenderThread->join();
        delete mRenderThread;
        mRenderThread = nullptr;
    }
}

void VideoStreamGLPlayer::setDecodeOnPause(bool decodeOnPause) {
    if (mVideoThread != nullptr) {
        mVideoThread->setDecodeOnPause(decodeOnPause);
    }
}

void VideoStreamGLPlayer::seekTo(float timeMs) {
    LOGD("VideoStreamGLPlayer::seekTo(): %f, currentPts: %f", timeMs, getCurrentTimestamp());
    // 跳过当前seek区间的帧，前后两帧
    if (getCurrentTimestamp() >= 0 && fabsf(getCurrentTimestamp() - timeMs) <= (2000.0f /mVideoThread->getFrameRate())) {
        setSeekTime(-1);
        return;
    }

    // 定位到某个帧中，如果队列已经存在，则直接返回不做处理
    float frame_duration = 1000.0f / mVideoThread->getFrameRate();
    bool hasSeek = false;
    while (mFrameQueue->size() > 0) {
        Picture *picture = nullptr;
        picture = mFrameQueue->pop();
        if (picture != nullptr) {
            // 如果队列取出来的帧的pts大于seek的时间，并且在两个间隔之内，就认为队列中存在要seek的帧，直接退出
            if (picture->pts > timeMs && picture->pts - 2 * frame_duration < timeMs) {
                hasSeek = true;
            }
            LOGD("skip video frame time(ms): %f, mSeekTime(ms): %f", picture->pts, timeMs);
            freeFrame(picture->frame);
            free(picture);
            // 直接退出查找过程
            if (hasSeek) {
                break;
            }
        }
    }
    // 存在seek的位置，则直接退出，并回调seek成功
    if (hasSeek) {
        onSeekComplete(timeMs);
        return;
    }

    // 设置定位的时间
    setSeekTime(timeMs);
    mCondition.signal();

    // 需要定位到某个时间戳
    if (mVideoThread != nullptr) {
        mVideoThread->seekTo(timeMs);
    }
}

float VideoStreamGLPlayer::getDuration() {
    if (mVideoThread != nullptr) {
        return mVideoThread->getDuration();
    }
    return 0;
}

int VideoStreamGLPlayer::getRotate() {
    if (mVideoThread != nullptr) {
        return mVideoThread->getRotate();
    }
    return 0;
}

int VideoStreamGLPlayer::getVideoWidth() {
    if (mVideoThread != nullptr) {
        return mVideoThread->getWidth();
    }
    return 0;
}

int VideoStreamGLPlayer::getVideoHeight() {
    if (mVideoThread != nullptr) {
        return mVideoThread->getHeight();
    }
    return 0;
}

bool VideoStreamGLPlayer::isLooping() {
    return mLooping;
}

bool VideoStreamGLPlayer::isPlaying() {
    return !(mAbortRequest || mPauseRequest);
}

/**
 * 判断是否存在视频流
 * @return
 */
bool VideoStreamGLPlayer::hasVideo() {
    if (mVideoThread != nullptr) {
        return mVideoThread->hasVideo();
    }
    return false;
}

/**
 * 清空缓冲队列
 */
void VideoStreamGLPlayer::flushQueue() {
    if (mFrameQueue != nullptr) {
        while (mFrameQueue->size() > 0) {
            auto picture = mFrameQueue->pop();
            if (picture) {
                freeFrame(picture->frame);
                free(picture);
            }
        }
    }
}

/**
 * 释放帧对象
 * @param frame
 */
void VideoStreamGLPlayer::freeFrame(AVFrame *frame) {
    if (frame != nullptr) {
        av_frame_unref(frame);
        av_frame_free(&frame);
    }
}

/**
 * 设置当前时间戳
 * @param timeStamp
 */
void VideoStreamGLPlayer::setCurrentTimestamp(float timeStamp) {
    mCurrentPts = timeStamp;
    if (mTimestamp.lock() != nullptr) {
        mTimestamp.lock()->setVideoTime(timeStamp);
    }
}

/**
 * 获取当前时间戳
 * @return
 */
float VideoStreamGLPlayer::getCurrentTimestamp() {
    if (mTimestamp.lock() != nullptr) {
        return mTimestamp.lock()->getClock();
    }
    return mCurrentPts;
}

/**
 * 设置seek的时间
 * @param time
 * @return
 */
float VideoStreamGLPlayer::setSeekTime(float time) {
    mSeekTime = time;
    return 0;
}

/**
 * 获取seek的时间
 * @return
 */
float VideoStreamGLPlayer::getSeekTime() {
    return mSeekTime;
}

/**
 * 获取倍速
 * @return
 */
float VideoStreamGLPlayer::getSpeed() {
    return mSpeed;
}

/**
 * 设置Surface
 * @param window
 */
void VideoStreamGLPlayer::surfaceCreated(ANativeWindow *window) {
    if (mVideoRender != nullptr) {
        mVideoRender->surfaceCreated(window);
    }
}

void VideoStreamGLPlayer::surfaceChange(int width, int height) {
    if (mVideoRender != nullptr) {
        mVideoRender->surfaceChanged(width, height);
    }
    mForceRender = true;
    mCondition.signal();
}

void VideoStreamGLPlayer::changeFilter(RenderNodeType type, const char *filterName) {
    if (mVideoRender != nullptr) {
        mVideoRender->changeFilter(type, filterName);
    }
    mForceRender = true;
    mCondition.signal();
}

void VideoStreamGLPlayer::changeFilter(RenderNodeType type, const int id) {
    if (mVideoRender != nullptr) {
        mVideoRender->changeFilter(type, id);
    }
    mForceRender = true;
    mCondition.signal();
}

void VideoStreamGLPlayer::onDecodeStart() {
    LOGD("VideoStreamGLPlayer::onDecodeStart()");
}

void VideoStreamGLPlayer::onDecodeFinish() {
    LOGD("VideoStreamGLPlayer::onDecodeFinish()");
}

void VideoStreamGLPlayer::onSeekComplete(float seekTime) {
    LOGD("VideoStreamGLPlayer::onSeekComplete(): seekTime: %f", seekTime);
    setCurrentTimestamp(seekTime);
    mForceRender = true;
    mCondition.signal();
    // seek 完成回调
    if (mPlayListener.lock() != nullptr) {
        mPlayListener.lock()->onSeekComplete(AVMEDIA_TYPE_VIDEO);
    }
}

void VideoStreamGLPlayer::onSeekError(int ret) {
    LOGE("VideoStreamGLPlayer::onSeekError: %s", av_err2str(ret));
}

void VideoStreamGLPlayer::run() {
    mExit = false;
    uint64_t offset = 0;
    while (true) {
        uint64_t startMs = getCurrentTimeMs();
        mMutex.lock();
        if (mAbortRequest) {
            mMutex.unlock();
            break;
        }

        if (mPauseRequest && !mForceRender) {
            LOGD("VideoStreamGLPlayer::pause....");
            mCondition.wait(mMutex);
            mMutex.unlock();
            continue;
        }

        // 如果此时没有窗口，则等待10毫秒继续下一轮刷新
        if (mVideoThread->getWidth() <= 0 || mVideoThread->getHeight() <= 0) {
            mCondition.waitRelativeMs(mMutex, 10);
            mMutex.unlock();
            continue;
        }

        // 刷新并渲染视频帧
        refreshRenderFrame();

        // 按照mRefreshRate fps 的频率刷新
        uint64_t currentMs = getCurrentTimeMs();
        if (mRefreshRate > 0 && !mForceRender) {
            nsecs_t duration = static_cast<nsecs_t>(1000.0f / mRefreshRate);
            if (offset > duration / 2.0) {
                offset = 0;
            }
            duration = duration - (currentMs - startMs - (uint64_t)fmax(offset, 0));
            mCondition.waitRelativeMs(mMutex, duration);
        } else if (!mForceRender) { // 否则16毫秒刷新一次
            mCondition.waitRelativeMs(mMutex, 16);
        }
        mForceRender = false;
        mMutex.unlock();
        mCondition.signal();
        offset = currentMs - getCurrentTimeMs();
    }

    // 终止渲染线程
    if (mVideoRender != nullptr) {
        mVideoRender->terminate(true);
    }

    mExit = true;
    LOGD("video render thread exit!");
}

/**
 * 刷新并渲染当前帧
 */
void VideoStreamGLPlayer::refreshRenderFrame() {
    int result = 0;
    if (mVideoThread == nullptr) {
        LOGE("video thread is null!");
        return;
    }
    if (mFrameQueue == nullptr) {
        LOGE("video frame is null!");
        return;
    }

    // 刷新时长，表示刷新一个的间隔
    float duration = 1000.0f / mRefreshRate;
    // 乘上速度，这样就表示一次刷新代表实际的时间间隔
    duration = duration * getSpeed();
    // 帧间平均间隔
    float frame_duration = 1000.0f / mVideoThread->getFrameRate();
    // 如果下一帧的时间大于主时钟并且超出刷新间隔，说明这次刷新还是拿上一次的帧
    // mNextFramePts - main pts - duration > duration / 2
    if (mNextFramePts > mVideoThread->getDuration()) {
        mNextFramePts = 0;
    }
    if (getSeekTime() < 0 && (mNextFramePts > mCurrentPts && mNextFramePts - mCurrentPts <= 2 * frame_duration && mNextFramePts - duration > getCurrentTimestamp())) {
        // 更新当前视频时钟
        setCurrentTimestamp(mCurrentPts + duration);
        return;
    }

    // 不断从帧队列中取出帧
    while (true) {

        // 播放器已经退出，直接退出不做渲染
        if (!isPlaying()) {
            break;
        }

        // 不处于播放阶段，并且不处于强制渲染阶段
        if (!isPlaying() && !mForceRender) {
            LOGD("video play is not playing");
            break;
        }

        Picture *picture = nullptr;
        if (!mFrameQueue->empty()) {
            picture = mFrameQueue->pop();
        }
        if (picture != nullptr) {
            auto frame = picture->frame;
            float pts = picture->pts;
            picture->frame = nullptr;
            free(picture);
            float timestamp = getCurrentTimestamp();
            float seekTime = getSeekTime();
            if (mCurrentFrame != nullptr) {
                if (DEBUG && seekTime >= 0) {
                    LOGD("picture queue frame pts(ms): %f, current pts(ms): %f", picture->pts, timestamp);
                }
                if (timestamp < 0 || (seekTime < 0 && pts < timestamp && pts > timestamp - duration)
                    || (seekTime >= 0 && (pts > timestamp - duration || pts > seekTime - duration)) // 处于seeking状态，判断队列中的pts小于当前位置
                    || (pts - timestamp) > fmax(duration / 2.0f, 0)) {  // 接近下一帧的时间
                    freeFrame(mCurrentFrame);
                    mCurrentFrame = frame;
                    setCurrentTimestamp(pts);
                    mNextFramePts = pts + frame_duration;
                    if (seekTime >= 0) {
                        LOGD("VideoStreamPlayer:: seek current picture time: %f", timestamp);
                    }
                    break;
                } else {
                    if (DEBUG && seekTime >= 0) {
                        LOGD("VideoStreamPlayer:: seeking skip picture time: %f, seekTime: %f", pts, seekTime);
                    }
                    freeFrame(frame);
                    // 非seek状态下，如果队列为空，则直接退出
                    if (seekTime < 0 && mFrameQueue->empty()) {
                        break;
                    }
                }
            } else {
                mCurrentFrame = frame;
                setCurrentTimestamp(pts);
                mNextFramePts = pts + frame_duration;
                break;
            }
        }
    }

    // 通知当前seek结束
    setSeekTime(-1);
    mCondition.signal();

    // 如果当前帧存在，则直接渲染
    renderFrame(mCurrentFrame);
}

/**
 * 上载纹理并渲染
 */
void VideoStreamGLPlayer::renderFrame(AVFrame *frame) {
    if (frame != nullptr && mVideoRender != nullptr) {
        switch (frame->format) {
            // 上载yuv420p纹理
            case AV_PIX_FMT_YUV420P:
            case AV_PIX_FMT_YUVJ420P: {
                mVideoRender->initTexture(frame->width, frame->height, mVideoThread->getRotate());
                mVideoRender->uploadData(frame->data[0], frame->linesize[0],
                                         frame->data[1], frame->linesize[1],
                                         frame->data[2], frame->linesize[2]);
                break;
            }

                // 非mtk的CPU硬解码得到的格式是NV12，转换为I420
            case AV_PIX_FMT_NV12: {
                // 创建缓冲区
                if (mBuffer == nullptr) {
                    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_YUV420P, frame->width, frame->height, 1);
                    mBuffer = (uint8_t *)av_malloc(numBytes * sizeof(uint8_t));
                    mConvertFrame = av_frame_alloc();
                    av_image_fill_arrays(mConvertFrame->data, mConvertFrame->linesize, mBuffer,
                                         AV_PIX_FMT_YUV420P, frame->width, frame->height, 1);
                }
                // 转换为YUV420P
                libyuv::NV12ToI420(frame->data[0], frame->linesize[0],
                                   frame->data[1], frame->linesize[1],
                                   mConvertFrame->data[0], mConvertFrame->linesize[0],
                                   mConvertFrame->data[1], mConvertFrame->linesize[1],
                                   mConvertFrame->data[2], mConvertFrame->linesize[2],
                                   frame->width, frame->height);

                // 上载纹理数据
                mVideoRender->initTexture(frame->width, frame->height, mVideoThread->getRotate());
                mVideoRender->uploadData(mConvertFrame->data[0], mConvertFrame->linesize[0],
                                         mConvertFrame->data[1], mConvertFrame->linesize[1],
                                         mConvertFrame->data[2], mConvertFrame->linesize[2]);
                break;
            }

            case AV_PIX_FMT_NV21: {
                // 创建缓冲区
                if (mBuffer == nullptr) {
                    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_YUV420P, frame->width, frame->height, 1);
                    mBuffer = (uint8_t *)av_malloc(numBytes * sizeof(uint8_t));
                    mConvertFrame = av_frame_alloc();
                    av_image_fill_arrays(mConvertFrame->data, mConvertFrame->linesize, mBuffer,
                                         AV_PIX_FMT_YUV420P, frame->width, frame->height, 1);
                }
                // 转换为YUV420P
                libyuv::NV21ToI420(frame->data[0], frame->linesize[0],
                                   frame->data[1], frame->linesize[1],
                                   mConvertFrame->data[0], mConvertFrame->linesize[0],
                                   mConvertFrame->data[1], mConvertFrame->linesize[1],
                                   mConvertFrame->data[2], mConvertFrame->linesize[2],
                                   frame->width, frame->height);

                // 上载纹理数据
                mVideoRender->initTexture(frame->width, frame->height, mVideoThread->getRotate());
                mVideoRender->uploadData(mConvertFrame->data[0], mConvertFrame->linesize[0],
                                         mConvertFrame->data[1], mConvertFrame->linesize[1],
                                         mConvertFrame->data[2], mConvertFrame->linesize[2]);
                break;
            }

                // 其他格式转换为YUV420P再上载纹理
            default: {
                pSwsContext = sws_getCachedContext(pSwsContext, frame->width, frame->height,
                                                   (AVPixelFormat) frame->format, frame->width, frame->height,
                                                   AV_PIX_FMT_YUV420P, SWS_BICUBIC,nullptr, nullptr, nullptr);
                // 创建缓冲区
                if (mBuffer == nullptr) {
                    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_YUV420P, frame->width, frame->height, 1);
                    mBuffer = (uint8_t *)av_malloc(numBytes * sizeof(uint8_t));
                    mConvertFrame = av_frame_alloc();
                    av_image_fill_arrays(mConvertFrame->data, mConvertFrame->linesize, mBuffer,
                                         AV_PIX_FMT_YUV420P, frame->width, frame->height, 1);
                }
                // 转码
                if (pSwsContext != nullptr) {
                    sws_scale(pSwsContext, (uint8_t const *const *) frame->data,
                              frame->linesize, 0, frame->height,
                              mConvertFrame->data, mConvertFrame->linesize);
                }

                // 上载纹理数据
                mVideoRender->initTexture(frame->width, frame->height, mVideoThread->getRotate());
                mVideoRender->uploadData(mConvertFrame->data[0], mConvertFrame->linesize[0],
                                         mConvertFrame->data[1], mConvertFrame->linesize[1],
                                         mConvertFrame->data[2], mConvertFrame->linesize[2]);
                break;
            }
        }
        mVideoRender->setTimeStamp(getCurrentTimestamp());
        mVideoRender->renderFrame();
    }
}

// ------------------------------------ 解码线程监听器 ------------------------------------------------

VideoStreamDecodeListener::VideoStreamDecodeListener(VideoStreamGLPlayer *player) : player(player) {

}

VideoStreamDecodeListener::~VideoStreamDecodeListener() {
    player = nullptr;
}

void VideoStreamDecodeListener::onDecodeStart(AVMediaType type) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr) {
        player->onDecodeStart();
    }
}

void VideoStreamDecodeListener::onDecodeFinish(AVMediaType type) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr) {
        player->onDecodeFinish();
    }
}

void VideoStreamDecodeListener::onSeekComplete(AVMediaType type, float seekTime) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr) {
        player->onSeekComplete(seekTime);
    }
}

void VideoStreamDecodeListener::onSeekError(AVMediaType type, int ret) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr) {
        player->onSeekError(ret);
    }
}