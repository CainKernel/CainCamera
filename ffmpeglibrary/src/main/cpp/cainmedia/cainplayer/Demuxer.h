//
// Created by Administrator on 2018/3/21.
//

#ifndef CAINCAMERA_DEMUXER_H
#define CAINCAMERA_DEMUXER_H

#define MAX_PACKET_COUNT 200

#ifdef __cplusplus
extern "c" {
#endif

#include <libavformat/avformat.h>
#include "Mutex.h"
#include "Thread.h"

#ifdef __cplusplus
};
#endif

#include "AudioDecoder.h"
#include "VideoDecoder.h"

class Demuxer {

public:
    Demuxer(VideoDecoder *videoDecoder, AudioDecoder *audioDecoder);
    virtual ~Demuxer();
    // 设置开始位置
    void setStartTime(int64_t startTime);
    // 定位
    void streamSeek(int64_t pos);
    // 打开文件
    int open(const char *filename);
    // 开始解复用
    void start();
    // 停止解复用
    void stop();
    // 暂停解复用
    void paused();
    // 通知
    void notify();
    // 解复用
    void demux();



private:
    // 解复用线程
    static int demuxThread(void *arg);

private:
    // 文件名
    char *fileName;
    // 解复用上下文
    AVFormatContext *pFormatCtx;
    bool mPrepared;     // 等待开始状态
    bool mAbortRequest; // 停止状态标志
    bool mPaused;       // 暂停状态标志
    bool mLastPaused;   // 上一次暂停状态
    bool mOpenSuccess;  // 打开状态标志
    bool mSeekRequest;  // 定位标志
    bool mReadFinish;   // 读取结束标志

    int64_t mStartTime;

    Mutex *mMutex;
    Cond *mCondition;
    Thread *mThread;

    AudioDecoder *mAudioDecoder;
    VideoDecoder *mVideoDecoder;
};


#endif //CAINCAMERA_DEMUXER_H
