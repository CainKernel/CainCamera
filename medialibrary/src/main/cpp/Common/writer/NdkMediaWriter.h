//
// Created by CainHuang on 2020/1/11.
//

#ifndef NDKMEDIAWRITER_H
#define NDKMEDIAWRITER_H

#if defined(__ANDROID__)

#include <Resampler.h>
#include <map>
#include "NdkAudioEncoder.h"
#include "NdkVideoEncoder.h"
#include "MediaWriter.h"
#include "AVMediaMuxer.h"
#include <AVFormatter.h>

/**
 * 基于AMediaCodec硬编码器的媒体写入器
 */
class NdkMediaWriter : MediaWriter {
public:
    NdkMediaWriter();

    virtual ~NdkMediaWriter();

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
    // 打开输出文件
    int openOutputFile();

    // 打开编码器
    int openEncoder(AVMediaType mediaType);

    // 重置所有参数
    void reset();

    // 获取缓冲区大小
    int getBufferSize();

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
    int mAudioBitRate;              // 音频比特率
    AVSampleFormat mSampleFormat;   // 采样格式
    bool mHasAudio;                 // 是否存在音频流数据

    // 编码上下文
    std::shared_ptr<NdkMediaCodecMuxer> mMediaMuxer; // media muxer
    std::shared_ptr<NdkVideoEncoder> mVideoEncoder;  // video encoder
    std::shared_ptr<NdkAudioEncoder> mAudioEncoder;  // audio encoder
    std::shared_ptr<Resampler> mResampler;           // audio resampler

    AVFrame *mImageFrame;           // 视频缓冲帧
    uint8_t *mImageBuffer;          // 视频缓冲区
    int mImageCount;                // 视频数量
    int64_t mStartPts;              // 开始pts
    int64_t mLastPts;               // 上一帧pts
};

#endif /* defined(__ANDROID__) */

#endif //NDKMEDIAWRITER_H
