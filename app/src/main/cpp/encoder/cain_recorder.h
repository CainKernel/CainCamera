//
// Created by Administrator on 2018/1/4.
//

#ifndef CAINCAMERA_CAIN_RECORDER_H
#define CAINCAMERA_CAIN_RECORDER_H

#include "common_encoder.h"
#include "native_log.h"

#include "encoder_params.h"

class CainRecorder {
public:
    // 录制状态，默认处于空闲状态
    int recorderState = RECORDER_IDLE;
    // 开始时间
    int64_t startTime = 0;
    // 计算帧数
    int frameCount = 0;
    // muxer部分
    AVFormatContext *mFormatCtx;
    // 视频编码器部分
    AVCodec *mVideoCodec;
    AVCodecContext *mVCodecCtx;
    AVStream *mVideoStream;
    AVFrame *mVideoFrame;
    AVPacket mVideoPacket;
    int y_length = 0;
    int uv_length = 0;

    // 音频编码器部分
    AVCodec *mAudioCodec;
    AVCodecContext *mACodecCtx;
    AVStream *mAudioStream;
    AVFrame *mAudioFrame;
    AVPacket mAudioPacket;
    int mSampleSize = 0; // 采样大小

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
    // 刷出剩余编码帧
    int flushFrame(AVFormatContext *fmt_ctx, int streamIndex);
    // h264编码
    int avcEncode(jbyte *yuvData);
    // aac编码
    int aacEncode(jbyte *pcmData);
    // 释放资源
    void release();

private:
    // 初始化视频编码器
    int initVideoEncoder();
    // 初始化音频编码器
    int initAudioEncoder();
};

#endif //CAINCAMERA_CAIN_RECORDER_H
