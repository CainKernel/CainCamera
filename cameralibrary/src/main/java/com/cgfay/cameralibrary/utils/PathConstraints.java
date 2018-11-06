package com.cgfay.cameralibrary.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * 路径常量
 * Created by cain.huang on 2017/8/8.
 */
public class PathConstraints {

    private PathConstraints() {}

    /**
     * 获取图片缓存绝对路径
     * @param context
     * @return
     */
    public static String getImageCachePath(Context context) {
        String directoryPath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            directoryPath = context.getCacheDir().getAbsolutePath();
        }
        String path = directoryPath + File.separator + "CainCamera_" + System.currentTimeMillis() + ".jpeg";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }

    /**
     * 获取视频缓存绝对路径
     * @param context
     * @return
     */
    public static String getVideoCachePath(Context context) {
        String directoryPath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            directoryPath = context.getCacheDir().getAbsolutePath();
        }
        String path = directoryPath + File.separator + "CainCamera_" + System.currentTimeMillis() + ".mp4";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }
}
