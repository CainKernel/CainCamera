package com.cgfay.cameralibrary.listener;

import com.cgfay.cameralibrary.engine.model.GalleryType;

/**
 * 媒体拍摄回调
 */
public interface OnPreviewCaptureListener {

    // 媒体选择
    void onMediaSelectedListener(String path, GalleryType type);
}
