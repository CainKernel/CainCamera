package com.cgfay.filterlibrary.glfilter.advanced.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 锐度变换
 * Created by cain.huang on 2017/8/8.
 */

public class GLImageSharpenFilter extends GLImageFilter {

    private static final String VERTEX_SHADER = "" +
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
    private int mImageWidthFactorHandle;
    private int mImageHeightFactorHandle;

    public GLImageSharpenFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageSharpenFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mImageWidthFactorHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageWidthFactor");
        mImageHeightFactorHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageHeightFactor");
        mSharpnessLoc = GLES30.glGetUniformLocation(mProgramHandle, "sharpness");
        setSharpness(0);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setFloat(mImageWidthFactorHandle, 1.0f / width);
        setFloat(mImageHeightFactorHandle, 1.0f / height);
    }

    /**
     * 设置锐度
     * @param sharpness -4.0 ~ 4.0, 默认为0
     */
    public void setSharpness(float sharpness) {
        if (sharpness < -4.0) {
            sharpness = -4.0f;
        } else if (sharpness > 4.0f) {
            sharpness = 4.0f;
        }
        mSharpness = sharpness;
        setFloat(mSharpnessLoc, mSharpness);
    }
}
