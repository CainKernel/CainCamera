package com.cgfay.cameralibrary.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;

/**
 * 视频合并过程对话框
 */
public class CombineVideoDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";
    private static final String KEY_DIMABLE = "dimable";

    private TextView mMessageView;

    public static CombineVideoDialogFragment newInstance(String message) {
        return newInstance(message, false);
    }

    public static CombineVideoDialogFragment newInstance(String message, boolean dimable) {
        CombineVideoDialogFragment dialogFragment = new CombineVideoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(KEY_DIMABLE, dimable);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Fragment parent = getParentFragment();
        String message = getArguments().getString(ARG_MESSAGE);
        boolean dimable = getArguments().getBoolean(KEY_DIMABLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        View view = View.inflate(getActivity(), R.layout.view_preview_combine_loading, null);
        builder.setView(view);
        ProgressBar pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        mMessageView = (TextView) view.findViewById(R.id.tv_hint);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pb_loading.setIndeterminateTintList(
                    ContextCompat.getColorStateList(getActivity(), R.color.blue));
        }
        mMessageView.setText(message);
        Dialog dialog = builder.create();
        dialog.setCancelable(dimable);
        dialog.setCanceledOnTouchOutside(dimable);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    /**
     * 设置进度信息
     * @param message
     */
    public void setProgressMessage(String message) {
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }
}
