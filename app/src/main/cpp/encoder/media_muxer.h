//
// Created by cain on 2018/1/1.
//

#ifndef CAINCAMERA_MP4_MUXER_H
#define CAINCAMERA_MP4_MUXER_H

#include "common_encoder.h"
#include "encoder_params.h"

using namespace std;

/**
 * 音视频复用器
 */
class EncoderMuxer {
private:
    EncoderParams *mEncoderParams;      // 编码参数
    bool interleaved = true;            // 是否复用

public:
    AVFormatContext *mFormatCtx;        // 复用上下文
    AVOutputFormat *mOutputFormat;      // 输出格式

public:
    EncoderMuxer(EncoderParams *params);
    ~EncoderMuxer(){}

    // 初始化混合器
    int init();

    // 开始
    int muxerHeader();

    // 复用尾部
    int muxerTrailer();

};

#endif //CAINCAMERA_MP4_MUXER_H
