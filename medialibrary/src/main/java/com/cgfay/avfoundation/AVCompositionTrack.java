package com.cgfay.avfoundation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体组合轨道，轨道下面的片段暂不支持重叠处理
 */
public class AVCompositionTrack implements AVAssetTrack {

    /**
     * 媒体资源
     */
    @Nullable
    protected AVAsset mAsset;

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
    protected AVTimeRange mTimeRange;

    /**
     * 视频帧大小
     */
    protected CGSize mNaturalSize;

    /**
     * 转换对象，比如90度、270度等转换
     */
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
    protected List<AVCompositionTrackSegment> mTrackSegments;

    public AVCompositionTrack() {
        mAsset = null;
        mPreferredTransform = AffineTransform.kAffineTransformIdentity;
        mPreferredVolume = 1.0f;
        mFrameReordering = true;
        mTrackSegments = new ArrayList<>();
    }

    /**
     * 获取某个时间的轨道片段
     *
     * @param time 时间
     * @return 轨道片段
     */
    @Nullable
    @Override
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
     */
    @Nullable
    @Override
    public AVAsset getAsset() {
        return mAsset;
    }

    /**
     * 获取当前轨道的ID
     */
    @Override
    public int getTrackID() {
        return mTrackID;
    }

    /**
     * 获取当前轨道的媒体类型
     */
    @Override
    public AVMediaType getMediaType() {
        return mMediaType;
    }

    /**
     * 获取当前轨道的时间区间
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
     *
     * @return 轨道片段列表
     */
    @Override
    public List<AVCompositionTrackSegment> getTrackSegments() {
        return mTrackSegments;
    }
}
