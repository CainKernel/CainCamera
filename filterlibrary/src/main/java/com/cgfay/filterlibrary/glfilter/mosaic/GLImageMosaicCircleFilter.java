package com.cgfay.filterlibrary.glfilter.mosaic;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 圆形马赛克
 */
public class GLImageMosaicCircleFilter extends GLImageFilter {

    private int mImageWidthHandle;
    private int mImageHeightHandle;
    private int mMosaicSizeLoc;

    private float mMosaicSize;

    public GLImageMosaicCircleFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/mosaic/fragment_mosaic_circle.glsl"));
    }

    public GLImageMosaicCircleFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mImageWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageWidth");
            mImageHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageHeight");
            mMosaicSizeLoc = GLES30.glGetUniformLocation(mProgramHandle, "mosaicSize");
            setMosaicSize(30.0f);
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniform1f(mMosaicSizeLoc, mMosaicSize);
        GLES30.glUniform1f(mImageWidthHandle, mImageWidth);
        GLES30.glUniform1f(mImageHeightHandle, mImageHeight);
    }

    /**
     * 设置马赛克大小
     * @param size
     */
    public void setMosaicSize(float size) {
        mMosaicSize = size;
    }
}
