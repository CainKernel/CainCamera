//
// Created by cain on 2018/2/8.
//

#include <Timer.h>
#include "CainPlayer.h"

static AVPacket flush_pkt;

/**
 * 构造器
 */
CainPlayer::CainPlayer()
        : filename(NULL),
          nativeWindow(NULL),
          currentPosition(0),
          duration(0),
          looping(false),
          prepared(false),
          paused(false),
          stopped(false),
          muted(false),
          reversed(false),
          displayWidth(0),
          displayHeight(0),
          videoWidth(0),
          videoHeight(0),
          seekRequest(false),
          seekPos(-1),
          ic(NULL),
          realtime(false) {

}

/**
 * 析构器
 */
CainPlayer::~CainPlayer() {
    release();
}

/**
 * 设置数据源
 * @param path
 */
void CainPlayer::setDataSource(const char *path) {
    // 清除旧文件
    if (filename) {
        free(filename);
    }
    filename = av_strdup(path);
}

/**
 * 设置Surface
 * @param env
 * @param surface
 */
void CainPlayer::setSurface(JNIEnv *env, jobject surface) {
    if (nativeWindow) {
        ANativeWindow_release(nativeWindow);
    }
    nativeWindow = ANativeWindow_fromSurface(env, surface);
}

/**
 * 获取当前位置
 */
int CainPlayer::getCurrentPosition() {
    return currentPosition;
}


/**
 * 获取时长
 * @return
 */
int CainPlayer::getDuration() {
    return videoLength;
}

/**
 * 是否循环播放
 * @return
 */
bool CainPlayer::isLooping() {
    return looping;
}

/**
 * 是否正在播放
 * @return
 */
bool CainPlayer::isPlaying() {
    // 已经准备好，并且不处于暂停和停止状态
    return prepared && !paused && !stopped;
}

/**
 * 是否处于停止状态
 * @return
 */
bool CainPlayer::isStopped() {
    return stopped;
}

/**
 * 暂停
 */
void CainPlayer::pause() {
    if (prepared) {
        paused = true;
    }
}


/**
 * 开始
 */
void CainPlayer::start() {
    if (prepared) {
        stopped = false;
        paused = false;
    }
}

/**
 * 停止
 */
void CainPlayer::stop() {
    if (prepared) {
        stopped = true;
    }
}

/**
 * 解码中断回调，当处于停止状态时，停止解码
 * @param arg
 * @return
 */
static int decode_interrupt_cb(void *arg) {
    CainPlayer * player = (CainPlayer *)arg;
    return player->isStopped();
}

/**
 * 异步装载流媒体
 */
void CainPlayer::prepare() {
    // 如果文件名不存在，则直接退出
    if (!filename) {
        ALOGE("File Not Found!");
        return;
    }
    // 打开媒体流失败，则需要关闭媒体流
    if (openStream() < 0) {
        closeStream();
        return;
    }
    prepared = true;
}

/**
 * 重置所有状态
 */
void CainPlayer::reset() {
    if (filename) {
        free(filename);
    }
    if (nativeWindow) {
        ANativeWindow_release(nativeWindow);
    }
    currentPosition = 0;
    duration = 0;
    looping = false;
    prepared = false;
    paused  = false;
    stopped = false;
    muted = false;
    reversed = false;
    displayWidth = 0;
    displayHeight = 0;
    videoWidth = 0;
    videoHeight = 0;
    seekRequest = false;
    seekPos = -1;
}

/**
 * 释放资源
 */
void CainPlayer::release() {
    stop();
    if (filename) {
        free(filename);
    }
    if (nativeWindow) {
        ANativeWindow_release(nativeWindow);
    }
    avformat_network_deinit();
    if (ic) {
        avformat_close_input(&ic);
    }
}

/**
 * 指定播放区域
 * @param msec
 */
void CainPlayer::seekTo(int msec) {
    seekPos = msec;
}



/**
 * 设置是否循环播放
 * @param loop
 */
void CainPlayer::setLooping(bool loop) {
    looping = loop;
}

/**
 * 设置是否倒放
 * @param reverse
 */
void CainPlayer::setReverse(bool reverse) {
    reversed = reverse;
}

/**
 * 设置是否播放声音
 * @param play
 */
void CainPlayer::setPlayAudio(bool play) {
    muted = !play;
}

/**
 * 设置播放速度
 * @param playbackRate
 */
void CainPlayer::setPlaybackRate(float playbackRate) {
    this->playbackRate = playbackRate;
}
/**
 * 改变大小
 * @param width
 * @param height
 */
void CainPlayer::changedSize(int width, int height) {
    displayWidth = width;
    displayHeight = height;
}

/**
 * 判断是否实时流
 * @param s
 * @return
 */
