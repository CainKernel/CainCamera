package com.cgfay.scan.utils;

import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

final class ExifInterfaceUtils {

    private static final String TAG = ExifInterfaceUtils.class.getSimpleName();

    private ExifInterfaceUtils() {
    }

    /**
     * 创建一个新的ExifInterface对象
     * @param filePath      文件名
     * @return              ExifInterface对象
     * @throws IOException  IO异常
     */
    public static ExifInterface newInstance(String filePath) throws IOException {
        if (TextUtils.isEmpty(filePath)) {
            throw new NullPointerException("filePath should not be empty!");
        }
        return new ExifInterface(filePath);
    }

    /**
     * 获取exif的时间
     * @param filePath  文件路径
     * @return          日期对象
     */
    private static Date getExifDateTime(String filePath) {
        ExifInterface exif;
        try {

            exif = newInstance(filePath);
        } catch (IOException ex) {
            Log.e(TAG, "getExifDateTime: cannot read exif:", ex);
            return null;
        }
        String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
        if (TextUtils.isEmpty(date)) {
            return null;
        }


        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "getExifDateTime: failed to parse date token:", e);
        }

        return null;
    }

    /**
     * 获取毫秒时间
     * @param filePath  文件名
     * @return          日期的毫秒值
     */
    public static long getExifDateTimeInMillis(String filePath) {
        Date date = getExifDateTime(filePath);
        if (date == null) {
            return -1;
        }
        return date.getTime();
    }

    /**
     * 获取Exif角度
     * @param filePath  文件名
     * @return          角度，失败返回-1
     */
    public static int getExifOrientation(String filePath) {
        ExifInterface exif;
        try {
            exif = newInstance(filePath);
        } catch (IOException ex) {
            Log.e(TAG, "getExifOrientation: cannot read exif:", ex);
            return -1;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }
}
