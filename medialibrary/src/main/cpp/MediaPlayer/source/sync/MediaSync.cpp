//
// Created by cain on 2018/12/30.
//

#include "MediaSync.h"

MediaSync::MediaSync(PlayerState *playerState) {
    this->playerState = playerState;
    audioDecoder = NULL;
    videoDecoder = NULL;
    audioClock = new MediaClock();
    videoClock = new MediaClock();
    extClock = new MediaClock();

    mExit = true;
    abortRequest = true;
    syncThread = NULL;

    forceRefresh = 0;
    maxFrameDuration = 10.0;
    frameTimerRefresh = 1;
    frameTimer = 0;


    videoDevice = NULL;
    swsContext = NULL;
    mBuffer = NULL;
    pFrameARGB = NULL;
}

MediaSync::~MediaSync() {

}

void MediaSync::reset() {
    stop();
    playerState = NULL;
    videoDecoder = NULL;
    audioDecoder = NULL;
    videoDevice = NULL;

    if (pFrameARGB) {
        av_frame_free(&pFrameARGB);
        av_free(pFrameARGB);
        pFrameARGB = NULL;
    }
    if (mBuffer) {
        av_freep(&mBuffer);
        mBuffer = NULL;
    }
    if (swsContext) {
        sws_freeContext(swsContext);
        swsContext = NULL;
    }
}

void MediaSync::start(VideoDecoder *videoDecoder, AudioDecoder *audioDecoder) {
    mMutex.lock();
    this->videoDecoder = videoDecoder;
    this->audioDecoder = audioDecoder;
    abortRequest = false;
    mExit = false;
    mCondition.signal();
    mMutex.unlock();
    if (videoDecoder && !syncThread) {
        syncThread = new Thread(this);
        syncThread->start();
    }
}

void MediaSync::stop() {
    mMutex.lock();
    abortRequest = true;
    mCondition.signal();
    mMutex.unlock();

    mMutex.lock();
    while (!mExit) {
        mCondition.wait(mMutex);
    }
    mMutex.unlock();
    if (syncThread) {
        syncThread->join();
        delete syncThread;
        syncThread = NULL;
    }
}

void MediaSync::setVideoDevice(VideoDevice *device) {
    Mutex::Autolock lock(mMutex);
    this->videoDevice = device;
}

void MediaSync::setMaxDuration(double maxDuration) {
    this->maxFrameDuration = maxDuration;
}

void MediaSync::refreshVideoTimer() {
    mMutex.lock();
    this->frameTimerRefresh = 1;
    mCondition.signal();
    mMutex.unlock();
}

void MediaSync::updateAudioClock(double pts, double time) {
    audioClock->setClock(pts, time);
    extClock->syncToSlave(audioClock);
}

double MediaSync::getAudioDiffClock() {
    return audioClock->getClock() - getMasterClock();
}

void MediaSync::updateExternalClock(double pts) {
    extClock->setClock(pts);
}

double MediaSync::getMasterClock() {
    double val = 0;
    switch (playerState->syncType) {
        case AV_SYNC_VIDEO: {
            val = videoClock->getClock();
            break;
        }
        case AV_SYNC_AUDIO: {
            val = audioClock->getClock();
            break;
        }
        case AV_SYNC_EXTERNAL: {
            val = extClock->getClock();
            break;
        }
    }
    return val;
}

MediaClock* MediaSync::getAudioClock() {
    return audioClock;
}

MediaClock *MediaSync::getVideoClock() {
    return videoClock;
}

MediaClock *MediaSync::getExternalClock() {
    return extClock;
}

void MediaSync::run() {
    double remaining_time = 0.0;
    while (true) {

        if (abortRequest || playerState->abortRequest) {
            if (videoDevice != NULL) {
                videoDevice->terminate();
            }
            break;
        }

        if (remaining_time > 0.0) {
            av_usleep((int64_t) (remaining_time * 1000000.0));
        }
        remaining_time = REFRESH_RATE;
        if (!playerState->pauseRequest || forceRefresh) {
            refreshVideo(&remaining_time);
        }
    }

    mExit = true;
    mCondition.signal();
}

