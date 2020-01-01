//
// Created by CainHuang on 2019/8/17.
//

#ifndef FFMEDIARECORDER_H
#define FFMEDIARECORDER_H

#include <AVMediaHeader.h>
#include <SafetyQueue.h>
#include <AVMediaData.h>
#include <Thread.h>
#include <AVFormatter.h>
#include <AVFrameFilter.h>
#include <writer/AVMediaWriter.h>
#include <YuvConvertor.h>
#include "RecordParams.h"

/**
 * 录制监听器
 */
class OnRecordListener {
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

class FFMediaRecorder : public Runnable {
public:
    FFMediaRecorder();

    virtual ~FFMediaRecorder();

    // 设置录制监听器
    void setOnRecordListener(OnRecordListener *listener);

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

    RecordParams *getRecordParams();

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mRecordThread;
    OnRecordListener *mRecordListener;
    SafetyQueue<AVMediaData *> *mFrameQueue;
    bool mAbortRequest; // 停止请求
    bool mStartRequest; // 开始录制请求
    bool mExit;         // 完成退出标志

    RecordParams *mRecordParams;    // 录制参数

    YuvConvertor *mYuvConvertor;    // Yuv转换器
    AVFrameFilter *mFrameFilter;    // AVFilter处理;
    AVMediaWriter *mMediaWriter;    // 媒体文件写入
};


#endif //FFMEDIARECORDER_H
