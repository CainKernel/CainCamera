package com.cgfay.caincamera.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 长按录制按钮
 * @author CainHuang
 * @date 2019/7/7
 */
@SuppressLint("AppCompatCustomView")
public class RecordButton extends TextView {

    private static final int BACKGROUND_COLOR = Color.parseColor("#fe2c55");

    private int mBackgroundColor = BACKGROUND_COLOR;

    private Paint mPaint;

    private OnRecordListener mListener;

    private boolean mEnable;

    private Handler mMainHandler;

    public RecordButton(Context context) {
        super(context);
        init();
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackground(null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBackgroundColor);
        mEnable = true;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2,
                getMeasuredWidth() / 2, mPaint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mListener == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mEnable = false;
                mListener.onRecordStart();
                mMainHandler.postDelayed(()-> {
                    mEnable = true;
                    setEnabled(true);
                }, 1000);
                setEnabled(false);
                startAnimation();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mEnable) {
                    mMainHandler.post(()-> {
                        mListener.onRecordStop();
                    });
                } else {
                    mMainHandler.postDelayed(()-> {
                        mListener.onRecordStop();
                    }, 1000);
                }
                stopAnimation();
                break;
        }
        return true;
    }

    private void startAnimation() {

    }

    private void stopAnimation() {

    }

    public void setOnRecordListener(OnRecordListener listener) {
        mListener = listener;
    }

    public interface OnRecordListener {
        void onRecordStart();

        void onRecordStop();
    }
}