void MediaSync::refreshVideo(double *remaining_time) {
    double time;

    // 检查外部时钟
    if (!playerState->pauseRequest && playerState->realTime &&
        playerState->syncType == AV_SYNC_EXTERNAL) {
        checkExternalClockSpeed();
    }

    for (;;) {

        if (playerState->abortRequest || !videoDecoder) {
            break;
        }

        // 判断是否存在帧队列是否存在数据
        if (videoDecoder->getFrameSize() > 0) {
            double lastDuration, duration, delay;
            Frame *currentFrame, *lastFrame;
            // 上一帧
            lastFrame = videoDecoder->getFrameQueue()->lastFrame();
            // 当前帧
            currentFrame = videoDecoder->getFrameQueue()->currentFrame();
            // 判断是否需要强制更新帧的时间
            if (frameTimerRefresh) {
                frameTimer = av_gettime_relative() / 1000000.0;
                frameTimerRefresh = 0;
            }

            // 如果处于暂停状态，则直接显示
            if (playerState->abortRequest || playerState->pauseRequest) {
                break;
            }

            // 计算上一次显示时长
            lastDuration = calculateDuration(lastFrame, currentFrame);
            // 根据上一次显示的时长，计算延时
            delay = calculateDelay(lastDuration);
            // 处理超过延时阈值的情况
            if (fabs(delay) > AV_SYNC_THRESHOLD_MAX) {
                if (delay > 0) {
                    delay = AV_SYNC_THRESHOLD_MAX;
                } else {
                    delay = 0;
                }
            }
            // 获取当前时间
            time = av_gettime_relative() / 1000000.0;
            if (isnan(frameTimer) || time < frameTimer) {
                frameTimer = time;
            }
            // 如果当前时间小于帧计时器的时间 + 延时时间，则表示还没到当前帧
            if (time < frameTimer + delay) {
                *remaining_time = FFMIN(frameTimer + delay - time, *remaining_time);
                break;
            }

            // 更新帧计时器
            frameTimer += delay;
            // 帧计时器落后当前时间超过了阈值，则用当前的时间作为帧计时器时间
            if (delay > 0 && time - frameTimer > AV_SYNC_THRESHOLD_MAX) {
                frameTimer = time;
            }

            // 更新视频时钟的pts
            mMutex.lock();
            if (!isnan(currentFrame->pts)) {
                videoClock->setClock(currentFrame->pts);
                extClock->syncToSlave(videoClock);
            }
            mMutex.unlock();

            // 如果队列中还剩余超过一帧的数据时，需要拿到下一帧，然后计算间隔，并判断是否需要进行舍帧操作
            if (videoDecoder->getFrameSize() > 1) {
                Frame *nextFrame = videoDecoder->getFrameQueue()->nextFrame();
                duration = calculateDuration(currentFrame, nextFrame);
                // 如果不处于同步到视频状态，并且处于跳帧状态，则跳过当前帧
                if ((time > frameTimer + duration)
                    && (playerState->frameDrop > 0
                        || (playerState->frameDrop && playerState->syncType != AV_SYNC_VIDEO))) {
                    videoDecoder->getFrameQueue()->popFrame();
                    continue;
                }
            }

            // 下一帧
            videoDecoder->getFrameQueue()->popFrame();
            forceRefresh = 1;
        }

        break;
    }

    // 显示画面
    if (!playerState->displayDisable && forceRefresh && videoDecoder
        && videoDecoder->getFrameQueue()->getShowIndex()) {
        renderVideo();
    }
    forceRefresh = 0;
}

void MediaSync::checkExternalClockSpeed() {
    if (videoDecoder && videoDecoder->getPacketSize() <= EXTERNAL_CLOCK_MIN_FRAMES
        || audioDecoder && audioDecoder->getPacketSize() <= EXTERNAL_CLOCK_MIN_FRAMES) {
        extClock->setSpeed(FFMAX(EXTERNAL_CLOCK_SPEED_MIN,
                                 extClock->getSpeed() - EXTERNAL_CLOCK_SPEED_STEP));
    } else if ((!videoDecoder || videoDecoder->getPacketSize() > EXTERNAL_CLOCK_MAX_FRAMES)
               && (!audioDecoder || audioDecoder->getPacketSize() > EXTERNAL_CLOCK_MAX_FRAMES)) {
        extClock->setSpeed(FFMIN(EXTERNAL_CLOCK_SPEED_MAX,
                                 extClock->getSpeed() + EXTERNAL_CLOCK_SPEED_STEP));
    } else {
        double speed = extClock->getSpeed();
        if (speed != 1.0) {
            extClock->setSpeed(speed + EXTERNAL_CLOCK_SPEED_STEP * (1.0 - speed) / fabs(1.0 - speed));
        }
    }
}

