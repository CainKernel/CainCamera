package com.cgfay.filterlibrary.glfilter.advanced.mosaic;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 方形马赛克滤镜
 */
public class GLImageMosaicFilter extends GLImageFilter {

    private static final String FRAGMENT_MOSAIC = "" +
            "precision highp float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform float imageWidthFactor;\n" +
            "uniform float imageHeightFactor;\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform float mosaicSize;\n" +
            "void main()\n" +
            "{\n" +
            "  vec2 uv  = textureCoordinate.xy;\n" +
            "  float dx = mosaicSize * imageWidthFactor;\n" +
            "  float dy = mosaicSize * imageHeightFactor;\n" +
            "  vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));\n" +
            "  vec3 tc = texture2D(inputTexture, coord).xyz;\n" +
            "  gl_FragColor = vec4(tc, 1.0);\n" +
            "}";

    private int mImageWidthFactorLoc;
    private int mImageHeightFactorLoc;
    private int mMosaicSizeLoc;

    private float mImageWidthFactor;
    private float mImageHeightFactor;
    private float mMosaicSize;  // 马赛克大小，1 ~ imagewidth/imageHeight

    public GLImageMosaicFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_MOSAIC);
    }

    public GLImageMosaicFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mImageWidthFactorLoc = GLES30.glGetUniformLocation(mProgramHandle, "imageWidthFactor");
        mImageHeightFactorLoc = GLES30.glGetUniformLocation(mProgramHandle, "imageHeightFactor");
        mMosaicSizeLoc = GLES30.glGetUniformLocation(mProgramHandle, "mosaicSize");
        setMosaicSize(1.0f);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        mImageWidthFactor = 1.0f / width;
        mImageHeightFactor = 1.0f / height;
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniform1f(mMosaicSizeLoc, mMosaicSize);
        GLES30.glUniform1f(mImageWidthFactorLoc, mImageWidthFactor);
        GLES30.glUniform1f(mImageHeightFactorLoc, mImageHeightFactor);
    }

    /**
     * 设置马赛克大小
     * @param size
     */
    public void setMosaicSize(float size) {
        mMosaicSize = size;
    }
}
