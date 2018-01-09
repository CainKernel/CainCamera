package com.cgfay.cainfilter.filter.image;

import android.opengl.GLES30;

import com.cgfay.cainfilter.filter.base.BaseImageFilter;

/**
 * 锐度变换
 * Created by cain.huang on 2017/8/8.
 */

public class SharpnessFilter extends BaseImageFilter {
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;                                       \n" +
            "attribute vec4 aPosition;                                      \n" +
            "attribute vec4 aTextureCoord;                                  \n" +
            "                                                               \n" +
            "uniform float imageWidthFactor;                                \n" +
            "uniform float imageHeightFactor;                               \n" +
            "uniform float sharpness;                                       \n" +
            "                                                               \n" +
            "varying vec2 textureCoordinate;                                \n" +
            "varying vec2 leftTextureCoordinate;                            \n" +
            "varying vec2 rightTextureCoordinate;                           \n" +
            "varying vec2 topTextureCoordinate;                             \n" +
            "varying vec2 bottomTextureCoordinate;                          \n" +
            "                                                               \n" +
            "varying float centerMultiplier;                                \n" +
            "varying float edgeMultiplier;                                  \n" +
            "void main()                                                    \n" +
            "{                                                              \n" +
            "    gl_Position = aPosition;                                   \n" +
            "                                                               \n" +
            "    mediump vec2 widthStep = vec2(imageWidthFactor, 0.0);      \n" +
            "    mediump vec2 heightStep = vec2(0.0, imageHeightFactor);    \n" +
            "                                                               \n" +
            "    textureCoordinate = aTextureCoord.xy;                      \n" +
            "    leftTextureCoordinate = aTextureCoord.xy - widthStep;      \n" +
            "    rightTextureCoordinate = aTextureCoord.xy + widthStep;     \n" +
            "    topTextureCoordinate = aTextureCoord.xy + heightStep;      \n" +
            "    bottomTextureCoordinate = aTextureCoord.xy - heightStep;   \n" +
            "                                                               \n" +
            "    centerMultiplier = 1.0 + 4.0 * sharpness;                  \n" +
            "    edgeMultiplier = sharpness;                                \n" +
            "}                                                              ";

    private static final String FRAGMENT_SHADER =
            "precision highp float;                                                                             \n" +
            "                                                                                                   \n" +
            "varying highp vec2 textureCoordinate;                                                              \n" +
            "varying highp vec2 leftTextureCoordinate;                                                          \n" +
            "varying highp vec2 rightTextureCoordinate;                                                         \n" +
            "varying highp vec2 topTextureCoordinate;                                                           \n" +
            "varying highp vec2 bottomTextureCoordinate;                                                        \n" +
            "                                                                                                   \n" +
            "varying highp float centerMultiplier;                                                              \n" +
            "varying highp float edgeMultiplier;                                                                \n" +
            "                                                                                                   \n" +
            "uniform sampler2D inputImageTexture;                                                               \n" +
            "                                                                                                   \n" +
            "void main()                                                                                        \n" +
            "{                                                                                                  \n" +
            "    mediump vec3 textureColor = texture2D(inputImageTexture, textureCoordinate).rgb;               \n" +
            "    mediump vec3 leftTextureColor = texture2D(inputImageTexture, leftTextureCoordinate).rgb;       \n" +
            "    mediump vec3 rightTextureColor = texture2D(inputImageTexture, rightTextureCoordinate).rgb;     \n" +
            "    mediump vec3 topTextureColor = texture2D(inputImageTexture, topTextureCoordinate).rgb;         \n" +
            "    mediump vec3 bottomTextureColor = texture2D(inputImageTexture, bottomTextureCoordinate).rgb;   \n" +
            "                                                                                                   \n" +
            "    gl_FragColor = vec4((textureColor * centerMultiplier                                           \n" +
            "               - (leftTextureColor * edgeMultiplier                                                \n" +
            "               + rightTextureColor * edgeMultiplier                                                \n" +
            "               + topTextureColor * edgeMultiplier                                                  \n" +
            "               + bottomTextureColor * edgeMultiplier)),                                            \n" +
            "               texture2D(inputImageTexture, bottomTextureCoordinate).w);                           \n" +
            "}                                                                                                  ";



    private int mSharpnessLoc;
    private float mSharpness;
    private int mImageWidthFactorLoc;
    private int mImageHeightFactorLoc;

    public SharpnessFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public SharpnessFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mImageWidthFactorLoc = GLES30.glGetUniformLocation(mProgramHandle, "imageWidthFactor");
        mImageHeightFactorLoc = GLES30.glGetUniformLocation(mProgramHandle, "imageHeightFactor");
        mSharpnessLoc = GLES30.glGetUniformLocation(mProgramHandle, "sharpness");
        setSharpness(0);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setFloat(mImageWidthFactorLoc, 1.0f / width);
        setFloat(mImageHeightFactorLoc, 1.0f / height);
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        mSharpness = sharpness;
        setFloat(mSharpnessLoc, mSharpness);
    }
}
