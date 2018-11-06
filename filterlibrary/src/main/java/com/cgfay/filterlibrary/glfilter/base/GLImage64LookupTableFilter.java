package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 应用查找表(3D LUT)滤镜(64 x 64)
 * Created by cain.huang on 2018/3/8.
 */
public class GLImage64LookupTableFilter extends GLImageFilter {

    private float intensity;
    private int mIntensityLoc;
    private int mCurveTextureLoc;

    private int mCurveTexture = OpenGLUtils.GL_NOT_INIT;

    public GLImage64LookupTableFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/base/fragment_lookup_table_64.glsl"));
    }

    public GLImage64LookupTableFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mIntensityLoc = GLES30.glGetUniformLocation(mProgramHandle, "intensity");
        mCurveTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "curveTexture");
        setIntensity(1.0f);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        OpenGLUtils.bindTexture(mCurveTextureLoc, mCurveTexture, 1);
        GLES30.glUniform1f(mIntensityLoc, intensity);
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
    public void setIntensity(float value) {
        float opacity;
        if (value <= 0) {
            opacity = 0.0f;
        } else if (value > 1.0f) {
            opacity = 1.0f;
        } else {
            opacity = value;
        }
        intensity = opacity;
        setFloat(mIntensityLoc, intensity);
    }

}
