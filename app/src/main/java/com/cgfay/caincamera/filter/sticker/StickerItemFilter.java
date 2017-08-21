package com.cgfay.caincamera.filter.sticker;

import android.opengl.Matrix;

import com.cgfay.caincamera.filter.base.BaseImageFilter;

import java.nio.FloatBuffer;

/**
 * 3D贴纸
 * Created by cain.huang on 2017/8/21.
 */
public class StickerItemFilter extends BaseImageFilter {


    // 贴纸的位置
    private float mPositionX = 0.0f;
    private float mPositionY = 0.0f;
    private float mPositionZ = 0.0f;

    // 贴纸的缩放
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private float mScaleZ = 1.0f;


    public StickerItemFilter() {
        super();
        setupLookAt();
    }

    public StickerItemFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        setupLookAt();
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setupProjection();
    }

    @Override
    public void drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        calculateMVPMatrix();
        super.drawFrame(textureId, vertexBuffer, textureBuffer);
    }

    @Override
    protected void calculateMVPMatrix() {
        // 模型矩阵变换
        Matrix.setIdentityM(mModelMatrix, 0); // 重置模型矩阵方便计算
        // 计算模型矩阵的位移
        Matrix.translateM(mModelMatrix, 0, mPositionX, mPositionY, -1.0f + mPositionZ);
        // 计算模型矩阵的缩放
        Matrix.scaleM(mModelMatrix, 0, mScaleX, mScaleY, mScaleZ);
        // 计算模型矩阵的姿态角
        Matrix.rotateM(mModelMatrix, 0, mYawAngle, 1.0f, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, mPitchAngle, 0, 1.0f, 0);
        Matrix.rotateM(mModelMatrix, 0, mRollAngle, 0, 0, 1.0f);
        // 综合矩阵变换
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
    }

    /**
     * 初始化相机矩阵(视图矩阵)
     */
    private void setupLookAt() {
        // 设置视图矩阵
        int rmOffset = 0;
        float eyeX = 0.0f;
        float eyeY = 0.0f;
        float eyeZ = 3.0f;

        float centerX = 0.0f;
        float centerY = 0.0f;
        float centerZ = 0.0f;

        float upX = 0.0f;
        float upY = 1.0f;
        float upZ = 0.0f;
        Matrix.setLookAtM(mViewMatrix, rmOffset,
                eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ,
                upX, upY, upZ);
    }

    /**
     * 初始化投影矩阵
     */
    private void setupProjection() {
        // 设置透视投影矩阵
//        float mRatio = 9 * 1.0f / 16;
//        float left = -mRatio;
//        float right = mRatio;
        int offset = 0;

        float left = -1;
        float right = 1;
        float bottom = -1.0f;
        float top = 1.0f;
        float near = 3.0f;
        float far = 7.0f;
        Matrix.frustumM(mProjectionMatrix, offset,
                left, right, bottom, top, near, far);
    }

    /**
     * 设置贴纸的起始位置
     * @param x
     * @param y
     * @param z
     */
    public void setTrackPosition(float x, float y, float z) {
        mPositionX = x;
        mPositionY = y;
        mPositionZ = z;
    }

    /**
     * 设置贴纸的缩放比例
     * @param scaleX
     */
    public void setTrackScale(float scaleX, float scaleY, float scaleZ) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mScaleZ = scaleZ;
    }
}
