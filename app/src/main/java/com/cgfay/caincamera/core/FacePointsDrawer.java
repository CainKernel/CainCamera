package com.cgfay.caincamera.core;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.cgfay.caincamera.utils.GlUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * 人脸关键点绘制
 * Created by cain.huang on 2017/8/18.
 */
public class FacePointsDrawer {
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;                   \n" +
            "attribute vec4 aPosition;                  \n" +
            "void main() {                              \n" +
            "    gl_Position = aPosition * uMVPMatrix;  \n" +
            "    gl_PointSize = 8.0;                    \n" +
            "}                                          ";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;                   \n" +
            "uniform vec4 inputColor;                   \n" +
            "void main() {                              \n" +
            "  gl_FragColor = inputColor;               \n" +
            "}                                          ";

    private static final float[] Color = {
            0.2f, 0.709803922f, 0.898039216f, 1.0f
    };

    protected int mProgramHandle;
    protected int muMVPMatrixLoc;
    protected int maPositionLoc;
    private int mInputColorLoc;
    private float[] mMVPMatrix = new float[16];
    // 是否正在绘制
    private boolean hasDrawing = false;
    // 画点
    private ArrayList<ArrayList<float[]>> mPoints = new ArrayList<ArrayList<float[]>>();

    public FacePointsDrawer() {
        initProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    protected void initProgram(String vertexShader, String fragmentShader) {
        mProgramHandle = GlUtil.createProgram(vertexShader, fragmentShader);
        maPositionLoc = GLES30.glGetAttribLocation(mProgramHandle, "aPosition");
        muMVPMatrixLoc = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        mInputColorLoc = GLES30.glGetUniformLocation(mProgramHandle, "inputColor");
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    /**
     * 绘制点
     */
    public void drawPoints() {
        if (mPoints == null || mPoints.size() <= 0 || hasDrawing) {
            return;
        }
        // 标志位变更需要同步
        hasDrawing = true;
        GLES30.glUseProgram(mProgramHandle);
        GLES30.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES30.glEnableVertexAttribArray(maPositionLoc);
        GLES30.glUniform4fv(mInputColorLoc, 1, Color, 0);
        ArrayList<ArrayList<float[]>> points = new ArrayList<ArrayList<float[]>>();
        synchronized (this) {
            if (mPoints != null) {
                points = (ArrayList<ArrayList<float[]>>) mPoints.clone();
            }
        }
        for (int i = 0; i < points.size(); i++) {
            ArrayList<float[]> vertexLists = points.get(i);
            if (vertexLists != null) {
                for (int j = 0; j < vertexLists.size(); j++) {
                    float[] point = vertexLists.get(j);
                    if (point != null && point.length > 0) {
                        FloatBuffer buffer = GlUtil.createFloatBuffer(point);
                        GLES30.glVertexAttribPointer(maPositionLoc, 3,
                                GLES30.GL_FLOAT, false, 0, buffer);
                        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
                        buffer.clear();
                    }
                }
            }
        }
        GLES30.glDisableVertexAttribArray(maPositionLoc);
        GLES30.glUseProgram(0);
        hasDrawing = false;
    }

    /**
     * 设置变换矩阵
     * @param mvpMatrix
     */
    public void setMVPMAtrix(float[] mvpMatrix) {
        mMVPMatrix = mvpMatrix;
    }

    /**
     * 添加点
     * @param points
     */
    public void addPoints(ArrayList<ArrayList<float[]>> points) {
        // 如果还在绘制过程，则舍弃当前检测的点
        mPoints = points;
    }

    /**
     * 释放资源
     */
    public void release() {
        GLES30.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }
}
