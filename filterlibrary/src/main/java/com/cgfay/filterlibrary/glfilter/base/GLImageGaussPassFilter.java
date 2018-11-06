package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 某个通道的高斯模糊
 */
class GLImageGaussPassFilter extends GLImageFilter {

    protected float mBlurSize = 1f;

    private int mTexelWidthOffsetHandle;
    private int mTexelHeightOffsetHandle;

    private float mTexelWidth;
    private float mTexelHeight;

    public GLImageGaussPassFilter(Context context) {
        this(context, OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_gaussian_pass.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_gaussian_pass.glsl"));
    }

    public GLImageGaussPassFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mTexelWidthOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelWidthOffset");
        mTexelHeightOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelHeightOffset");
    }

    /**
     * 设置模糊半径大小，默认为1.0f
     * @param blurSize
     */
    public void setBlurSize(float blurSize) {
        mBlurSize = blurSize;
    }

    /**
     * 设置高斯模糊的宽高
     * @param width
     * @param height
     */
    public void setTexelOffsetSize(float width, float height) {
        mTexelWidth = width;
        mTexelHeight = height;
        if (mTexelWidth != 0) {
            setFloat(mTexelWidthOffsetHandle, mBlurSize / mTexelWidth);
        } else {
            setFloat(mTexelWidthOffsetHandle, 0.0f);
        }
        if (mTexelHeight != 0) {
            setFloat(mTexelHeightOffsetHandle, mBlurSize / mTexelHeight);
        } else {
            setFloat(mTexelHeightOffsetHandle, 0.0f);
        }
    }
}
