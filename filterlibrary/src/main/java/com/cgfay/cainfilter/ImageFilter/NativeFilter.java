package com.cgfay.cainfilter.ImageFilter;

import android.graphics.Bitmap;

/**
 * Native层滤镜组
 * Created by Administrator on 2018/3/6.
 */

public class NativeFilter {
    static {
        System.loadLibrary("imagefilter");
    }
    private NativeFilter() {}

    // ----------------------------------- 图像调节 ----------------------------------------
    // 设置亮度
    static native boolean setBrightness(float brightness, Bitmap srcBitmap, Bitmap destBitmap);
    // 设置对比度
    static native boolean setContrast(float contrast, Bitmap srcBitmap, Bitmap destBitmap);
    // 设置曝光
    static native boolean setExposure(float exposure, Bitmap srcBitmap, Bitmap destBitmap);
    // 设置色调
    static native boolean setHue(float hue, Bitmap srcBitmap, Bitmap destBitmap);
    // 设置饱和度
    static native boolean setSaturation(float saturation, Bitmap srcBitmap, Bitmap destBitmap);
    // 设置锐度
    static native boolean setSharpness(float sharpness, Bitmap srcBitmap, Bitmap destBitmap);

    // ------------------------------------ 滤镜 --------------------------------------------
    // 灰色滤镜
    static native boolean grayFilter(Bitmap srcBitmap, Bitmap destBitmap);
    // 马赛克滤镜
    static native boolean mosaicFilter(float mosaicSize, Bitmap srcBitmap, Bitmap destBitmap);
}
