//
// Created by cain on 2018/11/25.
//

#include "AVMusicPlayer.h"
#include "AndroidLog.h"
#include <pthread.h>

AVMusicPlayer::AVMusicPlayer() {
    mPlayerStatus = new PlayerStatus();
    mCallback = NULL;
    url = NULL;
    pFormatCtx = NULL;
    audioDecoder = NULL;
    mPrepared = false;
    mStarted = false;
    looping = false;
    exit = false;
    mDuration = 0;
    pthread_mutex_init(&mMutex, NULL);
    pthread_cond_init(&mCondition, NULL);
    pthread_mutex_init(&mSeekMutex, NULL);
}

AVMusicPlayer::~AVMusicPlayer() {
    release();
    avformat_network_deinit();
    free((void *) url);
    url = NULL;
    pthread_mutex_destroy(&mMutex);
    pthread_cond_destroy(&mCondition);
    pthread_mutex_destroy(&mSeekMutex);
}

/**
 * 设置数据源
 * @param url
 */
void AVMusicPlayer::setDataSource(const char *url) {
    this->url = av_strdup(url);
}

/**
 * 设置播放器回调
 * @param callback
 */
void AVMusicPlayer::setPlayerCallback(PlayerCallback *callback) {
    mCallback = callback;
    if (audioDecoder != NULL) {
        audioDecoder->setPlayerCallback(mCallback);
    }
}

/**
 * 播放器准备线程处理实体
 * @param data
 * @return
 */
static void *prepareThreadRun(void *data) {
    AVMusicPlayer *player = (AVMusicPlayer *) data;
    player->prepareDecoder();
    player->exitPrepareThread();
    return NULL;
}

/**
 * 准备播放器
 */
void AVMusicPlayer::prepare() {
    if (url == NULL) {
        return;
    }
    if (mPrepared) {
        return;
    }
    pthread_create(&mPrepareThread, NULL, prepareThreadRun, this);
}

/**
 * 解码中断回调
 * @param ctx
 * @return
 */
static int avformat_callback(void *ctx) {
    AVMusicPlayer *musicPlayer = (AVMusicPlayer *) ctx;
    if (musicPlayer->isExit()) {
        return AVERROR_EOF;
    }
    return 0;
}

/**
 * 准备解码器
 */
void AVMusicPlayer::prepareDecoder() {
    pthread_mutex_lock(&mMutex);
    // 注册ffmpeg资源
    av_register_all();
    avformat_network_init();

    // 创建解复用上下文
    pFormatCtx = avformat_alloc_context();
    pFormatCtx->interrupt_callback.callback = avformat_callback;
    pFormatCtx->interrupt_callback.opaque = this;

    // 打开文件
    if (avformat_open_input(&pFormatCtx, url, NULL, NULL) != 0) {
        if (mCallback != NULL) {
            mCallback->onError(1001, "can not open url");
        }
        exit = true;
        pthread_cond_signal(&mCondition);
        pthread_mutex_unlock(&mMutex);
        return;
    }

    // 查找媒体流信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        if (mCallback != NULL) {
            mCallback->onError(1002, "can not find streams from url");
        }
        exit = true;
        pthread_cond_signal(&mCondition);
        pthread_mutex_unlock(&mMutex);
        return;
    }

    // 查找音频流并创建音频解码器
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            if (audioDecoder == NULL) {
                audioDecoder = new AVAudioDecoder();
                audioDecoder->setPlayerStatus(mPlayerStatus);
                if (mCallback != NULL) {
                    audioDecoder->setPlayerCallback(mCallback);
                }
                audioDecoder->setSampleRate(pFormatCtx->streams[i]->codecpar->sample_rate);
                audioDecoder->setStreamIndex(i);
                audioDecoder->setCodecParameters(pFormatCtx->streams[i]->codecpar);
                audioDecoder->setTimeBase(pFormatCtx->streams[i]->time_base);
                audioDecoder->setDuration(pFormatCtx->duration / AV_TIME_BASE);
                mDuration = audioDecoder->getDuration();
                break;
            }
        }
    }

    // 判断是否存在音频解码器
    if (audioDecoder == NULL) {
        if (mCallback != NULL) {
            mCallback->onError(1003, "can not find decoder");
        }
        exit = true;
        pthread_cond_signal(&mCondition);
        pthread_mutex_unlock(&mMutex);
        return;
    }

    // 判断解码器是否存在
    AVCodec *pCodec = avcodec_find_decoder(audioDecoder->getCodecParameters()->codec_id);
    if (!pCodec) {
        if (mCallback != NULL) {
            mCallback->onError(1003, "can not find decoder");
        }
        exit = true;
        pthread_cond_signal(&mCondition);
        pthread_mutex_unlock(&mMutex);
        return;
    }

    // 利用解码器创建解码上下文
    AVCodecContext *pCodecContext = avcodec_alloc_context3(pCodec);
    if (!pCodecContext) {
        if (mCallback != NULL) {
            mCallback->onError(1004, "can not alloc new decodecctx");
        }
        exit = true;
        pthread_cond_signal(&mCondition);
        pthread_mutex_unlock(&mMutex);
        return;
    }

    // 复制解码器参数到解码上下文
    if (avcodec_parameters_to_context(pCodecContext, audioDecoder->getCodecParameters()) < 0) {
        if (mCallback != NULL) {
            mCallback->onError(1005, "ccan not fill decodecctx");
        }
        exit = true;
        pthread_cond_signal(&mCondition);
        pthread_mutex_unlock(&mMutex);
        return;
    }

    // 打开解码器
    if (avcodec_open2(pCodecContext, pCodec, 0) != 0) {
        if (mCallback != NULL) {
            mCallback->onError(1006, "cant not open audioDecoder streams");
        }
        exit = true;
        pthread_cond_signal(&mCondition);
        pthread_mutex_unlock(&mMutex);
        return;
    }
    audioDecoder->setCodecContext(pCodecContext);

    // 准备完成回调
    if (mCallback != NULL) {
        if (mPlayerStatus != NULL && !mPlayerStatus->isExit()) {
            mCallback->onPrepared();
        } else {
            exit = true;
            pthread_cond_signal(&mCondition);
        }
    }
    pthread_mutex_unlock(&mMutex);
    mPrepared = true;

}