bool CainPlayer::isRealtime(AVFormatContext *s) {
    if(!strcmp(s->iformat->name, "rtp") || !strcmp(s->iformat->name, "rtsp")
       || !strcmp(s->iformat->name, "sdp")) {
        return true;
    }
    if (s->pb && (!strncmp(s->filename, "rtp:", 4) || !strncmp(s->filename, "udp:", 4))) {
        return true;
    }
    return false;
}

/**
 * 读文件线程
 * @param arg
 * @return
 */
int CainPlayer::readThreadHandle(void *arg) {
    CainPlayer *player = (CainPlayer *)arg;
    return player->demux();
}

/**
 * 音频解码线程
 * @param arg
 * @return
 */
int CainPlayer::audioThreadHandle(void *arg) {
    CainPlayer *player = (CainPlayer *) arg;
    return player->decodeAudio();
}

/**
 * 视频解码线程
 * @param arg
 * @return
 */
int CainPlayer::videoThreadHandle(void *arg) {
    CainPlayer *player = (CainPlayer *) arg;
    return player->decodeVideo();
}

/**
 * 视频刷新线程，用于输出画面
 * @param arg
 * @return
 */
int CainPlayer::videoDisplayThreadHandle(void *arg) {
    CainPlayer *player = (CainPlayer *) arg;
    return player->refreshVideo();
}

/**
 * 打开媒体流
 * @return
 */
int CainPlayer::openStream() {
    // 注册组件
    av_register_all();
    avformat_network_init();

    // 初始化flush数据包
    av_init_packet(&flush_pkt);
    flush_pkt.data = (uint8_t *)&flush_pkt;

    // 初始化裸数据队列
    audioQueue = new PacketQueue();
    audioQueue->setFlushPacket(&flush_pkt);

    videoQueue = new PacketQueue();
    videoQueue->setFlushPacket(&flush_pkt);

    // 初始化帧队列
    audioFrameQueue = new FrameQueue(audioQueue, SAMPLE_QUEUE_SIZE, 1);
    videoFrameQueue = new FrameQueue(videoQueue, VIDEO_PICTURE_QUEUE_SIZE, 1);

    // 初始化时钟
    audioClock = new Clock(&audioQueue->serial);
    videoClock = new Clock(&videoQueue->serial);
    externClock = new Clock(&externClock->serial);

    // 创建刷新画面线程
    if (!videoRefreshThread) {
        videoRefreshThread = ThreadCreate(videoDisplayThreadHandle, this, "Video Refresh Thread");
    }
    // 如果无法创建刷新线程，则不要再继续执行
    if (!videoRefreshThread) {
        ALOGE("Fail to create video refresh thread.");
        closeStream();
        return -1;
    }

    // 创建读线程的条件锁
    readCondition = CondCreate();
    // 判断是否成功创建条件锁，如果创建失败，则不能打开解复用线程
    if (!readCondition) {
        ALOGE("Fail to create Condtion Lock.");
        closeStream();
        return -1;
    }
    // 创建读文件线程
    readThread = ThreadCreate(readThreadHandle, this, "Read File Thread");
    if (!readThread) {
        ALOGE("Fail to create read file thread");
        closeStream();
        return -1;
    }

    return 0;
}

/**
 * 打开媒体流
 * @param streamIndex
 * @return
 */
