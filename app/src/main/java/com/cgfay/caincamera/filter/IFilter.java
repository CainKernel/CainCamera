package com.cgfay.caincamera.filter;

import java.nio.FloatBuffer;

/**
 * Created by cain on 2017/7/9.
 */

public interface IFilter {

    // 直接绘制到Texture
    void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
              int vertexCount, int coordsPerVertex, int vertexStride,
              float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride);

    // 直接绘制到texture
    void draw(int textureId, float[] texMatrix);

    // 绘制到Framebuffer中
    void drawFramebuffer(int framebuffer, int textureId, float[] texMatrix);

    // Texture的类型，TEXTURE_2D 还是 TEXUTURE_EXTERNAL_OES等
    int getTextureType();

    // 绘制完成后输出的Texture
    int getOutputTexture();

    // 释放资源
    void release();

}
