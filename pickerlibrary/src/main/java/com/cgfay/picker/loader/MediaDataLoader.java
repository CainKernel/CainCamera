package com.cgfay.picker.loader;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.loader.content.CursorLoader;

import com.cgfay.picker.model.AlbumData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * 媒体数据加载器
 */
public class MediaDataLoader extends CursorLoader {

    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

//    private final static String DISTINCT_DATA = "DISTINCT " + MediaStore.MediaColumns.DATA;

    private final static String[] PROJECTION_ALL = new String[] {
//            DISTINCT_DATA,
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
    };

    private final static String[] PROJECTION_IMAGE = new String[]{
//            DISTINCT_DATA,
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
    };

    private final static String[] PROJECTION_VIDEO = new String[]{
//            DISTINCT_DATA,
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.Video.VideoColumns.DURATION,
    };

    private static final String MEDIA_ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";

    private final static String MEDIA_SIZE = MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_ALL =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " or " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " and " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String[] SELECTION_ALL_TYPE_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private static final String SELECTION_ALBUM_ALL =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " or " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " and " + " bucket_id=?"
                    + " and " + MediaStore.MediaColumns.SIZE + ">0";

    private final static String SELECTION_IMAGE_TYPE = "(" + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + " or " + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + " or " + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + " or " + MediaStore.MediaColumns.MIME_TYPE + "=?)";

    private final static String[] SELECTION_IMAGE_TYPE_ARGS = {"image/jpeg", "image/jpg", "image/bmp", "image/png"};

    private final static String SELECTION_IMAGE_ALL = MEDIA_SIZE + " and " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            + " and " + SELECTION_IMAGE_TYPE;

    private static final String SELECTION_ALBUM_IMAGE_TYPE = SELECTION_IMAGE_TYPE + " and " + "bucket_id=?";

    private final static String SELECTION_ALBUM_IMAGE = MEDIA_SIZE + " and " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            + " and " + SELECTION_ALBUM_IMAGE_TYPE;

    private final static String SELECTION_VIDEO_TYPE = "(" + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + " or " + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + " or " + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + " or " + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + " or " + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + " or " + MediaStore.MediaColumns.MIME_TYPE + "=?"
            + ")";

    private final static String[] SELECTION_VIDEO_TYPE_ARGS = {"video/mpeg", "video/mp4", "video/m4v", "video/3gpp", "video/x-matroska", "video/avi"};

    private final static String SELECTION_VIDEO_ALL = MEDIA_SIZE + " and " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            + " and " + SELECTION_VIDEO_TYPE;

    private static final String SELECTION_ALBUM_VIDEO_TYPE = SELECTION_VIDEO_TYPE + " and " + "bucket_id=?";

    private final static String SELECTION_ALBUM_VIDEO = MEDIA_SIZE + " and " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            + " and " + SELECTION_ALBUM_VIDEO_TYPE;

    private MediaDataLoader(@NonNull Context context, @Nullable String[] projection, @NonNull String selection, @NonNull String[] selectionArgs) {
        super(context, QUERY_URI, projection, selection, selectionArgs, MEDIA_ORDER);
    }

