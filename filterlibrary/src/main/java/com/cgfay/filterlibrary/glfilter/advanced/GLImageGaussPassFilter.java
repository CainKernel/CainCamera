package com.cgfay.filterlibrary.glfilter.advanced;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

class GLImageGaussPassFilter extends GLImageFilter {

    private static final String VERTEX_SHADER = "" +
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "\n" +
            "const int GAUSSIAN_SAMPLES = 9;\n" +
            "\n" +
            "uniform float texelWidthOffset;\n" +
            "uniform float texelHeightOffset;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	gl_Position = uMVPMatrix * aPosition;\n" +
            "	textureCoordinate = aTextureCoord.xy;\n" +
            "	\n" +
            "	// Calculate the positions for the blur\n" +
            "	int multiplier = 0;\n" +
            "	vec2 blurStep;\n" +
            "   vec2 singleStepOffset = vec2(texelHeightOffset, texelWidthOffset);\n" +
            "    \n" +
            "	for (int i = 0; i < GAUSSIAN_SAMPLES; i++)\n" +
            "   {\n" +
            "		multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));\n" +
            "       // Blur in x (horizontal)\n" +
            "       blurStep = float(multiplier) * singleStepOffset;\n" +
            "		blurCoordinates[i] = aTextureCoord.xy + blurStep;\n" +
            "	}\n" +
            "}\n";

    private static final String FRAGMENT_SHADER = "" +
            "uniform sampler2D inputTexture;\n" +
            "\n" +
            "const lowp int GAUSSIAN_SAMPLES = 9;\n" +
            "\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	lowp vec3 sum = vec3(0.0);\n" +
            "   lowp vec4 fragColor=texture2D(inputTexture,textureCoordinate);\n" +
            "	\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[0]).rgb * 0.05;\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[1]).rgb * 0.09;\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[2]).rgb * 0.12;\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[3]).rgb * 0.15;\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[4]).rgb * 0.18;\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[5]).rgb * 0.15;\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[6]).rgb * 0.12;\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[7]).rgb * 0.09;\n" +
            "    sum += texture2D(inputTexture, blurCoordinates[8]).rgb * 0.05;\n" +
            "\n" +
            "	gl_FragColor = vec4(sum, fragColor.a);\n" +
            "}";

    protected float mBlurSize = 1f;

    private int mTexelWidthOffsetHandle;
    private int mTexelHeightOffsetHandle;

    private float mTexelWidth;
    private float mTexelHeight;

    public GLImageGaussPassFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageGaussPassFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mTexelWidthOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelWidthOffset");
        mTexelHeightOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelHeightOffset");
    }

    /**
     * 设置模糊半径大小，默认为1.0f
     * @param blurSize
     */
    public void setBlurSize(float blurSize) {
        mBlurSize = blurSize;
    }

    /**
     * 设置高斯模糊的宽高
     * @param width
     * @param height
     */
    public void setTexelOffsetSize(float width, float height) {
        mTexelWidth = width;
        mTexelHeight = height;
        if (mTexelWidth != 0) {
            setFloat(mTexelWidthOffsetHandle, mBlurSize / mTexelWidth);
        } else {
            setFloat(mTexelWidthOffsetHandle, 0.0f);
        }
        if (mTexelHeight != 0) {
            setFloat(mTexelHeightOffsetHandle, mBlurSize / mTexelHeight);
        } else {
            setFloat(mTexelHeightOffsetHandle, 0.0f);
        }
    }
}
