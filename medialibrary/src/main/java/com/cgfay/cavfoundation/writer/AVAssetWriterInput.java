package com.cgfay.cavfoundation.writer;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.AVMediaType;
import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;

/**
 * 媒体写入输入器
 */
public class AVAssetWriterInput {

    /**
     * 媒体类型
     */
    private AVMediaType mMediaType;

    /**
     * 写入大小
     */
    private CGSize mNaturalSize;

    /**
     * 仿射变换对象
     */
    @NonNull
    private AffineTransform mTransform;

    /**
     * 写入音量大小
     */
    private float mPreferredVolume;

    /**
     * 媒体时钟刻度
     */
    private int mMediaTimeScale;

    public AVAssetWriterInput(AVMediaType mediaType) {
        this.mMediaType = mediaType;
    }

    /**
     * 媒体类型
     */
    public AVMediaType getMediaType() {
        return mMediaType;
    }

    /**
     * 获取时钟刻度
     */
    public int getMediaTimeScale() {
        return mMediaTimeScale;
    }

    /**
     * 设置时钟刻度
     */
    public void setMediaTimeScale(int mediaTimeScale) {
        mMediaTimeScale = mediaTimeScale;
    }

}
