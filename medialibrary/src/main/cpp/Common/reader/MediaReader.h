//
// Created by CainHuang on 2019/9/15.
//

#ifndef READER_H
#define READER_H

#include "../AVMediaHeader.h"
#include "OnDecodeListener.h"


/**
 * 媒体读取器
 */
class MediaReader {
public:
    // 设置数据源
    virtual void setDataSource(const char *url) = 0;

    // 指定解码格式，对应命令行的 -f 的参数
    virtual void setInputFormat(const char *format) = 0;

    // 设置媒体读取监听器
    virtual void setReadListener(OnDecodeListener *listener, bool autoRelease) = 0;

    // 添加格式参数
    virtual void addFormatOptions(std::string key, std::string value) = 0;

    // 添加解码参数
    virtual void addDecodeOptions(std::string key, std::string value) = 0;

    // 打开文件
    virtual int openInputFile() = 0;

    // 定位
    virtual int seekTo(float timeMs) = 0;

    // 获取时长
    virtual int64_t getDuration() = 0;

    // 解码数据包
    virtual int decodePacket() = 0;
};

#endif //READER_H