int CainPlayer::openStream(int streamIndex) {

    AVCodecContext *avctx;
    AVCodec *codec;
    // 判断索引是否正确
    if (streamIndex < 0 || streamIndex >= ic->nb_streams) {
        return -1;
    }

    // 创建解码上下文
    avctx = avcodec_alloc_context3(NULL);
    if (!avctx) {
        return AVERROR(ENOMEM);
    }

    // 复制解码器信息
    int ret = avcodec_parameters_to_context(avctx, ic->streams[streamIndex]->codecpar);
    if (ret < 0) {
        avcodec_free_context(&avctx);
        return ret;
    }
    // 设置罗数据的时钟基准
    av_codec_set_pkt_timebase(avctx, ic->streams[streamIndex]->time_base);
    // 查找解码器
    codec = avcodec_find_encoder(avctx->codec_id);
    // 判断解码器是否存在
    if (!codec) {
        ALOGE("Fail to find Codec: %d", avctx->codec_id);
        ret = AVERROR(EINVAL);
        avcodec_free_context(&avctx);
        return ret;
    }
    // 设置解码器ID
    avctx->codec_id = codec->id;

    // 打开解码器
    if ((ret = avcodec_open2(avctx, codec, NULL)) < 0) {
        ret = AVERROR_OPTION_NOT_FOUND;
        ALOGE("Fail to open codec: %d", codec->id);
        avcodec_free_context(&avctx);
        return ret;
    }
    ic->streams[streamIndex]->discard = AVDISCARD_DEFAULT;

    ret = 0;
    // 根据类型打开解码线程
    switch (avctx->codec_type) {
        case AVMEDIA_TYPE_AUDIO:
            // 设置音频格式
            inSampleFmt = avctx->sample_fmt;
            outSampleFmt = AV_SAMPLE_FMT_S16;
            inSampleRate = avctx->sample_rate;
            outSampleRate = 44100;
            inChannelLayout = avctx->channel_layout;
            outChannelLayout = AV_CH_LAYOUT_STEREO;
            // 创建音频重采样上下文
            swr_ctx = swr_alloc();
            swr_alloc_set_opts(swr_ctx, outChannelLayout, outSampleFmt, outSampleRate,
                               inChannelLayout, inSampleFmt, inSampleRate, 0, NULL);
            swr_init(swr_ctx);
            channels = av_get_channel_layout_nb_channels(outChannelLayout);

            // TODO 打开音频设备

            // 获取音频流
            audioStream = ic->streams[streamIndex];
            // 音频解码器初始化
            audioDecoder = new Decoder(avctx, audioQueue, readCondition);
            audioDecoder->setFlushPacket(&flush_pkt);
            // 设置解码器开始的时钟基准(timebase)
            if ((ic->iformat->flags & (AVFMT_NOBINSEARCH | AVFMT_NOGENSEARCH | AVFMT_NO_BYTE_SEEK))
                && !ic->iformat->read_seek) {
                audioDecoder->start_pts = audioStream->start_time;
                audioDecoder->start_pts_tb = audioStream->time_base;
            }
            // 创建并开始音频解码线程
            audioDecoder->decoder_tid = ThreadCreate(audioThreadHandle, this, "Audio Decode Thread");
            break;

        case AVMEDIA_TYPE_VIDEO:
            // 获取视频流
            videoStream = ic->streams[streamIndex];
            // 视频解码器初始化
            videoDecoder = new Decoder(avctx, videoQueue, readCondition);
            videoDecoder->setFlushPacket(&flush_pkt);
            // 创建并开始视频解码线程
            videoDecoder->decoder_tid = ThreadCreate(videoThreadHandle, this, "Video Decode Thread");
            queue_attachments_req = true;
            break;

        default:
            break;
    }
    return ret;
}

/**
 * 关闭媒体流
 * @return
 */
int CainPlayer::closeStream() {
    // 停止解码
    stop();

    // 等待读文件线程退出
    ThreadWait(readThread, NULL);
    free(readThread);
    readThread = NULL;

    // 销毁读文件条件锁
    CondDestroy(readCondition);
    readCondition = NULL;

    // 关闭音频流以及退出音频解码线程
    if (audioStreamIdx >= 0) {
        closeStream(audioStreamIdx);
    }

    // 关闭视频流以及退出视频解码线程
    if (videoStreamIdx >= 0) {
        closeStream(videoStreamIdx);
    }

    // 关闭封装格式上下文
    avformat_close_input(&ic);

    // 等待刷新线程退出
    ThreadWait(videoRefreshThread, NULL);
    free(videoRefreshThread);
    videoRefreshThread = NULL;

    // 销毁队列
    delete audioQueue;
    audioQueue = NULL;
    delete audioFrameQueue;
    audioFrameQueue = NULL;
    delete videoQueue;
    videoQueue = NULL;
    delete videoFrameQueue;
    videoFrameQueue = NULL;

    // 销毁时钟
    delete audioClock;
    audioClock = NULL;
    delete videoClock;
    videoClock = NULL;
    delete externClock;
    externClock = NULL;

    // 销毁转码上下文
    if (swr_ctx) {
        swr_close(swr_ctx);
        swr_free(&swr_ctx);
        swr_ctx = NULL;
    }

    // 关闭网络
    avformat_network_deinit();

    // 释放文件名
    if (filename) {
        free(filename);
    }

    return 0;
}

/**
 * 关闭媒体流
 * @param streamIndex
 * @return
 */
int CainPlayer::closeStream(int streamIndex) {
    AVCodecParameters *codecpar;
    if (streamIndex < 0 || streamIndex >= ic->nb_streams) {
        return -1;
    }
    codecpar = ic->streams[streamIndex]->codecpar;

    // 根据类型销毁不同的解码器、释放线程等
    switch (codecpar->codec_type) {
        // 音频流
        case AVMEDIA_TYPE_AUDIO:
            // 解码器停止解码
            audioDecoder->abort(audioFrameQueue);
            delete audioDecoder;
            // TODO 关闭音频设备
            // 释放音频重采样上下文
            swr_free(&swr_ctx);
            break;

        // 视频流
        case AVMEDIA_TYPE_VIDEO:
            videoDecoder->abort(videoFrameQueue);
            delete videoDecoder;
            break;

        default:
            break;
    }
    // 销毁码流
    ic->streams[streamIndex]->discard = AVDISCARD_ALL;
    switch (codecpar->codec_type) {
        case AVMEDIA_TYPE_AUDIO:
            audioStream = NULL;
            audioStreamIdx = -1;
            break;

        case AVMEDIA_TYPE_VIDEO:
            videoStream = NULL;
            videoStreamIdx = -1;
            break;

        default:
            break;
    }
    return 0;
}

