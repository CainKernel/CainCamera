//
// Created by cain on 2017/12/27.
//

#ifndef CAINCAMERA_H264_ENCODER_H
#define CAINCAMERA_H264_ENCODER_H

#include "common_encoder.h"
#include "encoder_params.h"
#include "encoder.h"

using namespace std;

class AVCEncoder : public MediaEncoder {

public:
    AVCEncoder(EncoderParams *params);

    // 初始化编码器
    int init(EncoderMuxer * muxer);

    // 开始编码
    static void *startEncoder(void *obj);

    // 编码尾部
    int encoderEndian();

    ~AVCEncoder() {}

protected:
    int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index);
};

#endif //CAINCAMERA_YUVENCODER_H
