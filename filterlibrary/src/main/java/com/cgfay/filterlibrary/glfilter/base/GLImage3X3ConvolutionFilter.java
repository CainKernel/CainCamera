package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES20;

public class GLImage3X3ConvolutionFilter extends GLImage3x3TextureSamplingFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "\n" +
            "uniform sampler2D inputTexture;\n" +
            "\n" +
            "uniform mediump mat3 convolutionMatrix;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 leftTextureCoordinate;\n" +
            "varying vec2 rightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 topTextureCoordinate;\n" +
            "varying vec2 topLeftTextureCoordinate;\n" +
            "varying vec2 topRightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 bottomTextureCoordinate;\n" +
            "varying vec2 bottomLeftTextureCoordinate;\n" +
            "varying vec2 bottomRightTextureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    mediump vec4 bottomColor = texture2D(inputTexture, bottomTextureCoordinate);\n" +
            "    mediump vec4 bottomLeftColor = texture2D(inputTexture, bottomLeftTextureCoordinate);\n" +
            "    mediump vec4 bottomRightColor = texture2D(inputTexture, bottomRightTextureCoordinate);\n" +
            "    mediump vec4 centerColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    mediump vec4 leftColor = texture2D(inputTexture, leftTextureCoordinate);\n" +
            "    mediump vec4 rightColor = texture2D(inputTexture, rightTextureCoordinate);\n" +
            "    mediump vec4 topColor = texture2D(inputTexture, topTextureCoordinate);\n" +
            "    mediump vec4 topRightColor = texture2D(inputTexture, topRightTextureCoordinate);\n" +
            "    mediump vec4 topLeftColor = texture2D(inputTexture, topLeftTextureCoordinate);\n" +
            "\n" +
            "    mediump vec4 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];\n" +
            "    resultColor += leftColor * convolutionMatrix[1][0] + centerColor * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];\n" +
            "    resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];\n" +
            "\n" +
            "    gl_FragColor = resultColor;\n" +
            "}";

    private float[] mConvolutionKernel = new float[] {
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f
    };

    private int mUniformConvolutionMatrix;

    public GLImage3X3ConvolutionFilter(Context context) {
        this(context, SAMPLING_VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImage3X3ConvolutionFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mUniformConvolutionMatrix = GLES20.glGetUniformLocation(mProgramHandle, "convolutionMatrix");
        setConvolutionKernel(new float[] {
                -1.0f, 0.0f, 1.0f,
                -2.0f, 0.0f, 2.0f,
                -1.0f, 0.0f, 1.0f
        });
    }

    /**
     * Sets the convolution kernel.
     * @param convolutionKernel the new convolution kernel
     */
    public void setConvolutionKernel(final float[] convolutionKernel) {
        mConvolutionKernel = convolutionKernel;
        setUniformMatrix3f(mUniformConvolutionMatrix, mConvolutionKernel);
    }

}
