package com.cgfay.picker.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.cgfay.picker.loader.AlbumDataLoader;

/**
 * 相册数据对象
 */
public class AlbumData implements Parcelable {

    public static final Creator<AlbumData> CREATOR = new Creator<AlbumData>() {
        @Override
        public AlbumData createFromParcel(Parcel source) {
            return new AlbumData(source);
        }

        @Override
        public AlbumData[] newArray(int size) {
            return new AlbumData[size];
        }
    };

    public static final String ALBUM_ID_ALL = "-1";
    public static final String ALBUM_NAME_ALL = "All";

    private final String mId;
    private final Uri mCoverPath;
    private final String mDisplayName;
    private long mCount;

    public AlbumData(String id, Uri coverUri, String displayName, long count) {
        mId = id;
        mCoverPath = coverUri;
        mDisplayName = displayName;
        mCount = count;
    }

    private AlbumData(Parcel source) {
        mId = source.readString();
        mCoverPath = source.readParcelable(Uri.class.getClassLoader());
        mDisplayName = source.readString();
        mCount = source.readLong();
    }

    public static AlbumData valueOf(Cursor cursor) {
        int index = cursor.getColumnIndex(AlbumDataLoader.COLUMN_URI);
        int count = cursor.getColumnIndex(AlbumDataLoader.COLUMN_COUNT);
        String uri = index >= 0 ? cursor.getString(index) : null;
        return new AlbumData(cursor.getString(cursor.getColumnIndex("bucket_id")),
                Uri.parse(uri != null ? uri : ""),
                cursor.getString(cursor.getColumnIndex("bucket_display_name")),
                count > 0 ? cursor.getLong(count) : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeParcelable(mCoverPath, 0);
        dest.writeString(mDisplayName);
        dest.writeLong(mCount);
    }

    public String getId() {
        return mId;
    }

    /**
     * 获取封面路径
     * @return
     */
    public Uri getCoverUri() {
        return mCoverPath;
    }

    /**
     * 获取显示名称
     * @return
     */
    public String getDisplayName() {
        if (isAll()) {
            return "所有照片";
        }
        return mDisplayName;
    }

    /**
     * 获取相册数量
     * @return
     */
    public long getCount() {
        return mCount;
    }

    /**
     * 加入拍摄item
     */
    public void addCaptureCount() {
        mCount++;
    }

    public boolean isAll() {
        return ALBUM_ID_ALL.equals(mId);
    }

    public boolean isEmpty() {
        return mCount == 0;
    }

    @Override
    public String toString() {
        return "AlbumData{" +
                "mId='" + mId + '\'' +
                ", mCoverPath='" + mCoverPath + '\'' +
                ", mDisplayName='" + mDisplayName + '\'' +
                ", mCount=" + mCount +
                '}';
    }
}
