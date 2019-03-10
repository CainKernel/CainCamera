//
// Created by cain on 2019/2/1.
//

#include <player/AVMessageQueue.h>
#include "CainMediaPlayer.h"

CainMediaPlayer::CainMediaPlayer() {
    msgThread = nullptr;
    abortRequest = true;
    videoDevice = nullptr;
    mediaPlayer = nullptr;
    mListener = nullptr;
    mPrepareSync = false;
    mPrepareStatus = NO_ERROR;
    mAudioSessionId = 0;
    mSeeking = false;
    mSeekingPosition = 0;
}

CainMediaPlayer::~CainMediaPlayer() {

}

void CainMediaPlayer::init() {

    mMutex.lock();
    abortRequest = false;
    mCondition.signal();
    mMutex.unlock();

    mMutex.lock();
    if (videoDevice == nullptr) {
        videoDevice = new GLESDevice();
    }
    if (msgThread == nullptr) {
        msgThread = new Thread(this);
        msgThread->start();
    }
    mMutex.unlock();
}

void CainMediaPlayer::disconnect() {

    mMutex.lock();
    abortRequest = true;
    mCondition.signal();
    mMutex.unlock();

    reset();

    if (msgThread != nullptr) {
        msgThread->join();
        delete msgThread;
        msgThread = nullptr;
    }

    if (videoDevice != nullptr) {
        delete videoDevice;
        videoDevice = nullptr;
    }
    if (mListener != nullptr) {
        delete mListener;
        mListener = nullptr;
    }

}

status_t CainMediaPlayer::setDataSource(const char *url, int64_t offset, const char *headers) {
    if (url == nullptr) {
        return BAD_VALUE;
    }
    if (mediaPlayer == nullptr) {
        mediaPlayer = new MediaPlayer();
    }
    mediaPlayer->setDataSource(url, offset, headers);
    mediaPlayer->setVideoDevice(videoDevice);
    return NO_ERROR;
}

status_t CainMediaPlayer::setMetadataFilter(char **allow, char **block) {
    // do nothing
    return NO_ERROR;
}

status_t
CainMediaPlayer::getMetadata(bool update_only, bool apply_filter, AVDictionary **metadata) {
    if (mediaPlayer != nullptr) {
        return mediaPlayer->getMetadata(metadata);
    }
    return NO_ERROR;
}

status_t CainMediaPlayer::setVideoSurface(ANativeWindow *native_window) {
    if (mediaPlayer == nullptr) {
        return NO_INIT;
    }
    if (native_window != nullptr) {
        videoDevice->surfaceCreated(native_window);
        return NO_ERROR;
    }
    return NO_ERROR;
}

status_t CainMediaPlayer::setListener(MediaPlayerListener *listener) {
    if (mListener != nullptr) {
        delete mListener;
        mListener = nullptr;
    }
    mListener = listener;
    return NO_ERROR;
}

status_t CainMediaPlayer::prepare() {
    if (mediaPlayer == nullptr) {
        return NO_INIT;
    }
    if (mPrepareSync) {
        return -EALREADY;
    }
    mPrepareSync = true;
    status_t ret = mediaPlayer->prepare();
    if (ret != NO_ERROR) {
        return ret;
    }
    if (mPrepareSync) {
        mPrepareSync = false;
    }
    return mPrepareStatus;
}

status_t CainMediaPlayer::prepareAsync() {
    if (mediaPlayer != nullptr) {
        return mediaPlayer->prepareAsync();
    }
    return INVALID_OPERATION;
}

void CainMediaPlayer::start() {
    if (mediaPlayer != nullptr) {
        mediaPlayer->start();
    }
}

void CainMediaPlayer::stop() {
    if (mediaPlayer) {
        mediaPlayer->stop();
    }
}

void CainMediaPlayer::pause() {
    if (mediaPlayer) {
        mediaPlayer->pause();
    }
}

void CainMediaPlayer::resume() {
    if (mediaPlayer) {
        mediaPlayer->resume();
    }
}

bool CainMediaPlayer::isPlaying() {
    if (mediaPlayer) {
        return (mediaPlayer->isPlaying() != 0);
    }
    return false;
}

int CainMediaPlayer::getRotate() {
    if (mediaPlayer != nullptr) {
        return mediaPlayer->getRotate();
    }
    return 0;
}

int CainMediaPlayer::getVideoWidth() {
    if (mediaPlayer != nullptr) {
        return mediaPlayer->getVideoWidth();
    }
    return 0;
}

int CainMediaPlayer::getVideoHeight() {
    if (mediaPlayer != nullptr) {
        return mediaPlayer->getVideoHeight();
    }
    return 0;
}

