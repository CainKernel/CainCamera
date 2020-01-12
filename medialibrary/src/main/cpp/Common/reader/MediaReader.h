//
// Created by CainHuang on 2019/9/15.
//

#ifndef READER_H
#define READER_H

#include "../AVMediaHeader.h"



/**
 * 媒体读取器
 */
class MediaReader {
public:
    // 设置数据源
    virtual void setDataSource(const char *url) = 0;

    // 设置媒体读取监听器
    virtual void setReadListener(OnDecodeListener *listener, bool autoRelease) = 0;

    // 定位
    virtual int seekTo(float timeMs) = 0;

    // 获取时长
    virtual int64_t getDuration() = 0;

    // 解码数据包
    virtual int decodePacket() = 0;
};

#endif //READER_H
