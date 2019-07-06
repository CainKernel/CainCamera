package com.cgfay.camera.engine.listener;

/**
 * 相机回调
 */
public interface OnCameraCallback {

    // 相机已打开
    void onCameraOpened();

    // 预览回调
    void onPreviewCallback(byte[] data);
}
