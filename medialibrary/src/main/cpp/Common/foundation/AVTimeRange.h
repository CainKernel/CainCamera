//
// Created by CainHuang on 2020/5/23.
//

#ifndef AVTIMERANGE_H
#define AVTIMERANGE_H

#include "AVTime.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct AVTimeRange {
    AVTime start;
    AVTime duration;
} AVTimeRange;

extern const AVTimeRange kAVTimeRangeZero;

extern const AVTimeRange kAVTimeRangeInvalid;

/**
 * 创建时间区间结构体
 */
AVTimeRange AVTimeRangeMake(AVTime start, AVTime duration);

/**
 * 给定其实结束时间创建时间区间
 */
AVTimeRange AVTimeRangeFromTimeToTime(AVTime start, AVTime end);

/**
 * 获取两个CMTimeRange的并集
 */
AVTimeRange AVTimeRangeGetUnion(AVTimeRange range, AVTimeRange otherRange);

/**
 * 获取两个CMTimeRange的交集
 */
AVTimeRange AVTimeRangeGetIntersection(AVTimeRange range, AVTimeRange otherRange);

/**
 * 判断两个CMTimeRange是否相等
 */
bool AVTimeRangeEqual(AVTimeRange range, AVTimeRange otherRange);

/**
 * 判断时间区间是否存在某个时间
 */
bool AVTimeRangeContainsTime(AVTimeRange timeRange, AVTime time);

/**
 * 判断时间区间range里面是否包含另外一个时间区间
 */
bool AVTimeRangeContainsTimeRange(AVTimeRange range, AVTimeRange otherRange);

/**
 * 获取时间区间的结尾
 * AVTimeRangeContainsTime(range, CMTimeRangeGetEnd(range)) is always false.
 */
AVTime AVTimeRangeGetEnd(AVTimeRange range);

/**
 * The start and end time of fromRange will be mapped to the start and end time of toRange respectively.
 * Other times will be mapped linearly, using the formula:
 *     result = (time-fromRange.start)*(toRange.duration/fromRange.duration)+toRange.start
 *     If either AVTimeRange argument is empty, an invalid CMTime will be returned.
 *     If t does not have the same epoch as fromRange.start, an invalid CMTime will be returned.
 *     If both fromRange and toRange have duration kCMTimePositiveInfinity,
 *     t will be offset relative to the differences between their starts, but not scaled.
 */
AVTime AVTimeMapTimeFromRangeToRange(AVTime time, AVTimeRange fromRange, AVTimeRange toRange);

/**
 * 返回时间区间最接近的时间值
 */
AVTime AVTimeClampToRange(AVTime time, AVTimeRange range);

/**
 * 将一段时长从fromRange映射到toRange
 */
AVTime AVTimeMapDurationFromDurationToRange(AVTime duration, AVTimeRange fromRange, AVTimeRange toRange);

typedef struct AVTimeMapping {
    AVTimeRange source;
    AVTimeRange target;
} AVTimeMapping;

extern const AVTimeMapping kAVTimeMappingInvalid;

/**
 * 创建一个AVTimeMapping映射关系
 */
AVTimeMapping AVTimeMappingMake(AVTimeRange source, AVTimeRange target);

/**
 * 创建一个空的AVTimeMapping映射关系
 * @param target
 * @return
 */
AVTimeMapping CMTimeMappingMakeEmpty(AVTimeRange target);

#ifdef __cplusplus
}
#endif

#endif //AVTIMERANGE_H
