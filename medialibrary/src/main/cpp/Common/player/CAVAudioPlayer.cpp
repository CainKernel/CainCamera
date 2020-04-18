//
// Created by CainHuang on 2020-01-30.
//

#include "CAVAudioPlayer.h"

CAVAudioPlayer::CAVAudioPlayer() {
    mStreamPlayListener = std::make_shared<AudioPlayerListener>(this);
    mAudioPlayer = std::make_shared<AudioStreamPlayer>(mStreamPlayListener);
    mMessageQueue = std::unique_ptr<MessageQueue>(new MessageQueue());
    mThread = nullptr;
    mPlayListener = nullptr;
    mAbortRequest = true;
}

CAVAudioPlayer::~CAVAudioPlayer() {
    release();
    LOGD("CAVAudioPlayer::destructor()");
}

void CAVAudioPlayer::init() {
    mAbortRequest = false;
    mCondition.signal();
    if (mThread == nullptr) {
        mThread = new Thread(this);
    }
    if (!mThread->isActive()) {
        mThread->start();
    }
}

void CAVAudioPlayer::release() {
    mAbortRequest = true;
    mCondition.signal();
    if (mThread != nullptr) {
        mThread->join();
        delete mThread;
        mThread = nullptr;
    }
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->release();
        mAudioPlayer.reset();
        mAudioPlayer = nullptr;
    }
    if (mMessageQueue != nullptr) {
        mMessageQueue->flush();
        mMessageQueue.reset();
        mMessageQueue = nullptr;
    }
    if (mStreamPlayListener != nullptr) {
        mStreamPlayListener.reset();
        mStreamPlayListener = nullptr;
    }
    if (mPlayListener != nullptr) {
        mPlayListener.reset();
        mPlayListener = nullptr;
    }
}

void CAVAudioPlayer::setOnPlayingListener(std::shared_ptr<OnPlayListener> listener) {
    if (mPlayListener != nullptr) {
        mPlayListener.reset();
        mPlayListener = nullptr;
    }
    mPlayListener = listener;
}

status_t CAVAudioPlayer::setDataSource(const char *path) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setDataSource(path);
    }
    return OK;
}

status_t CAVAudioPlayer::setSpeed(float speed) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setSpeed(speed);
    }
    return OK;
}

status_t CAVAudioPlayer::setLooping(bool looping) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setLooping(looping);
    }
    return OK;
}

status_t CAVAudioPlayer::setRange(float start, float end) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setRange(start, end);
    }
    return OK;
}

status_t CAVAudioPlayer::setVolume(float leftVolume, float rightVolume) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setVolume(leftVolume, rightVolume);
    }
    return OK;
}

status_t CAVAudioPlayer::prepare() {
    LOGD("CAVAudioPlayer::prepare()");
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_PREPARE));
    mCondition.signal();
    return OK;
}

status_t CAVAudioPlayer::start() {
    LOGD("CAVAudioPlayer::start()");
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_START));
    mCondition.signal();
    return OK;
}

status_t CAVAudioPlayer::pause() {
    LOGD("CAVAudioPlayer::pause()");
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_PAUSE));
    mCondition.signal();
    return OK;
}

status_t CAVAudioPlayer::stop() {
    LOGD("CAVAudioPlayer::stop()");
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_STOP));
    mCondition.signal();
    return OK;
}

status_t CAVAudioPlayer::seekTo(float timeMs) {
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_SEEK, (int)(timeMs * 1000), -1));
    mCondition.signal();
    return OK;
}

float CAVAudioPlayer::getDuration() {
    if (mAudioPlayer != nullptr) {
        return mAudioPlayer->getDuration();
    }
    return 0;
}

bool CAVAudioPlayer::isLooping() {
    if (mAudioPlayer != nullptr) {
        return mAudioPlayer->isLooping();
    }
    return false;
}

bool CAVAudioPlayer::isPlaying() {
    if (mAudioPlayer != nullptr) {
        return mAudioPlayer->isPlaying();
    }
    return false;
}

void CAVAudioPlayer::preparePlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->prepare();
    }
}

void CAVAudioPlayer::startPlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->start();
    }
}

void CAVAudioPlayer::pausePlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->pause();
    }
}

void CAVAudioPlayer::stopPlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->stop();
    }
}

void CAVAudioPlayer::seekPlayer(float timeMs) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->seekTo(timeMs);
    }
}

void CAVAudioPlayer::notify(int msg, int arg1, int arg2) {
    mMessageQueue->pushMessage(new Message(msg, arg1, arg2));
    mCondition.signal();
}

void CAVAudioPlayer::postEvent(int what, int arg1, int arg2, void *obj) {
    if (mPlayListener != nullptr) {
        mPlayListener->notify(what, arg1, arg2, obj);
    }
}

