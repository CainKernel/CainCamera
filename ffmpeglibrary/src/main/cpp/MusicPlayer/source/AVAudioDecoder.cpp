//
// Created by cain on 2018/11/25.
//

#include "AVAudioDecoder.h"

AVAudioDecoder::AVAudioDecoder() {
    this->mCallback = NULL;
    this->playerStatus = NULL;
    this->sampleRate = 0;
    buffer = NULL;
    audioQueue = new AudioQueue();

    soundTouch = new soundtouch::SoundTouch();
    soundTouch->setChannels(2);
    soundTouch->setPitch(pitch);
    soundTouch->setRate(speed);
    soundTouch->setTempo(tempo);

    mPacket = av_packet_alloc();
    mFrame = av_frame_alloc();
    swr_ctx = NULL;
}

AVAudioDecoder::~AVAudioDecoder() {
    if (mPacket != NULL) {
        av_packet_free(&mPacket);
        av_free(mPacket);
        mPacket = NULL;
    }

    if (mFrame != NULL) {
        av_frame_free(&mFrame);
        av_free(mFrame);
        mFrame = NULL;
    }

    if (swr_ctx != NULL) {
        swr_free(&swr_ctx);
        swr_ctx = NULL;
    }
}

/**
 * 设置播放器状态对象
 * @param playerStatus
 */
void AVAudioDecoder::setPlayerStatus(PlayerStatus *playerStatus) {
    this->playerStatus = playerStatus;
}

/**
 * 设置采样率
 * @param sampleRate
 */
void AVAudioDecoder::setSampleRate(int sampleRate) {
    this->sampleRate = sampleRate;
    buffer = (uint8_t *) av_malloc(sampleRate * 4);
    sampleBuffer = static_cast<soundtouch::SAMPLETYPE *>(malloc(sampleRate * 4));
    if (soundTouch != NULL) {
        soundTouch->setSampleRate(sampleRate);
    }
}

/**
 * 设置播放器回调
 * @param playerCallback
 */
void AVAudioDecoder::setPlayerCallback(PlayerCallback *playerCallback) {
    this->mCallback = playerCallback;
}

/**
 * 解码线程执行
 * @param data
 * @return
 */
static void* decodeThreadRun(void *data) {
    AVAudioDecoder * decoder = (AVAudioDecoder *) data;
    decoder->initOpenSLES();
    return NULL;
}

/**
 * 开始
 */
void AVAudioDecoder::start() {
    pthread_create(&mInitThread, NULL, decodeThreadRun, this);
}

/**
 * 音频解码
 * @param pcmbuf
 * @return
 */
