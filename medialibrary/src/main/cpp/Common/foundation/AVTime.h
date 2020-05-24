//
// Created by CainHuang on 2020/5/23.
//

#ifndef AVTIME_H
#define AVTIME_H

#include <cstdint>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct AVTime {
    int64_t value;
    int32_t timescale;
} AVTime;

const static int DEFAULT_TIME_SCALE = 600;

extern const AVTime kAVTimeInvalid;

extern const AVTime kAVTimeZero;

/**
 * 创建一个时间对象
 */
AVTime AVTimeMake(int64_t value, int32_t timescale);

/**
 * 使用秒数创建AVTime对象
 */
AVTime AVTimeMakeWithSeconds(double seconds);

/**
 * 使用秒数创建AVTime对象
 */
AVTime AVTimeMakeWithSeconds(double seconds, int32_t preferredTimescale);

/**
 * 将AVTime转换为timescale刻度
 */
AVTime AVTimeConvertScale(AVTime time, int32_t timescale);

/**
 * AVTime相加
 */
AVTime AVTimeAdd(AVTime lhs, AVTime rhs);

/**
 * AVTime相减
 */
AVTime AVTimeSubtract(AVTime lhs, AVTime rhs);

/**
 * AVTime 乘以倍数
 */
AVTime AVTimeMultiply(AVTime time, int multiplier);

/**
 * AVTime 乘以倍数
 */
AVTime AVTimeMultiplyByFloat64(AVTime time, double multiplier);

/**
 * AVTime 乘以倍数并除以divisor
 */
AVTime AVTimeMultiplyByRatio(AVTime time, int multiplier, int divisor);

/**
 * 比较两个时间结构体并返回比较结果
 * @return -1 = less than, 1 = greater than, 0 = equal
 */
int AVTimeCompare(AVTime lhs, AVTime rhs);

/**
 * 比较两个时间结构体是否相等
 */
bool AVTimeEqual(AVTime lhs, AVTime rhs);

/**
 * 获得较小的时间
 */
AVTime AVTimeMinimum(AVTime lhs, AVTime rhs);

/**
 * 获取较大的时间
 */
AVTime AVTimeMaximum(AVTime lhs, AVTime rhs);

#ifdef __cplusplus
}
#endif

#endif //AVTIME_H
