//
// Created by admin on 2018/4/29.
//

#ifndef CAINPLAYER_AVMEDIAPLAYER_H
#define CAINPLAYER_AVMEDIAPLAYER_H


#include <android/native_window.h>
#include "pthread.h"
#include "../decoder/AVDecoder.h"
#include "../common/MediaJniCall.h"
#include "../decoder/AVAudioDecoder.h"
#include "../decoder/AVVideoDecoder.h"
#include "../common/MediaStatus.h"
#include "../common/MediaStream.h"
#include "AndroidLog.h"
#include "../synchronizer/AVSynchronizer.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <libavformat/avformat.h>

#ifdef __cplusplus
}
#endif

class AVMediaPlayer {
public:
    AVMediaPlayer(MediaJniCall *jniCall, const char *urlpath);

    virtual ~AVMediaPlayer();

    // 设置数据源
    void setDataSource(const char *dataSource);

    // 设置Surface
    void setSurface(ANativeWindow *window);

    // 准备解码器
    int prepare();

    // 开始播放
    int start();

    // 定位
    int seek(int64_t sec);

    // 初始化解码上下文
    int createCodecContext(AVCodecParameters *parameters, AVDecoder *decoder);

    // 释放资源
    void release();

    // 暂停
    void pause();

    // 再启动
    void resume();

    // 停止
    void stop();

    // 音频通道
    void setAudioStream(int id);

    // 音频流索引
    void setVideoStream(int id);

    // 获取音频流数量
    int getAudioStreams();

    // 获取视频宽度
    int getVideoWidth();

    // 获取视频高度
    int getVideoHeight();

    // 是否处于退出状态
    bool isExit();

    // 获取时长
    int64_t getDuration();

    // 解复用
    int demuxFile();

private:
    // 解码中断回调
    static int avformat_interrupt_cb(void *ctx);
    // 解复用线程句柄
    static int demuxThread(void *context);

    // 所管理器方法，用于多线程解码
    static int lockmgr(void **mtx, enum AVLockOp op);

    MediaJniCall *mediaJniCall;
    AVAudioDecoder *audioDecoder;
    AVVideoDecoder *videoDecoder;
    AVSynchronizer *synchronizer;
    MediaStatus *mediaStatus;

    char *fileName;
    AVFormatContext *pFormatCtx;
    int64_t duration;
    bool exit;
    bool exitByUser;

    std::deque<MediaStream*> audioStreams;
    std::deque<MediaStream*> videoStreams;

    pthread_mutex_t mMutex;
    pthread_mutex_t mSeekMutex;

    // 解复用线程
    Thread *mThread;

    ANativeWindow *nativeWindow;
};

#endif //CAINPLAYER_AVMEDIAPLAYER_H
