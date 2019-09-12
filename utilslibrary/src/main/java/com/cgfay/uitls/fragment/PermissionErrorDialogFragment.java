package com.cgfay.uitls.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

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
        return new AlertDialog.Builder(activity)
                .setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mErrorForceClose) {
                            activity.finish();
                        }
                    }
                })
                .create();
    }

}
