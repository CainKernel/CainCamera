package com.cgfay.cavfoundation.reader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.cavfoundation.AVAssetTrack;
import com.cgfay.cavfoundation.AVMediaType;
import com.cgfay.cavfoundation.AVVideoComposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 媒体读取器视频输出
 */
public class AVAssetReaderVideoCompositionOutput implements AVAssetReaderOutput {

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
     * 视频轨道列表
     */
    private List<AVAssetTrack> mVideoTracks = new ArrayList<>();

    /**
     * 视频描述对象
     */
    private AVVideoComposition mVideoComposition;

    /**
     * 视频输出参数
     */
    @Nullable
    private final HashMap<String, Object> mVideoSettings;

    public AVAssetReaderVideoCompositionOutput(@NonNull List<AVAssetTrack> videoTracks) {
        this(videoTracks, null);
    }

    public AVAssetReaderVideoCompositionOutput(@NonNull List<AVAssetTrack> videoTracks, @Nullable HashMap<String, Object> settings) {
        mVideoTracks.clear();
        mVideoTracks.addAll(videoTracks);
        mVideoSettings = settings;
        mMediaType = AVMediaType.AVMediaTypeVideo;
        mAlwaysCopiesSampleData = false;
        mSupportsRandomAccess = false;
    }

    /**
     * 获取渲染指令描述对象
     */
    @Nullable
    public AVVideoComposition getVideoComposition() {
        return mVideoComposition;
    }

    /**
     * 设置渲染指令描述对象
     */
    public void setVideoComposition(@Nullable AVVideoComposition videoComposition) {
        mVideoComposition = videoComposition;
    }

    /**
     * 获取视频轨道列表
     */
    @NonNull
    public List<AVAssetTrack> getVideoTracks() {
        return mVideoTracks;
    }

    /**
     * 媒体类型
     */
    @Override
    public AVMediaType getMediaType() {
        return mMediaType;
    }

    /**
     * 是否复制数据
     */
    @Override
    public boolean isAlwaysCopiesSampleData() {
        return mAlwaysCopiesSampleData;
    }

    /**
     * 设置是否复制解码数据
     */
    @Override
    public void setAlwaysCopiesSampleData(boolean alwaysCopiesSampleData) {
        mAlwaysCopiesSampleData = alwaysCopiesSampleData;
    }

    /**
     * 设置是否支持随机访问
     */
    @Override
    public void setSupportsRandomAccess(boolean supportsRandomAccess) {
        mSupportsRandomAccess = supportsRandomAccess;
    }

    /**
     * 判断是否支持随机访问
     */
    @Override
    public boolean isSupportsRandomAccess() {
        return mSupportsRandomAccess;
    }

    /**
     * 获取视频输出参数
     */
    @Nullable
    public HashMap<String, Object> getVideoSettings() {
        return mVideoSettings;
    }

}
