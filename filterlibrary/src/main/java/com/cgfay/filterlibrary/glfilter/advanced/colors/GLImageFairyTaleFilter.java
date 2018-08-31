package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 童话滤镜
 * Created by cain.huang on 2017/11/16.
 */

public class GLImageFairyTaleFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
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
    private int mFairyTaleTextureHandle;

    public GLImageFairyTaleFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageFairyTaleFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mFairyTaleTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "faryTaleTexture");
        createTexture();
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        mFairyTaleTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/fairytale.png");
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mFairyTaleTexture);
        GLES30.glUniform1i(mFairyTaleTextureHandle, 1);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(1, new int[]{mFairyTaleTexture}, 0);
    }
}
