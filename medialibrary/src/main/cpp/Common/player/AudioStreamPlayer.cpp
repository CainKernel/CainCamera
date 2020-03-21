//
// Created by CainHuang on 2020-02-27.
//

#include "AudioStreamPlayer.h"

AudioStreamPlayer::AudioStreamPlayer(const std::shared_ptr<StreamPlayListener> &listener) {
    LOGD("AudioStreamPlayer::constructor()");
    mSpeed = 1.0f;
    mLooping = false;
    mPrepared = false;
    mPlaying = false;
    mPlayListener = listener;
    mAudioTranscoder = nullptr;
    mSampleRate = 44100;
    mChannels = 2;
    mCurrentPts = 0;

    mDecodeListener = std::make_shared<AudioDecodeListener>(this);
    mFrameQueue = new SafetyQueue<AVMediaData *>();
    mAudioThread = std::make_shared<DecodeAudioThread>();
    mAudioThread->setDecodeFrameQueue(mFrameQueue);
    mAudioThread->setOnDecodeListener(mDecodeListener);
    mAudioThread->setOutput(mSampleRate, mChannels);
    mAudioProvider = std::make_shared<StreamAudioProvider>();
    auto provider = std::dynamic_pointer_cast<StreamAudioProvider>(mAudioProvider);
    provider->setPlayer(this);
    mAudioPlayer = std::make_shared<AudioSLPlay>(mAudioProvider);
    mAudioTranscoder = std::make_shared<SonicAudioTranscoder>(mSampleRate, mChannels);
}

AudioStreamPlayer::~AudioStreamPlayer() {
    release();
    LOGD("AudioStreamPlayer::destructor()");
}

void AudioStreamPlayer::release() {
    LOGD("AudioStreamPlayer::release()");
    stop();
    if (mAudioThread != nullptr) {
        mAudioThread->stop();
        mAudioThread.reset();
        mAudioThread = nullptr;
    }
    if (mDecodeListener != nullptr) {
        mDecodeListener.reset();
        mDecodeListener = nullptr;
    }
    if (mFrameQueue != nullptr) {
        delete mFrameQueue;
        mFrameQueue = nullptr;
    }
}

void AudioStreamPlayer::setOnPlayingListener(std::shared_ptr<StreamPlayListener> listener) {
    if (mPlayListener != nullptr && mPlayListener != listener) {
        mPlayListener.reset();
    }
    mPlayListener = listener;
}

void AudioStreamPlayer::setDataSource(const char *path) {
    if (mAudioThread != nullptr) {
        mAudioThread->setDataSource(path);
    }
}

void AudioStreamPlayer::setDecoderName(const char *decoder) {
    if (mAudioThread != nullptr) {
        mAudioThread->setDecodeName(decoder);
    }
}

void AudioStreamPlayer::setSpeed(float speed) {
    mSpeed = speed;
}

void AudioStreamPlayer::setLooping(bool looping) {
    mLooping = looping;
    if (mAudioThread != nullptr) {
        mAudioThread->setLooping(looping);
    }
}

void AudioStreamPlayer::setRange(float start, float end) {
    if (mAudioThread != nullptr) {
        mAudioThread->setRange(start, end);
    }
}

void AudioStreamPlayer::setVolume(float leftVolume, float rightVolume) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setStereoVolume(leftVolume, rightVolume);
    }
}

void AudioStreamPlayer::start() {
    LOGD("AudioStreamPlayer::start()");
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

void AudioStreamPlayer::pause() {
    LOGD("AudioStreamPlayer::pause()");
    mPlaying = false;
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->pause();
    }
    if (mAudioThread != nullptr) {
        mAudioThread->pause();
    }
}

void AudioStreamPlayer::stop() {
    LOGD("AudioStreamPlayer::stop()");
    mPlaying = false;
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->stop();
    }
    if (mAudioThread != nullptr) {
        mAudioThread->stop();
    }
    if (mAudioTranscoder != nullptr) {
        mAudioTranscoder->flush();
    }
    flushQueue();
}