int AVAudioDecoder::decodeAudio(void **pcmbuf) {
    int dataSize = 0;
    while (playerStatus != NULL && !playerStatus->isExit()) {
        // 定位或者不处于播放状态，则继续下一轮循环
        if (playerStatus->isSeek() || !playerStatus->isPlaying()) {
            continue;
        }
        // 取出裸数据包
        if (audioQueue->getPacketSize() == 0) {
            continue;
        }
        if (mPacket == NULL) {
            mPacket = av_packet_alloc();
        }
        if (audioQueue->getPacket(mPacket) != 0) {
            av_packet_unref(mPacket);
            continue;
        }
        // 音频解码
        int ret = 0;
        ret = avcodec_send_packet(pCodecContext, mPacket);
        if (ret != 0) {
            av_packet_unref(mPacket);
            continue;
        }
        if (mFrame == NULL) {
            mFrame = av_frame_alloc();
        }
        ret = avcodec_receive_frame(pCodecContext, mFrame);
        if (ret == 0) {
            if (mFrame->channels && mFrame->channel_layout == 0) {
                mFrame->channel_layout = av_get_default_channel_layout(mFrame->channels);
            } else if (mFrame->channels == 0 && mFrame->channel_layout > 0) {
                mFrame->channels = av_get_channel_layout_nb_channels(mFrame->channel_layout);
            }

            // 初始化重采样上下文
            if (swr_ctx == NULL) {
                swr_ctx = swr_alloc_set_opts(
                        NULL,
                        AV_CH_LAYOUT_STEREO,
                        AV_SAMPLE_FMT_S16,
                        mFrame->sample_rate,
                        mFrame->channel_layout,
                        (AVSampleFormat) mFrame->format,
                        mFrame->sample_rate,
                        NULL, NULL
                );

                if (!swr_ctx || swr_init(swr_ctx) < 0) {
                    av_packet_unref(mPacket);
                    av_frame_unref(mFrame);
                    swr_free(&swr_ctx);
                    continue;
                }
            }
            // 音频重采样
            resampleSize = swr_convert(
                    swr_ctx,
                    &buffer,
                    mFrame->nb_samples,
                    (const uint8_t **) mFrame->data,
                    mFrame->nb_samples);
            // 计算出采样大小
            int out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
            dataSize = resampleSize * out_channels * av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);
            // 计算当前时间
            currentTime = mFrame->pts * av_q2d(timeBase);
            if (currentTime < clock) {
                currentTime = clock;
            }
            clock = currentTime;
            *pcmbuf = buffer;
            av_packet_unref(mPacket);
            av_frame_unref(mFrame);
            break;
        } else {
            av_packet_unref(mPacket);
            av_frame_unref(mFrame);
            continue;
        }
    }
    return dataSize;
}

/**
 * 转码PCM数据实现变速变调
 * @return
 */
int AVAudioDecoder::translatePCM() {
    int dataSize;
    while (playerStatus != NULL && !playerStatus->isExit()) {
        out_buffer = NULL;
        if (finished) {
            finished = false;
            // 解码音频PCM数据
            dataSize = decodeAudio(reinterpret_cast<void **>(&out_buffer));
            if (dataSize > 0) {
                // 采样率数据拼接成16位再做处理，这么做的原因是消除变速之后出现的噪音
                for (int i = 0; i < dataSize / 2 + 1; i++) {
                    sampleBuffer[i] = (out_buffer[i * 2] | ((out_buffer[i * 2 + 1]) << 8));
                }
                soundTouch->putSamples(sampleBuffer, resampleSize);
                num = soundTouch->receiveSamples(sampleBuffer, dataSize / 4);
            } else {
                soundTouch->flush();
            }
        }
        if (num == 0) {
            finished = true;
            continue;
        } else {
            if (out_buffer == NULL) {
                num = soundTouch->receiveSamples(sampleBuffer, dataSize / 4);
                if (num == 0) {
                    finished = true;
                    continue;
                }
            }
            return num;
        }
    }
    return 0;
}

/**
 * 将经过转换后的PCM数据放入队列中执行
 */
void AVAudioDecoder::enqueuePCMD() {
    // 获取经过变速变调处理后的PCM数据
    int bufferSize = translatePCM();
    if (bufferSize > 0) {
        // 重新计算时钟
        clock += bufferSize / ((double)(sampleRate * 4));
        if (clock - lastTime >= 0.1) {
            lastTime = clock;
            int current = (int) clock % duration;
            if (mCallback != NULL) {
                mCallback->onCurrentInfo(current, duration);
            }
        }
        if (mCallback != NULL) {
            mCallback->onVolumeDB(calculateVolumeDB(reinterpret_cast<char *>(sampleBuffer), bufferSize * 4));
        }
        (*pcmBufferQueue)->Enqueue(pcmBufferQueue, (char *) sampleBuffer, bufferSize * 4);
    }
}

/**
 * PCM缓冲回调
 * @param bf
 * @param context
 */
static void pcmBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void * context) {
    AVAudioDecoder *decoder = (AVAudioDecoder *) context;
    if (decoder != NULL) {
        decoder->enqueuePCMD();
    }
}

/**
 * 初始化OpenGLES
 */
