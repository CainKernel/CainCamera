package com.cgfay.cainfilter.ImageFilter;

import android.graphics.Bitmap;

import com.cgfay.cainfilter.type.ImageFilterType;

/**
 * 静态滤镜
 * Created by Administrator on 2018/3/9.
 */
public class ImageFilter {
    private static ImageFilter sInstance;
    public static ImageFilter getInstance() {
        if (sInstance == null) {
            sInstance = new ImageFilter();
        }
        return sInstance;
    }

    private ImageFilter() {}


    /**
     * 设置亮度 -255 ~ 255
     * @param brightness
     * @param srcBitmap
     * @param destBitmap
     */
    public void setBrightness(float brightness, Bitmap srcBitmap, Bitmap destBitmap) {
        NativeFilter.setBrightness(brightness, srcBitmap, destBitmap);
    }

    /**
     * 设置对比度
     * @param contrast
     * @param srcBitmap
     * @param destBitmap
     */
    public void setContrast(float contrast, Bitmap srcBitmap, Bitmap destBitmap) {
        NativeFilter.setContrast(contrast, srcBitmap, destBitmap);
    }


    /**
     * 设置曝光
     * @param exposure
     * @param srcBitmap
     * @param destBitmap
     */
    public void setExposure(float exposure, Bitmap srcBitmap, Bitmap destBitmap) {
        NativeFilter.setExposure(exposure, srcBitmap, destBitmap);
    }

    /**
     * 设置色调
     * @param hue
     * @param srcBitmap
     * @param destBitmap
     */
    public void setHue(float hue, Bitmap srcBitmap, Bitmap destBitmap) {
        NativeFilter.setHue(hue, srcBitmap, destBitmap);
    }

    /**
     * 设置饱和度
     * @param saturation
     * @param srcBitmap
     * @param destBitmap
     */
    public void setSaturation(float saturation, Bitmap srcBitmap, Bitmap destBitmap) {
        NativeFilter.setSaturation(saturation, srcBitmap, destBitmap);
    }

    /**
     * 设置锐度
     * @param sharpness
     * @param srcBitmap
     * @param destBitmap
     */
    public void setSharpness(float sharpness, Bitmap srcBitmap, Bitmap destBitmap) {
        NativeFilter.setSharpness(sharpness, srcBitmap, destBitmap);
    }

    /**
     * 切换滤镜
     * @param type
     */
    public void changeFilter(ImageFilterType type, Bitmap srcBitmap, Bitmap destBitmap) {
        changeFilter(type, -1, srcBitmap, destBitmap);
    }

    public void changeFilter(ImageFilterType type, float value, Bitmap srcBitmap, Bitmap destBitmap) {
        switch (type) {
            case Gray:
                NativeFilter.grayFilter(srcBitmap, destBitmap);
                break;

            case Mosaic:
                NativeFilter.mosaicFilter(value, srcBitmap, destBitmap);
                break;

            case NONE:
            default:
                break;
        }
    }

}
