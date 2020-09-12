package com.cgfay.caincamera.renderer;

import android.content.Context;
import android.opengl.GLES30;
import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;

/**
 * 同框录制
 */
public class GLImageDuetFilter extends GLImageFilter {

    // 变换矩阵句柄
    private int mMVPMatrixHandle;
    private float[] mMVPMatrix = new float[16];

    private int mOffsetDxHandle;
    private int mOffsetDyHandle;
    private int mTypeHandle;

    public GLImageDuetFilter(Context context) {
        this(context, OpenGLUtils.getShaderFromAssets(context,
                "shader/multiframe/vertex_duet.glsl"),
                OpenGLUtils.getShaderFromAssets(context,
                "shader/multiframe/fragment_duet.glsl"));
    }

    private GLImageDuetFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
            mOffsetDxHandle = GLES30.glGetUniformLocation(mProgramHandle, "offset_dx");
            mOffsetDyHandle = GLES30.glGetUniformLocation(mProgramHandle, "offset_dy");
            mTypeHandle = GLES30.glGetUniformLocation(mProgramHandle, "type");
        } else {
            mMVPMatrixHandle = OpenGLUtils.GL_NOT_INIT;
            mOffsetDxHandle = OpenGLUtils.GL_NOT_INIT;
            mOffsetDyHandle = OpenGLUtils.GL_NOT_INIT;
            mTypeHandle = OpenGLUtils.GL_NOT_INIT;
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mMVPMatrixHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        }
        // 绘制到FBO中，需要开启混合模式
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);
        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA, GLES30.GL_ONE, GLES30.GL_ONE);
    }

    @Override
    public void onDrawFrameAfter() {
        super.onDrawFrameAfter();
        GLES30.glDisable(GLES30.GL_BLEND);
    }

    public void setMVPMatrix(float[] MVPMatrix) {
        mMVPMatrix = MVPMatrix;
    }

    /**
     * 设置同框类型
     */
    public void setDuetType(float type) {
        if (mTypeHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mTypeHandle, type);
        }
    }

    /**
     * 设置x轴偏移量
     */
    public void setOffsetX(float offset) {
        if (mOffsetDxHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mOffsetDxHandle, offset);
        }
    }

    /**
     * 设置y轴偏移量
     */
    public void setOffsetY(float offset) {
        if (mOffsetDyHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mOffsetDyHandle, offset);
        }
    }
}
