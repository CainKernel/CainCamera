//
// Created by cain on 2018/1/1.
//

#ifndef CAINCAMERA_ENCODER_H
#define CAINCAMERA_ENCODER_H

#include "common_encoder.h"
#include "encoder_params.h"
#include "media_muxer.h"
using namespace std;

class EncoderMuxer;

class MediaEncoder {
public:
    MediaEncoder(EncoderParams *params);
    ~MediaEncoder() {}

    // 初始化编码器
    virtual int init(EncoderMuxer * muxer) = 0;

    // 发送帧
    int sendOneFrame(uint8_t *buf);

    // 发送停止信号
    void sendStop();

    // 释放资源
    void release();

    // 编码尾部
    virtual int encoderEndian() = 0;

    // 获取多媒体码流
    AVStream *getMediaStream();

protected:
    virtual int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index) = 0;

    EncoderMuxer * mMuxer;                // 复用器
    EncoderParams *mEncoderParams;      // 编码参数
    int isEnd = 0;                      // 停止标志
    int isRelease = 0;                  // 释放标志
    safety_queue<uint8_t *> mFrameQueue;// 编码队列
    AVStream *mMediaStream;             // 码流
    AVCodecContext *mCodecContext;      // 编码上下文
    AVCodec *mCodec;                    // 编码器
    AVPacket mAVPacket;                 // 编码后的包
    AVFrame *mFrame;                    // 需要编码的帧
    int mFrameCount = 0;                // 帧大小
    int mBufferSize = 0;                // 缓冲大小
    int mGotFrame = 0;                  // 编码帧数目
};

#endif //CAINCAMERA_ENCODER_H
