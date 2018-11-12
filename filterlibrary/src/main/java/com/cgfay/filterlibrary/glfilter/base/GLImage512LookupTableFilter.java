package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 应用查找表(3D LUT)滤镜(512 x 512)
 * Created by cain.huang on 2018/3/8.
 */

public class GLImage512LookupTableFilter extends GLImageFilter {

    private float mStrength;
    private int mStrengthHandle;
    private int mLookupTableTextureHandle;

    private int mCurveTexture = OpenGLUtils.GL_NOT_INIT;

    public GLImage512LookupTableFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/base/fragment_lookup_table_512.glsl"));
    }

    public GLImage512LookupTableFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "strength");
        mLookupTableTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "curveTexture");
        setStrength(1.0f);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        OpenGLUtils.bindTexture(mLookupTableTextureHandle, mCurveTexture, 1);
        GLES30.glUniform1f(mStrengthHandle, mStrength);
    }

    @Override
    public void release() {
        GLES30.glDeleteTextures(1, new int[]{ mCurveTexture }, 0);
        super.release();
    }

    /**
     *  设置变化值，0.0f ~ 1.0f
     * @param value
     */
    public void setStrength(float value) {
        float opacity;
        if (value <= 0) {
            opacity = 0.0f;
        } else if (value > 1.0f) {
            opacity = 1.0f;
        } else {
            opacity = value;
        }
        mStrength = opacity;
        setFloat(mStrengthHandle, mStrength);
    }
}
