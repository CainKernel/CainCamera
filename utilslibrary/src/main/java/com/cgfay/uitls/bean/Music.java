package com.cgfay.uitls.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * 音频数据
 */
public class Music implements Parcelable {

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel source) {
            return new Music(source);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    private long id;          // id
    private String name;        // 歌曲名
    private String songUrl;     // 路径
    private long duration;       // 时长

    public Music(long id, String name, String songUrl, long duration) {
        this.id = id;
        this.name = name;
        this.songUrl = songUrl;
        this.duration = duration;
    }

    private Music(Parcel source) {
        id = source.readLong();
        name =  source.readString();
        songUrl = source.readString();
        duration = source.readInt();
    }

    public static Music valueof(Cursor cursor) {
        return new Music(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
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
        dest.writeString(songUrl);
        dest.writeLong(duration);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Music)) {
            return false;
        }
        Music music = (Music)obj;

        return (id == music.id)
                && ((!TextUtils.isEmpty(name) && name.equals(music.name))
                || (TextUtils.isEmpty(name) && TextUtils.isEmpty(music.name)))
                && ((!TextUtils.isEmpty(songUrl) && songUrl.equals(music.songUrl))
                || (TextUtils.isEmpty(songUrl) && TextUtils.isEmpty(music.songUrl)))
                && (duration == music.duration);
    }

    @Override
    public int hashCode() {
        int result = -1;
        result = 31 * result + Long.valueOf(id).hashCode();
        if (!TextUtils.isEmpty(name)) {
            result = 31 * result + name.hashCode();
        }
        if (!TextUtils.isEmpty(songUrl)) {
            result = 31 * result + songUrl.hashCode();
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

    public String getSongUrl() {
        return songUrl;
    }

    public long getDuration() {
        return duration;
    }
}