//
// Created by admin on 2018/4/29.
//

#include "AVMediaPlayer.h"

AVMediaPlayer::AVMediaPlayer(MediaJniCall *jniCall, const char *url) {
    pthread_mutex_init(&mMutex, NULL);
    pthread_mutex_init(&mSeekMutex, NULL);
    mediaJniCall = jniCall;
    fileName = strdup(url);
    duration = AV_NOPTS_VALUE;
    pFormatCtx = NULL;
    audioDecoder = NULL;
    videoDecoder = NULL;
    synchronizer = NULL;
    mediaStatus = new MediaStatus();
    exit = false;
    exitByUser = false;
    mThread = NULL;
    nativeWindow = NULL;
}

AVMediaPlayer::~AVMediaPlayer() {
    pthread_mutex_destroy(&mMutex);
    nativeWindow = NULL;
    if (fileName) {
        free(fileName);
        fileName = NULL;
    }
}

/**
 * 设置数据源
 * @param dataSource
 */
void AVMediaPlayer::setDataSource(const char *dataSource) {
    fileName = strdup(dataSource);
}

/**
 * 设置Surface
 * @param window
 */
void AVMediaPlayer::setSurface(ANativeWindow *window) {
    this->nativeWindow = window;
    if (synchronizer != NULL) {
        synchronizer->setSurface(nativeWindow);
    }
}

/**
 * 准备解码器
 * @return
 */
int AVMediaPlayer::prepare() {
    // 判断文件是否存在
    if (!fileName) {
        if (mediaJniCall) {
            mediaJniCall->onError(WORKER_THREAD, OPEN_URL_FAILED, "file not found!");
        }
        return -1;
    }

    pthread_mutex_lock(&mMutex);
    exit = false;
    // 注册所有解码器
    av_register_all();
    avformat_network_init();

    // 注册多线程锁管理器
    if (av_lockmgr_register(lockmgr)) {
        if (mediaJniCall) {
            mediaJniCall->onError(WORKER_THREAD, REGISTER_LOCK_MANAGER,
                                  "Could not initialize lock manager!");
        }
        return -1;
    }


    // 创建解复用上下文(解封装上下文)
    pFormatCtx = avformat_alloc_context();
    // 判断解复用上下文是否创建成功
    if (pFormatCtx == NULL) {
        exit = true;
        pthread_mutex_unlock(&mMutex);
        return -1;
    }

    // 打开文件
    if (avformat_open_input(&pFormatCtx, fileName, NULL, NULL) != 0) {
        if (mediaJniCall != NULL) {
            mediaJniCall->onError(WORKER_THREAD, OPEN_URL_FAILED, "can not open url");
        }
        exit = true;
        pthread_mutex_unlock(&mMutex);
        return -1;
    }

    // 设置解复用中断回调
    pFormatCtx->interrupt_callback.callback = avformat_interrupt_cb;
    pFormatCtx->interrupt_callback.opaque = this;

    // 查找媒体流信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        if(mediaJniCall != NULL) {
            mediaJniCall->onError(WORKER_THREAD, FIND_STREAMS_FAILED,
                                  "can not find streams from url");
        }
        exit = true;
        pthread_mutex_unlock(&mMutex);
        return -1;
    }

    // 计算时长
    duration = pFormatCtx->duration / 1000000;

    // 查找媒体流索引
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO ) {
            MediaStream *stream = new MediaStream(i, pFormatCtx->streams[i]->time_base);
            audioStreams.push_front(stream);
        } else if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            int num = pFormatCtx->streams[i]->avg_frame_rate.num;
            int den = pFormatCtx->streams[i]->avg_frame_rate.den;
            if (num != 0 && den != 0) {
                int fps = pFormatCtx->streams[i]->avg_frame_rate.num / pFormatCtx->streams[i]->avg_frame_rate.den;
                MediaStream *wl = new MediaStream(i, pFormatCtx->streams[i]->time_base, fps);
                videoStreams.push_front(wl);
            }
        }
    }

    // 判断音频流是否存在，如果存在，则创建音频解码所需的解码上下文
    if (audioStreams.size() > 0) {
        audioDecoder = new AVAudioDecoder(mediaStatus, mediaJniCall);
        // 默认使用第一个音频流
        setAudioStream(0);
        // 判断音频解码器的媒体流索引是否存在
        if (audioDecoder->getStreamIndex() >= 0
            && audioDecoder->getStreamIndex() < pFormatCtx->nb_streams) {
            if (createCodecContext(pFormatCtx->streams[audioDecoder->getStreamIndex()]->codecpar,
                                   audioDecoder) != 0) {
                exit = true;
                pthread_mutex_unlock(&mMutex);
                return -1;
            }
        }

    }

    // 判断视频流是否存在，如果存在，则创建视频解码所需的解码上下文
    if (videoStreams.size() > 0) {
        videoDecoder = new AVVideoDecoder(mediaStatus, mediaJniCall);
        setVideoStream(0);
        if (videoDecoder->getStreamIndex() >= 0
            && videoDecoder->getStreamIndex() < pFormatCtx->nb_streams) {
            if (createCodecContext(pFormatCtx->streams[videoDecoder->getStreamIndex()]->codecpar,
                                   videoDecoder) != 0) {
                exit = true;
                pthread_mutex_unlock(&mMutex);
                return -1;
            }
        }
    }

    // 判断音频解码器和视频解码器是否存在
    if (audioDecoder == NULL && videoDecoder == NULL) {
        exit = true;
        pthread_mutex_unlock(&mMutex);
        return -1;
    }

    // 音频解码器是否存在
    if (audioDecoder != NULL) {
        audioDecoder->setDuration((int)duration);
        audioDecoder->setSampleRate(audioDecoder->getCodecContext()->sample_rate);
        // TODO 状态似乎不对，导致内存泄漏
//        if (videoDecoder != NULL) {
//            audioDecoder->setVideo(true);
//        } else {
//            audioDecoder->setVideo(false);
//        }
    }

    // 判断视频解码器是否存在，设置解码器的时长
    if (videoDecoder != NULL) {
        // 设置视频时长
        videoDecoder->setDuration((int)duration);
    }

    // 创建同步器
    synchronizer = new AVSynchronizer(audioDecoder, videoDecoder, mediaStatus, mediaJniCall);
    if (nativeWindow != NULL) {
        synchronizer->setSurface(nativeWindow);
    }

    // 通知Java层准备完成
    if (mediaJniCall) {
        mediaJniCall->onPrepared(WORKER_THREAD);
    }
    exit = true;
    pthread_mutex_unlock(&mMutex);
    return 0;
}

