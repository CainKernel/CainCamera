package com.cgfay.avfoundation;

import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * 可变媒体轨道片段数据对象
 */
public class AVCompositionTrackSegment extends AVAssetTrackSegment {

    /**
     * 源媒体Uri
     */
    private Uri mSourceUri;

    /**
     * 源媒体轨道
     */
    private int mSourceTrackID;

    public AVCompositionTrackSegment(@NonNull AVTimeRange timeRange) {
        super(timeRange);
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
        mEmpty = true;
        mSourceUri = uri;
        mSourceTrackID = sourceTrackID;
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