void CAVAudioPlayer::run() {

    while(true) {

        // 退出消息队列
        if (mAbortRequest) {
            break;
        }

        mMutex.lock();
        if (mMessageQueue->empty()) {
            mCondition.wait(mMutex);
        }
        mMutex.unlock();

        auto msg = mMessageQueue->popMessage();
        if (!msg) {
            continue;
        }
        int what = msg->getWhat();
        switch (what) {

            // 刷新缓冲区
            case MSG_FLUSH: {
                LOGD("CAVAudioPlayer is flushing.\n");
                break;
            }

            // 播放出错
            case MSG_ERROR: {
                LOGD("CAVAudioPlayer occurs error: %d\n", msg->getArg1());
                postEvent(MEDIA_ERROR, msg->getArg1(), 0);
                break;
            }

            // 播放器准备完成回调
            case MSG_PREPARED: {
                postEvent(MEDIA_PREPARED);
                break;
            }

            // 开始播放
            case MSG_STARTED: {
                LOGD("CAVAudioPlayer is started!\n");
                postEvent(MEDIA_STARTED, 0, 0);
                break;
            }

            case MSG_COMPLETED: {
                LOGD("CAVAudioPlayer is playback completed.\n");
                postEvent(MEDIA_PLAYBACK_COMPLETE, 0, 0);
                break;
            }

            // 开始音频解码
            case MSG_AUDIO_START: {
                LOGD("CAVAudioPlayer starts audio decoder.\n");
                break;
            }

            // 打开输出文件
            case MSG_OPEN_INPUT: {
                LOGD("CAVAudioPlayer is opening input file");
                break;
            }

            // 查找媒体流信息
            case MSG_FIND_STREAM_INFO: {
                LOGD("CAVAudioPlayer is finding audio stream info.\n");
                break;
            }

            // 跳转完成
            case MSG_SEEK_COMPLETE: {
                LOGD("CAVAudioPlayer seeks completed!\n");
                postEvent(MEDIA_SEEK_COMPLETE, 0, 0);
                break;
            }

            // 准备播放器
            case MSG_REQUEST_PREPARE: {
                LOGD("CAVAudioPlayer is preparing...");
                preparePlayer();
                break;
            }

            // 开始播放
            case MSG_REQUEST_START: {
                LOGD("CAVAudioPlayer is starting");
                startPlayer();
                break;
            }

            // 暂停播放
            case MSG_REQUEST_PAUSE: {
                LOGD("CAVAudioPlayer is pausing...");
                pausePlayer();
                break;
            }

            // 停止播放
            case MSG_REQUEST_STOP: {
                LOGD("CAVAudioPlayer is stopping...");
                stopPlayer();
                break;
            }

            // 播放跳转
            case MSG_REQUEST_SEEK: {
                LOGD("CAVAudioPlayer is seeking...");
                float timeMs = msg->getArg1() / 1000.0f;
                seekPlayer(timeMs);
                break;
            }

            // 当前pts回调
            case MSG_CURRENT_POSITION: {
                postEvent(MEDIA_CURRENT, msg->getArg1(), msg->getArg2());
                break;
            }

            default: {
                LOGE("CAVAudioPlayer unknown MSG_xxx(%d)\n", msg->getWhat());
                break;
            }
        }
        delete msg;
    }
}


AudioPlayerListener::AudioPlayerListener(CAVAudioPlayer *player) : player(player) {

}

AudioPlayerListener::~AudioPlayerListener() {
    LOGD("AudioPlayerListener::destructor()");
    player = nullptr;
}

void AudioPlayerListener::onPrepared(AVMediaType type) {
    if (player != nullptr && type == AVMEDIA_TYPE_AUDIO) {
        player->notify(MSG_PREPARED);
    }
}

void AudioPlayerListener::onPlaying(AVMediaType type, float pts) {
    if (player != nullptr && type == AVMEDIA_TYPE_AUDIO) {
        player->notify(MSG_CURRENT_POSITION, pts, player->getDuration());
    }
}

void AudioPlayerListener::onSeekComplete(AVMediaType type) {
    if (player != nullptr && type == AVMEDIA_TYPE_AUDIO) {
        player->notify(MSG_SEEK_COMPLETE);
    }
}

void AudioPlayerListener::onCompletion(AVMediaType type) {
    if (player != nullptr && type == AVMEDIA_TYPE_AUDIO) {
        player->notify(MSG_COMPLETED);
    }
}

void AudioPlayerListener::onError(AVMediaType type, int errorCode, const char *msg) {
    if (player != nullptr && type == AVMEDIA_TYPE_AUDIO) {
        player->notify(MSG_ERROR, errorCode);
    }
}
