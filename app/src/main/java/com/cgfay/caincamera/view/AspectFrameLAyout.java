package com.cgfay.caincamera.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by cain.huang on 2017/7/20.
 */

public class AspectFrameLAyout extends FrameLayout {
    // 触摸点击默认值，触摸差值小于该值算点击事件
    private final static float TOUCH_SIZE = 7.5f;

    // 宽高比
    private double mTargetAspect = -1.0;

    private float mTouchPreviewX = 0;
    private float mTouchPreviewY = 0;



    public AspectFrameLAyout(@NonNull Context context) {
        super(context);
    }

    public AspectFrameLAyout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectFrameLAyout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(double aspectRatio) {
        if (aspectRatio < 0) {
            throw  new IllegalArgumentException("ratio < 0");
        }
        if (mTargetAspect != aspectRatio) {
            mTargetAspect = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mTargetAspect > 0) {
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            int horizPadding = getPaddingLeft() + getPaddingRight();
            int vertPadding = getPaddingTop() + getPaddingBottom();

            initialWidth -= horizPadding;
            initialHeight -= vertPadding;

            double viewAspectRatio = (double) initialWidth / initialHeight;
            double aspectDiff = mTargetAspect / viewAspectRatio - 1;

            if (Math.abs(aspectDiff) >= 0.01) {
                if (aspectDiff > 0) {
                    initialHeight = (int)(initialWidth / mTargetAspect);
                } else {
                    initialWidth = (int)(initialHeight * mTargetAspect);
                }
            }
            initialWidth += horizPadding;
            initialHeight += vertPadding;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchPreviewX = event.getX();
                mTouchPreviewY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                if (Math.abs(event.getX() - mTouchPreviewX) < TOUCH_SIZE
                        && Math.abs(event.getY() - mTouchPreviewY) < TOUCH_SIZE) {
                    processClickEvent(event);
                } else {
                    processTouchMovingEvent(event);
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 点击事件
     * @param event
     */
    private void processClickEvent(MotionEvent event) {

    }

    /**
     * 触摸滑动事件
     * @param event
     */
    private void processTouchMovingEvent(MotionEvent event) {

    }
}