/**
 * 解码中断回调
 * @param ctx
 * @return
 */
int AVMediaPlayer::avformat_interrupt_cb(void *ctx) {
    AVMediaPlayer *player = (AVMediaPlayer *) ctx;
    if (player->isExit()) {
        return AVERROR_EOF;
    }
    return 0;
}

/**
 * 创建解码上下文
 * @param parameters    媒体流的解码参数
 * @param decoder       解码器对象
 * @return  返回创建解码上下文操作的结果，0表示成功，-1表示失败
 */
int AVMediaPlayer::createCodecContext(AVCodecParameters *parameters, AVDecoder *decoder) {

    // 查找解码器
    AVCodec *pCodec = avcodec_find_decoder(parameters->codec_id);
    if (!pCodec) {
        if (mediaJniCall) {
            mediaJniCall->onError(WORKER_THREAD, 3, "get avcodec fail");
        }
        exit = true;
        return -1;
    }
    // 创建解码上下文
    AVCodecContext *context = avcodec_alloc_context3(pCodec);
    if (!context) {
        if (mediaJniCall) {
            mediaJniCall->onError(WORKER_THREAD, 4, "alloc avcodecctx fail");
        }
        exit = true;
        return -1;
    }

    // 复制解码参数
    if (avcodec_parameters_to_context(context, parameters) != 0) {
        if (mediaJniCall) {
            mediaJniCall->onError(WORKER_THREAD, 5, "copy avcodecctx fail");
        }
        exit = true;
        return -1;
    }

    // 打开解码器
    if (avcodec_open2(context, pCodec, 0) != 0) {
        if (mediaJniCall) {
            mediaJniCall->onError(WORKER_THREAD, 6, "open avcodecctx fail");
        }
        exit = true;
        return -1;
    }

    // 将解码上下文赋值给解码器对象
    decoder->setCodecContext(context);
    return 0;
}

/**
 * 开始播放，在onPrepared回调中执行
 * @return
 */
int AVMediaPlayer::start() {
    exit = false;
    int ret  = -1;
    // 播放音频
    if (audioDecoder != NULL) {
        audioDecoder->start();
    }

    // 播放视频
    if (videoDecoder != NULL) {
        videoDecoder->start();
    }

    // 视频同步器开始
    if (synchronizer != NULL) {
        synchronizer->start();
    }

    // 创建解复用线程
    mThread = ThreadCreate(demuxThread, this, "Demux Thread");
    return 0;
}

