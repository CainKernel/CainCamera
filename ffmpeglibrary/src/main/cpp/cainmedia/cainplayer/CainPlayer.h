//
// Created by Administrator on 2018/3/21.
//

#ifndef CAINCAMERA_CAINPLAYER_H
#define CAINCAMERA_CAINPLAYER_H

#include <jni.h>
#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#ifdef __cplusplus
extern "C" {
#endif

#include "libavformat/avformat.h"
#include "libavfilter/avfiltergraph.h"
#include "libavfilter/buffersink.h"
#include "libswresample/swresample.h"

#ifdef __cplusplus
};
#endif
#include "VideoDecoder.h"
#include "AudioDecoder.h"
#include "Demuxer.h"

class CainPlayer {
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

private:
    char *fileName;                 // 路径
    ANativeWindow *mWindow;         // ANativeWidow
    Demuxer *mDemuxer;              // 解复用器
    VideoDecoder *mVideoDecoder;    // 视频解码器
    AudioDecoder *mAudioDecoder;    // 音频解码器
};

#endif //CAINCAMERA_CAINPLAYER_H
