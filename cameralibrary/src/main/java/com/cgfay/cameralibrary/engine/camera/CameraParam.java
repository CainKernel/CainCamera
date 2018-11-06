package com.cgfay.cameralibrary.engine.camera;

import android.hardware.Camera;

import com.cgfay.cameralibrary.engine.listener.OnCameraCallback;
import com.cgfay.cameralibrary.engine.listener.OnCaptureListener;
import com.cgfay.cameralibrary.engine.listener.OnFpsListener;
import com.cgfay.cameralibrary.engine.model.AspectRatio;
import com.cgfay.cameralibrary.engine.model.GalleryType;
import com.cgfay.cameralibrary.listener.OnGallerySelectedListener;
import com.cgfay.cameralibrary.listener.OnPreviewCaptureListener;
import com.cgfay.filterlibrary.glfilter.beauty.bean.BeautyParam;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupParam;

/**
 * 相机配置参数
 */
public final class CameraParam {

    // 最大权重
    public static final int MAX_FOCUS_WEIGHT = 1000;
    // 录制时长(毫秒)
    public static final int DEFAULT_RECORD_TIME = 15000;

    // 16:9的默认宽高(理想值)
    public static final int DEFAULT_16_9_WIDTH = 1280;
    public static final int DEFAULT_16_9_HEIGHT = 720;
    // 4:3的默认宽高(理想值)
    public static final int DEFAULT_4_3_WIDTH = 1024;
    public static final int DEFAULT_4_3_HEIGHT = 768;
    // 期望fps
    public static final int DESIRED_PREVIEW_FPS = 30;
    // 这里反过来是因为相机的分辨率跟屏幕的分辨率宽高刚好反过来
    public static final float Ratio_4_3 = 0.75f;
    public static final float Ratio_16_9 = 0.5625f;

    // 对焦权重最大值
    public static final int Weight =  100;

    // 是否显示人脸关键点
    public boolean drawFacePoints;
    // 是否显示fps
    public boolean showFps;
    // 相机长宽比类型
    public AspectRatio aspectRatio;
    // 当前长宽比
    public float currentRatio;
    // 期望帧率
    public int expectFps;
    // 实际帧率
    public int previewFps;
    // 期望预览宽度
    public int expectWidth;
    // 期望预览高度
    public int expectHeight;
    // 实际预览宽度
    public int previewWidth;
    // 实际预览高度
    public int previewHeight;
    // 是否高清拍照
    public boolean highDefinition;
    // 预览角度
    public int orientation;
    // 是否后置摄像头
    public boolean backCamera;
    // 摄像头id
    public int cameraId;
    // 是否支持闪光灯
    public boolean supportFlash;
    // 对焦权重，最大值为1000
    public int focusWeight;
    // 是否允许录制
    public boolean recordable;
    // 录制时长(ms)
    public int recordTime;
    // 录音权限
    public boolean audioPermitted;
    // 是否允许录制音频
    public boolean recordAudio;
    // 是否触屏拍照
    public boolean touchTake;
    // 是否延时拍照
    public boolean takeDelay;
    // 是否夜光增强
    public boolean luminousEnhancement;
    // 亮度值
    public int brightness;
    // 拍照类型
    public GalleryType mGalleryType;

    // 图库监听器
    public OnGallerySelectedListener gallerySelectedListener;
    // 拍摄监听器
    public OnPreviewCaptureListener captureListener;

    // 预览回调
    public OnCameraCallback cameraCallback;
    // 截屏回调
    public OnCaptureListener captureCallback;
    // fps回调
    public OnFpsListener fpsCallback;
    // 是否显示对比效果
    public boolean showCompare;
    // 是否拍照
    public boolean isTakePicture;

    // 是否允许景深
    public boolean enableDepthBlur;
    // 是否允许暗角
    public boolean enableVignette;
    // 美颜参数
    public BeautyParam beauty;
    // 彩妆参数
    public MakeupParam makeup;

    private static final CameraParam mInstance = new CameraParam();

    private CameraParam() {
        reset();
    }

    /**
     * 重置为初始状态
     */
    private void reset() {
        drawFacePoints = false;
        showFps = false;
        aspectRatio = AspectRatio.RATIO_4_3;
        currentRatio = 0.75f;
        expectFps = DESIRED_PREVIEW_FPS;
        previewFps = 0;
        expectWidth = DEFAULT_4_3_WIDTH;
        expectHeight = DEFAULT_4_3_HEIGHT;
        previewWidth = 0;
        previewHeight = 0;
        highDefinition = false;
        orientation = 0;
        backCamera = false;
        cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        supportFlash = false;
        focusWeight = 1000;
        recordable = true;
        recordTime = DEFAULT_RECORD_TIME;
        audioPermitted = false;
        recordAudio = true;
        touchTake = false;
        takeDelay = false;
        luminousEnhancement = false;
        brightness = -1;
        mGalleryType = GalleryType.PICTURE;
        gallerySelectedListener = null;
        captureListener = null;
        cameraCallback = null;
        captureCallback = null;
        fpsCallback = null;
        showCompare = false;
        isTakePicture = false;
        enableDepthBlur = false;
        enableVignette = false;
        beauty = new BeautyParam();
        makeup = new MakeupParam();
    }

    /**
     * 获取相机配置参数
     * @return
     */
    public static CameraParam getInstance() {
        return mInstance;
    }

    /**
     * 设置预览长宽比
     * @param aspectRatio
     */
    public void setAspectRatio(AspectRatio aspectRatio) {
        this.aspectRatio = aspectRatio;
        if (aspectRatio == AspectRatio.Ratio_16_9) {
            expectWidth = DEFAULT_16_9_WIDTH;
            expectHeight = DEFAULT_16_9_HEIGHT;
            currentRatio = Ratio_16_9;
        } else {
            expectWidth = DEFAULT_4_3_WIDTH;
            expectHeight = DEFAULT_4_3_HEIGHT;
            currentRatio = Ratio_4_3;
        }
    }

    /**
     * 设置对焦权重
     * @param focusWeight
     */
    public void setFocusWeight(int focusWeight) {
        if (focusWeight < 0 || focusWeight > MAX_FOCUS_WEIGHT) {
            throw new IllegalArgumentException("focusWeight must be 0 ~ 1000");
        }
        this.focusWeight = focusWeight;
    }

}