int AVMediaPlayer::demuxThread(void *context) {
    AVMediaPlayer *player = (AVMediaPlayer *) context;
    player->demuxFile();
    return 0;
}
/**
 * 对文件进行解封装(解复用)
 * @return
 */
int AVMediaPlayer::demuxFile() {
    int ret = -1;

    // 进入解码状态
    while(!mediaStatus->isExit()) {
        exit = false;
        // 暂停
        if (mediaStatus->isPause()) {
            av_usleep(1000 * 100);
            continue;
        }

        // 判断视频解码器中的裸数据包队列存放的裸数据包是否过多
        if (audioDecoder != NULL && audioDecoder->getPacketSize() > 30) {
            av_usleep(1000 * 100);
            continue;
        }

        // 判断视频解码器中的裸数据包队列存放的裸数据包是否过多
        if (videoDecoder != NULL && videoDecoder->getPacketSize() > 30) {
            av_usleep(1000 * 100);
            continue;
        }

        // 从文件中读取裸数据包
        AVPacket *packet = av_packet_alloc();
        pthread_mutex_lock(&mSeekMutex);
        ret = av_read_frame(pFormatCtx, packet);
        pthread_mutex_unlock(&mSeekMutex);
        // 判断是否处于定位状态
        if (mediaStatus->isSeek()) {
            av_packet_free(&packet);
            av_free(packet);
            continue;
        }

        // 如果成功读取裸数据包，则将裸数据包入队，否则释放裸数据包对象
        if (ret == 0) {
            if (audioDecoder && packet->stream_index ==  audioDecoder->getStreamIndex()) {
                int result = audioDecoder->putPacket(packet);
                if (result != 0) {
                    av_packet_free(&packet);
                    av_free(packet);
                    packet = NULL;
                }
            } else if (videoDecoder && packet->stream_index == videoDecoder->getStreamIndex()) {
                // 入队裸数据包
                int result = videoDecoder->putPacket(packet);
                if (result != 0) {
                    av_packet_free(&packet);
                    av_free(packet);
                    packet = NULL;
                }
            } else {    // 非音视频的裸数据包不做处理，直接销毁
                av_packet_free(&packet);
                av_free(packet);
                packet = NULL;
            }
        } else { // 读取裸数据包失败，则释放裸数据包资源，防止内存泄漏
            av_packet_free(&packet);
            av_free(packet);
            packet = NULL;

            // TODO 判断是否出错还是到了文件结尾，加入循环播放功能
            if ((videoDecoder != NULL && videoDecoder->getFrameSize() == 0)
                || (audioDecoder != NULL && audioDecoder->getPacketSize() == 0)) {
                mediaStatus->setExit(true);
                break;
            }
        }
    }

    // 播放完成
    if (!exitByUser && mediaJniCall != NULL) {
        mediaJniCall->onCompletion(WORKER_THREAD);
    }
    exit = true;
    return 0;
}

/**
 * 释放资源
 */
void AVMediaPlayer::release() {
    mediaStatus->setExit(true);
    ThreadDestroy(mThread);
    mThread = NULL;

    pthread_mutex_lock(&mMutex);
    int sleepCount = 0;

    // 等待退出
    while (!exit) {
        if (sleepCount > 1000) {
            exit = true;
        }
        sleepCount++;
        av_usleep(1000 * 10);
    }

    // 释放视频同步器
    if (synchronizer) {
        synchronizer->release();
        delete(synchronizer);
        synchronizer = NULL;
    }

    // 释放音频解码器
    if (audioDecoder != NULL) {
        audioDecoder->release();
        delete(audioDecoder);
        audioDecoder = NULL;
    }

    // 释放视频解码器
    if (videoDecoder != NULL) {
        videoDecoder->release();
        delete(videoDecoder);
        videoDecoder = NULL;
    }

    // 释放解封装上下文
    if (pFormatCtx != NULL) {
        avformat_close_input(&pFormatCtx);
        avformat_free_context(pFormatCtx);
        pFormatCtx = NULL;
    }

    // 释放JNI回调对象
    if (mediaJniCall != NULL) {
        mediaJniCall = NULL;
    }
    pthread_mutex_unlock(&mMutex);

    avformat_network_deinit();
    av_lockmgr_register(NULL);
}

/**
 * 暂停
 */
void AVMediaPlayer::pause() {
    if (mediaStatus != NULL) {
        mediaStatus->setPause(true);
        if (audioDecoder != NULL) {
            audioDecoder->pause();
        }
    }
}

