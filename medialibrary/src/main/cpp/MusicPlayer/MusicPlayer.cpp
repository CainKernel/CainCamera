//
// Created by CainHuang on 2020-01-30.
//

#include "MusicPlayer.h"

MusicPlayer::MusicPlayer() {
    mAudioPlayer = std::make_shared<AudioStreamPlayer>();
}

MusicPlayer::~MusicPlayer() {
    release();
    if (mAudioPlayer != nullptr) {
        mAudioPlayer.reset();
        mAudioPlayer = nullptr;
    }
    LOGD("MusicPlayer::destructor()");
}

void MusicPlayer::release() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->release();
    }
}

void MusicPlayer::setOnPlayingListener(std::shared_ptr<StreamPlayListener> listener) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setOnPlayingListener(listener);
    }
}

void MusicPlayer::setDataSource(const char *path) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setDataSource(path);
    }
}

void MusicPlayer::setSpeed(float speed) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setSpeed(speed);
    }
}

void MusicPlayer::setLooping(bool looping) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setLooping(looping);
    }
}

void MusicPlayer::setRange(float start, float end) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setRange(start, end);
    }
}

void MusicPlayer::setVolume(float leftVolume, float rightVolume) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setVolume(leftVolume, rightVolume);
    }
}

void MusicPlayer::start() {
    LOGD("MusicPlayer::start()");
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->start();
    }
}

void MusicPlayer::pause() {
    LOGD("MusicPlayer::pause()");
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->pause();
    }
}

void MusicPlayer::stop() {
    LOGD("MusicPlayer::stop()");
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->stop();
    }
}

void MusicPlayer::seekTo(float timeMs) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->seekTo(timeMs);
    }
}

float MusicPlayer::getDuration() {
    if (mAudioPlayer != nullptr) {
        return mAudioPlayer->getDuration();
    }
    return 0;
}

bool MusicPlayer::isLooping() {
    if (mAudioPlayer != nullptr) {
        return mAudioPlayer->isLooping();
    }
    return false;
}

bool MusicPlayer::isPlaying() {
    if (mAudioPlayer != nullptr) {
        return mAudioPlayer->isPlaying();
    }
    return false;
}


