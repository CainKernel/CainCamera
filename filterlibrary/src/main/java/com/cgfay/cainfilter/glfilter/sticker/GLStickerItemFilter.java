package com.cgfay.cainfilter.glfilter.sticker;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.cgfay.cainfilter.core.ParamsManager;
import com.cgfay.cainfilter.type.StickerType;
import com.cgfay.cainfilter.utils.GlUtil;
import com.cgfay.utilslibrary.BitmapUtils;

import java.nio.FloatBuffer;

/**
 * 渲染某个部分的贴纸
 * Created by cain on 2018/1/13.
 */

public class GLStickerItemFilter {

    /**
     * 纹理坐标
     */
    private static final float[] TextureCoords = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    /**
     * 顶点坐标
     */
    private static final float[] VertexCoords = {
            -1.0f, -1.0f, 0.0f, // 0 bottom left
            1.0f,  -1.0f, 0.0f, // 1 bottom right
            -1.0f,  1.0f, 0.0f, // 2 top left
            1.0f,   1.0f, 0.0f, // 3 top right
    };

    // 每个顶点的坐标数
    private static final int mCoordsPerVertex = 3;
    private static final int mCoordsPerTexture = 2;

    private int mTextureId = GlUtil.GL_NOT_INIT; // 当前贴纸的Texture

    private FloatBuffer mVertexBuffer;  // 顶点坐标缓冲
    private FloatBuffer mTextureBuffer; // 纹理坐标缓冲

    private float[] mViewMatrix = new float[16];    // 视图矩阵
    private float[] mModelMatrix = new float[16];   // 模型矩阵
    private float[] mProjectionMatrix = new float[16];  // 投影矩阵
    private float[] mMVPMatrix = new float[16]; // 总变换矩阵

    // 贴纸的欧拉角
    private float mPitchAngle = 0.0f;
    private float mYawAngle = 0.0f;
    private float mRollAngle = 0.0f;

    // 贴纸的类型(默认没有)
    private StickerType mStickerType = StickerType.NONE;

    // 贴纸的索引
    private int mStickerIndex = 0;
    // 贴纸的总数
    private int mStickerSum = 12;

    // 中心店
    private float mCenterX = 0.0f;
    private float mCenterY = 0.0f;
    private float mCenterZ = 0.0f;

    // 贴纸持有的数据
    private Bitmap mBitmap = null;

    // 贴纸的路径
    private String mStickerPath = null;

    public GLStickerItemFilter(StickerType type) {
        mStickerType = type;
        initIdentityMatrix();
        initBuffer();
        updateTexture();
    }

    /**
     * 创建坐标缓冲
     */
    private void initBuffer() {
        mVertexBuffer = GlUtil.createFloatBuffer(VertexCoords);
        mTextureBuffer = GlUtil.createFloatBuffer(TextureCoords);
    }

