package com.cgfay.picker.loader;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import com.cgfay.picker.model.AlbumData;
import com.cgfay.picker.model.MimeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 相册加载器
 */
public class AlbumDataLoader extends CursorLoader {


    public static final String COLUMN_BUCKET_ID = "bucket_id";
    public static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_COUNT = "count";
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

    private static final String[] COLUMNS = {
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            COLUMN_URI,
            COLUMN_COUNT,
    };

    private static final String[] PROJECTION_Q = {
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
    };

    private static final String SELECTION_ALL_Q = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private static final String SELECTION_FOR_SINGLE_MEDIA_TYPE_Q =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[] { String.valueOf(mediaType) };
    }

    private static final String NORMAL_ORDER_BY = "datetaken DESC";
    // 优先排序Camera目录
    private static final String BUCKET_ORDER_BY = "CASE bucket_display_name WHEN 'Camera' THEN 1 ELSE 100 END ASC, datetaken DESC";

    private AlbumDataLoader(@NonNull Context context, String selection, String[] selectionArgs) {
        super(context, QUERY_URI, PROJECTION_Q, selection, selectionArgs, BUCKET_ORDER_BY);
    }

    private AlbumDataLoader(@NonNull Context context, String selection, String[] selectionArgs, String order) {
        super(context, QUERY_URI, PROJECTION_Q, selection, selectionArgs, order);
    }

    /**
     * 获取图片加载器，相册不做排序
     * @param context
     * @return
     */
    public static CursorLoader getImageLoaderWithoutBucketSort(@NonNull Context context) {
        return new AlbumDataLoader(context, SELECTION_FOR_SINGLE_MEDIA_TYPE_Q,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), NORMAL_ORDER_BY);
    }

    public static CursorLoader getImageLoader(@NonNull Context context) {
        return new AlbumDataLoader(context, SELECTION_FOR_SINGLE_MEDIA_TYPE_Q,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE));
    }

    public static CursorLoader getVideoLoader(@NonNull Context context) {
        return new AlbumDataLoader(context, SELECTION_FOR_SINGLE_MEDIA_TYPE_Q,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO));
    }

    public static CursorLoader getAllLoader(@NonNull Context context) {
        return new AlbumDataLoader(context, SELECTION_ALL_Q, SELECTION_ALL_ARGS);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor albums = super.loadInBackground();
        MatrixCursor allAlbum = new MatrixCursor(COLUMNS);
        int totalCount = 0;
        Uri allAlbumCoverUri = null;

        // Pseudo GROUP BY
        @SuppressLint("UseSparseArrays")
        Map<Long, Long> countMap = new HashMap<>();
        if (albums != null) {
            while (albums.moveToNext()) {
                long bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID));
                Long count = countMap.get(bucketId);
                if (count == null) {
                    count = 1L;
                } else {
                    count++;
                }
                countMap.put(bucketId, count);
            }
        }

        // 支持的Cursor
        MatrixCursor supportAlbums = new MatrixCursor(COLUMNS);
        if (albums != null) {
            if (albums.moveToFirst()) {
                allAlbumCoverUri = getUri(albums);
                Set<Long> done = new HashSet<>();
                do {
                    long bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID));
                    if (done.contains(bucketId)) {
                        continue;
                    }
                    long fileId = albums.getLong(
                            albums.getColumnIndex(MediaStore.Files.FileColumns._ID));
                    String bucketDisplayName = albums.getString(
                            albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME));
                    String mimeType = albums.getString(
                            albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                    Uri uri = getUri(albums);
                    Long lCount = countMap.get(bucketId);
                    long count = lCount != null ? lCount : 0;
                    if (count > 1) {
                        supportAlbums.addRow(new String[]{
                                Long.toString(fileId),
                                Long.toString(bucketId),
                                bucketDisplayName,
                                mimeType,
                                uri.toString(),
                                String.valueOf(count)});
                        done.add(bucketId);
                        totalCount += count;
                    }
                } while (albums.moveToNext());
            }
        }

        // 所有照片
        allAlbum.addRow(new String[]{
                AlbumData.ALBUM_ID_ALL, AlbumData.ALBUM_ID_ALL, AlbumData.ALBUM_NAME_ALL, null,
                allAlbumCoverUri == null ? null : allAlbumCoverUri.toString(),
                String.valueOf(totalCount)});

        return new MergeCursor(new Cursor[]{allAlbum, supportAlbums});
    }

    private static Uri getUri(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
        String mimeType = cursor.getString(
                cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
        Uri contentUri;
        if (MimeType.isImage(mimeType)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (MimeType.isVideo(mimeType)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            // unknown
            contentUri = MediaStore.Files.getContentUri("external");
        }
        return ContentUris.withAppendedId(contentUri, id);
    }
}
