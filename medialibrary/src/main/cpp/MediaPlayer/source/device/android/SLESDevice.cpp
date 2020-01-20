//
// Created by cain on 2018/12/28.
//

#include <AndroidLog.h>
#include "SLESDevice.h"

#define OPENSLES_BUFFERS 4 // 最大缓冲区数量
#define OPENSLES_BUFLEN  10 // 缓冲区长度(毫秒)

SLESDevice::SLESDevice() {
    slObject = NULL;
    slEngine = NULL;
    slOutputMixObject = NULL;
    slPlayerObject = NULL;
    slPlayItf = NULL;
    slVolumeItf = NULL;
    slBufferQueueItf = NULL;
    memset(&audioDeviceSpec, 0, sizeof(AudioDeviceSpec));
    abortRequest = 1;
    pauseRequest = 0;
    flushRequest = 0;
    audioThread = NULL;
    updateVolume = false;
}

SLESDevice::~SLESDevice() {
    mMutex.lock();
    memset(&audioDeviceSpec, 0, sizeof(AudioDeviceSpec));
    if (slPlayerObject != NULL) {
        (*slPlayerObject)->Destroy(slPlayerObject);
        slPlayerObject = NULL;
        slPlayItf = NULL;
        slVolumeItf = NULL;
        slBufferQueueItf = NULL;
    }

    if (slOutputMixObject != NULL) {
        (*slOutputMixObject)->Destroy(slOutputMixObject);
        slOutputMixObject = NULL;
    }

    if (slObject != NULL) {
        (*slObject)->Destroy(slObject);
        slObject = NULL;
        slEngine = NULL;
    }
    mMutex.unlock();
}

void SLESDevice::start() {
    // 回调存在时，表示成功打开SLES音频设备，另外开一个线程播放音频
    if (audioDeviceSpec.callback != NULL) {
        abortRequest = 0;
        pauseRequest = 0;
        if (!audioThread) {
            audioThread = new Thread(this, Priority_High);
            audioThread->start();
        }
    } else {
        ALOGE("audio device callback is NULL!");
    }
}

void SLESDevice::stop() {

    mMutex.lock();
    abortRequest = 1;
    mCondition.signal();
    mMutex.unlock();

    if (audioThread) {
        audioThread->join();
        delete audioThread;
        audioThread = NULL;
    }
}

void SLESDevice::pause() {
    mMutex.lock();
    pauseRequest = 1;
    mCondition.signal();
    mMutex.unlock();
}

void SLESDevice::resume() {
    mMutex.lock();
    pauseRequest = 0;
    mCondition.signal();
    mMutex.unlock();
}

/**
 * 清空SL缓冲队列
 */
void SLESDevice::flush() {
    mMutex.lock();
    flushRequest = 1;
    mCondition.signal();
    mMutex.unlock();
}

/**
 * 设置音量
 * @param left_volume
 * @param right_volume
 */
void SLESDevice::setStereoVolume(float left_volume, float right_volume) {
    Mutex::Autolock lock(mMutex);
    if (!updateVolume) {
        leftVolume = left_volume;
        rightVolume = right_volume;
        updateVolume = true;
    }
    mCondition.signal();
}