/**
 * 判断是否有足够的裸数据包
 * @param st
 * @param streamIndex
 * @param queue
 * @return
 */
int CainPlayer::hasEnoughPackets(AVStream *st, int streamIndex, PacketQueue *queue) {
    return streamIndex < 0 || queue->isAbort()
           || (st->disposition & AV_DISPOSITION_ATTACHED_PIC)
           || queue->size() > MIN_FRAMES
              && (!queue->getDuration() || av_q2d(st->time_base) * queue->getDuration() > 1.0);
}

/**
 * 解复用
 */
int CainPlayer::demux() {
    int ret = 0;
    // 裸数据
    AVPacket packet, *pkt = &packet;
    int64_t stream_start_time;
    int pkt_in_play_range = 0;
    int64_t pkt_ts;
    // 创建等待锁
    Mutex *waitMutex = MutexCreate();
    if (!waitMutex) {
        ALOGE("Fail to create Mutex.");
        return -1;
    }
    // 封装格式上下文
    ic = avformat_alloc_context();
    if (!ic) {
        ALOGE("Could not allocate context.");
        MutexDestroy(waitMutex);
        return -1;
    }

    // 设置中断回调
    ic->interrupt_callback.callback = decode_interrupt_cb;
    ic->interrupt_callback.opaque = this;

    // 打开视频文件
    if (avformat_open_input(&ic, filename, NULL, NULL) < 0) {
        ALOGE("Fail to open file: %s", filename);
        MutexDestroy(waitMutex);
        return -1;
    }

    // 获取视频信息
    if (avformat_find_stream_info(ic, NULL) < 0) {
        ALOGE("Fail to get stream info");
        MutexDestroy(waitMutex);
        return -1;
    }

    // 不使用avio_feof() 测试结尾
    if (ic->pb) {
        ic->pb->eof_reached = 0;
    }

    // 计算一帧的最大显示时长
    maxFrameDuration = (ic->iformat->flags & AVFMT_TS_DISCONT) ? 10.0 : 3600.0;

    // 计算起始位置
    if (start_time != AV_NOPTS_VALUE) {
        int64_t timestamp;
        timestamp = start_time;
        /* add the stream start time */
        if (ic->start_time != AV_NOPTS_VALUE) {
            timestamp += ic->start_time;
        }
        // 定位到实际的开始位置
        ret = avformat_seek_file(ic, -1, INT64_MIN, timestamp, INT64_MAX, 0);
        if (ret < 0) {
            av_log(NULL, AV_LOG_WARNING, "%s: could not seek to position %0.3f\n",
                   filename, (double)timestamp / AV_TIME_BASE);
        }
    }

    // 判断是否实时流
    realtime = isRealtime(ic);

    // 查找音视频索引
    videoStreamIdx = -1;
    audioStreamIdx = -1;
    for (int i = 0; i < ic->nb_streams; ++i) {
        if (ic->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO
            && videoStreamIdx == -1) {
            videoStreamIdx = i;
        } else if (ic->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO
                   && audioStreamIdx == -1) {
            audioStreamIdx = i;
        }
    }

    // 如果音频和视频流都不存在，则退出
    if (audioStreamIdx < 0 && videoStreamIdx < 0) {
        ALOGE("Failed to open file '%s' or configure filetergraph", ic->filename);
        MutexDestroy(waitMutex);
        return -1;
    }

    int audioOpened = -1;
    int videoOpened = -1;
    // 打开音频流
    if (audioStreamIdx >= 0) {
        audioOpened = openStream(audioStreamIdx);
    }
    // 打开视频流
    if (videoStreamIdx >= 0) {
        videoOpened = openStream(videoStreamIdx);
    }

    // 判断如果音频流和视频流都打开失败，则直接退出
    if (audioOpened < 0 && videoOpened < 0) {
        ALOGE("Fail to open audio and video stream.");
        MutexDestroy(waitMutex);
        return -1;
    }

    // 开启缓冲区
    if (infiniteBuffer < 0 && realtime) {
        infiniteBuffer = 1;
    }

    // 解复用阶段，不断地从文件中读出数据
    for (;;) {

        // 如果停止，则退出
        if (isStopped()) {
            break;
        }

        // 判断是否处于暂停状态，av_read_pause/av_read_play不要调用多次
        if (paused != lastPaused) {
            lastPaused = paused;
            if (paused) {
                av_read_pause(ic);
            } else {
                av_read_play(ic);
            }
        }

#if CONFIG_RTSP_DEMUXER || CONFIG_MMSH_PROTOCOL
        if (isStopped() && (!strcmp(ic->iformat->name, "rtsp") ||
                (ic->pb && !strncmp(filename, "mmsh:", 5)))) {
            // 延时10毫秒再继续
            DelayMs(10);
            continue;
        }
#endif

        // 定位请求，则需要定位到具体的位置，然后清空裸数据队列，放入新的裸数据
        if (seekRequest) {
            int64_t seek_target = seekPos;
            int64_t seek_min    = seekRel > 0 ? seek_target - seekRel + 2 : INT64_MIN;
            int64_t seek_max    = seekRel < 0 ? seek_target - seekRel : INT64_MAX;
            // 定位文件
            ret = avformat_seek_file(ic, -1, seek_min, seek_target, seek_max, seekFlags);
            if (ret < 0) {
                av_log(NULL, AV_LOG_ERROR, "%s: error while seeking\n", filename);
            } else {
                // 清空音频裸数据队列
                if (audioStreamIdx >= 0) {
                    audioQueue->flush();
                    audioQueue->put(&flush_pkt);
                }
                // 清空视频裸数据队列
                if (videoStreamIdx >= 0) {
                    videoQueue->flush();
                    videoQueue->put(&flush_pkt);
                }

                // 根据定位的标志设置时钟
                if (seekFlags & AVSEEK_FLAG_BYTE) {
                    externClock->setClock(NAN, 0);
                } else {
                    externClock->setClock(seek_target / (double)AV_TIME_BASE, 0);
                }

            }
            seekRequest = false;
            queue_attachments_req = true;
            eof = false;
        }

        if (queue_attachments_req) {
            if (videoStream && videoStream->disposition  & AV_DISPOSITION_ATTACHED_PIC) {
                AVPacket copy;
                if ((ret = av_copy_packet(&copy, &videoStream->attached_pic)) < 0) {
                    MutexDestroy(waitMutex);
                    return -1;
                }
                videoQueue->put(&copy);
                videoQueue->putNullPacket(videoStreamIdx);
            }
            queue_attachments_req = false;
        }

        // 待解码数据写入队列失败，并且待解码数据还有足够的包时，等待待解码队列的数据消耗掉
        if (infiniteBuffer < 1 &&
            (audioQueue->size() + videoQueue->size() > MAX_QUEUE_SIZE
             || (hasEnoughPackets(audioStream, audioStreamIdx, audioQueue) &&
                 hasEnoughPackets(videoStream, videoStreamIdx, videoQueue)))) {
            // 等待10毫秒再继续
            MutexLock(waitMutex);
            CondWaitTimeout(readCondition, waitMutex, 10);
            MutexUnlock(waitMutex);
            continue;
        }

        if (!paused &&
            (!audioStream || (audioDecoder->finished == audioQueue->serial && audioFrameQueue->nbRemaining() == 0)) &&
            (!videoStream || (videoDecoder->finished == videoQueue->serial && videoFrameQueue->nbRemaining() == 0))) {
            // 如果循环播放，则重新定位到开始位置
            if (looping) {
                streamSeek(start_time != AV_NOPTS_VALUE ? start_time : 0, 0, 0);
            } else if (autoexit) {
                ret = AVERROR_EOF;
                MutexDestroy(waitMutex);
                return ret;
            }
        }
        // 读取数据包
        ret = av_read_frame(ic, pkt);
        // 读取失败时，需要判定是读结束了还是读取出错
        if (ret < 0) {
            // 如果时到了结尾，则入队一个空的裸数据包
            if ((ret == AVERROR_EOF || avio_feof(ic->pb)) && !eof) {
                if (videoStreamIdx >= 0) {
                    videoQueue->putNullPacket(videoStreamIdx);
                }
                if (audioStreamIdx >= 0) {
                    audioQueue->putNullPacket(audioStreamIdx);
                }
                eof = true;
            }
            // 如果是出错，则退出读循环
            if (ic->pb && ic->pb->error) {
                break;
            }
            // 如果都不是，则可能是没有读到数据，等待10毫秒再读
            MutexLock(waitMutex);
            CondWaitTimeout(readCondition, waitMutex, 10);
            MutexUnlock(waitMutex);
            continue;
        } else {    // 如果能够读到数据，则表示没有读到文件结尾，文件结尾标志置为false
            eof = false;
        }

        // 检查裸数据包是否在用户指定的播放范围内，在播放范围内则入队，否则舍弃
        stream_start_time = ic->streams[pkt->stream_index]->start_time;
        pkt_ts = pkt->pts == AV_NOPTS_VALUE ? pkt->dts : pkt->pts;
        pkt_in_play_range = duration == AV_NOPTS_VALUE ||
                            (pkt_ts - (stream_start_time != AV_NOPTS_VALUE ? stream_start_time : 0)) *
                            av_q2d(ic->streams[pkt->stream_index]->time_base) -
                            (double)(start_time != AV_NOPTS_VALUE ? start_time : 0) / 1000000
                            <= ((double)duration / 1000000);
        // 将裸数据包入队或销毁
        if (pkt->stream_index == audioStreamIdx && pkt_in_play_range) {
            audioQueue->put(pkt);
        } else if (pkt->stream_index == videoStreamIdx && pkt_in_play_range) {
            videoQueue->put(pkt);
        } else {
            av_packet_unref(pkt);
        }
    }
    MutexDestroy(waitMutex);
    return ret;
}

