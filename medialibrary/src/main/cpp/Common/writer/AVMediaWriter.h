//
// Created by CainHuang on 2019/8/11.
//

#ifndef AVMEDIAWRITER_H
#define AVMEDIAWRITER_H

#include <map>
#include <Resampler.h>
#include "MediaWriter.h"
#include "AVMediaMuxer.h"
#include "AVAudioEncoder.h"
#include "AVVideoEncoder.h"
#include "../AVFormatter.h"

/**
 * 媒体写入器
 */
class AVMediaWriter : public MediaWriter {
public:
    AVMediaWriter();

    virtual ~AVMediaWriter();

    // 设置输出文件
    void setOutputPath(const char *dstUrl) override;

    // 设置是否使用时间戳形式
    void setUseTimeStamp(bool use) override;

    // 添加编码参数
    void addEncodeOptions(std::string key, std::string value);

    // 指定音频编码器
    void setAudioEncoderName(const char *encoder);

    // 指定视频编码器
    void setVideoEncoderName(const char *encoder);

    // 设置最大比特率
    void setMaxBitRate(int maxBitRate) override;

    // 设置质量系数
    void setQuality(int quality);

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

    // 填充视频数据
    int fillImage(AVMediaData *data);

    // 填充音频数据
    int fillSample(AVMediaData *data);

    // 重置所有参数
    void reset();

private:

    std::map<std::string, std::string> mEncodeOptions;  // 编码参数
    std::map<std::string, std::string> mVideoMetadata;  // 视频meta数据

    const char *mDstUrl;            // 输出路径

    int mWidth;                     // 视频宽度
    int mHeight;                    // 视频高度
    int mFrameRate;                 // 视频帧率
    AVPixelFormat mPixelFormat;     // 像素格式
    int64_t mMaxBitRate;            // 最大比特率
    const char *mVideoEncodeName;   // 指定视频编码器名称
    bool mUseTimeStamp;             // 是否使用时间戳计算pts
    bool mHasVideo;                 // 是否存在视频流数据

    int mSampleRate;                // 采样率
    int mChannels;                  // 声道数
    int mAudioBitRate;              // 音频比特率
    AVSampleFormat mSampleFormat;   // 采样格式
    const char *mAudioEncodeName;   // 指定音频编码器名称
    bool mHasAudio;                 // 是否存在音频流数据

    // 编码上下文
    std::shared_ptr<AVMediaMuxer> mMediaMuxer;      // media muxer
    std::shared_ptr<AVVideoEncoder> mVideoEncoder;  // video encoder
    std::shared_ptr<AVAudioEncoder> mAudioEncoder;  // audio encoder
    std::shared_ptr<Resampler> mResampler;          // audio resampler

    AVFrame *mImageFrame;           // 视频缓冲帧
    uint8_t *mImageBuffer;          // 视频缓冲区
    int mImageCount;                // 视频数量
    int64_t mStartPts;              // 开始pts
    int64_t mLastPts;               // 上一帧pts

};

#endif //AVMEDIAWRITER_H
