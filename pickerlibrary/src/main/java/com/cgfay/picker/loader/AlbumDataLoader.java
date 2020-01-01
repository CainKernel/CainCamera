package com.cgfay.picker.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

import com.cgfay.picker.model.AlbumData;

/**
 * 相册加载器
 */
public class AlbumDataLoader extends CursorLoader {

    public static final String COLUMN_COUNT = "count";
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

    private static final String[] COLUMNS = {
      MediaStore.Files.FileColumns._ID,
      "bucket_id",
      "bucket_display_name",
      MediaStore.MediaColumns.DATA,
      COLUMN_COUNT,
    };

    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            "bucket_id",
            "bucket_display_name",
            MediaStore.MediaColumns.DATA,
            "COUNT(*) AS " + COLUMN_COUNT
    };

    private static final String SELECTION_ALL =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + ") GROUP BY (bucket_id";

    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private static final String SELECTION_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + ") GROUP BY (bucket_id";

    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[] { String.valueOf(mediaType) };
    }

    private static final String NORMAL_ORDER_BY = "datetaken DESC";
    // 优先排序Camera目录
    private static final String BUCKET_ORDER_BY = "CASE bucket_display_name WHEN 'Camera' THEN 1 ELSE 100 END ASC, datetaken DESC";

    private AlbumDataLoader(@NonNull Context context, String selection, String[] selectionArgs) {
        super(context, QUERY_URI, PROJECTION, selection, selectionArgs, BUCKET_ORDER_BY);
    }

    private AlbumDataLoader(@NonNull Context context, String selection, String[] selectionArgs, String order) {
        super(context, QUERY_URI, PROJECTION, selection, selectionArgs, order);
    }

    /**
     * 获取图片加载器，相册不做排序
     * @param context
     * @return
     */
    public static CursorLoader getImageLoaderWithoutBucketSort(@NonNull Context context) {
        return new AlbumDataLoader(context, SELECTION_FOR_SINGLE_MEDIA_TYPE,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), NORMAL_ORDER_BY);
    }

    public static CursorLoader getImageLoader(@NonNull Context context) {
        return new AlbumDataLoader(context, SELECTION_FOR_SINGLE_MEDIA_TYPE,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE));
    }

    public static CursorLoader getVideoLoader(@NonNull Context context) {
        return new AlbumDataLoader(context, SELECTION_FOR_SINGLE_MEDIA_TYPE,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO));
    }

    public static CursorLoader getAllLoader(@NonNull Context context) {
        return new AlbumDataLoader(context, SELECTION_ALL, SELECTION_ALL_ARGS);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor albums = super.loadInBackground();
        MatrixCursor allAlbum = new MatrixCursor(COLUMNS);
        int count = 0;
        String coverPath = "";
        if (albums != null) {
            while (albums.moveToNext()) {
                count += albums.getInt(albums.getColumnIndex(COLUMN_COUNT));
            }
            if (albums.moveToFirst()) {
                coverPath = albums.getString(albums.getColumnIndex(MediaStore.MediaColumns.DATA));
            }
        }
        // 添加全部相册选项
        allAlbum.addRow(new String[] {AlbumData.ALBUM_ID_ALL, AlbumData.ALBUM_ID_ALL,
                AlbumData.ALBUM_NAME_ALL, coverPath, String.valueOf(count)});

        return new MergeCursor(new Cursor[]{allAlbum, albums});
    }
}
