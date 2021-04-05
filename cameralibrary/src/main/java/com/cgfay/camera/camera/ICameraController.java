package com.cgfay.camera.camera;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;

/**
 * 相机控制器接口
 */
public interface ICameraController {

    /**
     * 打开相机
     */
    void openCamera();

    /**
     * 关闭相机
     */
    void closeCamera();

    /**
     * 设置准备成功监听器
     * @param listener
     */
    void setOnSurfaceTextureListener(OnSurfaceTextureListener listener);

    /**
     * 设置预览回调
     */
    void setPreviewCallback(PreviewCallback callback);

    /**
     * 设置纹理更新回调
     */
    void setOnFrameAvailableListener(OnFrameAvailableListener listener);

    /**
     * 切换相机
     */
    void switchCamera();

    /**
     * 设置是否为前置摄像头
     * @param front 是否前置摄像头
     */
    void setFront(boolean front);

    /**
     * 是否前置摄像头
     */
    boolean isFront();

    /**
     * 获取预览Surface的旋转角度
     */
    int getOrientation();

    /**
     * 获取预览宽度
     */
    int getPreviewWidth();

    /**
     * 获取预览高度
     */
    int getPreviewHeight();

    /**
     * 是否支持自动对焦
     */
    boolean canAutoFocus();

    /**
     * 设置对焦区域
     * @param rect 对焦区域
     */
    void setFocusArea(Rect rect);

    /**
     * 获取对焦区域
     */
    Rect getFocusArea(float x, float y, int width, int height, int focusSize);

    /**
     * 判断是否支持闪光灯
     * @param front 是否前置摄像头
     */
    boolean supportTorch(boolean front);

    /**
     * 设置闪光灯
     * @param on 是否打开闪光灯
     */
    void setFlashLight(boolean on);

    /**
     * zoom in
     */
    void zoomIn();

    /**
     * zoom out
     */
    void zoomOut();
}
