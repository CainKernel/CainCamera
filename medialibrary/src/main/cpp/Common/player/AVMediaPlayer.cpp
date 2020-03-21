//
// Created by CainHuang on 2020-03-14.
//

#include "AVMediaPlayer.h"

AVMediaPlayer::AVMediaPlayer() {
    LOGD("FFMediaPlayer::constructor()");
    mVideoPlayer = std::make_shared<VideoStreamPlayer>(/*mStreamPlayListener*/);
    mAudioPlayer = std::make_shared<AudioStreamPlayer>(/*mStreamPlayListener*/);
    mMessageQueue = std::unique_ptr<MessageQueue>(new MessageQueue());
    mLooping = false;
}

AVMediaPlayer::~AVMediaPlayer() {
    release();
    LOGD("AVMediaPlayer::destructor()");
}

void AVMediaPlayer::release() {
    LOGD("AVMediaPlayer::release()");
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->release();
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->release();
    }
    if (mMessageQueue != nullptr) {
        mMessageQueue->flush();
        mMessageQueue.reset();
        mMessageQueue = nullptr;
    }
}

void AVMediaPlayer::setDataSource(const char *path) {
    LOGD("AVMediaPlayer::setDataSource(): %s", path);
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setDataSource(path);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setDataSource(path);
    }
}

void AVMediaPlayer::setAudioDecoder(const char *decoder) {
    LOGD("AVMediaPlayer::setAudioDecoder(): %s", decoder);
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setDecoderName(decoder);
    }
}

void AVMediaPlayer::setVideoDecoder(const char *decoder) {
    LOGD("AVMediaPlayer::setVideoDecoder(): %s", decoder);
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setDecoderName(decoder);
    }
}

#if defined(__ANDROID__)
void AVMediaPlayer::setVideoSurface(ANativeWindow *window) {
    LOGD("AVMediaPlayer::setVideoSurface()");
    if (mVideoPlayer != nullptr) {
        auto play = mVideoPlayer->getPlayer();
        auto videoPlayer = std::dynamic_pointer_cast<AVideoPlay>(play);
        if (videoPlayer != nullptr) {
            videoPlayer->setOutputSurface(window);
        }
    }
}
#endif

void AVMediaPlayer::setSpeed(float speed) {
    LOGD("AVMediaPlayer::setSpeed(): %.2f", speed);
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setSpeed(speed);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setSpeed(speed);
    }
}

void AVMediaPlayer::setLooping(bool looping) {
    LOGD("AVMediaPlayer::setLooping(): %d", looping);
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setLooping(looping);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setLooping(looping);
    }
}

void AVMediaPlayer::setRange(float start, float end) {
    LOGD("AVMediaPlayer::setRange(): {%.2f, %.2f}", start, end);
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setRange(start, end);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setRange(start, end);
    }
}

void AVMediaPlayer::setVolume(float leftVolume, float rightVolume) {
    LOGD("AVMediaPlayer::setVolume(): {%.2f, %.2f}", leftVolume, rightVolume);
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setVolume(leftVolume, rightVolume);
    }
}

void AVMediaPlayer::start() {
    LOGD("AVMediaPlayer::start()");
    if (mThread == nullptr) {
        mThread = new Thread(this);
    }
    if (!mThread->isActive()) {
        mMessageQueue->pushMessage(new Message(OPT_START));
        mCondition.signal();
        mThread->start();
    }
}

void AVMediaPlayer::pause() {
    LOGD("AVMediaPlayer::pause()");
    mMessageQueue->pushMessage(new Message(OPT_PAUSE));
    mCondition.signal();
}

void AVMediaPlayer::stop() {
    LOGD("AVMediaPlayer::stop()");
    mMessageQueue->pushMessage(new Message(OPT_STOP));
    mCondition.signal();
    if (mThread != nullptr) {
        mThread->join();
        delete mThread;
        mThread = nullptr;
    }
}

void AVMediaPlayer::seekTo(float timeMs) {
    LOGD("AVMediaPlayer::seekTo(): %.2f", timeMs);
    float *ptr = &timeMs;
    mMessageQueue->pushMessage(new Message(OPT_SEEK, ptr));
    mCondition.signal();
}

float AVMediaPlayer::getDuration() {
    float duration = 0;
    if (mAudioPlayer != nullptr) {
        duration = mAudioPlayer->getDuration();
    }
    if (mVideoPlayer != nullptr && duration < mVideoPlayer->getDuration()) {
        duration = mVideoPlayer->getDuration();
    }
    return duration;
}

int AVMediaPlayer::getVideoWidth() {
    if (mVideoPlayer != nullptr) {
        return mVideoPlayer->getVideoWidth();
    }
    return 0;
}

int AVMediaPlayer::getVideoHeight() {
    if (mVideoPlayer != nullptr) {
        return mVideoPlayer->getVideoHeight();
    }
    return 0;
}

bool AVMediaPlayer::isLooping() {
    return mLooping;
}

bool AVMediaPlayer::isPlaying() {
    bool playing = false;
    if (mAudioPlayer != nullptr) {
        playing |= mAudioPlayer->isPlaying();
    }
    if (mVideoPlayer != nullptr) {
        playing |= mVideoPlayer->isPlaying();
    }
    return playing;
}

void AVMediaPlayer::run() {
    bool abortRequest = false;
    while (true) {

        if (abortRequest) {
            break;
        }

        mMutex.lock();
        if (mMessageQueue->empty()) {
            mCondition.wait(mMutex);
        }
        mMutex.unlock();

        auto message = mMessageQueue->popMessage();
        int what = message->getWhat();
        switch(what) {
            // 开始
            case OPT_START: {
                startPlayer();
                break;
            }

            // 暂停
            case OPT_PAUSE: {
                pausePlayer();
                break;
            }

            // 停止
            case OPT_STOP: {
                stopPlayer();
                abortRequest = true;
                break;
            }

            // 定位
            case OPT_SEEK: {
                float *timeMs = (float *)message->getObj();
                seekPlayer(*timeMs);
                break;
            }

            default: {
                break;
            }
        }
        delete message;
    }
}

/**
 * 播放器开启
 */
void AVMediaPlayer::startPlayer() {
    LOGD("AVMediaPlayer::startPlayer()");
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->start();
    }
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->start();
    }
}

/**
 * 暂停播放器
 */
void AVMediaPlayer::pausePlayer() {
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->pause();
    }
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->pause();
    }
}

/**
 * 停止播放器
 */
void AVMediaPlayer::stopPlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->stop();
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->stop();
    }
}

/**
 * 定位到某个位置
 * @param timeMs
 */
void AVMediaPlayer::seekPlayer(float timeMs) {
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->seekTo(timeMs);
    }
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->seekTo(timeMs);
    }
}
