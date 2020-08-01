package com.cgfay.cavfoundation.reader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.cavfoundation.AVAssetTrack;
import com.cgfay.cavfoundation.AVMediaType;

import java.util.HashMap;

/**
 * 媒体读取轨道输出
 */
public class AVAssetReaderTrackOutput implements AVAssetReaderOutput {

    /**
     * 媒体类型
     */
    private AVMediaType mMediaType;

    /**
     * 是否总是复制解码数据
     */
    private boolean mAlwaysCopiesSampleData;

    /**
     * 是否支持随机访问
     */
    private boolean mSupportsRandomAccess;

    /**
     * 绑定的轨道
     */
    @NonNull
    private AVAssetTrack mTrack;

    /**
     * 输出设置参数
     */
    @Nullable
    private HashMap<String, Object> mOutputSettings;

    public AVAssetReaderTrackOutput(@NonNull AVAssetTrack track) {
        this(track, null);
    }

    public AVAssetReaderTrackOutput(@NonNull AVAssetTrack track, @Nullable HashMap<String, Object> outputSettings) {
        mTrack = track;
        mOutputSettings = outputSettings;
        mMediaType = track.getMediaType();
        mAlwaysCopiesSampleData = false;
        mSupportsRandomAccess = false;
    }

    /**
     * 获取轨道对象
     */
    @NonNull
    public AVAssetTrack getTrack() {
        return mTrack;
    }

    /**
     * 媒体类型
     */
    public AVMediaType getMediaType() {
        return mMediaType;
    }

    /**
     * 是否复制数据
     */
    public boolean isAlwaysCopiesSampleData() {
        return mAlwaysCopiesSampleData;
    }

    /**
     * 设置是否复制解码数据
     */
    public void setAlwaysCopiesSampleData(boolean alwaysCopiesSampleData) {
        this.mAlwaysCopiesSampleData = alwaysCopiesSampleData;
    }

    /**
     * 判断是否支持随机访问
     */
    public boolean isSupportsRandomAccess() {
        return mSupportsRandomAccess;
    }

    /**
     * 设置是否支持随机访问
     */
    public void setSupportsRandomAccess(boolean supportsRandomAccess) {
        this.mSupportsRandomAccess = supportsRandomAccess;
    }

    /**
     * 获取输出设置参数
     */
    @Nullable
    public HashMap<String, Object> getOutputSettings() {
        return mOutputSettings;
    }
}
