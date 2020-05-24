package com.cgfay.avfoundation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeRange;
import com.cgfay.coremedia.AVTimeRangeUtils;
import com.cgfay.coremedia.AVTimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 媒体组合轨道，轨道下面的片段暂不支持重叠处理
 */
public class AVCompositionTrack implements AVAssetTrack<AVCompositionTrackSegment> {

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
     * 时间刻度
     */
    private int mNaturalTimeScale;

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
     * 插入一段空的时间长度为duration的时间范围
     * @param duration 时长
     */
    public void insertEmptyTimeRange(@NonNull AVTime duration) {
        if (mTrackSegments.size() == 0) {
            mTrackSegments.add(new AVCompositionTrackSegment(new AVTimeRange(AVTime.kAVTimeZero, duration)));
        } else {
            // 获取最后一个片段的时间区间的结束位置作为时间区间的起始位置
            AVCompositionTrackSegment segment = mTrackSegments.get(mTrackSegments.size() - 1);
            AVTimeRange timeRange = new AVTimeRange(AVTimeRangeUtils.timeRangeGetEnd(segment.getTimeMapping().getTarget()), duration);
            mTrackSegments.add(new AVCompositionTrackSegment(timeRange));
        }
    }

    /**
     * 插入空的时间区间，在片段之后插入
     * @param timeRange 时间区间
     */
    public void insertEmptyTimeRange(@NonNull AVTimeRange timeRange) {
        if (mTrackSegments.size() == 0) {
            // 第一段必须是kAVTimeZero开始
            timeRange.setStart(AVTime.kAVTimeZero);
            mTrackSegments.add(new AVCompositionTrackSegment(timeRange));
        } else {
            // 获取最后一个片段的时间区间的结束位置作为时间区间的起始位置
            AVCompositionTrackSegment segment = mTrackSegments.get(mTrackSegments.size() - 1);
            timeRange.setStart(AVTimeRangeUtils.timeRangeGetEnd(segment.mTimeMapping.getTarget()));
            mTrackSegments.add(new AVCompositionTrackSegment(timeRange));
        }
    }

    /**
     * 移除时间区间，可能出现跨时间区间的情况，需要特殊处理
     * @param timeRange 时间区间
     */
    public void removeTimeRange(@NonNull AVTimeRange timeRange) {
        if (mTrackSegments.size() == 0) {
            return;
        }
        // 遍历移除该时间区间
        Iterator<AVCompositionTrackSegment> iterator = mTrackSegments.iterator();

        AVTime startTime = AVTime.kAVTimeInvalid;
        while (iterator.hasNext()) {
            AVCompositionTrackSegment segment = iterator.next();
            // 获取相交的时间区间
            AVTimeRange intersectionRange = AVTimeRangeUtils.timeRangeGetIntersection(segment.getTimeMapping().getTarget(), timeRange);

            // 1、如果不存在交集，则判断前面的片段是否被修改过开始时间
            if (AVTimeRangeUtils.timeRangeEqual(intersectionRange, AVTimeRange.kAVTimeRangeZero)) {
                // 如果前面被修改过开始时间，说明这个片段的起始时间已经发生变化，需要重新更新时间
                if (AVTimeUtils.timeCompare(startTime, AVTime.kAVTimeInvalid) != 0) {
                    segment.getTimeMapping().getTarget().setStart(startTime);

                    // 计算出下个片段的开始时间，没有下一个片段时，直接停止不需要再计算
                    if (iterator.hasNext()) {
                        startTime = AVTimeRangeUtils.timeRangeGetEnd(segment.getTimeMapping().getTarget());
                    }
                }
            } else if (AVTimeRangeUtils.timeRangeEqual(segment.getTimeMapping().getTarget(), intersectionRange)) {
                // 2、如果相交的时间区间与片段的时间区间相同，则移除该片段

                // 如果前面被修改过开始时间，说明这个片段的起始时间已经发生变化，需要重新更新时间
                if (AVTimeUtils.timeCompare(startTime, AVTime.kAVTimeInvalid) != 0) {
                    segment.getTimeMapping().getTarget().setStart(startTime);
                }

                // 保存当前开始的时间，做作下一个片段的开始时间
                if (iterator.hasNext()) {
                    startTime = segment.getTimeMapping().getTarget().getStart();
                }

                // 移除当前片段
                iterator.remove();
            } else {
                // 3、存在相交的一部分，需要减少一部分时间区间，此时要更新TimeMapping的值

                // 判断起始时间是否被更改过，如果被更改过，说明上一轮时间移除过一段时间区间，需要更新轨道的时间区间的起始位置
                if (AVTimeUtils.timeCompare(startTime, AVTime.kAVTimeInvalid) != 0) {
                    segment.getTimeMapping().getTarget().setStart(startTime);
                }
                // 计算下一个片段使用的开始时间
                if (iterator.hasNext()) {
                    startTime = AVTimeRangeUtils.timeRangeGetEnd(segment.getTimeMapping().getTarget());
                }
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
        // 轨道片段按照起始时间进行排序
        Collections.sort(mTrackSegments);
        for (AVCompositionTrackSegment segment : mTrackSegments) {

        }
    }

    /**
     * 获取某个时间的轨道片段
     *
     * @param time 时间
     * @return 轨道片段
     */
    @Nullable
    @Override
    public AVCompositionTrackSegment segmentForTrackTime(@NonNull AVTime time) {
        if (time.equals(AVTime.kAVTimeInvalid)) {
            return null;
        }
        AVCompositionTrackSegment result = null;
        for (AVCompositionTrackSegment segment : mTrackSegments) {
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

    public int getNaturalTimeScale() {
        return mNaturalTimeScale;
    }

    public void setNaturalTimeScale(int naturalTimeScale) {
        mNaturalTimeScale = naturalTimeScale;
    }
}
