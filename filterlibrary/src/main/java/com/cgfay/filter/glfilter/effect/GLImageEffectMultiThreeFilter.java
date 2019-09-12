package com.cgfay.filter.glfilter.effect;

import android.content.Context;

import com.cgfay.filter.glfilter.utils.OpenGLUtils;

/**
 * 仿抖音三屏特效
 */
public class GLImageEffectMultiThreeFilter extends GLImageEffectFilter {

    public GLImageEffectMultiThreeFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/effect/fragment_effect_multi_three.glsl"));
    }

    public GLImageEffectMultiThreeFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
