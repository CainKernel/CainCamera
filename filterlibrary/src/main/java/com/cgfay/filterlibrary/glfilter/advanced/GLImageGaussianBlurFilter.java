package com.cgfay.filterlibrary.glfilter.advanced;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * 高斯模糊滤镜
 */
public class GLImageGaussianBlurFilter extends GLImageFilter {

    protected GLImageGaussPassFilter mVerticalPassFilter;
    protected GLImageGaussPassFilter mHorizontalPassFilter;

    private int mCurrentTexture;

    public GLImageGaussianBlurFilter(Context context) {
        super(context, null, null);
        initFilters();
    }

    public GLImageGaussianBlurFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        initFilters(vertexShader, fragmentShader);
    }

    private void initFilters() {
        mVerticalPassFilter = new GLImageGaussPassFilter(mContext);
        mHorizontalPassFilter = new GLImageGaussPassFilter(mContext);
    }

    private void initFilters(String vertexShader, String fragmentShader) {
        mVerticalPassFilter = new GLImageGaussPassFilter(mContext, vertexShader, fragmentShader);
        mHorizontalPassFilter = new GLImageGaussPassFilter(mContext, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mVerticalPassFilter != null) {
            mVerticalPassFilter.initProgramHandle();
        }
        if (mHorizontalPassFilter != null) {
            mHorizontalPassFilter.initProgramHandle();
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (mVerticalPassFilter != null) {
            mVerticalPassFilter.onInputSizeChanged(width, height);
            mVerticalPassFilter.setTexelOffsetSize(0, height);
        }
        if (mHorizontalPassFilter != null) {
            mHorizontalPassFilter.onInputSizeChanged(width, height);
            mHorizontalPassFilter.setTexelOffsetSize(width, 0);
        }
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        if (mVerticalPassFilter != null) {
            mVerticalPassFilter.onDisplaySizeChanged(width, height);
        }
        if (mHorizontalPassFilter != null) {
            mHorizontalPassFilter.onDisplaySizeChanged(width, height);
        }
    }


    @Override
    public boolean drawFrame(int textureId) {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE) {
            return false;
        }

        mCurrentTexture = textureId;
        if (mVerticalPassFilter != null) {
            mCurrentTexture = mVerticalPassFilter.drawFrameBuffer(mCurrentTexture);
        }
        if (mHorizontalPassFilter != null) {
            return mHorizontalPassFilter.drawFrame(mCurrentTexture);
        }

        return false;
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE) {
            return false;
        }
        mCurrentTexture = textureId;
        if (mVerticalPassFilter != null) {
            mCurrentTexture = mVerticalPassFilter.drawFrameBuffer(mCurrentTexture, vertexBuffer, textureBuffer);
        }
        if (mHorizontalPassFilter != null) {
            return mHorizontalPassFilter.drawFrame(mCurrentTexture, vertexBuffer, textureBuffer);
        }
        return false;
    }

    @Override
    public int drawFrameBuffer(int textureId) {
        mCurrentTexture = textureId;
        if (mCurrentTexture == OpenGLUtils.GL_NOT_TEXTURE) {
            return mCurrentTexture;
        }
        if (mVerticalPassFilter != null) {
            mCurrentTexture = mVerticalPassFilter.drawFrameBuffer(mCurrentTexture);
        }
        if (mHorizontalPassFilter != null) {
            mCurrentTexture = mHorizontalPassFilter.drawFrameBuffer(mCurrentTexture);
        }
        return mCurrentTexture;
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        mCurrentTexture = textureId;
        if (mCurrentTexture == OpenGLUtils.GL_NOT_TEXTURE) {
            return mCurrentTexture;
        }

        if (mVerticalPassFilter != null) {
            mCurrentTexture = mVerticalPassFilter.drawFrameBuffer(mCurrentTexture, vertexBuffer, textureBuffer);
        }
        if (mHorizontalPassFilter != null) {
            mCurrentTexture = mHorizontalPassFilter.drawFrameBuffer(mCurrentTexture, vertexBuffer, textureBuffer);
        }
        return mCurrentTexture;
    }

    @Override
    public void initFrameBuffer(int width, int height) {
        super.initFrameBuffer(width, height);
        if (mVerticalPassFilter != null) {
            mVerticalPassFilter.initFrameBuffer(width, height);
        }
        if (mHorizontalPassFilter != null) {
            mHorizontalPassFilter.initFrameBuffer(width, height);
        }
    }

    @Override
    public void destroyFrameBuffer() {
        super.destroyFrameBuffer();
        if (mVerticalPassFilter != null) {
            mVerticalPassFilter.destroyFrameBuffer();
        }
        if (mHorizontalPassFilter != null) {
            mHorizontalPassFilter.destroyFrameBuffer();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mVerticalPassFilter != null) {
            mVerticalPassFilter.release();
            mVerticalPassFilter = null;
        }
        if (mHorizontalPassFilter != null) {
            mHorizontalPassFilter.release();
            mHorizontalPassFilter = null;
        }
    }

    /**
     * 设置模糊半径大小，默认为1.0f
     * @param blurSize
     */
    public void setBlurSize(float blurSize) {
        if (mVerticalPassFilter != null) {
            mVerticalPassFilter.setBlurSize(blurSize);
        }

        if (mHorizontalPassFilter != null) {
            mHorizontalPassFilter.setBlurSize(blurSize);
        }
    }
}
