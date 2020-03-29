//
// Created by CainHuang on 2020-03-21.
//

#ifndef TIMESTAMP_H
#define TIMESTAMP_H

#include <mutex>
/**
 * 同步类型
 */
enum SyncType {
    sync_audio,     // 同步到音频时钟
    sync_video,     // 同步到视频时钟
    sync_external,  // 同步到外部时钟
};

/**
 * 播放器音视频时间戳时间戳
 */
class Timestamp {
public:
    Timestamp();

    Timestamp(SyncType mSyncType);

    virtual ~Timestamp();

    // 设置同步类型，默认同步到音频时钟
    void setSyncType(SyncType type);

    // 设置音频时钟
    void setAudioTime(float time);

    // 设置视频时钟
    void setVideoTime(float time);

    // 设置外部时钟
    void setExtClockTime(float time);

    // 获取时钟
    float getClock();

    // 获取视频时钟
    float getVideoClock();
private:
    // 更新时间戳
    void update();

private:
    mutable std::mutex mutex;
    SyncType mSyncType; // 同步类型
    float mAudioClock;  // 音频时钟
    float mVideoClock;  // 视频时钟
    float mExtClock;    // 外部时钟
    float mMainClock;   // 主时钟
};


#endif //TIMESTAMP_H
