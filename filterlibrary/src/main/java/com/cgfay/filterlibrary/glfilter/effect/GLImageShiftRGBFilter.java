package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * RGB通道偏移滤镜
 */
public class GLImageShiftRGBFilter extends GLImageFilter {

    private int mScaleHandle;

    private float mScale = 1.0f;
    private float mOffset = 0.0f;

    public GLImageShiftRGBFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_shit_rgb.glsl"));
    }

    public GLImageShiftRGBFilter(Context context, String vertexShader, String fragmentShader) {
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
        mScale = 1.0f + 0.3f * getInterpolation(mOffset);
        mOffset += 0.06f;
        if (mOffset > 1.0f) {
            mOffset = 0.0f;
        }
        GLES20.glUniform1f(mScaleHandle, mScale);
    }

    private float getInterpolation(float input) {
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }
}
