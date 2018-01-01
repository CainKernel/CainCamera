//
// Created by cain on 2017/12/28.
//

#ifndef CAINCAMERA_ENCODER_PARAMS_H
#define CAINCAMERA_ENCODER_PARAMS_H

#include <jni.h>

typedef struct EncoderParams {

    const char *mMediaPath; // 视频文件
    int mPreviewWidth;      // 预览宽度
    int mPreviewHeight;     // 预览高度
    int mVideoWidth;        // 输出宽度
    int mVideoHeight;       // 输出高度
    int mFrameRate;         // 帧率
    long long mBitRate;     // 码率
    int mAudioBitRate;      // 音频码率
    int mAudioSampleRate;   // 采样率
    JNIEnv *env;            // 全局指针
    JavaVM *javaVM;         // JVM指针
    jclass javaClass;       // Java接口类的class对象

};

#endif //CAINCAMERA_ENCODER_PARAMS_H
