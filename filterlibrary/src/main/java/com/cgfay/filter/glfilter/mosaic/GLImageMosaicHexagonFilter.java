package com.cgfay.filter.glfilter.mosaic;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;

/**
 * 六边形马赛克滤镜
 */
public class GLImageMosaicHexagonFilter extends GLImageFilter {

    private int mMosaicSizeHandle;
    private float mMosaicSize;

    public GLImageMosaicHexagonFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/mosaic/fragment_mosaic_hexagon.glsl"));
    }

    public GLImageMosaicHexagonFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mMosaicSizeHandle = GLES30.glGetUniformLocation(mProgramHandle, "mosaicSize");
            setMosaicSize(30.0f);
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniform1f(mMosaicSizeHandle, mMosaicSize * (1.0f / Math.min(mImageWidth, mImageHeight)));
    }

    /**
     * 设置马赛克大小
     * @param size
     */
    public void setMosaicSize(float size) {
        mMosaicSize = size;
    }
}
