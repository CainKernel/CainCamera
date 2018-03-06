package com.cgfay.cainfilter.glfilter.color;

import android.opengl.GLES30;

import com.cgfay.cainfilter.core.ParamsManager;
import com.cgfay.cainfilter.glfilter.base.BaseImageFilter;
import com.cgfay.cainfilter.utils.GlUtil;

/**
 * 童话滤镜
 * Created by cain.huang on 2017/11/16.
 */

public class FairyTaleFilter extends BaseImageFilter {

    private static final String FRAGMENT_SHADER =
            "precision highp float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform sampler2D faryTaleTexture; // lookup texture\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    mediump float blueColor = textureColor.b * 63.0;\n" +
            "    mediump vec2 quad1;\n" +
            "    quad1.y = floor(floor(blueColor) / 8.0);\n" +
            "    quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
            "    mediump vec2 quad2;\n" +
            "    quad2.y = floor(ceil(blueColor) / 8.0);\n" +
            "    quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
            "    highp vec2 texPos1;\n" +
            "    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "    highp vec2 texPos2;\n" +
            "    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "    lowp vec4 newColor1 = texture2D(faryTaleTexture, texPos1);\n" +
            "    lowp vec4 newColor2 = texture2D(faryTaleTexture, texPos2);\n" +
            "    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            "    gl_FragColor = vec4(newColor.rgb, textureColor.w);\n" +
            "}";


    private int mFairyTaleTexture;
    private int mFairyTaleTextureLoc;

    public FairyTaleFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public FairyTaleFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mFairyTaleTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "faryTaleTexture");
        createTexture();
    }

    private void createTexture() {
        mFairyTaleTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/fairytale.png");
    }

    @Override
    public void onDrawArraysBegin() {
        super.onDrawArraysBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mFairyTaleTexture);
        GLES30.glUniform1i(mFairyTaleTextureLoc, 1);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(1, new int[]{mFairyTaleTexture}, 0);
    }
}
