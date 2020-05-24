package com.cgfay.avfoundation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;

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
    AVTimeRange getTimeRange();

    /**
     * 获取帧大小
     */
    CGSize getNaturalSize();

    /**
     * 获取转换对象
     */
    AffineTransform getPreferredTransform();

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
    List<? extends AVAssetTrackSegment> getTrackSegments();

}
