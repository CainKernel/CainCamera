package com.cgfay.cavfoundation.capture;

import android.media.AudioFormat;
import android.media.AudioRecord;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.codec.CAVAudioInfo;

/**
 * 静音输入
 */
public class CAVCaptureAudioMuteInput implements CAVCaptureAudioInput {

    private CAVAudioInfo mAudioInfo;

    private int mBufferSize;

    public CAVCaptureAudioMuteInput(@NonNull CAVAudioInfo audioInfo) {
        mAudioInfo = audioInfo;
    }

    /**
     * 准备读取器
     */
    @Override
    public void prepare() {
        int channelConfig = mAudioInfo.getChannelCount() == 1
                ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        mBufferSize = AudioRecord.getMinBufferSize(mAudioInfo.getSampleRate(), channelConfig,
                mAudioInfo.getAudioFormat());
    }

    /**
     * 是否已经初始化
     */
    @Override
    public boolean isInitialized() {
        return true;
    }

    /**
     * 开始读取
     */
    @Override
    public void start() {
        // do nothing
    }

    /**
     * 停止读取
     */
    @Override
    public void stop() {
        // do nothing
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        // do nothing
    }

    /**
     * 读取音频PCM数据
     *
     * @param pcmData pcm数据
     * @param length  最大录制长度
     * @return 读取到的数据长度
     */
    @Override
    public int readAudio(byte[] pcmData, int length) {
        int size = Math.min(mBufferSize, length);
        for (int i = 0; i < size; i++) {
            pcmData[i] = (byte)0;
        }
        return size;
    }

    /**
     * 音频参数
     */
    @NonNull
    @Override
    public CAVAudioInfo getAudioInfo() {
        return mAudioInfo;
    }

    /**
     * 是否同步到视频流时钟
     */
    @Override
    public boolean isSyncToVideo() {
        return true;
    }
}
