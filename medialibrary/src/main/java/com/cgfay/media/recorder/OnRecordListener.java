package com.cgfay.media.recorder;

import com.cgfay.avfoundation.AVMediaType;

/**
 * 录制监听器, MediaRecorder内部使用
 * @author CainHuang
 * @date 2019/6/30
 */
interface OnRecordListener {

    // 录制开始
    void onRecordStart(AVMediaType type);

    // 录制进度
    void onRecording(AVMediaType type, long duration);

    // 录制完成
    void onRecordFinish(RecordInfo info);
}
