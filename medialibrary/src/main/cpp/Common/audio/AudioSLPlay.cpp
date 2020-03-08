//
// Created by CainHuang on 2020-01-14.
//

#include "AudioSLPlay.h"

AudioSLPlay::AudioSLPlay(const std::shared_ptr<AudioProvider> &audioProvider)
              : AudioPlay(audioProvider) {
    LOGD("AudioSLPlay::constructor()");
    reset();
    createEngine();
}

AudioSLPlay::~AudioSLPlay() {
    release();
    LOGD("AudioSLPlay::destructor()");
}

/**
 * SLES缓冲回调
 * @param bf
 * @param context
 */
void slBufferPCMCallBack(SLAndroidSimpleBufferQueueItf bf, void *context) {
    auto player = (AudioSLPlay *) context;
    player->receiveAudioData();
}

/**
 * 打开播放器
 * @param sampleRate
 * @param channels
 * @return
 */
int AudioSLPlay::open(int sampleRate, int channels) {
    LOGD("AudioSLPlay::open()");
    SLresult result;
    // 创建播放器
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, slOutputMixObject};
    SLDataSink audioSink = {&outputMix, nullptr};

    SLDataLocator_AndroidSimpleBufferQueue android_queue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            OPENSLES_BUFFERS
    };

    // 根据通道数设置通道mask
    SLuint32 channelMask;
    switch (channels) {
        case 2: {
            channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
            break;
        }
        case 1: {
            channelMask = SL_SPEAKER_FRONT_CENTER;
            break;
        }
        default: {
            LOGE("%s, invalid channel %d", __func__, channels);
            return -1;
        }
    }
    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM,              // 播放器PCM格式
            (SLuint32)channels,             // 声道数
            getSLSampleRate(sampleRate),    // SL采样率
            SL_PCMSAMPLEFORMAT_FIXED_16,    // 位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,    // 和位数一致
            channelMask,                    // 格式
            SL_BYTEORDER_LITTLEENDIAN       // 小端存储
    };

    SLDataSource slDataSource = {&android_queue, &format_pcm};

    const SLInterfaceID ids[3] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE, SL_IID_VOLUME, SL_IID_PLAY};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    // 创建音频播放器
    result = (*slEngine)->CreateAudioPlayer(slEngine, &slPlayerObject, &slDataSource,
                                            &audioSink, 3, ids, req);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slEngine->CreateAudioPlayer() failed", __func__);
        return -1;
    }

    // 初始化音频播放器
    result = (*slPlayerObject)->Realize(slPlayerObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slPlayerObject->Realize() failed", __func__);
        return -1;
    }

    // 获取播放器对象
    result = (*slPlayerObject)->GetInterface(slPlayerObject, SL_IID_PLAY, &slPlayItf);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slPlayerObject->GetInterface(SL_IID_PLAY) failed", __func__);
        return -1;
    }

    // 获取音量设置对象
    result = (*slPlayerObject)->GetInterface(slPlayerObject, SL_IID_VOLUME, &slVolumeItf);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slPlayerObject->GetInterface(SL_IID_VOLUME) failed", __func__);
        return -1;
    }

    // 获取音频缓冲区队列对象
    result = (*slPlayerObject)->GetInterface(slPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                             &slBufferQueueItf);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slPlayerObject->GetInterface(SL_IID_ANDROIDSIMPLEBUFFERQUEUE) failed", __func__);
        return -1;
    }

    // 注册队列回调处理对象
    result = (*slBufferQueueItf)->RegisterCallback(slBufferQueueItf, slBufferPCMCallBack, this);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slBufferQueueItf->RegisterCallback() failed", __func__);
        return -1;
    }

    // 这里计算缓冲区大小等参数
    int bytesPerFrame  = format_pcm.numChannels * format_pcm.bitsPerSample / 8;     // 一帧占多少字节
    int framePerBuffer = OPENSLES_BUFLEN * format_pcm.samplesPerSec / 1000000;      // 一个缓冲区有多少帧数据
    mMaxBufferSize  = bytesPerFrame * framePerBuffer;                               // 一个缓冲区大小

    LOGI("OpenSL-ES: bytes_per_frame  = %d bytes\n",  bytesPerFrame);
    LOGI("OpenSL-ES: milli_per_buffer = %d ms\n",     OPENSLES_BUFLEN);
    LOGI("OpenSL-ES: frame_per_buffer = %d frames\n", framePerBuffer);
    LOGI("OpenSL-ES: buffer size      = %d bytes\n",  mMaxBufferSize);

    if (mBuffer == nullptr) {
        mBuffer = (short *) malloc((size_t)mMaxBufferSize);
    }
    // 获取采样率
    mSampleRate = format_pcm.samplesPerSec / 1000;

    LOGD("open audio player success!");
    mInited = true;
    return result;
}

