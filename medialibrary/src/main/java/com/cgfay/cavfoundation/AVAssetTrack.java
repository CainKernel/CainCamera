package com.cgfay.cavfoundation;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeMapping;
import com.cgfay.coremedia.AVTimeRange;
import com.cgfay.coremedia.AVTimeRangeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体轨道接口
 */
public class AVAssetTrack<T extends AVAssetTrackSegment> {

    /**
     * 非法轨道ID
     */
    public static final int kTrackIDInvalid = -1;

    /**
     * 源媒体数据
     */
    @Nullable
    protected AVAsset mAsset;

    /**
     * 源数据Uri路径
     */
    @Nullable
    protected Uri mUri;

    /**
     * 轨道ID
     */
    protected int mTrackID;

    /**
     * 媒体类型
     */
    protected AVMediaType mMediaType;

    /**
     * 轨道的时间区间
     */
    @NonNull
    protected AVTimeRange mTimeRange;

    /**
     * 视频帧大小
     */
    @NonNull
    protected CGSize mNaturalSize;

    /**
     * 时间刻度，如果是视频，则采用默认的600，如果是音频，则采用输入的采样率
     */
    protected int mNaturalTimeScale;

    /**
     * 转换对象，比如90度、270度等转换
     */
    @NonNull
    protected AffineTransform mPreferredTransform;

    /**
     * 默认音量
     */
    protected float mPreferredVolume;

    /**
     * 是否需要timestamps重排序
     */
    protected boolean mFrameReordering;

    /**
     * 轨道片段列表
     */
    protected List<T> mTrackSegments = new ArrayList<>();

    AVAssetTrack() {
        mAsset = null;
        mUri = null;
        mTrackID = kTrackIDInvalid;
        mTimeRange = AVTimeRange.kAVTimeRangeInvalid;
        mNaturalSize = CGSize.kSizeZero;
        mPreferredTransform = new AffineTransform().idt();
        mPreferredVolume = 1.0f;
        mFrameReordering = false;
    }

    public AVAssetTrack(@NonNull AVAsset asset, @NonNull Uri uri, int trackID,
                           @NonNull AVMediaType type, @NonNull AVTimeRange timeRange) {
        this(asset, uri, trackID, type, timeRange, CGSize.kSizeZero);
    }

    public AVAssetTrack(@NonNull AVAsset asset, @NonNull Uri uri, int trackID,
                           @NonNull AVMediaType type, @NonNull AVTimeRange timeRange,
                           @NonNull CGSize size) {
        mAsset = asset;
        mUri = uri;
        mTrackID = trackID;
        mMediaType = type;
        mTimeRange = timeRange;
        mNaturalSize = size;
        mPreferredTransform = new AffineTransform().idt();
        mPreferredVolume = 1.0f;
        mFrameReordering = false;
        // 创建片段信息
        AVTimeMapping mapping = new AVTimeMapping(timeRange, timeRange);
        AVAssetTrackSegment segment = new AVAssetTrackSegment(mapping);
        // 判断是否轨道id是否合法，合法则不为空
        segment.setEmpty(mTrackID == kTrackIDInvalid);
        mTrackSegments.add((T) segment);
    }

    /**
     * 获取包含某个时间的轨道片段
     */
    @Nullable
    public T segmentForTrackTime(@NonNull AVTime time) {
        if (time.equals(AVTime.kAVTimeInvalid)) {
            return null;
        }
        T result = null;
        for (T segment : mTrackSegments) {
            if (AVTimeRangeUtils.timeRangeContainsTime(segment.getTimeMapping().getTarget(),
                    time)) {
                result = segment;
                break;
            }
        }
        return result;
    }

    /**
     * 获取源媒体对象
     */
    @Nullable
    public AVAsset getAsset() {
        return mAsset;
    }

    /**
     * 获取Uri
     */
    @Nullable
    public Uri getUri() {
        return mUri;
    }

    /**
     * 获取当前轨道的ID
     */
    public int getTrackID() {
        return mTrackID;
    }

    /**
     * 获取当前轨道的媒体类型
     */
    public AVMediaType getMediaType() {
        return mMediaType;
    }

    /**
     * 获取当前轨道的时间区间
     */
    @NonNull
    public AVTimeRange getTimeRange() {
        return mTimeRange;
    }

    /**
     * 获取帧大小
     */
    @NonNull
    public CGSize getNaturalSize() {
        return mNaturalSize;
    }

    /**
     * 获取轨道刻度
     */
    public int getNaturalTimeScale() {
        return mNaturalTimeScale;
    }

    /**
     * 获取转换对象
     */
    @NonNull
    public AffineTransform getPreferredTransform() {
        return mPreferredTransform;
    }

    /**
     * 获取默认音量
     */
    public float getPreferredVolume() {
        return mPreferredVolume;
    }

    /**
     * 判断是否需要重拍时间戳
     */
    public boolean isFrameReordering() {
        return mFrameReordering;
    }

    /**
     * 获取轨道片段
     * @return 轨道片段列表
     */
    @NonNull
    public List<? extends AVAssetTrackSegment> getTrackSegments() {
        return mTrackSegments;
    }

}
