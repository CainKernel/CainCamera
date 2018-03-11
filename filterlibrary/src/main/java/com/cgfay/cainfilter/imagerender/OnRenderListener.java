package com.cgfay.cainfilter.imagerender;

import java.nio.ByteBuffer;

/**
 * 渲染回调
 * Created by cain on 2018/3/11.
 */
public interface OnRenderListener {

    // 保存图片回调
    void onSaveImageListener(ByteBuffer buffer, int width, int height);
}
