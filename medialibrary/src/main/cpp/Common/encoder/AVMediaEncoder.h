//
// Created by CainHuang on 2020-01-09.
//

#ifndef AVMEDIAENCODER_H
#define AVMEDIAENCODER_H

#include "AVMediaHeader.h"
#include "AVMediaData.h"
#include "muxer/AVMediaMuxer.h"

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
    int openEncoder(std::map<std::string, std::string> encodeOptions);

    // 编码一帧数据
    int encodeFrame(AVFrame *frame, int *gotFrame);

    // 将一帧数据送去编码
    int sendFrame(AVFrame *frame);

    // 接收编码后的数据包
    int receiveEncodePacket(AVPacket *packet, int *gotFrame);

    //  将数据包写入文件中
    int writePacket(AVPacket *packet);

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
