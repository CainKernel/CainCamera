package com.cgfay.medialibrary.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class MediaStoreUtils {

    /**
     * 是否存在相机
     * @param context
     * @return
     */
    public static boolean hasCameraFeature(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}
