//
// Created by CainHuang on 2020-01-14.
//

#include "AudioSLPlay.h"

AudioSLPlay::AudioSLPlay(const std::shared_ptr<AudioProvider> &audioProvider)
              : AudioPlay(audioProvider) {
    LOGD("AudioSLPlay::constructor()");
    reset();
    createEngine();
    mBufferNumber = 2;
    mBufferQueue = new SafetyQueue<short *>();
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
            (SLuint32)mBufferNumber
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
    result = (*slBufferQueueItf)->RegisterCallback(slBufferQueueItf, nullptr, this);
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

    // 创建缓冲区
    for (int i = 0; i < mBufferNumber; ++i) {
        auto buffer = (short *) malloc((size_t)mMaxBufferSize);
        memset(buffer, 0, (size_t)mMaxBufferSize);
        mBufferQueue->push(buffer);
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
    if (!mAudioThread->isActive()) {
        mAudioThread->start();
    }
    LOGD("AudioSLPlay::start() success");
}

/**
 * 停止播放
 */
void AudioSLPlay::stop() {
    LOGD("AudioSLPlay::stop()");
    mAbortRequest = true;
    mCondition.signal();
    if (mAudioThread != nullptr) {
        mAudioThread->join();
        delete mAudioThread;
        mAudioThread = nullptr;
    }
    LOGD("AudioSLPlay::stop() success");
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
    mFlushRequest = true;
    mCondition.signal();
}

/**
 * 设置播放音量
 * @param leftVolume    左音量
 * @param rightVolume   右音量
 */
void AudioSLPlay::setStereoVolume(float leftVolume, float rightVolume) {
    LOGD("AudioSLPlay::setStereoVolume(): {%.2f, %.2f}", leftVolume, rightVolume);
    if (!mUpdateVolume) {
        mLeftVolume = leftVolume;
        mRightVolume = rightVolume;
        mUpdateVolume = true;
    }
    mCondition.signal();
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
    mFlushRequest = false;
    mUpdateVolume = false;
    mAudioThread = nullptr;
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
    // 释放缓冲区内存
    if (mBufferQueue != nullptr) {
        while (mBufferQueue->size() > 0) {
            auto buffer = mBufferQueue->pop();
            free(buffer);
        }
        delete mBufferQueue;
        mBufferQueue = nullptr;
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
 * 音频播放
 */
void AudioSLPlay::audioPlay() {
    int size = 0;
    LOGD("AudioSLPlay::audioPlay()");
    if (!mAbortRequest && !mPauseRequest) {
        enginePlay();
    }
    while (true) {

        // 获取缓冲队列状态
        SLAndroidSimpleBufferQueueState slState = {0};
        SLresult slRet = (*slBufferQueueItf)->GetState(slBufferQueueItf, &slState);
        if (slRet != SL_RESULT_SUCCESS) {
            LOGE("%s: slBufferQueueItf->GetState() failed\n", __func__);
            mMutex.unlock();
        }

        // 判断暂停或者队列中缓冲区填满了
        mMutex.lock();
        while (!mAbortRequest && (slState.count >= mBufferNumber)) {
            mCondition.waitRelativeMs(mMutex, 5);
            slRet = (*slBufferQueueItf)->GetState(slBufferQueueItf, &slState);
            if (slRet != SL_RESULT_SUCCESS) {
                LOGE("%s: slBufferQueueItf->GetState() failed\n", __func__);
                mMutex.unlock();
            }
        }
        // 刷新缓冲区
        if (mFlushRequest) {
            engineFlush();
            mFlushRequest = false;
        }
        mMutex.unlock();

        // 退出播放线程
        mMutex.lock();
        if (mAbortRequest) {
            LOGD("AudioSLPlay::exiting...");
            mMutex.unlock();
            break;
        }

        // 暂停继续
        if (mPauseRequest) {
            LOGD("AudioSLPlay::pause...");
            mCondition.wait(mMutex);
            mMutex.unlock();
            continue;
        }
        mMutex.unlock();

        // 从缓冲队列中取出缓冲区对象，不存在则说明已经退出播放线程，直接退出
        short *buffer = nullptr;
        if (mBufferQueue != nullptr && !mBufferQueue->empty()) {
            buffer = mBufferQueue->pop();
        }
        if (!buffer) {
            break;
        }

        // 通过回调填充PCM数据
        mMutex.lock();
        if (nullptr != mAudioProvider.lock()) {
            size = mAudioProvider.lock()->onAudioProvide(&buffer, mMaxBufferSize);
            if (size > mMaxBufferSize) {
                mMaxBufferSize = size;
            }
        }
        // buffer用完放回队列中
        if (size <= 0 && buffer) {
            if (mBufferQueue != nullptr) {
                mBufferQueue->push(buffer);
            } else {
                free(buffer);
            }
        }
        mMutex.unlock();

        // 更新音量
        if (mUpdateVolume) {
            engineSetVolume();
            mUpdateVolume = false;
        }

        // 退出播放线程
        if (mAbortRequest) {
            engineFlush();
            break;
        }

        if (size > 0) {
            if (!isEnginePlaying()) {
                enginePlay();
            }
            slRet = (*slBufferQueueItf)->Enqueue(slBufferQueueItf, buffer, (SLuint32)size);
            // buffer用完放回去
            if (mBufferQueue != nullptr) {
                mBufferQueue->push(buffer);
            } else {
                free(buffer);
            }
            if (slRet == SL_RESULT_SUCCESS) {
                // do nothing
            } else if (slRet == SL_RESULT_BUFFER_INSUFFICIENT) {
                // don't retry, just pass through
                LOGE("SL_RESULT_BUFFER_INSUFFICIENT\n");
            } else {
                LOGE("slBufferQueueItf->Enqueue() = %d\n", (int)slRet);
                break;
            }
        }
    }
    mMutex.unlock();
    engineStop();
    engineFlush();
    LOGD("audio play thread exit!");
}

/**
 * 引擎暂停
 */
void AudioSLPlay::enginePause() {
    LOGD("AudioSLPlay::enginePause()");
    if (slPlayItf != nullptr) {
        (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PAUSED);
    }
}

/**
 * 引擎停止
 */
void AudioSLPlay::engineStop() {
    LOGD("AudioSLPlay::engineStop()");
    if (slPlayItf != nullptr) {
        (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_STOPPED);
    }
}

/**
 * 引擎播放
 */
void AudioSLPlay::enginePlay() {
    LOGD("AudioSLPlay::enginePlay()");
    if (slPlayItf != nullptr) {
        (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
    }
}

/**
 * 引擎刷新缓冲区
 */
void AudioSLPlay::engineFlush() {
    LOGD("AudioSLPlay::engineFlush()");
    if (slBufferQueueItf != nullptr) {
        (*slBufferQueueItf)->Clear(slBufferQueueItf);
    }
}

/**
 * 是否处于播放状态
 * @return
 */
bool AudioSLPlay::isEnginePlaying() {
    if (slPlayItf != nullptr) {
        SLuint32 state;
        SLresult result = (*slPlayItf)->GetPlayState(slPlayItf, &state);
        if (result == SL_RESULT_SUCCESS) {
            return (state == SL_PLAYSTATE_PLAYING);
        }
    }
    return false;
}

/**
 * 引擎设置音量
 */
void AudioSLPlay::engineSetVolume() {
    if (slVolumeItf != NULL) {
        SLmillibel level = getAmplificationLevel((mLeftVolume + mRightVolume) / 2);
        SLresult result = (*slVolumeItf)->SetVolumeLevel(slVolumeItf, level);
        if (result != SL_RESULT_SUCCESS) {
            LOGE("slVolumeItf->SetVolumeLevel failed %d\n", (int)result);
        }
    }
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