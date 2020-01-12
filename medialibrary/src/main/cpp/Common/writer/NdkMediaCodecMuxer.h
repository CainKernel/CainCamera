//
// Created by CainHuang on 2020/1/12.
//

#ifndef NDKMEDIACODERMUXER_H
#define NDKMEDIACODERMUXER_H

#include <cstdint>

#if defined(__ANDROID__)

#include <media/NdkMediaCodec.h>
#include <media/NdkMediaMuxer.h>

#include "AVMediaHeader.h"

class NdkMediaCodecMuxer {
public:
    NdkMediaCodecMuxer();

    virtual ~NdkMediaCodecMuxer();

    // 设置输出路径
    void setOutputPath(const char *path);

    // 设置是否存在音频
    void setHasAudio(bool hasAudio);

    // 设置是否存在视频
    void setHasVideo(bool hasVideo);

    // 打开复用器
    int openMuxer();

    // 开始
    void start();

    // 关闭复用器
    void closeMuxer();

    // 添加轨道
    int addTrack(AMediaFormat *mediaFormat);

    // 写入编码数据
    int writeFrame(size_t trackId, uint8_t *encodeData, const AMediaCodecBufferInfo *info);

    // 复用器是否开始
    bool isStart();

private:
    const char *mPath;
    AMediaMuxer *mMediaMuxer;
    bool mMuxerStarted;
    bool mHasAudio;
    bool mHasVideo;
    int startTimes; // 记录开始复用器的次数
};

#endif /* defined(__ANDROID__) */

#endif //NDKMEDIACODERMUXER_H
