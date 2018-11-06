package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 缩放滤镜
 */
public class GLImageScaleFilter extends GLImageFilter {

    private int mScaleHandle;

    private boolean plus = true;
    private float mScale = 1.0f;
    private float mOffset = 0.0f;

    public GLImageScaleFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_scale.glsl"));
    }

    public GLImageScaleFilter(Context context, String vertexShader, String fragmentShader) {
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
        mOffset += plus ? +0.06f : -0.06f;
        if (mOffset >= 1.0f) {
            plus = false;
        } else if (mOffset <= 0.0f) {
            plus = true;
        }
        mScale = 1.0f + 0.5f * getInterpolation(mOffset);
        GLES20.glUniform1f(mScaleHandle, mScale);
    }

    private float getInterpolation(float input) {
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

}
