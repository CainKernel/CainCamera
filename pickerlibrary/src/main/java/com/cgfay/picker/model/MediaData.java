package com.cgfay.picker.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.picker.utils.MediaMetadataUtils;
import com.cgfay.picker.utils.UriToPathUtils;

import java.io.File;

/**
 * 媒体数据对象
 */
public class MediaData implements Parcelable {

    private static final String TAG = "MediaData";

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
    private int mOrientation;

    public MediaData(@NonNull Context context, @NonNull Cursor cursor) throws Exception {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
        mMimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
        mWidth = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH));
        mHeight = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT));
        Uri contentUri;
        if (isImage()) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (isVideo()) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        Uri uri = ContentUris.withAppendedId(contentUri, id);
        mPath = UriToPathUtils.getPath(context, uri);
        if (!TextUtils.isEmpty(mPath)) {
            File file = new File(mPath);
            if (file.exists() && !file.isDirectory()) {
                if (isVideo()) {
                    int durationId = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION);
                    mDuration = cursor.getLong(durationId);
                } else {
                    mDuration = 0;
                }
//                // 如果是否需要过滤宽高异常的图片？？？
//                if (isImage()) {
//                    MediaMetadataUtils.buildImageMetadata(this);
//                    if (mWidth <=0 || mHeight <= 0) {
//                        throw new Exception("width or height is anomaly!");
//                    }
//                }
            } else {
                throw new Exception("File not exit!");
            }
        } else {
            throw new Exception("path not exit!");
        }
    }

    private MediaData(Parcel source) {
        mMimeType = source.readString();
        mPath = source.readString();
        mSize = source.readLong();
        mDuration = source.readLong();
        mWidth = source.readInt();
        mHeight = source.readInt();
        mOrientation = source.readInt();
    }

    public static MediaData valueOf(@NonNull Context context, @NonNull Cursor cursor) {
        MediaData mediaData = null;
        try {
            mediaData = new MediaData(context, cursor);
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
        dest.writeInt(mOrientation);
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

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public int getOrientation() {
        return mOrientation;
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
