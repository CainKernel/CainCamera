package com.cgfay.video.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 特效类型
 */
public enum EffectMimeType implements Parcelable {

    FILTER(0),      // 滤镜特效
    TRANSITION(1),  // 转场特效
    MULTIFRAME(2),  // 分屏特效
    TIME(3);        // 时间特效

    private int mimeType;

    EffectMimeType(int type) {
        this.mimeType = type;
    }

    public int getMimeType() {
        return mimeType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mimeType);
    }

    public static final Creator<EffectMimeType> CREATOR = new Creator<EffectMimeType>() {
        @Override
        public EffectMimeType createFromParcel(Parcel in) {
            return EffectMimeType.values()[in.readInt()];
        }

        @Override
        public EffectMimeType[] newArray(int size) {
            return new EffectMimeType[size];
        }
    };
}
