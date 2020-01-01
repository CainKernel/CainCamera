package com.cgfay.camera.camera;

import android.graphics.SurfaceTexture;

import androidx.annotation.NonNull;

/**
 * SurfaceTexture准备成功监听器
 */
public interface OnSurfaceTextureListener {

    void onSurfaceTexturePrepared(@NonNull SurfaceTexture surfaceTexture);
}
