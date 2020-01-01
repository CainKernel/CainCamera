//
// Created by CainHuang on 2019/9/15.
//

#ifndef MEDIACODECRECORDER_H
#define MEDIACODECRECORDER_H

#if defined(__ANDROID__)

#include <AVMediaHeader.h>
#include <AVMediaData.h>
#include <SafetyQueue.h>
#include <YuvConvertor.h>
#include <writer/MediaCodecWriter.h>
#include "RecordConfig.h"

/**
 * 录制监听器
 */
class OnMediaRecordListener {
public:
    // 录制器准备完成回调
    virtual void onRecordStart() = 0;
    // 正在录制
    virtual void onRecording(float duration) = 0;
    // 录制完成回调
    virtual void onRecordFinish(bool success, float) = 0;
    // 录制出错回调
    virtual void onRecordError(const char *msg) = 0;
};

/**
 * 使用NdkMediaCodec进行录制
 */
class MediaCodecRecorder : public Runnable {
public:
    MediaCodecRecorder();

    virtual ~MediaCodecRecorder();

    // 设置录制监听器
    void setOnRecordListener(OnMediaRecordListener *listener);

    // 准备录制器
    int prepare();

    // 释放资源
    void release();

    // 录制媒体数据
    int recordFrame(AVMediaData *data);

    // 开始录制
    void startRecord();

    // 停止录制
    void stopRecord();

    // 是否正在录制
    bool isRecording();

    void run() override;

    RecordConfig *getRecordConfig();

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mRecordThread;
    OnMediaRecordListener *mRecordListener;
    SafetyQueue<AVMediaData *> *mFrameQueue;
    bool mAbortRequest; // 停止请求
    bool mStartRequest; // 开始录制请求
    bool mExit;         // 完成退出标志

    RecordConfig *mRecordConfig;        // 录制参数配置

    YuvConvertor *mYuvConvertor;        // Yuv转换器
    MediaCodecWriter *mMediaWriter;   // 媒体文件写入器
};

#endif  /* defined(__ANDROID__) */

#endif //MEDIACODECRECORDER_H
