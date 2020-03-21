//
// Created by CainHuang on 2020-02-25.
//

#include "VideoStreamPlayer.h"

#define DEBUG 0

VideoStreamPlayer::VideoStreamPlayer(const std::shared_ptr<StreamPlayListener> &listener) {
    LOGD("VideoStreamPlayer::constructor()");
    mDecodeListener = std::make_shared<VideoDecodeListener>(this);
    mFrameQueue = new SafetyQueue<Picture *>();
    mVideoThread = std::make_shared<DecodeVideoThread>();
    mVideoThread->setDecodeFrameQueue(mFrameQueue);
    mVideoThread->setOnDecodeListener(mDecodeListener);
    mVideoProvider = std::make_shared<VideoStreamProvider>();
    auto provider = std::dynamic_pointer_cast<VideoStreamProvider>(mVideoProvider);
    provider->setPlayer(this);
    mVideoPlayer = std::make_shared<AVideoPlay>(mVideoProvider);

    pSwsContext = nullptr;

    mCurrentFrame = nullptr;
    mConvertFrame = av_frame_alloc();
    mSpeed = 1.0f;
    mLooping = false;
    mPrepared = false;
    mPlaying = false;
    mExit = true;
    mPlayListener = listener;
    mCurrentPts = -1;
    mForceRender = false;
    mSeekTime = -1;
}

VideoStreamPlayer::~VideoStreamPlayer() {
    release();
    LOGD("VideoStreamPlayer::destructor()");
}

void VideoStreamPlayer::release() {
    LOGD("VideoStreamPlayer::release()");
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
    if (mConvertFrame != nullptr) {
        freeFrame(mConvertFrame);
        mConvertFrame = nullptr;
    }
}

/**
 * 设置时间戳对象
 * @param timestamp
 */
void VideoStreamPlayer::setTimestamp(std::shared_ptr<Timestamp> timestamp) {
    mTimestamp = timestamp;
    mCondition.signal();
}

void VideoStreamPlayer::setDataSource(const char *path) {
    if (mVideoThread != nullptr) {
        mVideoThread->setDataSource(path);
    }
}

void VideoStreamPlayer::setDecoderName(const char *decoder) {
    if (mVideoThread != nullptr) {
        mVideoThread->setDecodeName(decoder);
    }
}

void VideoStreamPlayer::setSpeed(float speed) {
    AutoMutex lock(mMutex);
    mSpeed = speed;
    mCondition.signal();
}

void VideoStreamPlayer::setLooping(bool looping) {
    mLooping = looping;
    if (mVideoThread != nullptr) {
        mVideoThread->setLooping(looping);
    }
}

void VideoStreamPlayer::setRange(float start, float end) {
    if (mVideoThread != nullptr) {
        mVideoThread->setRange(start, end);
    }
}

void VideoStreamPlayer::start() {
    LOGD("VideoStreamPlayer::start()");
    if (!mVideoThread || !mVideoPlayer) {
        return;
    }
    if (!mPrepared) {
        int ret = mVideoThread->prepare();
        if (ret < 0) {
            return;
        }
        mPrepared = true;
    }
    mExit = false;
    mVideoThread->start();
//    mVideoPlayer->setRefreshRate(mVideoThread->getFrameRate());
    LOGD("width: %d, height: %d, rotation: %.2f", mVideoThread->getWidth(), mVideoThread->getHeight(),
            mVideoThread->getRotation());
    mVideoPlayer->setOutput(mVideoThread->getWidth(), mVideoThread->getHeight());
    mVideoPlayer->start();
    mPlaying = true;
}

void VideoStreamPlayer::pause() {
    LOGD("VideoStreamPlayer::pause()");
    mPlaying = false;
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->pause();
    }
    if (mVideoThread != nullptr) {
        mVideoThread->pause();
    }
}

