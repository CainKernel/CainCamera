package com.cgfay.uitls.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.cgfay.uitls.dialog.DialogBuilder;
import com.cgfay.utilslibrary.R;

/**
 * 预览页面返回对话框
 */
public class BackPressedDialogFragment extends DialogFragment {

    public static final String MESSAGE = "message";

    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            mActivity = getActivity();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Fragment parent = getParentFragment();
        Bundle bundle = getArguments();
        int resId = -1;
        if (bundle != null) {
            resId = bundle.getInt(MESSAGE, -1);
        }
        return DialogBuilder.from(mActivity, R.layout.dialog_two_button)
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .setText(R.id.tv_dialog_title, resId == -1 ? R.string.back_pressed_message : resId)
                .setDismissOnClick(R.id.btn_dialog_cancel, true)
                .setText(R.id.btn_dialog_cancel, "取消")
                .setDismissOnClick(R.id.btn_dialog_ok, true)
                .setText(R.id.btn_dialog_ok, "确定")
                .setOnClickListener(R.id.btn_dialog_ok, v -> {
                    if (parent != null) {
                        Activity activity = parent.getActivity();
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                })
                .create();
    }
}
