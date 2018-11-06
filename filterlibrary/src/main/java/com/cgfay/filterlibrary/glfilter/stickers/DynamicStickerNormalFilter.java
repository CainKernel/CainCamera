package com.cgfay.filterlibrary.glfilter.stickers;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerNormalData;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;
import com.cgfay.landmarklibrary.FacePointsUtils;
import com.cgfay.landmarklibrary.LandmarkEngine;
import com.cgfay.landmarklibrary.OneFace;

import java.nio.FloatBuffer;

/**
 * 绘制普通贴纸(非前景贴纸)
 */
public class DynamicStickerNormalFilter extends DynamicStickerBaseFilter {

    // 视椎体缩放倍数，具体数据与setLookAt 和 frustumM有关
    // 备注：setLookAt 和 frustumM 设置的结果导致了视点(eye)到近平面(near)和视点(eye)到贴纸(center)恰好是2倍的关系
    private static final float ProjectionScale = 2.0f;

    // 变换矩阵句柄
    private int mMVPMatrixHandle;

    // 贴纸变换矩阵
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    // 长宽比
    private float mRatio;

    // 贴纸坐标缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // 贴纸顶点
    private float[] mStickerVertices = new float[8];

    public DynamicStickerNormalFilter(Context context, DynamicSticker sticker) {
        super(context, sticker, OpenGLUtils.getShaderFromAssets(context, "shader/sticker/vertex_sticker_normal.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/sticker/fragment_sticker_normal.glsl"));
        // 创建贴纸加载器列表
        if (mDynamicSticker != null && mDynamicSticker.dataList != null) {
            for (int i = 0; i < mDynamicSticker.dataList.size(); i++) {
                if (mDynamicSticker.dataList.get(i) instanceof DynamicStickerNormalData) {
                    String path = mDynamicSticker.unzipPath + "/" + mDynamicSticker.dataList.get(i).stickerName;
                    mStickerLoaderList.add(new DynamicStickerLoader(this, mDynamicSticker.dataList.get(i), path));
                }
            }
        }
        initMatrix();
        initBuffer();
    }

