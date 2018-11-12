package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;

import java.nio.ShortBuffer;

/**
 * 使用glDrawElements绘制图像
 */
public class GLImageDrawElementsFilter extends GLImageFilter {

    protected ShortBuffer mIndexBuffer;

    public GLImageDrawElementsFilter(Context context) {
        super(context);
        releaseBuffers();
        mIndexBuffer = OpenGLUtils.createShortBuffer(TextureRotationUtils.Indices);
    }

    /**
     * 释放纹理缓冲
     */
    protected void releaseBuffers() {
        if (mIndexBuffer != null) {
            mIndexBuffer.clear();
            mIndexBuffer = null;
        }
    }

    @Override
    protected void onDrawFrame() {
        // 如果不存在索引缓冲，则直接用glDrawArrays绘制
        if (mIndexBuffer != null) {
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndexBuffer.capacity(), GLES30.GL_UNSIGNED_SHORT, mIndexBuffer);
        } else {
            super.onDrawFrame();
        }
    }

    @Override
    public void release() {
        super.release();
        releaseBuffers();
    }
}