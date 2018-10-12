package com.cgfay.filterlibrary.glfilter.advanced.face;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.text.TextUtils;
import android.util.SparseArray;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.landmarklibrary.LandmarkEngine;
import com.cgfay.landmarklibrary.OneFace;

import java.nio.FloatBuffer;

/**
 * 人脸关键点调试滤镜
 */
public class GLImageFacePointsFilter extends GLImageFilter {

    private static final String TAG = "FacePointDrawerFilter";

    private static final String VertexShader = "" +
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    gl_PointSize = 8.0;\n" +
            "}";

    private static final String FragmentShader = "" +
            "precision mediump float;\n" +
            "uniform vec4 color;\n" +
            "void main() {\n" +
            "    gl_FragColor = color;\n" +
            "}";

    // 关键点滤镜
    private final float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };

    private int mColorHandle;

    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];

    private int mPointCount = 106;
    private float[] mPoints;
    private FloatBuffer mPointVertexBuffer;

    public GLImageFacePointsFilter(Context context) {
        this(context, VertexShader, FragmentShader);
    }

    public GLImageFacePointsFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mPoints = new float[mPointCount * 2];
        mPointVertexBuffer = OpenGLUtils.createFloatBuffer(mPoints);
    }

    @Override
    public void initProgramHandle() {
        // 只有在shader都不为空的情况下才初始化程序句柄
        if (!TextUtils.isEmpty(mVertexShader) && !TextUtils.isEmpty(mFragmentShader)) {
            mProgramHandle = OpenGLUtils.createProgram(mVertexShader, mFragmentShader);
            mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
            mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "aPosition");
            mColorHandle = GLES30.glGetUniformLocation(mProgramHandle, "color");
            mIsInitialized = true;
        } else {
            mPositionHandle = OpenGLUtils.GL_NOT_INIT;
            mMVPMatrixHandle = OpenGLUtils.GL_NOT_INIT;
            mColorHandle = OpenGLUtils.GL_NOT_INIT;
            mIsInitialized = false;
        }
        mTextureCoordinateHandle = OpenGLUtils.GL_NOT_INIT;
        mInputTextureHandle = OpenGLUtils.GL_NOT_TEXTURE;
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        // 设置视图矩阵
        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, -3,
                0f, 0f, 0f,
                0f, 1f, 0f);
        int ratio = 1;
        // 投影矩阵
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        // 计算总变换
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 没有初始化、滤镜不可用时直接返回
        if (!mIsInitialized || !mFilterEnable) {
            return false;
        }
        // 设置视口大小
        GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
        // 使用当前的program
        GLES30.glUseProgram(mProgramHandle);
        // 运行延时任务
        runPendingOnDrawTasks();
        // 使能顶点句柄
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        // 绑定总变换矩阵
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        // 绑定颜色
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);
        onDrawFrameBegin();
        // 逐个顶点绘制出来
        synchronized (this) {
            SparseArray<OneFace> faceArrays = LandmarkEngine.getInstance().getFaceArrays();
            for (int i = 0; i < faceArrays.size(); i++) {
                if (faceArrays.get(i).vertexPoints != null) {
                    copyPoints(faceArrays.get(i).vertexPoints);
                    mPointVertexBuffer.clear();
                    mPointVertexBuffer.put(mPoints, 0, mPoints.length);
                    mPointVertexBuffer.position(0);
                    GLES30.glVertexAttribPointer(mPositionHandle, 2,
                            GLES30.GL_FLOAT, false, 8, mPointVertexBuffer);
                    GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mPointCount);
                }
            }
        }
        onDrawFrameAfter();
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        return true;
    }

    /**
     * 复制顶点坐标
     * @param vertexPoints
     */
    private void copyPoints(float[] vertexPoints) {
        if (vertexPoints == null) {
            return;
        }
        for (int i = 0; i < vertexPoints.length && i < mPoints.length; i++) {
            mPoints[i] = vertexPoints[i];
        }
        if (vertexPoints.length < mPoints.length) {
            for (int i = vertexPoints.length; i < mPoints.length; i++) {
                mPoints[i] = -2;
            }
        }
    }

    /**
     * 备注：用于调试使用，禁用FBO，直接绘制
     * @param textureId
     * @param vertexBuffer
     * @param textureBuffer
     * @return
     */
    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 没有FBO、没初始化、输入纹理不合法、滤镜不可用时，直接返回
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE || mFrameBuffers == null
                || !mIsInitialized || !mFilterEnable) {
            return textureId;
        }
        drawFrame(textureId, vertexBuffer, textureBuffer);
        return textureId;
    }

    @Override
    public void initFrameBuffer(int width, int height) {
        // do nothing
    }

    @Override
    public void destroyFrameBuffer() {
        // do nothing
    }
}
