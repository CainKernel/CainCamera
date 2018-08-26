package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES20;

public class GLImage3x3TextureSamplingFilter extends GLImageFilter {

    protected static final String SAMPLING_VERTEX_SHADER = "" +
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "\n" +
            "uniform highp float texelWidth; \n" +
            "uniform highp float texelHeight; \n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 leftTextureCoordinate;\n" +
            "varying vec2 rightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 topTextureCoordinate;\n" +
            "varying vec2 topLeftTextureCoordinate;\n" +
            "varying vec2 topRightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 bottomTextureCoordinate;\n" +
            "varying vec2 bottomLeftTextureCoordinate;\n" +
            "varying vec2 bottomRightTextureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "\n" +
            "    vec2 widthStep = vec2(texelWidth, 0.0);\n" +
            "    vec2 heightStep = vec2(0.0, texelHeight);\n" +
            "    vec2 widthHeightStep = vec2(texelWidth, texelHeight);\n" +
            "    vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);\n" +
            "\n" +
            "    textureCoordinate = aTextureCoord.xy;\n" +
            "    leftTextureCoordinate = aTextureCoord.xy - widthStep;\n" +
            "    rightTextureCoordinate = aTextureCoord.xy + widthStep;\n" +
            "\n" +
            "    topTextureCoordinate = aTextureCoord.xy - heightStep;\n" +
            "    topLeftTextureCoordinate = aTextureCoord.xy - widthHeightStep;\n" +
            "    topRightTextureCoordinate = aTextureCoord.xy + widthNegativeHeightStep;\n" +
            "\n" +
            "    bottomTextureCoordinate = aTextureCoord.xy + heightStep;\n" +
            "    bottomLeftTextureCoordinate = aTextureCoord.xy - widthNegativeHeightStep;\n" +
            "    bottomRightTextureCoordinate = aTextureCoord.xy + widthHeightStep;\n" +
            "}";

    private int mUniformTexelWidthLocation;
    private int mUniformTexelHeightLocation;

    private boolean mHasOverriddenImageSizeFactor = false;
    private float mTexelWidth;
    private float mTexelHeight;
    private float mLineSize = 1.0f;

    public GLImage3x3TextureSamplingFilter(Context context) {
        this(context, SAMPLING_VERTEX_SHADER, FRAGMENT_SHADER_2D);
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
