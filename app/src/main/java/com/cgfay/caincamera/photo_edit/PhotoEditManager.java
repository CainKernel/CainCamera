package com.cgfay.caincamera.photo_edit;

import android.util.Log;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.bean.ImageMeta;

/**
 * 图片编辑管理器
 * Created by cain on 2017/11/15.
 */

public final class PhotoEditManager {

    private static final String TAG = "PhotoEditManager";

    private static PhotoEditManager mInstance;

    private PhotoEditThread mThread;
    private PhotoEditHandler mHandler;

    public static PhotoEditManager getInstance() {
        if (mInstance == null) {
            mInstance = new PhotoEditManager();
        }
        return mInstance;
    }

    private PhotoEditManager() {}


    public void surfaceCreated(SurfaceHolder holder) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SURFACE_CREATED, holder));
        }
    }

    public void surfaceChanged(int width, int height) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SURFACE_CHANGED, width, height));
        }
    }

    /**
     * 销毁时需要同步进行
     */
    public void surfaceDestoryed() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SURFACE_DESTORYED));
        }
    }

    /**
     * 等待操作完成
     */
    private void waitUntilReady() {
        try {
            wait();
        } catch (InterruptedException e) {
            Log.w(TAG, "wait was interrupted");
        }
    }

    /**
     * 开启图片编辑线程
     */
    public void startPhotoEditThread() {
        mThread = new PhotoEditThread("Photo Edit Thread!");
        mThread.start();
        mHandler = new PhotoEditHandler(mThread.getLooper(), mThread);
        mThread.setPhotoEditHandler(mHandler);
    }

    /**
     * 销毁图片编辑线程
     */
    public void destoryPhotoEditThread() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mThread != null) {
            mThread.quitSafely();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mThread = null;
        mHandler = null;
    }

    /**
     * 设置图片元数据
     * @param imageMeta
     */
    public void setImageMeta(ImageMeta imageMeta) {

        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SET_IMAGE_META, imageMeta));
        }

    }

    /**
     * 设置亮度
     * @param brightness
     */
    public void setBrightness(float brightness) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SET_BRIGHTNESS, brightness));
        }
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SET_CONTRAST, contrast));
        }
    }

    /**
     * 设置曝光
     * @param exposure
     */
    public void setExposure(float exposure) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SET_EXPOSURE, exposure));
        }
    }

    /**
     * 设置色调 0 ~ 360度
     * @param hue
     */
    public void setHue(float hue) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SET_HUE, hue));
        }
    }

    /**
     * 设置饱和度 0.0 ~ 2.0之间
     * @param saturation
     */
    public void setSaturation(float saturation) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SET_SATURATION, saturation));
        }
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_SET_SHARPNESS, sharpness));
        }
    }

}
