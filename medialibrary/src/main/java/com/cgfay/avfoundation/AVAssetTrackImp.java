package com.cgfay.avfoundation;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体轨道，用于表示音频、视频、字幕等轨道数据
 * 一个轨道对象表示持有的音频、视频、字幕轨道数据
 * 片段列表表示当前轨道需要展示的片段区间。
 */
public class AVAssetTrackImp implements AVAssetTrack {

    /**
     * 轨道所依附的源媒体对象
     */
    private WeakReference<AVAsset> mWeakMediaSource;

    /**
     * 源数据Uri路径
     */
    @NonNull
    private Uri mUri;

    /**
     * 轨道ID
     */
    private int mTrackID;

    /**
     * 媒体类型
     */
    private AVMediaType mMediaType;

    /**
     * 轨道的时间区间
     */
    private AVTimeRange mTimeRange;

    /**
     * 视频帧大小
     */
    private CGSize mNaturalSize;

    /**
     * 转换对象，比如90度、270度等转换
     */
    private AffineTransform mPreferredTransform;

    /**
     * 默认音量
     */
    private float mPreferredVolume;

    /**
     * 是否需要timestamps重排序
     */
    private boolean mFrameReordering;

    /**
     * 轨道片段列表
     */
    private List<AVAssetTrackSegment> mTrackSegments;

    public AVAssetTrackImp(@NonNull AVAsset mediaSource, @NonNull Uri uri) {
        mWeakMediaSource = new WeakReference<>(mediaSource);
        mUri = uri;
        mTrackID = kTrackIDInvalid;
        mTimeRange = AVTimeRange.kAVTimeRangeZero;
        mFrameReordering = false;
        mNaturalSize = CGSize.kSizeZero;
        mPreferredTransform = AffineTransform.kAffineTransformIdentity;
        mPreferredVolume = 1.0f;
        mFrameReordering = false;
        mTrackSegments = new ArrayList<>();
    }

    public AVAssetTrackImp(@NonNull AVAsset source, @NonNull Uri uri, int trackID, @NonNull AVMediaType type,
                           @NonNull AVTimeRange timeRange) {
        mWeakMediaSource = new WeakReference<>(source);
        mUri = uri;
        mTrackID = trackID;
        mMediaType = type;
        mTimeRange = timeRange;
        mNaturalSize = CGSize.kSizeZero;
        mPreferredTransform = AffineTransform.kAffineTransformIdentity;
        mPreferredVolume = 1.0f;
        mFrameReordering = false;
        mTrackSegments = new ArrayList<>();
    }

    /**
     * 获取某个时间的轨道片段
     * @param time  时间结点
     * @return      轨道片段
     */
    @Override
    @Nullable
    public AVAssetTrackSegment segmentForTrackTime(@NonNull AVTime time) {
        if (time.equals(AVTime.kAVTimeInvalid)) {
            return null;
        }
        AVAssetTrackSegment result = null;
        for (AVAssetTrackSegment segment : mTrackSegments) {
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
     * @return 源媒体对象
     */
    @Override
    @Nullable
    public AVAsset getAsset() {
        if (mWeakMediaSource == null || mWeakMediaSource.get() == null) {
            return null;
        }
        return mWeakMediaSource.get();
    }

    /**
     * 获取源路径
     */
    @NonNull
    public Uri getUri() {
        return mUri;
    }

    /**
     * 获取当前轨道的ID
     * @return 轨道ID
     */
    @Override
    public int getTrackID() {
        return mTrackID;
    }

    /**
     * 获取当前轨道的媒体类型
     * @return  媒体类型
     */
    @Override
    public AVMediaType getMediaType() {
        return mMediaType;
    }

    /**
     * 获取当前轨道的时间区间
     * @return  时间区间
     */
    @Override
    public AVTimeRange getTimeRange() {
        return mTimeRange;
    }

    /**
     * 获取帧大小
     */
    @Override
    public CGSize getNaturalSize() {
        return mNaturalSize;
    }

    /**
     * 获取转换对象
     */
    @Override
    public AffineTransform getPreferredTransform() {
        return mPreferredTransform;
    }

    /**
     * 获取默认音量
     *
     * @return
     */
    @Override
    public float getPreferredVolume() {
        return mPreferredVolume;
    }

    /**
     * 判断是否需要重拍时间戳
     */
    @Override
    public boolean isFrameReordering() {
        return mFrameReordering;
    }

    /**
     * 获取轨道片段
     * @return 轨道片段列表
     */
    @Override
    public List<AVAssetTrackSegment> getTrackSegments() {
        return mTrackSegments;
    }
}
