package com.cgfay.filterlibrary.glfilter.multiframe;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageGaussianBlurFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * 边框模糊滤镜，跟FaceU的边框滤镜效果一致
 */
public class GLImageFrameBlurFilter extends GLImageFilter {

    private int mBlurTextureHandle;
    private int mBlurOffsetXHandle;
    private int mBlurOffsetYHandle;
    private float blurOffsetX;
    private float blurOffsetY;

    // 高斯模糊滤镜
    private GLImageGaussianBlurFilter mGaussianBlurFilter;
    // 高斯模糊图像缩放半径
    private float mBlurScale = 0.5f;
    private int mBlurTexture;

    public GLImageFrameBlurFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/multiframe/fragment_frame_blur.glsl"));
    }

    public GLImageFrameBlurFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mGaussianBlurFilter = new GLImageGaussianBlurFilter(mContext);
        mBlurTexture = OpenGLUtils.GL_NOT_TEXTURE;
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mBlurTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture");
            mBlurOffsetXHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurOffsetX");
            mBlurOffsetYHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurOffsetY");
            setBlurOffset(0.15f, 0.15f);
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mBlurTextureHandle, mBlurTexture, 1);
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.onInputSizeChanged((int) (width * mBlurScale), (int) (height * mBlurScale));
            mGaussianBlurFilter.initFrameBuffer((int)(width * mBlurScale), (int)(height * mBlurScale));
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

    /**
     * 模糊的偏移值
     * @param offsetX 偏移值 0.0 ~ 1.0f
     * @param offsetY 偏移值 0.0 ~ 1.0f
     */
    public void setBlurOffset(float offsetX, float offsetY) {
        if (offsetX < 0.0f) {
            offsetX = 0.0f;
        } else if (offsetX > 1.0f) {
            offsetX = 1.0f;
        }
        this.blurOffsetX = offsetX;
        if (offsetY < 0.0f) {
            offsetY = 0.0f;
        } else if (offsetY > 1.0f) {
            offsetY = 1.0f;
        }
        this.blurOffsetY = offsetY;

        setFloat(mBlurOffsetXHandle, blurOffsetX);
        setFloat(mBlurOffsetYHandle, blurOffsetY);
    }

}