void VideoStreamPlayer::stop() {
    LOGD("VideoStreamPlayer::stop()");
    mPlaying = false;
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->stop();
    }
    if (mVideoThread != nullptr) {
        mVideoThread->stop();
    }
    mExit = true;
    mCondition.signal();
    flushQueue();
}

void VideoStreamPlayer::setDecodeOnPause(bool decodeOnPause) {
    if (mVideoThread != nullptr) {
        mVideoThread->setDecodeOnPause(decodeOnPause);
    }
}

void VideoStreamPlayer::seekTo(float timeMs) {
    LOGD("VideoStreamPlayer::seekTo(): %f, currentPts: %f", timeMs, getCurrentTimestamp());
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
        if (mDecodeListener != nullptr) {
            mDecodeListener->onSeekComplete(AVMEDIA_TYPE_VIDEO, timeMs);
        } else {
            onSeekComplete(timeMs);
        }
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

float VideoStreamPlayer::getDuration() {
    if (mVideoThread != nullptr) {
        return mVideoThread->getDuration();
    }
    return 0;
}

int VideoStreamPlayer::getVideoWidth() {
    if (mVideoThread != nullptr) {
        return mVideoThread->getWidth();
    }
    return 0;
}

int VideoStreamPlayer::getVideoHeight() {
    if (mVideoThread != nullptr) {
        return mVideoThread->getHeight();
    }
    return 0;
}

bool VideoStreamPlayer::isLooping() {
    return mLooping;
}

bool VideoStreamPlayer::isPlaying() {
    return mPlaying;
}

std::shared_ptr<VideoPlay> VideoStreamPlayer::getPlayer() {
    return mVideoPlayer;
}

/**
 * 清空缓冲队列
 */
void VideoStreamPlayer::flushQueue() {
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
void VideoStreamPlayer::freeFrame(AVFrame *frame) {
    if (frame) {
        av_frame_unref(frame);
        av_frame_free(&frame);
    }
}

/**
 * 设置当前时间戳
 * @param timeStamp
 */
void VideoStreamPlayer::setCurrentTimestamp(float timeStamp) {
    AutoMutex lock(mMutex);
    mCurrentPts = timeStamp;
    if (mTimestamp.lock() != nullptr) {
        mTimestamp.lock()->setVideoTime(timeStamp);
    }
}

/**
 * 获取当前时间戳
 * @return
 */
float VideoStreamPlayer::getCurrentTimestamp() {
    AutoMutex lock(mMutex);
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
float VideoStreamPlayer::setSeekTime(float time) {
    AutoMutex lock(mMutex);
    mSeekTime = time;
    return 0;
}

/**
 * 获取seek的时间
 * @return
 */
float VideoStreamPlayer::getSeekTime() {
    AutoMutex lock(mMutex);
    return mSeekTime;
}

/**
 * 获取倍速
 * @return
 */
float VideoStreamPlayer::getSpeed() {
    AutoMutex lock(mMutex);
    return mSpeed;
}

/**
 * 帧提供者
 * @param buffer
 * @param width
 * @param height
 * @param format
 * @return
 */
int VideoStreamPlayer::onVideoProvide(uint8_t *buffer, int width, int height, AVPixelFormat format) {
    int result = 0;
    if (mVideoThread == nullptr) {
        LOGE("video thread is null!");
        return result;
    }
    if (mFrameQueue == nullptr) {
        LOGE("video frame is null!");
        return result;
    }

    // 刷新时长，表示刷新一个的间隔
    float duration = 1000.0f / mVideoThread->getFrameRate();
    // 乘上速度，这样就表示一次刷新表示的实际间隔长度
    duration = duration * getSpeed();
    // 不断从帧队列中取出帧
    while (true) {

        // 播放器已经退出，直接退出不做渲染
        if (mExit) {
            break;
        }

        // 不处于播放阶段，并且不处于强制渲染阶段
        if (!mVideoPlayer->isPlaying() && !mForceRender) {
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
                    || (pts - timestamp) > fmax(duration - 10, 0)) {  // 接近下一帧的时间
                    freeFrame(mCurrentFrame);
                    mCurrentFrame = frame;
                    setCurrentTimestamp(pts);
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
                break;
            }
        }
    }

    // 通知当前seek结束
    setSeekTime(-1);
    mCondition.signal();

    // 如果当前帧存在，则直接渲染
    if (mCurrentFrame != nullptr) {
        result = renderFrame(buffer, width, height, format);
        mForceRender = true;
    }

    return result;
}

/**
 * 渲染一帧视频
 */
int VideoStreamPlayer::renderFrame(uint8_t *buffer, int width, int height, AVPixelFormat format) {
    int result = 0;
    if (DEBUG) {
        LOGD("currentFrame->format: %s, dest format: %s, width: %d, height: %d",
             av_get_pix_fmt_name((AVPixelFormat) mCurrentFrame->format),
             av_get_pix_fmt_name(format), width, height);
    }
    if (format == AV_PIX_FMT_RGBA) {
        av_image_fill_arrays(mConvertFrame->data, mConvertFrame->linesize, buffer,
                             AV_PIX_FMT_RGBA, width, height, 1);
        switch (mCurrentFrame->format) {
            // MTK硬解码以及FFmpeg软解码都是YUV420P/YUVJ420P
            case AV_PIX_FMT_YUV420P: {
                // libyuv的ABGR排列跟Android中Surface的RGBA排列相同
                libyuv::I420ToABGR(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                   mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                   mCurrentFrame->data[2], mCurrentFrame->linesize[2],
                                   mConvertFrame->data[0], mConvertFrame->linesize[0],
                                   width, height);
                break;
            }

            case AV_PIX_FMT_YUVJ420P: {
                libyuv::J420ToABGR(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                   mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                   mCurrentFrame->data[2], mCurrentFrame->linesize[2],
                                   mConvertFrame->data[0], mConvertFrame->linesize[0],
                                   width, height);
                break;
            }

                // 非mtk的CPU硬解码得到的格式是NV12，这里先转换为I420然后再转成ABGR格式
            case AV_PIX_FMT_NV12: {
                auto tempFrame = av_frame_alloc();
                tempFrame->format = AV_PIX_FMT_YUV420P;
                tempFrame->width = mCurrentFrame->width;
                tempFrame->height = mCurrentFrame->height;
                // 分配16字节对齐的内存区域
                av_frame_get_buffer(tempFrame, 16);
                libyuv::NV12ToI420(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                   mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                   tempFrame->data[0], tempFrame->linesize[0],
                                   tempFrame->data[1], tempFrame->linesize[1],
                                   tempFrame->data[2], tempFrame->linesize[2],
                                   mCurrentFrame->width, mCurrentFrame->height);
                libyuv::I420ToABGR(tempFrame->data[0], tempFrame->linesize[0],
                                   tempFrame->data[1], tempFrame->linesize[1],
                                   tempFrame->data[2], tempFrame->linesize[2],
                                   mConvertFrame->data[0], mConvertFrame->linesize[0],
                                   width, height);
                av_frame_unref(tempFrame);
                av_frame_free(&tempFrame);
                break;
            }

            case AV_PIX_FMT_NV21: {
                auto tempFrame = av_frame_alloc();
                tempFrame->format = AV_PIX_FMT_YUV420P;
                tempFrame->width = mCurrentFrame->width;
                tempFrame->height = mCurrentFrame->height;
                // 分配16字节对齐的内存区域
                av_frame_get_buffer(tempFrame, 16);
                libyuv::NV21ToI420(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                   mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                   tempFrame->data[0], tempFrame->linesize[0],
                                   tempFrame->data[1], tempFrame->linesize[1],
                                   tempFrame->data[2], tempFrame->linesize[2],
                                   mCurrentFrame->width, mCurrentFrame->height);
                libyuv::I420ToABGR(tempFrame->data[0], tempFrame->linesize[0],
                                   tempFrame->data[1], tempFrame->linesize[1],
                                   tempFrame->data[2], tempFrame->linesize[2],
                                   mConvertFrame->data[0], mConvertFrame->linesize[0],
                                   width, height);
                av_frame_unref(tempFrame);
                av_frame_free(&tempFrame);
                break;
            }

                // 其他格式用SwsContext进行转码
            default: {
                if (!pSwsContext) {
                    pSwsContext = sws_getContext(mCurrentFrame->width, mCurrentFrame->height,
                                                 (AVPixelFormat)mCurrentFrame->format,
                                                 width, height, format,
                                                 SWS_BICUBIC, nullptr, nullptr, nullptr);

                }
                sws_scale(pSwsContext, mCurrentFrame->data, mCurrentFrame->linesize,
                          0, height, mConvertFrame->data, mConvertFrame->linesize);
                break;
            }
        }
        result = mConvertFrame->linesize[0];
    } else { // 其他格式转码不做处理
        av_image_fill_arrays(mConvertFrame->data, mConvertFrame->linesize, buffer,
                             format, width, height, 1);
        if (!pSwsContext) {
            pSwsContext = sws_getContext(mCurrentFrame->width, mCurrentFrame->height,
                                         (AVPixelFormat)mCurrentFrame->format,
                                         width, height, format,
                                         SWS_BICUBIC, nullptr, nullptr, nullptr);

        }
        sws_scale(pSwsContext, mCurrentFrame->data, mCurrentFrame->linesize,
                  0, height, mConvertFrame->data, mConvertFrame->linesize);
        result = mConvertFrame->linesize[0];
    }

    // 计算当前时长
    if (mPlayListener.lock() != nullptr) {
        mPlayListener.lock()->onPlaying(AVMEDIA_TYPE_VIDEO, getCurrentTimestamp());
    } else {
        LOGD("video play curent pts: %f", getCurrentTimestamp());
    }
    return result;
}

void VideoStreamPlayer::onDecodeStart() {
    LOGD("VideoStreamPlayer::onDecodeStart()");
}

void VideoStreamPlayer::onDecodeFinish() {
    LOGD("VideoStreamPlayer::onDecodeFinish()");
}

void VideoStreamPlayer::onSeekComplete(float seekTime) {
    LOGD("VideoStreamPlayer::onSeekComplete(): seekTime: %f, seeked time: %f", mSeekTime, seekTime);
    mForceRender = true;
    mCondition.signal();
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->requestRender();
    }
}

void VideoStreamPlayer::onSeekError(int ret) {
    LOGE("VideoStreamPlayer::onSeekError: %s", av_err2str(ret));
}

// ------------------------------------ 渲染线程回调 ------------------------------------------------

VideoStreamProvider::VideoStreamProvider() {
    this->player = nullptr;
}

VideoStreamProvider::~VideoStreamProvider() {
    this->player = nullptr;
}

void VideoStreamProvider::setPlayer(VideoStreamPlayer *player) {
    this->player = player;
}

int VideoStreamProvider::onVideoProvide(uint8_t *buffer, int width, int height, AVPixelFormat format) {
    if (player) {
        return player->onVideoProvide(buffer, width, height, format);
    }
    return 0;
}

// ------------------------------------ 解码线程监听器 ------------------------------------------------
VideoDecodeListener::VideoDecodeListener(VideoStreamPlayer *player) : player(player) {

}

VideoDecodeListener::~VideoDecodeListener() {
    player = nullptr;
}

void VideoDecodeListener::onDecodeStart(AVMediaType type) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr) {
        player->onDecodeStart();
    }
}

void VideoDecodeListener::onDecodeFinish(AVMediaType type) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr) {
        player->onDecodeFinish();
    }
}

void VideoDecodeListener::onSeekComplete(AVMediaType type, float seekTime) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr) {
        player->onSeekComplete(seekTime);
    }
}

void VideoDecodeListener::onSeekError(AVMediaType type, int ret) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr) {
        player->onSeekError(ret);
    }
}
