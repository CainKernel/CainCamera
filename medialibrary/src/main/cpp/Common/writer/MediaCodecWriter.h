//
// Created by CainHuang on 2019/9/15.
//

#ifndef MEDIACODECWRITER_H
#define MEDIACODECWRITER_H

#if defined(__ANDROID__)

#include "MediaWriter.h"

#include <media/NdkMediaFormat.h>
#include <media/NdkMediaMuxer.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaError.h>

#define VIDEO_MIME_TYPE "video/avc"
#define AUDIO_MIME_TYPE "audio/mp4a-latm"



class MediaCodecWriter : public MediaWriter {
public:
    MediaCodecWriter();

    virtual ~MediaCodecWriter();

    // 设置输出文件
    void setOutputPath(const char *dstUrl) override;

    // 设置是否使用时间戳形式
    void setUseTimeStamp(bool use) override;

    // 设置最大比特率
    void setMaxBitRate(int maxBitRate) override;

    // 设置视频输出参数
    void setOutputVideo(int width, int height, int frameRate, AVPixelFormat pixelFormat) override;

    // 设置音频输出参数
    void setOutputAudio(int sampleRate, int channels, AVSampleFormat sampleFormat) override;

    // 准备编码器
    int prepare() override;

    // 编码媒体数据
    int encodeMediaData(AVMediaData *mediaData) override;

    // 编码媒体数据
    int encodeMediaData(AVMediaData *mediaData, int *gotFrame) override;

    // 编码一帧数据
    int encodeFrame(AVFrame *frame, AVMediaType type) override;

    // 编码一帧数据
    int encodeFrame(AVFrame *frame, AVMediaType type, int *gotFrame) override;

    // 停止写入
    int stop() override;

    // 释放资源
    void release() override;

private:
    // 重置所有资源
    void reset();

    // 刷新缓冲区
    void flush();

    // 打开音频编码器
    int openAudioEncoder();

    // 打开视频编码器
    int openVideoEncoder();

    // 关闭编码器
    int closeEncoder(AMediaCodec *codec);

    // 计算时间戳
    uint64_t computePresentationTime();

    // 查找起始码
    int avcFindStartCode(uint8 *data, int offset, int end);

    // 查找起始码
    int findStartCode(uint8 *data, int offset, int end);

private:

    const char *mDstUrl;            // 输出路径
    int mWidth;                     // 视频宽度
    int mHeight;                    // 视频高度
    int mFrameRate;                 // 视频帧率
    AVPixelFormat mPixelFormat;     // 像素格式
    int64_t mMaxBitRate;            // 最大比特率
    bool mUseTimeStamp;             // 是否使用时间戳计算pts
    bool mHasVideo;                 // 是否存在视频流数据

    int mSampleRate;                // 采样率
    int mChannels;                  // 声道数
    AVSampleFormat mSampleFormat;   // 采样格式
    bool mHasAudio;                 // 是否存在音频流数据

    AMediaMuxer *mMediaMuxer;       // 媒体复用器
    AMediaCodec *mVideoCodec;       // 视频编码器
    AMediaCodec *mAudioCodec;       // 音频编码器

    int mFrameIndex;                // 视频索引

};

#endif /* defined(__ANDROID__) */

#endif //MEDIACODECWRITER_H