/**
 * 退出准备解码器线程
 */
void AVMusicPlayer::exitPrepareThread() {
    pthread_exit(&mPrepareThread);
}

/**
 * 播放器播放线程处理实体
 * @param data
 * @return
 */
static void *playThreadRun(void *data) {
    AVMusicPlayer *player = (AVMusicPlayer *) data;
    player->playMusic();
    return NULL;
}

/**
 * 开始
 */
void AVMusicPlayer::start() {
    if (mStarted) {
        return;
    }
    mStarted = true;
    pthread_create(&mPlayThread, NULL, playThreadRun, this);
}

/**
 * 播放音乐
 */
void AVMusicPlayer::playMusic() {
    if (audioDecoder == NULL) {
        return;
    }
    mPlayerStatus->setPlaying(true);
    audioDecoder->start();
    // 不断进行解码
    while (mPlayerStatus != NULL && !mPlayerStatus->isExit()) {

        // 定位处理或者暂停状态
        if (mPlayerStatus->isSeek() || !mPlayerStatus->isPlaying()) {
            continue;
        }

        // 队列数据过大，则等待消耗
        if (audioDecoder->getQueue()->getPacketSize() > 40) {
            continue;
        }

        // 从文件中不断读取音频裸数据放入队列中
        AVPacket *avPacket = av_packet_alloc();
        if (av_read_frame(pFormatCtx, avPacket) == 0) {
            if (avPacket->stream_index == audioDecoder->getStreamIndex()) {
                audioDecoder->getQueue()->putPacket(avPacket);
            } else {
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
            }
        } else { // 读取不成功处理，包括读取结束、读取失败等情况
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            while (mPlayerStatus != NULL && !mPlayerStatus->isExit()) {
                // 如果队列中还存在数据，则继续等待消耗完全
                if (audioDecoder->getQueue()->getPacketSize() > 0) {
                    continue;
                } else if (looping) {   // 如果循环播放，则定位到开头继续解码
                    seek(0);
                    break;
                } else { // 退出解码
                    mPlayerStatus->setExit(true);
                    break;
                }
            }
            // 如果不需要循环播放，则退出解码循环
            if (!looping) {
                break;
            }
        }
    }
    if (mPlayerStatus != NULL) {
        mPlayerStatus->setPlaying(false);
    }
    if (mCallback != NULL) {
        mCallback->onComplete();
    }

    exit = true;
    pthread_cond_signal(&mCondition);

    // 退出播放线程
    mStarted = false;
    pthread_exit(&mPlayThread);
}

/**
 * 暂停
 */
void AVMusicPlayer::pause() {
    if (audioDecoder != NULL) {
        audioDecoder->pause();
    }
    if (mPlayerStatus != NULL) {
        mPlayerStatus->setPlaying(false);
    }
}

/**
 * 启动
 */
void AVMusicPlayer::resume() {
    if (audioDecoder != NULL) {
        audioDecoder->resume();
    }
    if (mPlayerStatus != NULL) {
        mPlayerStatus->setPlaying(true);
    }
}

/**
 * 停止
 */
void AVMusicPlayer::stop() {
    if (audioDecoder != NULL) {
        audioDecoder->stop();
    }
    if (mPlayerStatus != NULL) {
        mPlayerStatus->setExit(true);
    }
    exit = true;
    pthread_cond_signal(&mCondition);
}

/**
 * 释放资源
 */
