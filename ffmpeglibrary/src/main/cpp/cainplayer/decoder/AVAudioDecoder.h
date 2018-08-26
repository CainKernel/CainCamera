//
// Created by admin on 2018/4/29.
//

#ifndef CAINPLAYER_AVAUDIODECODER_H
#define CAINPLAYER_AVAUDIODECODER_H


#include "AVDecoder.h"
#include "../common/MediaQueue.h"
#include "../../common/AndroidLog.h"
#include "../common/MediaStatus.h"
#include "../common/MediaJniCall.h"

#ifdef __cplusplus
extern "C" {
#endif

#include "libswresample/swresample.h"
#include "libavutil/time.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#ifdef __cplusplus
};
#endif

class AVAudioDecoder : public AVDecoder {
public:
    AVAudioDecoder(MediaStatus *status, MediaJniCall *jniCall);

    virtual ~AVAudioDecoder();

    // 设置是否视频
    void setVideo(bool video);

    // 播放
    void start();

    // 获取PCM数据
    int getPcmData(void **pcm);

    // 初始化OpenSLES
    int initOpenSL();

    // 暂停
    void pause();

    // 启动
    void resume();

    // 释放资源
    void release();

    // 获取采样率
    int getSLSampleRate();

    // 设置时钟
    void setClock(int secds);

    // 设置采样率
    void setSampleRate(int sampleRate);

    // 退出播放线程
    void exitDecodeThread();

    // pcm缓冲回调
    void pcmCallback(SLAndroidSimpleBufferQueueItf bf);

private:
    // 解码线程句柄
    static void *decodeThreadHandle(void *context);

    // PCM回调
    static void pcmBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void * context);

    pthread_t audioThread;

    int ret;                // 函数调用返回结果
    int64_t dst_layout;     // 重采样为立体声
    int dst_nb_samples;     // 计算转换后的sample个数 a * b / c
    uint8_t *out_buffer;    // buffer 内存区域
    int out_channels;       // 输出声道数
    int data_size;          // buffer大小
    enum AVSampleFormat dst_format;

    AVPacket *mPacket;      // 存放裸数据包
    AVFrame *mFrame;        // 存放解码后的音频数据
    SwrContext *swr_ctx;    // 音频重采样上下文

    // opensl es
    void *buffer;
    int pcmSize;
    int sampleRate;
    bool isExit;
    bool isVideo;
    bool isReadPacketFinish;

    // 引擎接口
    SLObjectItf engineObject;
    SLEngineItf engineEngine;

    //混音器
    SLObjectItf outputMixObject;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb;
    SLEnvironmentalReverbSettings reverbSettings;

    // pcm
    SLObjectItf pcmPlayerObject;
    SLPlayItf pcmPlayerPlay;
    SLVolumeItf pcmPlayerVolume;

    //缓冲器队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue;
};

#endif //CAINPLAYER_AVAUDIODECODER_H
