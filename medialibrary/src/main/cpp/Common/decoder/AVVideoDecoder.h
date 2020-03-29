//
// Created by CainHuang on 2020-01-10.
//

#ifndef AVVIDEODECODER_H
#define AVVIDEODECODER_H


#include "AVMediaDecoder.h"

class AVVideoDecoder : public AVMediaDecoder {

public:
    AVVideoDecoder(const std::shared_ptr<AVMediaDemuxer> &mediaDemuxer);

    virtual ~AVVideoDecoder();

    AVMediaType getMediaType() override;

    int getWidth();

    int getHeight();

    int getRotate();

    int getFrameRate();

    AVPixelFormat getFormat();

protected:
    // 计算出视频的旋转角度
    double calculateRotation();

protected:
    // 初始化视频metadata
    void initMetadata() override;

private:
    int mWidth;
    int mHeight;
    AVPixelFormat mPixelFormat;
    int mFrameRate;
    int mRotate;
};


#endif //AVVIDEODECODER_H
