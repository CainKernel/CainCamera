//
// Created by CainHuang on 2020/5/23.
//

#include <math.h>
#include "AVTime.h"

const static AVTime kAVTimeInvalid = {0, 0};

const static AVTime kAVTimeZero = {0, DEFAULT_TIME_SCALE};

/**
 * 获取最大公约数
 */
int getGCD(int lhs, int rhs) {
    int remainder;
    remainder = lhs % rhs;
    while (remainder != 0) {
        lhs = rhs;
        rhs = remainder;
        remainder = lhs % rhs;
    }
    return rhs;
}

/**
 * 获取最小公倍数
 */
int getLCM(int lhs, int rhs) {
    return lhs * (rhs / getGCD(lhs, rhs));
}

/**
 * 创建一个时间对象
 */
AVTime AVTimeMake(int64_t value, int32_t timescale) {
    AVTime time;
    time.value = value;
    time.timescale = timescale;
    return time;
}
/**
 * 使用秒数创建AVTime对象
 */
AVTime AVTimeMakeWithSeconds(double seconds) {
    return AVTimeMakeWithSeconds(seconds, DEFAULT_TIME_SCALE);
}

/**
 * 使用秒数创建AVTime对象
 */
AVTime AVTimeMakeWithSeconds(double seconds, int32_t preferredTimescale) {
    AVTime time;
    time.value = (int64_t)round(seconds * preferredTimescale);
    time.timescale = preferredTimescale;
    return time;
}

/**
 * 将AVTime转换为timescale刻度
 */
AVTime AVTimeConvertScale(AVTime time, int32_t timescale) {
    if (AVTimeCompare(time, kAVTimeInvalid) == 0) {
        return kAVTimeInvalid;
    }
    if (AVTimeCompare(time, kAVTimeZero) == 0) {
        AVTime result;
        result.value = 0;
        result.timescale = timescale;
        return result;
    }
    AVTime result;
    result.value = (int64_t)round(time.value * timescale / time.timescale);
    result.timescale = timescale;
    return result;
}

/**
 * AVTime相加
 */
AVTime AVTimeAdd(AVTime lhs, AVTime rhs) {
    AVTime result;
    if (AVTimeCompare(lhs, kAVTimeInvalid) == 0) {
        result.value = rhs.value;
        result.timescale = rhs.timescale;
        return result;
    }
    if (AVTimeCompare(rhs, kAVTimeInvalid) == 0) {
        result.value = lhs.value;
        result.timescale = lhs.timescale;
        return result;
    }
    int32_t timescale = lhs.timescale;
    if (lhs.timescale != rhs.timescale) {
        timescale = getLCM(lhs.timescale, rhs.timescale);
        result.timescale = timescale;
        result.value = (int64_t)round(lhs.value + rhs.value);
    } else {
        result.value = lhs.value + lhs.value;
        result.timescale = timescale;
    }
    return result;
}

/**
 * AVTime相减
 */
AVTime AVTimeSubtract(AVTime lhs, AVTime rhs) {
    AVTime result;
    if (AVTimeCompare(lhs, kAVTimeInvalid) == 0 || AVTimeCompare(lhs, kAVTimeZero) == 0) {
        result.value = rhs.value;
        result.timescale = rhs.timescale;
        return result;
    }
    if (AVTimeCompare(rhs, kAVTimeInvalid) == 0 || AVTimeCompare(rhs, kAVTimeZero) == 0) {
        result.value = lhs.value;
        result.timescale = lhs.timescale;
        return result;
    }
    if (lhs.timescale != rhs.timescale) {
        result.timescale = getLCM(lhs.timescale, rhs.timescale);
        int64_t lvalue = lhs.value;
        int64_t rvalue = rhs.value;
        if (lhs.timescale != result.timescale) {
            AVTime newlhs = AVTimeConvertScale(lhs, result.timescale);
            lvalue = newlhs.value;
        }
        if (rhs.timescale != result.timescale) {
            AVTime newrhs = AVTimeConvertScale(rhs, result.timescale);
            rvalue = newrhs.value;
        }
        result.value = (int64_t)round(lvalue - rvalue);
    } else {
        result.timescale = lhs.timescale;
        result.value = lhs.value - rhs.value;
    }
    return result;
}

/**
 * AVTime 乘以倍数
 */
AVTime AVTimeMultiply(AVTime time, int multiplier) {
    if (AVTimeCompare(time, kAVTimeInvalid) == 0) {
        return kAVTimeInvalid;
    }
    if (AVTimeCompare(time, kAVTimeZero) == 0) {
        return kAVTimeZero;
    }
    AVTime result;
    result.value = (int64_t)round(time.value * multiplier);
    result.timescale = time.timescale;
    return result;
}

/**
 * AVTime 乘以倍数
 */
AVTime AVTimeMultiplyByFloat64(AVTime time, double multiplier) {
    if (AVTimeCompare(time, kAVTimeInvalid) == 0) {
        return kAVTimeInvalid;
    }
    if (AVTimeCompare(time, kAVTimeZero) == 0) {
        return kAVTimeZero;
    }
    AVTime result;
    result.value = (int64_t)round(time.value * multiplier);
    result.timescale = time.timescale;
    return result;
}

/**
 * AVTime 乘以倍数并除以divisor
 */
AVTime AVTimeMultiplyByRatio(AVTime time, int multiplier, int divisor) {
    if (AVTimeCompare(time, kAVTimeInvalid) == 0) {
        return kAVTimeInvalid;
    }
    if (AVTimeCompare(time, kAVTimeZero) == 0) {
        return kAVTimeZero;
    }
    AVTime result;
    result.value = (int64_t)round(time.value * multiplier / divisor);
    result.timescale = time.timescale;
    return result;
}

/**
 * 比较两个时间结构体并返回比较结果
 * @return -1 = less than, 1 = greater than, 0 = equal
 */
int AVTimeCompare(AVTime lhs, AVTime rhs) {
    if (lhs.timescale != rhs.timescale) {
        if (lhs.timescale == 0) {
            return -1;
        }
        if (rhs.timescale == 0) {
            return 1;
        }
        int timescale = getLCM(lhs.timescale, rhs.timescale);
        int64_t lvalue = (int64_t)round(lhs.value * timescale / lhs.timescale);
        int64_t rvalue = (int64_t)round(rhs.value * timescale / rhs.timescale);
        if (lvalue < rvalue) {
            return -1;
        } else if (lvalue > rvalue) {
            return 1;
        } else {
            return 0;
        }
    } else {
        // 处理 timescale 相等的情况
        if (lhs.timescale == 0) {
            return 0;
        }
        if (lhs.value < rhs.value) {
            return -1;
        } else if (lhs.value > rhs.value) {
            return 1;
        } else {
            return 0;
        }
    }
}


/**
 * 比较两个时间结构体是否相等
 */
bool AVTimeEqual(AVTime lhs, AVTime rhs) {
    int value = AVTimeCompare(lhs, rhs);
    return (value == 0);
}

/**
 * 获得较小的时间
 */
AVTime AVTimeMinimum(AVTime lhs, AVTime rhs) {
    int value = AVTimeCompare(lhs, rhs);
    if (value <= 0) {
        return lhs;
    } else {
        return rhs;
    }
}

/**
 * 获取较大的时间
 */
AVTime AVTimeMaximum(AVTime lhs, AVTime rhs) {
    int value = AVTimeCompare(lhs, rhs);
    if (value >= 0) {
        return lhs;
    } else {
        return rhs;
    }
}