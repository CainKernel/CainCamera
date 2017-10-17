package com.cgfay.caincamera.filter.sticker;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.utils.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 贴纸滤镜
 * Created by cain.huang on 2017/8/4.
 */

public class StickerFilter extends BaseImageFilter {

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;        // 变换矩阵\n" +
                    "attribute vec4 aPosition;       // 位置坐标\n" +
                    "attribute vec2 aTextureCoord;   // 原始纹理坐标\n" +
                    "attribute vec2 aBitmapCoord;    // 贴图坐标\n" +
                    "\n" +
                    "varying vec2 textureCoordinate; // 输出texture坐标\n" +
                    "varying vec2 mipmapCoordinate;  // 输出mipmap坐标\n" +
                    "\n" +
                    "void main() {\n" +
                    "    // texture的坐标\n" +
                    "    textureCoordinate = aTextureCoord;\n" +
                    "    mipmapCoordinate = aBitmapCoord;\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 textureCoordinate;    // texture坐标\n" +
                    "varying vec2 mipmapCoordinate;     // mipmap坐标\n" +
                    "uniform vec4 color;\n" +
                    "uniform sampler2D inputTexture;    // 原始Texture\n" +
                    "uniform sampler2D mipmapTexture;   // 贴图Texture\n" +
                    "\n" +
                    "uniform vec2 v_mid; // 旋转中心点\n" +
                    "uniform vec3 v_Rotation; // 旋转角度\n" +
                    "uniform vec2 v_scale; //缩放\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    " vec2 rotated = vec2(cos(v_Rotation[2])*(mipmapCoordinate.x - v_mid.x)*v_scale.x\n" +
                    "                      + sin(v_Rotation[2])*(mipmapCoordinate.y - v_mid.y)*v_scale.y + v_mid.x,\n" +
                    "                    cos(v_Rotation[2])*(mipmapCoordinate.y - v_mid.y)*v_scale.y\n" +
                    "                    - sin(v_Rotation[2])*(mipmapCoordinate.x - v_mid.x)*v_scale.x + v_mid.y);\n" +
                    "    lowp vec4 sourceColor = texture2D(inputTexture, 1.0 - textureCoordinate);\n" +
                    "    lowp vec4 mipmapColor = texture2D(mipmapTexture, rotated);\n" +
                    "    vec4 resultColor;\n" +
                    "    resultColor[3] = sourceColor[3];\n" +
                    "\n" +
                    "    if( sourceColor[3] > 0.0 ) {\n" +
                    "        resultColor[3] = color[3] * sourceColor[3];\n" +
                    "        resultColor[0] = (pow(mipmapColor[0], 5.0) * color[3] + sourceColor[0] * (1.0 - color[3])) * sourceColor[3];\n" +
                    "        resultColor[1] = (pow(mipmapColor[1], 5.0) * color[3] + sourceColor[1] * (1.0 - color[3])) * sourceColor[3];\n" +
                    "        resultColor[2] = (pow(mipmapColor[2], 5.0) * color[3] + sourceColor[2] * (1.0 - color[3])) * sourceColor[3];\n" +
                    "    }\n" +
                    "    gl_FragColor = resultColor;\n" +
                    "}";

    private float[] OrgVertices = new float[] {
            -1.0f, -1.0f, 0.0f, // x ,y, z
            1.0f, -1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f,
            1.0f,  1.0f, 0.0f
    };

    private float[] TrackVertices = new float[] {
            -1.0f, -1.0f, 0.0f, // x ,y, z
            1.0f, -1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f,
            1.0f,  1.0f, 0.0f
    };

    private float[] TextureVertices = new float[] {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    private short mIndices[] = {0, 1, 2, 0, 2, 3};
    private int mIndicesLength;

    private ShortBuffer mIndicesBuffers;

    private FloatBuffer mVertexBuffer;

    private int mBitmapTextureLoc;
    private int mColorLoc;
    private int maBitmapCoordLoc;
    private int mRotationLoc;
    private int mMiddleLoc;
    private int mScaleLoc;

    private FloatBuffer mTextureBuffer;

    private float[] ColorValues = { 1.0f, 1.0f, 1.0f, 0.5f};

    private Bitmap mBitmap;
    private int[] mTextures = new int[1];

    public StickerFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public StickerFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        maBitmapCoordLoc = GLES30.glGetAttribLocation(mProgramHandle, "aBitmapCoord");
        mBitmapTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "mipmapTexture");
        mColorLoc = GLES30.glGetUniformLocation(mProgramHandle, "color");
        mRotationLoc = GLES30.glGetUniformLocation(mProgramHandle, "v_Rotation");
        mMiddleLoc = GLES30.glGetUniformLocation(mProgramHandle, "v_mid");
        mScaleLoc = GLES30.glGetUniformLocation(mProgramHandle, "v_scale");
        // 视图矩阵
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -1, 0f, 0f, 0f, 0f, 1f, 0f);
        // 计算变换矩阵，将图像翻转
        Matrix.rotateM(mMVPMatrix, 0, 180, 0, 0, 1);
        setTrackScale(0.5f);
        setTextures(TextureVertices);

