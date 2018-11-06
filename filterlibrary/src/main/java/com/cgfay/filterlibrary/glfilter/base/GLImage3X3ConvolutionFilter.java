package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

public class GLImage3X3ConvolutionFilter extends GLImage3x3TextureSamplingFilter {

    private float[] mConvolutionKernel = new float[] {
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f
    };

    private int mUniformConvolutionMatrix;

    public GLImage3X3ConvolutionFilter(Context context) {
        this(context, OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_3x3_texture_sampling.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_3x3_convolution.glsl"));
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
