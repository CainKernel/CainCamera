package com.cgfay.cameralibrary.engine.render;

import android.content.Context;

import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.listener.OnCameraCallback;
import com.cgfay.cameralibrary.engine.listener.OnCaptureListener;
import com.cgfay.cameralibrary.engine.listener.OnFpsListener;

/**
 * 渲染引擎Builder
 */
public final class RenderBuilder {

    private PreviewRenderer mPreviewRenderer;
    private CameraParam mCameraParam;

    RenderBuilder(PreviewRenderer renderer, OnCameraCallback callback) {
        mPreviewRenderer = renderer;
        mCameraParam = CameraParam.getInstance();
        mCameraParam.cameraCallback = callback;
    }

    /**
     * 设置拍照回调
     * @param callback
     */
    public RenderBuilder setCaptureFrameCallback(OnCaptureListener callback) {
        mCameraParam.captureCallback = callback;
        return this;
    }

    /**
     * 设置fps回调
     * @param callback
     */
    public RenderBuilder setFpsCallback(OnFpsListener callback) {
        mCameraParam.fpsCallback = callback;
        return this;
    }

    /**
     * 初始化渲染器
     * @param context
     */
    public void initRenderer(Context context) {
        mPreviewRenderer.initRenderer(context);
    }
}
