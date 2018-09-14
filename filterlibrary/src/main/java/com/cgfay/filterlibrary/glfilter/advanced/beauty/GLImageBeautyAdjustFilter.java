package com.cgfay.filterlibrary.glfilter.advanced.beauty;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 磨皮调节滤镜，高反差保留法最后一步
 */
class GLImageBeautyAdjustFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;         // 输入原图\n" +
            "uniform sampler2D blurTexture;          // 原图的高斯模糊纹理\n" +
            "uniform sampler2D highPassBlurTexture;  // 高反差保留的高斯模糊纹理\n" +
            "uniform lowp float intensity;           // 磨皮程度\n" +
            "void main() {\n" +
            "    lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    lowp vec4 blurColor = texture2D(blurTexture, textureCoordinate);\n" +
            "    lowp vec4 highPassBlurColor = texture2D(highPassBlurTexture, textureCoordinate);\n" +
            "    // 调节蓝色通道值\n" +
            "    mediump float value = clamp((min(sourceColor.b, blurColor.b) - 0.2) * 5.0, 0.0, 1.0);\n" +
            "    // 找到模糊之后RGB通道的最大值\n" +
            "    mediump float maxChannelColor = max(max(highPassBlurColor.r, highPassBlurColor.g), highPassBlurColor.b);\n" +
            "    // 计算当前的强度\n" +
            "    mediump float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * intensity;\n" +
            "    // 混合输出结果\n" +
            "    lowp vec3 resultColor = mix(sourceColor.rgb, blurColor.rgb, currentIntensity);\n" +
            "    // 输出颜色\n" +
            "    gl_FragColor = vec4(resultColor, 1.0);\n" +
            "}";

    private int mBlurTextureHandle; // 第一轮高斯模糊的结果
    private int mBlurTexture2Handle; // 第二轮高斯模糊的结果

    private int mIntensityHandle; // 磨皮程度 0.0 ~ 1.0
    private float mIntensity;

    private int mBlurTexture;
    private int mHighPassBlurTexture;

    public GLImageBeautyAdjustFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
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
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(getTextureType(), mBlurTexture);
        GLES20.glUniform1i(mBlurTextureHandle, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(getTextureType(), mHighPassBlurTexture);
        GLES20.glUniform1i(mBlurTexture2Handle, 2);

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