void AVAudioDecoder::initOpenSLES() {
    SLresult result;
    // 创建OpenSLES引擎
    result = slCreateEngine(&engineObject, 0, 0, 0, 0, 0);
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    // 创建混音器
    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, mids, mreq);
    (void)result;
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    (void)result;
    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB, &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) {
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings);
        (void)result;
    }
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&outputMix, 0};


    // 配置PCM格式信息
    SLDataLocator_AndroidSimpleBufferQueue android_queue={SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};

    SLDataFormat_PCM pcm={
            SL_DATAFORMAT_PCM,//播放pcm格式的数据
            2,//2个声道（立体声）
            (SLuint32)getOpenSLESSampleRate(sampleRate),//44100hz的频率
            SL_PCMSAMPLEFORMAT_FIXED_16,//位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,//和位数一致就行
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,//立体声（前左前右）
            SL_BYTEORDER_LITTLEENDIAN//结束标志
    };
    SLDataSource slDataSource = {&android_queue, &pcm};


    const SLInterfaceID ids[4] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_PLAYBACKRATE, SL_IID_MUTESOLO};
    const SLboolean req[4] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    (*engineEngine)->CreateAudioPlayer(engineEngine, &pcmPlayerObject, &slDataSource, &audioSnk, 4, ids, req);
    // 初始化播放器
    (*pcmPlayerObject)->Realize(pcmPlayerObject, SL_BOOLEAN_FALSE);

    // 得到接口后调用  获取Player接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_PLAY, &pcmPlayerPlay);
    // 获取声音接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_VOLUME, &pcmVolumePlay);
    // 获取声道接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_MUTESOLO, &pcmMutePlay);

    // 注册回调缓冲区 获取缓冲队列接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_BUFFERQUEUE, &pcmBufferQueue);
    setVolume(volumePercent);
    setChannelType(channelType);
    // 缓冲接口回调
    (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue, pcmBufferCallBack, this);
    // 切换成播放状态
    (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    // 入队
    enqueuePCMD();

    // 退出初始化线程
    pthread_exit(&mInitThread);
}

int AVAudioDecoder::getOpenSLESSampleRate(int sample_rate) {
    int rate = 0;
    switch (sample_rate) {
        case 8000:
            rate = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            rate = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            rate = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            rate = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            rate = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            rate = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            rate = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            rate = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            rate = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            rate = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            rate = SL_SAMPLINGRATE_88_2;
            break;
        case 96000:
            rate = SL_SAMPLINGRATE_96;
            break;
        case 192000:
            rate = SL_SAMPLINGRATE_192;
            break;
        default:
            rate =  SL_SAMPLINGRATE_44_1;
    }
    return rate;
}

void AVAudioDecoder::pause() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay,  SL_PLAYSTATE_PAUSED);
    }
}

void AVAudioDecoder::resume() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay,  SL_PLAYSTATE_PLAYING);
    }
}

void AVAudioDecoder::stop() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay,  SL_PLAYSTATE_STOPPED);
    }
}

void AVAudioDecoder::release() {
    if (audioQueue != NULL) {
        audioQueue->clear();
        delete(audioQueue);
        audioQueue = NULL;
    }

    if (pcmPlayerObject != NULL) {
        (*pcmPlayerObject)->Destroy(pcmPlayerObject);
        pcmPlayerObject = NULL;
        pcmPlayerPlay = NULL;
        pcmBufferQueue = NULL;
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

    if (buffer != NULL) {
        free(buffer);
        buffer = NULL;
    }

    if (pCodecContext != NULL) {
        avcodec_close(pCodecContext);
        avcodec_free_context(&pCodecContext);
        pCodecContext = NULL;
    }

    if (playerStatus != NULL) {
        playerStatus = NULL;
    }
    if (mCallback != NULL) {
        mCallback = NULL;
    }
}

void AVAudioDecoder::setVolume(int percent) {
    volumePercent = percent;
    if (pcmVolumePlay != NULL) {
        if (percent > 30) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -20));
        } else if (percent > 25) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -22));
        } else if (percent > 20) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -25));
        } else if (percent > 15) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -28));
        } else if (percent > 10) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -30));
        } else if (percent > 5) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -34));
        } else if (percent > 3) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -37));
        } else if (percent > 0) {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -40));
        } else {
            (*pcmVolumePlay)->SetVolumeLevel(pcmVolumePlay, (SLmillibel)((100 - percent) * -100));
        }
    }
}

