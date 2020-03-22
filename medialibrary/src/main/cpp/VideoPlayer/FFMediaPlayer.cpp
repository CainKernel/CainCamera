//
// Created by CainHuang on 2020-02-24.
//

#include "FFMediaPlayer.h"

FFMediaPlayer::FFMediaPlayer() {
    LOGD("FFMediaPlayer::constructor()");
    mThread = nullptr;
    mMessageQueue = std::unique_ptr<MessageQueue>(new MessageQueue());
    mTimestamp = std::make_shared<Timestamp>();
    mStreamPlayListener = std::make_shared<MediaStreamPlayerListener>(this);
    mVideoPlayer = std::make_shared<VideoStreamPlayer>(mStreamPlayListener);
    mVideoPlayer->setTimestamp(mTimestamp);

    mAudioPlayer = std::make_shared<AudioStreamPlayer>(mStreamPlayListener);
    mAudioPlayer->setTimestamp(mTimestamp);
}

FFMediaPlayer::~FFMediaPlayer() {
    release();
    LOGD("FFMediaPlayer::destructor()");
}

void FFMediaPlayer::init() {
    if (mThread == nullptr) {
        mThread = new Thread(this);
    }
    if (!mThread->isActive()) {
        mThread->start();
    }
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

void FFMediaPlayer::prepare() {
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_PREPARE));
    mCondition.signal();
}

void FFMediaPlayer::start() {
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_START));
    mCondition.signal();
}

void FFMediaPlayer::pause() {
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_PAUSE));
    mCondition.signal();
}

void FFMediaPlayer::stop() {
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_STOP));
    mCondition.signal();
}

void FFMediaPlayer::setDecodeOnPause(bool decodeOnPause) {
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->setDecodeOnPause(decodeOnPause);
    }
}

void FFMediaPlayer::seekTo(float timeMs) {
    mMessageQueue->pushMessage(new Message(MSG_REQUEST_SEEK, (int)(timeMs * 1000), -1));
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
    if (mTimestamp != nullptr) {
        mTimestamp.reset();
        mTimestamp = nullptr;
    }
}

void FFMediaPlayer::onPlaying(float pts) {
    mMessageQueue->pushMessage(new Message(MSG_CURRENT_POSITION, (int)(pts * 1000), -1));
    mCondition.signal();
}

void FFMediaPlayer::onSeekComplete(AVMediaType type) {
    if (type == AVMEDIA_TYPE_VIDEO) {
        mMessageQueue->pushMessage(new Message(MSG_SEEK_COMPLETE));
        mCondition.signal();
    }
}

void FFMediaPlayer::onCompletion(AVMediaType type) {
    mMessageQueue->pushMessage(new Message(MSG_COMPLETED));
    mCondition.signal();
}

