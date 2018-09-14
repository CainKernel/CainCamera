package com.cgfay.filterlibrary.glfilter.advanced.beauty;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.advanced.GLImageGaussianBlurFilter;

/**
 * 美颜用的高斯模糊
 */
class GLImageBeautyBlurFilter extends GLImageGaussianBlurFilter {

    private static final String VERTEX_SHADER = "" +
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "\n" +
            "// 高斯算子左右偏移值，当偏移值为5时，高斯算子为 11 x 11\n" +
            "const int SHIFT_SIZE = 5;\n" +
            "\n" +
            "uniform highp float texelWidthOffset;\n" +
            "uniform highp float texelHeightOffset;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec4 blurShiftCoordinates[SHIFT_SIZE];\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    textureCoordinate = aTextureCoord.xy;\n" +
            "    // 偏移步距\n" +
            "    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n" +
            "    // 记录偏移坐标\n" +
            "    for (int i = 0; i < SHIFT_SIZE; i++) {\n" +
            "        blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,\n" +
            "                                       textureCoordinate.xy + float(i + 1) * singleStepOffset);\n" +
            "    }\n" +
            "}";

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "const int SHIFT_SIZE = 5; // 高斯算子左右偏移值\n" +
            "varying vec4 blurShiftCoordinates[SHIFT_SIZE];\n" +
            "void main() {\n" +
            "    // 计算当前坐标的颜色值\n" +
            "    vec4 currentColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    mediump vec3 sum = currentColor.rgb;\n" +
            "    // 计算偏移坐标的颜色值总和\n" +
            "    for (int i = 0; i < SHIFT_SIZE; i++) {\n" +
            "        sum += texture2D(inputTexture, blurShiftCoordinates[i].xy).rgb;\n" +
            "        sum += texture2D(inputTexture, blurShiftCoordinates[i].zw).rgb;\n" +
            "    }\n" +
            "    // 求出平均值\n" +
            "    gl_FragColor = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);\n" +
            "}";

    public GLImageBeautyBlurFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageBeautyBlurFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

}
