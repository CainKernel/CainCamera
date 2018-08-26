package com.cgfay.filterlibrary.glfilter.advanced.sticker;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 动态贴纸
 */
public class GLImageStickerFilter extends GLImageFilter {

    public GLImageStickerFilter(Context context) {
        super(context);
    }

    public GLImageStickerFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
