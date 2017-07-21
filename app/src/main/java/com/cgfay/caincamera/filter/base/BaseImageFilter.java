package com.cgfay.caincamera.filter.base;

import android.graphics.PointF;
import android.opengl.GLES20;

import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.utils.GlUtil;

import java.nio.FloatBuffer;
import java.util.LinkedList;

/**
 * 基类滤镜
 * Created by cain on 2017/7/9.
 */

public class BaseImageFilter implements IFilter {

    static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;                                   \n" +
            "uniform mat4 uTexMatrix;                                   \n" +
            "attribute vec4 aPosition;                                  \n" +
            "attribute vec4 aTextureCoord;                              \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "void main() {                                              \n" +
            "    gl_Position = uMVPMatrix * aPosition;                  \n" +
            "    textureCoordinate = (uTexMatrix * aTextureCoord).xy;   \n" +
            "}                                                          \n";

    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;                                   \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "uniform sampler2D sTexture;                                \n" +
            "void main() {                                              \n" +
            "    gl_FragColor = texture2D(sTexture, textureCoordinate); \n" +
            "}                                                          \n";

    static final int SIZEOF_FLOAT = 4;
    private static final float SquareVertices[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
    };
    protected static final float TextureVertices[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    protected static final float TextureVertices_90[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };

    protected static final float TextureVertices_180[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };

    protected static final float TextureVertices_270[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f
    };

    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(SquareVertices);

    FloatBuffer mVertexArray = FULL_RECTANGLE_BUF;
    FloatBuffer mTexCoordArray = GlUtil.createFloatBuffer(TextureVertices);
    int mCoordsPerVertex = 2;
    int mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
    int mVertexCount = SquareVertices.length / mCoordsPerVertex;
    int mTexCoordStride = 2 * SIZEOF_FLOAT;

    int mProgramHandle;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
    int maPositionLoc;
    int maTextureCoordLoc;

    private int mTextureId = -1;

    private final LinkedList<Runnable> mRunOnDraw;

    public BaseImageFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER_2D);
    }

    public BaseImageFilter(String vertexShader, String fragmentShader) {
        mRunOnDraw = new LinkedList<>();
        mProgramHandle = GlUtil.createProgram(vertexShader, fragmentShader);
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
    }

    /**
     *
     * @param mvpMatrix     投影矩阵
     * @param vertexBuffer  顶点数据
     * @param firstVertex   第一个顶点索引
     * @param vertexCount   顶点数
     * @param coordsPerVertex 每个顶点由多少个值组成，比如（x,y,z）有3个值
     * @param vertexStride  每个顶点位置数据宽度，通常是vertexCount * sizeof(float)
     * @param texMatrix     纹理坐标变换矩阵，主要用于SurfaceTexture
     * @param texBuffer     顶点纹理数据缓冲区
     * @param textureId     纹理的id
     * @param texStride     每个顶点纹理数据宽度，例如(s,t)宽度是2 * sizeof(float)
     */
    @Override
    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        GLES20.glUseProgram(mProgramHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureType(), textureId);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        runPendingOnDrawTasks();
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);
        onDrawArraysBegin();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        onDrawArraysAfter();
        GLES20.glBindTexture(getTextureType(), 0);
        GLES20.glUseProgram(0);
        mTextureId = textureId;
    }

    /**
     * 直接绘制Texture
     * @param textureId
     * @param texMatrix SurfaceTexture的变换矩阵
     */
    @Override
    public void draw(int textureId, float[] texMatrix) {
        draw(GlUtil.IDENTITY_MATRIX, mVertexArray,
                0, mVertexCount,
                mCoordsPerVertex, mVertexStride,
                texMatrix, mTexCoordArray,
                textureId, mTexCoordStride);
    }

    /**
     * 绘制到FBO
     * @param framebuffer
     * @param textureId
     * @param texMatrix
     */
    @Override
    public void drawFramebuffer(int framebuffer, int textureId, float[] texMatrix) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);
        GLES20.glUseProgram(mProgramHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, GlUtil.IDENTITY_MATRIX, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        runPendingOnDrawTasks();
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES20.GL_FLOAT, false, mVertexStride, mVertexArray);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, mTexCoordStride, mTexCoordArray);
        onDrawArraysBegin();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        onDrawArraysAfter();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        mTextureId = textureId;
    }

    /**
     * 获取Texture类型
     * GLES20.TEXTURE_2D / GLES11Ext.GL_TEXTURE_EXTERNAL_OES等
     */
    @Override
    public int getTextureType() {
        return GLES20.GL_TEXTURE_2D;
    }

    @Override
    public int getOutputTexture() {
        return mTextureId;
    }

    /**
     * 调用drawArrays之前，方便添加其他属性
     */
    public void onDrawArraysBegin() {

    }

    /**
     * drawArrays调用之后，方便销毁其他属性
     */
    public void onDrawArraysAfter() {

    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
        mTextureId = -1;
    }

    public FloatBuffer getTexCoordArray() {
        FloatBuffer result = null;
        switch (CameraUtils.getPreviewOrientation()) {
            case 0:
                result = GlUtil.createFloatBuffer(TextureVertices);
                break;

            case 90:
                result = GlUtil.createFloatBuffer(TextureVertices_90);
                break;

            case 180:
                result = GlUtil.createFloatBuffer(TextureVertices_180);
                break;

            case 270:
                result = GlUtil.createFloatBuffer(TextureVertices_270);
                break;

            default:
                result = GlUtil.createFloatBuffer(TextureVertices);
        }
        return result;
    }

    ///------------------ 同一变量(uniform)设置 ------------------------///
    protected void setInteger(final int location, final int intValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location, intValue);
            }
        });
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatArray(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setPoint(final int location, final PointF point) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    protected void setUniformMatrix3f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void setUniformMatrix4f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }
}
