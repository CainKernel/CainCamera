package com.cgfay.cavfoundation.codec;

import android.media.MediaCodecInfo;

/**
 * 视频参数
 */
public class CAVVideoInfo {

    // 非法轨道索引
    public static final int INVALID_TRACK = -1;

    // H264 mime
    public static final String MIME_AVC = "video/avc";
    // H265 mime
    public static final String MIME_HEVC = "video/hevc";

    // 默认编码参数
    public static final int DEFAULT_WIDTH = 720;
    public static final int DEFAULT_HEIGHT = 1280;
    public static final int DEFAULT_FRAME_RATE = 30;
    public static final int DEFAULT_BITRATE = 6693560;

    // 低端机编码参数
    public static final int LOW_WIDTH = 576;
    public static final int LOW_HEIGHT = 1024;
    public static final int LOW_BITRATE = 3921332;

    // 轨道索引
    private int mTrackIndex;
    // 视频宽度
    private int mWidth;
    // 视频高度
    private int mHeight;
    // 视频帧率
    private int mFrameRate;
    // gop大小
    private int mGopSize;
    // 比特率
    private int mBitRate;
    // profile参数
    private int mProfile;
    // level参数
    private int mLevel;
    // 媒体类型
    private String mMimeType;

    public CAVVideoInfo() {
        mTrackIndex = INVALID_TRACK;
        mMimeType = MIME_AVC;
        mWidth = DEFAULT_WIDTH;
        mHeight = DEFAULT_HEIGHT;
        mFrameRate = DEFAULT_FRAME_RATE;
        mGopSize = DEFAULT_FRAME_RATE;
        mBitRate = DEFAULT_BITRATE;
        mProfile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh;
        mLevel = MediaCodecInfo.CodecProfileLevel.AVCLevel31;
    }

    public int getTrackIndex() {
        return mTrackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        mTrackIndex = trackIndex;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    public int getGopSize() {
        return mGopSize;
    }

    public void setGopSize(int gopSize) {
        mGopSize = gopSize;
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

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

}
