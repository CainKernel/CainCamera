package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 仿抖音四屏特效
 */
public class GLImageEffectMultiFourFilter extends GLImageEffectFilter {

    public GLImageEffectMultiFourFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/effect/fragment_effect_multi_four.glsl"));
    }

    public GLImageEffectMultiFourFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
