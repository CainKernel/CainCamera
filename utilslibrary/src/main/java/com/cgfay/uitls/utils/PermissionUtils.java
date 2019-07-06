package com.cgfay.uitls.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.cgfay.uitls.fragment.PermissionConfirmDialogFragment;
import com.cgfay.utilslibrary.R;

/**
 * Created by cain on 17-7-22.
 */

public final class PermissionUtils {

    private static final String FRAGMENT_DIALOG = "dialog";

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

    /**
     * 请求相机权限
     * @param fragment
     */
    public static void requestCameraPermission(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            PermissionConfirmDialogFragment.newInstance(fragment.getString(R.string.request_camera_permission), com.cgfay.uitls.utils.PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                    .show(fragment.getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            fragment.requestPermissions(new String[]{ Manifest.permission.CAMERA},
                    com.cgfay.uitls.utils.PermissionUtils.REQUEST_CAMERA_PERMISSION);
        }
    }

    /**
     * 请求存储权限
     */
    public static void requestStoragePermission(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(fragment.getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                    .show(fragment.getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            fragment.requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtils.REQUEST_STORAGE_PERMISSION);
        }
    }

    /**
     * 请求录音权限
     */
    public static void requestRecordSoundPermission(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            PermissionConfirmDialogFragment.newInstance(fragment.getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                    .show(fragment.getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            fragment.requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO},
                    PermissionUtils.REQUEST_SOUND_PERMISSION);
        }
    }
}
