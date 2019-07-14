package com.cgfay.filter.recorder;

import android.media.AudioFormat;

/**
 * 音频参数
 * @author CainHuang
 * @date 2019/6/30
 */
public class AudioParams {

    public static final String MIME_TYPE = "audio/mp4a-latm";

    public static final int SAMPLE_RATE = 44100;        // 44.1[KHz] is only setting guaranteed to be available on all devices.

    public static final int SAMPLE_PER_FRAME = 1024;    // AAC, bytes/frame/channel

    public static final int FRAMES_PER_BUFFER = 25; 	// AAC, frame/buffer/sec

    public static final int BIT_RATE = 64000;

    public static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;

    public static final int CHANNEL_COUNT = 1;

    public static final int BITS_PER_SAMPLE = AudioFormat.ENCODING_PCM_16BIT;


    private int mSampleRate;    // 采样率
    private int mNbSamples;     // 一帧的采样点数
    SpeedMode mSpeedMode;       // 速度模式
    private String mAudioPath;   // 文件名
    private long mMaxDuration;  // 最大时长

    public AudioParams() {
        mSampleRate = SAMPLE_RATE;
        mNbSamples = SAMPLE_PER_FRAME;
        mSpeedMode = SpeedMode.MODE_NORMAL;
    }

    public void setSampleRate(int sampleRate) {
        this.mSampleRate = sampleRate;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void setNbSamples(int nbSamples) {
        this.mNbSamples = nbSamples;
    }

    public int getNbSamples() {
        return mNbSamples;
    }

    public void setSpeedMode(SpeedMode mode) {
        this.mSpeedMode = mode;
    }

    public SpeedMode getSpeedMode() {
        return mSpeedMode;
    }

    public void setAudioPath(String audioPath) {
        this.mAudioPath = audioPath;
    }

    public String getAudioPath() {
        return mAudioPath;
    }

    public void setMaxDuration(long maxDuration) {
        this.mMaxDuration = maxDuration;
    }

    public long getMaxDuration() {
        return mMaxDuration;
    }

}
