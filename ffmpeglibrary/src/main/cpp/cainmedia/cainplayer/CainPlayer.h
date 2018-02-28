//
// Created by cain on 2018/2/8.
//

#ifndef CAINCAMERA_CAINPLAYER_H
#define CAINCAMERA_CAINPLAYER_H

#include "CainPlayerDefinition.h"

#include <assert.h>

#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include "native_log.h"
#include "Thread.h"
#include "Mutex.h"
#include "PacketQueue.h"
#include "FrameQueue.h"
#include "Clock.h"
#include "Decoder.h"

// 刷新频率
#define REFRESH_RATE 0.01

class CainPlayer {

private:
    // 文件名
    char *filename;
    // 显示窗口
    ANativeWindow *nativeWindow;
    // 当前播放位置
    int currentPosition;
    // 视频时长
    int videoLength;
    // 是否循环播放
    bool looping;
    // 已经准备
    bool prepared;
    // 是否处于暂停状态
    bool paused;
    // 上一次停止状态
    bool lastPaused;
    // 是否处于停止状态
    bool stopped;
    // 是否静音
    bool muted;
    // 播放速度，默认为1.0f
    float playbackRate = 1.0f;
    // 是否倒放
    bool reversed;
    // 窗口宽度
    int displayWidth;
    // 窗口高度
    int displayHeight;
    // 视频宽度
    int videoWidth;
    // 视频高度
    int videoHeight;
    // 查找/定位请求
    bool seekRequest;
    // 定位位置
    int seekPos;
    int seekRel;
    int seekFlags;
    // 附着请求
    bool queue_attachments_req;

    // 开始时间
    int64_t start_time;
    // 帧间隔
    int64_t duration = AV_NOPTS_VALUE;
    // 是否实时流
    bool realtime;
    // 读文件结束标志
    bool eof;
    // 无限缓冲区
    int infiniteBuffer = -1;
    // 是否自动退出
    bool autoexit;

    // 封装格式上下文
    AVFormatContext *ic;

    // 视频流索引
    int videoStreamIdx;
    // 视频流
    AVStream *videoStream;
    // 最大显示时长
    double maxFrameDuration;
    // 是否需要强制刷新
    bool forceRefresh;

    // 音频索引
    int audioStreamIdx;
    // 音频流
    AVStream *audioStream;
    // 输入采样格式
    AVSampleFormat inSampleFmt;
    // 输出采样格式
    AVSampleFormat outSampleFmt;
    // 输入采样率
    int inSampleRate;
    // 输出采样率
    int outSampleRate;
    // 声道数
    int channels;
    // 输入声道格式
    uint64_t inChannelLayout;
    // 输出声道格式
    uint64_t outChannelLayout;
    // 音频重采样上下文
    SwrContext *swr_ctx;
    // 下一音频帧的pts
    int64_t audioNextPts = AV_NOPTS_VALUE;
    // 下一音频帧的timebase
    AVRational audioNextPtsTimebase;

    // 解封装线程
    Thread *readThread;
    // 读线程条件锁
    Cond *readCondition;
    // 视频刷新线程
    Thread *videoRefreshThread;

    // 音频时钟
    Clock *audioClock;
    // 视频时钟
    Clock *videoClock;
    // 外部时钟
    Clock *externClock;

    // 音频解码器
    Decoder *audioDecoder;
    // 视频解码器
    Decoder *videoDecoder;

    // 同步类型（音频同步、视频同步）
    int syncType;
    // 步进，主要用于seek时，调到下一帧使用
    int step;

    // 帧计时器
    double frame_timer;
    // 是否需要舍弃帧
    int framedrop = -1;

    // 音频裸数据队列
    PacketQueue *audioQueue;
    // 音频帧队列
    FrameQueue *audioFrameQueue;
    // 视频裸数据队列
    PacketQueue *videoQueue;
    // 视频帧队列
    FrameQueue *videoFrameQueue;

public:
    CainPlayer();
    ~CainPlayer();
    // 设置数据源
    void setDataSource(const char *path);
    // 设置surface
    void setSurface(JNIEnv *env, jobject surface);
    // 获取当前进度
    int getCurrentPosition(void);
    // 得到媒体时长
    int getDuration(void);
    // 是否循环播放
    bool isLooping(void);
    // 是否正在播放
    bool isPlaying(void);
    // 是否处于停止状态
    bool isStopped(void);
    // 暂停
    void pause(void);
    // 开始
    void start(void);
    // 停止
    void stop(void);
    // 异步装载流媒体
    void prepare(void);
    // 重置所有状态
    void reset(void);
    // 回收资源
    void release(void);
    // 指定播放位置
    void seekTo(int msec);
    // 设置是否单曲循环
    void setLooping(bool loop);
    // 设置是否倒放
    void setReverse(bool reverse);
    // 设置是否播放声音
    void setPlayAudio(bool play);
    // 设置播放的速度
    void setPlaybackRate(float playbackRate);
    // 输入大小发生改变
    void changedSize(int width, int height);

    // 解复用(读线程执行实体)
    int demux(void);
    // 音频解码(音频解码线程执行实体)
    int decodeAudio(void);
    // 视频解码(视频解码线程执行实体)
    int decodeVideo(void);
    // 刷新画面(视频刷新线程执行实体)
    int refreshVideo(void);

private:
    // 判断是否实时流
    static bool isRealtime(AVFormatContext *s);
    // 读文件线程句柄
    static int readThreadHandle(void * arg);
    // 音频解码线程句柄
    static int audioThreadHandle(void *arg);
    // 视频解码线程句柄
    static int videoThreadHandle(void * arg);
    // 视频刷新线程句柄
    static int videoDisplayThreadHandle(void *arg);

    // 判断是否有足够的包
    int hasEnoughPackets(AVStream *st, int streamIndex, PacketQueue *queue);
    // 定位媒体流
    void streamSeek(int64_t pos, int64_t rel, int seek_by_bytes);

    // 打开媒体流
    int openStream(void);
    // 打开媒体流
    int openStream(int streamIndex);
    // 关闭媒体流
    int closeStream(void);
    // 关闭媒体流
    int closeStream(int streamIndex);

    // 获取解码视频帧
    int getVideoFrame(AVFrame *frame);
    // 刷新画面
    void refreshVideo(double *remaining_time);
    // 显示画面
    void videoDisplay(void);
    // 获取上一帧的显示时长
    double getLastDisplayDuration(Frame *vp, Frame *nextvp);
    // 计算目标延时
    double calculateTargetDelay(double delay);
    // 更新视频的pts
    void updateVideoPts(double pts, int64_t pos, int serial);

    // 获取主同步类型
    int getMasterSyncType(void);
    //  获取主时钟
    double getMasterClock(void);
    // 检查外部时钟速度
    void checkExternalClockSpeed(void);
};

#endif //CAINCAMERA_CAINPLAYER_H