/**
 * 开始播放
 */
void AudioSLPlay::start() {
    LOGD("AudioSLPlay::start()");
    if (!mInited) {
        LOGD("AudioSLPlay has not inited!");
        return;
    }
    mAbortRequest = false;
    mPauseRequest = false;
    mCondition.signal();
    if (!mAudioThread) {
        mAudioThread = new Thread(this, Priority_High);
    }
    if (mAudioThread && !mAudioThread->isActive()) {
        mAudioThread->start();
    }
}

/**
 * 停止播放
 */
void AudioSLPlay::stop() {
    LOGD("AudioSLPlay::stop()");
    mAbortRequest = true;
    mCondition.signal();
    if (mAudioThread && mAudioThread->isActive()) {
        mAudioThread->join();
        delete mAudioThread;
        mAudioThread = nullptr;
    }
}

/**
 * 暂停
 */
void AudioSLPlay::pause() {
    LOGD("AudioSLPlay::pause()");
    mPauseRequest = true;
    mCondition.signal();
}

/**
 * 继续播放
 */
void AudioSLPlay::resume() {
    LOGD("AudioSLPlay::resume()");
    mPauseRequest = false;
    mCondition.signal();
}

/**
 * 清空缓冲区
 */
void AudioSLPlay::flush() {
    LOGD("AudioSLPlay::flush()");
    if (slBufferQueueItf != nullptr) {
        (*slBufferQueueItf)->Clear(slBufferQueueItf);
    }
}

/**
 * 设置播放音量
 * @param leftVolume    左音量
 * @param rightVolume   右音量
 */
void AudioSLPlay::setStereoVolume(float leftVolume, float rightVolume) {
    if (slVolumeItf != nullptr) {
        SLmillibel level = getAmplificationLevel((leftVolume + rightVolume) / 2);
        SLresult result = (*slVolumeItf)->SetVolumeLevel(slVolumeItf, level);
        if (result != SL_RESULT_SUCCESS) {
            LOGE("slVolumeItf->SetVolumeLevel failed %d\n", (int)result);
        }
    }
}

void AudioSLPlay::run() {
    audioPlay();
}

void AudioSLPlay::reset() {
    slObject = nullptr;
    slEngine = nullptr;
    slOutputMixObject = nullptr;
    slPlayerObject = nullptr;
    slPlayItf = nullptr;
    slVolumeItf = nullptr;
    slBufferQueueItf = nullptr;
    mAbortRequest = true;
    mPauseRequest = false;
    mAudioThread = nullptr;
    mBuffer = nullptr;
    mInited = false;
}

void AudioSLPlay::release() {
    stop();
    mInited = false;
    if (slPlayerObject != nullptr) {
        (*slPlayerObject)->Destroy(slPlayerObject);
        slPlayerObject = nullptr;
        slPlayItf = nullptr;
        slVolumeItf = nullptr;
        slBufferQueueItf = nullptr;
    }

    if (slOutputMixObject != nullptr) {
        (*slOutputMixObject)->Destroy(slOutputMixObject);
        slOutputMixObject = nullptr;
    }

    if (slObject != nullptr) {
        (*slObject)->Destroy(slObject);
        slObject = nullptr;
        slEngine = nullptr;
    }
    if (mBuffer != nullptr) {
        free(mBuffer);
        mBuffer = nullptr;
    }
}

/**
 * 创建引擎
 */
int AudioSLPlay::createEngine() {
    SLresult result;

    // 创建一个SL引擎对象
    result = slCreateEngine(&slObject, 0, nullptr, 0, nullptr, nullptr);
    if ((result) != SL_RESULT_SUCCESS) {
        LOGE("%s: slCreateEngine() failed", __func__);
        return -1;
    }

    // 初始化SL引擎
    result = (*slObject)->Realize(slObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slObject->Realize() failed", __func__);
        return -1;
    }

    // 获取对象
    result = (*slObject)->GetInterface(slObject, SL_IID_ENGINE, &slEngine);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slObject->GetInterface() failed", __func__);
        return -1;
    }

    // 创建输出mix对象
    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};
    result = (*slEngine)->CreateOutputMix(slEngine, &slOutputMixObject, 1, mids, mreq);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slEngine->CreateOutputMix() failed", __func__);
        return -1;
    }

    // 初始化输出mix对象
    result = (*slOutputMixObject)->Realize(slOutputMixObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("%s: slOutputMixObject->Realize() failed", __func__);
        return -1;
    }

    return result;
}

