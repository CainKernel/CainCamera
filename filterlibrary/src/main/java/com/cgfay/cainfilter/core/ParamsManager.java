package com.cgfay.cainfilter.core;

import android.content.Context;
import android.os.Environment;

import com.cgfay.cainfilter.type.GalleryType;

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

    // 默认相册位置
    public static final String AlbumPath = StoragePath + "/DCIM/Camera/";

    // 图片存放地址
    public static final String ImagePath = StoragePath + "/CainCamera/Image/";

    // 视频存放地址
    public static final String VideoPath = StoragePath + "/CainCamera/Video/";

    // 是否绘制人脸关键点
    public static boolean enableDrawingPoints = false;

    // 人脸识别是否正常
    public static boolean canFaceTrack = false;

    // 存放预览类型，GIF表情包、PICTURE拍照、VIDEO视频等
    public static GalleryType mGalleryType = GalleryType.PICTURE;

    // 是否允许录音(用户自行设置，默认开启)
    public static boolean canRecordingAudio = true;

    // 是否允许位置
    public static boolean canRecordingLocation = false;

    // 是否倒置(Nexus 5X与其他手机不一样，后置摄像头图像会倒置)
    public static boolean mBackReverse = false;
}
