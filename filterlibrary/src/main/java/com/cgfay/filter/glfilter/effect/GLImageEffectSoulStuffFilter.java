package com.cgfay.filter.glfilter.effect;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filter.glfilter.utils.OpenGLUtils;

/**
 * 仿灵魂出窍特效
 */
public class GLImageEffectSoulStuffFilter extends GLImageEffectFilter {

    private int mScaleHandle;

    private float mScale = 1.0f;
    private float mOffset = 0.0f;

    public GLImageEffectSoulStuffFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_soul_stuff.glsl"));
    }

    public GLImageEffectSoulStuffFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mScaleHandle = GLES20.glGetUniformLocation(mProgramHandle, "scale");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES20.glUniform1f(mScaleHandle, mScale);
    }

    @Override
    protected void calculateInterval() {
        // 步进，40ms算一次步进
        float interval = mCurrentPosition % 40.0f;
        mOffset += interval * 0.0025f;
        if (mOffset > 1.0f) {
            mOffset = 0.0f;
        }
        mScale = 1.0f + 0.3f * getInterpolation(mOffset);
    }

    private float getInterpolation(float input) {
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

}
