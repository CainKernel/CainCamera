//
// Created by CainHuang on 2020-01-09.
//

#ifndef AVVIDEOENCODER_H
#define AVVIDEOENCODER_H

#include "AVMediaEncoder.h"

/**
 * 视频编码器
 */
class AVVideoEncoder : public AVMediaEncoder {

public:
    AVVideoEncoder(const std::shared_ptr<AVMediaMuxer> &mediaMuxer);

    virtual ~AVVideoEncoder();

    // 设置视频参数
    void setVideoParams(int width, int height, AVPixelFormat pixelFormat, int frameRate,
            int maxBitRate, bool useTimeStamep, std::map<std::string, std::string> metadata);

    AVMediaType getMediaType() override;

    AVCodecID getCodecId() override;
};


#endif //AVVIDEOENCODER_H
