package com.cgfay.filterlibrary.glfilter.beauty;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageGaussianBlurFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 美颜用的高斯模糊
 */
class GLImageBeautyBlurFilter extends GLImageGaussianBlurFilter {

    public GLImageBeautyBlurFilter(Context context) {
        this(context, OpenGLUtils.getShaderFromAssets(context, "shader/beauty/vertex_beauty_blur.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/beauty/fragment_beauty_blur.glsl"));
    }

    public GLImageBeautyBlurFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

}
