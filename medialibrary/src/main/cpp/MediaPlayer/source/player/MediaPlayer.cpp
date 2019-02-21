//
// Created by cain on 2018/12/26.
//

#include "MediaPlayer.h"

/**
 * FFmpeg操作锁管理回调
 * @param mtx
 * @param op
 * @return
 */
static int lockmgrCallback(void **mtx, enum AVLockOp op) {
    switch (op) {
        case AV_LOCK_CREATE: {
            *mtx = new Mutex();
            if (!*mtx) {
                av_log(NULL, AV_LOG_FATAL, "failed to create mutex.\n");
                return 1;
            }
            return 0;
        }

        case AV_LOCK_OBTAIN: {
            if (!*mtx) {
                return 1;
            }
            return ((Mutex *)(*mtx))->lock() != 0;
        }

        case AV_LOCK_RELEASE: {
            if (!*mtx) {
                return 1;
            }
            return ((Mutex *)(*mtx))->unlock() != 0;
        }

        case AV_LOCK_DESTROY: {
            if (!*mtx) {
                delete (*mtx);
                *mtx = NULL;
            }
            return 0;
        }
    }
    return 1;
}

MediaPlayer::MediaPlayer() {
    av_register_all();
    avformat_network_init();
    playerState = new PlayerState();
    messageQueue = new AVMessageQueue();
    mDuration = -1;
    audioDecoder = NULL;
    videoDecoder = NULL;
    pFormatCtx = NULL;
    lastPaused = -1;
    attachmentRequest = 0;

#if defined(__ANDROID__)
    audioDevice = new SLESDevice();
#else
    audioDevice = new AudioDevice();
#endif

    mediaSync = new MediaSync(playerState);
    audioResampler = NULL;
    readThread = NULL;
    mExit = true;

    // 注册一个多线程锁管理回调，主要是解决多个视频源时保持avcodec_open/close的原子操作
    if (av_lockmgr_register(lockmgrCallback)) {
        av_log(NULL, AV_LOG_FATAL, "Could not initialize lock manager!\n");
    }

}

MediaPlayer::~MediaPlayer() {
    avformat_network_deinit();
    av_lockmgr_register(NULL);
}

status_t MediaPlayer::reset() {
    stop();
    if (mediaSync) {
        mediaSync->reset();
        delete mediaSync;
        mediaSync = NULL;
    }
    if (audioDecoder != NULL) {
        audioDecoder->stop();
        delete audioDecoder;
        audioDecoder = NULL;
    }
    if (videoDecoder != NULL) {
        videoDecoder->stop();
        delete videoDecoder;
        videoDecoder = NULL;
    }
    if (audioDevice != NULL) {
        audioDevice->stop();
        delete audioDevice;
        audioDevice = NULL;
    }
    if (audioResampler) {
        delete audioResampler;
        audioResampler = NULL;
    }
    if (pFormatCtx != NULL) {
        avformat_close_input(&pFormatCtx);
        avformat_free_context(pFormatCtx);
        pFormatCtx = NULL;
    }
    if (messageQueue) {
        messageQueue->release();
        delete messageQueue;
    }
    if (playerState) {
        delete playerState;
        playerState = NULL;
    }
    return NO_ERROR;
}

void MediaPlayer::setDataSource(const char *url, int64_t offset, const char *headers) {
    Mutex::Autolock lock(mMutex);
    playerState->url = av_strdup(url);
    playerState->offset = offset;
    if (headers) {
        playerState->headers = av_strdup(headers);
    }
}

void MediaPlayer::setVideoDevice(VideoDevice *videoDevice) {
    Mutex::Autolock lock(mMutex);
    mediaSync->setVideoDevice(videoDevice);
}

status_t MediaPlayer::prepare() {
    Mutex::Autolock lock(mMutex);
    if (!playerState->url) {
        return BAD_VALUE;
    }
    playerState->abortRequest = 0;
    if (!readThread) {
        readThread = new Thread(this);
        readThread->start();
    }
    return NO_ERROR;
}

status_t MediaPlayer::prepareAsync() {
    Mutex::Autolock lock(mMutex);
    if (!playerState->url) {
        return BAD_VALUE;
    }
    // 发送消息请求准备
    if (messageQueue) {
        messageQueue->postMessage(MSG_REQUEST_PREPARE);
    }
    return NO_ERROR;
}

