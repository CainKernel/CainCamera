package com.cgfay.ffmpeglibrary.MediaRecorder;

/**
 * 录制状态监听器
 */
public interface RecordStateListener {

    // 录制出错
    void onRecordError();

    // 已经准备好
    void onRecordPrepared();

    // 录制开始
    void onRecordStarted();

    // 录制停止
    void onRecordStopped();

    // 录制器释放
    void onRecordReleased();
}
