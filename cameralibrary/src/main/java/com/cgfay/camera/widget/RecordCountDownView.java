package com.cgfay.camera.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cgfay.cameralibrary.R;

import java.lang.ref.WeakReference;

/**
 * 录制倒计时显示控件
 */
public class RecordCountDownView extends AppCompatTextView {

    private static final int COUNT_DOWN = 0x001;

    private static final int COUNT_DURATION = 1000;
    private int mCountDown = 3;

    // 是否处于倒计时阶段
    private volatile boolean isCountDowning;

    private OnCountDownListener mListener;
    private final CountDownHandler mHandler;

    public RecordCountDownView(Context context) {
        this(context, null);
    }

    public RecordCountDownView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordCountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
        mHandler = new CountDownHandler(this);
    }

    /**
     * 设置倒计时时长
     * @param countDown
     */
    public void setCountDown(int countDown) {
        mCountDown = countDown;
    }

    /**
     * 取消倒计时
     */
    public void cancel() {
        mHandler.removeCallbacksAndMessages(null);
        setText("");
        if (mListener != null) {
            mListener.onCountDownCancel();
        }
        isCountDowning = false;
    }

    /**
     * 开始倒计时
     */
    public void start() {
        isCountDowning = true;
        mHandler.sendMessage(mHandler.obtainMessage(COUNT_DOWN, mCountDown, 0));
    }

    /**
     * 判断是否正处于倒计时阶段
     * @return
     */
    public boolean isCountDowning() {
        return isCountDowning;
    }

    /**
     * 更新文字的动画
     */
    private void updateTextAnimate() {
        Animation enterAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_fade_in);
        startAnimation(enterAnimation);
    }

    public interface OnCountDownListener {

        void onCountDownEnd();

        void onCountDownCancel();
    }

    public void addOnCountDownListener(OnCountDownListener listener) {
        mListener = listener;
    }

    private static class CountDownHandler extends Handler {

        private final WeakReference<RecordCountDownView> mWeakCountDownView;

        public CountDownHandler(@NonNull RecordCountDownView countDownView) {
            mWeakCountDownView = new WeakReference<>(countDownView);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakCountDownView.get() == null) {
                return;
            }
            RecordCountDownView countDownView = mWeakCountDownView.get();
            switch (msg.what) {
                case COUNT_DOWN:
                    if (msg.arg1 > 0) {
                        countDownView.setText(String.valueOf(msg.arg1));
                        countDownView.updateTextAnimate();
                        sendMessageDelayed(obtainMessage(COUNT_DOWN, msg.arg1 - 1, 0), COUNT_DURATION);
                    } else {
                        countDownView.setText("");
                        if (countDownView.mListener != null) {
                            countDownView.mListener.onCountDownEnd();
                        }
                        countDownView.isCountDowning = false;
                    }
                    break;
            }
        }
    }
}
