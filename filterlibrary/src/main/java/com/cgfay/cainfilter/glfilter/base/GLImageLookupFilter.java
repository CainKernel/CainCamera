package com.cgfay.cainfilter.glfilter.base;

import android.opengl.GLES30;

import com.cgfay.cainfilter.utils.GlUtil;

/**
 * 应用查找表(LUT)滤镜
 * Created by Administrator on 2018/3/8.
 */

public class GLImageLookupFilter extends GLImageFilter {
    private static final String FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputTexture; // 图像texture\n" +
            "uniform sampler2D curveTexture; // 滤镜texture\n" +
            "\n" +
            "uniform lowp float intensity; // 0 ~ 1.0f 变化值\n" +
            "\n" +
            "void main() {\n" +
            "    lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);\n" +
            "\n" +
            "\n" +
            "    mediump float blueColor = textureColor.b * 63.0;\n" +
            "\n" +
            "    mediump vec2 quad1;\n" +
            "    quad1.y = floor(blueColor/8.0);\n" +
            "    quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
            "\n" +
            "    mediump vec2 quad2;\n" +
            "    quad2.y = floor(ceil(blueColor)/7.999);\n" +
            "    quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
            "\n" +
            "    highp vec2 texPos1;\n" +
            "    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "\n" +
            "    highp vec2 texPos2;\n" +
            "    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "\n" +
            "    lowp vec4 newColor1 = texture2D(curveTexture, texPos1);\n" +
            "    lowp vec4 newColor2 = texture2D(curveTexture, texPos2);\n" +
            "\n" +
            "    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            "    gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), intensity);\n" +
            " }";

    private int mIntensityLoc;
    private int mCurveTextureLoc;

    private int mCurveTexture = GlUtil.GL_NOT_INIT;

    public GLImageLookupFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageLookupFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mIntensityLoc = GLES30.glGetUniformLocation(mProgramHandle, "intensity");
        mCurveTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "curveTexture");
        setIntensity(1.0f);
    }

    @Override
    public void onDrawArraysBegin() {
        super.onDrawArraysBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mCurveTexture);
        GLES30.glUniform1i(mCurveTextureLoc, 1);
    }

    @Override
    public void release() {
        GLES30.glDeleteTextures(1, new int[]{mCurveTexture}, 0);
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
        setFloat(mIntensityLoc, opacity);
    }
}
