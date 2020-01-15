//
// Created by CainHuang on 2020-01-10.
//

#ifndef AVMEDIADEMUXER_H
#define AVMEDIADEMUXER_H

#include <map>
#include "../AVMediaHeader.h"

/**
 * 解复用器
 */
class AVMediaDemuxer {
public:
    AVMediaDemuxer();

    virtual ~AVMediaDemuxer();

    // 设置输入文件
    void setInputPath(const char *path);

    // 设置输入格式
    void setInputFormat(const char *format);

    // 打开解复用器
    int openDemuxer(std::map<std::string, std::string> formatOptions);

    // 定位到某个时间(ms)
    int seekTo(float timeMs);

    // 关闭解复用器
    void closeDemuxer();

    // 打印复用器信息
    void printInfo();

    // 获取总时长
    int64_t getDuration();

    // 获取解复用上下文
    AVFormatContext *getContext();

    bool hasAudioStream();

    bool hasVideoStream();

    // if is live video
    bool isRealTime();

private:
    const char *mInputPath;         // 输入文件路径
    AVInputFormat *iformat;         // 指定文件封装格式，也就是解复用器
    AVFormatContext *pFormatCtx;    // 解复用上下文
    int64_t mDuration;              // 文件总时长
};


#endif //AVMEDIADEMUXER_H