/**
 * 查找/定位媒体流
 * @param pos
 * @param rel
 * @param seek_by_bytes
 */
void CainPlayer::streamSeek(int64_t pos, int64_t rel, int seek_by_bytes) {
    if (!seekRequest) {
        seekPos = pos;
        seekRel = rel;
        seekFlags &= ~AVSEEK_FLAG_BYTE;
        if (seek_by_bytes) {
            seekFlags |= AVSEEK_FLAG_BYTE;
        }
        seekRequest = true;
        CondSignal(readCondition);
    }
}

/**
 * 解码视频
 */
int CainPlayer::decodeVideo() {
    AVFrame *frame = av_frame_alloc();
    double pts;
    double duration;
    int ret;
    // 获取时钟基准
    AVRational tb = videoStream->time_base;
    // 猜测视频帧率
    AVRational frameRate = av_guess_frame_rate(ic, videoStream, NULL);
    if (!frame) {
        return AVERROR(ENOMEM);
    }

    // 解码循环
    for (;;) {
        // 获取解码视频帧
        ret = getVideoFrame(frame);
        // 解码出错，直接退出循环
        if (ret < 0) {
            break;
        }
        // 没有得到解码数据，则继续下一轮查询
        if (!ret) {
            continue;
        }
        // 计算帧的pts、duration等
        duration = (frameRate.num && frameRate.den
                    ? av_q2d((AVRational){frameRate.den, frameRate.num}) : 0);
        pts = (frame->pts == AV_NOPTS_VALUE) ? NAN : frame->pts * av_q2d(tb);

        // 获取已解码队列中的可写入的位置
        Frame *vp;
        if (!(vp = videoFrameQueue->peekWritable())) {
            break;
        }
        // 设置相应的参数
        vp->sar = frame->sample_aspect_ratio;
        vp->uploaded = 0;
        vp->width = frame->width;
        vp->height = frame->height;
        vp->format = frame->format;
        vp->pts = pts;
        vp->duration = duration;
        vp->pos = av_frame_get_pkt_pos(frame);
        vp->serial = videoDecoder->pkt_serial;
        // 复制数据
        av_frame_move_ref(vp->frame, frame);
        // 入队
        videoFrameQueue->push();
        av_frame_unref(frame);

    }
    // 释放视频帧局部变量
    av_frame_free(&frame);
    return 0;
}

