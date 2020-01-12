//
// Created by CainHuang on 2020/1/8.
//

#ifndef MEDIAFRAMEPROVIDER_H
#define MEDIAFRAMEPROVIDER_H

#include <Thread.h>
#include <reader/AVMediaReader.h>
#include <AVMediaHeader.h>
#include <AVMediaData.h>
#include <AVFormatter.h>
#include <SafetyQueue.h>
#include <YuvData.h>

// 定义最大缓存帧
#define MAX_FRAME_KEEP 30

/**
 * 媒体帧提供者
 */
class MediaFrameProvider : public Runnable, OnDecodeListener {
public:
    MediaFrameProvider();

    virtual ~MediaFrameProvider();

    // 设置输入文件路径
    void setDataSource(const char *path);

    // 设置起始位置
    void setStart(float start);

    // 设置结束为止
    void setEnd(float end);

    // 准备数据
    int prepare();

    // 开始提供媒体帧数据
    void start();

    // 取消提供媒体帧数据
    void cancel();

    // 释放所有资源
    void release();

    // AVFrame 数据帧解码回调
    void onDecodedFrame(AVFrame *frame, AVMediaType type) override;

    // 是否需要等待
    bool isDecodeWaiting() override;

    void run() override;

    // 获取音频队列
    SafetyQueue<AVMediaData *> *getAudioQueue();

    // 获取视频队列
    SafetyQueue<AVMediaData *> *getVideoQueue();
private:
    // 音频重采样
    int resampleAudio(AVFrame *frame);

    // 视频转码
    int convertVideo(AVFrame *frame);

    // 填充YuvData数据到AVMediaData对象中
    void fillVideoData(AVMediaData *model, YuvData *yuvData, int width, int height);
private:
    Thread *mThread;
    AVMediaReader *mMediaReader;
    SafetyQueue<AVMediaData *> *mVideoFrameQueue;
    SafetyQueue<AVMediaData *> *mAudioFrameQueue;
    bool abortRequest;

    // 帧最大缓存数量
    int mMaxFrameKeep;
    float mStart;
    float mEnd;
};


#endif //MEDIAFRAMEPROVIDER_H
