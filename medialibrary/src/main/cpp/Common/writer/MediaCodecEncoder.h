//
// Created by CainHuang on 2020-01-02.
//

#ifndef MEDIACODECENCODER_H
#define MEDIACODECENCODER_H

#include <AVMediaHeader.h>

class MediaCodecEncoder {
public:
    virtual ~MediaCodecEncoder(){}

    // 设置输出路径
    virtual void setOutputPath(const char *path) = 0;

    // 准备编码器
    virtual int prepare() = 0;

    // 关闭编码器
    virtual int closeEncoder() = 0;

    // 释放资源
    virtual void release() = 0;

    // 编码数据
    virtual void encode(AVMediaData *data) = 0;
};

#endif //MEDIACODECENCODER_H
