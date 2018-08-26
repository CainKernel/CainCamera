//
// Created by cain on 2018/5/1.
//

#include "MediaPlayerHandler.h"

MediaPlayerHandler::MediaPlayerHandler(AVMediaPlayer *mediaPlayer, MessageQueue *queue)
        : Handler(queue) {
    this->mediaPlayer = mediaPlayer;
    mCallback = NULL;
}

MediaPlayerHandler::~MediaPlayerHandler() {
    mediaPlayer = NULL;
    mCallback = NULL;
}

void MediaPlayerHandler::handleMessage(Message *msg) {
    if (mediaPlayer == NULL) {
        return;
    }
    int what = msg->getWhat();
    switch (what) {

        // 设置数据源
        case kMsgPlayerSetDataSource: {
            char *dataSource = (char *)msg->getObj();
            mediaPlayer->setDataSource(dataSource);
            break;
        }

        // 设置Surface
        case kMsgPlayerSetSurface: {
            ANativeWindow *window = (ANativeWindow *) msg->getObj();
            mediaPlayer->setSurface(window);
            break;
        }

        // 设置音频轨道信息
        case kMsgPlayerSetAudioChannel: {
            int index = msg->getArg1();
            mediaPlayer->setAudioStream(index);
            break;
        }

        // 准备解码器
        case kMsgPlayerPrepare: {
            mediaPlayer->prepare();
            break;
        }

        // 开始播放
        case kMsgPlayerStart: {
            mediaPlayer->start();
            break;
        }

        // 开始定位
        case kMsgPlayerSeek: {
            int64_t sec = msg->getArg1();
            mediaPlayer->seek(sec);
            break;
        }

        // 停止
        case kMsgPlayerStop: {
            mediaPlayer->stop();
            mediaPlayer->release();
            mediaPlayer = NULL;
            // 调用播放器释放完成回调，用于释放外面传进来的播放器对象
            if (mCallback) {
                mCallback();
            }
            break;
        }

        // 暂停
        case kMsgPlayerPause: {
            mediaPlayer->pause();
            break;
        }

        // 启动
        case kMsgPlayerResume: {
            mediaPlayer->resume();
            break;
        }

        default:
            break;
    }
}

void MediaPlayerHandler::setMediaPlayer(AVMediaPlayer *player) {
    this->mediaPlayer = player;
}

void MediaPlayerHandler::setPlayerReleaseCallback(playerReleaseCallback *callback) {
    mCallback = callback;
}

void MediaPlayerHandler::setSurface(ANativeWindow *nativeWindow) {
    postMessage(new Message(kMsgPlayerSetSurface, nativeWindow));
}

void MediaPlayerHandler::setAudioStream(int index) {
    postMessage(new Message(kMsgPlayerSetAudioChannel, index, -1));
}

void MediaPlayerHandler::prepare() {
    postMessage(new Message(kMsgPlayerPrepare));
}

void MediaPlayerHandler::start() {
    postMessage(new Message(kMsgPlayerStart));
}

void MediaPlayerHandler::stop() {
    postMessage(new Message(kMsgPlayerStop));
}

void MediaPlayerHandler::seek(int64_t sec) {
    postMessage(new Message(kMsgPlayerSeek, sec, -1));
}

void MediaPlayerHandler::pause() {
    postMessage(new Message(kMsgPlayerPause));
}

void MediaPlayerHandler::resume() {
    postMessage(new Message(kMsgPlayerResume));
}
