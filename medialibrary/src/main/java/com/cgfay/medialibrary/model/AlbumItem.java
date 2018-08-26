package com.cgfay.medialibrary.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.cgfay.medialibrary.R;
import com.cgfay.medialibrary.scanner.AlbumCursorLoader;

/**
 * 相册item对象
 */
public class AlbumItem implements Parcelable {

    public static final Creator<AlbumItem> CREATOR = new Creator<AlbumItem>() {
        @Override
        public AlbumItem createFromParcel(Parcel source) {
            return new AlbumItem(source);
        }

        @Override
        public AlbumItem[] newArray(int size) {
            return new AlbumItem[size];
        }
    };

    public static final String ALBUM_ID_ALL = "-1";
    public static final String ALBUM_NAME_ALL = "All";

    private final String id;
    private final String coverPath;
    private final String displayName;
    private long count;

    AlbumItem(String id, String coverPath, String displayName, long count) {
        this.id = id;
        this.coverPath = coverPath;
        this.displayName = displayName;
        this.count = count;
    }

    AlbumItem(Parcel source) {
        id = source.readString();
        coverPath = source.readString();
        displayName = source.readString();
        count = source.readLong();
    }

    public static AlbumItem valueOf(Cursor cursor) {
        return new AlbumItem(cursor.getString(cursor.getColumnIndex("bucket_id")),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)),
                cursor.getString(cursor.getColumnIndex("bucket_display_name")),
                cursor.getLong(cursor.getColumnIndex(AlbumCursorLoader.COLUMN_COUNT)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(coverPath);
        dest.writeString(displayName);
        dest.writeLong(count);
    }

    public String getId() {
        return id;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public String getDisplayName(Context context) {
        if (isAll()) {
            return context.getString(R.string.album_name_all);
        }
        return displayName;
    }

    public long getCount() {
        return count;
    }

    public void addCaptureCount() {
        count++;
    }

    public boolean isAll() {
        return ALBUM_ID_ALL.equals(id);
    }

    public boolean isEmpty() {
        return count == 0;
    }
}