        setFloatVec2(mMiddleLoc, new float[]{ 0.5f, 0.5f});
        setFloatVec2(mScaleLoc, new float[]{1.0f, 1.0f});
        setFloatVec3(mRotationLoc, new float[]{0, 60, 45});
    }

    /**
     * 初始化索引
     * @param mIndices
     */
    private void setIndices(short[] mIndices) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mIndices.length * 2);
        byteBuffer.order(ByteOrder.nativeOrder());
        mIndicesBuffers = byteBuffer.asShortBuffer();
        mIndicesBuffers.put(mIndices);
        mIndicesBuffers.position(0);
        mIndicesLength = mIndices.length;
    }

    /**
     * 设置顶点
     * @param vertices
     */
    private void setVertices(float[] vertices) {
        ByteBuffer mByteBuffers = ByteBuffer.allocateDirect(vertices.length * 4);
        mByteBuffers.order(ByteOrder.nativeOrder());
        mVertexBuffer = mByteBuffers.asFloatBuffer();
        mVertexBuffer.position(0);
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        float aspect = (float) width / height; // 计算宽高比
//        aspect = 1; // 如果是1的话，这里是不会发生变形之类的，强制变成宽高比的话，会将画面拉伸变形为正方形
//        Matrix.frustumM(mProjectionMatrix, 0, -aspect, aspect, -1, 1, 1, 10);
        Matrix.perspectiveM(mProjectionMatrix, 0, 60, aspect, 2, 10);
    }

    @Override
    public void drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        super.drawFrame(textureId, vertexBuffer, textureBuffer);
        if (textureId == GlUtil.GL_NOT_INIT) {
            return;
        }
        // 绘制贴纸
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glUseProgram(mProgramHandle);
//        calculateMVPMatrix();
        GLES30.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        runPendingOnDrawTasks();
        // 顶点坐标
        GLES30.glEnableVertexAttribArray(maPositionLoc);
        GLES30.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES30.GL_FLOAT, false, mVertexStride, vertexBuffer);
        // texture坐标
        GLES30.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES30.glVertexAttribPointer(maTextureCoordLoc, CoordsPerTexture,
                GLES30.GL_FLOAT, false, mTexCoordStride, textureBuffer);
        // Bitmap坐标
        GLES30.glEnableVertexAttribArray(maBitmapCoordLoc);
        GLES30.glVertexAttribPointer(maBitmapCoordLoc, CoordsPerTexture,
                GLES30.GL_FLOAT, false, mTexCoordStride, mTextureBuffer);
        // 绑定纹理0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(getTextureType(), textureId);
        GLES30.glUniform1i(mInputTextureLoc, 0);
        // 绑定纹理1
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mTextures[0]);
        GLES30.glUniform1i(mBitmapTextureLoc, 1);

        GLES30.glUniform4fv(mColorLoc, 1, FloatBuffer.wrap(ColorValues));

        onDrawArraysBegin();
        // 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mVertexCount);
        onDrawArraysAfter();
        GLES30.glDisableVertexAttribArray(maPositionLoc);
        GLES30.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glBindTexture(getTextureType(), 0);
        GLES30.glUseProgram(0);
    }

    /**
     * 设置贴纸图片
     * @param bitmap
     */
    public void setStickerBitmap(Bitmap bitmap) {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mBitmap = bitmap;
        if (mTextures.length > 0 && mTextures[0] != 0) {
            GLES30.glDeleteTextures(1, mTextures, 0);
        }
        createBitmapTexture();
        mBitmap.recycle();
    }

    /**
     * 创建Texture
     */
    private void createBitmapTexture() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            if (mBitmap != null) {
                // 创建texture
                GLES30.glGenTextures(1, mTextures, 0);
                // 绑定texture
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextures[0]);
                // 设置参数
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_LINEAR);
                // 创建mip贴图
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0);
                // 创建完成之后解绑
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            }
        }
    }

    /**
     * 设置缩放
     * @param trackScale
     */
    public void setTrackScale(float trackScale) {
        for(int i = 0; i < 4; i++) {
            TrackVertices[i * 3] = OrgVertices[i * 3] * trackScale;
            TrackVertices[i * 3 + 1] = OrgVertices[i * 3 + 1] * trackScale;
            TrackVertices[i * 3 + 2] = OrgVertices[i * 3 + 2] * trackScale;
        }
        setVertices(TrackVertices);
    }

    private void setTextures(float[] textureVertices) {
        ByteBuffer mByteBuffers = ByteBuffer.allocateDirect(textureVertices.length * 4);
        mByteBuffers.order(ByteOrder.nativeOrder());
        mTextureBuffer = mByteBuffers.asFloatBuffer();
        mTextureBuffer.position(0);
        mTextureBuffer.put(textureVertices);
        mTextureBuffer.position(0);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(1, mTextures, 0);
    }
}
