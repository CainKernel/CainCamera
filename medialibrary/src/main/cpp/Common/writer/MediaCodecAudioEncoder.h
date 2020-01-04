//
// Created by CainHuang on 2020-01-02.
//

#ifndef MEDIACODECAUDIOENCODER_H
#define MEDIACODECAUDIOENCODER_H


#if defined(__ANDROID__)

#include <AVMediaHeader.h>
#include <AVMediaData.h>

#include <media/NdkMediaFormat.h>
#include <media/NdkMediaMuxer.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaError.h>

#include "MediaCodecEncoder.h"
#include "MediaCodecProfileLevel.h"

#define AUDIO_MIME_TYPE "audio/mp4a-latm"
#define BUFFER_SIZE 8192
#define ENCODE_TIMEOUT -1

/**
 * 音频编码器
 */
class MediaCodecAudioEncoder : public MediaCodecEncoder {
public:
    MediaCodecAudioEncoder(int bitrate, int sampleRate, int channelCount);

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
    AMediaFormat *mMediaFormat;
    AMediaCodec *mMediaCodec;
    AMediaMuxer *mMediaMuxer;
    bool mMuxerStarted;

    int mBitrate;
    int mSampleRate;
    int mChannelCount;
    const char *mOutputPath;
    int mFd;
    ssize_t mAudioTrackId;
    int mTotalBytesRead;
    double mPresentationTimeUs;
    int mBufferSize;

};

#endif /* defined(__ANDROID__) */

#endif //MEDIACODECAUDIOENCODER_H