/**
 * 获取解码视频帧
 * @param frame
 * @return
 */
int CainPlayer::getVideoFrame(AVFrame *frame) {
    int got_picture;
    // 解码视频帧
    if ((got_picture = videoDecoder->decodeFrame(frame, NULL)) < 0) {
        return -1;
    }
    // 判断是否成功解码出一帧数据
    if (got_picture) {
        double dpts = NAN;
        if (frame->pts != AV_NOPTS_VALUE) {
            dpts = av_q2d(videoStream->time_base) * frame->pts;
        }
        frame->sample_aspect_ratio = av_guess_sample_aspect_ratio(ic, videoStream, frame);

        // 是否需要执行丢帧操作
        if (framedrop > 0 || (framedrop && getMasterSyncType() != AV_SYNC_VIDEO_MASTER)) {
            if (frame->pts != AV_NOPTS_VALUE) {
                double diff = dpts - getMasterSyncType();
                if (!isnan(diff) && fabs(diff) < AV_NOSYNC_THRESHOLD &&
                    diff < 0 &&
                    videoDecoder->pkt_serial == videoClock->serial &&
                    videoQueue->size()) {
                    av_frame_unref(frame);
                    got_picture = 0;
                }
            }
        }

    }

    return got_picture;
}

/**
 * 解码音频
 */
