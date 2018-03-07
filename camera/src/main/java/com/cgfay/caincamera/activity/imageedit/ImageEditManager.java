package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * 图片编辑管理器
 * Created by cain on 2017/11/15.
 */

public final class ImageEditManager {

    private static final String TAG = "ImageEditManager";

    private final WeakReference<Context> mWeakContext;
    private final WeakReference<ImageView> mWeakImageView;

    // 原图像
    private Bitmap mSourceBitmap;
    // 当前图像
    private Bitmap mCurrentBitmap;
    // 图像路径
    private String mImagePath;

    private Handler mEditHandler;
    private HandlerThread mEditHandlerThread;

    private Handler mMainHandler;

    public ImageEditManager(Context context, String imagePath, ImageView imageView) {
        mWeakContext = new WeakReference<Context>(context);
        mWeakImageView = new WeakReference<ImageView>(imageView);
        mImagePath = imagePath;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 开始图片编辑线程
     */
    public void startImageEditThread() {
        mEditHandlerThread = new HandlerThread("Image Edit Thread");
        mEditHandlerThread.start();
        mEditHandler = new Handler(mEditHandlerThread.getLooper());
    }

    /**
     * 停止图片编辑线程
     */
    public void stopImageEditThread() {
        if (mEditHandler != null) {
            mEditHandler.removeCallbacksAndMessages(null);
        }
        if (mEditHandlerThread != null) {
            mEditHandlerThread.quitSafely();
        }
        try {
            mEditHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mEditHandlerThread = null;
        mEditHandler = null;
    }


    /**
     * 释放持有的资源
     */
    public void release() {
        mWeakContext.clear();
        mWeakImageView.clear();
        if (mSourceBitmap != null && !mSourceBitmap.isRecycled()) {
            mSourceBitmap.recycle();
            mSourceBitmap = null;
        }
        if (mCurrentBitmap != null && !mCurrentBitmap.isRecycled()) {
            mCurrentBitmap.recycle();
            mCurrentBitmap = null;
        }
    }


    /**
     * 显示图片
     */
    public void showSourceImage() {

        if (mEditHandler != null) {
            mEditHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSourceBitmap = BitmapFactory.decodeFile(mImagePath);
                    setImageBitmap(mSourceBitmap);
                }
            });
        }
    }

    /**
     * 设置图片
     * @param bitmap
     */
    private void setImageBitmap(final Bitmap bitmap) {
        if (mMainHandler != null && mWeakImageView.get() != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWeakImageView.get().setImageBitmap(bitmap);
                }
            });
        }
    }

    /**
     * 设置亮度
     * @param brightness
     */
    public void setBrightness(float brightness) {
        Log.d(TAG, "brightness = " + brightness);
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        Log.d(TAG, "contrast = " + contrast);
    }

    /**
     * 设置曝光
     * @param exposure
     */
    public void setExposure(float exposure) {
        Log.d(TAG, "exposure = " + exposure);
    }

    /**
     * 设置色调 0 ~ 360度
     * @param hue
     */
    public void setHue(float hue) {
        Log.d(TAG, "hue = " + hue);
    }

    /**
     * 设置饱和度 0.0 ~ 2.0之间
     * @param saturation
     */
    public void setSaturation(float saturation) {
        Log.d(TAG, "saturation = " + saturation);
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        Log.d(TAG, "sharpness = " + sharpness);
    }

}
