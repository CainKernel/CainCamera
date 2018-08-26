/*
 * Copyright (c) 2003 Fabrice Bellard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * 本代码根据ffmpeg的muxing.c修改而来
 * Created by cain on 2018/1/6.
 */

#ifndef CAINCAMERA_FFMPEG_ENCODER_H
#define CAINCAMERA_FFMPEG_ENCODER_H

#include "CommonRecorder.h"
#include "AndroidLog.h"
#include "../openmax/OMX_IVCommon.h"

class CainEncoder {

public:
    CainEncoder();
    ~CainEncoder();

    // 设置输出路径
    void setOutputFile(const char *path);
    // 设置视频大小
    void setVideoSize(int width, int height);
    // 设置视频帧率
    void setVideoFrameRate(int frameRate);
    // 设置视频码率
    void setVideoBitRate(long long bitRate);
    // 设置视频颜色格式
    void setVideoColorFormat(OMX_COLOR_FORMATTYPE fmt);
    // 设置是否允许音频编码
    void setEnableAudioEncode(bool enable);
    // 设置音频采样频率
    void setAudioSampleRate(int sampleRate);
    // 设置音频码率
    void setAudioBitRate(int bitRate);
    // 初始化编码器
    bool initEncoder();
    // 视频编码
    status_t videoEncode(uint8_t *data);
    // 音频编码
    status_t audioEncode(uint8_t *data, int len);
    // 编码结束
    status_t stopEncode();
    // 释放资源
    bool release();
    // 获取缓冲大小
    int getAudioEncodeSize();

private:
    bool isInited;
    AVOutputFormat *fmt;
    AVFormatContext *fmt_ctx;
    AVCodec *audio_codec;
    AVCodec *video_codec;
    bool have_video;
    bool have_audio;

    // 视频编码流
    OutputStream video_st;
    // 音频编码流
    OutputStream audio_st;
    AVPacket video_pkt, audio_pkt;
    // 输出文件
    char mOutputFile[MAX_STRING_PATH_LEN];
    // 视频宽度
    int mWidth;
    // 视频高度
    int mHeight;
    // 帧率
    int mFrameRate;
    // 码率
    long long mBitRate;
    // 是否允许音频编码
    int enableAudio;
    // 音频码率
    int mAudioBitRate;
    // 音频采样率
    int mAudioSampleRate;
    // 音频采样大小
    int audioSampleSize;
    // 颜色格式
    OMX_COLOR_FORMATTYPE mColor;
    // 像素格式
    AVPixelFormat mPixFmt;
    // 重置参数
    void reset();
    // 添加码流
    bool addStream(OutputStream *ost, AVFormatContext *oc, AVCodec **codec,
                   enum AVCodecID codec_id);
    // 打开音频编码器
    bool openAudio(AVCodec *codec, OutputStream *ost, AVDictionary *opt_arg);
    // 创建音频帧
    AVFrame *allocAudioFrame(int channels, enum AVSampleFormat sample_fmt, uint64_t channel_layout,
                             int sample_rate, int frame_size);
    // 打开视频编码器
    bool openVideo(AVCodec *codec, OutputStream *ost, AVDictionary *opt_arg);
    // 创建视频帧
    AVFrame *allocVideoFrame(enum AVPixelFormat pix_fmt, int width, int height);
    // 关闭码流
    void closeStream(AVFormatContext *oc, OutputStream *ost);
    // 写入帧
    int writeFrame(AVFormatContext *fmt_ctx, const AVRational *time_base, AVStream *st,
                   AVPacket *pkt);

    CainEncoder(const CainEncoder&);
    CainEncoder&operator=(const CainEncoder&);

};
#endif //CAINCAMERA_FFMPEG_ENCODER_H
