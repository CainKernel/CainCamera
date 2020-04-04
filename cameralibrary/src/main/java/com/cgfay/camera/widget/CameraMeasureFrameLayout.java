package com.cgfay.camera.widget;

import android.content.Context;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by cain.huang on 2017/7/20.
 */

public class CameraMeasureFrameLayout extends FrameLayout {

    public CameraMeasureFrameLayout(@NonNull Context context) {
        super(context);
    }

    public CameraMeasureFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraMeasureFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 计算MeasureLayout
        if (mListener != null) {
            mListener.onMeasure(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        }
    }

    public interface OnMeasureListener {
        void onMeasure(int width, int height);
    }

    /**
     * 监听处理
     */
    public void setOnMeasureListener(OnMeasureListener listener) {
        mListener = listener;
    }

    private OnMeasureListener mListener;
}
