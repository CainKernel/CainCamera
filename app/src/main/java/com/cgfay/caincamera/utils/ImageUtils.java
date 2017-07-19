package com.cgfay.caincamera.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by cain on 2017/7/9.
 */

public class ImageUtils {

    /**
     * 旋转图片
     * @param bitmap
     * @param rotation
     * @return
     */
    public static Bitmap getRotatedBitmap(Bitmap bitmap, int rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
        return result;
    }
}
