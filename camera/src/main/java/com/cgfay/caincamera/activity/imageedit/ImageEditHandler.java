package com.cgfay.caincamera.activity.imageedit;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cgfay.cainfilter.type.GLFilterGroupType;
import com.cgfay.cainfilter.type.GLFilterType;

import java.lang.ref.WeakReference;

/**
 * 图片编辑Handler回调
 * Created by Administrator on 2018/3/13.
 */

public class ImageEditHandler extends Handler {

    static final int MSG_INIT_SURFACE = 0x001;
    static final int MSG_DESTROY_SURFACE = 0x002;

    static final int MSG_CREATE_TEXTURE = 0x101;
    static final int MSG_DRAW_IMAGE = 0x102;

    static final int MSG_SET_BRIGHTNESS = 0x200;
    static final int MSG_SET_CONTRAST = 0x201;
    static final int MSG_SET_EXPOSURE = 0x202;
    static final int MSG_SET_HUE = 0x203;
    static final int MSG_SET_SATURATION = 0x204;
    static final int MSG_SET_SHARPNESS = 0x205;

    static final int MSG_CHANGE_FILTER = 0x301;
    static final int MSG_CHANGE_FILTER_GROUP = 0x302;


    private WeakReference<ImageEditThread> mWeakThread;

    public ImageEditHandler(Looper looper, ImageEditThread thread) {
        super(looper);
        mWeakThread = new WeakReference<ImageEditThread>(thread);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeakThread == null || mWeakThread.get() == null) {
            return;
        }

        ImageEditThread thread = mWeakThread.get();
        switch (msg.what) {
            // 初始化EGLSurface
            case MSG_INIT_SURFACE:
                if (msg.arg1 > 0 && msg.arg2 > 0) {
                    thread.initSurfaceAndFilter(msg.arg1, msg.arg2);
                } else {
                    throw new IllegalArgumentException("failed to get width or height!");
                }
                break;

            // 销毁EGLSurface
            case MSG_DESTROY_SURFACE:
                thread.destroySurfaceAndFilter();
                break;

            // 创建Texture
            case MSG_CREATE_TEXTURE:
                thread.createBitmapTexture((Bitmap) msg.obj);
                break;

            // 渲染图片
            case MSG_DRAW_IMAGE:
                thread.drawImage();
                break;

            // 设置亮度
            case MSG_SET_BRIGHTNESS:
                thread.setBrightness((Float) msg.obj);
                break;

            // 设置对比度
            case MSG_SET_CONTRAST:
                thread.setContrast((Float) msg.obj);
                break;

            // 设置曝光
            case MSG_SET_EXPOSURE:
                thread.setExposure((Float) msg.obj);
                break;

            // 设置色调
            case MSG_SET_HUE:
                thread.setHue((Float) msg.obj);
                break;

            // 设置饱和度
            case MSG_SET_SATURATION:
                thread.setSaturation((Float) msg.obj);
                break;

            // 设置锐度
            case MSG_SET_SHARPNESS:
                thread.setSharpness((Float) msg.obj);
                break;

            // 切换滤镜
            case MSG_CHANGE_FILTER:
                thread.changeFilter((GLFilterType) msg.obj);
                break;

            // 切换滤镜组
            case MSG_CHANGE_FILTER_GROUP:
                thread.changeFilterGroup((GLFilterGroupType) msg.obj);
                break;
            default:
                throw new IllegalStateException("Can not handle message what is: " + msg.what);
        }
    }
}
