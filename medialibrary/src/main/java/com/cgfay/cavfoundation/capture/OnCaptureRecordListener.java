package com.cgfay.cavfoundation.capture;

/**
 * 录制监听器
 */
public interface OnCaptureRecordListener {

    /**
     * 开始录制
     */
    void onCaptureStart();

    /**
     * 正在录制
     * @param duration  录制的时长
     */
    void onCapturing(long duration);

    /**
     * 录制回调
     * @param path  视频路径
     */
    void onCaptureFinish(String path, long duration);

}
