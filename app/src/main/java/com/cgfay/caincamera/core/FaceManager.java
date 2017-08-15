package com.cgfay.caincamera.core;

import com.cgfay.caincamera.utils.CameraUtils;
import com.megvii.facepp.sdk.Facepp;

import java.util.ArrayList;

/**
 * Face++的人脸检测管理器
 * Created by cain.huang on 2017/8/15.
 */
public class FaceManager {

    private static FaceManager instance;

    private FaceDetector mFaceDetector;

    private FaceManager() {
        mFaceDetector = new FaceDetector();
    }

    public static FaceManager getInstance() {

        if (instance == null) {
            instance = new FaceManager();
        }
        return instance;
    }

    /**
     * 创建线程
     */
    public void createHandleThread() {
        mFaceDetector.create();
    }

    /**
     * 初始化人脸检测配置
     * @param width 图像的宽度
     * @param height 图像的高度
     */
    public void initFaceConfig(int width, int height) {
        mFaceDetector.initFaceConfig(width, height);
    }

    /**
     * 人脸检测
     * @param data
     * @param width
     * @param height
     */
    public void faceDetecting(byte[] data, int width, int height) {
        mFaceDetector.faceDetecting(data, width, height);
    }

    /**
     * 销毁FaceDetector
     */
    public void destory() {
        mFaceDetector.destory();
    }

    /**
     * 获取FaceDetector
     * @return
     */
    public FaceDetector getFaceDetector() {
        return mFaceDetector;
    }
}
