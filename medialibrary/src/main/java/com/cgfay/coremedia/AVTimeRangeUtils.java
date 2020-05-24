package com.cgfay.coremedia;

import androidx.annotation.NonNull;

/**
 * 时间区间处理
 */
public class AVTimeRangeUtils {

    private AVTimeRangeUtils() {

    }

    /**
     * 给定其实结束时间创建时间区间
     */
    public static AVTimeRange timeRangeFromTimeToTime(@NonNull AVTime start, @NonNull AVTime end) {
        if (start.equals(end)) {
            return AVTimeRange.kAVTimeRangeZero;
        }
        AVTime duration = AVTimeUtils.timeSubtract(end, start);
        return new AVTimeRange(start, duration);
    }

    /**
     * 获取两个AVTimeRange的并集
     */
    public static AVTimeRange timeRangeGetUnion(@NonNull AVTimeRange range,
                                                @NonNull AVTimeRange otherRange) {
        if (timeRangeEqual(range, AVTimeRange.kAVTimeRangeInvalid)
                || otherRange.equals(AVTimeRange.kAVTimeRangeInvalid)) {
            return AVTimeRange.kAVTimeRangeInvalid;
        } else if (timeRangeEqual(range, AVTimeRange.kAVTimeRangeZero)
                || otherRange.equals(AVTimeRange.kAVTimeRangeZero)) {
            return AVTimeRange.kAVTimeRangeZero;
        }
        // 处理交集不为0的情况，直接拿最小最大值作为交集区间开始结束位置
        AVTime unionStart = AVTimeUtils.timeMinimum(range.getStart(), otherRange.getStart());
        AVTime unionEnd = AVTimeUtils.timeMaximum(range.getEnd(), otherRange.getEnd());
        return timeRangeFromTimeToTime(unionStart, unionEnd);
    }

    /**
     * 获取两个AVTimeRange的交集
     */
    public static AVTimeRange timeRangeGetIntersection(@NonNull AVTimeRange range,
                                                       @NonNull AVTimeRange otherRange) {
        AVTime end = range.getEnd();
        AVTime otherEnd = otherRange.getEnd();
        // otherRange.start >= end 或者 otherRange.end < range.start，说明没有交集
        if (AVTimeUtils.timeCompare(otherRange.getStart(), end) > 0
                || AVTimeUtils.timeCompare(otherEnd, range.getStart()) < 0) {
            return AVTimeRange.kAVTimeRangeZero;
        }
        // 使用最大的开始时间作为交集的开头
        AVTime intersectStart = AVTimeUtils.timeMaximum(range.getStart(), otherRange.getStart());
        // 使用最小结束时间作为交集的结尾
        AVTime intersectEnd = AVTimeUtils.timeMinimum(end, otherEnd);
        // 创建交集区间
        return timeRangeFromTimeToTime(intersectStart, intersectEnd);
    }

    /**
     * 判断两个AVTimeRange是否相等
     * @param range
     * @param otherRange
     * @return
     */
    public static boolean timeRangeEqual(@NonNull AVTimeRange range,
                                         @NonNull AVTimeRange otherRange) {
        if (range.equals(otherRange)) {
            return true;
        }
        return range.getStart().equals(otherRange.getStart())
                && range.getDuration().equals(otherRange.getDuration());
    }

    /**
     * 判断时间区间是否存在某个时间
     */
    public static boolean timeRangeContainsTime(@NonNull AVTimeRange range, AVTime time) {
       return AVTimeUtils.timeCompare(range.getStart(), time) <= 0
               && AVTimeUtils.timeCompare(range.getEnd(), time) >= 0;
    }

    /**
     * 判断时间区间range里面是否包含另外一个时间区间
     */
    public static boolean timeRangeContainsTimeRange(@NonNull AVTimeRange range,
                                                     @NonNull AVTimeRange otherRange) {
        AVTime start = range.getStart();
        AVTime end = range.getEnd();
        AVTime otherStart = otherRange.getStart();
        AVTime otherEnd = otherRange.getEnd();
        return (AVTimeUtils.timeCompare(start, otherStart) >= 0 && AVTimeUtils.timeCompare(end, otherEnd) <= 0)
                || (AVTimeUtils.timeCompare(start, otherStart) <= 0 && AVTimeUtils.timeCompare(end, otherEnd) >= 0);
    }

    /**
     * 获取时间区间的结尾
     * This function returns a CMTime structure that indicates the end of the time range specified by the <i>range</i> parameter.
     * AVTimeRangeContainsTime(range, AVTimeRangeGetEnd(range)) is always false.
     */
    public static AVTime timeRangeGetEnd(@NonNull AVTimeRange range) {
        if (timeRangeEqual(range, AVTimeRange.kAVTimeRangeInvalid)) {
            return AVTime.kAVTimeInvalid;
        }
        if (timeRangeEqual(range, AVTimeRange.kAVTimeRangeZero)) {
            return new AVTime(1, AVTime.DEFAULT_TIME_SCALE);
        }
        // 时间往后挪一个刻度值，保证AVTimeRangeContainsTime(range, AVTimeRangeGetEnd(range)) 一直为false
        AVTime time = range.getEnd();
        time.setValue(time.getValue() + 1);
        return time;
    }

