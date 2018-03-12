package com.cgfay.utilslibrary;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cain on 2017/7/9.
 */

public class BitmapUtils {

    public static final String[] EXIF_TAGS = {
            "FNumber",
            ExifInterface.TAG_DATETIME,
            "ExposureTime",
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            "GPSAltitude", "GPSAltitudeRef",
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_IMAGE_LENGTH,
            ExifInterface.TAG_IMAGE_WIDTH, "ISOSpeedRatings",
            ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL,
            ExifInterface.TAG_WHITE_BALANCE,
    };

    /**
     * 旋转图片
     * @param bitmap
     * @param rotation
     * @return
     */
    public static Bitmap getRotatedBitmap(Bitmap bitmap, int rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
    }

    /**
     * 镜像翻转图片
     * @param bitmap
     * @return
     */
    public static Bitmap flipBitmap(Bitmap bitmap) {
        return flipBitmap(bitmap, true, false);
    }

    /**
     * 翻转图片
     * @param bitmap
     * @param flipX
     * @param flipY
     * @return
     */
    public static Bitmap flipBitmap(Bitmap bitmap, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();
        matrix.setScale(flipX ? -1 : 1, flipY ? -1 : 1);
        matrix.postTranslate(bitmap.getWidth(), 0);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
    }


    /**
     * 加载Assets文件夹下的图片
     * @param context
     * @param fileName
     * @return
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream is = manager.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 加载Assets文件夹下的图片
     * @param context
     * @param fileName
     * @return
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName, Bitmap inBitmap) {
        Bitmap bitmap = null;
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream is = manager.open(fileName);
            if (inBitmap != null && !inBitmap.isRecycled()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                // 使用inBitmap时，inSampleSize得设置为1
                options.inSampleSize = 1;
                // 这个属性一定要在inBitmap之前使用，否则会弹出一下异常
                // BitmapFactory: Unable to reuse an immutable bitmap as an image decoder target.
                options.inMutable = true;
                options.inBitmap = inBitmap;
                bitmap = BitmapFactory.decodeStream(is, null, options);
            } else {
                bitmap = BitmapFactory.decodeStream(is);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // 计算原始图像的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // 当原始图像的高和宽大于所需高度和宽度时
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // 算出长宽比后去比例小的作为inSamplesize，保证最后imageview的dimension比request的大
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // 计算总像素是否大于请求的宽高积的2倍
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }


    /**
     * 从文件读取Bitmap
     * @param dst
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getBitmapFromFile(File dst, int width, int height) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts = null;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getPath(), opts);
                // 计算图片缩放比例
                opts.inSampleSize = calculateInSampleSize(opts, width, height);
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;
            }
            try {
                return BitmapFactory.decodeFile(dst.getPath(), opts);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 保存图片
     * @param context
     * @param path
     * @param bitmap
     */
    public static void saveBitmap(Context context, String path, Bitmap bitmap) {
        saveBitmap(context, path, bitmap, true);
    }

    /**
     * 保存图片
     * @param context
     * @param path
     * @param bitmap
     * @param addToMediaStore
     */
    public static void saveBitmap(Context context, String path, Bitmap bitmap,
                                  boolean addToMediaStore) {
        final File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean compress = true;
        if (path.endsWith(".png")) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        } else if (path.endsWith(".jpeg") || path.endsWith(".jpg")) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        } else { // 除了png和jpeg之外的图片格式暂时不支持
            compress = false;
        }
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 添加到媒体库
        if (addToMediaStore && compress) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, path);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    /**
     * 获取图片旋转角度
     * @param path
     * @return
     */
    public static int getOrientation(final String path) {
        int rotation = 0;
        try {
            File file = new File(path);
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;

                default:
                    rotation = 0;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotation;
    }

    /**
     * 获取Uri路径图片的旋转角度
     * @param context
     * @param uri
     * @return
     */
    public static int getOrientation(Context context, Uri uri) {
        final String scheme = uri.getScheme();
        ContentProviderClient provider = null;
        if (scheme == null || ContentResolver.SCHEME_FILE.equals(scheme)) {
            return getOrientation(uri.getPath());
        } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            try {
                provider = context.getContentResolver().acquireContentProviderClient(uri);
            } catch (SecurityException e) {
                return 0;
            }
            if (provider != null) {
                Cursor cursor;
                try {
                    cursor = provider.query(uri, new String[] {
                            MediaStore.Images.ImageColumns.ORIENTATION,
                            MediaStore.Images.ImageColumns.DATA},
                            null, null, null);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return 0;
                }
                if (cursor == null) {
                    return 0;
                }

                int orientationIndex = cursor
                        .getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
                int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

                try {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();

                        int rotation = 0;

                        if (orientationIndex > -1) {
                            rotation = cursor.getInt(orientationIndex);
                        }

                        if (dataIndex > -1) {
                            String path = cursor.getString(dataIndex);
                            rotation |= getOrientation(path);
                        }
                        return rotation;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return 0;
    }

    /**
     * 获取图片大小
     * @param path
     * @return
     */
    public static Size getBitmapSize(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return new Size(options.outWidth, options.outHeight);
    }

    /**
     * 将Bitmap图片旋转90度
     * @param data
     * @return
     */
    public static Bitmap rotateBitmap(byte[] data) {
        return rotateBitmap(data, 90);
    }

    /**
     * 将Bitmap图片旋转一定角度
     * @param data
     * @param rotate
     * @return
     */
    public static Bitmap rotateBitmap(byte[] data, int rotate) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postRotate(rotate);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        System.gc();
        return rotatedBitmap;
    }

    /**
     * 将Bitmap图片旋转90度
     * @param bitmap
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap) {
        return rotateBitmap(bitmap, 90);
    }

    /**
     * 将Bitmap图片旋转一定角度
     * @param bitmap
     * @param rotate
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postRotate(rotate);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    /**
     * 获取Exif参数
     * @param path
     * @param bundle
     * @return
     */
    public static boolean loadExifAttributes(String path, Bundle bundle) {
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        for (String tag : EXIF_TAGS) {
            bundle.putString(tag, exifInterface.getAttribute(tag));
        }
        return true;
    }

    /**
     * 保存Exif属性
     * @param path
     * @param bundle
     * @return
     */
    public static boolean saveExifAttributes(String path, Bundle bundle) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        for (String tag : EXIF_TAGS) {
            if (bundle.containsKey(tag)) {
                exif.setAttribute(tag, bundle.getString(tag));
            }
        }
        try {
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