status_t CainMediaPlayer::seekTo(float msec) {
    if (mediaPlayer != nullptr) {
        // if in seeking state, put seek message in queue, to process after preview seeking.
        if (mSeeking) {
            mediaPlayer->getMessageQueue()->postMessage(MSG_REQUEST_SEEK, msec);
        } else {
            mediaPlayer->seekTo(msec);
            mSeekingPosition = (long)msec;
            mSeeking = true;
        }
    }
    return NO_ERROR;
}

long CainMediaPlayer::getCurrentPosition() {
    if (mediaPlayer != nullptr) {
        if (mSeeking) {
            return mSeekingPosition;
        }
        return mediaPlayer->getCurrentPosition();
    }
    return 0;
}

long CainMediaPlayer::getDuration() {
    if (mediaPlayer != nullptr) {
        return mediaPlayer->getDuration();
    }
    return -1;
}

status_t CainMediaPlayer::reset() {
    mPrepareSync = false;
    if (mediaPlayer != nullptr) {
        mediaPlayer->reset();
        delete mediaPlayer;
        mediaPlayer = nullptr;
    }
    return NO_ERROR;
}

status_t CainMediaPlayer::setAudioStreamType(int type) {
    // TODO setAudioStreamType
    return NO_ERROR;
}

status_t CainMediaPlayer::setLooping(bool looping) {
    if (mediaPlayer != nullptr) {
        mediaPlayer->setLooping(looping);
    }
    return NO_ERROR;
}

bool CainMediaPlayer::isLooping() {
    if (mediaPlayer != nullptr) {
        return (mediaPlayer->isLooping() != 0);
    }
    return false;
}

status_t CainMediaPlayer::setVolume(float leftVolume, float rightVolume) {
    if (mediaPlayer != nullptr) {
        mediaPlayer->setVolume(leftVolume, rightVolume);
    }
    return NO_ERROR;
}

void CainMediaPlayer::setMute(bool mute) {
    if (mediaPlayer != nullptr) {
        mediaPlayer->setMute(mute);
    }
}

void CainMediaPlayer::setRate(float speed) {
    if (mediaPlayer != nullptr) {
        mediaPlayer->setRate(speed);
    }
}

void CainMediaPlayer::setPitch(float pitch) {
    if (mediaPlayer != nullptr) {
        mediaPlayer->setPitch(pitch);
    }
}

status_t CainMediaPlayer::setAudioSessionId(int sessionId) {
    if (sessionId < 0) {
        return BAD_VALUE;
    }
    mAudioSessionId = sessionId;
    return NO_ERROR;
}

int CainMediaPlayer::getAudioSessionId() {
    return mAudioSessionId;
}

void CainMediaPlayer::setOption(int category, const char *type, const char *option) {
    if (mediaPlayer != nullptr) {
        mediaPlayer->getPlayerState()->setOption(category, type, option);
    }
}

void CainMediaPlayer::setOption(int category, const char *type, int64_t option) {
    if (mediaPlayer != nullptr) {
        mediaPlayer->getPlayerState()->setOptionLong(category, type, option);
    }
}

void CainMediaPlayer::notify(int msg, int ext1, int ext2, void *obj, int len) {
    if (mediaPlayer != nullptr) {
        mediaPlayer->getMessageQueue()->postMessage(msg, ext1, ext2, obj, len);
    }
}

void CainMediaPlayer::postEvent(int what, int arg1, int arg2, void *obj) {
    if (mListener != nullptr) {
        mListener->notify(what, arg1, arg2, obj);
    }
}

