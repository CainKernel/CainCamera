package com.cgfay.landmarklibrary;

import android.util.SparseArray;

/**
 * 人脸关键点引擎
 */
public final class LandmarkEngine {

    private static class EngineHolder {
        public static LandmarkEngine instance = new LandmarkEngine();
    }

    private LandmarkEngine() {
        mFaceArrays = new SparseArray<OneFace>();
    }

    public static LandmarkEngine getInstance() {
        return EngineHolder.instance;
    }

    private final Object mSyncFence = new Object();

    // 人脸对象列表
    // 由于人脸数据个数有限，图像中的人脸个数小于千级，而且人脸索引是连续的，用SparseArray比Hashmap性能要更好
    private final SparseArray<OneFace> mFaceArrays;

    // 手机当前的方向，0表示正屏幕，3表示倒过来，1表示左屏幕，2表示右屏幕
    private float mOrientation;
    private boolean mNeedFlip;

    /**
     * 设置旋转角度
     * @param orientation
     */
    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    /**
     * 设置是否需要翻转
     * @param flip
     */
    public void setNeedFlip(boolean flip) {
        mNeedFlip = flip;
    }

    /**
     * 设置人脸数
     * @param size
     */
    public void setFaceSize(int size) {
        synchronized (mSyncFence) {
            // 剔除脏数据，有可能在前一次检测的人脸多余当前人脸
            if (mFaceArrays.size() > size) {
                mFaceArrays.removeAtRange(size, mFaceArrays.size() - size);
            }
        }
    }

    /**
     * 是否存在人脸
     * @return
     */
    public boolean hasFace() {
        boolean result;
        synchronized (mSyncFence) {
            result = mFaceArrays.size() > 0;
        }
        return result;
    }

    /**
     * 获取一个人脸关键点数据对象
     * @return
     */
    public OneFace getOneFace(int index) {
        OneFace oneFace = null;
        synchronized (mSyncFence) {
            oneFace = mFaceArrays.get(index);
            if (oneFace == null) {
                oneFace = new OneFace();
            }
        }
        return oneFace;
    }

    /**
     * 插入一个人脸关键点数据对象
     * @param index
     */
    public void putOneFace(int index, OneFace oneFace) {
        synchronized (mSyncFence) {
            mFaceArrays.put(index, oneFace);
        }
    }

    /**
     * 获取人脸个数
     * @return
     */
    public int getFaceSize() {
        return mFaceArrays.size();
    }

    /**
     * 获取人脸列表
     * @return
     */
    public SparseArray<OneFace> getFaceArrays() {
        return mFaceArrays;
    }

    /**
     * 清空所有人脸对象
     */
    public void clearAll() {
        synchronized (mSyncFence) {
            mFaceArrays.clear();
        }
    }

    /**
     * 计算额外人脸顶点，新增8个额外顶点坐标
     * @param vertexPoints
     * @param index
     */
    public void calculateExtraFacePoints(float[] vertexPoints, int index) {
        if (vertexPoints == null || index >= mFaceArrays.size() || mFaceArrays.get(index) == null
                || mFaceArrays.get(index).vertexPoints.length + 8 * 2 > vertexPoints.length) {
            return;
        }
        OneFace oneFace = mFaceArrays.get(index);
        // 复制关键点的数据
        System.arraycopy(oneFace.vertexPoints, 0, vertexPoints, 0, oneFace.vertexPoints.length);
        // 新增的人脸关键点
        float[] point = new float[2];
        // 嘴唇中心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.mouthUpperLipBottom * 2],
                vertexPoints[FaceLandmark.mouthUpperLipBottom * 2 + 1],
                vertexPoints[FaceLandmark.mouthLowerLipTop * 2],
                vertexPoints[FaceLandmark.mouthLowerLipTop * 2 + 1]
        );
        vertexPoints[FaceLandmark.mouthCenter * 2] = point[0];
        vertexPoints[FaceLandmark.mouthCenter * 2 + 1] = point[1];

