package com.cgfay.caincamera.core;

import android.content.Context;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.utils.faceplus.ConUtil;
import com.megvii.facepp.sdk.Facepp;

import java.lang.ref.WeakReference;

/**
 * Face++ SDK的管理器
 * Created by cain on 17-7-31.
 */
public class FacePlusManager {

    // 最小识别大小
    private static final int min_face_size = 200;
    // 识别时间间隔
    private static final int detection_interval = 25;

    private static FacePlusManager mInstance;

    private WeakReference<Context> mWeakReferenceContext;

    private Facepp facepp;

    // 是否只识别一个人脸
    private boolean isOneFaceTracking = false;

    private FacePlusManager() {}

    public static FacePlusManager getInstance() {
        if (mInstance == null) {
            mInstance = new FacePlusManager();
        }
        return mInstance;
    }

    /**
     * 初始化Face++配置
     * @param context 上下文
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void initFacePlus(Context context, int left, int top, int right, int bottom) {
        mWeakReferenceContext = new WeakReference<Context>(context);
        facepp.init(context, ConUtil.getFileContent(context, R.raw.megviifacepp_0_4_7_model));
        updateFacePlusSetting(left, top, right, bottom);
    }

    /**
     * 更新Face++的配置
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void updateFacePlusSetting(int left, int top, int right, int bottom) {
        Context context = mWeakReferenceContext.get();
        if (context != null) {
            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
            faceppConfig.interval = detection_interval;
            faceppConfig.minFaceSize = min_face_size;
            faceppConfig.roi_left = left;
            faceppConfig.roi_top = top;
            faceppConfig.roi_right = right;
            faceppConfig.roi_bottom = bottom;
            if (isOneFaceTracking) {
                faceppConfig.one_face_tracking = 1;
            } else {
                faceppConfig.one_face_tracking = 0;
            }
            // 设置人脸检测模式
            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING;
//        String[] array = context.getResources().getStringArray(R.array.trackig_mode_array);
//        if (trackModel.equals(array[0]))
//            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING;
//        else if (trackModel.equals(array[1]))
//            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_ROBUST;
//        else if (trackModel.equals(array[2]))
//            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST;

            facepp.setFaceppConfig(faceppConfig);
        }
    }
}