    /**
     * 初始化纹理
     */
    private void initMatrix() {
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    /**
     * 初始化缓冲
     */
    private void initBuffer() {
        releaseBuffer();
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    /**
     * 释放缓冲
     */
    private void releaseBuffer() {
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
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        } else {
            mMVPMatrixHandle = OpenGLUtils.GL_NOT_INIT;
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        mRatio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1.0f, 1.0f, 3.0f, 9.0f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public void release() {
        super.release();
        releaseBuffer();
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 绘制到FBO
        int stickerTexture = drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        // 绘制显示
        return super.drawFrame(stickerTexture, vertexBuffer, textureBuffer);
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 1、先将图像绘制到FBO中
        Matrix.setIdentityM(mMVPMatrix, 0);
        super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        // 2、将贴纸逐个绘制到FBO中
        if (mStickerLoaderList.size() > 0 && LandmarkEngine.getInstance().hasFace()) {
            // 逐个人脸绘制
            int faceCount = Math.min(LandmarkEngine.getInstance().getFaceArrays().size(), mStickerLoaderList.get(0).getMaxCount());
            for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
                OneFace oneFace = LandmarkEngine.getInstance().getOneFace(faceIndex);
                // 如果置信度大于0.5，表示这是一个正常的人脸，绘制贴纸
                if (oneFace.confidence > 0.5f) {
                    for (int stickerIndex = 0; stickerIndex < mStickerLoaderList.size(); stickerIndex++) {
                        synchronized (this) {
                            mStickerLoaderList.get(stickerIndex).updateStickerTexture();
                            calculateStickerVertices((DynamicStickerNormalData) mStickerLoaderList.get(stickerIndex).getStickerData(),
                                    oneFace);
                            super.drawFrameBuffer(mStickerLoaderList.get(stickerIndex).getStickerTexture(), mVertexBuffer, mTextureBuffer);
                        }
                    }
                }
            }
        }
        return mFrameBufferTextures[0];
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mMVPMatrixHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        }
        // 绘制到FBO中，需要开启混合模式
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);
        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA, GLES30.GL_ONE, GLES30.GL_ONE);
    }

    @Override
    public void onDrawFrameAfter() {
        super.onDrawFrameAfter();
        GLES30.glDisable(GLES30.GL_BLEND);
    }

    /**
     * 更新贴纸顶点
     * TODO 待优化的点：消除姿态角误差、姿态角给贴纸偏移量造成的误差
     * @param stickerData
     */
    private void calculateStickerVertices(DynamicStickerNormalData stickerData, OneFace oneFace) {
        if (oneFace == null || oneFace.vertexPoints == null) {
            return;
        }
        // 步骤一、计算贴纸的中心点和顶点坐标
        // 备注：由于frustumM设置的bottom 和top 为 -1.0 和 1.0，这里为了方便计算，直接用高度作为基准值来计算
        // 1.1、计算贴纸相对于人脸的宽高
        float stickerWidth = (float) FacePointsUtils.getDistance(
                (oneFace.vertexPoints[stickerData.startIndex * 2] * 0.5f + 0.5f) * mImageWidth,
                (oneFace.vertexPoints[stickerData.startIndex * 2 + 1] * 0.5f + 0.5f) * mImageHeight,
                (oneFace.vertexPoints[stickerData.endIndex * 2] * 0.5f + 0.5f) * mImageWidth,
                (oneFace.vertexPoints[stickerData.endIndex * 2 + 1] * 0.5f + 0.5f) * mImageHeight) * stickerData.baseScale;
        float stickerHeight = stickerWidth * (float) stickerData.height / (float) stickerData.width;

        // 1.2、根据贴纸的参数计算出中心点的坐标
        float centerX = 0.0f;
        float centerY = 0.0f;
        for (int i = 0; i < stickerData.centerIndexList.length; i++) {
            centerX += (oneFace.vertexPoints[stickerData.centerIndexList[i] * 2] * 0.5f + 0.5f) * mImageWidth;
            centerY += (oneFace.vertexPoints[stickerData.centerIndexList[i] * 2 + 1] * 0.5f + 0.5f) * mImageHeight;
        }
        centerX /= (float) stickerData.centerIndexList.length;
        centerY /= (float) stickerData.centerIndexList.length;
        centerX = centerX / mImageHeight * ProjectionScale;
        centerY = centerY / mImageHeight * ProjectionScale;
        // 1.3、求出真正的中心点顶点坐标，这里由于frustumM设置了长宽比，因此ndc坐标计算时需要变成mRatio:1，这里需要转换一下
        float ndcCenterX = (centerX - mRatio) * ProjectionScale;
        float ndcCenterY = (centerY - 1.0f) * ProjectionScale;

        // 1.4、贴纸的宽高在ndc坐标系中的长度
        float ndcStickerWidth = stickerWidth / mImageHeight * ProjectionScale;
        float ndcStickerHeight = ndcStickerWidth * (float) stickerData.height / (float) stickerData.width;

        // 1.5、根据贴纸参数求偏移的ndc坐标
        float offsetX = (stickerWidth * stickerData.offsetX) / mImageHeight * ProjectionScale;
        float offsetY = (stickerHeight * stickerData.offsetY) / mImageHeight * ProjectionScale;

        // 1.6、贴纸带偏移量的锚点的ndc坐标，即实际贴纸的中心点在OpenGL的顶点坐标系中的位置
        float anchorX = ndcCenterX + offsetX * ProjectionScale;
        float anchorY = ndcCenterY + offsetY * ProjectionScale;

        // 1.7、根据前面的锚点，计算出贴纸实际的顶点坐标
        mStickerVertices[0] = anchorX - ndcStickerWidth; mStickerVertices[1] = anchorY - ndcStickerHeight;
        mStickerVertices[2] = anchorX + ndcStickerWidth; mStickerVertices[3] = anchorY - ndcStickerHeight;
        mStickerVertices[4] = anchorX - ndcStickerWidth; mStickerVertices[5] = anchorY + ndcStickerHeight;
        mStickerVertices[6] = anchorX + ndcStickerWidth; mStickerVertices[7] = anchorY + ndcStickerHeight;
        mVertexBuffer.clear();
        mVertexBuffer.position(0);
        mVertexBuffer.put(mStickerVertices);

        // 步骤二、根据人脸姿态角计算透视变换的总变换矩阵
        // 2.1、将Z轴平移到贴纸中心点，因为贴纸模型矩阵需要做姿态角变换
        // 平移主要是防止贴纸变形
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, ndcCenterX, ndcCenterY, 0);

        // 2.2、贴纸姿态角旋转
        // TODO 人脸关键点给回来的pitch角度似乎不太对？？SDK给过来的pitch角度值太小了，比如抬头低头pitch的实际角度30度了，SDK返回的结果才十几度，后续再看看如何优化
        float pitchAngle = -(float) (oneFace.pitch * 180f / Math.PI);
        float yawAngle = (float) (oneFace.yaw * 180f / Math.PI);
        float rollAngle = (float) (oneFace.roll * 180f / Math.PI);
        // 限定左右扭头幅度不超过50°，销毁人脸关键点SDK带来的偏差
        if (Math.abs(yawAngle) > 50) {
            yawAngle = (yawAngle / Math.abs(yawAngle)) * 50;
        }
        // 限定抬头低头最大角度，消除人脸关键点SDK带来的偏差
        if (Math.abs(pitchAngle) > 30) {
            pitchAngle = (pitchAngle / Math.abs(pitchAngle)) * 30;
        }
        // 贴纸姿态角变换，优先z轴变换，消除手机旋转的角度影响，否则会导致扭头、抬头、低头时贴纸变形的情况
        Matrix.rotateM(mModelMatrix, 0, rollAngle, 0, 0, 1);
        Matrix.rotateM(mModelMatrix, 0, yawAngle, 0, 1, 0);
        Matrix.rotateM(mModelMatrix, 0, pitchAngle, 1, 0, 0);

        // 2.4、将Z轴平移回到原来构建的视椎体的位置，即需要将坐标z轴平移回到屏幕中心，此时才是贴纸的实际模型矩阵
        Matrix.translateM(mModelMatrix, 0, -ndcCenterX, -ndcCenterY, 0);

        // 2.5、计算总变换矩阵。MVPMatrix 的矩阵计算是 MVPMatrix = ProjectionMatrix * ViewMatrix * ModelMatrix
        // 备注：矩阵相乘的顺序不同得到的结果是不一样的，不同的顺序会导致前面计算过程不一致，这点希望大家要注意
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mModelMatrix, 0);
    }
}