void MediaPlayer::start() {
    Mutex::Autolock lock(mMutex);
    playerState->abortRequest = 0;
    playerState->pauseRequest = 0;
    mExit = false;
    mCondition.signal();
}

void MediaPlayer::pause() {
    Mutex::Autolock lock(mMutex);
    playerState->pauseRequest = 1;
    mCondition.signal();
}

void MediaPlayer::resume() {
    Mutex::Autolock lock(mMutex);
    playerState->pauseRequest = 0;
    mCondition.signal();
}

void MediaPlayer::stop() {
    mMutex.lock();
    playerState->abortRequest = 1;
    mCondition.signal();
    mMutex.unlock();
    mMutex.lock();
    while (!mExit) {
        mCondition.wait(mMutex);
    }
    mMutex.unlock();
    if (readThread != NULL) {
        readThread->join();
        delete readThread;
        readThread = NULL;
    }
}

void MediaPlayer::seekTo(float timeMs) {
    // when is a live media stream, duration is -1
    if (!playerState->realTime && mDuration < 0) {
        return;
    }

    // 等待上一次操作完成
    mMutex.lock();
    while (playerState->seekRequest) {
        mCondition.wait(mMutex);
    }
    mMutex.unlock();

    if (!playerState->seekRequest) {
        int64_t start_time = 0;
        int64_t seek_pos = av_rescale(timeMs, AV_TIME_BASE, 1000);
        start_time = pFormatCtx ? pFormatCtx->start_time : 0;
        if (start_time > 0 && start_time != AV_NOPTS_VALUE) {
            seek_pos += start_time;
        }
        playerState->seekPos = seek_pos;
        playerState->seekRel = 0;
        playerState->seekFlags &= ~AVSEEK_FLAG_BYTE;
        playerState->seekRequest = 1;
        mCondition.signal();
    }

}

void MediaPlayer::setLooping(int looping) {
    mMutex.lock();
    playerState->loop = looping;
    mCondition.signal();
    mMutex.unlock();
}

void MediaPlayer::setVolume(float leftVolume, float rightVolume) {
    if (audioDevice) {
        audioDevice->setStereoVolume(leftVolume, rightVolume);
    }
}

void MediaPlayer::setMute(int mute) {
    mMutex.lock();
    playerState->mute = mute;
    mCondition.signal();
    mMutex.unlock();
}

void MediaPlayer::setRate(float rate) {
    mMutex.lock();
    playerState->playbackRate = rate;
    mCondition.signal();
    mMutex.unlock();
}

void MediaPlayer::setPitch(float pitch) {
    mMutex.lock();
    playerState->playbackPitch = pitch;
    mCondition.signal();
    mMutex.unlock();
}

int MediaPlayer::getVideoWidth() {
    Mutex::Autolock lock(mMutex);
    if (videoDecoder) {
        return videoDecoder->getCodecContext()->width;
    }
    return 0;
}

int MediaPlayer::getVideoHeight() {
    Mutex::Autolock lock(mMutex);
    if (videoDecoder) {
        return videoDecoder->getCodecContext()->height;
    }
    return 0;
}

long MediaPlayer::getCurrentPosition() {
    Mutex::Autolock lock(mMutex);
    int64_t currentPosition = 0;
    // 处于定位
    if (playerState->seekRequest) {
        currentPosition = playerState->seekPos;
    } else {

        // 起始延时
        int64_t start_time = pFormatCtx->start_time;
        int64_t start_diff = 0;
        if (start_time > 0 && start_time != AV_NOPTS_VALUE) {
            start_diff = av_rescale(start_time, 1000, AV_TIME_BASE);
        }

        // 计算主时钟的时间
        int64_t pos = 0;
        double clock = mediaSync->getMasterClock();
        if (isnan(clock)) {
            pos = playerState->seekPos;
        } else {
            pos = (int64_t)(clock * 1000);
        }
        if (pos < 0 || pos < start_diff) {
            return 0;
        }
        return (long) (pos - start_diff);
    }
    return (long)currentPosition;
}

long MediaPlayer::getDuration() {
    Mutex::Autolock lock(mMutex);
    return (long)mDuration;
}

int MediaPlayer::isPlaying() {
    Mutex::Autolock lock(mMutex);
    return !playerState->abortRequest && !playerState->pauseRequest;
}

int MediaPlayer::isLooping() {
    return playerState->loop;
}

int MediaPlayer::getMetadata(AVDictionary **metadata) {
    if (!pFormatCtx) {
        return -1;
    }
    // TODO getMetadata
    return NO_ERROR;
}

