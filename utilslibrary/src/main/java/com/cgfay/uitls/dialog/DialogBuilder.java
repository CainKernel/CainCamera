package com.cgfay.uitls.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cgfay.utilslibrary.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 静态对话框构建器
 */
public class DialogBuilder {

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, ResBinder> mResBinder = new HashMap<>();

    private final Context mContext;
    private final @LayoutRes int mLayout;
    private boolean mCancelable;
    private boolean mCancelOnTouchOutside;

    private DialogBuilder(@NonNull Context context, @LayoutRes int layout) {
        mContext = context;
        mLayout = layout;
        mCancelable = true;
        mCancelOnTouchOutside = true;
    }

    public static DialogBuilder from(@NonNull Context context, @LayoutRes int layout) {
        return new DialogBuilder(context, layout);
    }

    /**
     * 设置背景颜色
     * @param id
     * @param backgroundColor
     * @return
     */
    public DialogBuilder setBackgroundColor(@IdRes int id, @ColorInt int backgroundColor) {
        ResBinder binder = getResBinder(id);
        binder.setBackgroundColor(backgroundColor);
        return this;
    }

    /**
     * 设置Drawable
     * @param id
     * @param drawable
     * @return
     */
    public DialogBuilder setDrawable(@IdRes int id, @DrawableRes int drawable) {
        ResBinder binder = getResBinder(id);
        binder.setDrawable(drawable);
        return this;
    }

    /**
     * 设置文字
     * @param id
     * @param text
     * @return
     */
    public DialogBuilder setText(@IdRes int id, @Nullable String text) {
        ResBinder binder = getResBinder(id);
        binder.setText(text);
        return this;
    }

    /**
     * 设置文字
     * @param id
     * @param text
     * @return
     */
    public DialogBuilder setText(@IdRes int id, @StringRes int text) {
        ResBinder binder = getResBinder(id);
        binder.setText(mContext.getString(text));
        return this;
    }

    /**
     * 设置是否点击关闭对话框
     * @param id
     * @param dismissOnClick
     * @return
     */
    public DialogBuilder setDismissOnClick(@IdRes int id, boolean dismissOnClick) {
        ResBinder binder = getResBinder(id);
        binder.setDismissOnClick(dismissOnClick);
        return this;
    }

    /**
     * 设置点击监听器
     * @param id
     * @param listener
     * @return
     */
    public DialogBuilder setOnClickListener(@IdRes int id, View.OnClickListener listener) {
        ResBinder binder = getResBinder(id);
        binder.setOnClickListener(listener);
        return this;
    }

    /**
     * 设置是否允许取消
     * @param cancelable
     * @return
     */
    public DialogBuilder setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        return this;
    }

    /**
     * 设置点击对话框外区域取消
     * @param canceled
     * @return
     */
    public DialogBuilder setCanceledOnTouchOutside(boolean canceled) {
        mCancelOnTouchOutside = canceled;
        return this;
    }

    private @NonNull ResBinder getResBinder(@IdRes int id) {
        ResBinder binder = mResBinder.get(id);
        if (binder == null) {
            binder = new ResBinder(id);
            mResBinder.put(id, binder);
        }
        return binder;
    }

    /**
     * 创建一个对话框
     * @return
     */
    public Dialog create() {
        Dialog dialog = new Dialog(mContext, R.style.CommonDialogStyle);
        View view = LayoutInflater.from(mContext).inflate(mLayout, null);
        initView(dialog, view);
        dialog.setContentView(view);
        dialog.setCancelable(mCancelable);
        dialog.setCanceledOnTouchOutside(mCancelOnTouchOutside);
        return dialog;
    }

    /**
     * 显示对话框
     * @return
     */
    public Dialog show() {
        Dialog dialog = create();
        dialog.show();
        return dialog;
    }

    private void initView(@NonNull Dialog dialog, @NonNull View view) {
        for (Map.Entry<Integer, ResBinder> entry : mResBinder.entrySet()) {
            View resView = view.findViewById(entry.getKey());
            initResView(dialog, resView, entry.getValue());
        }
    }

    private void initResView(@NonNull Dialog dialog, @NonNull View resView, @NonNull ResBinder binder) {
        resView.setVisibility(View.VISIBLE);
        if (binder.getBackgroundColor() != 0) {
            resView.setBackgroundColor(binder.getBackgroundColor());
        }
        if (binder.getDrawable() != 0) {
            resView.setBackgroundResource(binder.getDrawable());
        }
        if (resView instanceof TextView && !TextUtils.isEmpty(binder.getText())) {
            ((TextView) resView).setText(binder.getText());
        }
        resView.setOnClickListener(v -> {
            if (binder.isDismissOnClick()) {
                dialog.dismiss();
            }
            if (binder.getClickListener() != null) {
                binder.getClickListener().onClick(v);
            }
        });
    }

    private static class ResBinder {

        private @IdRes int mId;

        private @ColorInt int mBackgroundColor;

        private @DrawableRes int mDrawable;

        private String mText;

        private boolean mDismissOnClick;

        private View.OnClickListener mClickListener;

        public ResBinder(@IdRes int id) {
            mId = id;
            mBackgroundColor = 0;
            mDrawable = 0;
            mText = null;
            mDismissOnClick = false;
            mClickListener = null;
        }

        public void setBackgroundColor(@ColorInt int backgroundColor) {
            mBackgroundColor = backgroundColor;
        }

        public @ColorInt int getBackgroundColor() {
            return mBackgroundColor;
        }

        public void setDrawable(@DrawableRes int id) {
            mDrawable = id;
        }

        public @DrawableRes int getDrawable() {
            return mDrawable;
        }

        public void setText(@Nullable String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }

        public void setDismissOnClick(boolean dismissOnClick) {
            mDismissOnClick = dismissOnClick;
        }

        public boolean isDismissOnClick() {
            return mDismissOnClick;
        }

        public void setOnClickListener(View.OnClickListener listener) {
            mClickListener = listener;
        }

        public View.OnClickListener getClickListener() {
            return mClickListener;
        }
    }
}
