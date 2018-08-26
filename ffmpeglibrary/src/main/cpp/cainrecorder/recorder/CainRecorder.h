//
// Created by cain.huang on 2018/1/4.
//

#ifndef CAINCAMERA_CAIN_RECORDER_H
#define CAINCAMERA_CAIN_RECORDER_H

#include "CommonRecorder.h"
#include "AndroidLog.h"

#include "RecorderParams.h"
#include "CainEncoder.h"

class CainRecorder {
public:
    // 录制状态，默认处于空闲状态
    RecordState recorderState;
    // 编码器
    CainEncoder *encoder;

public:
    // 参数保存
    EncoderParams *params;

    // 构造函数
    CainRecorder(EncoderParams *);
    // 析构函数
    ~CainRecorder(){};

    // 初始化编码器
    int initRecorder();
    // 开始录制
    void startRecord();
    // 录制结尾
    void recordEndian();
    // h264编码
    int avcEncode(jbyte *yuvData);
    // aac编码
    int aacEncode(jbyte *pcmData);
    // 释放资源
    void release();
};

#endif //CAINCAMERA_CAIN_RECORDER_H