static int avformat_interrupt_cb(void *ctx) {
    PlayerState *playerState = (PlayerState *) ctx;
    if (playerState->abortRequest) {
        return AVERROR_EOF;
    }
    return 0;
}

AVMessageQueue *MediaPlayer::getMessageQueue() {
    Mutex::Autolock lock(mMutex);
    return messageQueue;
}

PlayerState *MediaPlayer::getPlayerState() {
    Mutex::Autolock lock(mMutex);
    return playerState;
}

void MediaPlayer::run() {
    readPackets();
}

int MediaPlayer::readPackets() {
    int ret = 0;
    AVDictionaryEntry *t;
    AVDictionary **opts;
    int scan_all_pmts_set = 0;

    // 准备解码器
    mMutex.lock();
    do {
        // 创建解复用上下文
        pFormatCtx = avformat_alloc_context();
        if (!pFormatCtx) {
            av_log(NULL, AV_LOG_FATAL, "Could not allocate context.\n");
            ret = AVERROR(ENOMEM);
            break;
        }

        // 设置解复用中断回调
        pFormatCtx->interrupt_callback.callback = avformat_interrupt_cb;
        pFormatCtx->interrupt_callback.opaque = playerState;
        if (!av_dict_get(playerState->format_opts, "scan_all_pmts", NULL, AV_DICT_MATCH_CASE)) {
            av_dict_set(&playerState->format_opts, "scan_all_pmts", "1", AV_DICT_DONT_OVERWRITE);
            scan_all_pmts_set = 1;
        }

        // 处理文件头
        if (playerState->headers) {
            av_dict_set(&playerState->format_opts, "headers", playerState->headers, 0);
        }
        // 处理文件偏移量
        if (playerState->offset > 0) {
            pFormatCtx->skip_initial_bytes = playerState->offset;
        }

        // 设置rtmp/rtsp的超时值
        if (av_stristart(playerState->url, "rtmp", NULL) || av_stristart(playerState->url, "rtsp", NULL)) {
            // There is total different meaning for 'timeout' option in rtmp
            av_log(NULL, AV_LOG_WARNING, "remove 'timeout' option for rtmp.\n");
            av_dict_set(&playerState->format_opts, "timeout", NULL, 0);
        }

        // 打开文件
        ret = avformat_open_input(&pFormatCtx, playerState->url, playerState->iformat, &playerState->format_opts);
        if (ret < 0) {
            printError(playerState->url, ret);
            ret = -1;
            break;
        }

        // 打开文件回调
        if (messageQueue) {
            messageQueue->postMessage(MSG_OPEN_INPUT);
        }

        if (scan_all_pmts_set) {
            av_dict_set(&playerState->format_opts, "scan_all_pmts", NULL, AV_DICT_MATCH_CASE);
        }

        if ((t = av_dict_get(playerState->format_opts, "", NULL, AV_DICT_IGNORE_SUFFIX))) {
            av_log(NULL, AV_LOG_ERROR, "Option %s not found.\n", t->key);
            ret = AVERROR_OPTION_NOT_FOUND;
            break;
        }

        if (playerState->genpts) {
            pFormatCtx->flags |= AVFMT_FLAG_GENPTS;
        }
        av_format_inject_global_side_data(pFormatCtx);

        opts = setupStreamInfoOptions(pFormatCtx, playerState->codec_opts);

        // 查找媒体流信息
        ret = avformat_find_stream_info(pFormatCtx, opts);
        if (opts != NULL) {
            for (int i = 0; i < pFormatCtx->nb_streams; i++) {
                if (opts[i] != NULL) {
                    av_dict_free(&opts[i]);
                }
            }
            av_freep(&opts);
        }

        if (ret < 0) {
            av_log(NULL, AV_LOG_WARNING,
                   "%s: could not find codec parameters\n", playerState->url);
            ret = -1;
            break;
        }

        // 查找媒体流信息回调
        if (messageQueue) {
            messageQueue->postMessage(MSG_FIND_STREAM_INFO);
        }

        // 判断是否实时流，判断是否需要设置无限缓冲区
        playerState->realTime = isRealTime(pFormatCtx);
        if (playerState->infiniteBuffer < 0 && playerState->realTime) {
            playerState->infiniteBuffer = 1;
        }

        // Gets the duration of the file, -1 if no duration available.
        if (playerState->realTime) {
            mDuration = -1;
        } else {
            mDuration = -1;
            if (pFormatCtx->duration != AV_NOPTS_VALUE) {
                mDuration = av_rescale(pFormatCtx->duration, 1000, AV_TIME_BASE);
            }
        }

        if (pFormatCtx->pb) {
            pFormatCtx->pb->eof_reached = 0;
        }
        // 判断是否以字节方式定位
        playerState->seekByBytes = !!(pFormatCtx->iformat->flags & AVFMT_TS_DISCONT)
                                     && strcmp("ogg", pFormatCtx->iformat->name);

        // 设置最大帧间隔
        mediaSync->setMaxDuration((pFormatCtx->iformat->flags & AVFMT_TS_DISCONT) ? 10.0 : 3600.0);

        // 如果不是从头开始播放，则跳转到播放位置
        if (playerState->startTime != AV_NOPTS_VALUE) {
            int64_t timestamp;

            timestamp = playerState->startTime;
            if (pFormatCtx->start_time != AV_NOPTS_VALUE) {
                timestamp += pFormatCtx->start_time;
            }
            playerState->mMutex.lock();
            ret = avformat_seek_file(pFormatCtx, -1, INT64_MIN, timestamp, INT64_MAX, 0);
            playerState->mMutex.unlock();
            if (ret < 0) {
                av_log(NULL, AV_LOG_WARNING, "%s: could not seek to position %0.3f\n",
                       playerState->url, (double)timestamp / AV_TIME_BASE);
            }
        }

        // 查找媒体流信息
        int audioIndex = -1;
        int videoIndex = -1;
        for (int i = 0; i < pFormatCtx->nb_streams; ++i) {
            if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
                if (audioIndex == -1) {
                    audioIndex = i;
                }
            } else if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                if (videoIndex == -1) {
                    videoIndex = i;
                }
            }
        }
        // 如果不禁止视频流，则查找最合适的视频流索引
        if (!playerState->videoDisable) {
            videoIndex = av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_VIDEO,
                                             videoIndex, -1, NULL, 0);
        } else {
            videoIndex = -1;
        }
        // 如果不禁止音频流，则查找最合适的音频流索引(与视频流关联的音频流)
        if (!playerState->audioDisable) {
            audioIndex = av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_AUDIO,
                                             audioIndex, videoIndex, NULL, 0);
        } else {
            audioIndex = -1;
        }

        // 如果音频流和视频流都没有找到，则直接退出
        if (audioIndex == -1 && videoIndex == -1) {
            av_log(NULL, AV_LOG_WARNING,
                   "%s: could not find audio and video stream\n", playerState->url);
            ret = -1;
            break;
        }

        // 根据媒体流索引准备解码器
        if (audioIndex >= 0) {
            prepareDecoder(audioIndex);
        }
        if (videoIndex >= 0) {
            prepareDecoder(videoIndex);
        }

        if (!audioDecoder && !videoDecoder) {
            av_log(NULL, AV_LOG_WARNING,
                   "failed to create audio and video decoder\n");
            ret = -1;
            break;
        }
        ret = 0;

        // 准备解码器消息回调
        if (messageQueue) {
            messageQueue->postMessage(MSG_PREPARE_DECODER);
        }

    } while (false);
    mMutex.unlock();

    // 出错返回
    if (ret < 0) {
        mExit = true;
        mCondition.signal();
        if (messageQueue) {
            const char errorMsg[] = "prepare decoder failed!";
            messageQueue->postMessage(MSG_ERROR, 0, 0,
                    (void *)errorMsg, sizeof(errorMsg) / errorMsg[0]);
        }
        return -1;
    }

    if (videoDecoder) {
        AVCodecParameters *codecpar = videoDecoder->getStream()->codecpar;
        if (messageQueue) {
            messageQueue->postMessage(MSG_VIDEO_SIZE_CHANGED,
                    codecpar->width, codecpar->height);
            messageQueue->postMessage(MSG_SAR_CHANGED, codecpar->sample_aspect_ratio.num,
                    codecpar->sample_aspect_ratio.den);
        }
    }

    // 准备完成回调
    if (messageQueue) {
        messageQueue->postMessage(MSG_PREPARED);
    }

    // 视频解码器开始解码
    if (videoDecoder != NULL) {
        videoDecoder->start();
        if (messageQueue) {
            messageQueue->postMessage(MSG_VIDEO_START);
        }
    } else {
        if (playerState->syncType == AV_SYNC_VIDEO) {
            playerState->syncType = AV_SYNC_AUDIO;
        }
    }

    // 音频解码器开始解码
    if (audioDecoder != NULL) {
        audioDecoder->start();
        if (messageQueue) {
            messageQueue->postMessage(MSG_AUDIO_START);
        }
    } else {
        if (playerState->syncType == AV_SYNC_AUDIO) {
            playerState->syncType = AV_SYNC_EXTERNAL;
        }
    }

    // 打开音频输出设备
    if (audioDecoder != NULL) {
        AVCodecContext *avctx = audioDecoder->getCodecContext();
        ret = openAudioDevice(avctx->channel_layout, avctx->channels,
                        avctx->sample_rate);
        if (ret < 0) {
            av_log(NULL, AV_LOG_WARNING, "could not open audio device\n");
            // 如果音频设备打开失败，则调整时钟的同步类型
            if (playerState->syncType == AV_SYNC_AUDIO) {
                if (videoDecoder != NULL) {
                    playerState->syncType = AV_SYNC_VIDEO;
                } else {
                    playerState->syncType = AV_SYNC_EXTERNAL;
                }
            }
        } else {
            // 启动音频输出设备
            audioDevice->start();
        }
    }

    if (videoDecoder) {
        if (playerState->syncType == AV_SYNC_AUDIO) {
            videoDecoder->setMasterClock(mediaSync->getAudioClock());
        } else if (playerState->syncType == AV_SYNC_VIDEO) {
            videoDecoder->setMasterClock(mediaSync->getVideoClock());
        } else {
            videoDecoder->setMasterClock(mediaSync->getExternalClock());
        }
    }

    // 开始同步
    mediaSync->start(videoDecoder, audioDecoder);

    // 等待开始
    if (playerState->pauseRequest) {
        // 请求开始
        if (messageQueue) {
            messageQueue->postMessage(MSG_REQUEST_START);
        }
        while ((!playerState->abortRequest) && playerState->pauseRequest) {
            av_usleep(10 * 1000);
        }
    }

    if (messageQueue) {
        messageQueue->postMessage(MSG_STARTED);
    }

    // 读数据包流程
    eof = 0;
    ret = 0;
    AVPacket pkt1, *pkt = &pkt1;
    int64_t stream_start_time;
    int playInRange = 0;
    int64_t pkt_ts;
    for (;;) {

        // 退出播放器
        if (playerState->abortRequest) {
            break;
        }

        // 是否暂停
        if (playerState->pauseRequest != lastPaused) {
            lastPaused = playerState->pauseRequest;
            if (playerState->pauseRequest) {
                av_read_pause(pFormatCtx);
            } else {
                av_read_play(pFormatCtx);
            }
        }

#if CONFIG_RTSP_DEMUXER || CONFIG_MMSH_PROTOCOL
        if (playerState->pauseRequest &&
            (!strcmp(pFormatCtx->iformat->name, "rtsp") ||
             (pFormatCtx->pb && !strncmp(url, "mmsh:", 5)))) {
            av_usleep(10 * 1000);
            continue;
        }
#endif
        // 定位处理
        if (playerState->seekRequest) {
            int64_t seek_target = playerState->seekPos;
            int64_t seek_min = playerState->seekRel > 0 ? seek_target - playerState->seekRel + 2: INT64_MIN;
            int64_t seek_max = playerState->seekRel < 0 ? seek_target - playerState->seekRel - 2: INT64_MAX;
            // 定位
            playerState->mMutex.lock();
            ret = avformat_seek_file(pFormatCtx, -1, seek_min, seek_target, seek_max, playerState->seekFlags);
            playerState->mMutex.unlock();
            if (ret < 0) {
                av_log(NULL, AV_LOG_ERROR, "%s: error while seeking\n", playerState->url);
            } else {
                if (audioDecoder) {
                    audioDecoder->flush();
                }
                if (videoDecoder) {
                    videoDecoder->flush();
                }

                // 更新外部时钟值
                if (playerState->seekFlags & AVSEEK_FLAG_BYTE) {
                    mediaSync->updateExternalClock(NAN);
                } else {
                    mediaSync->updateExternalClock(seek_target / (double)AV_TIME_BASE);
                }
                mediaSync->refreshVideoTimer();
            }
            attachmentRequest = 1;
            playerState->seekRequest = 0;
            mCondition.signal();
            eof = 0;
            // 定位完成回调通知
            if (messageQueue) {
                messageQueue->postMessage(MSG_SEEK_COMPLETE,
                        (int)av_rescale(seek_target, 1000, AV_TIME_BASE), ret);
            }
        }

        // 取得封面数据包
        if (attachmentRequest) {
            if (videoDecoder && (videoDecoder->getStream()->disposition
                                 & AV_DISPOSITION_ATTACHED_PIC)) {
                AVPacket copy;
                if ((ret = av_copy_packet(&copy, &videoDecoder->getStream()->attached_pic)) < 0) {
                    break;
                }
                videoDecoder->pushPacket(&copy);
            }
            attachmentRequest = 0;
        }

        // 如果队列中存在足够的数据包，则等待消耗
        // 备注：这里要等待一定时长的缓冲队列，要不然会导致OpenSLES播放音频出现卡顿等现象
        if (playerState->infiniteBuffer < 1 &&
            ((audioDecoder ? audioDecoder->getMemorySize() : 0) + (videoDecoder ? videoDecoder->getMemorySize() : 0) > MAX_QUEUE_SIZE
             || (!audioDecoder || audioDecoder->hasEnoughPackets()) && (!videoDecoder || videoDecoder->hasEnoughPackets()))) {
            continue;
        }

        // 读出数据包
        ret = av_read_frame(pFormatCtx, pkt);
        if (ret < 0) {
            // 如果没能读出裸数据包，判断是否是结尾
            if ((ret == AVERROR_EOF || avio_feof(pFormatCtx->pb)) && !eof) {
                eof = 1;
            }
            // 读取出错，则直接退出
            if (pFormatCtx->pb && pFormatCtx->pb->error) {
                ret = -1;
                break;
            }

            // 如果不处于暂停状态，并且队列中所有数据都没有，则判断是否需要
            if (!playerState->pauseRequest && (!audioDecoder || audioDecoder->getPacketSize() == 0)
                && (!videoDecoder || (videoDecoder->getPacketSize() == 0
                                      && videoDecoder->getFrameSize() == 0))) {
                if (playerState->loop) {
                    seekTo(playerState->startTime != AV_NOPTS_VALUE ? playerState->startTime : 0);
                } else if (playerState->autoExit) {
                    ret = AVERROR_EOF;
                    break;
                }
            }
            // 读取失败时，睡眠10毫秒继续
            av_usleep(10 * 1000);
            continue;
        } else {
            eof = 0;
        }

        // 计算pkt的pts是否处于播放范围内
        stream_start_time = pFormatCtx->streams[pkt->stream_index]->start_time;
        pkt_ts = pkt->pts == AV_NOPTS_VALUE ? pkt->dts : pkt->pts;
        // 播放范围
        playInRange = playerState->duration == AV_NOPTS_VALUE
                      || (pkt_ts - (stream_start_time != AV_NOPTS_VALUE ? stream_start_time : 0)) *
                         av_q2d(pFormatCtx->streams[pkt->stream_index]->time_base)
                         - (double)(playerState->startTime != AV_NOPTS_VALUE ? playerState->startTime : 0) / 1000000
                         <= ((double)playerState->duration / 1000000);
        if (playInRange && audioDecoder && pkt->stream_index == audioDecoder->getStreamIndex()) {
            audioDecoder->pushPacket(pkt);
        } else if (playInRange && videoDecoder && pkt->stream_index == videoDecoder->getStreamIndex()) {
            videoDecoder->pushPacket(pkt);
        } else {
            av_packet_unref(pkt);
        }
    }

    if (audioDecoder) {
        audioDecoder->stop();
    }
    if (videoDecoder) {
        videoDecoder->stop();
    }
    if (audioDevice) {
        audioDevice->stop();
    }
    if (mediaSync) {
        mediaSync->stop();
    }
    mExit = true;
    mCondition.signal();

    if (ret < 0) {
        if (messageQueue) {
            const char errorMsg[] = "error when reading packets!";
            messageQueue->postMessage(MSG_ERROR, 0, 0,
                    (void *)errorMsg, sizeof(errorMsg) / errorMsg[0]);
        }
    } else { // 播放完成
        if (messageQueue) {
            messageQueue->postMessage(MSG_COMPLETED);
        }
    }

    return ret;
}

