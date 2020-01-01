package com.cgfay.media;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * 视频编辑公共类
 */
public class VideoEditorUtil {

    public static final String TAG = "VideoEditorUtil";

    private VideoEditorUtil() {

    }

    /**
     * 创建文件路径
     * @param dir
     * @param suffix
     * @return
     */
    public static String createPath(String dir, String suffix) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int second = c.get(Calendar.SECOND);
        int millisecond = c.get(Calendar.MILLISECOND);
        year = year - 2000;
        String name = dir;
        File d = new File(name);

        // 如果目录不中存在，创建这个目录
        if (!d.exists())
            d.mkdir();
        name += "/";


        name += String.valueOf(year);
        name += String.valueOf(month);
        name += String.valueOf(day);
        name += String.valueOf(hour);
        name += String.valueOf(minute);
        name += String.valueOf(second);
        name += String.valueOf(millisecond);
        if (!suffix.startsWith(".")) {
            name += ".";
        }
        name += suffix;
        return name;
    }

    /**
     * 创建文件路径
     * @param suffix
     * @return
     */
    public static String createPathInBox(@NonNull Context context, String suffix) {
        String dir;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && context.getExternalCacheDir() != null) {
            dir = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            dir = context.getCacheDir().getAbsolutePath();
        }
        return createPath(dir, suffix);
    }

    /**
     * 在指定目录创建指定后缀文件
     * @param dir
     * @param suffix
     * @return
     */
    public static String createFile(String dir, String suffix) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int second = c.get(Calendar.SECOND);
        int millisecond = c.get(Calendar.MILLISECOND);
        year = year - 2000;
        String name = dir;
        File d = new File(name);

        // 如果目录不中存在，创建这个目录
        if (!d.exists()) {
            d.mkdir();
        }
        name += "/";

        name += String.valueOf(year);
        name += String.valueOf(month);
        name += String.valueOf(day);
        name += String.valueOf(hour);
        name += String.valueOf(minute);
        name += String.valueOf(second);
        name += String.valueOf(millisecond);
        if (!suffix.startsWith(".")) {
            name += ".";
        }
        name += suffix;

        try {
            Thread.sleep(1);  //保持文件名的唯一性.
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        File file = new File(name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return name;
    }

    /**
     * 创建指定后缀的文件
     * @param suffix
     * @return
     */
    public static String createFileInBox(@NonNull Context context, String suffix) {
        String dir;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && context.getExternalCacheDir() != null) {
            dir = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            dir = context.getCacheDir().getAbsolutePath();
        }
        return createFile(dir, suffix);
    }

}
