//
// Created by cain on 2018/1/20.
//

#ifndef CAINCAMERA_RTMPPUSH_H
#define CAINCAMERA_RTMPPUSH_H

#include "jni.h"
#include "librtmp/rtmp.h"

#include "pthread.h"
#include "strings.h"
#include "libx264/x264.h"
#include "libfaac/faac.h"
#include "android/log.h"

extern "C"{
#include "queue.h"

// release版本后需要注释掉
//#define VERBOSE 1

#ifdef VERBOSE

#define JNI_TAG "Cain_Pusher"
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, JNI_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, JNI_TAG, __VA_ARGS__)

#else

#define ALOGE(...) { }
#define ALOGI(...) { }

#endif

class RtmpPusher {

private:
    int mediaSize;
    // x264
    x264_t *videoEncoder;
    x264_picture_t* pic_in;
    x264_picture_t* pic_out;

    // rtmp
    RTMP* rtmpPusher;

    //rtmp开始时间
    long startTime;

    //faac
    faacEncHandle audioEncoder;
    unsigned long inputSamples;
    unsigned long maxOutputBytes;

    // 线程
    pthread_t publisher_tid;
    pthread_mutex_t mutex ;
    pthread_cond_t cond ;

    // 推流标志
    int pushing;
    // 请求停止
    int requestStop;

public:
    RtmpPusher()
            : mediaSize(0),
              videoEncoder(NULL),
              pic_in(NULL),
              pic_out(NULL),
              rtmpPusher(NULL),
              startTime(0),
              pushing(0),
              requestStop(0),
              publisher_tid(NULL),
              audioEncoder(NULL) {

    };

    // 宽高和比特率
    int mediaWidth, mediaHeight, bitRate;

private:
    int initRtmp(char *url);
    static void *rtmpPushThread(void *args);
    //视频编码
    void avcEncode(char *data, int clear);
    // 写入sps 和 pps 头部信息
    void writeSpsPpsFrame(char *pps, char *sps, int pps_len, int sps_len);
    // 写入帧
    void pushVideoFrame(char *buf, int len);
    // 入队
    void rtmpPacketPush(RTMPPacket *packet);
    // 初始化音频头部信息
    int initAudioHeader();
    // 释放资源
    static void nativeStop(RtmpPusher *pusher);

public :
    // 初始化视频
    int initVideo(const char *url, int width, int height, int bitrate);
    // 初始化音频
    int initAudio(int sampleRate, int channel);
    // 后置摄像头数据编码推流
    void avcEncodeWithBackCamera(char *data);
    // 前置摄像头数据编码推流
    void avcEncodeWithFrontCamera(char *data);
    // 手机横屏编码推流
    void avcEncodeLandscape(char *data);
    // 音频推流
    void aacEncode(char *data);
    // 停止推流
    void stop();

    // 获取视频编码器
    x264_t *getVideoEncoder();
    x264_picture_t* getPictureIn();
    x264_picture_t* getPictureOut();
    RTMP* getRtmpPusher();
    faacEncHandle getAudioEncoder();

    void setVideoEncoder(x264_t *encoder);
    void setPictureIn(x264_picture_t *picture);
    void setPictureOut(x264_picture_t *picture);
    void setRtmpPusher(RTMP *pusher);
    void setAudioEncoder(faacEncHandle encoder);

    // 停止状态
    int isStop();
    // 推流状态
    int isPushing();
};

}


#endif //CAINCAMERA_RTMPPUSH_H
