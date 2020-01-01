package com.cgfay.camera.camera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Image数据转换工具
 */
final class ImageConvert {

    private static final String TAG = "ImageConvert";
    private static final boolean VERBOSE = false;

    public static final int COLOR_FORMAT_I420 = 1;
    public static final int COLOR_FORMAT_NV21 = 2;
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {COLOR_FORMAT_I420, COLOR_FORMAT_NV21})
    @Retention(RetentionPolicy.SOURCE)
    @interface ColorFormat {}

    private ImageConvert() {

    }

    /**
     * 获取转换后的格式
     * @param image         Image数据对象
     * @param colorFormat   转换的颜色格式
     * @return              图像字节数组
     */
    public static byte[] getDataFromImage(@NonNull Image image, @ColorFormat int colorFormat) {
        if (colorFormat != COLOR_FORMAT_I420 && colorFormat != COLOR_FORMAT_NV21) {
            throw new IllegalArgumentException("only support COLOR_FORMAT_I420 " + "and COLOR_FORMAT_NV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        if (VERBOSE) {
            Log.v(TAG, "get data from " + planes.length + " planes");
        }
        int yLength = 0;
        int stride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0: {
                    yLength = 0;
                    stride = 1;
                    break;
                }
                case 1: {
                    if (colorFormat == COLOR_FORMAT_I420) {
                        yLength = width * height;
                        stride = 1;
                    } else {
                        yLength = width * height + 1;
                        stride = 2;
                    }
                    break;
                }
                case 2: {
                    if (colorFormat == COLOR_FORMAT_I420) {
                        yLength = (int) (width * height * 1.25);
                        stride = 1;
                    } else {
                        yLength = width * height;
                        stride = 2;
                    }
                    break;
                }
            }

            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            if (VERBOSE) {
                Log.v(TAG, "pixelStride " + pixelStride);
                Log.v(TAG, "rowStride " + rowStride);
                Log.v(TAG, "width " + width);
                Log.v(TAG, "height " + height);
                Log.v(TAG, "buffer size " + buffer.remaining());
            }

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && stride == 1) {
                    length = w;
                    buffer.get(data, yLength, length);
                    yLength += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[yLength] = rowData[col * pixelStride];
                        yLength += stride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            if (VERBOSE) {
                Log.v(TAG, "Finished reading data from plane " + i);
            }
        }

        return data;
    }

    /**
     * 判断Image对象中的格式是否支持，目前只支持YUV_420_888、NV21、YV12
     * @param image Image对象
     * @return  返回格式支持的结果
     */
    private static boolean isImageFormatSupported(@NonNull Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }
}
