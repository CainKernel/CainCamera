package com.cgfay.caincamera.core;

import java.nio.ByteBuffer;

/**
 * 拍照回调
 * Created by cain.huang on 2017/12/27.
 */

public interface CaptureFrameCallback {
    void onFrameCallback(ByteBuffer buffer, int width, int height);
}
