package com.cgfay.coremedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 时间刻度
 */
public class AVTime {

    /**
     * 24fp、30fps、60fps的最小公约数
     */
    public static final int DEFAULT_TIME_SCALE = 600;


    public static final AVTime kAVTimeZero = new AVTime();

    public static final AVTime kAVTimeInvalid = new AVTime(Long.MIN_VALUE, 0);

    /**
     * 时间值
     */
    private long value;

    /**
     * 时间刻度
     */
    private int timescale;

    public AVTime() {
        this(0, DEFAULT_TIME_SCALE);
    }

    public AVTime(long value, int timescale) {
        this.value = value;
        this.timescale = timescale;
    }

    /**
     * 设置时间值
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * 获取时间值
     */
    public long getValue() {
        return value;
    }

    /**
     * 设置时间刻度
     */
    public void setTimescale(int timescale) {
        this.timescale = timescale;
    }

    /**
     * 获取时间刻度
     */
    public int getTimescale() {
        return timescale;
    }

    /**
     * 获取时间(s)
     */
    public float getSeconds() {
        if (timescale != 0) {
            return value * 1.0f / timescale;
        } else if (value > 0) {
            return Float.POSITIVE_INFINITY;
        } else if (value < 0) {
            return Float.NEGATIVE_INFINITY;
        }
        return Float.NaN;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof AVTime) {
            int timescale = ((AVTime) obj).getTimescale();
            long value = ((AVTime) obj).getValue();
            if (timescale == this.timescale) {
                return this.value == value;
            }
            if (timescale != 0 && this.timescale != 0) {
                return value * this.timescale == this.value * timescale;
            }
        }
        return false;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "{" + value + "/" + timescale + " = " + ((timescale == 0) ? "Invalid"
                : (float) (value / timescale)) + "}";
    }
}
