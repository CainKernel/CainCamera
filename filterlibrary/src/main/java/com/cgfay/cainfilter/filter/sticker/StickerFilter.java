package com.cgfay.cainfilter.filter.sticker;

import android.graphics.Bitmap;
import android.opengl.GLES30;

import com.cgfay.cainfilter.filter.base.BaseImageFilter;
import com.cgfay.cainfilter.utils.GlUtil;
import com.cgfay.cainfilter.utils.TextureRotationUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * 贴纸滤镜，贴纸在经过透视变换、人脸在侧脸、抬头、低头等都会产生一个姿态角，
 * 根据姿态角做综合变换，我们可以得到贴纸在视锥体中的实际三维坐标。贴纸在经过三维坐标的变换后，如果需要跟
 * 原来的图像做混合处理，则需要自己手动计算透视变换，取得变换后投影到屏幕的实际UV坐标，然后在fragment shader
 * 里面做混合处理，比如我要变换各种颜色、色调等，则可以改变贴纸的颜色、色调、明亮程度等、然后再跟原图像进行混合
 * 这样同一个贴纸也能产生不同的颜色、色调效果
 * Created by cain.huang on 2017/11/24.
 */

public class StickerFilter extends BaseImageFilter {

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;        // 总变换矩阵\n" +
            "uniform mat4 uTexMatrix;        // 输入图像的缩放矩阵\n" +
            "attribute vec4 aPosition;       // 输入图像的位置坐标\n" +
            "attribute vec4 aTextureCoord;   // 输入图像纹理坐标\n" +
            "\n" +
            "attribute vec4 aMipmapCoord;    // 贴纸在视锥体空间中的垂直于z轴的假想坐标\n" +
            "\n" +
            "uniform int centerX;          // 贴纸处于屏幕的中心位置x\n" +
            "uniform int centerY;          // 贴纸处于屏幕的中心位置y\n" +
            "\n" +
            "varying vec2 textureCoordinate; // 输出texture坐标\n" +
            "varying vec2 mipmapCoordinate;  // 输出mipmap坐标\n" +
            "\n" +
            "/**\n" +
            " * 计算贴纸投影到屏幕的UV坐标\n" +
            " */\n" +
            "vec2 calculateUVPosition(vec4 modelPosition, mat4 mvpMatrix) {\n" +
            "    vec4 tmp = vec4(modelPosition);\n" +
            "    tmp = mvpMatrix * tmp; // gl_Position\n" +
            "    tmp /= tmp.w; // 经过这个步骤，tmp就是归一化标准坐标了.\n" +
            "    tmp = tmp * 0.5 + vec4(0.5f, 0.5f, 0.5f, 0.5f); // NDC坐标\n" +
            "    tmp += vec4(centerx, centerY, 0.5f, 0.5f);// 平移到贴纸中心\n" +
            "    return vec2(tmp.x, tmp.y); // 屏幕的UV坐标\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    // texture的坐标\n" +
            "    textureCoordinate = (uTexMatrix * aTextureCoord).xy;;\n" +
            "    // 变换矩阵\n" +
            "    mipmapCoordinate = calculateUVPosition(aMipmapCoord, uMVPMatrix);\n" +
            "    gl_Position = aPosition;\n" +
            "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;    // texture的uv坐标\n" +
            "varying vec2 mipmapCoordinate;     // mipmap经过透视变换到屏幕上的uv坐标\n" +
            "\n" +
            "uniform sampler2D inputTexture;    // 原始Texture\n" +
            "uniform sampler2D mipmapTexture;   // 贴图Texture\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    lowp vec4 sourceColor = texture2D(inputTexture, 1.0 - textureCoordinate);\n" +
            "    lowp vec4 mipmapColor = texture2D(mipmapTexture, mipmapCoordinate);\n" +
            "    // 混合处理，此时可以做各种混合处理，比如变换透明度，变换颜色等等\n" +
            "    gl_FragColor = mipmapColor * mipmapColor.a + sourceColor * (1.0 - mipmapColor.a);\n" +
            "}";

