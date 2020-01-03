package com.cgfay.video.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 特效类型
 */
public enum EffectMimeType implements Parcelable {

    FILTER("滤镜", 0),      // 滤镜特效
    TRANSITION("转场", 1),  // 转场特效
    MULTIFRAME("分屏", 2),  // 分屏特效
    TIME("时间", 3);        // 时间特效

    private int mimeType;
    private String mName;

    EffectMimeType(String name, int type) {
        this.mName = name;
        this.mimeType = type;
    }

    public int getMimeType() {
        return mimeType;
    }

    public String getName() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mimeType);
    }

    public static final Creator<EffectMimeType> CREATOR = new Creator<EffectMimeType>() {
        @Override
        public EffectMimeType createFromParcel(Parcel in) {
            return EffectMimeType.valueOf(in.readString());
        }

        @Override
        public EffectMimeType[] newArray(int size) {
            return new EffectMimeType[size];
        }
    };
}
