//
// Created by CainHuang on 2020-01-10.
//

#ifndef AVMEDIADECODER_H
#define AVMEDIADECODER_H

#include <map>
#include "AVMediaDemuxer.h"
#include "OnDecodeListener.h"

/**
 * 媒体解码器基类
 */
class AVMediaDecoder {
public:
    AVMediaDecoder(std::shared_ptr<AVMediaDemuxer> mediaDemuxer);

    virtual ~AVMediaDecoder();

    // 设置解码器名称
    virtual void setDecoder(const char *name);

    // 打开解码器
    virtual int openDecoder(std::map<std::string, std::string> decodeOptions);

    // 解码一个数据包
    virtual int decodePacket(AVPacket *packet, OnDecodeListener *listener, int *gotFrame);

    // 关闭解码器
    virtual void closeDecoder();

    // 获取媒体流索引
    int getStreamIndex();

    // 获取解码上下文
    AVCodecContext *getContext();

    // 获取媒体类型
    virtual AVMediaType getMediaType() = 0;

protected:
    // 初始化媒体数据
    virtual void initMetadata() = 0;
protected:
    std::weak_ptr<AVMediaDemuxer> mWeakDemuxer;
    const char *pCodecName;
    AVCodecContext *pCodecCtx;
    AVCodec *pCodec;
    AVStream *pStream;
    int mStreamIndex;
};


#endif //AVMEDIADECODER_H
