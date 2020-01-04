package com.cgfay.media.recorder;

import android.opengl.EGLContext;

/**
 * 视频参数
 * @author CainHuang
 * @date 2019/6/30
 */
public class VideoParams {

    // 录制视频的类型
    public static final String MIME_TYPE = "video/avc";

    // 帧率
    public static final int FRAME_RATE = 25;

    // I帧时长
    public static final int I_FRAME_INTERVAL = 1;

    /**
     * 16*1000 bps：可视电话质量
     * 128-384 * 1000 bps：视频会议系统质量
     * 1.25 * 1000000 bps：VCD质量（使用MPEG1压缩）
     * 5 * 1000000 bps：DVD质量（使用MPEG2压缩）
     * 8-15 * 1000000 bps：高清晰度电视（HDTV） 质量（使用H.264压缩）
     * 29.4  * 1000000 bps：HD DVD质量
     * 40 * 1000000 bps：蓝光光碟质量（使用MPEG2、H.264或VC-1压缩）
     */
//    public static final int BIT_RATE = 15 * 1000000;
    // 与抖音相同的视频比特率
    public static final int BIT_RATE = 6693560; // 1280 * 720
    public static final int BIT_RATE_LOW = 3921332; // 576 * 1024

    private int mVideoWidth;
    private int mVideoHeight;
    private int mBitRate;
    private String mVideoPath;
    private SpeedMode mSpeedMode;
    private long mMaxDuration; // us
    private EGLContext mEglContext;

    public VideoParams() {
        mBitRate = BIT_RATE;
        mSpeedMode = SpeedMode.MODE_NORMAL;
    }

    @Override
    public String toString() {
        return "VideoParams: " + mVideoWidth + "x" + mVideoHeight + "@" + mBitRate +
                " to " + mVideoPath;
    }

    public VideoParams setVideoPath(String fileName) {
        this.mVideoPath = fileName;
        return this;
    }

    public String getVideoPath() {
        return mVideoPath;
    }

    public VideoParams setVideoSize(int width, int height) {
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        if (mVideoWidth * mVideoHeight < 1280 * 720) {
            mBitRate = BIT_RATE_LOW;
        }
        return this;
    }

    public VideoParams setVideoWidth(int width) {
        this.mVideoWidth = width;
        return this;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public VideoParams setVideoHeight(int height) {
        this.mVideoHeight = height;
        return this;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public VideoParams setBitRate(int bitRate) {
        this.mBitRate = bitRate;
        return this;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public VideoParams setSpeedMode(SpeedMode mode) {
        this.mSpeedMode = mode;
        return this;
    }

    public SpeedMode getSpeedMode() {
        return mSpeedMode;
    }

    public VideoParams setMaxDuration(long maxDuration) {
        this.mMaxDuration = maxDuration;
        return this;
    }

    public long getMaxDuration() {
        return mMaxDuration;
    }

    public VideoParams setEglContext(EGLContext context) {
        this.mEglContext = context;
        return this;
    }

    public EGLContext getEglContext() {
        return mEglContext;
    }
}
