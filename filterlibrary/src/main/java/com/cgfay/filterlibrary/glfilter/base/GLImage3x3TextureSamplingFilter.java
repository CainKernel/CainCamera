package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

public class GLImage3x3TextureSamplingFilter extends GLImageFilter {

    private int mUniformTexelWidthLocation;
    private int mUniformTexelHeightLocation;

    private boolean mHasOverriddenImageSizeFactor = false;
    private float mTexelWidth;
    private float mTexelHeight;
    private float mLineSize = 1.0f;

    public GLImage3x3TextureSamplingFilter(Context context) {
        this(context, OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_3x3_texture_sampling.glsl"),
                FRAGMENT_SHADER);
    }

    public GLImage3x3TextureSamplingFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mUniformTexelWidthLocation = GLES20.glGetUniformLocation(mProgramHandle, "texelWidth");
        mUniformTexelHeightLocation = GLES20.glGetUniformLocation(mProgramHandle, "texelHeight");
        if (mTexelWidth != 0) {
            updateTexelValues();
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (!mHasOverriddenImageSizeFactor) {
            setLineSize(mLineSize);
        }
    }

    public void setTexelWidth(final float texelWidth) {
        mHasOverriddenImageSizeFactor = true;
        mTexelWidth = texelWidth;
        setFloat(mUniformTexelWidthLocation, texelWidth);
    }

    public void setTexelHeight(final float texelHeight) {
        mHasOverriddenImageSizeFactor = true;
        mTexelHeight = texelHeight;
        setFloat(mUniformTexelHeightLocation, texelHeight);
    }

    public void setLineSize(final float size) {
        mLineSize = size;
        mTexelWidth = size / mImageWidth;
        mTexelHeight = size / mImageHeight;
        updateTexelValues();
    }

    private void updateTexelValues() {
        setFloat(mUniformTexelWidthLocation, mTexelWidth);
        setFloat(mUniformTexelHeightLocation, mTexelHeight);
    }

}
