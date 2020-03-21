//
// Created by CainHuang on 2020-02-24.
//

#include "FFMediaPlayer.h"

FFMediaPlayer::FFMediaPlayer() {
    LOGD("FFMediaPlayer::constructor()");
    mThread = nullptr;
    mStreamPlayListener = std::make_shared<MediaStreamPlayerListener>(this);
    mVideoPlayer = std::make_shared<VideoStreamPlayer>(mStreamPlayListener);
    mAudioPlayer = std::make_shared<AudioStreamPlayer>(mStreamPlayListener);
    mMessageQueue = std::unique_ptr<MessageQueue>(new MessageQueue());
}

FFMediaPlayer::~FFMediaPlayer() {
    release();
    LOGD("FFMediaPlayer::destructor()");
}

void FFMediaPlayer::setVideoPlayListener(std::shared_ptr<OnPlayListener> listener) {
    if (mPlayListener != nullptr) {
        mPlayListener.reset();
        mPlayListener = nullptr;
    }
    mPlayListener = listener;
}

void FFMediaPlayer::setDataSource(const char *path) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setDataSource(path);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setDataSource(path);
    }
}

void FFMediaPlayer::setAudioDecoder(const char *decoder) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setDecoderName(decoder);
    }
}

void FFMediaPlayer::setVideoDecoder(const char *decoder) {
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setDecoderName(decoder);
    }
}

void FFMediaPlayer::setVideoSurface(ANativeWindow *window) {
    if (mVideoPlayer != nullptr) {
        auto play = mVideoPlayer->getPlayer();
        auto videoPlayer = std::dynamic_pointer_cast<AVideoPlay>(play);
        if (videoPlayer != nullptr) {
            videoPlayer->setOutputSurface(window);
        }
    }
}

void FFMediaPlayer::setSpeed(float speed) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setSpeed(speed);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setSpeed(speed);
    }
}

void FFMediaPlayer::setLooping(bool looping) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setLooping(looping);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setLooping(looping);
    }
}

void FFMediaPlayer::setRange(float start, float end) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setRange(start, end);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setRange(start, end);
    }
}

void FFMediaPlayer::setVolume(float leftVolume, float rightVolume) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setVolume(leftVolume, rightVolume);
    }
}

void FFMediaPlayer::start() {
    if (mThread == nullptr) {
        mThread = new Thread(this);
    }
    if (!mThread->isActive()) {
        mThread->start();
    }
    mMessageQueue->pushMessage(new Message(OPT_START));
    mCondition.signal();
}

void FFMediaPlayer::pause() {
    mMessageQueue->pushMessage(new Message(OPT_PAUSE));
    mCondition.signal();
}

void FFMediaPlayer::stop() {
    mMessageQueue->pushMessage(new Message(OPT_STOP));
    mCondition.signal();
    if (mThread != nullptr) {
        mThread->join();
        delete mThread;
        mThread = nullptr;
    }
}

void FFMediaPlayer::setDecodeOnPause(bool decodeOnPause) {
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setDecodeOnPause(decodeOnPause);
    }
}

void FFMediaPlayer::seekTo(float timeMs) {
    mMessageQueue->pushMessage(new Message(OPT_SEEK, (int)(timeMs * 1000), -1));
    mCondition.signal();
}

float FFMediaPlayer::getDuration() {
    float duration = 0;
    if (mAudioPlayer != nullptr) {
        duration = mAudioPlayer->getDuration();
    }
    if (mVideoPlayer != nullptr && duration < mVideoPlayer->getDuration()) {
        duration = mVideoPlayer->getDuration();
    }
    return duration;
}

int FFMediaPlayer::getVideoWidth() {
    if (mVideoPlayer != nullptr) {
        return mVideoPlayer->getVideoWidth();
    }
    return 0;
}

int FFMediaPlayer::getVideoHeight() {
    if (mVideoPlayer != nullptr) {
        return mVideoPlayer->getVideoHeight();
    }
    return 0;
}

bool FFMediaPlayer::isLooping() {
    return mVideoPlayer->isLooping();
}

bool FFMediaPlayer::isPlaying() {
    return mVideoPlayer->isPlaying();
}

void FFMediaPlayer::release() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->release();
        mAudioPlayer.reset();
        mAudioPlayer = nullptr;
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->release();
        mVideoPlayer.reset();
        mVideoPlayer = nullptr;
    }
    if (mMessageQueue != nullptr) {
        mMessageQueue->flush();
        mMessageQueue.reset();
        mMessageQueue = nullptr;
    }
}

std::shared_ptr<OnPlayListener> FFMediaPlayer::getPlayListener() {
    return mPlayListener;
}

void FFMediaPlayer::run() {
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
                float timeMs = message->getArg1() / 1000.0f;
                seekPlayer(timeMs);
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
 * 开始播放
 */
void FFMediaPlayer::startPlayer() {
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->start();
    }
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->start();
    }
    LOGD("start success");
}

/**
 * 暂停播放器
 */
void FFMediaPlayer::pausePlayer() {
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->pause();
    }
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->pause();
    }
    LOGD("pause finish");
}

/**
 * 停止播放器
 */
void FFMediaPlayer::stopPlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->stop();
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->stop();
    }
}

/**
 * 跳转到某个时间点
 * @param timeMs    跳转时间(ms)
 */
void FFMediaPlayer::seekPlayer(float timeMs) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->seekTo(timeMs);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->seekTo(timeMs);
    }
}

MediaStreamPlayerListener::MediaStreamPlayerListener(FFMediaPlayer *player) {
    this->player = player;
}

MediaStreamPlayerListener::~MediaStreamPlayerListener() {
    this->player = nullptr;
}

void MediaStreamPlayerListener::onPlaying(AVMediaType type, float pts) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr && player->getPlayListener() != nullptr) {
        player->getPlayListener()->onPlaying(pts);
    }
}

void MediaStreamPlayerListener::onSeekComplete(AVMediaType type) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr && player->getPlayListener() != nullptr) {
        player->getPlayListener()->onSeekComplete();
    }
}

void MediaStreamPlayerListener::onCompletion(AVMediaType type) {
    if (type == AVMEDIA_TYPE_VIDEO && player != nullptr && player->getPlayListener() != nullptr) {
        player->getPlayListener()->onCompletion();
    }
}

void MediaStreamPlayerListener::onError(AVMediaType type, int errorCode, const char *msg) {
    if (player != nullptr && player->getPlayListener() != nullptr) {
        player->getPlayListener()->onError(errorCode, msg);
    }
}
