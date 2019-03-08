package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 仿抖音闪白特效
 */
public class GLImageEffectGlitterWhiteFilter extends GLImageEffectFilter {

    private int mColorHandle;
    private float color;

    public GLImageEffectGlitterWhiteFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_glitter_white.glsl"));
    }

    public GLImageEffectGlitterWhiteFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mColorHandle = GLES30.glGetUniformLocation(mProgramHandle, "color");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniform1f(mColorHandle, color);
    }

    @Override
    protected void calculateInterval() {
        // 步进，40ms算一次步进
        float interval = mCurrentPosition % 40.0f;
        color += interval * 0.018f;
        if (color > 1.0f) {
            color = 0.0f;
        }
    }
}
