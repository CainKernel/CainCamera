package com.cgfay.uitls.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.cgfay.uitls.dialog.DialogBuilder;
import com.cgfay.utilslibrary.R;

/**
 * 权限出错对话框
 */
public class PermissionErrorDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";
    private static final String REQUEST_CODE = "requestCode";
    private static final String ERROR_CLOSE = "forceClose";

    private boolean mErrorForceClose = false;
    private int mRequestCode;

    public static PermissionErrorDialogFragment newInstance(String message, int requestCode) {
        return newInstance(message, requestCode, true);
    }

    public static PermissionErrorDialogFragment newInstance(String message, int requestCode, boolean errorForceClose) {
        PermissionErrorDialogFragment dialog = new PermissionErrorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putInt(REQUEST_CODE, requestCode);
        args.putBoolean(ERROR_CLOSE, errorForceClose);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        mRequestCode = getArguments().getInt(REQUEST_CODE);
        mErrorForceClose = getArguments().getBoolean(ERROR_CLOSE);
        return DialogBuilder.from(activity, R.layout.dialog_two_button)
                .setText(R.id.tv_dialog_title, getArguments().getString(ARG_MESSAGE))
                .setText(R.id.btn_dialog_cancel, "取消")
                .setDismissOnClick(R.id.btn_dialog_cancel, true)
                .setText(R.id.btn_dialog_ok, "确定")
                .setOnClickListener(R.id.btn_dialog_ok, v -> {
                    if (mErrorForceClose) {
                        activity.finish();
                    }
                })
                .create();
    }

}
