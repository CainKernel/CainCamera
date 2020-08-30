package com.cgfay.cavfoundation.capture;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.codec.AudioInfo;

/**
 * 音频读取接口
 */
public interface CAVCaptureAudioInput {

    /**
     * 准备读取器
     */
    void prepare();

    /**
     * 是否已经初始化
     */
    boolean isInitialized();

    /**
     * 开始读取
     */
    void start();

    /**
     * 停止读取
     */
    void stop();

    /**
     * 释放资源
     */
    void release();

    /**
     * 读取音频PCM数据
     * @param pcmData   pcm数据
     * @param length    最大录制长度
     * @return          读取到的数据长度
     */
    int readAudio(byte[] pcmData, int length);

    /**
     * 输入音频参数
     */
    @NonNull
    AudioInfo getAudioInfo();

    /**
     * 是否同步到视频流时钟
     */
    boolean isSyncToVideo();
}
