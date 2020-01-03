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

//    public static final int BIT_RATE = 96000;
    // 与抖音相同的音频比特率
    public static final int BIT_RATE = 128000;

    private int mSampleRate;    // 采样率
    private int mChannel;       // 采样声道
    private int mBitRate;       // 比特率
    private int mAudioFormat;   // 采样格式

    SpeedMode mSpeedMode;       // 速度模式
    private String mAudioPath;  // 文件名
    private long mMaxDuration;  // 最大时长

    public AudioParams() {
        mSampleRate = SAMPLE_RATE;
        mSpeedMode = SpeedMode.MODE_NORMAL;
        mChannel =  AudioFormat.CHANNEL_IN_STEREO;
        mBitRate = BIT_RATE;
        mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    }

    public void setSampleRate(int sampleRate) {
        this.mSampleRate = sampleRate;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void setChannel(int channel) {
        mChannel = channel;
    }

    public int getChannel() {
        return mChannel;
    }

    public void setBitRate(int bitRate) {
        mBitRate = bitRate;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public void setAudioFormat(int audioFormat) {
        mAudioFormat = audioFormat;
    }

    public int getAudioFormat() {
        return mAudioFormat;
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
