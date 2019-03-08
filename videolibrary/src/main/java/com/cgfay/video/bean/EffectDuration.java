package com.cgfay.video.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 特效选中时长对象，包含起始时间、结束时间
 */
public class EffectDuration implements Parcelable {

    private EffectType mEffectType; // 特效类型
    private int mSelectedColor;     // 选中的颜色
    private long mTimeStart;        // 选中起始时间
    private long mTimeEnd;          // 选中结束时间

    public EffectDuration(EffectType effectType, long start, long end) {
        this.mEffectType = effectType;
        this.mTimeStart = start;
        this.mTimeEnd = end;
    }

    private EffectDuration(Parcel source) {
        this.mSelectedColor = source.readInt();
        this.mTimeStart = source.readLong();
        this.mTimeEnd = source.readLong();
        this.mEffectType = source.readParcelable(mEffectType.getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mSelectedColor);
        dest.writeLong(mTimeStart);
        dest.writeLong(mTimeEnd);
        dest.writeParcelable(mEffectType, flags);
    }

    public EffectType getEffectType() {
        return mEffectType;
    }

    public void setEffectType(EffectType type) {
        this.mEffectType = type;
    }

    public void setColor(int color) {
        this.mSelectedColor = color;
    }

    public int getColor() {
        return mSelectedColor;
    }

    public long getStart() {
        return mTimeStart;
    }

    public void setStart(long start) {
        this.mTimeStart = start;
    }

    public long getEnd() {
        return mTimeEnd;
    }

    public void setEnd(long end) {
        this.mTimeEnd = end;
    }

    public static final Creator<EffectDuration> CREATOR = new Creator<EffectDuration>() {
        @Override
        public EffectDuration createFromParcel(Parcel source) {
            return new EffectDuration(source);
        }

        @Override
        public EffectDuration[] newArray(int size) {
            return new EffectDuration[size];
        }
    };

}
