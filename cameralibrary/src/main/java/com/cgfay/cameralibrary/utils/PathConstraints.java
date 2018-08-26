package com.cgfay.cameralibrary.utils;

import android.os.Environment;

import java.io.File;

/**
 * 路径常量
 * Created by cain.huang on 2017/8/8.
 */
public class PathConstraints {

    private PathConstraints() {}

    // 存储根目录
    private static final String StoragePath = Environment.getExternalStorageDirectory().getPath();

    // 默认相册位置
    public static final String AlbumPath = StoragePath + "/DCIM/Camera/";

    // 图片存放地址
    private static final String MediaPath = StoragePath + "/CainCamera/";

    // 是否允许录音(用户自行设置，默认开启)
    public static boolean canRecordingAudio = true;

    /**
     * 获取图片输出路径
     * @return
     */
    public static String getMediaPath() {
        String path = PathConstraints.MediaPath + "CainCamera_" + System.currentTimeMillis() + ".jpeg";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }

    /**
     * 获取视频输出路径
     * @return
     */
    public static String getVideoPath() {
        String path = PathConstraints.MediaPath + "CainCamera_" + System.currentTimeMillis() + ".mp4";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }
}
