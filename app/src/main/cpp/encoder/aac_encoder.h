//
// Created by cain on 2017/12/31.
//

#ifndef CAINCAMERA_AAC_ENCODER_H
#define CAINCAMERA_AAC_ENCODER_H

#include "common_encoder.h"
#include "encoder_params.h"
#include "encoder.h"

using namespace std;

class AACEncoder : public MediaEncoder {
public:
    AACEncoder(EncoderParams *params);

    // 初始化编码器
    int init(EncoderMuxer * muxer);

    // 开始编码
    static void *startEncoder(void *obj);

    // 编码尾部
    int encoderEndian();

    ~AACEncoder() {}

protected:
    int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index);

};

#endif //CAINCAMERA_AAC_ENCODER_H
