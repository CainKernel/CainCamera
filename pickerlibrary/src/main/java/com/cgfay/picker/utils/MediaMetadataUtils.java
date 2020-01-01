package com.cgfay.picker.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class MediaMetadataUtils {
    private static final String TAG = MediaMetadataUtils.class.getSimpleName();
    private static final int MAX_WIDTH = 2160;
    private static final String SCHEME_CONTENT = "content";

    private MediaMetadataUtils() {

    }

    /**
     * 获取像素数量
     * @param resolver
     * @param uri
     * @return
     */
    public static int getPixelsCount(ContentResolver resolver, Uri uri) {
        Point size = getBitmapBound(resolver, uri);
        return size.x * size.y;
    }

    /**
     * 图片Path转Uri
     * @param context
     * @param file
     * @return
     */
    public static Uri getImageContentUri(@NonNull Context context, @NonNull File file) {
        String filePath = file.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取图片大小
     * @param activity
     * @param path
     * @return
     */
    public static Point getBitmapSize(Activity activity, @NonNull String path) {
        if (activity == null) {
            return new Point(MAX_WIDTH, MAX_WIDTH);
        }
        Uri uri = getImageContentUri(activity, new File(path));
        if (uri == null) {
            return new Point(MAX_WIDTH, MAX_WIDTH);
        }
        return getBitmapSize(activity, uri);
    }

    /**
     * 获取图片大小
     * @param uri
     * @param activity
     * @return
     */
    public static Point getBitmapSize(@NonNull Activity activity, @NonNull Uri uri) {
        ContentResolver resolver = activity.getContentResolver();
        Point imageSize = getBitmapBound(resolver, uri);
        int width = imageSize.x;
        int height = imageSize.y;
        if (shouldRotate(resolver, uri)) {
            width = imageSize.y;
            height = imageSize.x;
        }
        if (width == 0 || height == 0) {
            return new Point(MAX_WIDTH, MAX_WIDTH);
        }
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float screenWidth = (float) metrics.widthPixels;
        float screenHeight = (float) metrics.heightPixels;
        float widthScale = screenWidth / width;
        float heightScale = screenHeight / height;
        if (widthScale > heightScale) {
            return new Point((int)(width * widthScale), (int) (height * heightScale));
        }
        return new Point((int) (width * widthScale), (int) (height * heightScale));
    }

    private static Point getBitmapBound(ContentResolver resolver, Uri uri) {
        InputStream is = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            is = resolver.openInputStream(uri);
            BitmapFactory.decodeStream(is, null, options);
            int width = options.outWidth;
            int height = options.outHeight;
            return new Point(width, height);
        } catch (FileNotFoundException ex) {
            return new Point(0, 0);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取uri路径
     * @param resolver
     * @param uri
     * @return
     */
    public static String getPath(ContentResolver resolver, Uri uri) {
        if (uri == null) {
            return null;
        }
        if (SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor cursor = null;
            try {

                cursor = resolver.query(uri, new String[] {MediaStore.Images.ImageColumns.DATA},
                        null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    return null;
                }
                return cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return uri.getPath();
    }

    /**
     * 是否需要旋转
     * @param resolver
     * @param uri
     * @return
     */
    private static boolean shouldRotate(ContentResolver resolver, Uri uri) {
        ExifInterface exif;
        try {
            exif = ExifInterfaceUtils.newInstance(getPath(resolver, uri));
        } catch (IOException e) {
            Log.e(TAG, "shouldRotate: could not read exif info of image:" + uri);
            return false;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        return orientation == ExifInterface.ORIENTATION_ROTATE_90
                || orientation == ExifInterface.ORIENTATION_ROTATE_270;
    }

    /**
     * 获取MB大小
     * @param byteSize 字节大小
     * @return
     */
    public static float getMBSize(long byteSize) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        df.applyPattern("0.0");
        String result = df.format((float) byteSize / 1024 / 1024);
        result = result.replaceAll(",", ".");
        return Float.valueOf(result);
    }
}