    private int mMipmapCoordLoc;

    // 贴纸在屏幕上的中心点
    private int mCenterXLoc;
    private int mCenterYLoc;

    private int mMipmapTextureLoc;

    // 贴纸的texture
    private int mStickerTexture;

    // 贴纸坐标缓冲
    private FloatBuffer mStickerVertexBuffer;

    // 贴纸中心点
    private int mCenterX = 0;
    private int mCenterY = 0;

    public StickerFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public StickerFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mMipmapCoordLoc = GLES30.glGetAttribLocation(mProgramHandle, "aMipmapCoord");
        mCenterXLoc = GLES30.glGetUniformLocation(mProgramHandle, "centerX");
        mCenterYLoc = GLES30.glGetUniformLocation(mProgramHandle, "centerY");
        mMipmapTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "mipmapTexture");
        // 默认全屏
        mStickerVertexBuffer = GlUtil.createFloatBuffer(TextureRotationUtils.CubeVertices);
    }

    @Override
    public void onDrawArraysBegin() {
        super.onDrawArraysBegin();
        // 设置中心点
        GLES30.glUniform1i(mCenterXLoc, mCenterX);
        GLES30.glUniform1i(mCenterYLoc, mCenterY);

        // 贴纸坐标定点绑定
        mStickerVertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mMipmapCoordLoc, 3,
                GLES30.GL_FLOAT, false, 0, mStickerVertexBuffer);
        GLES30.glEnableVertexAttribArray(mMipmapCoordLoc);
        // 计算总变换(贴纸部分)
        calculateMVPMatrix();
        // 将贴纸绑定到Texture1的位置
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mStickerTexture);
        GLES30.glUniform1i(mMipmapTextureLoc, 1);
    }

    @Override
    public void onDrawArraysAfter() {
        super.onDrawArraysAfter();
        GLES30.glDisableVertexAttribArray(mMipmapCoordLoc);
    }

    @Override
    public void release() {
        super.release();
        // 释放 texture资源，避免内存泄漏
        GLES30.glDeleteTextures(1, new int[]{ mStickerTexture }, 0);
    }

    @Override
    public int getTextureType() {
        return GLES30.GL_TEXTURE_2D;
    }

    /**
     * 设置贴纸顶点坐标
     * @param vertices
     */
    public void setStickerVertex(float[] vertices) {
        mStickerVertexBuffer.clear();
        mStickerVertexBuffer.put(vertices);
        mStickerVertexBuffer.position(0);
    }

    /**
     * 设置贴纸顶点坐标
     * @param vertexBuffer
     */
    public void setStickerVertex(FloatBuffer vertexBuffer) {
        mStickerVertexBuffer.clear();
        mStickerVertexBuffer.put(vertexBuffer);
        mStickerVertexBuffer.position(0);
    }

    /**
     * 设置贴纸Texture 切换成新的贴纸
     * @param texture
     */
    public void setStickerTexture(int texture) {
        GLES30.glDeleteTextures(1, new int[]{ mStickerTexture }, 0);
        // 绑定新贴纸
        mStickerTexture = texture;
    }

    /**
     * 更新贴纸Texture 用于实现贴纸动画
     * @param bitmap
     */
    public void updateTexture(Bitmap bitmap) {
        mStickerTexture = GlUtil.createTexctureWithOldTexture(mStickerTexture, bitmap);
    }

    /**
     * 更新贴纸Texture 用于实现贴纸动画
     * @param buffer
     * @param width
     * @param height
     */
    public void updateTexture(ByteBuffer buffer, int width, int height) {
        mStickerTexture = GlUtil.createTexctureWithOldTexture(mStickerTexture,
                buffer, width, height);
    }

    /**
     * 设置贴纸的中心点位置
     * @param x
     * @param y
     */
    public void setCenterPosition(int x, int y) {
        mCenterX = x;
        mCenterY = y;
    }

}
