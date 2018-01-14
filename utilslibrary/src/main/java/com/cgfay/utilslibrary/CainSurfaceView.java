package com.cgfay.utilslibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by cain on 2017/7/9.
 */

public class CainSurfaceView extends SurfaceView {

    private static final String TAG = "CameraSurfaceView";

    // 触摸点击默认值，触摸差值小于该值算点击事件
    private final static float MAX_TOUCH_SIZE_FOR_CLICK = 15f;
    // 小于该时间间隔则表示双击
    private final static int MAX_DOUBLE_CLICK_INTERVAL = 200;
    // 点击时间有效时间
    private final static int MAX_CLICK_INTERVAL = 100;

    private float mTouchPreviewX = 0;
    private float mTouchPreviewY = 0;
    private float mTouchUpX = 0;
    private float mTouchUpY = 0;

    private final Object mOperation = new Object();
    private boolean mTouchWithoutSwipe = false;

    // 对焦动画
    private ValueAnimator mFocusAnimator;
    // 对焦图片
    private ImageView mFocusImageView;

    private OnTouchScroller mScroller;
    private OnClickListener mClickListener;

    public CainSurfaceView(Context context) {
        super(context);
        init();
    }

    public CainSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CainSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    private boolean mIsWaitUpEvent = false;
    private boolean mIsWaitDoubleClick = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchPreviewX = event.getX();
                mTouchPreviewY = event.getY();
                mIsWaitUpEvent = true;
                postDelayed(mTimerForUpEvent, MAX_CLICK_INTERVAL);

                //判断是否支持对焦模式
                if (CameraUtils.getCamera()!=null) {
                    List<String> focusModes = CameraUtils.getCamera()
                            .getParameters().getSupportedFocusModes();

                    if (focusModes != null
                            && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        addFocusView();
                    }
                }

                break;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - mTouchPreviewX) > MAX_TOUCH_SIZE_FOR_CLICK
                        || Math.abs(mTouchPreviewY) > MAX_TOUCH_SIZE_FOR_CLICK) {
                    mIsWaitUpEvent = false;
                    removeCallbacks(mTimerForUpEvent);
                }
                break;

            case MotionEvent.ACTION_UP:
                mTouchUpX = event.getX();
                mTouchUpY = event.getY();
                mIsWaitUpEvent = false;
                removeCallbacks(mTimerForUpEvent);
                if (Math.abs(event.getX() - mTouchPreviewX) < MAX_TOUCH_SIZE_FOR_CLICK
                        && Math.abs(event.getY() - mTouchPreviewY) < MAX_TOUCH_SIZE_FOR_CLICK) {
                    synchronized (mOperation) {
                        mTouchWithoutSwipe = true;
                    }
                    processClickEvent();
                    synchronized (mOperation) {
                        mTouchWithoutSwipe = false;
                    }
                } else {
                    synchronized (mOperation) {
                        if (!mTouchWithoutSwipe) {
                            processTouchMovingEvent(event);
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                mIsWaitUpEvent = false;
                removeCallbacks(mTimerForUpEvent);
                break;
        }

        return true;
    }

    /**
     * 等待松手
     */
    private Runnable mTimerForUpEvent = new Runnable() {
        @Override
        public void run() {
            if (mIsWaitUpEvent) {
                mIsWaitUpEvent = false;
            } else {

            }
        }
    };

    /**
     * 等待是否存在第二次点击事件
     */
    private Runnable mTimerForSecondClick = new Runnable() {
        @Override
        public void run() {
            if (mIsWaitDoubleClick) {
                mIsWaitDoubleClick = false;
                if (mClickListener != null) {
                    mClickListener.onClick(mTouchUpX, mTouchUpY);
                }
            }
        }
    };

    /**
     * 点击事件
     */
    private void processClickEvent() {
        if (mIsWaitDoubleClick) {
            if (mClickListener != null) {
                mClickListener.doubleClick(mTouchUpX, mTouchUpY);
            }
            mIsWaitDoubleClick = false;
            removeCallbacks(mTimerForSecondClick);
        } else {
            mIsWaitDoubleClick = true;
            postDelayed(mTimerForSecondClick, MAX_DOUBLE_CLICK_INTERVAL);
        }
    }

    /**
     * 触摸滑动事件
     * @param event
     */
    private void processTouchMovingEvent(MotionEvent event) {
        if (Math.abs(event.getX() - mTouchPreviewX)
                > Math.abs(event.getY() - mTouchPreviewY) * 1.5) {
            if (event.getX() - mTouchPreviewX < 0) {
                if (mScroller != null) {
                    mScroller.swipeBack();
                }
            } else {
                if (mScroller != null) {
                    mScroller.swipeFrontal();
                }
            }
        } else if (Math.abs(event.getY() - mTouchPreviewY)
                > Math.abs(event.getX() - mTouchPreviewX) * 1.5) {
            if (event.getY() - mTouchPreviewY < 0) {
                if (mScroller != null) {
                    if (mTouchPreviewX < getWidth() / 2) {
                        mScroller.swipeUpper(true);
                    } else {
                        mScroller.swipeUpper(false);
                    }
                }
            } else {
                if (mScroller != null) {
                    if (mTouchPreviewX < getWidth() / 2) {
                        mScroller.swipeDown(true);
                    } else {
                        mScroller.swipeDown(false);
                    }
                }
            }
        }
    }

    /**
     * 添加对焦图片
     */
    private void addFocusView() {

        if (mFocusAnimator == null) {
            mFocusImageView = new ImageView(getContext());
            mFocusImageView.setImageResource(R.drawable.video_focus);
            mFocusImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mFocusImageView.measure(0, 0);
            mFocusImageView.setX(mTouchPreviewX - mFocusImageView.getMeasuredWidth() / 2);
            mFocusImageView.setY(mTouchPreviewY - mFocusImageView.getMeasuredHeight() / 2);
            final ViewGroup parent = (ViewGroup) getParent();
            parent.addView(mFocusImageView);

            mFocusAnimator = ValueAnimator.ofFloat(0, 1).setDuration(500);
            mFocusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if(mFocusImageView != null) {
                        float value = (float) animation.getAnimatedValue();
                        if (value <= 0.5f) {
                            mFocusImageView.setScaleX(1 + value);
                            mFocusImageView.setScaleY(1 + value);
                        } else {
                            mFocusImageView.setScaleX(2 - value);
                            mFocusImageView.setScaleY(2 - value);
                        }
                    }
                }
            });
            mFocusAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if(mFocusImageView != null) {
                        parent.removeView(mFocusImageView);
                        mFocusAnimator = null;
                    }
                }
            });
            mFocusAnimator.start();
        }
    }


    /**
     * 添加滑动回调
     * @param scroller
     */
    public void addScroller(OnTouchScroller scroller) {
        mScroller = scroller;
    }

    /**
     * 滑动监听器
     */
    public interface OnTouchScroller {
        void swipeBack();
        void swipeFrontal();
        void swipeUpper(boolean startInLeft);
        void swipeDown(boolean startInLeft);
    }

    /**
     * 添加点击事件回调
     * @param listener
     */
    public void addClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    /**
     * 点击事件监听器
     */
    public interface OnClickListener {
        void onClick(float x, float y);
        void doubleClick(float x, float y);
    }
}