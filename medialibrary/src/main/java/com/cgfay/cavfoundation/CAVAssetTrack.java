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
 * 固定媒体轨道
 * 固定媒体轨道会持有媒体对象mAsset和媒体的路径mUri，这两个都不为空对象
 */
public class CAVAssetTrack implements AVAssetTrack {

    /**
     * 源媒体数据
     */
    @NonNull
    private AVAsset mAsset;

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
    @NonNull
    private AVTimeRange mTimeRange;

    /**
     * 视频帧大小
     */
    @NonNull
    private CGSize mNaturalSize;

    /**
     * 时间刻度，如果是视频，则采用默认的600，如果是音频，则采用输入的采样率
     */
    protected int mNaturalTimeScale;

    /**
     * 转换对象，比如90度、270度等转换
     */
    @NonNull
    private AffineTransform mPreferredTransform;

    /**
     * 默认播放速度，通常是1.0
     */
    private float mPreferredRate;

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
    private List<AVAssetTrackSegment> mTrackSegments = new ArrayList<>();

    public CAVAssetTrack(@NonNull AVAsset asset, @NonNull Uri uri, int trackID,
                        @NonNull AVMediaType type, @NonNull AVTimeRange timeRange) {
        this(asset, uri, trackID, type, timeRange, CGSize.kSizeZero);
    }

    public CAVAssetTrack(@NonNull AVAsset asset, @NonNull Uri uri, int trackID,
                        @NonNull AVMediaType type, @NonNull AVTimeRange timeRange,
                        @NonNull CGSize size) {
        mAsset = asset;
        mUri = uri;
        mTrackID = trackID;
        mMediaType = type;
        mTimeRange = timeRange;
        mNaturalSize = size;
        mPreferredTransform = new AffineTransform().idt();
        mPreferredRate = 1.0f;
        mPreferredVolume = 1.0f;
        mFrameReordering = false;
        // 创建片段信息
        AVTimeMapping mapping = new AVTimeMapping(timeRange, timeRange);
        AVAssetTrackSegment segment = new AVAssetTrackSegment(mapping);
        // 判断是否轨道id是否合法，合法则不为空
        segment.setEmpty(mTrackID == kTrackIDInvalid);
        mTrackSegments.add(segment);
    }

    /**
     * 获取包含某个时间的轨道片段
     */
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
     */
    @NonNull
    @Override
    public AVAsset getAsset() {
        return mAsset;
    }

    /**
     * 获取Uri
     */
    @NonNull
    @Override
    public Uri getUri() {
        return mUri;
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
    @NonNull
    @Override
    public AVTimeRange getTimeRange() {
        return mTimeRange;
    }

    /**
     * 获取帧大小
     */
    @NonNull
    @Override
    public CGSize getNaturalSize() {
        return mNaturalSize;
    }

    /**
     * 获取轨道刻度
     */
    @Override
    public int getNaturalTimeScale() {
        return mNaturalTimeScale;
    }

    /**
     * 获取转换对象
     */
    @NonNull
    @Override
    public AffineTransform getPreferredTransform() {
        return mPreferredTransform;
    }

    /**
     * 获取默认速度
     */
    @Override
    public float getPreferredRate() {
        return mPreferredRate;
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
     * @return 轨道片段列表
     */
    @NonNull
    @Override
    public List<AVAssetTrackSegment> getTrackSegments() {
        return mTrackSegments;
    }
}
