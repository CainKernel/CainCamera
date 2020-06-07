//
// Created by CainHuang on 2020/5/23.
//

#include "AVTimeRange.h"

const AVTimeRange kAVTimeRangeZero = { kAVTimeZero, kAVTimeZero};

const AVTimeRange kAVTimeRangeInvalid = { kAVTimeInvalid, kAVTimeInvalid};

const AVTimeMapping kAVTimeMappingInvalid = { kAVTimeRangeInvalid, kAVTimeRangeInvalid};

/**
 * 创建时间区间结构体
 */
AVTimeRange AVTimeRangeMake(AVTime start, AVTime duration) {
    AVTimeRange range;
    range.start = AVTimeMake(start.value, start.timescale);
    range.duration = AVTimeMake(duration.value, duration.timescale);
    return range;
}

/**
 * 给定其实结束时间创建时间区间
 */
AVTimeRange AVTimeRangeFromTimeToTime(AVTime start, AVTime end) {
    AVTimeRange range;
    range.start = AVTimeMake(start.value, start.timescale);
    range.duration = AVTimeSubtract(end, start);
    return range;
}

/**
 * 获取两个CMTimeRange的并集
 */
AVTimeRange AVTimeRangeGetUnion(AVTimeRange range, AVTimeRange otherRange) {
    AVTimeRange result;
    if (AVTimeRangeEqual(range, kAVTimeRangeInvalid) || AVTimeRangeEqual(otherRange, kAVTimeRangeInvalid)) {
        return kAVTimeRangeInvalid;
    }
    if (AVTimeRangeEqual(range, kAVTimeRangeZero) || AVTimeRangeEqual(otherRange, kAVTimeRangeZero)) {
        return kAVTimeRangeZero;
    }
    // 处理交集不为0的情况，直接拿最小最大值作为交集区间开始结束位置
    AVTime unionStart = AVTimeMinimum(range.start, otherRange.start);
    AVTime unionEnd = AVTimeMaximum(AVTimeAdd(range.start, range.duration), AVTimeAdd(otherRange.start, otherRange.duration));
    return AVTimeRangeFromTimeToTime(unionStart, unionEnd);
}

/**
 * 获取两个CMTimeRange的交集
 */
AVTimeRange AVTimeRangeGetIntersection(AVTimeRange range, AVTimeRange otherRange) {
    AVTime end = AVTimeAdd(range.start, range.duration);
    AVTime otherEnd = AVTimeAdd(otherRange.start, otherRange.duration);
    // otherRange.start >= end 或者 otherRange.end < range.start，说明没有交集
    if (AVTimeCompare(otherRange.start, end) > 0 || AVTimeCompare(otherEnd, range.start) < 0) {
        return kAVTimeRangeZero;
    }
    // 使用最大的开始时间作为交集的开头
    AVTime intersectStart = AVTimeMaximum(range.start, otherRange.start);
    AVTime intersectEnd = AVTimeMinimum(end, otherEnd);
    return AVTimeRangeFromTimeToTime(intersectStart, intersectEnd);
}

/**
 * 判断两个CMTimeRange是否相等
 */
bool AVTimeRangeEqual(AVTimeRange range, AVTimeRange otherRange) {
    return (AVTimeEqual(range.start, otherRange.start) && AVTimeEqual(range.duration, otherRange.duration));
}

/**
 * 判断时间区间是否存在某个时间
 */
bool AVTimeRangeContainsTime(AVTimeRange timeRange, AVTime time) {
    return AVTimeCompare(timeRange.start, time) <= 0
            && AVTimeCompare(AVTimeAdd(timeRange.start, timeRange.duration), time);
}

/**
 * 判断时间区间range里面是否包含另外一个时间区间
 */
bool AVTimeRangeContainsTimeRange(AVTimeRange range, AVTimeRange otherRange) {
    AVTime end = AVTimeAdd(range.start, range.duration);
    AVTime otherEnd = AVTimeAdd(otherRange.start, otherRange.duration);
    return (AVTimeCompare(range.start, otherRange.start) >= 0 && AVTimeCompare(end, otherEnd) <= 0)
            || (AVTimeCompare(range.start, otherRange.start) <= 0 && AVTimeCompare(end, otherEnd) >= 0);
}

/**
 * 获取时间区间的结尾
 * AVTimeRangeContainsTime(range, CMTimeRangeGetEnd(range)) is always false.
 */
AVTime AVTimeRangeGetEnd(AVTimeRange range) {
    if (AVTimeRangeEqual(range, kAVTimeRangeInvalid)) {
        return kAVTimeInvalid;
    }
    if (AVTimeRangeEqual(range, kAVTimeRangeZero)) {
        return AVTimeMake(1, DEFAULT_TIME_SCALE);
    }
    AVTime time = AVTimeAdd(range.start, range.duration);
    time.value = time.value + 1;
    return time;
}