int CainPlayer::decodeAudio() {
    AVFrame *frame = av_frame_alloc();
    Frame *af;
    int got_frame = 0;
    AVRational tb;
    int ret = 0;

    if (!frame) {
        return AVERROR(ENOMEM);
    }

    do {
        if ((got_frame = audioDecoder->decodeFrame(frame, NULL)) < 0) {
            goto the_end;
        }
        if (got_frame) {
            tb = (AVRational){1, frame->sample_rate};

            // 检查是否帧队列是否可写入，如果不可写入，则直接释放
            if (!(af = audioFrameQueue->peekWritable())) {
                goto the_end;
            }
            // 设定帧的pts
            af->pts = (frame->pts == AV_NOPTS_VALUE) ? NAN : frame->pts * av_q2d(tb);
            af->pos = av_frame_get_pkt_pos(frame);
            af->serial = audioDecoder->pkt_serial;
            af->duration = av_q2d((AVRational){frame->nb_samples, frame->sample_rate});
            // 将解码后的音频帧压入解码后的音频队列
            av_frame_move_ref(af->frame, frame);
            audioFrameQueue->push();
        }
    } while (ret >= 0 || ret == AVERROR(EAGAIN) || ret == AVERROR_EOF);

the_end:
    // 释放
    av_frame_free(&frame);
    return ret;
}

/**
 * 刷新画面
 * @return
 */
int CainPlayer::refreshVideo() {
    double remaining_time = 0.0;
    while (!isStopped()) {
        // 判断如果还有剩余时间，则睡眠剩余的时间，然后再刷新视频画面
        if (remaining_time > 0.0) {
            av_usleep((unsigned int)(int64_t)(remaining_time * 1000000.0));
        }
        remaining_time = REFRESH_RATE;
        // 如果不处于停止状态或者处于强制刷新阶段，则进入刷新画面流程
        if (!isStopped() || forceRefresh) {
            refreshVideo(&remaining_time);
        }
    }
    return 0;
}

/**
 * 刷新画面
 * @param remaining_time 剩余时间
 */
void CainPlayer::refreshVideo(double *remaining_time) {
    double time;

    // 主同步类型是外部时钟同步，并且是实时码流，则检查外部时钟速度
    if (!paused && getMasterSyncType() == AV_SYNC_EXTERNAL_CLOCK && realtime) {
        checkExternalClockSpeed();
    }

    // 判断如果视频流存在，这里需要计算延时等操作
    if (videoStream) {
retry:
        if (videoFrameQueue->nbRemaining() == 0) {
            // 如果视频帧队列是空的，则什么都不做
        } else {
            double last_duration, duration, delay;
            Frame *vp, *lastvp;

            // 从视频帧队列中取出数据
            lastvp = videoFrameQueue->peekLast();
            vp = videoFrameQueue->peek();

            // 视频队列
            if (vp->serial != videoQueue->serial) {
                videoFrameQueue->next();
                goto retry;
            }

            if (lastvp->serial != vp->serial) {
                frame_timer = av_gettime_relative() / 1000000.0;
            }

            // 如果处于停止状态，直接显示
            if (paused) {
                goto display;
            }

            // 计算上一个时长
            last_duration = getLastDisplayDuration(lastvp, vp);
            // 计算目标延时
            delay = calculateTargetDelay(last_duration);
            // 获取时间
            time= av_gettime_relative()/1000000.0;
            // 如果时间小于帧时间加延时，则获取剩余时间，继续显示当前的帧
            if (time < frame_timer + delay) {
                // 获取剩余时间
                *remaining_time = FFMIN(frame_timer + delay - time, *remaining_time);
                goto display;
            }
            // 计算帧的计时器
            frame_timer += delay;
            // 判断当前的时间是否大于同步阈值，如果大于，则使用当前的时间作为帧的计时器
            if (delay > 0 && time - frame_timer > AV_SYNC_THRESHOLD_MAX) {
                frame_timer = time;
            }

            // 更新显示时间戳
            videoFrameQueue->lock();
            if (!isnan(vp->pts)) {
                updateVideoPts(vp->pts, vp->pos, vp->serial);
            }
            videoFrameQueue->unlock();

            // 判断是否还有剩余的帧
            if (videoFrameQueue->nbRemaining() > 1) {
                // 取得下一帧
                Frame *nextvp = videoFrameQueue->peekNext();
                // 取得下一帧的时长
                duration = getLastDisplayDuration(vp, nextvp);
                // 判断是否需要丢弃一部分帧
                if(!step
                   && (framedrop > 0 || (framedrop && getMasterSyncType() != AV_SYNC_VIDEO_MASTER))
                   && time > frame_timer + duration) {
                    videoFrameQueue->next();
                    goto retry;
                }
            }

            // 强制刷新
            forceRefresh = true;
        }

display:
        if (forceRefresh) {
            videoDisplay();
        }
    }

    forceRefresh = false;
}