    /**
     * 初始化单位矩阵
     */
    private void initIdentityMatrix() {
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        // 设置视图矩阵
        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, -4,               // 相机位置坐标
                0f, 0f, 0f,      // 查看目标的坐标
                0f, 1f, 0f);              // 相机向上的坐标
        // 设置姿态角
    }

    /**
     * 输入图像发生变化时，重新计算投影矩阵
     * @param width
     * @param height
     */
    public void onInputSizeChanged(int width, int height) {
        // 设置投影矩阵，这里保持长宽比主要是为了方面做姿态角换算
        float aspectRatio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0,
                -aspectRatio, aspectRatio, -1, 1, 2, 6);
    }

    /**
     * 计算视锥体变换矩阵(MVPMatrix)
     */
    public void calculateMVPMatrix() {
        // 模型矩阵变换，每次都需
        Matrix.setIdentityM(mModelMatrix, 0);
        // 计算模型的姿态角
        Matrix.rotateM(mModelMatrix, 0, mPitchAngle, 1.0f, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, mYawAngle, 0, 1.0f, 0);
        Matrix.rotateM(mModelMatrix, 0, mRollAngle, 0, 0, 1.0f);
        // 综合矩阵变换
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
    }

    /**
     * 更新贴纸
     */
    public void updateTexture() {
        mStickerPath = "stickers/";
        switch (mStickerType) {
            // 头
            case HEAD:
                mStickerPath  = mStickerPath + "tou/tou_";
                mCenterX = 0.0f;
                mCenterY = 0.6f;
                break;
            // 耳朵
            case EAR:
                mStickerPath  = mStickerPath + "erduo/erduo_";
                mCenterX = 0.0f;
                mCenterY = 0.1f;
                break;
            // 人脸
            case FACE:
                mStickerPath = mStickerPath + "lian/lian_";
                break;
            // 鼻子
            case NOSE:
                mStickerPath = mStickerPath + "bizi/bizi_";
                mCenterX = 0.0f;
                mCenterY = 0.1f;
                break;
            // 胡子
            case BEARD:
                mStickerPath = mStickerPath + "huzi/huzi_";
                mCenterX = 0.0f;
                mCenterY = -0.5f;
                break;

            // 没有贴纸
            case NONE:
                mStickerPath = null;
                break;

            // 前景帧（暂未使用）
            case FRAME:
            default:
                throw new IllegalStateException("unknown sticker type");

        }
        if (mStickerPath != null) {
            // 更新贴纸
            mStickerPath += indexToString(mStickerIndex) + ".png";
            mBitmap = BitmapUtils.getImageFromAssetsFile(ParamsManager.context, mStickerPath, mBitmap);
            if (mTextureId == GlUtil.GL_NOT_INIT) {
                mTextureId = GlUtil.createTexture(mBitmap);
            } else {
                mTextureId = GlUtil.createTextureWithOldTexture(mTextureId, mBitmap);
            }
            // 更新索引
            mStickerIndex++;
            mStickerIndex %= mStickerSum;
        }

    }

    /**
     * 将索引转成字符串
     * @param index
     * @return
     */
    String indexToString(int index) {
        // 防止超出索引范围，找不到地址
        index = index % mStickerSum;
        String indexStr = "00";
        if (index < 10) {
            indexStr += String.valueOf(index);
        } else if (index < 100) {
            indexStr = "0" + String.valueOf(index);
        } else if (index < 1000) {
            indexStr = String.valueOf(index);
        } else { // 超过1000张贴纸动画不支持
            throw new IllegalStateException("cannot find sticker path!");
        }
        return indexStr;
    }

    /**
     * 释放资源
     */
    public void release() {
        // 释放Bitmap
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        mVertexBuffer.clear();
        mVertexBuffer = null;
        mTextureBuffer.clear();
        mTextureBuffer = null;
        GLES30.glDeleteTextures(1, new int[] {mTextureId}, 0);
    }

    /**
     * 设置顶点坐标缓冲（需要保证三维顶点(长度为12的倍数)，做透视变换）
     * @param coords
     */
    public void setVertexCoords(float[] coords) {
        if (coords.length % 12 != 0) {
            throw new IllegalStateException("vertex coordinates is error!");
        }
        mVertexBuffer.clear();
        mVertexBuffer.put(coords);
        mVertexBuffer.position(0);
    }

    /**
     * 模型矩阵 X轴旋转角度（0 ~ 360）
     * @param angle
     */
    public void setYawAngle(float angle) {
        if (mYawAngle != angle) {
            mYawAngle = angle;
        }
    }

    /**
     * 模型矩阵 Y轴旋转角度(0 ~ 360)
     * @param angle
     */
    public void setPitchAngle(float angle) {
        if (mPitchAngle != angle) {
            mPitchAngle = angle;
        }
    }

    /**
     * 模型矩阵 Z轴旋转角度(0 ~ 360)
     * @param angle
     */
    public void setRollAngle(float angle) {
        if (mRollAngle != angle) {
            mRollAngle = angle;
        }
    }


    /**
     * 设置贴纸的总数(每个贴纸可能持有的数目不一样)
     * @param sum
     */
    public void setStickerSum(int sum) {
        mStickerSum = sum;
    }

    /**
     * 获取当前的Texture
     * @return
     */
    public int getTexture() {
        return mTextureId;
    }

    /**
     * 获取顶点坐标缓冲
     * @return
     */
    public FloatBuffer getVertexBuffer() {
        return mVertexBuffer;
    }

    /**
     * 获取纹理缓冲
     * @return
     */
    public FloatBuffer getTextureBuffer() {
        return mTextureBuffer;
    }

    /**
     * 获取每个顶点的坐标轴数
     * @return
     */
    public int getCoordsPerVertex() {
        return mCoordsPerVertex;
    }

    /**
     * 每个纹理的坐标轴数
     * @return
     */
    public int getCoordsPerTexture() {
        return mCoordsPerTexture;
    }

    /**
     * 获取总变换矩阵
     * @return
     */
    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }
}
