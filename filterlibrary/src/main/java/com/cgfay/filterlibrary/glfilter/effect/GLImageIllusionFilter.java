package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 幻觉滤镜
 */
public class GLImageIllusionFilter extends GLImageFilter {

    private int mLastTextureHandle;
    private int mLookupTableHandle;
    private int mLastTexture;
    private int mLookupTable;

    public GLImageIllusionFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_illusion.glsl"));
    }

    public GLImageIllusionFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mLastTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "inputTextureLast");
            mLookupTableHandle = GLES30.glGetUniformLocation(mProgramHandle, "lookupTable");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        // 绑定上一次纹理
        OpenGLUtils.bindTexture(mLastTextureHandle, mLastTexture, 1);
        // 绑定lut纹理
        OpenGLUtils.bindTexture(mLookupTableHandle, mLookupTable, 2);
    }

    /**
     * 设置上一次纹理id
     * @param lastTexture
     */
    public void setLastTexture(int lastTexture) {
        mLastTexture = lastTexture;
    }

    /**
     * 设置lut纹理id
     * @param lookupTableTexture
     */
    public void setLookupTable(int lookupTableTexture) {
        mLookupTable = lookupTableTexture;
    }
}
