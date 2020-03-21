//
// Created by CainHuang on 2020-03-21.
//

#include "Timestamp.h"

Timestamp::Timestamp() : mSyncType(sync_audio), mAudioClock(-1), mVideoClock(-1),
           mExtClock(-1), mMainClock(0) {
}

Timestamp::Timestamp(SyncType mSyncType) : mSyncType(mSyncType), mAudioClock(-1),
           mVideoClock(-1), mExtClock(-1), mMainClock(0) {
}

Timestamp::~Timestamp() {

}

/**
 * 设置同步类型
 * @param type
 */
void Timestamp::setSyncType(SyncType type) {
    std::lock_guard<std::mutex> lock(mutex);
    mSyncType = type;
    update();
}

/**
 * 设置音频时钟
 * @param time 音频时钟(ms)
 */
void Timestamp::setAudioTime(float time) {
    std::lock_guard<std::mutex> lock(mutex);
    mAudioClock = time;
    update();
}

/**
 * 设置视频时钟
 * @param time 视频时钟
 */
void Timestamp::setVideoTime(float time) {
    std::lock_guard<std::mutex> lock(mutex);
    mVideoClock = time;
    update();
}

/**
 * 设置外部时钟
 * @param time
 */
void Timestamp::setExtClockTime(float time) {
    std::lock_guard<std::mutex> lock(mutex);
    mExtClock = time;
    update();
}

/**
 * 更新主时钟
 */
void Timestamp::update() {
    switch (mSyncType) {
        // 同步到音频时钟
        case sync_audio: {
            if (mAudioClock < 0) {
                if (mVideoClock >= 0) {
                    mMainClock = mVideoClock;
                } else if (mExtClock >= 0) {
                    mMainClock = mExtClock;
                }
            } else {
                mMainClock = mAudioClock;
            }
            break;
        }

        // 同步到视频时钟
        case sync_video: {
            if (mVideoClock < 0) {
                if (mAudioClock >= 0) {
                    mMainClock = mAudioClock;
                } else if (mExtClock >= 0) {
                    mMainClock = mExtClock;
                }
            } else {
                mMainClock = mVideoClock;
            }
            break;
        }

        // 同步到外部时钟
        case sync_external: {
            if (mExtClock < 0) {
                if (mAudioClock >= 0) {
                    mMainClock = mAudioClock;
                } else if (mVideoClock >= 0) {
                    mMainClock = mVideoClock;
                }
            } else {
                mMainClock = mExtClock;
            }
            break;
        }
    }
}

/**
 * 获取时钟
 */
float Timestamp::getClock() {
    std::lock_guard<std::mutex> lock(mutex);
    update();
    return mMainClock;
}
