package com.cgfay.cavfoundation.capture;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.codec.CAVAudioInfo;

/**
 * 录音输入
 */
public class CAVCaptureAudioRecordInput implements CAVCaptureAudioInput {

    private static final String TAG = "CAVAudioRecordReader";

    private CAVAudioInfo mAudioInfo;
    private AudioRecord mAudioRecord;
    private int mBufferSize;

    public CAVCaptureAudioRecordInput(@NonNull CAVAudioInfo info) {
        mAudioInfo = info;
        mBufferSize = 0;
    }

    /**
     * 准备录制
     */
    @Override
    public void prepare() {
        Log.d(TAG, "prepare: ");
        if (mAudioRecord != null) {
            mAudioRecord.release();
        }
        int channelConfig = mAudioInfo.getChannelCount() == 1
                ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        int audioSource = MediaRecorder.AudioSource.MIC;
        mBufferSize = AudioRecord.getMinBufferSize(mAudioInfo.getSampleRate(), channelConfig,
                mAudioInfo.getAudioFormat());
        mAudioRecord = new AudioRecord(audioSource, mAudioInfo.getSampleRate(), channelConfig,
                mAudioInfo.getAudioFormat(), mBufferSize);
    }

    /**
     * 是否已经初始化
     */
    @Override
    public boolean isInitialized() {
        if (mAudioRecord != null) {
            Log.d(TAG, "isInitialized: " + mAudioRecord.getState() + ", record state: " + mAudioRecord.getRecordingState());
            return (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED)
                    && (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING);
        }
        return false;
    }

    /**
     * 开始读取
     */
    @Override
    public void start() {
        Log.d(TAG, "start: ");
        if (mAudioRecord != null) {
            mAudioRecord.startRecording();
        }
    }

    /**
     * 停止读取
     */
    @Override
    public void stop() {
        Log.d(TAG, "stop: ");
        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        Log.d(TAG, "release: ");
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
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
        Log.d(TAG, "readAudio: " + size);
        if (mAudioRecord != null) {
            return mAudioRecord.read(pcmData, 0, size);
        }
        return 0;
    }

    /**
     * 输入音频参数
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
        return false;
    }
}
