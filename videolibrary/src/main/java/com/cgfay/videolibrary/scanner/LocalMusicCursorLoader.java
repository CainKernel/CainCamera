package com.cgfay.videolibrary.scanner;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

/**
 * 本地音乐加载器
 */
public class LocalMusicCursorLoader extends CursorLoader {

    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,   // ID
            MediaStore.Audio.Media.TITLE,       // 歌曲名
            MediaStore.Audio.Media.DATA,        // 路径
            MediaStore.Audio.Media.DURATION,    // 时长
    };

    private static final String SELECTION_ALL=
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.Audio.Media.SIZE + ">0";

    private static final String[] SELECTION_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO),
    };

    private static final String ORDER_BY = "datetaken DESC";

    private LocalMusicCursorLoader(Context context, String selection, String[] selectionArgs) {
        super(context, QUERY_URI, PROJECTION, selection, selectionArgs, ORDER_BY);
    }

    public static CursorLoader newInstance(Context context) {
        String selection = SELECTION_ALL;
        String[] selectionArgs = SELECTION_ARGS;

        return new LocalMusicCursorLoader(context, selection, selectionArgs);
    }
}
