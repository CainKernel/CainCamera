//
// Created by cain on 2017/12/28.
//

#ifndef CAINCAMERA_ENCODER_PARAMS_H
#define CAINCAMERA_ENCODER_PARAMS_H

#include <jni.h>

typedef struct EncoderParams {

    const char *mediaPath;  // 视频文件
    int previewWidth;       // 预览宽度
    int previewHeight;      // 预览高度
    int videoWidth;         // 输出宽度
    int videoHeight;        // 输出高度
    int frameRate;          // 帧率
    long long bitRate;      // 码率
    int enableAudio;        // 是否允许音频编码
    int audioBitRate;       // 音频码率
    int audioSampleRate;    // 采样率
};

#endif //CAINCAMERA_ENCODER_PARAMS_H
