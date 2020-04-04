package com.cgfay.camera.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cgfay.uitls.utils.DisplayUtils;
import com.cgfay.uitls.utils.SystemUtils;

/**
 * 判断是否可用Camera2接口，也就是进而判断是否使用CameraX相机库
 */
public final class CameraApi {

    private static final String TAG = "CameraApi";

    private CameraApi() {
        
    }

    /**
     * 判断能否使用Camera2 的API
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasCamera2(Context context) {
        if (context == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            assert manager != null;
            String[] idList = manager.getCameraIdList();
            boolean notNull = true;
            if (idList.length == 0) {
                notNull = false;
            } else {
                for (final String str : idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notNull = false;
                        break;
                    }
                    final CameraCharacteristics characteristics = manager.getCameraCharacteristics(str);

                    Integer iSupportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (iSupportLevel != null
                            && (iSupportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                            || iSupportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED)) {
                        notNull = false;
                        break;
                    }
                }
            }
            return notNull;
        } catch (Throwable ignore) {
            return false;
        }
    }

    /**
     * 判断是否存在前置摄像头
     * @param context
     * @return
     */
    public static boolean hasFrontCamera(@NonNull Context context) {
        String brand = SystemUtils.getDeviceBrand();
        String model = SystemUtils.getSystemModel();
        // 华为折叠屏手机判断是否处于展开状态
        if (brand.contains("HUAWEI") && model.contains("TAH-")) {
            int width = DisplayUtils.getDisplayWidth(context);
            int height = DisplayUtils.getDisplayHeight(context);
            if (width < 0 || height < 0) {
                return true;
            }
            if (width < height) {
                int temp = width;
                width = height;
                height = temp;
            }
            Log.d(TAG, "hasFrontCamera: " + model + ", width = " + width + ", height = " + height);
            if (width * 1.0f / height <= 4.0 / 3.0) {
                return false;
            }
        }
        return true;
    }
}
