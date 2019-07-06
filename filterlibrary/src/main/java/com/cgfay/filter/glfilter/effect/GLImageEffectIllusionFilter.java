package com.cgfay.filter.glfilter.effect;

import android.content.Context;

import com.cgfay.filter.glfilter.utils.OpenGLUtils;

/**
 * 仿抖音幻觉特效
 */
public class GLImageEffectIllusionFilter extends GLImageEffectFilter {

    public GLImageEffectIllusionFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_illusion.glsl"));
    }

    public GLImageEffectIllusionFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
