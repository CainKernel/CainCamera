package com.cgfay.cavfoundation;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeMapping;
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
public class AVCompositionTrack implements AVAssetTrack {

    private static final String TAG = "AVCompositionTrack";

    /**
     * 默认音频时间刻度
     */
    private static final int DEFAULT_SAMPLE_RATE = 44100;

    /**
     * 源媒体数据
     */
    @Nullable
    private AVAsset mAsset;

    /**
     * 源数据Uri路径
     */
    @Nullable
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
     * 时间刻度，如果是视频，则采用默认的600，如果是音频，则采用默认的44100Hz
     */
    private int mNaturalTimeScale;

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
    private List<AVCompositionTrackSegment> mTrackSegments = new ArrayList<>();

    private AVCompositionTrack(@NonNull AVAsset asset, @NonNull Uri uri, int trackID,
                               @NonNull AVMediaType type, @NonNull AVTimeRange timeRange) {
        this(asset, uri, trackID, type, timeRange, CGSize.kSizeZero);
    }

    private AVCompositionTrack(@NonNull AVAsset asset, @NonNull Uri uri, int trackID,
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
        AVCompositionTrackSegment segment = new AVCompositionTrackSegment(uri, trackID, timeRange, timeRange);
        // 判断是否轨道id是否合法，合法则不为空
        segment.setEmpty(trackID == kTrackIDInvalid);
        mTrackSegments.add(segment);
        // 设置默认轨道的时间timescale，视频流使用600，音频流使用44100
        if (type == AVMediaType.AVMediaTypeVideo) {
            mNaturalTimeScale = AVTime.DEFAULT_TIME_SCALE;
        } else if (type == AVMediaType.AVMediaTypeAudio) {
            mNaturalTimeScale = DEFAULT_SAMPLE_RATE;
        }
    }

    public AVCompositionTrack(AVMediaType type) {
        mAsset = null;
        mUri = null;
        mMediaType = type;
        mTrackID = kTrackIDInvalid;
        mNaturalSize = CGSize.kSizeZero;
        mPreferredTransform = new AffineTransform().idt();
        mPreferredVolume = 1.0f;
        mFrameReordering = false;
        // 设置默认轨道的时间timescale，视频流使用600，音频流使用44100
        if (type == AVMediaType.AVMediaTypeVideo) {
            mNaturalTimeScale = AVTime.DEFAULT_TIME_SCALE;
        } else if (type == AVMediaType.AVMediaTypeAudio) {
            mNaturalTimeScale = DEFAULT_SAMPLE_RATE;
        }
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
        // 目前只支持组合轨道和源媒体轨道
        if (track instanceof AVCompositionTrack) {
            return insertTimeRange(timeRange, (AVCompositionTrack)track, startTime);
        } else if (track instanceof CAVAssetTrack) {
            return insertTimeRange(timeRange, (CAVAssetTrack) track, startTime);
        } else {
            return false;
        }
    }

