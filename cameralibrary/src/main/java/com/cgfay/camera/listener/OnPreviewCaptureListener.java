package com.cgfay.camera.listener;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 媒体拍摄回调
 */
public interface OnPreviewCaptureListener {

    int MediaTypePicture = 0;
    int MediaTypeVideo = 1;
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(value = {MediaTypePicture, MediaTypeVideo})
    @Retention(RetentionPolicy.SOURCE)
    @interface MediaType {}

    // 媒体选择
    void onMediaSelectedListener(String path, @MediaType int type);
}
