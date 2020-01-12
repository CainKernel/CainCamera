//
// Created by CainHuang on 2020-01-09.
//

#ifndef AVMEDIAENCODER_H
#define AVMEDIAENCODER_H

#include "../AVMediaHeader.h"
#include "../AVMediaData.h"
#include "AVMediaMuxer.h"

#include <map>

/**
 * 媒体编码器
 */
class AVMediaEncoder {
public:
    AVMediaEncoder(std::shared_ptr<AVMediaMuxer> mediaMuxer);

    virtual ~AVMediaEncoder();

    // 设置编码器名称
    void setEncoder(const char *name);

    // 创建编码器
    int createEncoder();

    // 打开编码器
    int openEncoder(std::map<std::string, std::string> mEncodeOptions);

    // 编码一帧数据
    int encodeFrame(AVFrame *frame, int *gotFrame);

    // 关闭编码器
    virtual void closeEncoder();

    // 获取上下文
    AVCodecContext *getContext();

    // 获取媒体类型
    virtual AVMediaType getMediaType() = 0;

    // 获取编码器id
    virtual AVCodecID getCodecId() = 0;

protected:
    std::weak_ptr<AVMediaMuxer> mWeakMuxer;
    const char *pCodecName;
    AVCodecContext *pCodecCtx;
    AVCodec *pCodec;
    AVStream *pStream;
};


#endif //AVMEDIAENCODER_H
