package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 仿抖音缩放特效
 *
 */
public class GLImageEffectScaleFilter extends GLImageEffectFilter {

    private int mScaleHandle;

    private boolean plus = true;
    private float mScale = 1.0f;
    private float mOffset = 0.0f;

    public GLImageEffectScaleFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_scale.glsl"));
    }

    public GLImageEffectScaleFilter(Context context, String vertexShader, String fragmentShader) {
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
        // 步进，60ms一次步进
        float interval = mCurrentPosition % 33.0f;
        mOffset += plus ? + interval * 0.0067f : -interval * 0.0067f;
        if (mOffset >= 1.0f) {
            plus = false;
        } else if (mOffset <= 0.0f) {
            plus = true;
        }
        mScale = 1.0f + 0.5f * getInterpolation(mOffset);
    }

    private float getInterpolation(float input) {
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

}