void AVMusicPlayer::release() {
    mPrepared = false;
    mStarted = false;
    if (mPlayerStatus != NULL) {
        mPlayerStatus->setExit(true);
    }
    pthread_mutex_lock(&mMutex);
    // 等待播放线程退出
    while (!exit) {
        pthread_cond_wait(&mCondition, &mMutex);
    }
    if (audioDecoder != NULL) {
        audioDecoder->release();
        delete audioDecoder;
        audioDecoder = NULL;
    }

    if (pFormatCtx != NULL) {
        avformat_close_input(&pFormatCtx);
        avformat_free_context(pFormatCtx);
        pFormatCtx = NULL;
    }

    if (mCallback != NULL) {
        delete mCallback;
        mCallback = NULL;
    }

    if (mPlayerStatus != NULL) {
        delete mPlayerStatus;
        mPlayerStatus = NULL;
    }
    pthread_mutex_unlock(&mMutex);
}

/**
 * 设置是否循环播放囊
 * @param looping
 */
void AVMusicPlayer::setLooping(bool looping) {
    this->looping = looping;
}

/**
 * 定位
 * @param seconds
 */
void AVMusicPlayer::seek(int64_t seconds) {
    if (mDuration <= 0) {
        return;
    }
    if (seconds >= 0 && seconds <= mDuration) {
        if (audioDecoder != NULL) {
            if (mPlayerStatus != NULL) {
                mPlayerStatus->setSeek(true);
            }
            audioDecoder->getQueue()->clear();
            audioDecoder->setClock(0);
            audioDecoder->setLastTime(0);
            pthread_mutex_lock(&mSeekMutex);
            int64_t rel = seconds * AV_TIME_BASE;
            avformat_seek_file(pFormatCtx, -1, INT64_MIN, rel, INT64_MAX, 0);
            pthread_mutex_unlock(&mSeekMutex);
            if (mPlayerStatus != NULL) {
                mPlayerStatus->setSeek(false);
            }
        }
    }
    if (!mPrepared) {
        prepare();
    } else if (!mStarted) {
        start();
    }
}

/**
 * 设置音量
 * @param percent
 */
void AVMusicPlayer::setVolume(int percent) {
    if (audioDecoder != NULL) {
        audioDecoder->setVolume(percent);
    }
}

/**
 * 设置声道
 * @param channelType
 */
void AVMusicPlayer::setChannelType(int channelType) {
    if (audioDecoder != NULL) {
        audioDecoder->setChannelType(channelType);
    }
}

/**
 * 设置音调
 * @param pitch
 */
void AVMusicPlayer::setPitch(float pitch) {
    if (audioDecoder != NULL) {
        audioDecoder->setPitch(pitch);
    }
}

/**
 * 设置速度
 * @param speed
 */
void AVMusicPlayer::setSpeed(float speed) {

    if (audioDecoder != NULL) {
        audioDecoder->setSpeed(speed);
    }

}

/**
 * 设置节拍
 * @param tempo
 */
void AVMusicPlayer::setTempo(float tempo) {
    if (audioDecoder != NULL) {
        audioDecoder->setTempo(tempo);
    }
}

/**
 * 速度改变
 * @param speedChange
 */
void AVMusicPlayer::setSpeedChange(double speedChange) {
    if (audioDecoder != NULL) {
        audioDecoder->setSpeedChange(speedChange);
    }
}

/**
 * 节拍改变
 * @param tempoChange
 */
void AVMusicPlayer::setTempoChange(double tempoChange) {
    if (audioDecoder != NULL) {
        audioDecoder->setTempoChange(tempoChange);
    }
}

/**
 * 设置八度音调节
 * @param pitchOctaves
 */
void AVMusicPlayer::setPitchOctaves(double pitchOctaves) {
    if (audioDecoder != NULL) {
        audioDecoder->setPitchOctaves(pitchOctaves);
    }
}

/**
 * 设置半音调节
 * @param semiTones
 */
void AVMusicPlayer::setPitchSemiTones(double semiTones) {
    if (audioDecoder != NULL) {
        audioDecoder->setPitchSemiTones(semiTones);
    }
}

/**
 * 获取采样率
 * @return
 */
int AVMusicPlayer::getSampleRate() {
    if (audioDecoder != NULL) {
        return audioDecoder->getCodecContext()->sample_rate;
    }
    return 0;
}

/**
 * 是否退出
 * @return
 */
bool AVMusicPlayer::isExit() {
    if (mPlayerStatus != NULL) {
        return mPlayerStatus->isExit();
    }
    return true;
}

/**
 * 获取时长
 * @return
 */
int AVMusicPlayer::getDuration() {
    return mDuration;
}

/**
 * 是否正在播放状态
 * @return
 */
bool AVMusicPlayer::isPlaying() {
    if (mPlayerStatus == NULL) {
        return false;
    }
    return mPlayerStatus->isPlaying();
}