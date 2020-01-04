package com.cgfay.media.recorder;

/**
 * 录制状态监听
 * @author CainHuang
 * @date 2019/6/30
 */
public interface OnRecordStateListener {

    // 录制开始
    void onRecordStart();

    // 录制进度
    void onRecording(long duration);

    // 录制结束
    void onRecordFinish(RecordInfo info);
}
