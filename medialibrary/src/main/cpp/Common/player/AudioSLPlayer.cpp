//
// Created by CainHuang on 2020-01-14.
//

#include "AudioSLPlayer.h"

AudioSLPlayer::AudioSLPlayer(const std::shared_ptr<AudioProvider> &audioProvider)
              : AudioPlayer(audioProvider) {
    reset();
}

AudioSLPlayer::~AudioSLPlayer() {
    release();
}

/**
 * SLES缓冲回调
 * @param bf
 * @param context
 */
void slBufferPCMCallBack(SLAndroidSimpleBufferQueueItf bf, void *context) {

}

int AudioSLPlayer::open(int sampleRate, int channels) {
    SLresult result;

    release();

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
    mBytesPerFrame   = format_pcm.numChannels * format_pcm.bitsPerSample / 8;  // 一帧占多少字节
    mMillisPerBuffer  = OPENSLES_BUFLEN;                                        // 每个缓冲区占多少毫秒
    mFramePerBuffer = mMillisPerBuffer * format_pcm.samplesPerSec / 1000000;  // 一个缓冲区有多少帧数据
    mBytesPerBuffer  = mBytesPerFrame * mFramePerBuffer;                    // 一个缓冲区大小
    mBufferSize   = OPENSLES_BUFFERS * mBytesPerBuffer;                        // 缓冲区总大小

    LOGI("OpenSL-ES: bytes_per_frame  = %d bytes\n",  mBytesPerFrame);
    LOGI("OpenSL-ES: milli_per_buffer = %d ms\n",     mMillisPerBuffer);
    LOGI("OpenSL-ES: frame_per_buffer = %d frames\n", mFramePerBuffer);
    LOGI("OpenSL-ES: mBufferSize  = %d bytes\n",  (int)mBufferSize);

    // 获取采样率
    mSampleRate = format_pcm.samplesPerSec / 1000;

    // 创建缓冲区
    mBuffer = (uint8_t *)malloc(mBufferSize);
    if (!mBuffer) {
        LOGE("%s: failed to alloc buffer %d\n", __func__, (int)mBufferSize);
        return -1;
    }

    // 填充缓冲区数据
    memset(mBuffer, 0, mBufferSize);
    for(int i = 0; i < OPENSLES_BUFFERS; ++i) {
        result = (*slBufferQueueItf)->Enqueue(slBufferQueueItf,
                                              mBuffer + i * mBytesPerBuffer,
                                              mBytesPerBuffer);
        if (result != SL_RESULT_SUCCESS)  {
            LOGE("%s: slBufferQueueItf->Enqueue(000...) failed", __func__);
        }
    }

    LOGD("open AudioSLPlayer success");
    mInited = true;
    // 返回缓冲大小
    return mBufferSize;
}

void AudioSLPlayer::start() {
    if (!mInited) {
        LOGD("AudioSLPlayer has not inited!");
        return;
    }
    if (!mAudioThread) {
        mAbortRequest = false;
        mPauseRequest = false;
        mAudioThread = new Thread(this, Priority_High);
        mAudioThread->start();
    }
}

void AudioSLPlayer::stop() {

    mMutex.lock();
    mAbortRequest = true;
    mCondition.signal();
    mMutex.unlock();

    if (mAudioThread && mAudioThread->isActive()) {
        mAudioThread->join();
        delete mAudioThread;
        mAudioThread = nullptr;
    }
}

void AudioSLPlayer::pause() {
    mMutex.lock();
    mPauseRequest = true;
    mCondition.signal();
    mMutex.unlock();
}

void AudioSLPlayer::resume() {
    mMutex.lock();
    mPauseRequest = false;
    mCondition.signal();
    mMutex.unlock();
}

void AudioSLPlayer::flush() {
    mMutex.lock();
    mFlushRequest = true;
    mCondition.signal();
    mMutex.unlock();
}