void CainMediaPlayer::run() {

    int retval;
    while (true) {

        if (abortRequest) {
            break;
        }

        // 如果此时播放器还没准备好，则睡眠10毫秒，等待播放器初始化
        if (!mediaPlayer || !mediaPlayer->getMessageQueue()) {
            av_usleep(10 * 1000);
            continue;
        }

        AVMessage msg;
        retval = mediaPlayer->getMessageQueue()->getMessage(&msg);
        if (retval < 0) {
            ALOGE("getMessage error");
            break;
        }

        assert(retval > 0);

        switch (msg.what) {
            case MSG_FLUSH: {
                ALOGD("CainMediaPlayer is flushing.\n");
                postEvent(MEDIA_NOP, 0, 0);
                break;
            }

            case MSG_ERROR: {
                ALOGD("CainMediaPlayer occurs error: %d\n", msg.arg1);
                if (mPrepareSync) {
                    mPrepareSync = false;
                    mPrepareStatus = msg.arg1;
                }
                postEvent(MEDIA_ERROR, msg.arg1, 0);
                break;
            }

            case MSG_PREPARED: {
                ALOGD("CainMediaPlayer is prepared.\n");
                if (mPrepareSync) {
                    mPrepareSync = false;
                    mPrepareStatus = NO_ERROR;
                }
                postEvent(MEDIA_PREPARED, 0, 0);
                break;
            }

            case MSG_STARTED: {
                ALOGD("CainMediaPlayer is started!");
                postEvent(MEDIA_STARTED, 0, 0);
                break;
            }

            case MSG_COMPLETED: {
                ALOGD("CainMediaPlayer is playback completed.\n");
                postEvent(MEDIA_PLAYBACK_COMPLETE, 0, 0);
                break;
            }

            case MSG_VIDEO_SIZE_CHANGED: {
                ALOGD("CainMediaPlayer is video size changing: %d, %d\n", msg.arg1, msg.arg2);
                postEvent(MEDIA_SET_VIDEO_SIZE, msg.arg1, msg.arg2);
                break;
            }

            case MSG_SAR_CHANGED: {
                ALOGD("CainMediaPlayer is sar changing: %d, %d\n", msg.arg1, msg.arg2);
                postEvent(MEDIA_SET_VIDEO_SAR, msg.arg1, msg.arg2);
                break;
            }

            case MSG_VIDEO_RENDERING_START: {
                ALOGD("CainMediaPlayer is video playing.\n");
                break;
            }

            case MSG_AUDIO_RENDERING_START: {
                ALOGD("CainMediaPlayer is audio playing.\n");
                break;
            }

            case MSG_VIDEO_ROTATION_CHANGED: {
                ALOGD("CainMediaPlayer's video rotation is changing: %d\n", msg.arg1);
                break;
            }

            case MSG_AUDIO_START: {
                ALOGD("CainMediaPlayer starts audio decoder.\n");
                break;
            }

            case MSG_VIDEO_START: {
                ALOGD("CainMediaPlayer starts video decoder.\n");
                break;
            }

            case MSG_OPEN_INPUT: {
                ALOGD("CainMediaPlayer is opening input file.\n");
                break;
            }

            case MSG_FIND_STREAM_INFO: {
                ALOGD("CanMediaPlayer is finding media stream info.\n");
                break;
            }

            case MSG_BUFFERING_START: {
                ALOGD("CanMediaPlayer is buffering start.\n");
                postEvent(MEDIA_INFO, MEDIA_INFO_BUFFERING_START, msg.arg1);
                break;
            }

            case MSG_BUFFERING_END: {
                ALOGD("CainMediaPlayer is buffering finish.\n");
                postEvent(MEDIA_INFO, MEDIA_INFO_BUFFERING_END, msg.arg1);
                break;
            }

            case MSG_BUFFERING_UPDATE: {
                ALOGD("CainMediaPlayer is buffering: %d, %d", msg.arg1, msg.arg2);
                postEvent(MEDIA_BUFFERING_UPDATE, msg.arg1, msg.arg2);
                break;
            }

            case MSG_BUFFERING_TIME_UPDATE: {
                ALOGD("CainMediaPlayer time text update");
                break;
            }

            case MSG_SEEK_COMPLETE: {
                ALOGD("CainMediaPlayer seeks completed!\n");
                mSeeking = false;
                postEvent(MEDIA_SEEK_COMPLETE, 0, 0);
                break;
            }

            case MSG_PLAYBACK_STATE_CHANGED: {
                ALOGD("CainMediaPlayer's playback state is changed.");
                break;
            }

            case MSG_TIMED_TEXT: {
                ALOGD("CainMediaPlayer is updating time text");
                postEvent(MEDIA_TIMED_TEXT, 0, 0, msg.obj);
                break;
            }

            case MSG_REQUEST_PREPARE: {
                ALOGD("CainMediaPlayer is preparing...");
                status_t ret = prepare();
                if (ret != NO_ERROR) {
                    ALOGE("CainMediaPlayer prepare error - '%d'", ret);
                }
                break;
            }

            case MSG_REQUEST_START: {
                ALOGD("CainMediaPlayer is waiting to start.");
                break;
            }

            case MSG_REQUEST_PAUSE: {
                ALOGD("CainMediaPlayer is pausing...");
                pause();
                break;
            }

            case MSG_REQUEST_SEEK: {
                ALOGD("CainMediaPlayer is seeking...");
                mSeeking = true;
                mSeekingPosition = (long)msg.arg1;
                if (mediaPlayer != nullptr) {
                    mediaPlayer->seekTo(mSeekingPosition);
                }
                break;
            }

            case MSG_CURRENT_POSITON: {
                postEvent(MEDIA_CURRENT, msg.arg1, msg.arg2);
                break;
            }

            default: {
                ALOGE("CainMediaPlayer unknown MSG_xxx(%d)\n", msg.what);
                break;
            }
        }
        message_free_resouce(&msg);
    }
}
