package com.cgfay.caincamera.activity.imagerender;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import com.cgfay.cainfilter.type.GLFilterType;

import java.lang.ref.WeakReference;

/**
 * 图片渲染Handler
 * Created by Administrator on 2018/3/8.
 */

public class ImageRenderHandler extends Handler {
    static final int MSG_SURFACE_CREATED = 0x001;
    static final int MSG_SURFACE_CHANGED = 0x002;
    static final int MSG_SURFACE_DESTORYED = 0x003;

    static final int MSG_DRAW_IMAGE = 0x100;
    static final int MSG_IMAGE_PATH = 0x101;
    static final int MSG_SCREEN_SIZE = 0x102;
    static final int MSG_SAVE_IMAGE = 0x103;

    static final int MSG_SET_BRIGHTNESS = 0x200;
    static final int MSG_SET_CONTRAST = 0x201;
    static final int MSG_SET_EXPOSURE = 0x202;
    static final int MSG_SET_HUE = 0x203;
    static final int MSG_SET_SATURATION = 0x204;
    static final int MSG_SET_SHARPNESS = 0x205;

    static final int MSG_CHANGE_FILTER = 0x301;

    private WeakReference<ImageRenderThread> mWeakThread;

    public ImageRenderHandler(Looper looper, ImageRenderThread thread) {
        super(looper);
        mWeakThread = new WeakReference<ImageRenderThread>(thread);
    }


    @Override
    public void handleMessage(Message msg) {
        if (mWeakThread == null || mWeakThread.get() == null) {
            return;
        }
        switch (msg.what) {
            case MSG_SURFACE_CREATED:
                mWeakThread.get().surfaceCreated((SurfaceHolder) msg.obj);
                break;

            case MSG_SURFACE_CHANGED:
                mWeakThread.get().surfaceChanged(msg.arg1, msg.arg2);
                break;

            case MSG_SURFACE_DESTORYED:
                mWeakThread.get().surfaceDestoryed();
                break;

            // 绘制图片
            case MSG_DRAW_IMAGE:
                mWeakThread.get().drawImage();
                break;

            // 设置图片数据
            case MSG_IMAGE_PATH:
                mWeakThread.get().setImagePath((String) msg.obj);
                break;

            // 图片原始大小
            case MSG_SCREEN_SIZE:
                mWeakThread.get().setScreenSize(msg.arg1, msg.arg2);
                break;

            // 保存图片
            case MSG_SAVE_IMAGE:
                mWeakThread.get().saveImage((OnRenderListener) msg.obj);
                break;

            // 设置亮度
            case MSG_SET_BRIGHTNESS:
                mWeakThread.get().setBrightness((Float) msg.obj);
                break;

            // 设置对比度
            case MSG_SET_CONTRAST:
                mWeakThread.get().setContrast((Float) msg.obj);
                break;

            // 设置曝光
            case MSG_SET_EXPOSURE:
                mWeakThread.get().setExposure((Float) msg.obj);
                break;

            // 设置色调
            case MSG_SET_HUE:
                mWeakThread.get().setHue((Float) msg.obj);
                break;

            // 设置饱和度
            case MSG_SET_SATURATION:
                mWeakThread.get().setSaturation((Float) msg.obj);
                break;

            // 设置锐度
            case MSG_SET_SHARPNESS:
                mWeakThread.get().setSharpness((Float) msg.obj);
                break;

            // 切换滤镜
            case MSG_CHANGE_FILTER:
                mWeakThread.get().changeFilter((GLFilterType) msg.obj);
                break;

            default:
                throw new IllegalStateException("Can not handle message what is: " + msg.what);
        }
    }
}
