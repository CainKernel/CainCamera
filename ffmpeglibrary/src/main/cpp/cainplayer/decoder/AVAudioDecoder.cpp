//
// Created by admin on 2018/4/29.
//

#include "AVAudioDecoder.h"

AVAudioDecoder::AVAudioDecoder(MediaStatus *status, MediaJniCall *jniCall)
        : AVDecoder(status, jniCall) {
    streamIndex = -1;
    ret = 0;
    dst_layout = 0;
    dst_nb_samples = 0;
    out_buffer = (uint8_t *) malloc(sampleRate * 2 * 2 * 2 / 3);
    out_channels = 0;
    data_size = 0;
    dst_format = AV_SAMPLE_FMT_S16;

    // 为裸数据包对象开启内存空间
    mPacket = av_packet_alloc();
    // 为帧对象开辟内存空间
    mFrame = av_frame_alloc();
    swr_ctx = NULL;

    buffer = NULL;
    pcmSize = 0;
    sampleRate = 44100;
    isExit = false;
    isVideo = false;
    isReadPacketFinish = true;
    engineObject = NULL;
    engineEngine = NULL;
    outputMixObject = NULL;
    outputMixEnvironmentalReverb = NULL;
    reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
    pcmPlayerObject = NULL;
    pcmPlayerPlay = NULL;
    pcmPlayerVolume = NULL;
    pcmBufferQueue = NULL;
}

AVAudioDecoder::~AVAudioDecoder() {
    // 释放AVPacket内存空间
    if (mPacket != NULL) {
        av_packet_free(&mPacket);
        av_free(mPacket);
        mPacket = NULL;
    }
    // 释放AVFrame
    if (mFrame != NULL) {
        av_frame_free(&mFrame);
        av_free(mFrame);
        mFrame = NULL;
    }
    // 释放重采样上下文
    if (swr_ctx != NULL) {
        swr_free(&swr_ctx);
        swr_ctx = NULL;
    }
}

/**
 * 释放资源
 */
void AVAudioDecoder::release() {

    pause();

    if (queue != NULL) {
        queue->notify();
    }

    int count = 0;
    while (!isExit) {
        if (count > 1000) {
            isExit = true;
        }
        count++;
        av_usleep(1000 * 10);
    }

    if (queue != NULL) {
        queue->release();
        delete(queue);
        queue = NULL;
    }

    if (pcmPlayerObject != NULL) {
        (*pcmPlayerObject)->Destroy(pcmPlayerObject);
        pcmPlayerObject = NULL;
        pcmPlayerPlay = NULL;
        pcmPlayerVolume = NULL;
        pcmBufferQueue = NULL;
        buffer = NULL;
        pcmSize = 0;
    }

    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentalReverb = NULL;
    }

    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

    if (out_buffer != NULL) {
        free(out_buffer);
        out_buffer = NULL;
    }

    if (buffer != NULL) {
        free(buffer);
        buffer = NULL;
    }

    if (pCodecContext != NULL) {
        avcodec_close(pCodecContext);
        avcodec_free_context(&pCodecContext);
        pCodecContext = NULL;
    }

    if (mediaStatus != NULL) {
        mediaStatus = NULL;
    }
}

/**
 * 音频播放线程
 * @param context
 * @return
 */
void *AVAudioDecoder::decodeThreadHandle(void *context) {
    AVAudioDecoder *audio = (AVAudioDecoder *) context;
    audio->initOpenSL();
    audio->exitDecodeThread();
    return NULL;
}


void AVAudioDecoder::exitDecodeThread() {
    pthread_exit(&audioThread);
}

void AVAudioDecoder::start() {
    pthread_create(&audioThread, NULL, decodeThreadHandle, this);
}

/**
 * 获取到PCM数据
 * @param pcm 存放解码后的数据
 * @return  得到的PCM数据大小
 */
