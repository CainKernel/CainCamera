package com.cgfay.media.recorder;

/**
 * 录制监听器, MediaRecorder内部使用
 * @author CainHuang
 * @date 2019/6/30
 */
interface OnRecordListener {

    // 录制开始
    void onRecordStart(MediaType type);

    // 录制进度
    void onRecording(MediaType type, long duration);

    // 录制完成
    void onRecordFinish(RecordInfo info);
}
