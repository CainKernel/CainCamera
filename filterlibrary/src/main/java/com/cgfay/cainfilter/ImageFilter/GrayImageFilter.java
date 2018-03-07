package com.cgfay.cainfilter.ImageFilter;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2018/3/6.
 */

public class GrayImageFilter implements IImageFilter {

    public GrayImageFilter() {

    }

    @Override
    public Bitmap processImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        pixels = NativeFilter.grayFilter(pixels, width, height);
        Bitmap result = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}
