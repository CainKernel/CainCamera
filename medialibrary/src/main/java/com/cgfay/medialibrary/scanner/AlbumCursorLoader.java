package com.cgfay.medialibrary.scanner;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import com.cgfay.medialibrary.model.AlbumItem;
import com.cgfay.medialibrary.engine.MediaScanParam;

/**
 * 相册加载器
 */
public class AlbumCursorLoader extends CursorLoader {

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

    private static final String BUCKET_ORDER_BY = "datetaken DESC";


    private AlbumCursorLoader(Context context, String selection, String[] selectionArgs) {
        super(context, QUERY_URI, PROJECTION, selection, selectionArgs, BUCKET_ORDER_BY);
    }

    public static CursorLoader newInstance(Context context) {
        String selection;
        String[] selectionArgs;
        if (MediaScanParam.getInstance().showImageOnly()) {
            selection = SELECTION_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        } else if (MediaScanParam.getInstance().showVideoOnly()) {
            selection = SELECTION_FOR_SINGLE_MEDIA_TYPE;
            selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        } else {
            selection = SELECTION_ALL;
            selectionArgs = SELECTION_ALL_ARGS;
        }
        return new AlbumCursorLoader(context, selection, selectionArgs);
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
        allAlbum.addRow(new String[] {AlbumItem.ALBUM_ID_ALL, AlbumItem.ALBUM_ID_ALL,
                AlbumItem.ALBUM_NAME_ALL, coverPath, String.valueOf(count)});

        return new MergeCursor(new Cursor[]{allAlbum, albums});
    }
}
