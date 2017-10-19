package com.cgfay.caincamera.multimedia;

import android.view.Surface;

import com.cgfay.caincamera.gles.EglCore;

/**
 * 录制接口
 * Created by cain on 2017/10/19.
 */

public interface RecordDrawer {

    void sendDraw();

    void setEglContext(EglCore eglCore, final int textureId,
                       final Surface surface, boolean isRecordable);

    void release();
}
