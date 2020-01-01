package com.cgfay.camera.render;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 渲染器Handler
 */
public class CameraRenderHandler extends Handler {

    static final int MSG_INIT = 0x01;               // 初始化
    static final int MSG_DISPLAY_CHANGE = 0x02;     // 显示发生变化
    static final int MSG_DESTROY = 0x03;            // 销毁
    static final int MSG_RENDER = 0x04;             // 渲染
    static final int MSG_CHANGE_FILTER = 0x05;      // 切换滤镜
    static final int MSG_CHANGE_MAKEUP = 0x06;      // 切换彩妆
    static final int MSG_CHANGE_RESOURCE = 0x07;    // 切换贴纸资源
    static final int MSG_CHANGE_EDGE_BLUR = 0x08;   // 边框模糊功能

    // 渲染事件处理队列
    private ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();

    private final WeakReference<CameraRenderer> mWeakRender;

    public CameraRenderHandler(Looper looper, CameraRenderer renderer) {
        super(looper);
        mWeakRender = new WeakReference<>(renderer);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeakRender.get() == null) {
            return;
        }

        handleQueueEvent();

        CameraRenderer renderer = mWeakRender.get();
        switch (msg.what) {
            // 初始化GL环境
            case MSG_INIT:
                if (msg.obj instanceof SurfaceHolder) {
                    renderer.initRender(((SurfaceHolder)msg.obj).getSurface());
                } else if (msg.obj instanceof Surface) {
                    renderer.initRender((Surface)msg.obj);
                } else if (msg.obj instanceof SurfaceTexture) {
                    renderer.initRender((SurfaceTexture) msg.obj);
                }
                break;

            case MSG_DISPLAY_CHANGE:
                renderer.setDisplaySize(msg.arg1, msg.arg2);
                break;

            // 销毁GL环境
            case MSG_DESTROY:
                renderer.release();
                break;

            // 渲染一帧数据
            case MSG_RENDER:
                renderer.onDrawFrame();
                break;

            // 切换滤镜
            case MSG_CHANGE_FILTER:
                renderer.changeDynamicFilter((DynamicColor) msg.obj);
                break;

            // 切换彩妆
            case MSG_CHANGE_MAKEUP:
                if (msg.obj == null) {
                    renderer.changeDynamicMakeup((DynamicMakeup) null);
                } else {
                    renderer.changeDynamicMakeup((DynamicMakeup) msg.obj);
                }
                break;

            // 切换道具资源
            case MSG_CHANGE_RESOURCE:
                if (msg.obj == null) {
                    renderer.changeDynamicResource((DynamicSticker) null);
                } else if (msg.obj instanceof DynamicColor) {
                    renderer.changeDynamicResource((DynamicColor) msg.obj);
                } else if (msg.obj instanceof DynamicSticker) {
                    renderer.changeDynamicResource((DynamicSticker) msg.obj);
                }
                break;

            // 切换边框模糊滤镜
            case MSG_CHANGE_EDGE_BLUR:
                renderer.changeEdgeBlurFilter((boolean)msg.obj);
                break;
        }
    }

    /**
     * 处理优先的队列事件
     */
    void handleQueueEvent() {
        synchronized (this) {
            Runnable runnable;
            while (!mEventQueue.isEmpty()) {
                runnable = mEventQueue.remove(0);
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }

    /**
     * 入队事件
     * @param runnable
     */
    public void queueEvent(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("runnable must not be null");
        }
        synchronized (this) {
            mEventQueue.add(runnable);
            notifyAll();
        }
    }
}
