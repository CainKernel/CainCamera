package com.cgfay.filterlibrary.glfilter.beauty;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 磨皮调节滤镜，高反差保留法最后一步
 */
class GLImageBeautyAdjustFilter extends GLImageFilter {

    private int mBlurTextureHandle; // 第一轮高斯模糊的结果
    private int mBlurTexture2Handle; // 第二轮高斯模糊的结果

    private int mIntensityHandle; // 磨皮程度 0.0 ~ 1.0
    private float mIntensity;

    private int mBlurTexture;
    private int mHighPassBlurTexture;

    public GLImageBeautyAdjustFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/beauty/fragment_beauty_adjust.glsl"));
    }

    public GLImageBeautyAdjustFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mBlurTextureHandle = GLES20.glGetUniformLocation(mProgramHandle, "blurTexture");
        mBlurTexture2Handle = GLES20.glGetUniformLocation(mProgramHandle, "highPassBlurTexture");
        mIntensityHandle = GLES20.glGetUniformLocation(mProgramHandle, "intensity");
        mIntensity = 1.0f;
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        OpenGLUtils.bindTexture(mBlurTextureHandle, mBlurTexture, 1);
        OpenGLUtils.bindTexture(mBlurTexture2Handle, mHighPassBlurTexture, 2);
        GLES20.glUniform1f(mIntensityHandle, mIntensity);
    }

    /**
     * 设置磨皮程度
     * @param intensity 0 ~ 1.0f
     */
    public void setSkinBeautyIntensity(float intensity) {
        mIntensity = intensity;
    }

    /**
     * 设置另外两个Texture
     * @param blurTexture
     * @param highPassBlurTexture
     */
    public void setBlurTexture(int blurTexture, int highPassBlurTexture) {
        mBlurTexture = blurTexture;
        mHighPassBlurTexture = highPassBlurTexture;
    }

}
