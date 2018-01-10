package com.cgfay.videoeditor;

/**
 * Created by cain.huang on 2018/1/3.
 */

public interface IMediaRecorder {
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
