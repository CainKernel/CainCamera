//
// Created by CainHuang on 2020-01-30.
//

#include "MusicPlayer.h"

MusicPlayer::MusicPlayer() {
    LOGD("MusicPlayer::constructor()");
    mSpeed = 1.0f;
    mLooping = false;
    mPrepared = false;
    mPlaying = false;
    mPlayListener = nullptr;
    mAudioTranscoder = nullptr;
    mSampleRate = 44100;
    mChannels = 2;
    mAudioPts = 0;

    mAudioFrame = new SafetyQueue<AVMediaData *>();
    mAudioThread = new AudioDecodeThread(mAudioFrame);
    mAudioThread->setOutput(mSampleRate, mChannels);
    mAudioProvider = std::make_shared<MusicAudioProvider>();
    auto provider = std::dynamic_pointer_cast<MusicAudioProvider>(mAudioProvider);
    provider->setPlayer(this);
    mAudioPlayer = std::make_shared<AudioSLPlayer>(mAudioProvider);
    mAudioTranscoder = std::make_shared<SonicAudioTranscoder>(mSampleRate, mChannels);
}

MusicPlayer::~MusicPlayer() {
    release();
    LOGD("MusicPlayer::destructor()");
}

void MusicPlayer::release() {
    LOGD("MusicPlayer::release()");
    stop();
    if (mAudioThread != nullptr) {
        delete mAudioThread;
        mAudioThread = nullptr;
    }
    if (mAudioFrame != nullptr) {
        delete mAudioFrame;
        mAudioFrame = nullptr;
    }
}

void MusicPlayer::setOnPlayingListener(std::shared_ptr<OnPlayListener> listener) {
    if (mPlayListener != nullptr) {
        mPlayListener.reset();
    }
    mPlayListener = listener;
}

void MusicPlayer::setDataSource(const char *path) {
    if (mAudioThread != nullptr) {
        mAudioThread->setDataSource(path);
    }
}

void MusicPlayer::setSpeed(float speed) {
    mMutex.lock();
    mSpeed = speed;
    mMutex.unlock();
}

void MusicPlayer::setLooping(bool looping) {
    mLooping = looping;
    if (mAudioThread != nullptr) {
        mAudioThread->setLooping(looping);
    }
}

void MusicPlayer::setRange(float start, float end) {
    if (mAudioThread != nullptr) {
        mAudioThread->setRange(start, end);
    }
}

void MusicPlayer::setVolume(float leftVolume, float rightVolume) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setStereoVolume(leftVolume, rightVolume);
    }
}

void MusicPlayer::start() {
    LOGD("MusicPlayer::start()");
    if (!mAudioThread || !mAudioPlayer) {
        return;
    }
    if (!mPrepared) {
        int ret = mAudioThread->prepare();
        if (ret < 0) {
            return;
        }
        ret = mAudioPlayer->open(mSampleRate, mChannels);
        if (ret < 0) {
            return;
        }
        mPrepared = true;
    }
    mAudioThread->start();
    mAudioPlayer->start();
    mPlaying = true;
}

void MusicPlayer::pause() {
    LOGD("MusicPlayer::pause()");
    mPlaying = false;
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->pause();
    }
    if (mAudioThread != nullptr) {
        mAudioThread->pause();
    }
}

void MusicPlayer::stop() {
    LOGD("MusicPlayer::stop()");
    mPlaying = false;
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->stop();
    }
    if (mAudioThread != nullptr) {
        mAudioThread->stop();
    }
}

void MusicPlayer::seekTo(float timeMs) {
    if (mAudioThread != nullptr) {
        mAudioThread->seekTo(timeMs);
    }
    mAudioPts = timeMs;
}

float MusicPlayer::getDuration() {
    if (mAudioThread != nullptr) {
        return mAudioThread->getDuration();
    }
    return 0;
}

bool MusicPlayer::isLooping() {
    return mLooping;
}

bool MusicPlayer::isPlaying() {
    return mPlaying;
}

int MusicPlayer::onAudioProvider(short **buffer, int bufSize) {
    if (mAudioFrame == nullptr) {
        LOGE("audio frame is null or exit!");
        return 0;
    }
    // 等待解码数据
    while (!mPlaying || mAudioFrame->empty()) {
        av_usleep(10 * 1000);
    }
    mMutex.lock();
    if (mAudioTranscoder != nullptr && mSpeed != mAudioTranscoder->getSpeed()) {
        mAudioTranscoder->setSpeed(mSpeed);
        mAudioTranscoder->flush();
    }
    mMutex.unlock();
    if (!mAudioFrame->empty()) {
        int size = 0;
        while (true) {
            if (mAudioFrame->empty()) {
                break;
            }
            auto data = mAudioFrame->pop();
            size = mAudioTranscoder->transcode(data, buffer, bufSize, mAudioPts);
            if (size > 0) {
                break;
            }
        }

        // 播放回调
        if (size > 0) {
            if (mPlayListener != nullptr) {
                mPlayListener->onPlaying(mAudioPts);
            } else {
                LOGD("audio play size: %d, pts(ms): %d", size, mAudioPts);
            }
        }
        return size;
    }
    return 0;
}

// ----------------------------------------- 音频播放回调 --------------------------------------------
MusicAudioProvider::MusicAudioProvider() {
    this->player = nullptr;
}

MusicAudioProvider::~MusicAudioProvider() {
    player = nullptr;
}

int MusicAudioProvider::onAudioProvide(short **buffer, int bufSize) {
    if (player) {
        return player->onAudioProvider(buffer, bufSize);
    }
    return 0;
}

void MusicAudioProvider::setPlayer(MusicPlayer *player) {
    this->player = player;
}