/**
 * 获取时间区间的结尾
 * AVTimeRangeContainsTime(range, CMTimeRangeGetEnd(range)) is always false.
 */
AVTime AVTimeRangeGetEndWithTimescale(AVTimeRange range, int32_t timescale) {
    if (AVTimeRangeEqual(range, kAVTimeRangeInvalid)) {
        return kAVTimeInvalid;
    }
    if (AVTimeRangeEqual(range, kAVTimeRangeZero)) {
        return AVTimeMake(1, timescale);
    }
    // 计算出结果后，并转换成新的timescale
    AVTime time = AVTimeAdd(range.start, range.duration);
    time = AVTimeConvertScale(time, timescale);
    time.value = time.value + 1;
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
AVTime AVTimeMapTimeFromRangeToRange(AVTime time, AVTimeRange fromRange, AVTimeRange toRange) {
    int compareStart = AVTimeCompare(time, fromRange.start);
    int compareEnd = AVTimeCompare(time, AVTimeAdd(fromRange.start, fromRange.duration));
    if (compareStart < 0 || compareEnd > 0) {
        return kAVTimeInvalid;
    }
    if (compareStart == 0) {
        AVTime result;
        result.value = toRange.start.value;
        result.timescale = toRange.start.timescale;
        return result;
    }
    if (compareEnd == 0) {
        return AVTimeAdd(toRange.start, toRange.duration);
    }

    AVTime duration = AVTimeSubtract(time, fromRange.start);
    duration = AVTimeMultiplyByRatio(duration,
            (int)(toRange.duration.value * fromRange.duration.timescale),
            (int)(toRange.duration.timescale * fromRange.duration.value));
    return AVTimeAdd(duration, toRange.start);
}

/**
 * 返回时间区间最接近的时间值
 */
AVTime AVTimeClampToRange(AVTime time, AVTimeRange range) {
    if (AVTimeEqual(time, kAVTimeInvalid) || AVTimeRangeEqual(range, kAVTimeRangeInvalid)) {
        return kAVTimeInvalid;
    }
    if (AVTimeRangeEqual(range, kAVTimeRangeZero)) {
        return kAVTimeZero;
    }
    AVTime end = AVTimeAdd(range.start, range.duration);
    int compareStart = AVTimeCompare(range.start, time);
    int compareEnd = AVTimeCompare(end, time);
    // 小于起始时间
    if (compareStart >= 0) {
        AVTime result;
        result.value = range.start.value;
        result.timescale = range.start.timescale;
        return result;
    }
    // 大于结束时间
    if (compareEnd <= 0) {
        return end;
    }
    // 在区间内的偏移值
    return AVTimeAdd(AVTimeSubtract(time, range.start), range.start);
}

/**
 * 将一段时长从fromRange映射到toRange
 */
AVTime AVTimeMapDurationFromDurationToRange(AVTime duration, AVTimeRange fromRange,
                                            AVTimeRange toRange) {
    if (AVTimeRangeEqual(fromRange, kAVTimeRangeZero) || AVTimeRangeEqual(fromRange, kAVTimeRangeInvalid)) {
        return kAVTimeInvalid;
    }
    if (AVTimeRangeEqual(toRange, kAVTimeRangeZero) || AVTimeRangeEqual(toRange, kAVTimeRangeInvalid)) {
        return kAVTimeInvalid;
    }
    if (AVTimeEqual(duration, kAVTimeInvalid)) {
        return kAVTimeInvalid;
    }
    if (AVTimeEqual(duration, kAVTimeZero)) {
        return kAVTimeZero;
    }
    return AVTimeMultiplyByRatio(duration, (int)(toRange.duration.value * fromRange.duration.timescale),
                                 (int)(toRange.duration.timescale * fromRange.duration.value));
}


/**
 * 创建一个AVTimeMapping映射关系
 */
AVTimeMapping AVTimeMappingMake(AVTimeRange source, AVTimeRange target) {
    AVTimeMapping mapping;
    mapping.source = AVTimeRangeMake(source.start, source.duration);
    mapping.target = AVTimeRangeMake(target.start, target.duration);
    return mapping;
}

/**
 * 创建一个空的AVTimeMapping映射关系
 */
AVTimeMapping CMTimeMappingMakeEmpty(AVTimeRange target) {
    AVTimeMapping mapping;
    mapping.source = kAVTimeRangeZero;
    mapping.target = AVTimeRangeMake(target.start, target.duration);
    return mapping;
}