package com.cgfay.avfoundation;

import androidx.annotation.NonNull;

/**
 * AVTime 处理类
 */
public class AVTimeUtils {

    private AVTimeUtils() {

    }

    /**
     * 将AVTime转换为timeScale刻度
     */
    public static AVTime timeConvertScale(@NonNull AVTime time, int timeSCale) {
        if (time == AVTime.kAVTimeInvalid) {
            return AVTime.kAVTimeInvalid;
        }
        if (time == AVTime.kAVTimeZero) {
            return new AVTime(0, timeSCale);
        }
        return new AVTime(time.getValue() * timeSCale / time.getTimescale(), timeSCale);
    }

    /**
     * AVTime对象相加
     */
    public static AVTime timeAdd(@NonNull AVTime lhs, @NonNull AVTime rhs) {
        if (lhs == AVTime.kAVTimeInvalid) {
            return new AVTime(rhs.getValue(), rhs.getTimescale());
        }
        if (rhs == AVTime.kAVTimeInvalid) {
            return new AVTime(lhs.getValue(), lhs.getTimescale());
        }

        int timeScale = lhs.getTimescale();
        if (lhs.getTimescale() != rhs.getTimescale()) {
            // 求出时间刻度的最小公倍数
            timeScale = getLCM(lhs.getTimescale(), rhs.getTimescale());
            // 转换成最小公倍数的时间刻度
            if (timeScale != lhs.getTimescale()) {
                lhs = timeConvertScale(lhs, timeScale);
            }
            // 转换成最小公倍数的时间刻度
            if (timeScale != rhs.getTimescale()) {
                rhs = timeConvertScale(rhs, timeScale);
            }
        }

        return new AVTime(lhs.getValue() + rhs.getValue(), timeScale);
    }

    /**
     * AVTime对象相减
     */
    public static AVTime timeSubtract(@NonNull AVTime lhs, @NonNull AVTime rhs) {
        if (lhs == AVTime.kAVTimeInvalid) {
            return new AVTime(rhs.getValue(), rhs.getTimescale());
        }
        if (rhs == AVTime.kAVTimeInvalid) {
            return new AVTime(lhs.getValue(), lhs.getTimescale());
        }

        int timeScale = lhs.getTimescale();
        if (lhs.getTimescale() != rhs.getTimescale()) {
            // 求出时间刻度的最小公倍数
            timeScale = getLCM(lhs.getTimescale(), rhs.getTimescale());
            // 转换成最小公倍数的时间刻度
            if (timeScale != lhs.getTimescale()) {
                lhs = timeConvertScale(lhs, timeScale);
            }
            // 转换成最小公倍数的时间刻度
            if (timeScale != rhs.getTimescale()) {
                rhs = timeConvertScale(rhs, timeScale);
            }
        }

        return new AVTime(lhs.getValue() - rhs.getValue(), timeScale);
    }

    /**
     * 时间乘以倍数
     */
    public static AVTime timeMultiply(@NonNull AVTime time, int multiplier) {
        if (time == AVTime.kAVTimeInvalid || time == AVTime.kAVTimeZero) {
            return time;
        }
        return new AVTime(time.getValue() * multiplier, time.getTimescale());
    }

    /**
     * 时间乘以倍数
     */
    public static AVTime timeMultiply(@NonNull AVTime time, long multiplier) {
        if (time == AVTime.kAVTimeInvalid || time == AVTime.kAVTimeZero) {
            return time;
        }
        return new AVTime(time.getValue() * multiplier, time.getTimescale());
    }

    /**
     * 时间乘以倍数
     */
    public static AVTime timeMultiply(@NonNull AVTime time, int multiplier, int divisor) {
        if (time == AVTime.kAVTimeInvalid || time == AVTime.kAVTimeZero) {
            return time;
        }
        return new AVTime(time.getValue() * multiplier / divisor, time.getTimescale());
    }

