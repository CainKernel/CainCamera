package com.cgfay.filterlibrary.glfilter.multiframe;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 四分镜滤镜
 */
public class GLImageMultiFourthFilter extends GLImageFilter {

    public GLImageMultiFourthFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/multiframe/fragment_multi_fourth.glsl"));
    }

    public GLImageMultiFourthFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
