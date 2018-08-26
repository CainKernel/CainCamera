package com.cgfay.cameralibrary.engine.listener;

import java.nio.ByteBuffer;

/**
 * 截帧监听器
 * Created by cain.huang on 2017/12/27.
 */
public interface OnCaptureListener {
    // 截帧回调
    void onCapture(ByteBuffer buffer, int width, int height);
}