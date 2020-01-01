package com.cgfay.uitls.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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
    public static boolean permissionChecking(@NonNull Context context, @NonNull String permission) {
        int targetVersion = 1;
        try {
            final PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            targetVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {

        }
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && targetVersion >= Build.VERSION_CODES.M) {
            result = (ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_GRANTED);
        } else {
            result = (PermissionChecker.checkSelfPermission(context, permission)
                    == PermissionChecker.PERMISSION_GRANTED);
        }
        return result;
    }

    /**
     * 检查某个权限是否授权
     * @param fragment
     * @param permission
     * @return
     */
    public static boolean permissionChecking(@NonNull Fragment fragment, @NonNull String permission) {
        if (fragment.getContext() != null) {
            return permissionChecking(fragment.getContext(), permission);
        }
        return false;
    }

    /**
     * 检查权限列表是否授权
     * @param context
     * @param permissions
     * @return
     */
    public static boolean permissionsChecking(@NonNull Context context, @NonNull String[] permissions) {
        int targetVersion = 1;
        try {
            final PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            targetVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {

        }

        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && targetVersion >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                result = (ContextCompat.checkSelfPermission(context, permissions[i])
                        == PackageManager.PERMISSION_GRANTED);
                if (!result) {
                    break;
                }
            }
        } else {
            for (int i = 0; i < permissions.length; i++) {
                result = (PermissionChecker.checkSelfPermission(context, permissions[i])
                        == PermissionChecker.PERMISSION_GRANTED);
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 检查权限列表是否授权
     * @param fragment
     * @param permissions
     * @return
     */
    public static boolean permissionsChecking(@NonNull Fragment fragment, @NonNull String[] permissions) {
        if (fragment.getContext() == null) {
            return false;
        }
        return permissionsChecking(fragment.getContext(), permissions);
    }

    /**
     * 请求相机权限
     * @param fragment
     */
    public static void requestCameraPermission(@NonNull Fragment fragment) {
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            PermissionConfirmDialogFragment.newInstance(fragment.getString(R.string.request_camera_permission),
                    PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                    .show(fragment.getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            fragment.requestPermissions(new String[]{ Manifest.permission.CAMERA},
                    com.cgfay.uitls.utils.PermissionUtils.REQUEST_CAMERA_PERMISSION);
        }
    }

    /**
     * 请求相机权限
     * @param activity
     */
    public static void requestCameraPermission(@NonNull FragmentActivity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            PermissionConfirmDialogFragment.newInstance(activity.getString(R.string.request_camera_permission),
                    PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                    .show(activity.getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.CAMERA},
                    com.cgfay.uitls.utils.PermissionUtils.REQUEST_CAMERA_PERMISSION);
        }
    }

    /**
     * 请求存储权限
     */
    public static void requestStoragePermission(@NonNull Fragment fragment) {
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(fragment.getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                    .show(fragment.getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            fragment.requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtils.REQUEST_STORAGE_PERMISSION);
        }
    }

    /**
     * 请求存储权限
     * @param activity
     */
    public static void requestStoragePermission(@NonNull FragmentActivity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(activity.getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                    .show(activity.getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtils.REQUEST_STORAGE_PERMISSION);
        }
    }

    /**
     * 请求录音权限
     */
    public static void requestRecordSoundPermission(@NonNull Fragment fragment) {
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            PermissionConfirmDialogFragment.newInstance(fragment.getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                    .show(fragment.getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            fragment.requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO},
                    PermissionUtils.REQUEST_SOUND_PERMISSION);
        }
    }

    /**
     * 请求录音权限
     */
    public static void requestRecordSoundPermission(FragmentActivity activity) {
        if (activity == null) {
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
            PermissionConfirmDialogFragment.newInstance(activity.getString(R.string.request_sound_permission),
                    PermissionUtils.REQUEST_SOUND_PERMISSION)
                    .show(activity.getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{ Manifest.permission.RECORD_AUDIO},
                    PermissionUtils.REQUEST_SOUND_PERMISSION);
        }
    }

    /**
     * 请求权限列表
     * @param fragment
     * @param permissions
     * @param request_code
     */
    public static void requestPermissions(@NonNull Fragment fragment, @NonNull String[] permissions, int request_code) {
        boolean hasPermissions = permissionsChecking(fragment, permissions);
        if (!hasPermissions) {
            fragment.requestPermissions(permissions, request_code);
        }
    }

    /**
     * 请求权限列表
     * @param activity
     * @param permissions
     */
    public static void requestPermissions(@NonNull FragmentActivity activity, @NonNull String[] permissions, int request_code) {
        boolean hasPermissions = permissionsChecking(activity, permissions);
        if (!hasPermissions) {
            ActivityCompat.requestPermissions(activity, permissions, request_code);
        }
    }

    /**
     * 打开权限设置页面
     * @param fragment
     */
    public static void launchPermissionSettings(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            return;
        }
        launchPermissionSettings(fragment.getActivity());
    }

    /**
     * 打开权限设置页面
     * @param activity
     */
    public static void launchPermissionSettings(@NonNull Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
