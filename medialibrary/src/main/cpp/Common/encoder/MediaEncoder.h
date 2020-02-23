//
// Created by CainHuang on 2020-01-02.
//

#ifndef MEDIACODECENCODER_H
#define MEDIACODECENCODER_H

#include <AVMediaHeader.h>
#include <AVMediaData.h>

/**
 * 编码监听器
 */
class OnEncodingListener {
public:
    virtual void onEncoding(long duration) = 0;
};

class MediaEncoder {
public:
    virtual ~MediaEncoder(){}

    // 设置编码监听器
    virtual void setOnEncodingListener(OnEncodingListener *listener) = 0;

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
