//
// Created by cain on 2018/4/28.
//

#include "AVSynchronizer.h"


AVSynchronizer::AVSynchronizer(AVAudioDecoder *audioDecoder, AVVideoDecoder *videoDecoder,
                               MediaStatus *status, MediaJniCall *jniCall) {
    this->audioDecoder = audioDecoder;
    this->videoDecoder = videoDecoder;
    this->mediaStatus = status;
    this->mediaJniCall = jniCall;
    nativeWindow = NULL;
    mVideoLooper = new VideoOutputLooper(videoDecoder);
    isExit = true;
    pthread_mutex_init(&mMutex, NULL);
}

AVSynchronizer::~AVSynchronizer() {
    audioDecoder = NULL;
    videoDecoder = NULL;
    mediaStatus = NULL;
    mediaJniCall = NULL;
    nativeWindow = NULL;
    if (mVideoLooper != NULL) {
        mVideoLooper->stop();
        delete(mVideoLooper);
        mVideoLooper = NULL;
    }
    pthread_mutex_destroy(&mMutex);
}

/**
 * 设置Surface
 * @param window
 */
void AVSynchronizer::setSurface(ANativeWindow *window) {
    this->nativeWindow = window;
    if (mVideoLooper != NULL) {
        mVideoLooper->onSurfaceCreated(nativeWindow);
    }
}

/**
 * Surface大小发生变化
 * @param width
 * @param height
 */
void AVSynchronizer::setSurfaceChanged(int width, int height) {
    if (mVideoLooper != NULL) {
        mVideoLooper->onSurfaceChanged(width, height);
    }
}

/**
 * 开始渲染
 */
void AVSynchronizer::start() {
    if (!videoDecoder) {
        return;
    }
    // 创建渲染线程
    renderThread = ThreadCreate(renderThreadHandle, this, "Render Thread");
}

/**
 * 停止渲染线程
 * @return
 */
void AVSynchronizer::stop() {
    pthread_mutex_lock(&mMutex);
    isExit = true;
    pthread_mutex_unlock(&mMutex);
    if (mVideoLooper != NULL) {
        mVideoLooper->onSurfaceDestroyed();
        mVideoLooper->stop();
    }
}
/**
 * 渲染视频帧
 */
void AVSynchronizer::renderFrame() {

    while (!mediaStatus->isExit()) {
        pthread_mutex_lock(&mMutex);
        isExit = false;
        pthread_mutex_unlock(&mMutex);

        // 如果处于暂停状态，则等待
        if (mediaStatus->isPause()) {
            continue;
        }

        // 如果处于定位状态，则继续
        if (mediaStatus->isSeek()) {
            if (mediaJniCall) {
                mediaJniCall->onLoad(WORKER_THREAD, true);
            }
            mediaStatus->setLoad(true);
            continue;
        }

        // 从AVFrame帧队列中取出数据
        pthread_mutex_lock(&mMutex);
        AVFrame *frame = av_frame_alloc();
        if (videoDecoder->getFrame(frame) != 0) {
            av_frame_free(&frame);
            av_free(frame);
            frame = NULL;
            pthread_mutex_unlock(&mMutex);
            continue;
        }

        // 计算显示时间戳(pts)
        if ((framePts = av_frame_get_best_effort_timestamp(frame)) == AV_NOPTS_VALUE) {
            framePts = 0;
        }
        // 计算实际的pts
        AVRational time = videoDecoder->getTimeBase();
        framePts *= av_q2d(time);
        // 同步时钟
        clock = synchronize(frame, framePts);

        // 计算当前视频帧与音频时钟的差值
        double diff = 0;
        if (audioDecoder != NULL) {
            diff = audioDecoder->getClock() - clock;
        }

        // 根据音视频时钟差值计算延时时长
        delayTime = getDelayTime(diff);
        playcount++;
        if (playcount > 500) {
            playcount = 0;
        }

        // 如果差值大于0.5，则判断是否高帧率视频(fps > 60)
        if (diff >= 0.5) {
            if (videoDecoder && videoDecoder->isBigFrameRate()) {
                // 舍帧策略
                if (playcount % 3 == 0) {
                    av_frame_free(&frame);
                    av_free(frame);
                    frame = NULL;
                    videoDecoder->clearToKeyPacket();
                    pthread_mutex_unlock(&mMutex);
                    continue;
                }
            } else {    // 如果差值小于0.5，则直接舍弃
                av_frame_free(&frame);
                av_free(frame);
                frame = NULL;
                videoDecoder->clearToKeyPacket();
                pthread_mutex_unlock(&mMutex);
                continue;
            }
        }

        // 延时
        av_usleep(delayTime * 1000);
        // 发送消息
        if (mediaJniCall) {
            mediaJniCall->onTimeInfo(WORKER_THREAD, clock, videoDecoder->getDuration());
        }
        // 渲染到屏幕上
        if (mVideoLooper != NULL) {
            mVideoLooper->onDisplayVideo(frame);
        }
        pthread_mutex_unlock(&mMutex);
    }
    isExit = true;
}

/**
 * 渲染线程句柄
 * @param data
 * @return
 */
int AVSynchronizer::renderThreadHandle(void *data) {
    AVSynchronizer *synchronizer = (AVSynchronizer *) data;
    synchronizer->renderFrame();
    return 0;
}

/**
 * 视频同步
 * @param srcFrame
 * @param pts
 */
double AVSynchronizer::synchronize(AVFrame *srcFrame, double pts) {
    double frame_delay;

    if (pts != 0) {
        video_clock = pts;
    } else {
        pts = video_clock;
    }

    frame_delay = av_q2d(videoDecoder->getTimeBase());
    frame_delay += srcFrame->repeat_pict * (frame_delay * 0.5);

    video_clock += frame_delay;

    return pts;
}

/**
 * 获取延时时间
 * @param diff
 * @return
 */
double AVSynchronizer::getDelayTime(double diff) {
    int rate = 0;
    if (videoDecoder) {
        rate = videoDecoder->getVideoRate();
    }
    if (diff > 0.003) {
        delayTime = delayTime / 3 * 2;
        if (delayTime < rate / 2) {
            delayTime = rate / 3 * 2;
        } else if (delayTime > rate * 2) {
            delayTime = rate * 2;
        }
    } else if (diff < -0.003) {
        delayTime = delayTime * 3 / 2;
        if (delayTime < rate / 2) {
            delayTime = rate / 3 * 2;
        } else if (delayTime > rate * 2) {
            delayTime = rate * 2;
        }
    } else if (diff == 0) {
        delayTime = rate;
    }
    if (diff > 1.0) {
        delayTime = 0;
    }
    if (diff < -1.0) {
        delayTime = rate * 2;
    }
    if (fabs(diff) > 10) {
        delayTime = rate;
    }
    return delayTime;
}

/**
 * 释放资源
 */
void AVSynchronizer::release() {
    ThreadDestroy(renderThread);
    nativeWindow = NULL;
    if (mVideoLooper != NULL) {
        mVideoLooper->stop();
        delete(mVideoLooper);
        mVideoLooper = NULL;
    }
}