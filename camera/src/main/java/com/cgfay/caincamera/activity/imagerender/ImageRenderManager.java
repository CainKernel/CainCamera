package com.cgfay.caincamera.activity.imagerender;

/**
 * Created by Administrator on 2018/3/8.
 */

import android.view.SurfaceHolder;

import com.cgfay.cainfilter.type.GLFilterType;

/**
 * 图片渲染管理器
 * Created by cain on 2017/11/15.
 */

public final class ImageRenderManager {

    private static final String TAG = "ImageRenderManager";

    private static ImageRenderManager mInstance;

    private ImageRenderThread mThread;
    private ImageRenderHandler mHandler;

    public static ImageRenderManager getInstance() {
        if (mInstance == null) {
            mInstance = new ImageRenderManager();
        }
        return mInstance;
    }

    private ImageRenderManager() {}


    public void surfaceCreated(SurfaceHolder holder) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SURFACE_CREATED, holder));
        }
    }

    public void surfaceChanged(int width, int height) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SURFACE_CHANGED, width, height));
        }
    }

    /**
     * 销毁时需要同步进行
     */
    public void surfaceDestoryed() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SURFACE_DESTORYED));
        }
    }

    /**
     * 开启图片编辑线程
     */
    public void startImageEditThread() {
        mThread = new ImageRenderThread("Photo Edit Thread!");
        mThread.start();
        mHandler = new ImageRenderHandler(mThread.getLooper(), mThread);
        mThread.setImageEditHandler(mHandler);
    }

    /**
     * 销毁图片编辑线程
     */
    public void destoryImageEditThread() {
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
     * 切换滤镜
     * @param type
     */
    public void changeFilterType(GLFilterType type) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_CHANGE_FILTER, type));
        }
    }

    /**
     * 设置图片元数据
     * @param path
     */
    public void setImagePath(String path) {

        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_IMAGE_PATH, path));
        }

    }

    /**
     * 设置屏幕大小
     * @param width
     * @param height
     */
    public void setScreenSize(int width, int height) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SCREEN_SIZE, width, height));
        }
    }

    /**
     * 保存图片
     * @param listener
     */
    public void saveImage(OnRenderListener listener) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SAVE_IMAGE, listener));
        }
    }

    /**
     * 设置亮度
     * @param brightness
     */
    public void setBrightness(float brightness) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SET_BRIGHTNESS, brightness));
        }
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SET_CONTRAST, contrast));
        }
    }

    /**
     * 设置曝光
     * @param exposure
     */
    public void setExposure(float exposure) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SET_EXPOSURE, exposure));
        }
    }

    /**
     * 设置色调 0 ~ 360度
     * @param hue
     */
    public void setHue(float hue) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SET_HUE, hue));
        }
    }

    /**
     * 设置饱和度 0.0 ~ 2.0之间
     * @param saturation
     */
    public void setSaturation(float saturation) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SET_SATURATION, saturation));
        }
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_SET_SHARPNESS, sharpness));
        }
    }

}