/**
 * 再启动
 */
void AVMediaPlayer::resume() {
    if (mediaStatus != NULL) {
        mediaStatus->setPause(false);
        if (audioDecoder != NULL) {
            audioDecoder->resume();
        }
    }
}

/**
 * 停止
 */
void AVMediaPlayer::stop() {
    exitByUser = true;
    if (mediaStatus != NULL) {
        mediaStatus->setExit(true);
    }
    if (synchronizer != NULL) {
        synchronizer->stop();
    }
}
/**
 * 定位
 * @param sec
 * @return
 */
int AVMediaPlayer::seek(int64_t sec) {

    if (sec >= duration) {
        return -1;
    }

    if (mediaStatus->isLoad()) {
        return -1;
    }

    if (pFormatCtx != NULL) {
        mediaStatus->setSeek(true);
        pthread_mutex_lock(&mSeekMutex);
        int64_t rel = sec * AV_TIME_BASE;
        // 定位到想要的秒数附近
        int ret = avformat_seek_file(pFormatCtx, -1, INT64_MIN, rel, INT64_MAX, 0);
        // 清空音频解码器中存放的数据
        if (audioDecoder != NULL) {
            audioDecoder->clearFrame();
            audioDecoder->clearPacket();
            audioDecoder->setClock(0);
        }
        // 清空视频解码器中存放的数据
        if (videoDecoder != NULL) {
            videoDecoder->clearFrame();
            videoDecoder->clearPacket();
            videoDecoder->setClock(0);
        }
        pthread_mutex_unlock(&mSeekMutex);
        mediaStatus->setSeek(false);
    }

    return 0;
}

/**
 * 设置音频流索引
 * @param index
 */
void AVMediaPlayer::setAudioStream(int index) {
    if (audioDecoder != NULL) {
        if (index < getAudioStreams()) {
            for (int i = 0; i < getAudioStreams(); i++) {
                if (i == index) {
                    audioDecoder->setTimeBase(audioStreams.at(i)->getTimeBase());
                    audioDecoder->setStreamIndex(audioStreams.at(i)->getStreamIndex());
                }
            }
        }
    }
}

/**
 * 设置视频流
 * @param id
 */
void AVMediaPlayer::setVideoStream(int id) {
    if (videoDecoder != NULL) {
        videoDecoder->setStreamIndex(videoStreams.at(id)->getStreamIndex());
        videoDecoder->setTimeBase(videoStreams.at(id)->getTimeBase());
        videoDecoder->setVideoRate(1000 / videoStreams.at(id)->getFps());
        videoDecoder->setBigFrameRate(videoStreams.at(id)->getFps() >= 60);
    }
}

/**
 * 获取音频流索引数量(可能存在多音轨的情况)
 * @return
 */
int AVMediaPlayer::getAudioStreams() {
    return audioStreams.size();
}

/**
 * 获取视频宽度
 * @return
 */
int AVMediaPlayer::getVideoWidth() {
    if (videoDecoder != NULL && videoDecoder->getCodecContext() != NULL) {
        return videoDecoder->getCodecContext()->width;
    }
    return 0;
}

/**
 * 获取视频高度
 * @return
 */
int AVMediaPlayer::getVideoHeight() {
    if (videoDecoder != NULL && videoDecoder->getCodecContext() != NULL) {
        return videoDecoder->getCodecContext()->height;
    }
    return 0;
}

/**
 * 判断是否处于退出状态(如果用于存放媒体状态的对象没有初始化，则永远处于退出状态)
 * @return
 */
bool AVMediaPlayer::isExit() {
    if (mediaStatus) {
        return mediaStatus->isExit();
    }
    return true;
}

/**
 * 获取时长
 * @return
 */
int64_t AVMediaPlayer::getDuration() {
    return duration;
}

/**
 * 多线程锁管理器
 * @param mtx
 * @param op
 * @return
 */
int AVMediaPlayer::lockmgr(void **mtx, enum AVLockOp op) {
    switch(op) {
        case AV_LOCK_CREATE:
            *mtx = MutexCreate();
            if(!*mtx) {
                av_log(NULL, AV_LOG_FATAL, "Failed to create Mutex!\n");
                return 1;
            }
            return 0;

        case AV_LOCK_OBTAIN:
            return !!MutexLock((Mutex*)*mtx);

        case AV_LOCK_RELEASE:
            return !!MutexUnlock((Mutex*)*mtx);

        case AV_LOCK_DESTROY:
            MutexDestroy((Mutex*)*mtx);
            return 0;
    }
    return 1;
}