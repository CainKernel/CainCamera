//
// Created by CainHuang on 2020-02-24.
//

#include "FFMediaPlayer.h"

FFMediaPlayer::FFMediaPlayer() {
    LOGD("FFMediaPlayer::constructor()");
    mStreamPlayListener = std::make_shared<VideoStreamPlayerListener>(this);
    mVideoPlayer = std::make_shared<VideoStreamPlayer>(mStreamPlayListener);
    mAudioPlayer = std::make_shared<AudioStreamPlayer>(mStreamPlayListener);
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
    if (window != nullptr && mVideoPlayer != nullptr) {
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

void FFMediaPlayer::start() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->start();
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->start();
    }
}

void FFMediaPlayer::pause() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->pause();
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->pause();
    }
}

void FFMediaPlayer::stop() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->stop();
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->stop();
    }
}

void FFMediaPlayer::seekTo(float timeMs) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->seekTo(timeMs);
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->seekTo(timeMs);
    }
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

bool FFMediaPlayer::isLooping() {
    return mVideoPlayer->isLooping();
}

bool FFMediaPlayer::isPlaying() {
    return mVideoPlayer->isPlaying();
}

void FFMediaPlayer::release() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->release();
    }
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->release();
    }
}

VideoStreamPlayerListener::VideoStreamPlayerListener(FFMediaPlayer *player) {
    this->player = player;
}

VideoStreamPlayerListener::~VideoStreamPlayerListener() {
    this->player = nullptr;
}

void VideoStreamPlayerListener::onPlaying(AVMediaType type, float pts) {
    LOGD("current type: %s, pts: %f", av_get_media_type_string(type), pts);
}

void VideoStreamPlayerListener::onSeekComplete(AVMediaType type) {

}

void VideoStreamPlayerListener::onCompletion(AVMediaType type) {

}

void VideoStreamPlayerListener::onError(AVMediaType type, int errorCode, const char *msg) {

}
