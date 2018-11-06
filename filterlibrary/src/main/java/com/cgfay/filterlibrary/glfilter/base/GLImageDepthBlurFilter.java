package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * 景深滤镜
 */
public class GLImageDepthBlurFilter extends GLImageFilter {

    private int mBlurImageHandle;
    private int mInnerHandle;
    private int mOuterHandle;
    private int mWidthHandle;
    private int mHeightHandle;
    private int mCenterHandle;
    private int mLine1Handle;
    private int mLine2Handle;
    private int mIntensityHandle;

    // 高斯模糊滤镜
    private GLImageGaussianBlurFilter mGaussianBlurFilter;

    // 高斯模糊图像缩放半径
    private float mBlurScale = 0.5f;
    // 存储经过高斯模糊处理的纹理id
    private int mBlurTexture;

    public GLImageDepthBlurFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/base/fragment_depth_blur.glsl"));
    }

    public GLImageDepthBlurFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mGaussianBlurFilter = new GLImageGaussianBlurFilter(context);
        mBlurTexture = OpenGLUtils.GL_NOT_TEXTURE;
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mBlurImageHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurImageTexture");
            mInnerHandle = GLES30.glGetUniformLocation(mProgramHandle, "inner");
            mOuterHandle = GLES30.glGetUniformLocation(mProgramHandle, "outer");
            mWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "width");
            mHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "height");
            mCenterHandle = GLES30.glGetUniformLocation(mProgramHandle, "center");
            mLine1Handle = GLES30.glGetUniformLocation(mProgramHandle, "line1");
            mLine2Handle = GLES30.glGetUniformLocation(mProgramHandle, "line2");
            mIntensityHandle = GLES30.glGetUniformLocation(mProgramHandle, "intensity");
            initUniformData();
        }
    }

    /**
     * 初始化同一变量值
     */
    private void initUniformData() {
        setFloat(mInnerHandle, 0.35f);
        setFloat(mOuterHandle, 0.12f);
        setPoint(mCenterHandle, new PointF(0.5f, 0.5f));
        setFloatVec3(mLine1Handle, new float[] {0.0f, 0.0f, -0.15f});
        setFloatVec3(mLine2Handle, new float[] {0.0f, 0.0f, -0.15f});
        setFloat(mIntensityHandle, 1.0f);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mBlurImageHandle, mBlurTexture, 1);
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setFloat(mWidthHandle, width);
        setFloat(mHeightHandle, height);
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.onInputSizeChanged((int) (width * mBlurScale), (int) (height * mBlurScale));
        }
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.onDisplaySizeChanged(width, height);
        }
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mGaussianBlurFilter != null) {
            mBlurTexture = mGaussianBlurFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        }
        return super.drawFrame(textureId, vertexBuffer, textureBuffer);
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mGaussianBlurFilter != null) {
            mBlurTexture = mGaussianBlurFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        }
        return super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
    }

    @Override
    public void initFrameBuffer(int width, int height) {
        super.initFrameBuffer(width, height);
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.initFrameBuffer((int)(width * mBlurScale), (int)(height * mBlurScale));
        }
    }

    @Override
    public void destroyFrameBuffer() {
        super.destroyFrameBuffer();
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.destroyFrameBuffer();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.release();
            mGaussianBlurFilter = null;
        }
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, new int[]{mBlurTexture}, 0);
        }
    }
}