int MediaPlayer::prepareDecoder(int streamIndex) {
    AVCodecContext *avctx;
    AVCodec *codec = NULL;
    AVDictionary *opts = NULL;
    AVDictionaryEntry *t = NULL;
    int ret = 0;
    const char *forcedCodecName = NULL;

    if (streamIndex < 0 || streamIndex >= pFormatCtx->nb_streams) {
        return -1;
    }

    // 创建解码上下文
    avctx = avcodec_alloc_context3(NULL);
    if (!avctx) {
        return AVERROR(ENOMEM);
    }

    do {
        // 复制解码上下文参数
        ret = avcodec_parameters_to_context(avctx, pFormatCtx->streams[streamIndex]->codecpar);
        if (ret < 0) {
            break;
        }

        // 设置时钟基准
        av_codec_set_pkt_timebase(avctx, pFormatCtx->streams[streamIndex]->time_base);

        // 优先使用指定的解码器
        switch(avctx->codec_type) {
            case AVMEDIA_TYPE_AUDIO: {
                forcedCodecName = playerState->audioCodecName;
                break;
            }
            case AVMEDIA_TYPE_VIDEO: {
                forcedCodecName = playerState->videoCodecName;
                break;
            }
        }
        if (forcedCodecName) {
            ALOGD("forceCodecName = %s", forcedCodecName);
            codec = avcodec_find_decoder_by_name(forcedCodecName);
        }

        // 如果没有找到指定的解码器，则查找默认的解码器
        if (!codec) {
            if (forcedCodecName) {
                av_log(NULL, AV_LOG_WARNING,
                       "No codec could be found with name '%s'\n", forcedCodecName);
            }
            codec = avcodec_find_decoder(avctx->codec_id);
        }

        // 判断是否成功得到解码器
        if (!codec) {
            av_log(NULL, AV_LOG_WARNING,
                   "No codec could be found with id %d\n", avctx->codec_id);
            ret = AVERROR(EINVAL);
            break;
        }
        avctx->codec_id = codec->id;

        // 设置一些播放参数
        int stream_lowres = playerState->lowres;
        if (stream_lowres > av_codec_get_max_lowres(codec)) {
            av_log(avctx, AV_LOG_WARNING, "The maximum value for lowres supported by the decoder is %d\n",
                   av_codec_get_max_lowres(codec));
            stream_lowres = av_codec_get_max_lowres(codec);
        }
        av_codec_set_lowres(avctx, stream_lowres);
#if FF_API_EMU_EDGE
        if (stream_lowres) {
            avctx->flags |= CODEC_FLAG_EMU_EDGE;
        }
#endif
        if (playerState->fast) {
            avctx->flags2 |= AV_CODEC_FLAG2_FAST;
        }
#if FF_API_EMU_EDGE
        if (codec->capabilities & AV_CODEC_CAP_DR1) {
            avctx->flags |= CODEC_FLAG_EMU_EDGE;
        }
#endif
        opts = filterCodecOptions(playerState->codec_opts, avctx->codec_id, pFormatCtx, pFormatCtx->streams[streamIndex], codec);
        if (!av_dict_get(opts, "threads", NULL, 0)) {
            av_dict_set(&opts, "threads", "auto", 0);
        }

        if (stream_lowres) {
            av_dict_set_int(&opts, "lowres", stream_lowres, 0);
        }

        if (avctx->codec_type == AVMEDIA_TYPE_VIDEO || avctx->codec_type == AVMEDIA_TYPE_AUDIO) {
            av_dict_set(&opts, "refcounted_frames", "1", 0);
        }

        // 打开解码器
        if ((ret = avcodec_open2(avctx, codec, &opts)) < 0) {
            break;
        }
        if ((t = av_dict_get(opts, "", NULL, AV_DICT_IGNORE_SUFFIX))) {
            av_log(NULL, AV_LOG_ERROR, "Option %s not found.\n", t->key);
            ret =  AVERROR_OPTION_NOT_FOUND;
            break;
        }

        // 根据解码器类型创建解码器
        pFormatCtx->streams[streamIndex]->discard = AVDISCARD_DEFAULT;
        switch (avctx->codec_type) {
            case AVMEDIA_TYPE_AUDIO: {
                audioDecoder = new AudioDecoder(avctx, pFormatCtx->streams[streamIndex],
                                                streamIndex, playerState);
                break;
            }

            case AVMEDIA_TYPE_VIDEO: {
                videoDecoder = new VideoDecoder(pFormatCtx, avctx, pFormatCtx->streams[streamIndex],
                                                streamIndex, playerState);
                attachmentRequest = 1;
                break;
            }

            default:{
                break;
            }
        }
    } while (false);

    // 准备失败，则需要释放创建的解码上下文
    if (ret < 0) {
        if (messageQueue) {
            const char errorMsg[] = "failed to open stream!";
            messageQueue->postMessage(MSG_ERROR, 0, 0,
                    (void *)errorMsg, sizeof(errorMsg) / errorMsg[0]);
        }
        avcodec_free_context(&avctx);
    }

    // 释放参数
    av_dict_free(&opts);

    return ret;
}