int AVAudioDecoder::getPcmData(void **pcm) {
    while (!mediaStatus->isExit()) {
        isExit = false;

        // 暂停
        if (mediaStatus->isPause()) {
            av_usleep(1000 * 100);
            continue;
        }

        // 定位
        if (mediaStatus->isSeek()) {
            if (mediaJniCall) {
                mediaJniCall->onLoad(WORKER_THREAD, true);
            }
            mediaStatus->setLoad(true);
            isReadPacketFinish = true;
            continue;
        }

        // 不是视频
        if (!isVideo) {
            if (queue->getPacketSize() == 0) {
                if (!mediaStatus->isLoad()) {
                    if (mediaJniCall) {
                        mediaJniCall->onLoad(WORKER_THREAD, true);
                    }
                    mediaStatus->setLoad(true);
                }
                continue;
            } else {
                if (mediaStatus->isLoad()) {
                    if (mediaJniCall) {
                        mediaJniCall->onLoad(WORKER_THREAD, false);
                    }
                    mediaStatus->setLoad(false);
                }
            }
        }

        // 入到裸数据包并发送给ffmpeg进行解码
        if (isReadPacketFinish) {
            isReadPacketFinish = false;
            // 取出裸数据包，如果没能成功，则将裸数据包的数据置为NULL
            if (queue->getPacket(mPacket) != 0) {
                av_packet_unref(mPacket);
                isReadPacketFinish = true;
                continue;
            }

            // 将裸数据包进行解码
            ret = avcodec_send_packet(pCodecContext, mPacket);

            // 如果不成功，则将裸数据包的数据置为NULL
            if (ret < 0 && ret != AVERROR(EAGAIN) && ret != AVERROR_EOF) {
                av_packet_unref(mPacket);
                isReadPacketFinish = true;
                continue;
            }
        }

        // 获取解码后的AVFrame
        if (avcodec_receive_frame(pCodecContext, mFrame) == 0) {
            // 设置通道数或channel_layout
            if (mFrame->channels > 0 && mFrame->channel_layout == 0) {
                mFrame->channel_layout = av_get_default_channel_layout(mFrame->channels);
            } else if (mFrame->channels == 0 && mFrame->channel_layout > 0) {
                mFrame->channels = av_get_channel_layout_nb_channels(mFrame->channel_layout);
            }
            // 重采样为立体声

            dst_layout = AV_CH_LAYOUT_STEREO;

            // 初始化重采样上下文
            if (swr_ctx == NULL) {
                // 设置转换参数
                swr_ctx = swr_alloc_set_opts(NULL, dst_layout, dst_format, mFrame->sample_rate,
                                             mFrame->channel_layout,
                                             (enum AVSampleFormat) mFrame->format,
                                             mFrame->sample_rate, 0, NULL);

                // 初始化重采样上下文
                if (swr_ctx == NULL || (ret = swr_init(swr_ctx)) < 0) {
                    swr_free(&swr_ctx);
                    swr_ctx = NULL;
                    av_packet_unref(mPacket);
                    av_frame_unref(mFrame);
                    continue;
                }
            }

            // 计算转换后的sample个数 a * b / c
            dst_nb_samples = av_rescale_rnd(
                    swr_get_delay(swr_ctx, mFrame->sample_rate) + mFrame->nb_samples,
                    mFrame->sample_rate, mFrame->sample_rate, AV_ROUND_INF);

            // 转换，返回值为转换后的sample个数
            int size = swr_convert(swr_ctx, &out_buffer, dst_nb_samples,
                             (const uint8_t **) mFrame->data, mFrame->nb_samples);

            // 根据布局获取声道数
            out_channels = av_get_channel_layout_nb_channels(dst_layout);
            data_size = out_channels * size * av_get_bytes_per_sample(dst_format);
            current = mFrame->pts * av_q2d(timeBase);
            if (current < clock) {
                current = clock;
            }
            clock = current;
            *pcm = out_buffer;

            av_packet_unref(mPacket);
            av_frame_unref(mFrame);
            break;
        } else {
            isReadPacketFinish = true;
            av_packet_unref(mPacket);
            av_frame_unref(mFrame);
            continue;
        }
    }

    isExit = true;
    return data_size;
}

