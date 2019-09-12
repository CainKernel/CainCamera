package com.cgfay.filter.glfilter.mosaic;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;

/**
 * 方形马赛克滤镜
 */
public class GLImageMosaicFilter extends GLImageFilter {

    private int mImageWidthFactorLoc;
    private int mImageHeightFactorLoc;
    private int mMosaicSizeLoc;

    private float mImageWidthFactor;
    private float mImageHeightFactor;
    private float mMosaicSize;  // 马赛克大小，1 ~ imagewidth/imageHeight

    public GLImageMosaicFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/mosaic/fragment_mosaic.glsl"));
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
