//
// Created by cain on 2018/11/25.
//

#ifndef CAINCAMERA_AVMUSICPLAYER_H
#define CAINCAMERA_AVMUSICPLAYER_H


#include <cstdint>
#include "PlayerCallback.h"
#include "AVAudioDecoder.h"
#include "PlayerStatus.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/time.h>
};

class AVMusicPlayer {
public:
    AVMusicPlayer();

    virtual ~AVMusicPlayer();

    // 设置数据源
    void setDataSource(const char *url);

    // 设置播放器回调
    void setPlayerCallback(PlayerCallback *callback);

    // 准备
    void prepare();

    // 准备解码器
    void prepareDecoder();

    // 退出准备线程
    void exitPrepareThread();

    // 开始
    void start();

    // 暂停
    void pause();

    // 启动
    void resume();

    // 停止
    void stop();

    // 释放资源
    void release();

    // 设置是否循环播放
    void setLooping(bool looping);

    // 定位
    void seek(int64_t seconds);

    // 设置音量
    void setVolume(int percent);

    // 设置声道
    void setChannelType(int channelType);

    // 设置节拍
    void setPitch(float pitch);

    // 设置速度
    void setSpeed(float speed);

    // 设置节拍
    void setTempo(float tempo);

    // 设置速度改变
    void setSpeedChange(double speedChange);

    // 设置节拍改变
    void setTempoChange(double tempoChange);

    // 设置八度音调节
    void setPitchOctaves(double pitchOctaves);

    // 设置半音调节
    void setPitchSemiTones(double semiTones);

    // 采样率
    int getSampleRate();

    // 是否退出
    bool isExit();

    // 获取时长
    int getDuration();

    // 是否播放状态
    bool isPlaying();

    // 播放音乐
    void playMusic(void);

private:

    PlayerCallback *mCallback;
    const char *url;
    pthread_t mPrepareThread;   // 准备线程
    pthread_t mPlayThread;      // 播放线程
    pthread_mutex_t mMutex;     // 操作锁
    pthread_cond_t mCondition;  // 条件变量
    pthread_mutex_t mSeekMutex; // 定位锁

    bool mPrepared; // 是否已经准备
    bool mStarted;  // 是否已经开始
    bool looping;   // 是否循环播放
    bool exit;      // 是否退出
    int mDuration;  // 时长

    AVFormatContext *pFormatCtx;
    AVAudioDecoder *audioDecoder;
    PlayerStatus *mPlayerStatus;

};


#endif //CAINCAMERA_AVMUSICPLAYER_H