double MediaSync::calculateDelay(double delay)  {
    double sync_threshold, diff = 0;
    // 如果不是同步到视频流，则需要计算延时时间
    if (playerState->syncType != AV_SYNC_VIDEO) {
        // 计算差值
        diff = videoClock->getClock() - getMasterClock();
        // 用差值与同步阈值计算延时
        sync_threshold = FFMAX(AV_SYNC_THRESHOLD_MIN, FFMIN(AV_SYNC_THRESHOLD_MAX, delay));
        if (!isnan(diff) && fabs(diff) < maxFrameDuration) {
            if (diff <= -sync_threshold) {
                delay = FFMAX(0, delay + diff);
            } else if (diff >= sync_threshold && delay > AV_SYNC_FRAMEDUP_THRESHOLD) {
                delay = delay + diff;
            } else if (diff >= sync_threshold) {
                delay = 2 * delay;
            }
        }
    }

    av_log(NULL, AV_LOG_TRACE, "video: delay=%0.3f A-V=%f\n", delay, -diff);

    return delay;
}

double MediaSync::calculateDuration(Frame *vp, Frame *nextvp) {
    double duration = nextvp->pts - vp->pts;
    if (isnan(duration) || duration <= 0 || duration > maxFrameDuration) {
        return vp->duration;
    } else {
        return duration;
    }
}

void MediaSync::renderVideo() {
    mMutex.lock();
    if (!videoDecoder || !videoDevice) {
        mMutex.unlock();
        return;
    }
    Frame *vp = videoDecoder->getFrameQueue()->lastFrame();
    int ret = 0;
    if (!vp->uploaded) {
        // 根据图像格式更新纹理数据
        switch (vp->frame->format) {
            // YUV420P 和 YUVJ420P 除了色彩空间不一样之外，其他的没什么区别
            // YUV420P表示的范围是 16 ~ 235，而YUVJ420P表示的范围是0 ~ 255
            // 这里做了兼容处理，后续可以优化，shader已经过验证
            case AV_PIX_FMT_YUVJ420P:
            case AV_PIX_FMT_YUV420P: {

                // 初始化纹理
                videoDevice->onInitTexture(vp->frame->width, vp->frame->height,
                                           FMT_YUV420P, BLEND_NONE);

                if (vp->frame->linesize[0] < 0 || vp->frame->linesize[1] < 0 || vp->frame->linesize[2] < 0) {
                    av_log(NULL, AV_LOG_ERROR, "Negative linesize is not supported for YUV.\n");
                    return;
                }
                ret = videoDevice->onUpdateYUV(vp->frame->data[0], vp->frame->linesize[0],
                                               vp->frame->data[1], vp->frame->linesize[1],
                                               vp->frame->data[2], vp->frame->linesize[2]);
                if (ret < 0) {
                    return;
                }
                break;
            }

            // 直接渲染BGRA，对应的是shader->argb格式
            case AV_PIX_FMT_BGRA: {
                videoDevice->onInitTexture(vp->frame->width, vp->frame->height,
                                           FMT_ARGB, BLEND_NONE);
                ret = videoDevice->onUpdateARGB(vp->frame->data[0], vp->frame->linesize[0]);
                if (ret < 0) {
                    return;
                }
                break;
            }

            // 其他格式转码成BGRA格式再做渲染
            default: {
                swsContext = sws_getCachedContext(swsContext,
                                                  vp->frame->width, vp->frame->height,
                                                  (AVPixelFormat) vp->frame->format,
                                                  vp->frame->width, vp->frame->height,
                                                  AV_PIX_FMT_BGRA, SWS_BICUBIC, NULL, NULL, NULL);
                if (!mBuffer) {
                    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_BGRA, vp->frame->width, vp->frame->height, 1);
                    mBuffer = (uint8_t *)av_malloc(numBytes * sizeof(uint8_t));
                    pFrameARGB = av_frame_alloc();
                    av_image_fill_arrays(pFrameARGB->data, pFrameARGB->linesize, mBuffer, AV_PIX_FMT_BGRA,
                                         vp->frame->width, vp->frame->height, 1);
                }
                if (swsContext != NULL) {
                    sws_scale(swsContext, (uint8_t const *const *) vp->frame->data,
                              vp->frame->linesize, 0, vp->frame->height,
                              pFrameARGB->data, pFrameARGB->linesize);
                }

                videoDevice->onInitTexture(vp->frame->width, vp->frame->height,
                                           FMT_ARGB, BLEND_NONE);
                ret = videoDevice->onUpdateARGB(pFrameARGB->data[0], pFrameARGB->linesize[0]);
                if (ret < 0) {
                    return;
                }
                break;
            }
        }
        vp->uploaded = 1;
    }
    // 请求渲染视频
    if (videoDevice != NULL) {
        videoDevice->onRequestRender(vp->frame->linesize[0] < 0 ? FLIP_VERTICAL : FLIP_NONE);
    }
    mMutex.unlock();
}