void audioPCMQueueCallback(void *opaque, uint8_t *stream, int len) {
    MediaPlayer *mediaPlayer = (MediaPlayer *) opaque;
    mediaPlayer->pcmQueueCallback(stream, len);
}

int MediaPlayer::openAudioDevice(int64_t wanted_channel_layout, int wanted_nb_channels,
                                 int wanted_sample_rate) {
    AudioDeviceSpec wanted_spec, spec;
    const int next_nb_channels[] = {0, 0, 1, 6, 2, 6, 4, 6};
    const int next_sample_rates[] = {44100, 48000};
    int next_sample_rate_idx = FF_ARRAY_ELEMS(next_sample_rates) - 1;
    if (wanted_nb_channels != av_get_channel_layout_nb_channels(wanted_channel_layout)
        || !wanted_channel_layout) {
        wanted_channel_layout = av_get_default_channel_layout(wanted_nb_channels);
        wanted_channel_layout &= ~AV_CH_LAYOUT_STEREO_DOWNMIX;
    }
    wanted_nb_channels = av_get_channel_layout_nb_channels(wanted_channel_layout);
    wanted_spec.channels = wanted_nb_channels;
    wanted_spec.freq = wanted_sample_rate;
    if (wanted_spec.freq <= 0 || wanted_spec.channels <= 0) {
        av_log(NULL, AV_LOG_ERROR, "Invalid sample rate or channel count!\n");
        return -1;
    }
    while (next_sample_rate_idx && next_sample_rates[next_sample_rate_idx] >= wanted_spec.freq) {
        next_sample_rate_idx--;
    }

    wanted_spec.format = AV_SAMPLE_FMT_S16;
    wanted_spec.samples = FFMAX(AUDIO_MIN_BUFFER_SIZE,
                                2 << av_log2(wanted_spec.freq / AUDIO_MAX_CALLBACKS_PER_SEC));
    wanted_spec.callback = audioPCMQueueCallback;
    wanted_spec.userdata = this;

    // 打开音频设备
    while (audioDevice->open(&wanted_spec, &spec) < 0) {
        av_log(NULL, AV_LOG_WARNING, "Failed to open audio device: (%d channels, %d Hz)!\n",
               wanted_spec.channels, wanted_spec.freq);
        wanted_spec.channels = next_nb_channels[FFMIN(7, wanted_spec.channels)];
        if (!wanted_spec.channels) {
            wanted_spec.freq = next_sample_rates[next_sample_rate_idx--];
            wanted_spec.channels = wanted_nb_channels;
            if (!wanted_spec.freq) {
                av_log(NULL, AV_LOG_ERROR, "No more combinations to try, audio open failed\n");
                return -1;
            }
        }
        wanted_channel_layout = av_get_default_channel_layout(wanted_spec.channels);
    }

    if (spec.format != AV_SAMPLE_FMT_S16) {
        av_log(NULL, AV_LOG_ERROR, "audio format %d is not supported!\n", spec.format);
        return -1;
    }

    if (spec.channels != wanted_spec.channels) {
        wanted_channel_layout = av_get_default_channel_layout(spec.channels);
        if (!wanted_channel_layout) {
            av_log(NULL, AV_LOG_ERROR, "channel count %d is not supported!\n", spec.channels);
            return -1;
        }
    }

    // 初始化音频重采样器
    if (!audioResampler) {
        audioResampler = new AudioResampler(playerState, audioDecoder, mediaSync);
    }
    // 设置需要重采样的参数
    audioResampler->setResampleParams(&spec, wanted_channel_layout);

    return spec.size;
}

void MediaPlayer::pcmQueueCallback(uint8_t *stream, int len) {
    if (!audioResampler) {
        memset(stream, 0, sizeof(len));
        return;
    }
    audioResampler->pcmQueueCallback(stream, len);
}