    /**
     * 插入一个时间段的媒体数据
     * @param timeRange         插入的时间区间
     * @param compositionTrack  组合轨道
     * @param startTime         插入的起始时间
     * @return                  返回插入结果
     */
    private boolean insertTimeRange(@NonNull AVTimeRange timeRange,
                                    @NonNull AVCompositionTrack compositionTrack,
                                    @NonNull AVTime startTime) {
        // 起始时间为kAVTimeInvalid，直接退出
        if (startTime.equals(AVTime.kAVTimeInvalid)) {
            Log.e(TAG, "insertTimeRange: startTime is invalid!");
            return false;
        }

        // 空轨道不做处理
        if (compositionTrack.getTrackSegments().isEmpty()) {
            Log.e(TAG, "insertTimeRange: AVCompositionTrack is has no track segments, insert failed!");
            return false;
        }

        // 如果没有片段，并且起始时间startTime不为kAVTimeZero，则先插入一段空的片段
        if (mTrackSegments.isEmpty() && !startTime.equals(AVTime.kAVTimeZero)) {
            AVTime duration = new AVTime(timeRange.getStart().getValue(), timeRange.getStart().getTimescale());
            duration.setValue(duration.getValue() - 1);
            AVTimeRange emptyRange = new AVTimeRange(AVTime.kAVTimeZero, duration);
            insertEmptyTimeRange(emptyRange);
        }

        // 用于记录要插入的片段列表
        List<AVCompositionTrackSegment> insertSegments = new ArrayList<>();
        List<AVCompositionTrackSegment> segments = compositionTrack.getTrackSegments();
        for (AVCompositionTrackSegment segment : segments) {
            // 如果交集时间区间不为空，则直接将交集部分所对应的轨道片段对象复制出来并重新设置时间区间，最后插入到轨道片段中
            AVTimeRange intersectTimeRange = AVTimeRangeUtils.timeRangeGetIntersection(segment.mTimeMapping.getTarget(), timeRange);
            if (!AVTimeRangeUtils.timeRangeEqual(AVTimeRange.kAVTimeRangeZero, intersectTimeRange)) {
                // 如果是空片段，则直接插入timeRange交集区间的空片段
                // 如果不是空片段，则非空部分需要计算出交集区间源媒体区间的timeRange，创建一个交集区间映射对象的片段
                if (segment.isEmpty()) {
                    insertEmptyTimeRange(new AVTimeRange(startTime, intersectTimeRange.getDuration()));
                } else {
                    // 计算出源轨道时长
                    AVTime duration = AVTimeRangeUtils.timeMapDurationFromRangeToRange(intersectTimeRange.getDuration(),
                            segment.mTimeMapping.getTarget(), segment.mTimeMapping.getSource());
                    // 计算出源轨道起始时间
                    AVTime start = AVTimeRangeUtils.timeMapTimeFromRangeToRange(intersectTimeRange.getStart(),
                            segment.mTimeMapping.getTarget(), segment.mTimeMapping.getSource());

                    // 计算出要插入的轨道时间映射关系
                    AVTimeMapping mapping = new AVTimeMapping(new AVTimeRange(start, duration),
                            new AVTimeRange(startTime, intersectTimeRange.getDuration()));

                    // 创建并记录要插入的轨道片段
                    try {
                        AVCompositionTrackSegment insertSegment = (AVCompositionTrackSegment) segment.clone();
                        insertSegment.mTimeMapping = mapping;
                        insertSegments.add(insertSegment);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }

        // 插入到轨道片段中并重新排序
        if (!insertSegments.isEmpty()) {
            mTrackSegments.addAll(insertSegments);
            Collections.sort(mTrackSegments);
        }
        // 插入片段之后，更新轨道的总时长，使用并集的方式更新
        mTimeRange = AVTimeRangeUtils.timeRangeGetUnion(mTimeRange, timeRange);
        return true;
    }

    /**
     * 将一个源媒体轨道的timeRange时间区间的内容插入到组合轨道的startTime开始的位置，时长为timeRange的duration
     *
     * @param timeRange 插入源媒体的轨道时间区间
     * @param track     源媒体轨道
     * @param startTime 插入的起始时间
     * @return          插入结果
     */
    private boolean insertTimeRange(@NonNull AVTimeRange timeRange, @NonNull CAVAssetTrack track,
                                    @NonNull AVTime startTime) {

        // 处理是CAVAssetTrack轨道的情况
        List<AVAssetTrackSegment> segments = track.getTrackSegments();
        // 如果组合轨道的片段是空的，则插入一段空的片段，说明这个CAVAssetTrack是有异常的
        if (segments.isEmpty()) {
            Log.e(TAG, "insertTimeRange: CAVAssetTrack is has no track segments, insert failed!");
            return false;
        }

        // 如果没有片段，并且起始时间startTime不为kAVTimeZero，则先插入一段空的片段
        if (mTrackSegments.isEmpty() && !startTime.equals(AVTime.kAVTimeZero)) {
            AVTime duration = new AVTime(timeRange.getStart().getValue(), timeRange.getStart().getTimescale());
            duration.setValue(duration.getValue() - 1);
            AVTimeRange emptyRange = new AVTimeRange(AVTime.kAVTimeZero, duration);
            insertEmptyTimeRange(emptyRange);
        }

        // 直接插入轨道片段，这是因为CAVAssetTrack只有一个片段，因此不需要处理多片段交集的情况
        AVTimeRange trackRange = new AVTimeRange(startTime, timeRange.getDuration());
        AVCompositionTrackSegment trackSegment = new AVCompositionTrackSegment(track.getUri(),
                track.getTrackID(), timeRange, trackRange);
        mTrackSegments.add(trackSegment);

        // 插入片段之后，更新轨道的总时长，使用并集的方式更新
        mTimeRange = AVTimeRangeUtils.timeRangeGetUnion(mTimeRange, timeRange);
        return true;
    }

    /**
     * 插入空的时间区间，在片段之后插入
     * @param timeRange 时间区间
     */
    public void insertEmptyTimeRange(@NonNull AVTimeRange timeRange) {
        if (mTrackSegments.size() == 0) {
            // 第一段必须是kAVTimeZero开始，因此这个时间区间要重新计算
            if (!timeRange.getStart().equals(AVTime.kAVTimeZero)) {
                AVTime duration = AVTimeUtils.timeSubtract(timeRange.getStart(), AVTime.kAVTimeZero);
                duration = AVTimeUtils.timeAdd(duration, timeRange.getDuration());
                timeRange.setStart(AVTime.kAVTimeZero);
                timeRange.setDuration(duration);
            }
            // 加入一个新的片段
            mTrackSegments.add(new AVCompositionTrackSegment(timeRange));
            // 计算出轨道的时间区间
            mTimeRange = new AVTimeRange(timeRange.getStart(), timeRange.getDuration());
        } else {
            // 获取最后一个片段的时间区间的结束位置作为时间区间的起始位置
            mTrackSegments.add(new AVCompositionTrackSegment(timeRange));
            // 轨道片段按照起始时间进行排序
            Collections.sort(mTrackSegments);
            // 计算轨道时间区间
            mTimeRange = AVTimeRangeUtils.timeRangeGetUnion(mTimeRange, timeRange);
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
        mTimeRange = new AVTimeRange(AVTime.kAVTimeZero,
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
     * 设置媒体资源对象
     */
    public void setAsset(@Nullable AVAsset asset) {
        mAsset = asset;
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
     * 设置媒体Uri
     */
    public void setUri(@Nullable Uri uri) {
        mUri = uri;
    }

    /**
     * 获取Uri
     */
    @Nullable
    @Override
    public Uri getUri() {
        return mUri;
    }

    /**
     * 设置轨道ID
     * @param trackID 轨道ID
     */
    public void setTrackID(int trackID) {
        mTrackID = trackID;
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
     * 设置轨道的时间区间
     */
    public void setTimeRange(@NonNull AVTimeRange timeRange) {
        mTimeRange = timeRange;
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
     * 设置轨道分辨率
     */
    public void setNaturalSize(@NonNull CGSize naturalSize) {
        mNaturalSize = naturalSize;
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
     * 设置轨道时间刻度
     */
    public void setNaturalTimeScale(int naturalTimeScale) {
        mNaturalTimeScale = naturalTimeScale;
    }

    /**
     * 获取轨道刻度
     */
    @Override
    public int getNaturalTimeScale() {
        return mNaturalTimeScale;
    }

    /**
     * 设置转换对象
     */
    public void setPreferredTransform(@NonNull AffineTransform preferredTransform) {
        mPreferredTransform = preferredTransform;
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
     * 设置默认速度
     */
    public void setPreferredRate(float rate) {
        mPreferredRate = rate;
    }

    /**
     * 获取默认速度
     */
    @Override
    public float getPreferredRate() {
        return mPreferredRate;
    }

    /**
     * 设置音量大小
     * @param preferredVolume 0.0 ~ 1.0
     */
    public void setPreferredVolume(float preferredVolume) {
        mPreferredVolume = preferredVolume;
    }

    /**
     * 获取默认音量
     */
    @Override
    public float getPreferredVolume() {
        return mPreferredVolume;
    }

    /**
     * 设置帧重排标志
     */
    public void setFrameReordering(boolean reordering) {
        mFrameReordering = reordering;
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
    @NonNull
    @Override
    public List<AVCompositionTrackSegment> getTrackSegments() {
        return mTrackSegments;
    }

}
