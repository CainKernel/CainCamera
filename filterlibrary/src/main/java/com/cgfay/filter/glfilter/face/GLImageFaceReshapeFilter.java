package com.cgfay.filter.glfilter.face;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.cgfay.filter.glfilter.base.GLImageDrawElementsFilter;
import com.cgfay.filter.glfilter.beauty.bean.BeautyParam;
import com.cgfay.filter.glfilter.beauty.bean.IBeautify;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.filter.glfilter.utils.TextureRotationUtils;
import com.cgfay.landmark.LandmarkEngine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * 脸型调整变换
 */
public class GLImageFaceReshapeFilter extends GLImageDrawElementsFilter implements IBeautify {

    // 122个关键点
    private final int IndicesLength = 122 * 2;
    // 106 个关键点
    private final int FacePoints = 106;
    // 顶点坐标
    private float[] mVertices = new float[122 * 2];
    // 纹理坐标
    private float[] mTextureVertices = new float[122 * 2];
    // 笛卡尔坐标系
    private float[] mCartesianVertices = new float[106 * 2];
    // 脸型程度
    private float[] mReshapeIntensity = new float[12];

    // 顶点坐标缓冲
    private FloatBuffer mVertexBuffer;
    // 纹理坐标缓冲
    private FloatBuffer mTextureBuffer;
    // 笛卡尔坐标缓冲
    private FloatBuffer mCartesianBuffer;

    private int mCartesianPointsHandle;
    private int mReshapeIntensityHandle;
    private int mTextureWidthHandle;
    private int mTextureHeightHandle;
    private int mEnableReshapeHandle;

