package com.cgfay.filterlibrary.ndkfilter;

import android.graphics.Bitmap;

/**
 * 图片滤镜
 */
public final class ImageFilter {

    static {
        System.loadLibrary("nativefilter");
    }

    private static class ImageFilterHolder {
        public static ImageFilter instance = new ImageFilter();
    }

    private ImageFilter() {}

    public static ImageFilter getInstance() {
        return ImageFilterHolder.instance;
    }

    private native int nativeMosaic(Bitmap source, int radius);
    private native int nativeLookupTable(Bitmap bitmap, Bitmap lookupTable);
    private native int nativeInvertFilter(Bitmap bitmap);
    private native int nativeBlackWhiteFilter(Bitmap bitmap);
    private native int nativeBrightContrastFilter(Bitmap bitmap, float brightness, float contrast);
    private native int nativeColorQuantizeFilter(Bitmap bitmap, float levels);
    private native int nativeHistogramEqualFilter(Bitmap bitmap);
    private native int nativeShiftFilter(Bitmap bitmap, int amount);
    private native int nativeVignetteFilter(Bitmap bitmap, float size);
    private native int nativeGaussianBlurFilter(Bitmap bitmap);
    private native int nativeStackBlurFilter(Bitmap bitmap, int radius);

    /**
     * 马赛克滤镜
     * @param bitmap    图片
     * @param radius    马赛克半径
     * @return          返回处理结果，0为处理成功，否则处理失败
     */
    public int filterMosaic(Bitmap bitmap, int radius) {
        return nativeMosaic(bitmap, radius);
    }

    /**
     * 颜色查找表滤镜
     * @param bitmap        输入图片
     * @param lookupTable   颜色查找表(32位BGRA格式)
     * @return              返回处理结果，0为处理成功，否则处理失败
     */
    public int filterLookupTable512(Bitmap bitmap, Bitmap lookupTable) {
        return nativeLookupTable(bitmap, lookupTable);
    }

    /**
     * 反色滤镜
     * @param source

     * @return
     */
    public int filterInvert(Bitmap source) {
        return nativeInvertFilter(source);
    }

    /**
     * 黑白滤镜
     * @param source

     * @return
     */
    public int filterBlackWhite(Bitmap source) {
        return nativeBlackWhiteFilter(source);
    }

    /**
     * 亮度对比度滤镜
     * @param bitmap

     * @param brightness
     * @param contrast
     * @return
     */
    public int filterBrightContrast(Bitmap bitmap,  float brightness, float contrast) {
        return nativeBrightContrastFilter(bitmap, brightness, contrast);
    }

    /**
     * 色彩量化滤镜
     * @param bitmap
     * @param levels
     * @return
     */
    public int filterColorQuantize(Bitmap bitmap, float levels) {
        return nativeColorQuantizeFilter(bitmap, levels);
    }

    /**
     * 直方图滤镜
     * @param bitmap
     * @return
     */
    public int filterHistogramEqual(Bitmap bitmap) {
        return nativeHistogramEqualFilter(bitmap);
    }

    /**
     * 像素偏移滤镜
     * @param bitmap
     * @param amount
     * @return
     */
    public int filterShift(Bitmap bitmap, int amount) {
        return nativeShiftFilter(bitmap, amount);
    }

    /**
     * 暗角滤镜
     * @param bitmap
     * @param size
     * @return
     */
    public int filterVignette(Bitmap bitmap, float size) {
        return nativeVignetteFilter(bitmap, size);
    }

    /**
     * 高斯模糊处理
     * @param bitmap
     * @return
     */
    public int filterGaussianBlur(Bitmap bitmap) {
        return nativeGaussianBlurFilter(bitmap);
    }

    /**
     * 堆栈模糊
     * @param bitmap
     * @param radius    模糊半径
     * @return
     */
    public int filterStackBlur(Bitmap bitmap, int radius) {
        return nativeStackBlurFilter(bitmap, radius);
    }
}
