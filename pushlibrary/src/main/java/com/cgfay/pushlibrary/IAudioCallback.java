package com.cgfay.pushlibrary;

/**
 * 音频接口
 * Created by cain on 2018/1/21.
 */

public interface IAudioCallback {

    int AUDIO_RECORD_ERROR_UNKNOWN = 0;
    /**
     * 采样率设置不支持
     */
    int AUDIO_RECORD_ERROR_SAMPLERATE_NOT_SUPPORT = 1;
    /**
     * 最小缓存获取失败
     */
    int AUDIO_RECORD_ERROR_GET_MIN_BUFFER_SIZE_NOT_SUPPORT = 2;
    /**
     * 创建AudioRecord失败
     */
    int AUDIO_RECORD_ERROR_CREATE_FAILED = 3;

    /**
     * 音频错误
     *
     * @param what 错误类型
     * @param message
     */
    public void onAudioError(int what, String message);
    /**
     * 接收音频数据
     *
     * @param sampleBuffer 音频数据
     * @param len
     */
    public void receiveAudioData(byte[] sampleBuffer, int len);
}
