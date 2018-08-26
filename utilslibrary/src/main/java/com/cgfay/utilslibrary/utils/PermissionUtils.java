package com.cgfay.utilslibrary.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by cain on 17-7-22.
 */

public final class PermissionUtils {

    // 请求相机权限
    public static final int REQUEST_CAMERA_PERMISSION = 0x01;
    // 请求存储权限
    public static final int REQUEST_STORAGE_PERMISSION = 0x02;
    // 请求声音权限
    public static final int REQUEST_SOUND_PERMISSION = 0x03;

    private PermissionUtils() {}

    /**
     * 检查某个权限是否授权
     * @param permission
     * @return
     */
    public static boolean permissionChecking(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
