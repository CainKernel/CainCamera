package com.cgfay.picker.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;

/**
 * 媒体数据对象
 */
public class MediaData implements Parcelable {

    public static final Creator<MediaData> CREATOR = new Creator<MediaData>() {
        @Override
        public MediaData createFromParcel(Parcel source) {
            return new MediaData(source);
        }

        @Override
        public MediaData[] newArray(int size) {
            return new MediaData[size];
        }
    };

    private String mMimeType;
    private String mPath;
    private long mSize;
    private long mDuration;
    private int mWidth;
    private int mHeight;

    public MediaData(@NonNull Cursor cursor) throws Exception {
        mPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
        File file = new File(mPath);
        if (file.exists() && !file.isDirectory()) {
            mMimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
            mWidth = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH));
            mHeight = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT));
            if (isVideo()) {
                int durationId = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION);
                mDuration = cursor.getLong(durationId);
            } else {
                mDuration = 0;
            }
        } else {
            throw new Exception("File not exit!");
        }
    }

    private MediaData(Parcel source) {
        mMimeType = source.readString();
        mPath = source.readString();
        mSize = source.readLong();
        mDuration = source.readLong();
        mWidth = source.readInt();
        mHeight = source.readInt();
    }

    public static MediaData valueOf(Cursor cursor) {
        MediaData mediaData = null;
        try {
            mediaData = new MediaData(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return mediaData;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMimeType);
        dest.writeString(mPath);
        dest.writeLong(mSize);
        dest.writeLong(mDuration);
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
    }

    @NonNull
    public String getPath() {
        return mPath;
    }

    @NonNull
    public String getMimeType() {
        return mMimeType;
    }

    public long getSize() {
        return mSize;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    /**
     * 是否图片
     */
    public boolean isImage() {
        if (TextUtils.isEmpty(mMimeType)) {
            return false;
        }
        return mMimeType.equals(MimeType.JPEG.getMimeType())
                || mMimeType.equals(MimeType.JPG.getMimeType())
                || mMimeType.equals(MimeType.BMP.getMimeType())
                || mMimeType.equals(MimeType.PNG.getMimeType());
    }

    /**
     * 是否视频
     */
    public boolean isVideo() {
        if (TextUtils.isEmpty(mMimeType)) {
            return false;
        }
        return mMimeType.equals(MimeType.MPEG.getMimeType())
                || mMimeType.equals(MimeType.MP4.getMimeType())
                || mMimeType.equals(MimeType.GPP.getMimeType())
                || mMimeType.equals(MimeType.MKV.getMimeType())
                || mMimeType.equals(MimeType.AVI.getMimeType());
    }

    /**
     * 是否gif
     */
    public boolean isGif() {
        if (TextUtils.isEmpty(mMimeType)) {
            return false;
        }
        return mMimeType.contentEquals(MimeType.GIF.getMimeType());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MediaData)) {
            return false;
        }
        MediaData item = (MediaData) obj;
        return (!TextUtils.isEmpty(mPath) && mMimeType.equals(item.mMimeType))
                && (!TextUtils.isEmpty(mPath) && mPath.equals(item.mPath))
                && (mSize == item.mSize)
                && (mDuration == item.mDuration)
                && (mWidth == item.mWidth)
                && (mHeight == item.mHeight);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + mMimeType.hashCode();
        result = 31 * result + mPath.hashCode();
        result = 31 * result + Long.valueOf(mSize).hashCode();
        result = 31 * result + Long.valueOf(mDuration).hashCode();
        result = 31 * result + Long.valueOf(mWidth).hashCode();
        result = 31 * result + Long.valueOf(mHeight).hashCode();
        return result;
    }
}
