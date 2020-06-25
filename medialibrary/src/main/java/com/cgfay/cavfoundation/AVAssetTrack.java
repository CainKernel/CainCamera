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
public interface AVAssetTrack {

    /**
     * 非法轨道ID
     */
    int kTrackIDInvalid = -1;

    /**
     * 获取包含某个时间的轨道片段
     */
    @Nullable
    AVAssetTrackSegment segmentForTrackTime(@NonNull AVTime time);

    /**
     * 获取源媒体对象
     */
    @Nullable
    AVAsset getAsset();

    /**
     * 获取Uri
     */
    @Nullable
    Uri getUri();

    /**
     * 获取当前轨道的ID
     */
    int getTrackID();

    /**
     * 获取当前轨道的媒体类型
     */
    AVMediaType getMediaType();

    /**
     * 获取当前轨道的时间区间
     */
    @NonNull
    AVTimeRange getTimeRange();

    /**
     * 获取帧大小
     */
    @NonNull
    CGSize getNaturalSize();

    /**
     * 获取轨道刻度
     */
    int getNaturalTimeScale();

    /**
     * 获取转换对象
     */
    @NonNull
    AffineTransform getPreferredTransform();

    /**
     * 获取默认速度
     */
    float getPreferredRate();

    /**
     * 获取默认音量
     */
    float getPreferredVolume();

    /**
     * 判断是否需要重拍时间戳
     */
    boolean isFrameReordering();

    /**
     * 获取轨道片段
     * @return 轨道片段列表
     */
    @NonNull
    List<? extends AVAssetTrackSegment> getTrackSegments();

}
