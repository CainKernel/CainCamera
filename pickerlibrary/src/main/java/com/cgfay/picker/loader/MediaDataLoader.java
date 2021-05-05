package com.cgfay.picker.loader;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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

    private final static String[] PROJECTION_ALL = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
    };

    private final static String[] PROJECTION_IMAGE = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
    };

    private final static String[] PROJECTION_VIDEO = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
            "duration",
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private final static String[] PROJECTION_VIDEO_Q = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DURATION,
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static final String MEDIA_ORDER_Q = MediaStore.MediaColumns.DATE_TAKEN + " DESC";
    private static final String MEDIA_ORDER = "datetaken DESC";

    private final static String MEDIA_SIZE = MediaStore.MediaColumns.SIZE + ">0";

    private static final String[] SELECTION_ALL_TYPE_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private final static String[] SELECTION_IMAGE_TYPE_ARGS = {
            "image/jpeg", "image/jpg", "image/bmp", "image/png"
    };


    private final static String[] SELECTION_VIDEO_TYPE_ARGS = {
            "video/mpeg", "video/mp4", "video/m4v", "video/3gpp", "video/x-matroska", "video/avi"
    };

    private MediaDataLoader(@NonNull Context context, @Nullable String[] projection,
                            @NonNull String selection, @NonNull String[] selectionArgs) {
        super(context, QUERY_URI, projection, selection, selectionArgs,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MEDIA_ORDER_Q : MEDIA_ORDER);
    }

    public static final int LOAD_ALL = 0;
    public static final int LOAD_VIDEO = 1;
    public static final int LOAD_IMAGE = 2;

    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {LOAD_ALL, LOAD_VIDEO, LOAD_IMAGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoadMimeType {
    }

    /**
     * 创建媒体数据加载器
     */
    public static CursorLoader createMediaDataLoader(@NonNull Context context,
                                                     @LoadMimeType int mimeType) {
        final String[] projection = getProjection(mimeType);
        final String selection;
        final String[] selectionArgs;
        switch (mimeType) {
            case LOAD_IMAGE: {
                selection = getSelectionMimeType(LOAD_IMAGE, SELECTION_IMAGE_TYPE_ARGS);
                selectionArgs = getAlbumSelectionImageType(AlbumData.ALBUM_ID_ALL);
                break;
            }

            case LOAD_VIDEO: {
                selection = getSelectionMimeType(LOAD_VIDEO, SELECTION_VIDEO_TYPE_ARGS);
                selectionArgs = getAlbumSelectionVideoType(AlbumData.ALBUM_ID_ALL);
                break;
            }

            case LOAD_ALL:
            default: {
                selection = getSelectionMimeType(LOAD_ALL, null);
                selectionArgs = getAlbumSelectionImageAndVideoType(AlbumData.ALBUM_ID_ALL);
                break;
            }
        }
        return new MediaDataLoader(context, projection, selection, selectionArgs);
    }

    /**
     * 创建媒体数据加载器
     */
    public static CursorLoader createMediaDataLoader(@NonNull Context context,
                                                     @NonNull AlbumData album,
                                                     @LoadMimeType int mimeType) {
        final String[] projection = getProjection(mimeType);
        final String selection;
        final String[] selectionArgs;

        switch (mimeType) {
            case LOAD_IMAGE: {
                selection = getSelectionMimeType(LOAD_IMAGE, SELECTION_IMAGE_TYPE_ARGS,
                        album.getId());
                selectionArgs = getAlbumSelectionImageType(album.getId());
                break;
            }

            case LOAD_VIDEO: {
                selection = getSelectionMimeType(LOAD_VIDEO, SELECTION_VIDEO_TYPE_ARGS,
                        album.getId());
                selectionArgs = getAlbumSelectionVideoType(album.getId());
                break;
            }

            case LOAD_ALL:
            default: {
                selection = getSelectionMimeType(LOAD_ALL, null, album.getId());
                selectionArgs = getAlbumSelectionImageAndVideoType(album.getId());
                break;
            }
        }

        return new MediaDataLoader(context, projection, selection, selectionArgs);
    }

    /**
     * 获取Projection
     *
     * @param mimeType 媒体类型
     */
    private static String[] getProjection(@LoadMimeType int mimeType) {
        switch (mimeType) {
            case LOAD_IMAGE:
                return PROJECTION_IMAGE;

            case LOAD_VIDEO:
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? PROJECTION_VIDEO_Q : PROJECTION_VIDEO;

            case LOAD_ALL:
            default:
                return PROJECTION_ALL;
        }
    }

    /**
     * 获取某个相册的图片类型
     *
     * @param bucketId 相册id
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
     * 获取图片和视频类型
     */
    public static String[] getAlbumSelectionImageAndVideoType(@NonNull String bucketId) {
        if (bucketId.equals(AlbumData.ALBUM_ID_ALL)) {
            return SELECTION_ALL_TYPE_ARGS;
        }
        return new String[]{
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                bucketId
        };
    }

    /**
     * 根据mimeType构建selection字符串
     */
    private static String getSelectionMimeType(@LoadMimeType int mediaType,
                                               @Nullable String[] mimeTypeArgs) {
        return getSelectionMimeType(mediaType, mimeTypeArgs, "");
    }

    /**
     * 根据mediaType、mimeType、bucketId构建selection字符串
     */
    private static String getSelectionMimeType(@LoadMimeType int mediaType,
                                               @Nullable String[] mimeTypeArgs,
                                               @Nullable String bucketId) {
        StringBuilder builder = new StringBuilder(MEDIA_SIZE);
        builder.append(" and ");

        // append media type
        if (mediaType == LOAD_ALL) {
            builder.append("(");
            builder.append(MediaStore.Files.FileColumns.MEDIA_TYPE + "=?");
            builder.append(" or ");
            builder.append(MediaStore.Files.FileColumns.MEDIA_TYPE + "=?");
            builder.append(")");
        } else {
            builder.append(MediaStore.Files.FileColumns.MEDIA_TYPE + "=");
            builder.append(mediaType == LOAD_IMAGE ? MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE :
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        }

        // setting custom mime type
        if (mimeTypeArgs != null && mimeTypeArgs.length > 0) {
            builder.append(" and ");
            builder.append("(");
            for (int i = 0; i < mimeTypeArgs.length; i++) {
                if (i != 0) {
                    builder.append(" or ");
                }
                builder.append(MediaStore.MediaColumns.MIME_TYPE + "=?");
            }
            builder.append(")");
        }

        // append bucket id
        if (!TextUtils.isEmpty(bucketId) && !AlbumData.ALBUM_ID_ALL.equals(bucketId)) {
            builder.append(" and ");
            builder.append("bucket_id=?");
        }

        return builder.toString();
    }

}
