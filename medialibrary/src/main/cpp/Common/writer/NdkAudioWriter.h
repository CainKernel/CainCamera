//
// Created by CainHuang on 2020-01-02.
//

#ifndef NDKAUDIOWRITER_H
#define NDKAUDIOWRITER_H


#if defined(__ANDROID__)

#include <media/NdkMediaFormat.h>
#include <media/NdkMediaMuxer.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaError.h>

#include "MediaEncoder.h"
#include "NdkCodecProfileLevel.h"

/**
 * 音频编码器
 */
class NdkAudioWriter : public MediaEncoder {
public:
    NdkAudioWriter(int bitrate, int sampleRate, int channelCount);

    // 设置编码监听器
    void setOnEncodingListener(OnEncodingListener *listener) override;

    // 设置输出路径
    void setOutputPath(const char *path) override;

    // 设置缓冲区大小
    void setBufferSize(int size);

    // 准备编码器
    int prepare() override;

    // 关闭编码器
    int closeEncoder() override;

    // 释放资源
    void release() override;

    // 编码媒体数据
    void encode(AVMediaData *data) override;

private:
    AMediaCodec *mMediaCodec;
    AMediaMuxer *mMediaMuxer;
    bool mMuxerStarted;

    int mBitrate;
    int mSampleRate;
    int mChannelCount;
    const char *mOutputPath;
    ssize_t mAudioTrackId;
    int mTotalBytesRead;
    double mPresentationTimeUs;
    int mBufferSize;

};

#endif /* defined(__ANDROID__) */

#endif //NDKAUDIOWRITER_H
