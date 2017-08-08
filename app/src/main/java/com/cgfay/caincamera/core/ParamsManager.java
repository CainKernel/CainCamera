package com.cgfay.caincamera.core;

import android.content.Context;
import android.os.Environment;

/**
 * 管理全局参数和上下文
 * Created by cain.huang on 2017/8/8.
 */
public class ParamsManager {

    private ParamsManager() {}

    // 上下文，方便滤镜使用
    public static Context context;

    // 存储根目录
    private static final String StoragePath = Environment.getExternalStorageDirectory().getPath();

    // 图片存放地址
    public static final String ImagePath = StoragePath + "/CainCamera/Image/";

    // 视频存放地址
    public static final String VideoPath = StoragePath + "/CainCamera/Video/";

}
