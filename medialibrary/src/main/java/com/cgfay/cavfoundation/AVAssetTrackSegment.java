package com.cgfay.cavfoundation;

import androidx.annotation.NonNull;

import com.cgfay.coremedia.AVTimeMapping;
import com.cgfay.coremedia.AVTimeRange;

/**
 * 轨道片段信息，用于表示轨道需要展示的片段
 * 轨道片段信息包含时间区间映射关系。或者是来自一个数据源的轨道
 */
public class AVAssetTrackSegment {

    /**
     * 是否空片段
     */
    protected boolean mEmpty;

    /**
     * 轨道片段映射对象，不能为空对象
     */
    protected AVTimeMapping mTimeMapping;

    public AVAssetTrackSegment(@NonNull AVTimeRange timeRange) {
        mEmpty = true;
        mTimeMapping = new AVTimeMapping(timeRange, timeRange);
    }

    protected AVAssetTrackSegment(@NonNull AVTimeMapping mapping) {
        mEmpty = true;
        mTimeMapping = mapping;
    }

    /**
     * 设置是否空的轨道片段
     */
    public void setEmpty(boolean empty) {
        mEmpty = empty;
    }

    /**
     * 是否空的轨道片段
     * @return  是否空的轨道片段
     */
    public boolean isEmpty() {
        return mEmpty;
    }

    /***
     * 时间映射关系
     */
    @NonNull
    public AVTimeMapping getTimeMapping() {
        return mTimeMapping;
    }
}
