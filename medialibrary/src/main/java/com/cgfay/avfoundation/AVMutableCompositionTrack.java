package com.cgfay.avfoundation;

import androidx.annotation.NonNull;

import java.util.Iterator;

/**
 * 可变媒体轨道对象
 */
public class AVMutableCompositionTrack extends AVCompositionTrack {

    public AVMutableCompositionTrack() {

    }

    /**
     * 插入一个时间段的媒体数据
     * @param timeRange 时间区间
     * @param track     源文件的媒体轨道
     * @param startTime 需要插入的起始位置
     * @return          是否插入成功
     */
    public boolean insertTimeRange(@NonNull AVTimeRange timeRange, @NonNull AVAssetTrack track,
                                   @NonNull AVTime startTime) {
        AVAsset asset = track.getAsset();
        // 如果源文件不存在或轨道ID不存在，则直接插入一个空的轨道
        if (asset == null || asset.getUri() == null || track.getTrackID() == kTrackIDInvalid) {
            insertEmptyTimeRange(new AVTimeRange(startTime, timeRange.getDuration()));
        } else {
            // 求出轨道时间区间交集，如果时间区间不在源媒体轨道区间中，则没法插入
            AVTimeRange sourceTimeRange = AVTimeRangeUtils.timeRangeGetIntersection(timeRange, track.getTimeRange());
            if (AVTimeRangeUtils.timeRangeEqual(sourceTimeRange, AVTimeRange.kAVTimeRangeZero)) {
                return false;
            }

            // 找到源媒体片段
            AVAssetTrackSegment sourceSegment = track.segmentForTrackTime(timeRange.getStart());
            if (sourceSegment == null) {
                return false;
            }

            // 找到源媒体的实际时长
            AVTime sourceDuration = AVTimeRangeUtils.timeMapDurationFromRangeToRange(sourceTimeRange.getDuration(),
                    sourceSegment.getTimeMapping().getTarget(), sourceSegment.getTimeMapping().getSource());

            // 找到源媒体的实际开始时间
            AVTime sourceStart = AVTimeRangeUtils.timeMapTimeFromRangeToRange(sourceTimeRange.getStart(),
                    sourceSegment.getTimeMapping().getTarget(), sourceSegment.getTimeMapping().getSource());

            // 如果找不到源媒体开始时间和时长，则插入失败
            if (sourceStart.equals(AVTime.kAVTimeInvalid) || sourceDuration.equals(AVTime.kAVTimeInvalid) || sourceDuration.equals(AVTime.kAVTimeZero)) {
                return false;
            }

            // 创建并插入新的轨道片段
            AVCompositionTrackSegment segment = new AVCompositionTrackSegment(asset.getUri(),
                    track.getTrackID(), new AVTimeRange(sourceStart, sourceDuration),
                    new AVTimeRange(startTime, sourceTimeRange.getDuration()));
            mTrackSegments.add(segment);
        }
        return true;
    }

    /**
     * 插入空的时间区间
     * @param timeRange 时间区间
     */
    public void insertEmptyTimeRange(@NonNull AVTimeRange timeRange) {
        mTrackSegments.add(new AVCompositionTrackSegment(timeRange));
    }

    /**
     * 移除时间区间
     * @param timeRange 时间区间
     */
    public void removeTimeRange(@NonNull AVTimeRange timeRange) {
        Iterator<AVCompositionTrackSegment> iterator = mTrackSegments.iterator();
        while (iterator.hasNext()) {
            AVCompositionTrackSegment segment = iterator.next();
            if (AVTimeRangeUtils.timeRangeEqual(segment.getTimeMapping().getTarget(), timeRange)) {
                iterator.remove();
            }
        }
    }

    /**
     * 缩放一段时间区间，目前不支持跨trackSegment的时间区间
     * @param timeRange
     * @param duration
     * 受缩放操作影响的每个trackSegment的速率将等于其生成的timeMapping的source.duration / target.duration。
     */
    public void scaleTimeRange(@NonNull AVTimeRange timeRange, AVTime duration) {

    }
}
