package com.cgfay.pushlibrary;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import static com.cgfay.pushlibrary.IAudioCallback.AUDIO_RECORD_ERROR_CREATE_FAILED;
import static com.cgfay.pushlibrary.IAudioCallback.AUDIO_RECORD_ERROR_GET_MIN_BUFFER_SIZE_NOT_SUPPORT;
import static com.cgfay.pushlibrary.IAudioCallback.AUDIO_RECORD_ERROR_SAMPLERATE_NOT_SUPPORT;
import static com.cgfay.pushlibrary.IAudioCallback.AUDIO_RECORD_ERROR_UNKNOWN;

/**
 * Created by cain on 2018/1/21.
 */

public class AudioRecorder implements Runnable {

    private int mAudioSize;

    // 是否停止线程
    private boolean mStop = false;

    private AudioRecord mAudioRecord = null;
    /** 采样率 */
    private int mSampleRate = 44100;
    private IAudioCallback mMediaRecorder;

    public AudioRecorder(IAudioCallback mediaRecorder) {
        this.mMediaRecorder = mediaRecorder;
    }

    /** 设置采样率 */
    public void setSampleRate(int sampleRate) {
        this.mSampleRate = sampleRate;
    }

    @Override
    public void run() {
        if (mSampleRate != 8000 && mSampleRate != 16000 && mSampleRate != 22050
                && mSampleRate != 44100) {
            mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_SAMPLERATE_NOT_SUPPORT,
                    "sampleRate not support.");
            return;
        }

        mAudioSize = AudioRecord.getMinBufferSize(mSampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (AudioRecord.ERROR_BAD_VALUE == mAudioSize) {
            mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_GET_MIN_BUFFER_SIZE_NOT_SUPPORT,
                    "parameters are not supported by the hardware.");
            return;
        }

        mAudioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, mSampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mAudioSize);
        if (null == mAudioRecord) {
            mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_CREATE_FAILED,
                    "new AudioRecord failed.");
            return;
        }
        try {
            mAudioRecord.startRecording();
        } catch (IllegalStateException e) {
            mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_UNKNOWN,
                    "startRecording failed.");
            return;
        }

        byte[] sampleBuffer = new byte[2048];

        try {
            while (!mStop) {
                int result = mAudioRecord.read(sampleBuffer, 0, sampleBuffer.length);
                if (result > 0) {
                    mMediaRecorder.receiveAudioData(sampleBuffer, result);
                }
            }
        } catch (Exception e) {
            String message = "";
            message = e.getMessage();
            mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_UNKNOWN, message);
        }

        mAudioRecord.release();
        mAudioRecord = null;
    }

    /**
     * 停止音频录制
     */
    public void stopRecord() {
        mStop = true;
    }
}