/**
 * TODO 显示视频画面，输出到屏幕上
 */
void CainPlayer::videoDisplay() {

}

/**
 * 获取上一帧的显示时长
 * @param vp
 * @param nextvp
 * @return
 */
double CainPlayer::getLastDisplayDuration(Frame *vp, Frame *nextvp) {
    // 如果当前帧的序列等于下一帧的序列，则需要计算显示时长
    if (vp->serial == nextvp->serial) {
        double duration = nextvp->pts - vp->pts;
        // 判断时长是否在合理范围内
        if (isnan(duration) || duration <= 0 || duration > maxFrameDuration) {
            return vp->duration;
        } else {
            return duration;
        }
    } else {
        return 0.0;
    }
}

/**
 * 计算目标延时
 * @param delay
 * @return
 */
double CainPlayer::calculateTargetDelay(double delay) {
    double sync_threshold, diff = 0;

    // 如果不是以视频为同步基准，则需要计算延时
    if (getMasterSyncType() != AV_SYNC_VIDEO_MASTER) {
        // 计算时间差
        diff = videoClock->getClock() - getMasterClock();
        // 计算同步阈值
        sync_threshold = FFMAX(AV_SYNC_THRESHOLD_MIN, FFMIN(AV_SYNC_THRESHOLD_MAX, delay));
        // 计算合理的延时值
        if (!isnan(diff) && fabs(diff) < maxFrameDuration) {
            // 滞后
            if (diff <= -sync_threshold) {
                delay = FFMAX(0, delay + diff);
            } else if (diff >= sync_threshold && delay > AV_SYNC_FRAMEDUP_THRESHOLD) { // 超前
                delay = delay + diff;
            } else if (diff >= sync_threshold) { // 超过了理论阈值
                delay = 2 * delay;
            }
        }
    }
    return delay;
}

/**
 * 更新视频时钟
 * @param pts
 * @param pos
 * @param serial
 */
void CainPlayer::updateVideoPts(double pts, int64_t pos, int serial) {
    videoClock->setClock(pts, serial);
    externClock->syncToSlave(videoClock);
}

/**
 * 获取主同步类型
 * @return
 */
int CainPlayer::getMasterSyncType(void) {
    if (syncType == AV_SYNC_VIDEO_MASTER) {
        if (videoStream) {
            return AV_SYNC_VIDEO_MASTER;
        } else {
            return AV_SYNC_AUDIO_MASTER;
        }
    } else if (syncType == AV_SYNC_AUDIO_MASTER) {
        if (audioStream)
            return AV_SYNC_AUDIO_MASTER;
        else
            return AV_SYNC_EXTERNAL_CLOCK;
    } else {
        return AV_SYNC_EXTERNAL_CLOCK;
    }
}

/**
 * 获取主时钟
 * @return
 */
double CainPlayer::getMasterClock(void) {
    double val;

    switch (getMasterSyncType()) {
        case AV_SYNC_VIDEO_MASTER:
            val = videoClock->getClock();
            break;
        case AV_SYNC_AUDIO_MASTER:
            val = audioClock->getClock();
            break;
        default:
            val = externClock->getClock();
            break;
    }

    return val;
}

/**
 * 检查外部时钟速度
 */
void CainPlayer::checkExternalClockSpeed(void) {
    if (videoStreamIdx >= 0 && videoQueue->size() <= EXTERNAL_CLOCK_MIN_FRAMES ||
            audioStreamIdx>= 0 && audioQueue->size() <= EXTERNAL_CLOCK_MIN_FRAMES) {
        externClock->setSpeed(FFMAX(EXTERNAL_CLOCK_SPEED_MIN,
                                    externClock->speed - EXTERNAL_CLOCK_SPEED_STEP));
    } else if ((videoStreamIdx < 0 || videoQueue->size() > EXTERNAL_CLOCK_MAX_FRAMES) &&
            (audioStreamIdx < 0 || audioQueue->size() > EXTERNAL_CLOCK_MAX_FRAMES)) {
        externClock->setSpeed(FFMIN(EXTERNAL_CLOCK_SPEED_MAX,
                                    externClock->speed + EXTERNAL_CLOCK_SPEED_STEP));
    } else {
        double speed = externClock->speed;
        if (speed != 1.0) {
            externClock->setSpeed(
                    speed + EXTERNAL_CLOCK_SPEED_STEP * (1.0 - speed) / fabs(1.0 - speed));
        }
    }
}

