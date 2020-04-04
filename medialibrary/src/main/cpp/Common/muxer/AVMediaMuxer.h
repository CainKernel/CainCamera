//
// Created by CainHuang on 2020-01-09.
//

#ifndef AVMEDIAMUXER_H
#define AVMEDIAMUXER_H

#include "AVMediaHeader.h"
#include "AVMediaData.h"

/**
 * 媒体复用器
 */
class AVMediaMuxer {
public:
    AVMediaMuxer();

    virtual ~AVMediaMuxer();

    // 设置输出文件路径
    void setOutputPath(const char *path);

    // 初始化复用器
    int init();

    // 打开复用器
    int openMuxer();

    // 创建新的媒体流
    AVStream *createStream(AVCodec *encoder);

    // 创建新的媒体流
    AVStream *createStream(AVCodecID id);

    // 写入文件头部信息
    int writeHeader(AVDictionary **options = nullptr);

    // 写入数据包
    int writeFrame(AVPacket *packet);

    // 写入文件尾部信息
    int writeTrailer();

    // 关闭复用器
    void closeMuxer();

    // 是否存在全局头部信息
    bool hasGlobalHeader();

    // 打印复用器信息
    void printInfo();
private:
    const char *mPath;              // 文件输出路径
    AVFormatContext *pFormatCtx;    // 复用上下文
};


#endif //AVMEDIAMUXER_H
