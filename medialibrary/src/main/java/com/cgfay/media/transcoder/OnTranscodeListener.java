package com.cgfay.media.transcoder;

/**
 * 转码监听器
 */
public interface OnTranscodeListener {

    // 转码开始
    void onTranscodeStart();

    // 正在转码
    void onTranscoding(float duration);

    // 转码完成
    void onTranscodeFinish(boolean success, float duration);

    // 转码出错
    void onTranscodeError(String msg);
}
