//
// Created by CainHuang on 2020-01-30.
//

#include "MusicPlayer.h"

MusicPlayer::MusicPlayer() {
    mStreamPlayListener = std::make_shared<AudioPlayerListener>(this);
    mAudioPlayer = std::make_shared<AudioStreamPlayer>(mStreamPlayListener);
    mMessageQueue = std::unique_ptr<MessageQueue>(new MessageQueue());
    mThread = nullptr;
    mPlayListener = nullptr;
    mAbortRequest = true;
}

MusicPlayer::~MusicPlayer() {
    release();
    LOGD("MusicPlayer::destructor()");
}

void MusicPlayer::init() {
    mAbortRequest = false;
    mCondition.signal();
    if (mThread == nullptr) {
        mThread = new Thread(this);
    }
    if (!mThread->isActive()) {
        mThread->start();
    }
}

void MusicPlayer::release() {
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

void MusicPlayer::setOnPlayingListener(std::shared_ptr<OnPlayListener> listener) {
    if (mPlayListener != nullptr) {
        mPlayListener.reset();
        mPlayListener = nullptr;
    }
    mPlayListener = listener;
}

status_t MusicPlayer::setDataSource(const char *path) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setDataSource(path);
    }
    return OK;
}

status_t MusicPlayer::setSpeed(float speed) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setSpeed(speed);
    }
    return OK;
}

status_t MusicPlayer::setLooping(bool looping) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setLooping(looping);
    }
    return OK;
}

status_t MusicPlayer::setRange(float start, float end) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setRange(start, end);
    }
    return OK;
}

status_t MusicPlayer::setVolume(float leftVolume, float rightVolume) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->setVolume(leftVolume, rightVolume);
    }
    return OK;
}

status_t MusicPlayer::prepare() {
    LOGD("MusicPlayer::prepare()");
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_PREPARE));
    mCondition.signal();
    return OK;
}

status_t MusicPlayer::start() {
    LOGD("MusicPlayer::start()");
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_START));
    mCondition.signal();
    return OK;
}

status_t MusicPlayer::pause() {
    LOGD("MusicPlayer::pause()");
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_PAUSE));
    mCondition.signal();
    return OK;
}

status_t MusicPlayer::stop() {
    LOGD("MusicPlayer::stop()");
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_STOP));
    mCondition.signal();
    return OK;
}

status_t MusicPlayer::seekTo(float timeMs) {
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_SEEK, (int)(timeMs * 1000), -1));
    mCondition.signal();
    return OK;
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

void MusicPlayer::preparePlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->prepare();
    }
}

void MusicPlayer::startPlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->start();
    }
}

void MusicPlayer::pausePlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->pause();
    }
}

void MusicPlayer::stopPlayer() {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->stop();
    }
}

void MusicPlayer::seekPlayer(float timeMs) {
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->seekTo(timeMs);
    }
}

void MusicPlayer::notify(int msg, int arg1, int arg2) {
    mMessageQueue->pushMessage(new Message(msg, arg1, arg2));
    mCondition.signal();
}

void MusicPlayer::postEvent(int what, int arg1, int arg2, void *obj) {
    if (mPlayListener != nullptr) {
        mPlayListener->notify(what, arg1, arg2, obj);
    }
}

void MusicPlayer::run() {

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
                LOGD("MusicPlayer is flushing.\n");
                break;
            }

            // 播放出错
            case MSG_ERROR: {
                LOGD("MusicPlayer occurs error: %d\n", msg->getArg1());
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
                LOGD("MusicPlayer is started!\n");
                postEvent(MEDIA_STARTED, 0, 0);
                break;
            }

            case MSG_COMPLETED: {
                LOGD("MusicPlayer is playback completed.\n");
                postEvent(MEDIA_PLAYBACK_COMPLETE, 0, 0);
                break;
            }

            // 开始音频解码
            case MSG_AUDIO_START: {
                LOGD("MusicPlayer starts audio decoder.\n");
                break;
            }

            // 打开输出文件
            case MSG_OPEN_INPUT: {
                LOGD("MusicPlayer is opening input file");
                break;
            }

            // 查找媒体流信息
            case MSG_FIND_STREAM_INFO: {
                LOGD("MusicPlayer is finding audio stream info.\n");
                break;
            }

            // 跳转完成
            case MSG_SEEK_COMPLETE: {
                LOGD("MusicPlayer seeks completed!\n");
                postEvent(MEDIA_SEEK_COMPLETE, 0, 0);
                break;
            }

            // 准备播放器
            case MSG_REQUEST_PREPARE: {
                LOGD("MusicPlayer is preparing...");
                preparePlayer();
                break;
            }

            // 开始播放
            case MSG_REQUEST_START: {
                LOGD("MusicPlayer is starting");
                startPlayer();
                break;
            }

            // 暂停播放
            case MSG_REQUEST_PAUSE: {
                LOGD("MusicPlayer is pausing...");
                pausePlayer();
                break;
            }

            // 停止播放
            case MSG_REQUEST_STOP: {
                LOGD("MusicPlayer is stopping...");
                stopPlayer();
                break;
            }

            // 播放跳转
            case MSG_REQUEST_SEEK: {
                LOGD("MusicPlayer is seeking...");
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
                LOGE("MusicPlayer unknown MSG_xxx(%d)\n", msg->getWhat());
                break;
            }
        }
        delete msg;
    }
}


AudioPlayerListener::AudioPlayerListener(MusicPlayer *player) : player(player) {

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
