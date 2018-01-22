package com.cgfay.pushlibrary;

/**
 * 推流器状态回调接口
 * Created by cain on 2018/1/22.
 */

public interface MediaPusherListener {
    // 推流器准备好
    void onPrepared(MediaPusher pusher);
    // 推流器已经开始
    void onStarted(MediaPusher pusher);
    // 推流器已经停止
    void onStopped(MediaPusher pusher);
    // 推流器完全释放
    void onReleased(MediaPusher pusher);
}
