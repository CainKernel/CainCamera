package com.cgfay.camera.camera;

import android.graphics.SurfaceTexture;

public interface OnFrameAvailableListener {
    void onFrameAvailable(SurfaceTexture surfaceTexture);
}
