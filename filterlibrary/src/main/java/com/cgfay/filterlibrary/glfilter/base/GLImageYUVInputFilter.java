package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * 渲染YUV/BGRA输入滤镜
 */
public class GLImageYUVInputFilter extends GLImageFilter {

    private int mRenderYUVHandle;
    private int mInputTexture2Handle;
    private int mInputTexture3Handle;

    // 纹理id, 当渲染YUV时，mInputTexture 为Y纹理，mInputTexture2为U纹理，mInputTexture3为V纹理
    // 当渲染BGRA时，只有mInputTexture有效
    private int[] mInputTexture = new int[3];
    // 是否渲染YUV。默认渲染YUV
    private int mRenderYUV;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // 存放YUV数据
    private Buffer yBuffer;
    private Buffer uBuffer;
    private Buffer vBuffer;

    private int yLinesize;
    private int uLinesize;
    private int vLinesize;

    public GLImageYUVInputFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/base/fragment_yuv_input.glsl"));
    }

    public GLImageYUVInputFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mRenderYUV = 1;
        mVertexBuffer = OpenGLUtils.createFloatBuffer(new float[] {
                -1.0f, -1.0f,
                1.0f,  -1.0f,
                -1.0f,  1.0f,
                1.0f,   1.0f,
        });
        mTextureBuffer = OpenGLUtils.createFloatBuffer(new float[] {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        });
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mRenderYUVHandle = GLES30.glGetUniformLocation(mProgramHandle, "renderYUV");
            mInputTexture2Handle = GLES30.glGetUniformLocation(mProgramHandle, "inputTexture2");
            mInputTexture3Handle = GLES30.glGetUniformLocation(mProgramHandle, "inputTexture3");
        }
        GLES30.glGenTextures(3, mInputTexture, 0);
        for (int i = 0; i < 3; i++) {
            // 绑定纹理空间
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[i]);
            //设置属性 当显示的纹理比加载的纹理大时 使用纹理坐标中最接近的若干个颜色 通过加权算法获得绘制颜色
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            // 比加载的小
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            // 如果纹理坐标超出范围 0,0-1,1 坐标会被截断在范围内
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        }
    }

    /**
     * 设置YUV 数据
     * @param ydata
     * @param udata
     * @param vdata
     * @param yLinesize
     * @param uLinesize
     * @param vLinesize
     */
    public void setYUVData(byte[] ydata, byte[] udata, byte[] vdata,
                           int yLinesize, int uLinesize, int vLinesize) {
        mRenderYUV = 1;
        yBuffer = ByteBuffer.wrap(ydata);
        uBuffer = ByteBuffer.wrap(udata);
        vBuffer = ByteBuffer.wrap(vdata);
        this.yLinesize = yLinesize;
        this.uLinesize = uLinesize;
        this.vLinesize = vLinesize;
        cropTexVertices(yLinesize);
    }

    /**
     * 渲染BGRA数据
     * @param data
     * @param linesize
     */
    public void setBGRAData(byte[] data, int linesize) {
        mRenderYUV = 0;
        yBuffer = ByteBuffer.wrap(data);
        this.yLinesize = linesize / 4;
        cropTexVertices(yLinesize);
    }

    /**
     * 裁剪掉多余的像素
     * @param linesize
     */
    private void cropTexVertices(int linesize) {
        if (mImageWidth != 0 && mImageWidth != linesize) {
            float normalized = (Math.abs(mImageWidth - linesize) + 0.5f) / linesize;
            mTextureBuffer.clear();
            mTextureBuffer.put(new float[] {
                    0.0f, 1.0f,
                    1.0f - normalized, 1.0f,
                    0.0f, 0.0f,
                    1.0f - normalized, 0.0f,
            });
        }
    }

    /**
     * 更新YUV纹理数据
     */
    private void updateYUV() {
        mRenderYUV = 1;
        if (yBuffer != null && uBuffer != null && vBuffer != null) {
            // 绑定Y纹理
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[0]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE,
                    yLinesize, mImageHeight, 0, GLES30.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yBuffer);
            GLES30.glUniform1i(mInputTextureHandle, 0);

            // 绑定U纹理
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[1]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE,
                    uLinesize, mImageHeight, 0, GLES30.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, uBuffer);
            GLES30.glUniform1i(mInputTextureHandle, 1);

            // 绑定V纹理
            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[2]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE,
                    vLinesize, mImageHeight, 0, GLES30.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, vBuffer);
            GLES30.glUniform1i(mInputTextureHandle, 2);
        }
        if (yBuffer != null) {
            yBuffer.clear();
            yBuffer = null;
        }
        if (uBuffer != null) {
            uBuffer.clear();
            uBuffer = null;
        }
        if (vBuffer != null) {
            vBuffer.clear();
            vBuffer = null;
        }
    }

    /**
     * 更新BGRA纹理数据
     */
    private void updateBGRA() {
        mRenderYUV = 0;
        if (yBuffer != null) {
            // 绑定Y纹理
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[0]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                    yLinesize, mImageHeight, 0, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, yBuffer);
            GLES30.glUniform1i(mInputTextureHandle, 0);
        }

        if (yBuffer != null) {
            yBuffer.clear();
            yBuffer = null;
        }
        if (uBuffer != null) {
            uBuffer.clear();
            uBuffer = null;
        }
        if (vBuffer != null) {
            vBuffer.clear();
            vBuffer = null;
        }
    }

    @Override
    protected void onDrawTexture(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {

        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        // 绑定顶点坐标缓冲
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, mCoordsPerVertex,
                GLES30.GL_FLOAT, false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        // 绑定纹理坐标缓冲
        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle);
        // 绑定纹理
        if (mRenderYUV == 1) {
            updateYUV();
        } else {
            updateBGRA();
        }
        GLES30.glUniform1i(mRenderYUVHandle, mRenderYUV);
        onDrawFrameBegin();
        onDrawFrame();
        onDrawFrameAfter();
        // 解绑
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES30.glBindTexture(getTextureType(), 0);

        GLES30.glUseProgram(0);
    }

    /**
     * 直接绘制输出,这里不能用外部的坐标缓冲区，因为输入的宽高和linesize不一样，需要做裁剪处理。
     * @return
     */
    public boolean drawFrame() {
        return drawFrame(mInputTexture[0], mVertexBuffer, mTextureBuffer);
    }

    /**
     * 绘制到FBO中，这里不能用外部的坐标缓冲区，因为输入的宽高和linesize不一样，需要做裁剪处理。
     * @return
     */
    public int drawFrameBuffer() {
        return drawFrameBuffer(mInputTexture[0], mVertexBuffer, mTextureBuffer);
    }

}
