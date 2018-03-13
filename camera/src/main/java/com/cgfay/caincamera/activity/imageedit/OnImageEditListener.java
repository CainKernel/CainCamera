package com.cgfay.caincamera.activity.imageedit;

import java.nio.ByteBuffer;

/**
 * 图片编辑线程回调
 * Created by Administrator on 2018/3/13.
 */
public interface OnImageEditListener {
    // 创建Texture回调
    void onTextureCreated();
    // 保存图片回调
    void onSaveImageListener(ByteBuffer buffer, int width, int height);
}
