package com.cgfay.uitls.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.cgfay.uitls.utils.PermissionUtils;


/**
 * 运行时权限请求对话框
 */
public class PermissionConfirmDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";
    private static final String REQUEST_CODE = "requestCode";
    private static final String ERROR_CLOSE = "forceClose";

    private boolean mErrorForceClose = false;

    private int mRequestCode;


    public static PermissionConfirmDialogFragment newInstance(String message, int requestCode) {
        return newInstance(message, requestCode, false);
    }

    public static PermissionConfirmDialogFragment newInstance(String message, int requestCode, boolean errorForceClose) {
        PermissionConfirmDialogFragment dialog = new PermissionConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putInt(REQUEST_CODE, requestCode);
        args.putBoolean(ERROR_CLOSE, errorForceClose);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Fragment parent = getParentFragment();
        mRequestCode = getArguments().getInt(REQUEST_CODE);
        mErrorForceClose = getArguments().getBoolean(ERROR_CLOSE);
        return new AlertDialog.Builder(getActivity())
                .setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    if (mRequestCode == PermissionUtils.REQUEST_CAMERA_PERMISSION) {
                        parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                PermissionUtils.REQUEST_CAMERA_PERMISSION);
                    } else if (mRequestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
                        parent.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PermissionUtils.REQUEST_STORAGE_PERMISSION);
                    } else if (mRequestCode == PermissionUtils.REQUEST_SOUND_PERMISSION) {
                        parent.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                PermissionUtils.REQUEST_SOUND_PERMISSION);
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> {
                            dialog.dismiss();
                            if (mErrorForceClose) {
                                Activity activity = parent.getActivity();
                                if (activity != null) {
                                    activity.finish();
                                }
                            }
                        })
                .create();
    }
}