void FFMediaPlayer::onError(int errorCode, const char *msg) {

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

        auto msg = mMessageQueue->popMessage();
        int what = msg->getWhat();
        switch(what) {

            case MSG_FLUSH: {
                LOGD("CainMediaPlayer is flushing.\n");
                break;
            }

            case MSG_ERROR: {
                LOGD("CainMediaPlayer occurs error: %d\n", msg->getArg1());
//                postEvent(MEDIA_ERROR, msg->getArg1(), 0);
                break;
            }

            case MSG_PREPARED: {
                LOGD("CainMediaPlayer is prepared.\n");
                if (mPlayListener != nullptr) {
                    mPlayListener->onPrepared();
                }
                break;
            }

            case MSG_STARTED: {
                LOGD("CainMediaPlayer is started!");
                break;
            }

            case MSG_COMPLETED: {
                LOGD("CainMediaPlayer is playback completed.\n");
                break;
            }

            case MSG_VIDEO_SIZE_CHANGED: {
                LOGD("CainMediaPlayer is video size changing: %d, %d\n", msg->getArg1(), msg->getArg2());
//                postEvent(MEDIA_SET_VIDEO_SIZE, msg->getArg1(), msg->getArg2());
                break;
            }

            case MSG_SAR_CHANGED: {
                LOGD("CainMediaPlayer is sar changing: %d, %d\n", msg->getArg1(), msg->getArg2());
//                postEvent(MEDIA_SET_VIDEO_SAR, msg->getArg1(), msg->getArg2());
                break;
            }

            case MSG_VIDEO_RENDERING_START: {
                LOGD("CainMediaPlayer is video playing.\n");
                break;
            }

            case MSG_AUDIO_RENDERING_START: {
                LOGD("CainMediaPlayer is audio playing.\n");
                break;
            }

            case MSG_VIDEO_ROTATION_CHANGED: {
                LOGD("CainMediaPlayer's video rotation is changing: %d\n", msg->getArg1());
                break;
            }

            case MSG_AUDIO_START: {
                LOGD("CainMediaPlayer starts audio decoder.\n");
                break;
            }

            case MSG_VIDEO_START: {
                LOGD("CainMediaPlayer starts video decoder.\n");
                break;
            }

            case MSG_OPEN_INPUT: {
                LOGD("CainMediaPlayer is opening input file.\n");
                break;
            }

            case MSG_FIND_STREAM_INFO: {
                LOGD("CanMediaPlayer is finding media stream info.\n");
                break;
            }

            case MSG_PREPARE_DECODER: {
                LOGD("CainMediaPlayer is preparing decoder.\n");
                break;
            }

            case MSG_BUFFERING_START: {
                LOGD("CanMediaPlayer is buffering start.\n");
//                postEvent(MEDIA_INFO, MEDIA_INFO_BUFFERING_START, msg->getArg1());
                break;
            }

            case MSG_BUFFERING_END: {
                LOGD("CainMediaPlayer is buffering finish.\n");
//                postEvent(MEDIA_INFO, MEDIA_INFO_BUFFERING_END, msg->getArg1());
                break;
            }

            case MSG_BUFFERING_UPDATE: {
                LOGD("CainMediaPlayer is buffering: %d, %d", msg->getArg1(), msg->getArg2());
//                postEvent(MEDIA_BUFFERING_UPDATE, msg->getArg1(), msg->getArg2());
                break;
            }

            case MSG_SEEK_COMPLETE: {
                LOGD("CainMediaPlayer seeks completed!\n");
//                postEvent(MEDIA_SEEK_COMPLETE, 0, 0);
                break;
            }

            // 准备操作
            case MSG_REQUEST_PREPARE: {
                preparePlayer();
                break;
            }

            // 开始
            case MSG_REQUEST_START: {
                startPlayer();
                break;
            }

            // 暂停
            case MSG_REQUEST_PAUSE: {
                pausePlayer();
                break;
            }

            // 停止
            case MSG_REQUEST_STOP: {
                stopPlayer();
                abortRequest = true;
                break;
            }

            // 定位
            case MSG_REQUEST_SEEK: {
                float timeMs = msg->getArg1() / 1000.0f;
                seekPlayer(timeMs);
                break;
            }

            // 播放进度回调
            case MSG_CURRENT_POSITION: {
                float timeMs = msg->getArg1() / 1000.0f;
                if (mPlayListener != nullptr) {
                    mPlayListener->onPlaying(timeMs);
                }
                break;
            }

            default: {
                break;
            }
        }
        delete msg;
    }
}

/**
 * 准备回调
 */
void FFMediaPlayer::preparePlayer() {
    if (mVideoPlayer != nullptr) {
        mVideoPlayer->start();
    }
    if (mAudioPlayer != nullptr) {
        mAudioPlayer->start();
    }
    mMessageQueue->pushMessage(new Message(MSG_PREPARED));
    mCondition.signal();
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
    if (type == AVMEDIA_TYPE_AUDIO) {
        if (player != nullptr) {
            player->onPlaying(pts);
        }
    }
}

void MediaStreamPlayerListener::onSeekComplete(AVMediaType type) {
    if (player != nullptr ) {
        player->onSeekComplete(type);
    }
}

void MediaStreamPlayerListener::onCompletion(AVMediaType type) {
    if (player != nullptr ) {
        player->onCompletion(type);
    }
}

void MediaStreamPlayerListener::onError(AVMediaType type, int errorCode, const char *msg) {
    if (player != nullptr ) {
        player->onError(errorCode, msg);
    }
}
