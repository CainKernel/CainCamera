package com.cgfay.caincamera.filter.base;

import android.opengl.GLES30;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜组基类
 * Created by cain on 17-7-17.
 */
public class BaseImageFilterGroup implements IFilter {

    private int mTextureId = -1;

    private List<BaseImageFilter> mFilters = new ArrayList<BaseImageFilter>();

    public BaseImageFilterGroup() {

    }

    @Override
    public void draw(float[] mvpMatrix,
                     FloatBuffer vertexBuffer,
                     int firstVertex,
                     int vertexCount,
                     int coordsPerVertex,
                     int vertexStride,
                     float[] texMatrix,
                     FloatBuffer texBuffer,
                     int textureId,
                     int texStride) {
        if (mFilters != null && mFilters.size() > 0) {

        } else {
            mTextureId = textureId;
        }
    }

    @Override
    public void draw(int textureId, float[] texMatrix) {
        if (mFilters != null && mFilters.size() > 0) {
            
        } else {
            mTextureId = textureId;
        }
    }

    @Override
    public void drawFramebuffer(int framebuffer, int textureId, float[] texMatrix) {

    }

    @Override
    public int getTextureType() {
        return GLES30.GL_TEXTURE_2D;
    }

    @Override
    public int getOutputTexture() {
        return mTextureId;
    }

    @Override
    public void release() {
        if (mFilters != null) {
            for (BaseImageFilter filter : mFilters) {
                filter.release();
            }
            mFilters.clear();
        }
        mTextureId = -1;
    }
}