/**
 * PCM回调
 * @param bf
 * @param context
 */
void AVAudioDecoder::pcmBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void * context) {
    AVAudioDecoder *decoder = (AVAudioDecoder *) context;
    if (decoder != NULL) {
        decoder->pcmCallback(bf);
    }
}

/**
 * PCM回调
 * @param bf
 */
void AVAudioDecoder::pcmCallback(SLAndroidSimpleBufferQueueItf bf) {
    buffer = NULL;
    pcmSize = getPcmData(&buffer);
    if (buffer && pcmSize > 0) {
        clock += pcmSize / ((double)(sampleRate * 2 * 2));
        if (mediaJniCall) {
            mediaJniCall->onTimeInfo(WORKER_THREAD, clock, duration);
        }
        (*pcmBufferQueue)->Enqueue(pcmBufferQueue, buffer, pcmSize);
    }
}

/**
 * 初始化OpenSLES
 * @return
 */
int AVAudioDecoder::initOpenSL() {
    SLresult result;
    result = slCreateEngine(&engineObject, 0, 0, 0, 0, 0);
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, mids, mreq);
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB, &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) {
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings);
        (void)result;
    }
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&outputMix, 0};

    SLDataLocator_AndroidSimpleBufferQueue android_queue={SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};

    SLDataFormat_PCM pcm = {
            SL_DATAFORMAT_PCM,                              // 播放pcm格式的数据
            2,                                              // 2个声道（立体声）
            (SLuint32)getSLSampleRate(),                    // 44100hz的频率
            SL_PCMSAMPLEFORMAT_FIXED_16,                    // 位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,                    // 和位数一致就行
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT, // 立体声（前左前右）
            SL_BYTEORDER_LITTLEENDIAN                       // 结束标志
    };
    SLDataSource slDataSource = {&android_queue, &pcm};

    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &pcmPlayerObject, &slDataSource, &audioSnk, 3, ids, req);
    (*pcmPlayerObject)->Realize(pcmPlayerObject, SL_BOOLEAN_FALSE);
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_PLAY, &pcmPlayerPlay);
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_BUFFERQUEUE, &pcmBufferQueue);
    (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue, pcmBufferCallBack, this);
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_VOLUME, &pcmPlayerVolume);
    (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    pcmBufferCallBack(pcmBufferQueue, this);
    return 0;
}

int AVAudioDecoder::getSLSampleRate() {
    switch (sampleRate) {
        case 8000:
            return SL_SAMPLINGRATE_8;
        case 11025:
            return SL_SAMPLINGRATE_11_025;
        case 12000:
            return SL_SAMPLINGRATE_12;
        case 16000:
            return SL_SAMPLINGRATE_16;
        case 22050:
            return SL_SAMPLINGRATE_22_05;
        case 24000:
            return SL_SAMPLINGRATE_24;
        case 32000:
            return SL_SAMPLINGRATE_32;
        case 44100:
            return SL_SAMPLINGRATE_44_1;
        case 48000:
            return SL_SAMPLINGRATE_48;
        case 64000:
            return SL_SAMPLINGRATE_64;
        case 88200:
            return SL_SAMPLINGRATE_88_2;
        case 96000:
            return SL_SAMPLINGRATE_96;
        case 192000:
            return SL_SAMPLINGRATE_192;
        default:
            return SL_SAMPLINGRATE_44_1;
    }
}

void AVAudioDecoder::pause() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay,  SL_PLAYSTATE_PAUSED);
    }
}

void AVAudioDecoder::resume() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    }
}

void AVAudioDecoder::setVideo(bool video) {
    isVideo = video;
}

void AVAudioDecoder::setClock(int secds) {
    current = secds;
    clock = secds;
}

void AVAudioDecoder::setSampleRate(int sampleRate) {
    this->sampleRate = sampleRate;
}