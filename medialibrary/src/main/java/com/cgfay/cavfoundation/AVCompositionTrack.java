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
import com.cgfay.coremedia.AVTimeUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 媒体组合轨道，轨道下面的片段暂不支持重叠处理
 */
public class AVCompositionTrack extends AVAssetTrack<AVCompositionTrackSegment> {

    private AVCompositionTrack(@NonNull AVAsset asset, @NonNull Uri uri, int trackID,
                               @NonNull AVMediaType type, @NonNull AVTimeRange timeRange) {
        this(asset, uri, trackID, type, timeRange, CGSize.kSizeZero);
    }

    private AVCompositionTrack(@NonNull AVAsset asset, @NonNull Uri uri, int trackID,
                               @NonNull AVMediaType type, @NonNull AVTimeRange timeRange,
                               @NonNull CGSize size) {
        super(asset, uri, trackID, type, timeRange, size);
    }

    public AVCompositionTrack(AVMediaType type) {
        super();
        mAsset = null;
        mMediaType = type;
        mPreferredTransform = new AffineTransform().idt();
        mPreferredVolume = 1.0f;
        mFrameReordering = true;
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
            // 轨道片段按照起始时间进行排序
            Collections.sort(mTrackSegments);
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
            // 轨道片段按照起始时间进行排序
            Collections.sort(mTrackSegments);
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
            // 轨道片段按照起始时间进行排序
            Collections.sort(mTrackSegments);
        }
    }

    /**
     * 移除时间区间，可能出现跨时间区间的情况，需要特殊处理
     * @param timeRange 时间区间
     */
    public void removeTimeRange(@NonNull AVTimeRange timeRange) {
        // 没有轨道片段，则直接退出
        if (mTrackSegments.size() == 0) {
            return;
        }

        // 判断时间区间是否在轨道时长内，不在直接跳过不做处理
        AVTimeRange trackIntersection = AVTimeRangeUtils.timeRangeGetIntersection(this.mTimeRange, timeRange);
        if (AVTimeRangeUtils.timeRangeEqual(trackIntersection, AVTimeRange.kAVTimeRangeZero)) {
            return;
        }

        // 需要额外插入的轨道片段，时间区间在轨道片段中间，将一个片段成两个片段时，该对象不为空
        AVCompositionTrackSegment otherInsertSegment = null;
        // 被删除的时长，记录被删除的总时长
        AVTime deleteDuration = AVTime.kAVTimeZero;
        // 遍历移除该时间区间
        Iterator<AVCompositionTrackSegment> iterator = mTrackSegments.iterator();
        while (iterator.hasNext()) {
            AVCompositionTrackSegment segment = iterator.next();
            AVTimeMapping mapping = segment.getTimeMapping();
            // 获取相交的时间区间
            AVTimeRange intersect = AVTimeRangeUtils.timeRangeGetIntersection(segment.getTimeMapping().getTarget(), timeRange);
            if (!AVTimeRangeUtils.timeRangeEqual(AVTimeRange.kAVTimeRangeZero, intersect)) {
                // 获取交集结束时间
                AVTime intersectEnd = AVTimeRangeUtils.timeRangeGetEnd(intersect, mNaturalTimeScale);
                // 获取片段目的时长
                AVTime targetDuration = AVTimeUtils.timeSubtract(mapping.getTarget().getDuration(), intersect.getDuration());
                // 交集映射到源数据的时长
                AVTime sourceIntersect = AVTimeRangeUtils.timeMapDurationFromRangeToRange(intersect.getDuration(),
                        mapping.getTarget(), mapping.getSource());
                // 交集是否在开头
                boolean onIntersectStart = intersect.getStart().equals(mapping.getTarget().getStart());
                // 交集是否在结尾
                boolean onIntersectEnd = AVTimeRangeUtils.timeRangeGetEnd(mapping.getTarget(), mNaturalTimeScale).equals(intersectEnd);

                // 1、如果交集是整个片段，删除整个片段，并记录总删除时长
                if (onIntersectStart && onIntersectEnd) {
                    iterator.remove();
                    // 记录总删除时长
                    deleteDuration = AVTimeUtils.timeAdd(deleteDuration, intersect.getDuration());
                } else if (onIntersectStart) {
                    // 2、交集在开头，删除交集区间，更新起始时间
                    // 2.1、计算出新的源数据时长和时间区间
                    AVTime newSourceStart = AVTimeRangeUtils.timeRangeGetEnd(
                            AVTimeRangeUtils.timeRangeFromTimeToTime(mapping.getTarget().getStart(), sourceIntersect),
                            mNaturalTimeScale);
                    AVTime newSourceDuration = AVTimeUtils.timeSubtract(mapping.getSource().getDuration(), sourceIntersect);
                    AVTimeRange sourRange = new AVTimeRange(newSourceStart, newSourceDuration);

                    // 2.2、计算出删除后的轨道时长和时间区间
                    AVTimeRange targetRange = new AVTimeRange(intersectEnd, targetDuration);

                    // 2.3、更新轨道片段的时间区间
                    mapping.setSource(sourRange);
                    mapping.setTarget(targetRange);

                    // 2.4、计算总的删除时长，给后面的片段使用
                    deleteDuration = AVTimeUtils.timeAdd(deleteDuration, intersect.getDuration());

                    // 2.5、删除完之后，需要调整删除之后的起始位置，前面片段有可能删除过一段时间
                    // 新的起始位置 = 原起始位置 - (前面的总删除时长 + 交集时长)
                    AVTime startTime = AVTimeUtils.timeSubtract(mapping.getTarget().getStart(), deleteDuration);
                    mapping.getTarget().setStart(startTime);

                    // 更新轨道片段的时间映射对象
                    segment.mTimeMapping = mapping;

                } else if (onIntersectEnd) {
                    // 3、交集在结尾，删除交集的区间，说明前面没有删除的时长，不需要更新起始时间s
                    // 3.1、计算出新的源数据时长和时间区间
                    AVTime newSourceDuration = AVTimeUtils.timeSubtract(mapping.getSource().getDuration(), sourceIntersect);
                    AVTimeRange sourceRange = new AVTimeRange(mapping.getSource().getStart(), newSourceDuration);

                    // 3.2、计算出删除后的轨道时长和时间区间
                    AVTimeRange targetRange = new AVTimeRange(mapping.getTarget().getStart(), targetDuration);

                    // 3.3、更新轨道片段的时间区间
                    mapping.setSource(sourceRange);
                    mapping.setTarget(targetRange);

                    // 3.4、计算总的删除时长，给后面的片段使用
                    deleteDuration = AVTimeUtils.timeAdd(deleteDuration, intersect.getDuration());

                    // 3.5、更新轨道片段的时间映射关系
                    segment.mTimeMapping = mapping;

                } else {
                    // 4、交集在中间的情况，原有片段删除交集起始位置的后半段，并添加原有片段交集结尾作为起始时间到原片段结尾时间的区间
                    try {
                        // 4.1、先复制一个片段
                        otherInsertSegment = (AVCompositionTrackSegment) segment.clone();

                        // 4.2、删除segment中的交集开始的后片段
                        // 计算开头剩余的区间
                        AVTime targetEndTime = AVTimeUtils.timeConvertScale(intersect.getStart(), mNaturalTimeScale);
                        targetEndTime.setValue(targetEndTime.getValue() - 1);
                        AVTime sourceEndTime = AVTimeRangeUtils.timeMapTimeFromRangeToRange(targetEndTime,
                                mapping.getTarget(), mapping.getSource());
                        AVTimeRange sourceRange = AVTimeRangeUtils.timeRangeFromTimeToTime(mapping.getSource().getStart(), sourceEndTime);
                        AVTimeRange targetRange = AVTimeRangeUtils.timeRangeFromTimeToTime(mapping.getTarget().getStart(), targetEndTime);
                        mapping.setSource(sourceRange);
                        mapping.setTarget(targetRange);
                        segment.mTimeMapping = mapping;

                        // 4.3、使用交集结尾作为另外一个片段的起始位置
                        AVTimeMapping otherMapping = otherInsertSegment.getTimeMapping();
                        AVTime otherTargetStart = AVTimeRangeUtils.timeRangeGetEnd(intersect, mNaturalTimeScale);
                        AVTime otherTargetEnd = AVTimeUtils.timeAdd(otherMapping.getTarget().getStart(), otherMapping.getTarget().getDuration());
                        AVTime otherSourceStart = AVTimeRangeUtils.timeMapTimeFromRangeToRange(otherTargetStart, otherMapping.getTarget(), otherMapping.getSource());
                        AVTime otherSourceEnd = AVTimeUtils.timeAdd(otherMapping.getSource().getStart(), otherMapping.getSource().getDuration());
                        otherMapping.setSource(AVTimeRangeUtils.timeRangeFromTimeToTime(otherSourceStart, otherSourceEnd));
                        otherMapping.setTarget(AVTimeRangeUtils.timeRangeFromTimeToTime(otherTargetStart, otherTargetEnd));

                        // 计算出总的删除时长
                        deleteDuration = AVTimeUtils.timeAdd(deleteDuration, intersect.getDuration());

                        // 计算出新片段和起始时间
                        otherMapping.getTarget().setStart(AVTimeUtils.timeSubtract(otherTargetStart, deleteDuration));
                        otherInsertSegment.mTimeMapping = otherMapping;
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            } else if (!deleteDuration.equals(AVTime.kAVTimeZero)) {
                // 如果有被删除过时间，则说明前面的片段有删除时长的处理，需要调整当前轨道片段的起始时间
                AVTime startTime = AVTimeUtils.timeSubtract(mapping.getTarget().getStart(), deleteDuration);
                mapping.getTarget().setStart(startTime);
                segment.mTimeMapping = mapping;
            }
        }

        // 如果存在需要新插入一个片段，插入后需要重新排序片段
        if (otherInsertSegment != null) {
            mTrackSegments.add(otherInsertSegment);
            Collections.sort(mTrackSegments);
        }

        // 新的轨道时长 = 总轨道时长 - 轨道交集时长
        this.mTimeRange = new AVTimeRange(AVTime.kAVTimeZero,
                AVTimeUtils.timeSubtract(mTimeRange.getDuration(),
                        trackIntersection.getDuration()));
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
     * 获取轨道片段
     *
     * @return 轨道片段列表
     */
    @NonNull
    @Override
    public List<AVCompositionTrackSegment> getTrackSegments() {
        return mTrackSegments;
    }

    /**
     * 设置默认时间刻度
     */
    public void setNaturalTimeScale(int naturalTimeScale) {
        mNaturalTimeScale = naturalTimeScale;
    }
}
