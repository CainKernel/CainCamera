package com.cgfay.caincamera.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * 弹窗基类
 * Created by cain on 2017/12/15.
 */

public class BasePopupWindow extends PopupWindow {

    protected Context mContext;

    private float mShowAlpha = 0.88f;

    private Drawable mBackground;

    public BasePopupWindow(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setAnimationStyle(android.R.style.Animation_Dialog);
        setOutsideTouchable(true);
        setFocusable(true);
    }

    @Override
    public void setOutsideTouchable(boolean touchable) {
        super.setOutsideTouchable(touchable);
        if (touchable) {
            if (mBackground == null) {
                mBackground = new ColorDrawable(0x00000000);
            }
            setBackgroundDrawable(mBackground);
        } else {
            setBackgroundDrawable(null);
        }
    }

    /**
     * 设置背景
     * @param background
     */
    public void setBackground(Drawable background) {
        mBackground = background;
        setOutsideTouchable(isOutsideTouchable());
    }

    @Override
    public void setContentView(View contentView) {
        super.setContentView(contentView);
        if (contentView != null) {
            contentView.setFocusable(true);
            contentView.setFocusableInTouchMode(true);
            contentView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismiss();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        alphaAnimator(1.0f, mShowAlpha).start();
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        alphaAnimator(1.0f, mShowAlpha).start();
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);
        alphaAnimator(1.0f, mShowAlpha).start();
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        alphaAnimator(1.0f, mShowAlpha).start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        alphaAnimator(mShowAlpha, 1.0f).start();
    }

    /**
     * 透明渐变动画
     * @param startAlpha
     * @param endAlpha
     * @return
     */
    private ValueAnimator alphaAnimator(float startAlpha, float endAlpha) {
        ValueAnimator animator = ValueAnimator.ofFloat(startAlpha, endAlpha);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                setWindowBackgroundAlpha(alpha);
            }
        });
        animator.setDuration(300);
        return animator;
    }

    /**
     * 设置窗口渐变
     * @param alpha
     */
    private void setWindowBackgroundAlpha(float alpha) {
        Window window = ((Activity)mContext).getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.alpha = alpha;
        window.setAttributes(layoutParams);
    }

}
