//
// Created by CainHuang on 2019/8/12.
//

#ifndef AVMEDIAREADER_H
#define AVMEDIAREADER_H

#include <string>
#include <map>
#include "AVMediaHeader.h"

/**
 * 读帧监听器
 */
class OnReadListener {
public:
    virtual void onReadFrame(AVFrame *frame, AVMediaType type) = 0;
};

/**
 * 媒体读取器
 */
class AVMediaReader : public Runnable {
public:
    AVMediaReader();

    virtual ~AVMediaReader();

    // 设置数据源
    void setDataSource(const char *url);

    // 设置起始位置
    void setStart(float timeMs);

    // 设置结束位置
    void setEnd(float timeMs);

    // 指定解码格式，对应命令行的 -f 的参数
    void setInputFormat(const char *format);

    // 指定音频解码器名称
    void setAudioDecoder(const char *decoder);

    // 指定视频解码器名称
    void setVideoDecoder(const char *decoder);

    // 添加格式参数
    void addFormatOptions(std::string key, std::string value);

    // 添加解码参数
    void addDecodeOptions(std::string key, std::string value);

    // 设置媒体读取监听器
    void setReadListener(OnReadListener *listener, bool autoRelease);

    // 定位
    void seekTo(float timeMs);

    // 开始读取
    void start();

    // 暂停读取
    void pause();

    // 继续读取
    void resume();

    // 停止读取
    void stop();

    void run() override ;

private:
    // 重置所有参数
    void reset();

    // 释放所有参数
    void release();

    // 打开输入文件
    int openInputFile();

    // 打开解码器
    int openDecoder(AVFormatContext *formatCtx, AVCodecContext **codecCtx, AVMediaType type);

    // 解码数据包
    void decodePacket(AVPacket *packet, OnReadListener *listener);

    // 读取数据包
    int readPackets();

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mThread;

    const char *mSrcPath;           // 媒体文件路径
    OnReadListener *mReadListener;  // 读取监听器
    bool mAutoRelease;              // 是否自动释放监听器

    AVInputFormat *iformat;         // 指定文件封装格式，也就是解复用器
    std::map<std::string, std::string> mFormatOptions;  // 格式参数
    std::map<std::string, std::string> mDecodeOptions;  // 解码参数

    const char *mVideoDecoder;      // 视频解码器名称
    const char *mAudioDecoder;      // 音频解码器名称

    AVFormatContext *pFormatCtx;    // 解复用上下文
    AVCodecContext *pAudioCodecCtx; // 音频解码上下文
    AVCodecContext *pVideoCodecCtx; // 视频解码上下文
    int mAudioIndex;                // 音频流索引
    int mVideoIndex;                // 视频流索引

    int mWidth;                     // 视频宽度
    int mHeight;                    // 视频高度
    AVPixelFormat mPixelFormat;     // 像素格式
    int mFrameRate;                 // 帧率
    int mSampleRate;                // 采样率

    int64_t mDuration;              // 文件总时长

    float mStart;                   // 起始时间
    float mEnd;                     // 结束时间

    int seekRequest;                // 定位请求
    int seekFlags;                  // 定位标志
    int64_t seekPos;                // 定位位置
    int64_t seekRel;                // 定位偏移

    bool abortRequest;              // 终止请求
    bool pauseRequest;              // 暂停请求

};


#endif //AVMEDIAREADER_H
