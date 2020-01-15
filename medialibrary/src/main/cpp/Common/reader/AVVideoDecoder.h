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

    int getFrameRate();

    AVPixelFormat getFormat();

    double getRotation();

protected:
    void initMetadata() override;

private:
    int mWidth;
    int mHeight;
    AVPixelFormat mPixelFormat;
    int mFrameRate;
};


#endif //AVVIDEODECODER_H