void AudioStreamPlayer::seekTo(float timeMs) {
    if (mAudioThread != nullptr) {
        mAudioThread->seekTo(timeMs);
    }
}

float AudioStreamPlayer::getDuration() {
    if (mAudioThread != nullptr) {
        return mAudioThread->getDuration();
    }
    return 0;
}

bool AudioStreamPlayer::isLooping() {
    return mLooping;
}

bool AudioStreamPlayer::isPlaying() {
    return mPlaying;
}

/**
 * 清空对立
 */
void AudioStreamPlayer::flushQueue() {
    if (mFrameQueue != nullptr) {
        while (mFrameQueue->size() > 0) {
            auto data = mFrameQueue->pop();
            if (data != nullptr) {
                data->free();
                delete data;
            }
        }
    }
}

void AudioStreamPlayer::onDecodeStart() {
    LOGD("AudioStreamPlayer::onDecodeStart()");
}

void AudioStreamPlayer::onDecodeFinish() {
    LOGD("AudioStreamPlayer::onDecodeFinish()");
}

void AudioStreamPlayer::onSeekComplete(float seekTime) {
    LOGD("AudioStreamPlayer::onSeekComplete(): %f", seekTime);
}

void AudioStreamPlayer::onSeekError(int ret) {
    LOGE("AudioStreamPlayer::onSeekError: %s", av_err2str(ret));
}

int AudioStreamPlayer::onAudioProvide(short **buffer, int bufSize) {
    if (mFrameQueue == nullptr) {
        LOGE("audio frame is null or not exit!");
        return 0;
    }
    // 等待解码数据
    if (mAudioTranscoder != nullptr && mSpeed != mAudioTranscoder->getSpeed()) {
        mAudioTranscoder->setSpeed(mSpeed);
        mAudioTranscoder->flush();
    }
    if (!mFrameQueue->empty()) {
        int size = 0;
        while (true) {
            if (mFrameQueue->empty()) {
                break;
            }
            auto data = mFrameQueue->pop();
            if (!data) {
                break;
            }
            size = mAudioTranscoder->transcode(data, buffer, bufSize, mCurrentPts);
            if (size > 0) {
                break;
            }
        }

        // 播放回调
        if (size > 0) {
            if (mPlayListener != nullptr) {
                mPlayListener->onPlaying(AVMEDIA_TYPE_AUDIO, mCurrentPts);
            } else {
                LOGD("audio play size: %d, pts(ms): %d", size, mCurrentPts);
            }
        }
        return size;
    }
    return 0;
}

// ------------------------------------ 播放线程回调 ------------------------------------------------

StreamAudioProvider::StreamAudioProvider() {
    this->player = nullptr;
}

StreamAudioProvider::~StreamAudioProvider() {
    this->player = nullptr;
}

int StreamAudioProvider::onAudioProvide(short **buffer, int bufSize) {
    if (player) {
        return player->onAudioProvide(buffer, bufSize);
    }
    return 0;
}

void StreamAudioProvider::setPlayer(AudioStreamPlayer *player) {
    this->player = player;
}

// ------------------------------------ 解码线程监听器 ------------------------------------------------

AudioDecodeListener::AudioDecodeListener(AudioStreamPlayer *player) : player(player) {

}

AudioDecodeListener::~AudioDecodeListener() {
    player = nullptr;
}

void AudioDecodeListener::onDecodeStart(AVMediaType type) {
    if (type == AVMEDIA_TYPE_AUDIO && player != nullptr) {
        player->onDecodeStart();
    }
}

void AudioDecodeListener::onDecodeFinish(AVMediaType type) {
    if (type == AVMEDIA_TYPE_AUDIO && player != nullptr) {
        player->onDecodeFinish();
    }
}

void AudioDecodeListener::onSeekComplete(AVMediaType type, float seekTime) {
    if (type == AVMEDIA_TYPE_AUDIO && player != nullptr) {
        player->onSeekComplete(seekTime);
    }
}

void AudioDecodeListener::onSeekError(AVMediaType type, int ret) {
    if (type == AVMEDIA_TYPE_AUDIO && player != nullptr) {
        player->onSeekError(ret);
    }
}
