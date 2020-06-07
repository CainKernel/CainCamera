package com.cgfay.cavfoundation;

import androidx.annotation.Nullable;

/**
 * 播放器轨道
 */
public class AVPlayerItemTrack {

    /**
     * 视频轨道
     */
    @Nullable
    private AVAssetTrack mAssetTrack;

    /**
     * 当前视频帧率, fps
     */
    private float mVideoFrameRate;

    public AVPlayerItemTrack(@Nullable AVAssetTrack assetTrack, float videoFrameRate) {
        mAssetTrack = assetTrack;
        this.mVideoFrameRate = videoFrameRate;
    }

    @Nullable
    public AVAssetTrack getAssetTrack() {
        return mAssetTrack;
    }

    /**
     * 设置视频帧率
     */
    public void setCurrentVideoFrameRate(float frameRate) {
        mVideoFrameRate = frameRate;
    }

    public float getCurrentVideoFrameRate() {
        return mVideoFrameRate;
    }
}
