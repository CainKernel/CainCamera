package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

public class GLImageEffectMultiSixFilter extends GLImageEffectFilter {

    public GLImageEffectMultiSixFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/effect/fragment_effect_multi_six.glsl"));
    }

    public GLImageEffectMultiSixFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
