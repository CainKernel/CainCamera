package com.cgfay.filterlibrary.glfilter.advanced.beauty;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 高通滤波器
 */
class GLImageBeautyHighPassFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture; // 输入原图\n" +
            "uniform sampler2D blurTexture;  // 高斯模糊图片\n" +
            "const float intensity = 24.0;   // 强光程度\n" +
            "void main() {\n" +
            "    lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    lowp vec4 blurColor = texture2D(blurTexture, textureCoordinate);\n" +
            "    // 高通滤波之后的颜色值\n" +
            "    highp vec4 highPassColor = sourceColor - blurColor;\n" +
            "    // 对应混合模式中的强光模式(color = 2.0 * color1 * color2)，对于高反差的颜色来说，color1 和color2 是同一个\n" +
            "    highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * intensity, 0.0, 1.0);\n" +
            "    highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * intensity, 0.0, 1.0);\n" +
            "    highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * intensity, 0.0, 1.0);\n" +
            "    // 输出的是把痘印等过滤掉\n" +
            "    gl_FragColor = vec4(highPassColor.rgb, 1.0);\n" +
            "}";

    private int mBlurTextureHandle;
    private int mBlurTexture;

    public GLImageBeautyHighPassFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageBeautyHighPassFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mBlurTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture");
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mBlurTexture);
        GLES30.glUniform1i(mBlurTextureHandle, 1);
    }

    /**
     * 设置经过高斯模糊的滤镜
     * @param texture
     */
    public void setBlurTexture(int texture) {
        mBlurTexture = texture;
    }

}
