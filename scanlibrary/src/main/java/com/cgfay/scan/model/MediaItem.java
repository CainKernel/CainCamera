package com.cgfay.scan.model;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * 媒体item对象
 */
public class MediaItem implements Parcelable {

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel source) {
            return new MediaItem(source);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    public static final long CAMERA_ID = -1; // 相机拍照
    public static final String CAMAERA_NAME = "Camera";
    public final long id;
    public final String mimeType;
    public final Uri uri;
    public final long size;
    public final long duration;

    private MediaItem(long id, String mimeType, long size, long duration) {
        this.id = id;
        this.mimeType = mimeType;
        Uri contentUri;
        if (isImage()) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (isVideo()) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        this.uri = ContentUris.withAppendedId(contentUri, id);
        this.size = size;
        this.duration = duration;
    }

    private MediaItem(Parcel source) {
        id = source.readLong();
        mimeType = source.readString();
        uri = source.readParcelable(Uri.class.getClassLoader());
        size = source.readLong();
        duration = source.readLong();
    }

    public static MediaItem valueOf(Cursor cursor) {
        return new MediaItem(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)),
                cursor.getLong(cursor.getColumnIndex("duration")));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(mimeType);
        dest.writeParcelable(uri, 0);
        dest.writeLong(size);
        dest.writeLong(duration);
    }

    /**
     * 获取uri
     * @return
     */
    public Uri getContentUri() {
        return uri;
    }

    /**
     * 是否相机
     * @return
     */
    public boolean isCapture() {
        return id == CAMERA_ID;
    }

    /**
     * 是否图片
     * @return
     */
    public boolean isImage() {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.equals(MimeType.JPEG.toString())
                || mimeType.equals(MimeType.PNG.toString())
                || mimeType.equals(MimeType.GIF.toString());
    }

    /**
     * 是否视频
     * @return
     */
    public boolean isVideo() {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.equals(MimeType.MPEG.toString())
                || mimeType.equals(MimeType.MP4.toString())
                || mimeType.equals(MimeType.MKV.toString())
                || mimeType.equals(MimeType.AVI.toString());
    }

    /**
     * 是否gif
     * @return
     */
    public boolean isGif() {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.contentEquals(MimeType.GIF.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MediaItem)) {
            return false;
        }
        MediaItem item = (MediaItem) obj;
        return (id == item.id)
                && ((mimeType != null && mimeType.equals(item.mimeType))
                    || (mimeType == null && item.mimeType == null))
                && ((uri != null && uri.equals(item.uri))
                    || (uri == null &&item.uri == null))
                && (size == item.size)
                && (duration == item.duration);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Long.valueOf(id).hashCode();
        if (mimeType != null) {
            result = 31 * result + mimeType.hashCode();
        }
        result = 31 * result + uri.hashCode();
        result = 31 * result + Long.valueOf(size).hashCode();
        result = 31 * result + Long.valueOf(duration).hashCode();
        return result;
    }
}
