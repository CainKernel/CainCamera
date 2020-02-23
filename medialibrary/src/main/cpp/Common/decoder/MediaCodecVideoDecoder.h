//
// Created by CainHuang on 2020/1/12.
//

#ifndef MEDIACODECVIDEODECODER_H
#define MEDIACODECVIDEODECODER_H

#if defined(__ANDROID__)

#include <jni.h>

#include "decoder/AVMediaDecoder.h"

extern "C" {
#include <libavutil/hwcontext.h>
#include <libavcodec/mediacodec.h>
};

class MediaCodecVideoDecoder : public AVMediaDecoder {
public:
    MediaCodecVideoDecoder(const std::shared_ptr<AVMediaDemuxer> &mediaDemuxer);

    void setDecoder(const char *name) override;

    // 打开解码器
    int openDecoder(std::map<std::string, std::string> decodeOptions) override;

    // 解码一个数据包
    int decodePacket(AVPacket *packet, OnDecodeListener *listener, int *gotFrame) override;

    // 获取媒体类型
    AVMediaType getMediaType() override;

    // 初始化媒体数据
    void initMetadata() override;

    // 渲染输出帧
    void renderFrame(AVFrame *frame);

private:
    int mWidth;
    int mHeight;
    AVPixelFormat mPixelFormat;
    int mFrameRate;
};

#endif /* defined(__ANDROID__) */

#endif //MEDIACODECVIDEODECODER_H