void AudioSLPlayer::setStereoVolume(float leftVolume, float rightVolume) {
    mMutex.lock();
    if (!mUpdateVolume) {
        mLeftVolume = leftVolume;
        mRightVolume = rightVolume;
        mUpdateVolume = true;
    }
    mCondition.signal();
    mMutex.unlock();
}

void AudioSLPlayer::run() {
    uint8_t *next_buffer = nullptr;
    int next_buffer_index = 0;

    if (!mAbortRequest && !mPauseRequest) {
        (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
    }

    while (true) {

        // 退出播放线程
        if (mAbortRequest) {
            break;
        }

        // 暂停
        if (mPauseRequest) {
            continue;
        }

        // 获取缓冲队列状态
        SLAndroidSimpleBufferQueueState slState = {0};
        SLresult slRet = (*slBufferQueueItf)->GetState(slBufferQueueItf, &slState);
        if (slRet != SL_RESULT_SUCCESS) {
            LOGE("%s: slBufferQueueItf->GetState() failed\n", __func__);
            mMutex.unlock();
        }
        // 判断暂停或者队列中缓冲区填满了
        mMutex.lock();
        if (!mAbortRequest && (mPauseRequest || slState.count >= OPENSLES_BUFFERS)) {
            while (!mAbortRequest && (mPauseRequest || slState.count >= OPENSLES_BUFFERS)) {

                if (!mPauseRequest) {
                    (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
                }
                mCondition.waitRelative(mMutex, 10 * 1000000);
                slRet = (*slBufferQueueItf)->GetState(slBufferQueueItf, &slState);
                if (slRet != SL_RESULT_SUCCESS) {
                    LOGE("%s: slBufferQueueItf->GetState() failed\n", __func__);
                    mMutex.unlock();
                }

                if (mPauseRequest) {
                    (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PAUSED);
                }
            }

            if (!mAbortRequest && !mPauseRequest) {
                (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
            }
        }
        if (mFlushRequest) {
            (*slBufferQueueItf)->Clear(slBufferQueueItf);
            mFlushRequest = false;
        }
        mMutex.unlock();

        mMutex.lock();
        // 通过回调填充PCM数据
        if (nullptr != mAudioProvider.lock()) {
            next_buffer = mBuffer + next_buffer_index * mBytesPerBuffer;
            next_buffer_index = (next_buffer_index + 1) % OPENSLES_BUFFERS;
            mAudioProvider.lock()->onAudioProvide(next_buffer, mBytesPerBuffer);
        }
        mMutex.unlock();

        // 更新音量
        if (mUpdateVolume) {
            if (slVolumeItf != nullptr) {
                SLmillibel level = getAmplificationLevel((mLeftVolume + mRightVolume) / 2);
                SLresult result = (*slVolumeItf)->SetVolumeLevel(slVolumeItf, level);
                if (result != SL_RESULT_SUCCESS) {
                    LOGE("slVolumeItf->SetVolumeLevel failed %d\n", (int)result);
                }
            }
            mUpdateVolume = false;
        }

        // 刷新缓冲区还是将数据入队缓冲区
        if (mFlushRequest) {
            (*slBufferQueueItf)->Clear(slBufferQueueItf);
            mFlushRequest = false;
        } else {
            if (slPlayItf != nullptr) {
                (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
            }
            slRet = (*slBufferQueueItf)->Enqueue(slBufferQueueItf, next_buffer, (SLuint32)mBytesPerBuffer);
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
    if (slPlayItf) {
        (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_STOPPED);
    }
}

void AudioSLPlayer::reset() {
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
    mAudioThread = nullptr;
    mUpdateVolume = false;
    mInited = false;
}

void AudioSLPlayer::release() {
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
    mInited = false;
}


SLuint32 AudioSLPlayer::getSLSampleRate(int sampleRate) {
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

SLmillibel AudioSLPlayer::getAmplificationLevel(float volumeLevel) {
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