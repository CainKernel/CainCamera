//
// Created by CainHuang on 2019/8/11.
//

#ifndef AVFRAMEFILTER_H
#define AVFRAMEFILTER_H

#include "AVMediaHeader.h"
#include "AVMediaData.h"

/**
 * AVFrame过滤器
 */
class AVFrameFilter {
public:
    AVFrameFilter();

    virtual ~AVFrameFilter();

    // 设置视频输入参数
    void setVideoInput(int width, int height, AVPixelFormat pixelFormat, int frameRate,
                       const char *filter);

    // 设置视频输出参数
    void setVideoOutput(AVPixelFormat format);

    // 设置音频输入参数
    void setAudioInput(int sampleRate, int channels, AVSampleFormat sampleFormat,
                       const char *filter);

    // 设置音频输出参数
    void setAudioOutput(int sampleRate, int channels, AVSampleFormat sampleFormat);

    // 初始化AVFilter
    int initFilter();

    // 初始化AVFilter
    int initFilter(AVMediaType type);

    // 过滤媒体数据
    int filterData(AVMediaData *mediaData);

    // 过滤一帧数据
    AVFrame *filterFrame(AVFrame *frame, AVMediaType type);

    // 释放所有资源
    void release();

private:
    // 初始化视频过滤器
    int initVideoFilter();

    // 初始化音频过滤器
    int initAudioFilter();

    // 过滤视频
    int filterVideo(AVMediaData *mediaData);

    // 过滤音频
    int filterAudio(AVMediaData *mediaData);

    // 过滤视频帧
    AVFrame *filterVideo(AVFrame *frame);

    // 过滤音频帧
    AVFrame *filterAudio(AVFrame *frame);

    void freeFrame(AVFrame *frame);
private:
    int mWidth;                             // 视频宽度
    int mHeight;                            // 视频高度
    int mFrameRate;                         // 视频帧率
    AVPixelFormat mInPixelFormat;           // 视频输入像素格式
    AVPixelFormat mOutPixelFormat;          // 视频输出像素格式
    const char *mVideoFilter;               // 视频过滤描述
    bool mVideoEnable;                      // 是否允许视频过滤

    AVFilterContext *mVideoBuffersinkCtx;   // AVFilter输出端
    AVFilterContext *mVideoBuffersrcCtx;    // AVFilter输入端
    AVFilterGraph *mVideoFilterGraph;       // FilterGraph

    int mInSampleRate;                      // 输出采样率
    int mInChannels;                        // 输入声道数
    int mOutSampleRate;                     // 输出采样率
    int mOutChannels;                       // 输出声道数
    AVSampleFormat mInSampleFormat;         // 输入采样格式
    AVSampleFormat mOutSampleFormat;        // 输出采样格式
    const char *mAudioFilter;               // 音频AVFilter描述
    bool mAudioEnable;                      // 是否允许音频过滤

    AVFilterContext *mAudioBuffersinkCtx;   // AVFilter输出端
    AVFilterContext *mAudioBuffersrcCtx;    // AVFilter输入端
    AVFilterGraph *mAudioFilterGraph;       // FilterGraph

};

#endif //AVFRAMEFILTER_H
