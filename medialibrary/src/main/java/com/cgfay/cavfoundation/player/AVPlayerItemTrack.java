package com.cgfay.cavfoundation.player;

import androidx.annotation.Nullable;

import com.cgfay.cavfoundation.AVAssetTrack;

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
     * 指示轨道四否允许在播放阶段展示
     */
    private boolean enabled;

    /**
     * 当前视频帧率, fps
     */
    private float mVideoFrameRate;

    public AVPlayerItemTrack(@Nullable AVAssetTrack assetTrack, float videoFrameRate) {
        mAssetTrack = assetTrack;
        enabled = (mAssetTrack != null);
        this.mVideoFrameRate = videoFrameRate;
    }

    @Nullable
    public AVAssetTrack getAssetTrack() {
        return mAssetTrack;
    }

    public boolean isEnabled() {
        return enabled;
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
