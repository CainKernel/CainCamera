package com.cgfay.coremedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 时间范围
 */
public class AVTimeRange {

    public static final AVTimeRange kAVTimeRangeZero = new AVTimeRange(AVTime.kAVTimeZero, AVTime.kAVTimeZero);

    public static final AVTimeRange kAVTimeRangeInvalid = new AVTimeRange(AVTime.kAVTimeInvalid, AVTime.kAVTimeInvalid);

    private AVTime start;
    private AVTime duration;

    public AVTimeRange(@NonNull AVTime start, @NonNull AVTime duration) {
        this.start = start;
        this.duration = duration;
    }

    /**
     * 设置开始时间
     * @param start
     */
    public void setStart(AVTime start) {
        this.start = start;
    }

    /**
     * 获取开始时间
     */
    public AVTime getStart() {
        return start;
    }

    /**
     * 获取时长
     */
    public AVTime getDuration() {
        return duration;
    }

    /**
     * 获取结束位置
     * @return
     */
    public AVTime getEnd() {
        return AVTimeUtils.timeAdd(start, duration);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof AVTimeRange) {
            AVTime start = ((AVTimeRange) obj).getStart();
            AVTime duration = ((AVTimeRange) obj).getDuration();
            if (this.start.equals(start) && this.duration.equals(duration)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + start.toString() + ", " + duration.toString() + "}";
    }
}
