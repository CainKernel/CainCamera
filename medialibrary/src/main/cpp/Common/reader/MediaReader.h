//
// Created by CainHuang on 2019/9/15.
//

#ifndef READER_H
#define READER_H

#include "../AVMediaHeader.h"

/**
 * 读帧监听器
 */
class OnReadListener {
public:
    virtual void onReadFrame(AVFrame *frame, AVMediaType type) = 0;
};

/**
 * 媒体读取器
 */
class MediaReader {
public:
    // 设置数据源
    virtual void setDataSource(const char *url) = 0;

    // 设置起始位置
    virtual void setStart(float timeMs) = 0;

    // 设置结束位置
    virtual void setEnd(float timeMs) = 0;

    // 设置媒体读取监听器
    virtual void setReadListener(OnReadListener *listener, bool autoRelease) = 0;

    // 定位
    virtual void seekTo(float timeMs) = 0;

    // 开始读取
    virtual void start() = 0;

    // 暂停读取
    virtual void pause() = 0;

    // 继续读取
    virtual void resume() = 0;

    // 停止读取
    virtual void stop() = 0;
};

#endif //READER_H