    /**
     * 时间乘以时长区间映射倍数, time * toDuration / fromDuration
     */
    public static AVTime timeMultiply(@NonNull AVTime time, @NonNull AVTime fromDuration,
                                      @NonNull AVTime toDuration) {
        if (time == AVTime.kAVTimeInvalid || time == AVTime.kAVTimeZero) {
            return time;
        }
        return timeMultiply(time,
                (int) (toDuration.getValue() * fromDuration.getTimescale()),
                (int)(toDuration.getTimescale() * fromDuration.getValue()));
    }

    /**
     * 比较两个时间对象，并返回比较结果
     * @return The numerical relationship of the two AVTimes (-1 = less than, 1 = greater than, 0 = equal).
     */
    public static int timeCompare(@NonNull AVTime lhs, @NonNull AVTime rhs) {
        if (lhs == AVTime.kAVTimeInvalid || rhs == AVTime.kAVTimeInvalid) {
            if (lhs == rhs) {
                return 0;
            } else if (rhs != AVTime.kAVTimeInvalid) {
                return -1;
            } else {
                return 1;
            }
        }

        if (lhs.getTimescale() != rhs.getTimescale()) {
            // 求出时间刻度的最小公倍数
            int timeScale = getLCM(lhs.getTimescale(), rhs.getTimescale());
            // 转换成最小公倍数的时间刻度
            if (timeScale != lhs.getTimescale()) {
                lhs = timeConvertScale(lhs, timeScale);
            }
            // 转换成最小公倍数的时间刻度
            if (timeScale != rhs.getTimescale()) {
                rhs = timeConvertScale(rhs, timeScale);
            }
        }

        if (lhs.getValue() < rhs.getValue()) {
            return -1;
        } else if (lhs.getValue() > rhs.getValue()) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 获取最小的时间对象
     */
    public static AVTime timeMinimum(@NonNull AVTime lhs, @NonNull AVTime rhs) {
        if (lhs == AVTime.kAVTimeInvalid) {
            return lhs;
        }
        if (rhs == AVTime.kAVTimeInvalid) {
            return rhs;
        }
        if (lhs.getTimescale() != rhs.getTimescale()) {
            // 求出时间刻度的最小公倍数
            int timeScale = getLCM(lhs.getTimescale(), rhs.getTimescale());
            // 转换成最小公倍数的时间刻度
            if (timeScale != lhs.getTimescale()) {
                lhs = timeConvertScale(lhs, timeScale);
            }
            // 转换成最小公倍数的时间刻度
            if (timeScale != rhs.getTimescale()) {
                rhs = timeConvertScale(rhs, timeScale);
            }
        }

        if (lhs.getValue() > rhs.getValue()) {
            return rhs;
        } else {
            return lhs;
        }
    }

    /**
     * 获取较大的时间对象
     */
    public static AVTime timeMaximum(@NonNull AVTime lhs, @NonNull AVTime rhs) {
        if (lhs == AVTime.kAVTimeInvalid) {
            return rhs;
        }
        if (rhs == AVTime.kAVTimeInvalid) {
            return lhs;
        }

        if (lhs.getTimescale() != rhs.getTimescale()) {
            // 求出时间刻度的最小公倍数
            int timeScale = getLCM(lhs.getTimescale(), rhs.getTimescale());
            // 转换成最小公倍数的时间刻度
            if (timeScale != lhs.getTimescale()) {
                lhs = timeConvertScale(lhs, timeScale);
            }
            // 转换成最小公倍数的时间刻度
            if (timeScale != rhs.getTimescale()) {
                rhs = timeConvertScale(rhs, timeScale);
            }
        }

        if (lhs.getValue() > rhs.getValue()) {
            return lhs;
        } else {
            return rhs;
        }
    }

    /**
     * 欧几里得算法求最大公约数
     */
    public static int getGCD(int lhs, int rhs) {
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
     * 求最小公倍数
     */
    public static int getLCM(int lhs, int rhs) {
        return lhs * (rhs / getGCD(lhs, rhs));
    }
}
