package com.cgfay.video.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 特效类型类型
 */
public class EffectType implements Parcelable {

    private EffectMimeType mimeType;    // 特效类型，滤镜特效、转场特效以及时间特效

    private String mThumb;  // 缩略图路径
    private String mName;   // 特效名
    private int id;         // 特效id，暂时没啥用。本来是预留做动态处理的id。

    public EffectType(EffectMimeType mimeType, String name, int id, String thumbPath) {
        this.mimeType = mimeType;
        this.mName = name;
        this.id = id;
        this.mThumb = thumbPath;
    }

    private EffectType(Parcel in) {
        id = in.readInt();
        mName = in.readString();
        mThumb = in.readString();
        mimeType = in.readParcelable(EffectMimeType.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(mName);
        dest.writeString(mThumb);
        dest.writeParcelable(mimeType, flags);
    }

    public EffectMimeType getMimeType() {
        return mimeType;
    }

    public String getThumb() {
        return mThumb;
    }

    public String getName() {
        return mName;
    }

    public int getId() {
        return id;
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
