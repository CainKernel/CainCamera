package com.cgfay.picker.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;

import com.cgfay.picker.utils.MediaMetadataUtils;
import com.cgfay.picker.utils.UriToPathUtils;

/**
 * 媒体数据对象
 */
public class MediaData implements Parcelable {

    private static final String TAG = "MediaData";
    private static final String EXTERNAL = "external";
    private static final String KEY_DURATION = "duration";
    private static final int KILO = 1000;
    private static final int DEFAULT_HASHCODE = 31;

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

    private long id;
    private final String mimeType;
    private final Uri uri;
    private final long size;
    private long durationMs;
    private int width;
    private int height;
    private int orientation;

    public MediaData(@NonNull Context context, @NonNull Cursor cursor) throws Exception {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
        width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH));
        height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT));
        size = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE));
        Uri contentUri;
        if (isImage()) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (isVideo()) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri(EXTERNAL);
        }
        this.uri = ContentUris.withAppendedId(contentUri, id);
        if (isVideo()) {
            int durationId;
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                durationId = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION);
            } else {
                durationId = cursor.getColumnIndexOrThrow(KEY_DURATION);
            }
            durationMs = cursor.getLong(durationId);
            if (durationMs == 0) {
                extractVideoMetadata(context);
            }
        } else {
            durationMs = 0;
        }
        Log.d(TAG, "MediaData: " + toString());
    }

    /**
     * 解析视频时长
     */
    private void extractVideoMetadata(@NonNull Context context) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(context, uri, null);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                if (format.containsKey(MediaFormat.KEY_MIME)) {
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (!TextUtils.isEmpty(mime) && mime.startsWith("video/")) {
                        durationMs = format.getLong(MediaFormat.KEY_DURATION) / KILO;
                        if (width == 0 || height == 0) {
                            width = format.getInteger(MediaFormat.KEY_WIDTH);
                            height = format.getInteger(MediaFormat.KEY_HEIGHT);
                        }
                        break;
                    }
                }
            }
        } finally {
            extractor.release();
        }
    }

    private MediaData(Parcel source) {
        id = source.readLong();
        mimeType = source.readString();
        uri = source.readParcelable(Uri.class.getClassLoader());
        size = source.readLong();
        durationMs = source.readLong();
        width = source.readInt();
        height = source.readInt();
        orientation = source.readInt();
    }

    public static MediaData valueOf(@NonNull Context context, @NonNull Cursor cursor) {
        MediaData mediaData = null;
        try {
            mediaData = new MediaData(context, cursor);
        } catch (Exception ignored) {
            // do nothing to ignored error media data
        }
        return mediaData;
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
        dest.writeLong(durationMs);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(orientation);
    }

    @NonNull
    public String getMimeType() {
        return mimeType;
    }

    public Uri getContentUri() {
        return uri;
    }

    public long getSize() {
        return size;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getOrientation() {
        return orientation;
    }

    /**
     * 是否图片
     */
    public boolean isImage() {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.equals(MimeType.JPEG.getMimeType())
                || mimeType.equals(MimeType.JPG.getMimeType())
                || mimeType.equals(MimeType.BMP.getMimeType())
                || mimeType.equals(MimeType.PNG.getMimeType());
    }

    /**
     * 是否视频
     */
    public boolean isVideo() {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.equals(MimeType.MPEG.getMimeType())
                || mimeType.equals(MimeType.MP4.getMimeType())
                || mimeType.equals(MimeType.GPP.getMimeType())
                || mimeType.equals(MimeType.MKV.getMimeType())
                || mimeType.equals(MimeType.AVI.getMimeType());
    }

    /**
     * 是否gif
     */
    public boolean isGif() {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        return mimeType.contentEquals(MimeType.GIF.getMimeType());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MediaData)) {
            return false;
        }
        MediaData other = (MediaData) obj;
        return (!TextUtils.isEmpty(mimeType) && mimeType.equals(other.mimeType))
                && (uri != null && uri.equals(other.uri)
                || (uri == null && other.uri == null))
                && (size == other.size)
                && (durationMs == other.durationMs)
                && (width == other.width)
                && (height == other.height);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = DEFAULT_HASHCODE * result + Long.valueOf(id).hashCode();
        if (!TextUtils.isEmpty(mimeType)) {
            result = DEFAULT_HASHCODE * result + mimeType.hashCode();
        }
        result = DEFAULT_HASHCODE * result + uri.hashCode();
        result = DEFAULT_HASHCODE * result + Long.valueOf(size).hashCode();
        result = DEFAULT_HASHCODE * result + Long.valueOf(durationMs).hashCode();
        result = DEFAULT_HASHCODE * result + Long.valueOf(width).hashCode();
        result = DEFAULT_HASHCODE * result + Long.valueOf(height).hashCode();
        return result;
    }
}
