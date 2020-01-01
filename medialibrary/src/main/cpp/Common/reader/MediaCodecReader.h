//
// Created by CainHuang on 2019/9/15.
//

#ifndef MEDIACODECREADER_H
#define MEDIACODECREADER_H

#if defined(__ANDROID__)

#include "MediaReader.h"
#include <media/NdkMediaExtractor.h>
#include <media/NdkMediaCodec.h>

class MediaCodecReader : public MediaReader, Runnable {

public:
    MediaCodecReader();

    virtual ~MediaCodecReader();

    // 设置数据源
    void setDataSource(const char *url) override;

    // 设置起始位置
    void setStart(float timeMs) override;

    // 设置结束位置
    void setEnd(float timeMs) override;

    // 设置媒体读取监听器
    void setReadListener(OnReadListener *listener, bool autoRelease) override;

    // 定位
    void seekTo(float timeMs) override;

    // 开始读取
    void start() override;

    // 暂停读取
    void pause() override;

    // 继续读取
    void resume() override;

    // 停止读取
    void stop() override;

    void run() override;

private:
    // 解码数据
    int decodePacket(AMediaCodec *coec, AVMediaType type, OnReadListener *listener);

private:
    AMediaExtractor *mMediaExtractor;
    AMediaCodec *mVideoCodec;
    AMediaCodec *mAudioCodec;
};

#endif /* defined(__ANDROID__) */

#endif //MEDIACODECREADER_H