/**
 * 获取音频数据
 * @return
 */
int AudioSLPlay::receiveAudioData() {
    SLresult slRet;
    int size = 0;

    // 通过回调填充PCM数据
    if (nullptr != mAudioProvider.lock()) {
        size = mAudioProvider.lock()->onAudioProvide(&mBuffer, mMaxBufferSize);
        if (size > mMaxBufferSize) {
            mMaxBufferSize = size;
        }
    }

    // 将缓冲数据放入数据流中
    if (size > 0 && mBuffer != nullptr) {
        (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
        slRet = (*slBufferQueueItf)->Enqueue(slBufferQueueItf, mBuffer, (SLuint32) size);
        if (slRet == SL_RESULT_SUCCESS) {
            // do nothing
        } else if (slRet == SL_RESULT_BUFFER_INSUFFICIENT) {
            // don't retry, just pass through
            LOGE("SL_RESULT_BUFFER_INSUFFICIENT\n");
        } else {
            LOGE("slBufferQueueItf->Enqueue() = %d\n", (int)slRet);
            return -1;
        }
    }
    return 0;
}

/**
 * 音频播放
 */
void AudioSLPlay::audioPlay() {
    int audio = 1;
    SLuint32 state;
    while (true) {
        mMutex.lock();
        if (mAbortRequest) {
            flush();
            if (slPlayItf != nullptr) {
                (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_STOPPED);
            }
            mMutex.unlock();
            break;
        }

        if (mPauseRequest) {
            if (slPlayItf != nullptr) {
                (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PAUSED);
            }
            mCondition.wait(mMutex);
            mMutex.unlock();
            continue;
        }

        // 切换播放状态
        if (slPlayItf != nullptr) {
            (*slPlayItf)->GetPlayState(slPlayItf, &state);
            if (state != SL_PLAYSTATE_PLAYING) {
                (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
                (*slBufferQueueItf)->Enqueue(slBufferQueueItf, &audio, 1);
            } else if (state == SL_PLAYSTATE_PAUSED) {
                (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
            } else if (state == SL_PLAYSTATE_PLAYING) {

            } else {
                LOGE("unknown audio play state");
            }
        }
        // 等待50毫秒查询下一轮数据
        mCondition.waitRelative(mMutex, 50 * 1000000);
        mMutex.unlock();
    }
    LOGD("audio play thread exit!");
}

/**
 * 将采样率数值转化为SL对应的数值
 * @param sampleRate
 * @return
 */
SLuint32 AudioSLPlay::getSLSampleRate(int sampleRate) {
    switch (sampleRate) {
        case 8000: {
            return SL_SAMPLINGRATE_8;
        }
        case 11025: {
            return SL_SAMPLINGRATE_11_025;
        }
        case 12000: {
            return SL_SAMPLINGRATE_12;
        }
        case 16000: {
            return SL_SAMPLINGRATE_16;
        }
        case 22050: {
            return SL_SAMPLINGRATE_22_05;
        }
        case 24000: {
            return SL_SAMPLINGRATE_24;
        }
        case 32000: {
            return SL_SAMPLINGRATE_32;
        }
        case 44100: {
            return SL_SAMPLINGRATE_44_1;
        }
        case 48000: {
            return SL_SAMPLINGRATE_48;
        }
        case 64000: {
            return SL_SAMPLINGRATE_64;
        }
        case 88200: {
            return SL_SAMPLINGRATE_88_2;
        }
        case 96000: {
            return SL_SAMPLINGRATE_96;
        }
        case 192000: {
            return SL_SAMPLINGRATE_192;
        }
        default: {
            return SL_SAMPLINGRATE_44_1;
        }
    }
}

/**
 * 将音量转化为SL对应的音量
 * @param volumeLevel
 * @return
 */
SLmillibel AudioSLPlay::getAmplificationLevel(float volumeLevel) {
    if (volumeLevel < 0.00000001) {
        return SL_MILLIBEL_MIN;
    }
    SLmillibel mb = (SLmillibel)lroundf(2000.f * log10f(volumeLevel));
    if (mb < SL_MILLIBEL_MIN) {
        mb = SL_MILLIBEL_MIN;
    } else if (mb > 0) {
        mb = 0;
    }
    return mb;
}