    public GLImageFaceReshapeFilter(Context context) {
        super(context, OpenGLUtils.getShaderFromAssets(context, "shader/face/vertex_face_reshape.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/face/fragment_face_reshape.glsl"));
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mCartesianPointsHandle = GLES30.glGetUniformLocation(mProgramHandle, "cartesianPoints");
            mReshapeIntensityHandle = GLES30.glGetUniformLocation(mProgramHandle, "reshapeIntensity");
            mTextureWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "textureWidth");
            mTextureHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "textureHeight");
            mEnableReshapeHandle = GLES30.glGetUniformLocation(mProgramHandle, "enableReshape");
        }
    }

    @Override
    protected void initBuffers() {
        mVertexBuffer = ByteBuffer.allocateDirect(IndicesLength * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer = ByteBuffer.allocateDirect(IndicesLength * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCartesianBuffer = ByteBuffer.allocateDirect(FacePoints * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mIndexBuffer = OpenGLUtils.createShortBuffer(FaceImageIndices);
    }

    @Override
    protected void releaseBuffers() {
        super.releaseBuffers();
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setInteger(mTextureWidthHandle, width);
        setInteger(mTextureHeightHandle, height);
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        updateFaceVertices();
        if (LandmarkEngine.getInstance().hasFace()) {
            return super.drawFrame(textureId, mVertexBuffer, mTextureBuffer);
        } else {
            return super.drawFrame(textureId, vertexBuffer, textureBuffer);
        }
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        updateFaceVertices();
        if (LandmarkEngine.getInstance().hasFace()) {
            return super.drawFrameBuffer(textureId, mVertexBuffer, mTextureBuffer);
        } else {
            return super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        // 不能开启GL_CULL_FACE，会导致索引绘制出错
        GLES30.glDisable(GL10.GL_CULL_FACE);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES20.glUniform1fv(mReshapeIntensityHandle, 7, FloatBuffer.wrap(mReshapeIntensity));
        GLES30.glUniform2fv(mCartesianPointsHandle, FacePoints, mCartesianBuffer);
    }

    /**
     * 更新顶点
     */
    private void updateFaceVertices() {
        if (LandmarkEngine.getInstance().hasFace()) {
            LandmarkEngine.getInstance().updateFaceAdjustPoints(mVertices, mTextureVertices, 0);
            mVertexBuffer.clear();
            mVertexBuffer.put(mVertices);
            mVertexBuffer.position(0);

            mTextureBuffer.clear();
            mTextureBuffer.put(mTextureVertices);
            mTextureBuffer.position(0);

            updateCartesianVertices();

            mIndexBuffer.clear();
            mIndexBuffer.put(FaceImageIndices);
            mIndexBuffer.position(0);
            mIndexLength = mIndexBuffer.capacity();

            setInteger(mEnableReshapeHandle, 1);
        } else { // 没有人脸时索引变回默认的6个
            mIndexBuffer.clear();
            mIndexBuffer.put(TextureRotationUtils.Indices);
            mIndexBuffer.position(0);
            mIndexLength = 6;
            setInteger(mEnableReshapeHandle, 0);
        }
    }

    /**
     * 更新笛卡尔坐标系
     */
    private void updateCartesianVertices() {
        for (int i = 0; i < FacePoints; i++) {
            mCartesianVertices[i * 2] = mTextureVertices[i * 2] * mImageWidth;
            mCartesianVertices[i * 2 + 1] = mTextureVertices[i * 2 + 1] * mImageHeight;
        }
        mCartesianBuffer.clear();
        mCartesianBuffer.put(mCartesianVertices);
        mCartesianBuffer.position(0);
    }


    @Override
    public void onBeauty(BeautyParam beauty) {
        if (beauty == null) {
            return;
        }
        mReshapeIntensity[0]  = beauty.faceLift;                // 瘦脸
        mReshapeIntensity[1]  = beauty.faceShave;               // 削脸
        mReshapeIntensity[2]  = beauty.faceNarrow;              // 小脸
        mReshapeIntensity[3]  = beauty.chinIntensity;           // 下巴
        mReshapeIntensity[4]  = beauty.foreheadIntensity;       // 额头
        mReshapeIntensity[5]  = beauty.eyeEnlargeIntensity;     // 大眼
        mReshapeIntensity[6]  = beauty.eyeDistanceIntensity;    // 眼距
        mReshapeIntensity[7]  = beauty.eyeCornerIntensity;      // 眼角
        mReshapeIntensity[8]  = beauty.noseThinIntensity;       // 瘦鼻
        mReshapeIntensity[9]  = beauty.alaeIntensity;           // 鼻翼
        mReshapeIntensity[10] = beauty.proboscisIntensity;      // 长鼻
        mReshapeIntensity[11] = beauty.mouthEnlargeIntensity;   // 嘴型
    }

    /**
     * 人脸图像索引(702个点)（122个关键点）
     * 具体的关键点可参考 landmarklibrary的assets目录下的三角剖.jpg
     */
    private static final short[] FaceImageIndices = {
            // 脸外索引(人脸顶部中心逆时针数)
            110, 114, 111,
            111, 114, 115,
            115, 111, 32,
            32, 115, 116,
            116, 32, 31,
            31, 116, 30,
            30, 116, 29,
            29, 116, 28,
            28, 116, 27,
            27, 116, 26,
            26, 116, 25,
            25, 116, 117,
            117, 25, 24,
            24, 117, 23,
            23, 117, 22,
            22, 117, 21,
            21, 117, 20,
            20, 117, 19,
            19, 117, 118,
            118, 19, 18,
            18, 118, 17,
            17, 118, 16,
            16, 118, 15,
            15, 118, 14,
            14, 118, 13,
            13, 118, 119,
            119, 13, 12,
            12, 119, 11,
            11, 119, 10,
            10, 119, 9,
            9, 119, 8,
            8, 119, 7,
            7, 119, 120,
            120, 7, 6,
            6, 120, 5,
            5, 120, 4,
            4, 120, 3,
            3, 120, 2,
            2, 120, 1,
            1, 120, 0,
            0, 120, 121,
            121, 0, 109,
            109, 121, 114,
            114, 109, 110,
            // 脸内部索引
            // 额头
            0, 33, 109,
            109, 33, 34,
            34, 109, 35,
            35, 109, 36,
            36, 109, 110,
            36, 110, 37,
            37, 110, 43,
            43, 110, 38,
            38, 110, 39,
            39, 110, 111,
            111, 39, 40,
            40, 111, 41,
            41, 111, 42,
            42, 111, 32,
            // 左眉毛
            33, 34, 64,
            64, 34, 65,
            65, 34, 107,
            107, 34, 35,
            35, 36, 107,
            107, 36, 66,
            66, 107, 65,
            66, 36, 67,
            67, 36, 37,
            37, 67, 43,
            // 右眉毛
            43, 38, 68,
            68, 38, 39,
            39, 68, 69,
            39, 40, 108,
            39, 108, 69,
            69, 108, 70,
            70, 108, 41,
            41, 108, 40,
            41, 70, 71,
            71, 41, 42,
            // 左眼
            0, 33, 52,
            33, 52, 64,
            52, 64, 53,
            64, 53, 65,
            65, 53, 72,
            65, 72, 66,
            66, 72, 54,
            66, 54, 67,
            54, 67, 55,
            67, 55, 78,
            67, 78, 43,
            52, 53, 57,
            53, 72, 74,
            53, 74, 57,
            74, 57, 73,
            72, 54, 104,
            72, 104, 74,
            74, 104, 73,
            73, 104, 56,
            104, 56, 54,
            54, 56, 55,
            // 右眼
            68, 43, 79,
            68, 79, 58,
            68, 58, 59,
            68, 59, 69,
            69, 59, 75,
            69, 75, 70,
            70, 75, 60,
            70, 60, 71,
            71, 60, 61,
            71, 61, 42,
            42, 61, 32,
            61, 60, 62,
            60, 75, 77,
            60, 77, 62,
            77, 62, 76,
            75, 77, 105,
            77, 105, 76,
            105, 76, 63,
            105, 63, 59,
            105, 59, 75,
            59, 63, 58,
            // 左脸颊
            0, 52, 1,
            1, 52, 2,
            2, 52, 57,
            2, 57, 3,
            3, 57, 4,
            4, 57, 112,
            57, 112, 74,
            74, 112, 56,
            56, 112, 80,
            80, 112, 82,
            82, 112, 7,
            7, 112, 6,
            6, 112, 5,
            5, 112, 4,
            56, 80, 55,
            55, 80, 78,
            // 右脸颊
            32, 61, 31,
            31, 61, 30,
            30, 61, 62,
            30, 62, 29,
            29, 62, 28,
            28, 62, 113,
            62, 113, 76,
            76, 113, 63,
            63, 113, 81,
            81, 113, 83,
            83, 113, 25,
            25, 113, 26,
            26, 113, 27,
            27, 113, 28,
            63, 81, 58,
            58, 81, 79,
            // 鼻子部分
            78, 43, 44,
            43, 44, 79,
            78, 44, 80,
            79, 81, 44,
            80, 44, 45,
            44, 81, 45,
            80, 45, 46,
            45, 81, 46,
            80, 46, 82,
            81, 46, 83,
            82, 46, 47,
            47, 46, 48,
            48, 46, 49,
            49, 46, 50,
            50, 46, 51,
            51, 46, 83,
            // 鼻子和嘴巴中间三角形
            7, 82, 84,
            82, 84, 47,
            84, 47, 85,
            85, 47, 48,
            48, 85, 86,
            86, 48, 49,
            49, 86, 87,
            49, 87, 88,
            88, 49, 50,
            88, 50, 89,
            89, 50, 51,
            89, 51, 90,
            51, 90, 83,
            83, 90, 25,
            // 上嘴唇部分
            84, 85, 96,
            96, 85, 97,
            97, 85, 86,
            86, 97, 98,
            86, 98, 87,
            87, 98, 88,
            88, 98, 99,
            88, 99, 89,
            89, 99, 100,
            89, 100, 90,
            // 下嘴唇部分
            90, 100, 91,
            100, 91, 101,
            101, 91, 92,
            101, 92, 102,
            102, 92, 93,
            102, 93, 94,
            102, 94, 103,
            103, 94, 95,
            103, 95, 96,
            96, 95, 84,
            // 唇间部分
            96, 97, 103,
            97, 103, 106,
            97, 106, 98,
            106, 103, 102,
            106, 102, 101,
            106, 101, 99,
            106, 98, 99,
            99, 101, 100,
            // 嘴巴与下巴之间的部分(关键点7 到25 与嘴巴鼻翼围起来的区域)
            7, 84, 8,
            8, 84, 9,
            9, 84, 10,
            10, 84, 95,
            10, 95, 11,
            11, 95, 12,
            12, 95, 94,
            12, 94, 13,
            13, 94, 14,
            14, 94, 93,
            14, 93, 15,
            15, 93, 16,
            16, 93, 17,
            17, 93, 18,
            18, 93, 92,
            18, 92, 19,
            19, 92, 20,
            20, 92, 91,
            20, 91, 21,
            21, 91, 22,
            22, 91, 90,
            22, 90, 23,
            23, 90, 24,
            24, 90, 25
    };

}
