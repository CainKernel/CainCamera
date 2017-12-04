package com.cgfay.caincamera.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.cgfay.caincamera.R;

/**
 * 拍照或录制视频按钮
 * Created by cain.huang on 2017/12/4.
 */

public class PictureVideoActionButton extends View {

    private static String TAG = "PictureVideoActionButton";

    // 默认动作时间500毫秒
    private static final int TIME_MSEC = 500;

    // 色彩
    private int mColorNormal, mColorPressed, mColorRecording, mColorWindowBackground;

    private Paint mPaint;

    private final int TOUCH_IN_RECORD = 1;    // 进入录制
    private final int TOUCH_IN_UNRECORD = 2;  // 退出录制
    private final int TOUCH_OUT_RECORD = 3;   // 处于录制状态
    private final int TOUCH_OUT_UNRECORD = 4; // 处于非录制状态
    private final int UNABLE_RECORD = 5;      // 允许录制

    // 默认情况下处于非录制状态
    private int status = TOUCH_OUT_UNRECORD;

    private long actionTime;
    private ActionListener mActionListener;

    // 判断录制视频还是拍照
    private boolean mIsRecord = false;

    public PictureVideoActionButton(Context context) {
        super(context);
        init();
    }

    public PictureVideoActionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PictureVideoActionButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mColorNormal = getContext().getResources().getColor(
                R.color.green);
        mColorPressed = getContext().getResources().getColor(R.color.yellow);
        mColorRecording = getContext().getResources().getColor(R.color.red);
        mColorWindowBackground = getContext().getResources().getColor(R.color.video_window_background);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = getWidth();
        final int height = getHeight();

        int length = width;
        if (width > height) {
            length = height;
        }


        int centerColor = 0;
        int outsideColor = 0;

        if (status == TOUCH_OUT_UNRECORD ||
                status == UNABLE_RECORD ||
                status == TOUCH_IN_UNRECORD) {
            centerColor = mColorNormal;
            outsideColor = mColorNormal;
        } else if (status == TOUCH_IN_RECORD) {
            centerColor = mColorPressed;
            outsideColor = mColorPressed;
        } else if (status == TOUCH_OUT_RECORD) {
            centerColor = mColorRecording;
            outsideColor = mColorRecording;
        }

        mPaint.setColor(outsideColor);
        canvas.drawCircle(width / 2, height / 2, length / 2, mPaint);

        mPaint.setColor(mColorWindowBackground);
        canvas.drawCircle(width / 2, height / 2, length / 2 * 0.9f, mPaint);

        mPaint.setColor(centerColor);
        canvas.drawCircle(width / 2, height / 2, length / 2 * 0.8f, mPaint);

        if (status == TOUCH_OUT_RECORD) {
            mPaint.setColor(0xffffffff);
            canvas.drawRect(length * 0.35f, length * 0.3f, length * 0.45f, length * 0.7f, mPaint);
            canvas.drawRect(length * 0.55f, length * 0.3f, length * 0.65f, length * 0.7f, mPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 如果只是拍照，则录制视频不需要处理触摸事件
        if (!mIsRecord) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (status == TOUCH_OUT_UNRECORD) {
                    actionTime = AnimationUtils.currentAnimationTimeMillis();
                    status = TOUCH_IN_RECORD;
                    if (mActionListener != null && mActionListener.enableChangeState()) {
                        mActionListener.startRecord();
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (status == TOUCH_IN_RECORD) {
                    long stopTime = AnimationUtils.currentAnimationTimeMillis();
                    if (stopTime - actionTime < TIME_MSEC) {
                        status = TOUCH_OUT_RECORD;
                    } else {
                        status = TOUCH_OUT_UNRECORD;
                        if (mActionListener != null && mActionListener.enableChangeState()) {
                            mActionListener.stopRecord();
                        }
                    }
                    invalidate();
                } else if (status == TOUCH_OUT_RECORD) {
                    long stopTime = AnimationUtils.currentAnimationTimeMillis();
                    if (stopTime - actionTime > TIME_MSEC) {
                        status = TOUCH_OUT_UNRECORD;
                        if (mActionListener != null && mActionListener.enableChangeState()) {
                            mActionListener.stopRecord();
                        }
                        invalidate();
                    }
                } else if (status == TOUCH_IN_UNRECORD) {
                    status = TOUCH_OUT_UNRECORD;
                }
                break;
        }
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            status = TOUCH_OUT_UNRECORD;
        } else {
            status = UNABLE_RECORD;
        }
        invalidate();
    }

    /**
     * 设置监听器
     * @param actionListener
     */
    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    /**
     * 开始失败
     */
    public void startFailed() {
        if (status == TOUCH_IN_RECORD) {
            status = TOUCH_IN_UNRECORD;
        } else if (status == TOUCH_OUT_RECORD) {
            status = TOUCH_OUT_UNRECORD;
        }
        invalidate();
    }

    /**
     * 设置是否录制视频
     * @param enable
     */
    public void setIsRecorder(boolean enable) {
        mIsRecord = enable;
    }


    public void resetStatus() {
        status = TOUCH_OUT_UNRECORD;
        invalidate();
    }

    /**
     * 回调监听
     */
    public interface ActionListener {

        // 开始录制
        void startRecord();

        // 停止录制
        void stopRecord();

        // 是否允许改变状态
        boolean enableChangeState();
    }
}