    public static final int LOAD_ALL = 0;
    public static final int LOAD_VIDEO = 1;
    public static final int LOAD_IMAGE = 2;
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {LOAD_ALL, LOAD_VIDEO, LOAD_IMAGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoadMimeType {}

    /**
     * 创建媒体数据加载器
     * @param context
     * @param mimeType
     * @return
     */
    public static CursorLoader createMediaDataLoader(@NonNull Context context, @LoadMimeType int mimeType) {
        final String[] projection = getProjection(mimeType);
        final String selection;
        final String[] selectionArgs;
        switch (mimeType) {
            case LOAD_IMAGE: {
                selection = getImageSelection(true);
                selectionArgs = getAlbumSelectionImageType(AlbumData.ALBUM_ID_ALL);
                break;
            }

            case LOAD_VIDEO: {
                selection = getVideoSelection(true);
                selectionArgs = getAlbumSelectionVideoType(AlbumData.ALBUM_ID_ALL);
                break;
            }

            default: {
                selection = getImageAndVideoSelection(true);
                selectionArgs = getAlbumSelectionImageAndVideoType(AlbumData.ALBUM_ID_ALL);
                break;
            }
        }
        return new MediaDataLoader(context, projection, selection, selectionArgs);
    }

    /**
     * 创建媒体数据加载器
     * @param context
     * @param album
     * @param mimeType
     * @return
     */
    public static CursorLoader createMediaDataLoader(@NonNull Context context, @NonNull AlbumData album, @LoadMimeType int mimeType) {
        final String[] projection = getProjection(mimeType);
        final String selection;
        final String[] selectionArgs;

        switch (mimeType) {
            case LOAD_IMAGE: {
                selection = getImageSelection(album.isAll());
                selectionArgs = getAlbumSelectionImageType(album.getId());
                break;
            }

            case LOAD_VIDEO: {
                selection = getVideoSelection(album.isAll());
                selectionArgs = getAlbumSelectionVideoType(album.getId());
                break;
            }

            default: {
                selection =  getImageAndVideoSelection(album.isAll());
                selectionArgs = getAlbumSelectionImageAndVideoType(album.getId());
                break;
            }
        }

        return new MediaDataLoader(context, projection, selection, selectionArgs);
    }

    /**
     * 获取Projection
     * @param mimeType
     * @return
     */
    private static String[] getProjection(@LoadMimeType int mimeType) {
        switch (mimeType) {
            case LOAD_IMAGE:
                return PROJECTION_IMAGE;

            case LOAD_VIDEO:
                return PROJECTION_VIDEO;

            default:
                return PROJECTION_ALL;
        }
    }

    /**
     * 获取图片selection
     * @param all   所有相册
     * @return
     */
    private static String getImageSelection(boolean all) {
        if (all) {
            return SELECTION_IMAGE_ALL;
        }
        return SELECTION_ALBUM_IMAGE;
    }

    /**
     * 获取某个相册的图片类型
     * @param bucketId 相册id
     * @return
     */
    private static String[] getAlbumSelectionImageType(@NonNull String bucketId) {
        if (bucketId.equals(AlbumData.ALBUM_ID_ALL)) {
            return SELECTION_IMAGE_TYPE_ARGS;
        }
        List<String> selectionType = new ArrayList<>();
        Collections.addAll(selectionType, SELECTION_IMAGE_TYPE_ARGS);
        selectionType.add(bucketId);
        String[] selection = new String[selectionType.size()];
        selectionType.toArray(selection);
        return selection;
    }

    /**
     * 获取视频Selection
     * @param all 是否所有相册
     * @return
     */
    private static String getVideoSelection(boolean all) {
        if (all) {
            return SELECTION_VIDEO_ALL;
        }
        return SELECTION_ALBUM_VIDEO;
    }

    /**
     * 获取某个相册的视频类型
     */
    private static String[] getAlbumSelectionVideoType(@NonNull String bucketId) {
        if (bucketId.equals(AlbumData.ALBUM_ID_ALL)) {
            return SELECTION_VIDEO_TYPE_ARGS;
        }
        List<String> selectionType = new ArrayList<>();
        Collections.addAll(selectionType, SELECTION_VIDEO_TYPE_ARGS);
        selectionType.add(bucketId);
        String[] selection = new String[selectionType.size()];
        selectionType.toArray(selection);
        return selection;
    }

    /**
     * 获取图片和视频的Selection
     * @param all
     * @return
     */
    private static String getImageAndVideoSelection(boolean all) {
        if (all) {
            return SELECTION_ALL;
        }
        return SELECTION_ALBUM_ALL;
    }

    /**
     * 获取图片和视频类型
     * @param bucketId
     * @return
     */
    public static String[] getAlbumSelectionImageAndVideoType(@NonNull String bucketId) {
        if (bucketId.equals(AlbumData.ALBUM_ID_ALL)) {
            return SELECTION_ALL_TYPE_ARGS;
        }
        return new String[] {
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                bucketId
        };
    }
}
