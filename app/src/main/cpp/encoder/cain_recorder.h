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
    // 编码队列
    safety_queue<uint8_t *> frameQueue;
    // muxer部分
    AVFormatContext *pFormatCtx;
    AVOutputFormat *pOutputFormat;
    // 视频编码器部分
    AVCodec *videoCodec;
    AVCodecContext *videoCodecContext;
    AVStream *videoStream;
    AVFrame *videoFrame;
    AVPacket videoPacket;
    int mVideoSize;
    // 图像转换上下文
    SwsContext *pConvertCtx;

    // 音频编码器部分
    AVCodec *audioCodec;
    AVCodecContext *audioCodecContext;
    AVStream *audioStream;
    AVFrame *audioFrame;
    AVPacket *audioPacket;
    uint8_t  *audioBuffer;
    int sampleSize; // 采样缓冲大小
    // 音频转换上下文
    SwrContext *samples_convert_ctx;

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
    // 停止录制
    void stopRecord();
    // 录制结尾
    void recordEndian();
    // 关闭录制
    void closeRecorder();
    // 发送编码帧
    void sendFrame(uint8_t *data, int type, int len);
    // 编码线程
    static void *encodeThread(void *obj);
    // 刷出剩余编码帧
    int flushFrame(AVFormatContext *fmt_ctx, int streamIndex);
    // h264编码
    int avcEncode(CainRecorder *recorder);
    // aac编码
    int aacEncode(CainRecorder * recorder);
    // 释放资源
    void release();
};

#endif //CAINCAMERA_CAIN_RECORDER_H