void AVAudioDecoder::setChannelType(int channel) {
    this->channelType = channel;
    if (pcmMutePlay != NULL) {
        if (channel == 0) { // 右声道
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, true);
        } else if (channel == 1) { // 左声道
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, true);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, false);
        } else if(channel == 2) { // 立体声
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, false);
        }
    }
}

void AVAudioDecoder::setPitch(float pitch) {
    this->pitch = pitch;
    if (soundTouch != NULL) {
        soundTouch->setPitch(pitch);
    }
}

void AVAudioDecoder::setSpeed(float speed) {
    this->speed = speed;
    if (soundTouch != NULL) {
        soundTouch->setRate(speed);
    }
}

void AVAudioDecoder::setTempo(float tempo) {
    this->tempo = tempo;
    if (soundTouch != NULL) {
        soundTouch->setTempo(tempo);
    }
}

void AVAudioDecoder::setSpeedChange(double speedChange) {
    if (soundTouch != NULL) {
        soundTouch->setRateChange(speedChange);
    }
}

void AVAudioDecoder::setTempoChange(double tempoChange) {
    if (soundTouch != NULL) {
        soundTouch->setTempoChange(tempoChange);
    }
}

void AVAudioDecoder::setPitchOctaves(double pitchOctaves) {
    if (soundTouch != NULL) {
        soundTouch->setPitchOctaves(pitchOctaves);
    }
}

void AVAudioDecoder::setPitchSemiTones(double semiTones) {
    if (soundTouch != NULL) {
        soundTouch->setPitchSemiTones(semiTones);
    }
}

int AVAudioDecoder::calculateVolumeDB(char *pcmcata, size_t pcmsize) {
    int db = 0;
    short int perValue = 0;
    double sum = 0;
    for (int i = 0; i < pcmsize; i+= 2) {
        memcpy(&perValue, pcmcata + i, 2);
        sum += abs(perValue);
    }
    sum = sum / (pcmsize / 2);
    if (sum > 0) {
        db = (int)(20.0 * log10(sum));
    }
    return db;
}

int AVAudioDecoder::getStreamIndex() const {
    return streamIndex;
}

void AVAudioDecoder::setStreamIndex(int streamIndex) {
    AVAudioDecoder::streamIndex = streamIndex;
}

AVCodecParameters *AVAudioDecoder::getCodecParameters() const {
    return pCodecParameters;
}

void AVAudioDecoder::setCodecParameters(AVCodecParameters *parameters) {
    AVAudioDecoder::pCodecParameters = parameters;
}

void AVAudioDecoder::setTimeBase(const AVRational &timeBase) {
    AVAudioDecoder::timeBase = timeBase;
}

int AVAudioDecoder::getDuration() const {
    return duration;
}

void AVAudioDecoder::setDuration(int duration) {
    AVAudioDecoder::duration = duration;
}

AVCodecContext *AVAudioDecoder::getCodecContext() const {
    return pCodecContext;
}

void AVAudioDecoder::setCodecContext(AVCodecContext *avCodecContext) {
    AVAudioDecoder::pCodecContext = avCodecContext;
}

AudioQueue *AVAudioDecoder::getQueue() const {
    return audioQueue;
}

void AVAudioDecoder::setClock(double clock) {
    AVAudioDecoder::clock = clock;
}

void AVAudioDecoder::setLastTime(double lastTime) {
    AVAudioDecoder::lastTime = lastTime;
}