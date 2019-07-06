package com.cgfay.camera.engine.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cgfay.camera.engine.camera.CameraParam;
import com.cgfay.camera.engine.listener.OnCameraCallback;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filter.glfilter.stickers.StaticStickerNormalFilter;
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker;

import java.lang.ref.WeakReference;


/**
 * 预览渲染器
 * Created by cain on 2018/8/15.
 */

public final class PreviewRenderer {

    private PreviewRenderer() {
        mCameraParam = CameraParam.getInstance();
    }

    private static class RenderHolder {
        private static PreviewRenderer instance = new PreviewRenderer();
    }

    public static PreviewRenderer getInstance() {
        return RenderHolder.instance;
    }

    // 相机渲染参数
    private CameraParam mCameraParam;

    // 渲染Handler
    private RenderHandler mRenderHandler;
    // 渲染线程
    private RenderThread mPreviewRenderThread;
    // 操作锁
    private final Object mSynOperation = new Object();

    /**
     * 设置相机回调
     * @param callback
     * @return
     */
    public RenderBuilder setCameraCallback(OnCameraCallback callback) {
        return new RenderBuilder(this, callback);
    }

    /**
     * 初始化渲染器
     */
    void initRenderer(Context context) {
        synchronized (mSynOperation) {
            mPreviewRenderThread = new RenderThread(context, "RenderThread");
            mPreviewRenderThread.start();
            mRenderHandler = new RenderHandler(mPreviewRenderThread);
            // 绑定Handler
            mPreviewRenderThread.setRenderHandler(mRenderHandler);
        }
    }

    /**
     * 销毁渲染器
     */
    public void destroyRenderer() {
        synchronized (mSynOperation) {
            if (mRenderHandler != null) {
                mRenderHandler.removeCallbacksAndMessages(null);
                mRenderHandler = null;
            }
            if (mPreviewRenderThread != null) {
                mPreviewRenderThread.quitSafely();
                try {
                    mPreviewRenderThread.join();
                } catch (InterruptedException e) {

                }
                mPreviewRenderThread = null;
            }
        }
    }

    /**
     * 绑定Surface
     * @param surface
     */
    public void bindSurface(Surface surface) {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_SURFACE_CREATED, surface));
        }
    }

    /**
     * 绑定SurfaceTexture
     * @param surfaceTexture
     */
    public void bindSurface(SurfaceTexture surfaceTexture) {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_SURFACE_CREATED, surfaceTexture));
        }
    }

    /**
     * 改变预览大小
     * @param width
     * @param height
     */
    public void changePreviewSize(int width, int height) {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_SURFACE_CHANGED, width, height));
        }
    }

    /**
     * 解绑Surface
     */
    public void unbindSurface() {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_SURFACE_DESTROYED));
        }
    }

    /**
     * 请求渲染
     */
    public void requestRender() {
        if (mPreviewRenderThread != null) {
            mPreviewRenderThread.requestRender();
        }
    }

    /**
     * 切换边框模糊功能
     * @param enableEdgeBlur
     */
    public void changeEdgeBlurFilter(boolean enableEdgeBlur) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_CHANGE_EDGE_BLUR, enableEdgeBlur));
        }
    }

    /**
     * 切换滤镜
     * @param color
     */
    public void changeDynamicFilter(DynamicColor color) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_CHANGE_DYNAMIC_COLOR, color));
        }
    }

    /**
     * 切换彩妆
     * @param makeup
     */
    public void changeDynamicMakeup(DynamicMakeup makeup) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_CHANGE_DYNAMIC_MAKEUP, makeup));
        }
    }

    /**
     * 切换动态资源
     * @param color
     */
    public void changeDynamicResource(DynamicColor color) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_CHANGE_DYNAMIC_RESOURCE, color));
        }
    }

    /**
     * 切换动态资源
     * @param sticker
     */
    public void changeDynamicResource(DynamicSticker sticker) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_CHANGE_DYNAMIC_RESOURCE, sticker));
        }
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_START_RECORDING));
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendEmptyMessage(RenderHandler.MSG_STOP_RECORDING);
        }
    }

    /**
     * 拍照
     */
    public void takePicture() {
        synchronized (mSynOperation) {
            if (!mCameraParam.isTakePicture) {
                mCameraParam.isTakePicture = true;
            }
        }
    }

    /**
     * 切换相机
     */
    public void switchCamera() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendEmptyMessage(RenderHandler.MSG_SWITCH_CAMERA);
        }
    }

    /**
     * 重新打开相机
     */
    public void reopenCamera() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendEmptyMessage(RenderHandler.MSG_REOPEN_CAMERA);
        }
    }

    /**
     * 是否需要进行对比
     * @param enable
     */
    public void enableCompare(boolean enable) {
        synchronized (mSynOperation) {
            mCameraParam.showCompare = enable;
        }
    }
    /**
     * 测试贴纸触摸事件
     * @param e
     */
    public StaticStickerNormalFilter touchDown(MotionEvent e) {
        synchronized (mSynOperation) {
            if (mPreviewRenderThread != null) {
                return mPreviewRenderThread.touchDown(e);
        }
        }
        return null;
    }
}
