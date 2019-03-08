package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 仿抖音黑白三屏特效
 */
public class GLImageEffectBlackWhiteThreeFilter extends GLImageEffectFilter {

    private int mScaleHandle;
    private float mScale = 1.2f;

    public GLImageEffectBlackWhiteThreeFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/effect/fragment_effect_multi_bw_three.glsl"));
    }

    public GLImageEffectBlackWhiteThreeFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mScaleHandle = GLES30.glGetUniformLocation(mProgramHandle, "scale");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniform1f(mScaleHandle, mScale);
    }
}
