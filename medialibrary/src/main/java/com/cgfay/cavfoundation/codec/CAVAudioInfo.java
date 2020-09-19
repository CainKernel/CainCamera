package com.cgfay.cavfoundation.codec;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;

/**
 * 音频参数
 */
public class CAVAudioInfo {

    // 非法轨道索引
    public static final int INVALID_TRACK = -1;

    public static final String AUDIO_MIME = "audio/mp4a-latm";
    // default audio bit rate
    public static final int AUDIO_BIT_RATE = 128000;
    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    public static final int DEFAULT_SAMPLE_RATE = 44100;
    // default channel count
    public static final int DEFAULT_CHANNEL_COUNT = 2;

    // 轨道索引
    private int mTrack;
    // 采样率
    private int mSampleRate;
    // 声道数
    private int mChannelCount;
    // 声道格式
    private int mAudioFormat;
    // 比特率
    private int mBitRate;
    // 编码profile
    private int mProfile;
    // 音频mime类型
    private String mMimeType;

    public CAVAudioInfo() {
        mTrack = INVALID_TRACK;
        mSampleRate = DEFAULT_SAMPLE_RATE;
        mChannelCount = DEFAULT_CHANNEL_COUNT;
        mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
        mBitRate = AUDIO_BIT_RATE;
        mProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
        mMimeType = AUDIO_MIME;
    }

    public int getTrack() {
        return mTrack;
    }

    public void setTrack(int track) {
        mTrack = track;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
    }

    public int getChannelCount() {
        return mChannelCount;
    }

    public void setChannelCount(int channelCount) {
        mChannelCount = channelCount;
    }

    public int getAudioFormat() {
        return mAudioFormat;
    }

    public void setAudioFormat(int audioFormat) {
        mAudioFormat = audioFormat;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public void setBitRate(int bitRate) {
        mBitRate = bitRate;
    }

    public int getProfile() {
        return mProfile;
    }

    public void setProfile(int profile) {
        mProfile = profile;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

}
