package com.cgfay.uitls.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * 音乐数据
 */
public class MusicData implements Parcelable {

    public static final Creator<MusicData> CREATOR = new Creator<MusicData>() {
        @Override
        public MusicData createFromParcel(Parcel source) {
            return new MusicData(source);
        }

        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }
    };

    private long id;            // id
    private String name;        // 歌曲名
    private String mPath;       // 路径
    private long duration;      // 时长

    public MusicData(long id, String name, String mPath, long duration) {
        this.id = id;
        this.name = name;
        this.mPath = mPath;
        this.duration = duration;
    }

    private MusicData(Parcel source) {
        id = source.readLong();
        name =  source.readString();
        mPath = source.readString();
        duration = source.readInt();
    }

    public static MusicData valueof(Cursor cursor) {
        return new MusicData(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(mPath);
        dest.writeLong(duration);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MusicData)) {
            return false;
        }
        MusicData musicData = (MusicData)obj;

        return (id == musicData.id)
                && ((!TextUtils.isEmpty(name) && name.equals(musicData.name))
                || (TextUtils.isEmpty(name) && TextUtils.isEmpty(musicData.name)))
                && ((!TextUtils.isEmpty(mPath) && mPath.equals(musicData.mPath))
                || (TextUtils.isEmpty(mPath) && TextUtils.isEmpty(musicData.mPath)))
                && (duration == musicData.duration);
    }

    @Override
    public int hashCode() {
        int result = -1;
        result = 31 * result + Long.valueOf(id).hashCode();
        if (!TextUtils.isEmpty(name)) {
            result = 31 * result + name.hashCode();
        }
        if (!TextUtils.isEmpty(mPath)) {
            result = 31 * result + mPath.hashCode();
        }
        result = 31 * result + Long.valueOf(duration).hashCode();
        return result;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return mPath;
    }

    public long getDuration() {
        return duration;
    }
}