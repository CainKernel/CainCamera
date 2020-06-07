package com.cgfay.cavfoundation;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.cgfay.coremedia.AVTimeMapping;
import com.cgfay.coremedia.AVTimeRange;
import com.cgfay.coremedia.AVTimeUtils;

/**
 * 可编辑媒体轨道片段
 * 备注：轨道片段插入的源媒体轨道Uri 和 trackID 如果存在，说明带轨道数据
 */
public class AVCompositionTrackSegment extends AVAssetTrackSegment implements Comparable<AVCompositionTrackSegment>, Cloneable {

    /**
     * 源媒体Uri
     */
    private Uri mSourceUri;

    /**
     * 源媒体轨道
     */
    private int mSourceTrackID;

    /**
     * 创建一个空的轨道片段
     * @param timeRange 轨道时间区间
     */
    public AVCompositionTrackSegment(@NonNull AVTimeRange timeRange) {
        super(timeRange);
        mEmpty = true;
        mSourceUri = null;
        mSourceTrackID = AVAssetTrack.kTrackIDInvalid;
    }

    /**
     * 根据源Uri路径、轨道ID创建一个片段
     * @param uri               源文件Uri
     * @param sourceTrackID     源轨道ID
     * @param sourceTimeRange   源文件的时间区间
     * @param targetTimeRange   映射到轨道的时间区间
     */
    public AVCompositionTrackSegment(@NonNull Uri uri, int sourceTrackID,
                                     @NonNull AVTimeRange sourceTimeRange,
                                     @NonNull AVTimeRange targetTimeRange) {
        super(new AVTimeMapping(sourceTimeRange, targetTimeRange));
        mEmpty = false;
        mSourceUri = uri;
        mSourceTrackID = sourceTrackID;
    }

    /**
     * 利用固定轨道片段创建一个可编辑媒体轨道片段
     * @param uri
     * @param sourceTrackID
     * @param segment
     */
    public AVCompositionTrackSegment(@NonNull Uri uri, int sourceTrackID, @NonNull AVAssetTrackSegment segment) {
        super(new AVTimeMapping(segment.getTimeMapping()));
        mSourceUri = uri;
        mSourceTrackID = sourceTrackID;
        mEmpty = segment.mEmpty;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        AVCompositionTrackSegment segment = (AVCompositionTrackSegment)super.clone();
        segment.mTimeMapping = new AVTimeMapping(segment.getTimeMapping());
        segment.mSourceUri = mSourceUri;
        segment.mSourceTrackID = mSourceTrackID;
        segment.mEmpty = mEmpty;
        return segment;
    }

    @Override
    public int compareTo(AVCompositionTrackSegment other) {
        return AVTimeUtils.timeCompare(mTimeMapping.getTarget().getStart(), other.mTimeMapping.getTarget().getStart());
    }

    /**
     * 获取源Uri路径
     */
    public Uri getSourceUri() {
        return mSourceUri;
    }

    /**
     * 获取源媒体轨道ID
     */
    public int getSourceTrackID() {
        return mSourceTrackID;
    }

}
