//
// Created by CainHuang on 2020-02-25.
//

#include "VideoStreamPlayer.h"

VideoStreamPlayer::VideoStreamPlayer(const std::shared_ptr<StreamPlayListener> &listener) {
    LOGD("VideoStreamPlayer::constructor()");
    mFrameQueue = new SafetyQueue<Picture *>();
    mVideoThread = new VideoDecodeThread(mFrameQueue);
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
    mPlayListener = listener;
    mCurrentPts = -1;
}

VideoStreamPlayer::~VideoStreamPlayer() {
    release();
    LOGD("VideoStreamPlayer::destructor()");
}

void VideoStreamPlayer::release() {
    LOGD("VideoStreamPlayer::release()");
    stop();
    if (mVideoThread != nullptr) {
        delete mVideoThread;
        mVideoThread = nullptr;
    }
    if (mConvertFrame != nullptr) {
        freeFrame(mConvertFrame);
        mConvertFrame = nullptr;
    }
}

std::shared_ptr<VideoPlay> VideoStreamPlayer::getPlayer() {
    return mVideoPlayer;
}

void VideoStreamPlayer::freeFrame(AVFrame *frame) {
    if (frame) {
        av_frame_unref(frame);
        av_frame_free(&frame);
    }
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
    mMutex.lock();
    mSpeed = speed;
    mMutex.unlock();
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
    mVideoThread->start();
    mVideoPlayer->setRefreshRate(mVideoThread->getAvgFrameRate());
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
}

void VideoStreamPlayer::seekTo(float timeMs) {
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

bool VideoStreamPlayer::isLooping() {
    return mLooping;
}

bool VideoStreamPlayer::isPlaying() {
    return mPlaying;
}

int VideoStreamPlayer::onVideoProvide(uint8_t *buffer, int width, int height, AVPixelFormat format) {
    int result = 0;
    if (mFrameQueue == nullptr) {
        LOGE("video frame is null or exit!");
        return result;
    }

    while (!mPlaying || mFrameQueue->empty()) {
        av_usleep(10 * 1000);
    }

    if (!mFrameQueue->empty()) {
        float duration = 1000.0f / mVideoThread->getAvgFrameRate();
        // 取出视频帧
        auto picture = mFrameQueue->front();
        if (picture) {
            AVFrame *frame = picture->frame;
            if (mCurrentFrame != nullptr) {
                if (mCurrentPts < 0
                    || picture->pts < mCurrentPts                       // 下一帧小于当前pts，说明属于seek之后的结果，立即刷新
                    || (picture->pts - mCurrentPts) >= duration - 10) {   // 接近下一帧的时间
                    mFrameQueue->pop();
                    freeFrame(mCurrentFrame);
                    mCurrentFrame = frame;
                    mCurrentPts = picture->pts;
                    free(picture);
                }
            } else {
                mFrameQueue->pop();
                mCurrentFrame = frame;
                mCurrentPts = picture->pts;
                free(picture);
            }
        } else { // 空对象移除
            mFrameQueue->pop();
        }

        // 如果当前帧存在，则直接渲染
        LOGD("currentFrame->format: %s, dest format: %s, width: %d, height: %d",
                av_get_pix_fmt_name((AVPixelFormat)mCurrentFrame->format),
                av_get_pix_fmt_name(format), width, height);
        if (mCurrentFrame) {
            if (format == AV_PIX_FMT_RGBA) {
                av_image_fill_arrays(mConvertFrame->data, mConvertFrame->linesize, buffer,
                                     AV_PIX_FMT_RGBA, width, height, 1);
                switch (mCurrentFrame->format) {
                    case AV_PIX_FMT_YUV420P: {
                        libyuv::I420ToARGB(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                           mCurrentFrame->data[2], mCurrentFrame->linesize[2],
                                           mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                           mConvertFrame->data[0], mConvertFrame->linesize[0],
                                           width, height);
                        break;
                    }

                    case AV_PIX_FMT_YUVJ420P: {
                        libyuv::J420ToARGB(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                           mCurrentFrame->data[2], mCurrentFrame->linesize[2],
                                           mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                           mConvertFrame->data[0], mConvertFrame->linesize[0],
                                           width, height);
                        break;
                    }

                    case AV_PIX_FMT_NV12: {
                        // Android的NV12和NV21是反过来的？否则颜色不对劲啊
                        libyuv::NV21ToARGB(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                mConvertFrame->data[0], mConvertFrame->linesize[0],
                                width, height);
                        break;
                    }

                    case AV_PIX_FMT_NV21: {
                        // Android的NV21和NV12是反过来的？
                        libyuv::NV12ToARGB(mCurrentFrame->data[0], mCurrentFrame->linesize[1],
                                mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                mConvertFrame->data[0], mConvertFrame->linesize[0],
                                width, height);
                        break;
                    }

                    case AV_PIX_FMT_RGBA: {
                        libyuv::RGBAToARGB(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                mConvertFrame->data[0], mConvertFrame->linesize[0],
                                width, height);
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
            } else if (format == AV_PIX_FMT_YUV420P) {
                av_image_fill_arrays(mConvertFrame->data, mConvertFrame->linesize, buffer,
                                     AV_PIX_FMT_YUV420P, width, height, 1);
                switch (mCurrentFrame->format) {
                    case AV_PIX_FMT_YUV420P:
                    case AV_PIX_FMT_YUVJ420P:{
                        libyuv::I420Copy(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                mCurrentFrame->data[2], mCurrentFrame->linesize[2],
                                mConvertFrame->data[0], mConvertFrame->linesize[0],
                                mConvertFrame->data[1], mConvertFrame->linesize[1],
                                mConvertFrame->data[2], mConvertFrame->linesize[2],
                                width, height);
                        break;
                    }

                    case AV_PIX_FMT_NV12: {
                        libyuv::NV12ToI420(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                mConvertFrame->data[0], mConvertFrame->linesize[0],
                                mConvertFrame->data[1], mConvertFrame->linesize[1],
                                mConvertFrame->data[2], mConvertFrame->linesize[2],
                                width, height);
                        break;
                    }

                    case AV_PIX_FMT_NV21: {
                        libyuv::NV21ToI420(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                           mCurrentFrame->data[1], mCurrentFrame->linesize[1],
                                           mConvertFrame->data[0], mConvertFrame->linesize[0],
                                           mConvertFrame->data[1], mConvertFrame->linesize[1],
                                           mConvertFrame->data[2], mConvertFrame->linesize[2],
                                           width, height);
                        break;
                    }

                    case AV_PIX_FMT_RGBA: {
                        libyuv::RGBAToI420(mCurrentFrame->data[0], mCurrentFrame->linesize[0],
                                           mConvertFrame->data[0], mConvertFrame->linesize[0],
                                           mConvertFrame->data[1], mConvertFrame->linesize[1],
                                           mConvertFrame->data[2], mConvertFrame->linesize[2],
                                           width, height);
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
            }

            // 计算当前时长
            if (mPlayListener.lock() != nullptr) {
                mPlayListener.lock()->onPlaying(AVMEDIA_TYPE_VIDEO, mCurrentPts);
            } else {
                LOGD("video play curent pts: %f", mCurrentPts);
            }
        }
    }

    return result;
}


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
