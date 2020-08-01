package com.cgfay.cavfoundation.reader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.cavfoundation.AVAudioMix;
import com.cgfay.cavfoundation.AVAssetTrack;
import com.cgfay.cavfoundation.AVMediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * todo 读取混音输出实现处理逻辑
 */
public class AVAssetReaderAudioMixOutput implements AVAssetReaderOutput {

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
     * 音频轨道列表
     */
    @NonNull
    private List<AVAssetTrack> mAudioTracks = new ArrayList<>();

    /**
     * 混音描述对象
     */
    @Nullable
    private AVAudioMix mAudioMix;

    /**
     * 音频输出参数
     */
    @Nullable
    private HashMap<String, Object> mAudioSettings;

    public AVAssetReaderAudioMixOutput(@NonNull List<AVAssetTrack> audioTracks) {

    }

    public AVAssetReaderAudioMixOutput(@NonNull List<AVAssetTrack> audioTracks,
                                       @Nullable HashMap<String, Object> settings) {
        mAudioTracks.clear();
        mAudioTracks.addAll(audioTracks);
        mAudioSettings = settings;
        mMediaType = AVMediaType.AVMediaTypeAudio;
        mAlwaysCopiesSampleData = true;
        mSupportsRandomAccess = false;
    }

    /**
     * 获取需要混音的音频轨道列表
     */
    @NonNull
    public List<AVAssetTrack> getAudioTracks() {
        return mAudioTracks;
    }

    /**
     * 获取混音描述对象
     */
    @Nullable
    public AVAudioMix getAudioMix() {
        return mAudioMix;
    }

    /**
     * 设置混音描述对象
     */
    public void setAudioMix(@Nullable AVAudioMix audioMix) {
        mAudioMix = audioMix;
    }

    /**
     * 媒体类型
     */
    @Nullable
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
     * 获取音频设置参数
     */
    @Nullable
    public HashMap<String, Object> getAudioSettings() {
        return mAudioSettings;
    }
}
