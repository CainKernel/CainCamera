package com.cgfay.scan.scanner;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import com.cgfay.scan.model.AlbumItem;
import com.cgfay.scan.model.MediaItem;
import com.cgfay.scan.engine.MediaScanParam;
import com.cgfay.scan.utils.MediaStoreUtils;


/**
 * 多媒体加载器
 */
public class MediaCursorLoader extends CursorLoader {

    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            "duration"
    };

    private static final String SELECTION_ALL =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private static final String SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[] {String.valueOf(mediaType)};
    }

    private static final String SELECTION_ALBUM =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + " bucket_id=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static String [] getSelectionAlbumArgs(String albumId) {
        return new String[] {
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                albumId
        };
    }

    private static final String SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + " bucket_id=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static String[] getSelectionAlbumArgsForSingleMediaType(int mediaType, String albumId) {
        return new String[] {String.valueOf(mediaType), albumId};
    }

    private static final String ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC";
    private final boolean mCaptureEnable; // 是否允许拍照item

    private MediaCursorLoader(Context context, String selection, String[] selectionArgs, boolean captureEnable) {
        super(context, QUERY_URI, PROJECTION, selection, selectionArgs, ORDER_BY);
        mCaptureEnable = captureEnable;
    }

    public static CursorLoader newInstance(Context context, AlbumItem albumItem, boolean captureEnable) {
        String selection;
        String[] selectionArgs;
        if (albumItem.isAll()) {
            if (MediaScanParam.getInstance().showImageOnly()) {
                selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE;
                selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            } else if (MediaScanParam.getInstance().showVideoOnly()) {
                selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE;
                selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
            } else {
                selection = SELECTION_ALL;
                selectionArgs = SELECTION_ALL_ARGS;
            }
        } else {
            if (MediaScanParam.getInstance().showImageOnly()) {
                selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE;
                selectionArgs = getSelectionAlbumArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                        albumItem.getId());
            } else if (MediaScanParam.getInstance().showVideoOnly()) {
                selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE;
                selectionArgs = getSelectionAlbumArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                        albumItem.getId());
            } else {
                selection = SELECTION_ALBUM;
                selectionArgs = getSelectionAlbumArgs(albumItem.getId());
            }

        }
        return new MediaCursorLoader(context, selection, selectionArgs, captureEnable);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor result = super.loadInBackground();
        if (!mCaptureEnable || !MediaStoreUtils.hasCameraFeature(getContext())) {
            return result;
        }
        // 添加一个拍照的Item
        MatrixCursor dummy = new MatrixCursor(PROJECTION);
        dummy.addRow(new Object[]{MediaItem.CAMERA_ID, MediaItem.CAMAERA_NAME, "", 0, 0});
        return new MergeCursor(new Cursor[]{dummy, result});
    }
}