    /**
     * The start and end time of fromRange will be mapped to the start and end time of toRange respectively.
     * Other times will be mapped linearly, using the formula:
     *     result = (time-fromRange.start)*(toRange.duration/fromRange.duration)+toRange.start
     *     If either AVTimeRange argument is empty, an invalid CMTime will be returned.
     *     If t does not have the same epoch as fromRange.start, an invalid CMTime will be returned.
     *     If both fromRange and toRange have duration kCMTimePositiveInfinity,
     *     t will be offset relative to the differences between their starts, but not scaled.
     */
    public static AVTime timeMapTimeFromRangeToRange(@NonNull AVTime time,
                                                     @NonNull AVTimeRange fromRange,
                                                     @NonNull AVTimeRange toRange) {
        int compareStart = AVTimeUtils.timeCompare(time, fromRange.getStart());
        int compareEnd = AVTimeUtils.timeCompare(time, fromRange.getEnd());
        // 如果超出范围，则返回无效的时间
        if (compareStart < 0 || compareEnd > 0) {
            return AVTime.kAVTimeInvalid;
        }
        // 如果是开始时间，直接返回映射之后的开始时间
        if (compareStart == 0) {
            return toRange.getStart();
        }
        // 如果是结束时间，直接返回映射之后的结束时间
        if (compareEnd == 0) {
            return toRange.getEnd();
        }

        // (time-fromRange.start)*(toRange.duration/fromRange.duration)
        AVTime duration = AVTimeUtils.timeSubtract(time, fromRange.getStart());
        duration = AVTimeUtils.timeMultiply(duration, fromRange.getDuration(), toRange.getDuration());
        return AVTimeUtils.timeAdd(duration, toRange.getStart());
    }

    /**
     * 返回时间区间最接近的时间值
     * For a given CMTime and AVTimeRange, returns the nearest CMTime inside that time range.
     *
     * Times inside the given time range will be returned unmodified.
     * Times before the start and after the end time of the time range will return the start and end time of
     * the range respectively.
     * If the AVTimeRange argument is empty, an invalid CMTime will be returned.
     * If the given CMTime is invalid, the returned CMTime will be invalid,
     */
    public static AVTime timeClampToRange(@NonNull AVTime time, @NonNull AVTimeRange range) {
        if (time.equals(AVTime.kAVTimeInvalid) || timeRangeEqual(range, AVTimeRange.kAVTimeRangeInvalid)) {
            return AVTime.kAVTimeInvalid;
        }
        if (timeRangeEqual(range, AVTimeRange.kAVTimeRangeZero)) {
            return AVTime.kAVTimeZero;
        }
        int compareStart = AVTimeUtils.timeCompare(range.getStart(), time);
        int compareEnd = AVTimeUtils.timeCompare(range.getEnd(), time);

        // 小于起始时间
        if (compareStart >= 0) {
            return range.getStart();
        }
        // 大于结束时间
        if (compareEnd <= 0) {
            return range.getEnd();
        }
        // 在区间之内的偏移值
        AVTime offset = AVTimeUtils.timeSubtract(time, range.getStart());
        return AVTimeUtils.timeAdd(offset, range.getStart());
    }

    /**
     * 将一段时长从fromRange映射到toRange
     * The duration will be scaled in proportion to the ratio between the ranges' durations:
     *     result = dur*(toRange.duration/fromRange.duration)
     *     If dur does not have the epoch zero, an invalid CMTime will be returned.
     */
    public static AVTime timeMapDurationFromRangeToRange(@NonNull AVTime duration,
                                                         @NonNull AVTimeRange fromRange,
                                                         @NonNull AVTimeRange toRange) {
        if (timeRangeEqual(fromRange, AVTimeRange.kAVTimeRangeZero)
                || timeRangeEqual(fromRange, AVTimeRange.kAVTimeRangeInvalid)) {
            return AVTime.kAVTimeInvalid;
        }
        if (timeRangeEqual(toRange, AVTimeRange.kAVTimeRangeZero)
                || timeRangeEqual(toRange, AVTimeRange.kAVTimeRangeInvalid)) {
            return AVTime.kAVTimeInvalid;
        }
        if (duration.equals(AVTime.kAVTimeInvalid)) {
            return AVTime.kAVTimeInvalid;
        }
        if (duration.equals(AVTime.kAVTimeZero)) {
            return AVTime.kAVTimeZero;
        }
        return AVTimeUtils.timeMultiply(duration, fromRange.getDuration(), toRange.getDuration());
    }
}
