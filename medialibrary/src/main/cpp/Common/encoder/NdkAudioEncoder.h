//
// Created by CainHuang on 2020-01-09.
//

#ifndef NDKAUDIOENCODER_H
#define NDKAUDIOENCODER_H

#if defined(__ANDROID__)

#include <media/NdkMediaFormat.h>
#include <media/NdkMediaMuxer.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaError.h>

#include "writer/NdkCodecProfileLevel.h"
#include "NdkMediaEncoder.h"

class NdkAudioEncoder : public NdkMediaEncoder {
public:
    NdkAudioEncoder(const std::shared_ptr<NdkMediaCodecMuxer> &mediaMuxer);

    virtual ~NdkAudioEncoder();

    // 设置音频参数
    void setAudioParams(int bitrate, int sampleRate, int channelCount);

    // 设置缓冲区大小
    void setBufferSize(int size);

    // 准备编码器
    int openEncoder() override;

    // 关闭编码器
    int closeEncoder() override;

    // 释放资源
    void release() override;

    // 编码媒体数据
    int encodeMediaData(AVMediaData *mediaData, int *gotFrame) override;

private:
    int mBitrate;
    int mSampleRate;
    int mChannelCount;
    int mTotalBytesRead;
    double mPresentationTimeUs;
    int mBufferSize;
};

#endif /* defined(__ANDROID__) */

#endif //NDKAUDIOENCODER_H
