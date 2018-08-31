package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 应用查找表(3D LUT)滤镜(64 x 64)
 * Created by cain.huang on 2018/3/8.
 */
public class GLImage64LookupTableFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
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
            "    mediump float blueColor = textureColor.b * 15.0;\n" +
            "\n" +
            "    mediump vec2 quad1;\n" +
            "    quad1.y = floor(blueColor / 4.0);\n" +
            "    quad1.x = floor(blueColor) - (quad1.y * 4.0);\n" +
            "\n" +
            "    mediump vec2 quad2;\n" +
            "    quad2.y = floor(ceil(blueColor) / 4.0);\n" +
            "    quad2.x = ceil(blueColor) - (quad2.y * 4.0);\n" +
            "\n" +
            "    highp vec2 texPos1;\n" +
            "    texPos1.x = (quad1.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);\n" +
            "    texPos1.y = (quad1.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);\n" +
            "\n" +
            "    highp vec2 texPos2;\n" +
            "    texPos2.x = (quad2.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);\n" +
            "    texPos2.y = (quad2.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);\n" +
            "\n" +
            "    lowp vec4 newColor1 = texture2D(curveTexture, texPos1);\n" +
            "    lowp vec4 newColor2 = texture2D(curveTexture, texPos2);\n" +
            "\n" +
            "    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            "    gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), intensity);\n" +
            " }";

    private float intensity;
    private int mIntensityLoc;
    private int mCurveTextureLoc;

    private int mCurveTexture = OpenGLUtils.GL_NOT_INIT;

    public GLImage64LookupTableFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
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
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mCurveTexture);
        GLES30.glUniform1i(mCurveTextureLoc, 1);
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
