package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.ShortBuffer;

/**
 * 使用glDrawElements绘制的基类滤镜
 */
public class GLImageDrawElementsFilter extends GLImageFilter {

    // 索引大小
    protected int mIndexSize;
    // 索引缓冲
    protected ShortBuffer mIndexBuffer;

    public GLImageDrawElementsFilter(Context context) {
        super(context);
    }

    public GLImageDrawElementsFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    protected void initBuffers() {
        mVertexBuffer = OpenGLUtils.createFloatBuffer(Vertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(Textures);
        mIndexBuffer = OpenGLUtils.createShortBuffer(Indices);
        mIndexSize = mIndexBuffer.capacity();
    }

    @Override
    protected void releaseBuffers() {
        super.releaseBuffers();
        if (mIndexBuffer != null) {
            mIndexBuffer.clear();
            mIndexBuffer = null;
        }
    }

    @Override
    protected void onDrawFrame() {
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndexSize, GLES30.GL_UNSIGNED_SHORT, mIndexBuffer);
    }

    /**
     * 顶点坐标
     */
    private static final float[] Vertices = {
            -1.0f, -1.0f,   // left-bottom
            1.0f, -1.0f,    // right-bottom
            -1.0f, 1.0f,    // left-top
            1.0f, 1.0f,     // right-top
    };

    /**
     * 纹理坐标
     */
    private static final float[] Textures =  {
            0.0f, 0.0f, // left-bottom
            1.0f, 0.0f, // right-bottom
            0.0f, 1.0f, // left-top
            1.0f, 1.0f, // right-top
    };

    /**
     * 索引
     */
    private static final short[] Indices = {
            0, 1, 2,
            2, 1, 3,
    };
}