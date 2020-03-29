package com.cgfay.video.render;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.cgfay.filter.glfilter.color.bean.DynamicColor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 渲染器handler
 */
public class EditRenderHandler extends Handler {

    static final int MSG_INIT = 0x01;               // 初始化
    static final int MSG_DISPLAY_CHANGE = 0x02;     // 显示发生变化
    static final int MSG_DESTROY = 0x03;            // 销毁
    static final int MSG_RENDER = 0x04;             // 渲染
    static final int MSG_CHANGE_FILTER = 0x05;      // 切换滤镜
    static final int MSG_CHANGE_EFFECT = 0x06;      // 切换特效

    private ArrayList<Runnable> mEventQueue = new ArrayList<>();

    private final WeakReference<EditRenderer> mWeakRender;

    public EditRenderHandler(Looper looper, EditRenderer renderer) {
        super(looper);
        mWeakRender = new WeakReference<>(renderer);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (mWeakRender.get() == null) {
            return;
        }
        handleQueueEvent();

        EditRenderer renderer = mWeakRender.get();
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

            // 切换特效
            case MSG_CHANGE_EFFECT: {

                break;
            }
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