void SLESDevice::run() {
    uint8_t *next_buffer = NULL;
    int next_buffer_index = 0;

    if (!abortRequest && !pauseRequest) {
        (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
    }

    while (true) {

        // 退出播放线程
        if (abortRequest) {
            break;
        }

        // 暂停
        if (pauseRequest) {
            continue;
        }

        // 获取缓冲队列状态
        SLAndroidSimpleBufferQueueState slState = {0};
        SLresult slRet = (*slBufferQueueItf)->GetState(slBufferQueueItf, &slState);
        if (slRet != SL_RESULT_SUCCESS) {
            ALOGE("%s: slBufferQueueItf->GetState() failed\n", __func__);
            mMutex.unlock();
        }
        // 判断暂停或者队列中缓冲区填满了
        mMutex.lock();
        if (!abortRequest && (pauseRequest || slState.count >= OPENSLES_BUFFERS)) {
            while (!abortRequest && (pauseRequest || slState.count >= OPENSLES_BUFFERS)) {

                if (!pauseRequest) {
                    (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
                }
                mCondition.waitRelative(mMutex, 10 * 1000000);
                slRet = (*slBufferQueueItf)->GetState(slBufferQueueItf, &slState);
                if (slRet != SL_RESULT_SUCCESS) {
                    ALOGE("%s: slBufferQueueItf->GetState() failed\n", __func__);
                    mMutex.unlock();
                }

                if (pauseRequest) {
                    (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PAUSED);
                }
            }

            if (!abortRequest && !pauseRequest) {
                (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
            }

        }
        if (flushRequest) {
            (*slBufferQueueItf)->Clear(slBufferQueueItf);
            flushRequest = 0;
        }
        mMutex.unlock();

        mMutex.lock();
        // 通过回调填充PCM数据
        if (audioDeviceSpec.callback != NULL) {
            next_buffer = buffer + next_buffer_index * bytes_per_buffer;
            next_buffer_index = (next_buffer_index + 1) % OPENSLES_BUFFERS;
            audioDeviceSpec.callback(audioDeviceSpec.userdata, next_buffer, bytes_per_buffer);
        }
        mMutex.unlock();

        // 更新音量
        if (updateVolume) {
            if (slVolumeItf != NULL) {
                SLmillibel level = getAmplificationLevel((leftVolume + rightVolume) / 2);
                SLresult result = (*slVolumeItf)->SetVolumeLevel(slVolumeItf, level);
                if (result != SL_RESULT_SUCCESS) {
                    ALOGE("slVolumeItf->SetVolumeLevel failed %d\n", (int)result);
                }
            }
            updateVolume = false;
        }

        // 刷新缓冲区还是将数据入队缓冲区
        if (flushRequest) {
            (*slBufferQueueItf)->Clear(slBufferQueueItf);
            flushRequest = 0;
        } else {
            if (slPlayItf != NULL) {
                (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_PLAYING);
            }
            slRet = (*slBufferQueueItf)->Enqueue(slBufferQueueItf, next_buffer, bytes_per_buffer);
            if (slRet == SL_RESULT_SUCCESS) {
                // do nothing
            } else if (slRet == SL_RESULT_BUFFER_INSUFFICIENT) {
                // don't retry, just pass through
                ALOGE("SL_RESULT_BUFFER_INSUFFICIENT\n");
            } else {
                ALOGE("slBufferQueueItf->Enqueue() = %d\n", (int)slRet);
                break;
            }
        }
    }
    if (slPlayItf) {
        (*slPlayItf)->SetPlayState(slPlayItf, SL_PLAYSTATE_STOPPED);
    }
}


/**
 * SLES缓冲回调
 * @param bf
 * @param context
 */
void slBufferPCMCallBack(SLAndroidSimpleBufferQueueItf bf, void *context) {

}

/**
 * 打开音频设备，并返回缓冲区大小
 * @param desired
 * @param obtained
 * @return
 */
int SLESDevice::open(const AudioDeviceSpec *desired, AudioDeviceSpec *obtained) {
    SLresult result;
    result = slCreateEngine(&slObject, 0, NULL, 0, NULL, NULL);
    if ((result) != SL_RESULT_SUCCESS) {
        ALOGE("%s: slCreateEngine() failed", __func__);
        return -1;
    }
    result = (*slObject)->Realize(slObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slObject->Realize() failed", __func__);
        return -1;
    }
    result = (*slObject)->GetInterface(slObject, SL_IID_ENGINE, &slEngine);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slObject->GetInterface() failed", __func__);
        return -1;
    }

    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};
    result = (*slEngine)->CreateOutputMix(slEngine, &slOutputMixObject, 1, mids, mreq);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slEngine->CreateOutputMix() failed", __func__);
        return -1;
    }
    result = (*slOutputMixObject)->Realize(slOutputMixObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slOutputMixObject->Realize() failed", __func__);
        return -1;
    }
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, slOutputMixObject};
    SLDataSink audioSink = {&outputMix, NULL};

    SLDataLocator_AndroidSimpleBufferQueue android_queue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            OPENSLES_BUFFERS
    };

    // 根据通道数设置通道mask
    SLuint32 channelMask;
    switch (desired->channels) {
        case 2: {
            channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
            break;
        }
        case 1: {
            channelMask = SL_SPEAKER_FRONT_CENTER;
            break;
        }
        default: {
            ALOGE("%s, invalid channel %d", __func__, desired->channels);
            return -1;
        }
    }
    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM,              // 播放器PCM格式
            desired->channels,              // 声道数
            getSLSampleRate(desired->freq), // SL采样率
            SL_PCMSAMPLEFORMAT_FIXED_16,    // 位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,    // 和位数一致
            channelMask,                    // 格式
            SL_BYTEORDER_LITTLEENDIAN       // 小端存储
    };

    SLDataSource slDataSource = {&android_queue, &format_pcm};

    const SLInterfaceID ids[3] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE, SL_IID_VOLUME, SL_IID_PLAY};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    result = (*slEngine)->CreateAudioPlayer(slEngine, &slPlayerObject, &slDataSource,
                                            &audioSink, 3, ids, req);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slEngine->CreateAudioPlayer() failed", __func__);
        return -1;
    }

    result = (*slPlayerObject)->Realize(slPlayerObject, SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slPlayerObject->Realize() failed", __func__);
        return -1;
    }

    result = (*slPlayerObject)->GetInterface(slPlayerObject, SL_IID_PLAY, &slPlayItf);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slPlayerObject->GetInterface(SL_IID_PLAY) failed", __func__);
        return -1;
    }

    result = (*slPlayerObject)->GetInterface(slPlayerObject, SL_IID_VOLUME, &slVolumeItf);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slPlayerObject->GetInterface(SL_IID_VOLUME) failed", __func__);
        return -1;
    }

    result = (*slPlayerObject)->GetInterface(slPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                             &slBufferQueueItf);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slPlayerObject->GetInterface(SL_IID_ANDROIDSIMPLEBUFFERQUEUE) failed", __func__);
        return -1;
    }

    result = (*slBufferQueueItf)->RegisterCallback(slBufferQueueItf, slBufferPCMCallBack, this);
    if (result != SL_RESULT_SUCCESS) {
        ALOGE("%s: slBufferQueueItf->RegisterCallback() failed", __func__);
        return -1;
    }

    // 这里计算缓冲区大小等参数
    bytes_per_frame   = format_pcm.numChannels * format_pcm.bitsPerSample / 8;  // 一帧占多少字节
    milli_per_buffer  = OPENSLES_BUFLEN;                                        // 每个缓冲区占多少毫秒
    frames_per_buffer = milli_per_buffer * format_pcm.samplesPerSec / 1000000;  // 一个缓冲区有多少帧数据
    bytes_per_buffer  = bytes_per_frame * frames_per_buffer;                    // 一个缓冲区大小
    buffer_capacity   = OPENSLES_BUFFERS * bytes_per_buffer;

    ALOGI("OpenSL-ES: bytes_per_frame  = %d bytes\n",  bytes_per_frame);
    ALOGI("OpenSL-ES: milli_per_buffer = %d ms\n",     milli_per_buffer);
    ALOGI("OpenSL-ES: frame_per_buffer = %d frames\n", frames_per_buffer);
    ALOGI("OpenSL-ES: buffer_capacity  = %d bytes\n",  buffer_capacity);
    ALOGI("OpenSL-ES: buffer_capacity  = %d bytes\n",  (int)buffer_capacity);

    if (obtained != NULL) {
        *obtained = *desired;
        obtained->size = (uint32_t)buffer_capacity;
        obtained->freq = format_pcm.samplesPerSec / 1000;
    }
    audioDeviceSpec = *desired;

    // 创建缓冲区
    buffer = (uint8_t *)malloc(buffer_capacity);
    if (!buffer) {
        ALOGE("%s: failed to alloc buffer %d\n", __func__, (int)buffer_capacity);
        return -1;
    }

    // 填充缓冲区数据
    memset(buffer, 0, buffer_capacity);
    for(int i = 0; i < OPENSLES_BUFFERS; ++i) {
        result = (*slBufferQueueItf)->Enqueue(slBufferQueueItf,
                                              buffer + i * bytes_per_buffer,
                                              bytes_per_buffer);
        if (result != SL_RESULT_SUCCESS)  {
            ALOGE("%s: slBufferQueueItf->Enqueue(000...) failed", __func__);
        }
    }

    ALOGD("open SLES Device success");
    // 返回缓冲大小
    return buffer_capacity;
}

/**
 * 转换成SL的采样率
 * @param sampleRate
 * @return
 */
SLuint32 SLESDevice::getSLSampleRate(int sampleRate) {
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
 * 计算SL的音量
 * @param volumeLevel
 * @return
 */
SLmillibel SLESDevice::getAmplificationLevel(float volumeLevel) {
    if (volumeLevel < 0.00000001) {
        return SL_MILLIBEL_MIN;
    }
    SLmillibel mb = lroundf(2000.f * log10f(volumeLevel));
    if (mb < SL_MILLIBEL_MIN) {
        mb = SL_MILLIBEL_MIN;
    } else if (mb > 0) {
        mb = 0;
    }
    return mb;
}


