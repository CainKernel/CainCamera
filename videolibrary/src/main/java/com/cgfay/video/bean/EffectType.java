package com.cgfay.video.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 特效类型类型
 */
public class EffectType implements Parcelable {

    private EffectMimeType mimeType;    // 特效类型，滤镜特效、转场特效以及时间特效

    private String mName;   // 特效名
    private int mFilter;    // 特效id

    public EffectType(EffectMimeType mimeType, String name, int filter) {
        this.mimeType = mimeType;
        this.mName = name;
        this.mFilter = filter;
    }

    private EffectType(Parcel in) {
        mFilter = in.readInt();
        mName = in.readString();
        mimeType = in.readParcelable(EffectMimeType.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mFilter);
        dest.writeString(mName);
        dest.writeParcelable(mimeType, flags);
    }

    public static final Creator<EffectType> CREATOR = new Creator<EffectType>() {
        @Override
        public EffectType createFromParcel(Parcel source) {
            return new EffectType(source);
        }

        @Override
        public EffectType[] newArray(int size) {
            return new EffectType[size];
        }
    };
}