        // 左眉心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.leftEyebrowUpperMiddle * 2],
                vertexPoints[FaceLandmark.leftEyebrowUpperMiddle * 2 + 1],
                vertexPoints[FaceLandmark.leftEyebrowLowerMiddle * 2],
                vertexPoints[FaceLandmark.leftEyebrowLowerMiddle * 2 + 1]
        );
        vertexPoints[FaceLandmark.leftEyebrowCenter * 2] = point[0];
        vertexPoints[FaceLandmark.leftEyebrowCenter * 2 + 1] = point[1];

        // 右眉心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.rightEyebrowUpperMiddle * 2],
                vertexPoints[FaceLandmark.rightEyebrowUpperMiddle * 2 + 1],
                vertexPoints[FaceLandmark.rightEyebrowLowerMiddle * 2],
                vertexPoints[FaceLandmark.rightEyebrowLowerMiddle * 2 + 1]
        );
        vertexPoints[FaceLandmark.rightEyebrowCenter * 2] = point[0];
        vertexPoints[FaceLandmark.rightEyebrowCenter * 2 + 1] = point[1];

        // 额头中心
        vertexPoints[FaceLandmark.headCenter * 2] = vertexPoints[FaceLandmark.eyeCenter * 2] * 2.0f - vertexPoints[FaceLandmark.noseLowerMiddle * 2];
        vertexPoints[FaceLandmark.headCenter * 2 + 1] = vertexPoints[FaceLandmark.eyeCenter * 2 + 1] * 2.0f - vertexPoints[FaceLandmark.noseLowerMiddle * 2 + 1];

        // 额头左侧，备注：这个点不太准确，后续优化
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.leftEyebrowLeftTopCorner * 2],
                vertexPoints[FaceLandmark.leftEyebrowLeftTopCorner * 2 + 1],
                vertexPoints[FaceLandmark.headCenter * 2],
                vertexPoints[FaceLandmark.headCenter * 2 + 1]
        );
        vertexPoints[FaceLandmark.leftHead * 2] = point[0];
        vertexPoints[FaceLandmark.leftHead * 2 + 1] = point[1];

        // 额头右侧，备注：这个点不太准确，后续优化
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.rightEyebrowRightTopCorner * 2],
                vertexPoints[FaceLandmark.rightEyebrowRightTopCorner * 2 + 1],
                vertexPoints[FaceLandmark.headCenter * 2],
                vertexPoints[FaceLandmark.headCenter * 2 + 1]
        );
        vertexPoints[FaceLandmark.rightHead * 2] = point[0];
        vertexPoints[FaceLandmark.rightHead * 2 + 1] = point[1];

        // 左脸颊中心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.leftCheekEdgeCenter * 2],
                vertexPoints[FaceLandmark.leftCheekEdgeCenter * 2 + 1],
                vertexPoints[FaceLandmark.noseLeft * 2],
                vertexPoints[FaceLandmark.noseLeft * 2 + 1]
        );
        vertexPoints[FaceLandmark.leftCheekCenter * 2] = point[0];
        vertexPoints[FaceLandmark.leftCheekCenter * 2 + 1] = point[1];

        // 右脸颊中心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.rightCheekEdgeCenter * 2],
                vertexPoints[FaceLandmark.rightCheekEdgeCenter * 2 + 1],
                vertexPoints[FaceLandmark.noseRight * 2],
                vertexPoints[FaceLandmark.noseRight * 2 + 1]
        );
        vertexPoints[FaceLandmark.rightCheekCenter * 2] = point[0];
        vertexPoints[FaceLandmark.rightCheekCenter * 2 + 1] = point[1];
    }

    /**
     * 计算
     * @param vertexPoints
     */
    private void calculateImageEdgePoints(float[] vertexPoints) {
        if (vertexPoints == null || vertexPoints.length < 122 * 2) {
            return;
        }

        if (mOrientation == 0) {
            vertexPoints[114 * 2] = 0;
            vertexPoints[114 * 2 + 1] = 1;
            vertexPoints[115 * 2] = 1;
            vertexPoints[115 * 2 + 1] = 1;
            vertexPoints[116 * 2] = 1;
            vertexPoints[116 * 2 + 1] = 0;
            vertexPoints[117 * 2] = 1;
            vertexPoints[117 * 2 + 1] = -1;
        } else if (mOrientation == 1) {
            vertexPoints[114 * 2] = 1;
            vertexPoints[114 * 2 + 1] = 0;
            vertexPoints[115 * 2] = 1;
            vertexPoints[115 * 2 + 1] = -1;
            vertexPoints[116 * 2] = 0;
            vertexPoints[116 * 2 + 1] = -1;
            vertexPoints[117 * 2] = -1;
            vertexPoints[117 * 2 + 1] = -1;
        } else if (mOrientation == 2) {
            vertexPoints[114 * 2] = -1;
            vertexPoints[114 * 2 + 1] = 0;
            vertexPoints[115 * 2] = -1;
            vertexPoints[115 * 2 + 1] = 1;
            vertexPoints[116 * 2] = 0;
            vertexPoints[116 * 2 + 1] = 1;
            vertexPoints[117 * 2] = 1;
            vertexPoints[117 * 2 + 1] = 1;
        } else if (mOrientation == 3) {
            vertexPoints[114 * 2] = 0;
            vertexPoints[114 * 2 + 1] = -1;
            vertexPoints[115 * 2] = -1;
            vertexPoints[115 * 2 + 1] = -1;
            vertexPoints[116 * 2] = -1;
            vertexPoints[116 * 2 + 1] = 0;
            vertexPoints[117 * 2] = -1;
            vertexPoints[117 * 2 + 1] = 1;
        }
        // 118 ~ 121 与 114 ~ 117 的顶点坐标恰好反过来
        vertexPoints[118 * 2] = -vertexPoints[114 * 2];
        vertexPoints[118 * 2 + 1] = -vertexPoints[114 * 2 + 1];
        vertexPoints[119 * 2] = -vertexPoints[115 * 2];
        vertexPoints[119 * 2 + 1] = -vertexPoints[115 * 2 + 1];
        vertexPoints[120 * 2] = -vertexPoints[116 * 2];
        vertexPoints[120 * 2 + 1] = -vertexPoints[116 * 2 + 1];
        vertexPoints[121 * 2] = -vertexPoints[117 * 2];
        vertexPoints[121 * 2 + 1] = -vertexPoints[117 * 2 + 1];

        // 是否需要做翻转处理，前置摄像头预览时，关键点是做了翻转处理的，因此图像边沿的关键点也要做翻转能处理
        if (mNeedFlip) {
            for (int i = 0; i < 8; i++) {
                vertexPoints[(114 + i) * 2] = -vertexPoints[(114 + i) * 2];
                vertexPoints[(114 + i) * 2 + 1] = -vertexPoints[(114 + i) * 2 + 1];
            }
        }

    }

    /**
     * 获取用于美型处理的坐标
     * @param vertexPoints  顶点坐标，一共122个顶点
     * @param texturePoints 纹理坐标，一共122个顶点
     * @param faceIndex     人脸索引
     */
    public void updateFaceAdjustPoints(float[] vertexPoints, float[] texturePoints, int faceIndex) {
        if (vertexPoints == null || vertexPoints.length != 122 * 2
                || texturePoints == null || texturePoints.length != 122 * 2) {
            return;
        }
        // 计算额外的人脸顶点坐标
        calculateExtraFacePoints(vertexPoints, faceIndex);
        // 计算图像边沿顶点坐标
        calculateImageEdgePoints(vertexPoints);
        // 计算纹理坐标
        for (int i = 0; i < vertexPoints.length; i++) {
            texturePoints[i] = vertexPoints[i] * 0.5f + 0.5f;
        }
    }

    /**
     * 阴影(修容)顶点坐标，修容用的是整个人脸的顶点坐标
     * @param vetexPoints
     * @param faceIndex
     */
    public void getShadowVertices(float[] vetexPoints, int faceIndex) {

    }

    /**
     * 取得脸颊(腮红)顶点坐标
     * @param vertexPoints
     * @param faceIndex
     */
    public void getBlushVertices(float[] vertexPoints, int faceIndex) {

    }

    /**
     * 取得眉毛顶点坐标
     * @param vertexPoints
     * @param faceIndex
     */
    public void getEyeBrowVertices(float[] vertexPoints, int faceIndex) {

    }

    /**
     * 取得眼睛(眼影、眼线等)顶点坐标
     * @param vertexPoints
     * @param faceIndex
     */
    public void getEyeVertices(float[] vertexPoints, int faceIndex) {

    }

    /**
     * 取得嘴唇(唇彩)顶点坐标
     * @param vertexPoints  存放嘴唇顶点坐标
     * @param faceIndex     人脸索引
     */
    public void getLipsVertices(float[] vertexPoints, int faceIndex) {
        // 嘴唇一共20个顶点，大小必须为40
        if (vertexPoints == null || vertexPoints.length != 40
                || faceIndex >= mFaceArrays.size() || mFaceArrays.get(faceIndex) == null) {
            return;
        }
        // 复制84 ~ 103共20个顶点坐标
        for (int i = 0; i < 20; i++) {
            // 顶点坐标
            vertexPoints[i * 2] = mFaceArrays.get(faceIndex).vertexPoints[(84 + i) * 2];
            vertexPoints[i * 2 + 1] = mFaceArrays.get(faceIndex).vertexPoints[(84 + i) * 2 + 1];
        }
    }
}
