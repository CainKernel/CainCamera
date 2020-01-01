package com.cgfay.camera.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.cameralibrary.R;
import com.cgfay.uitls.utils.DensityUtils;

/**
 * 录制按钮
 * @author CainHuang
 * @date 2019/12/14
 */
public class RecordButton extends View {

    // 触摸监听时长
    private static final int TOUCH_DURATION = 200;
    // 动画时长
    private static final int ANIMATION_MIN = 500;
    private static final int ANIMATION_MAX = 1200;

    // 默认边宽最大最小的dp值
    private static final int StrokeWidthMin = 3;
    private static final int StrokeWidthMax = 12;

    // 圆形与圆环之间的透明分割颜色
    private static final int mClipColor = Color.parseColor("#000000");

    private Context mContext;

    // 中间的矩形
    private Paint mRectPaint;
    // 中间圆形(矩形)的默认颜色
    private int mCircleColor = Color.WHITE;

    // 绘制圆环
    private Paint mCirclePaint;
    // 圆环默认颜色
    private int mStrokeColor = Color.parseColor("#33ffffff");

    private float mCorner;
    private float mCircleRadius;
    private float mCircleStrokeWidth;
    private float mRectWidth;

    private float mMinCircleRadius;
    private float mMaxCircleRadius;
    private float mMinRectWidth;
    private float mMaxRectWidth;
    private float mMinCorner;
    private float mMaxCorner;
    private float mMinCircleStrokeWidth;
    private float mMaxCircleStrokeWidth;

    private RectF mRectF = new RectF();

    private RecordMode mRecordMode = RecordMode.IDLE;

    private AnimatorSet mStartAnimatorSet = new AnimatorSet();

    private AnimatorSet mStopAnimatorSet = new AnimatorSet();

    private Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

    private Handler mHandler = new Handler();

    private TouchRunnable mTouchRunnable = new TouchRunnable();

    private RecordStateListener mRecordStateListener;

    private float mInitX;

    private float mInitY;

    private float mDownRawX;

    private float mDownRawY;

    private float mInfectionPoint;

    private SwipeDirection mSwipeDirection;

    private boolean mHasCancel = false;

    // 是否允许录制
    private boolean mRecordEnable;

    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mRecordEnable = true;
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RecordButton);
        try {
            mMinCircleStrokeWidth = DensityUtils.dp2px(context, StrokeWidthMin);
            mMinCircleStrokeWidth = ta.getDimension(R.styleable.RecordButton_circleStrokeWidthMin, mMinCircleStrokeWidth);
            mMaxCircleStrokeWidth = DensityUtils.dp2px(context, StrokeWidthMax);
            mMaxCircleStrokeWidth = ta.getDimension(R.styleable.RecordButton_circleStrokeWidthMax, mMaxCircleStrokeWidth);
            mCircleStrokeWidth = mMinCircleStrokeWidth;
            mCircleColor = ta.getColor(R.styleable.RecordButton_circleColor, mCircleColor);
            mStrokeColor = ta.getColor(R.styleable.RecordButton_strokeColor, mStrokeColor);
            mMaxRectWidth = ta.getDimension(R.styleable.RecordButton_rectWidthMax, mMaxRectWidth);
            mMinRectWidth = ta.getDimension(R.styleable.RecordButton_rectWidthMin, mMinRectWidth);
            mMinCorner = DensityUtils.dp2px(context, 5);
            mMinCorner = ta.getDimension(R.styleable.RecordButton_rectCorner, mMinCorner);
        } finally {
            ta.recycle();
        }

        setLayerType(LAYER_TYPE_HARDWARE, null);
        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setColor(mCircleColor);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mStrokeColor);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int centerX = width / 2;
        int centerY = height / 2;

        if (mMaxRectWidth == 0) {
            mMaxRectWidth = width / 3;
        }
        if (mMinRectWidth == 0) {
            mMinRectWidth = mMaxRectWidth * 0.6f;
        }
        mMinCircleRadius = mMaxRectWidth / 2 + mMinCircleStrokeWidth + DensityUtils.dp2px(mContext, 5);
        mMaxCircleRadius = width / 2f - mMaxCircleStrokeWidth;

        mMaxCorner = mMaxRectWidth / 2;

        if (mRectWidth == 0) {
            mRectWidth = mMaxRectWidth;
        }
        if (mCircleRadius == 0) {
            mCircleRadius = mMinCircleRadius;
        }
        if (mCorner == 0) {
            mCorner = mRectWidth / 2;
        }

        // 绘制圆环部分
        mCirclePaint.setColor(mStrokeColor);
        canvas.drawCircle(centerX, centerY, mCircleRadius, mCirclePaint);
        mCirclePaint.setXfermode(mXfermode);

        // 透明区域
        mCirclePaint.setColor(mClipColor);
        canvas.drawCircle(centerX, centerY, mCircleRadius - mCircleStrokeWidth, mCirclePaint);
        mCirclePaint.setXfermode(null);

        mRectF.left = centerX - mRectWidth / 2;
        mRectF.right = centerX + mRectWidth / 2;
        mRectF.top = centerY - mRectWidth / 2;
        mRectF.bottom = centerY + mRectWidth / 2;
        canvas.drawRoundRect(mRectF, mCorner, mCorner, mRectPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 如果不允许录制，则进度拍照状态
        if (!mRecordEnable) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mRecordMode == RecordMode.IDLE && inBeginRange(event)) {
                    mDownRawX = event.getRawX();
                    mDownRawY = event.getRawY();
                    startAnimate();
                    mHandler.postDelayed(mTouchRunnable, TOUCH_DURATION);
                    if (mRecordStateListener != null) {
                        mRecordStateListener.onRecordStart();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mHasCancel) {
                    if (mRecordMode == RecordMode.MODE_PRESS) {
                        SwipeDirection mOldDirection = mSwipeDirection;
                        float oldY = getY();
                        setX(mInitX+event.getRawX()-mDownRawX);
                        setY(mInitY+event.getRawY()-mDownRawY);
                        float newY = getY();

                        if (newY <= oldY) {
                            mSwipeDirection = SwipeDirection.SWIPE_UP;
                        } else {
                            mSwipeDirection = SwipeDirection.SWIPE_DOWN;
                        }

                        if (mOldDirection != mSwipeDirection) {
                            mInfectionPoint = oldY;
                        }
                        float zoomPercentage = (mInfectionPoint - getY()) / mInitY;
                        if (mRecordStateListener != null) {
                            mRecordStateListener.onZoom(zoomPercentage);
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!mHasCancel) {
                    if (mRecordMode == RecordMode.MODE_PRESS) {
                        if (mRecordStateListener != null) {
                            mRecordStateListener.onRecordStop();
                        }
                        resetPress(event.getX(), event.getY());
                    } else if (mRecordMode == RecordMode.IDLE && inBeginRange(event)) {
                        mHandler.removeCallbacks(mTouchRunnable);
                        mRecordMode = RecordMode.MODE_CLICK;
                    } else if (mRecordMode == RecordMode.MODE_CLICK && inEndRange(event)) {
                        if (mRecordStateListener != null) {
                            mRecordStateListener.onRecordStop();
                        }
                        resetClick();
                    }
                } else {
                    mHasCancel = false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean inBeginRange(MotionEvent event) {
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight() / 2;
        int minX = (int) (centerX - mMinCircleRadius);
        int maxX = (int) (centerX + mMinCircleRadius);
        int minY = (int) (centerY - mMinCircleRadius);
        int maxY = (int) (centerY + mMinCircleRadius);
        boolean isXInRange = event.getX() >= minX && event.getX() <= maxX;
        boolean isYInRange = event.getY() >= minY && event.getY() <= maxY;
        return isXInRange && isYInRange;
    }

    private boolean inEndRange(MotionEvent event) {
        int minX = 0;
        int maxX = getMeasuredWidth();
        int minY = 0;
        int maxY = getMeasuredHeight();
        boolean isXInRange = event.getX() >= minX && event.getX() <= maxX;
        boolean isYInRange = event.getY() >= minY && event.getY() <= maxY;
        return isXInRange && isYInRange;
    }

    private void resetPress(float x, float y) {
        mRecordMode = RecordMode.IDLE;
        mStartAnimatorSet.cancel();
        stopAnimate();
        setX(mInitX);
        setY(mInitY);
    }

    private void resetClick() {
        mRecordMode = RecordMode.IDLE;
        mStartAnimatorSet.cancel();
        stopAnimate();
    }

    /**
     * 重置录制按钮
     */
    public void reset() {
        if (mRecordMode == RecordMode.MODE_PRESS) {
            resetPress(0, 0);
        } else if (mRecordMode == RecordMode.MODE_CLICK) {
            resetClick();
        } else if (mRecordMode == RecordMode.IDLE) {
            if (mStartAnimatorSet.isRunning()) {
                mHasCancel = true;
                mStartAnimatorSet.cancel();
                stopAnimate();
                mHandler.removeCallbacks(mTouchRunnable);
                mRecordMode = RecordMode.IDLE;
            }
        }
    }

    /**
     * 打开动画
     */
    private void startAnimate() {
        AnimatorSet startAnimatorSet = new AnimatorSet();
        ObjectAnimator cornerAnimator = ObjectAnimator.ofFloat(this, "corner",
                mMaxCorner, mMinCorner)
                .setDuration(ANIMATION_MIN);
        ObjectAnimator rectSizeAnimator = ObjectAnimator.ofFloat(this, "rectWidth",
                mMaxRectWidth, mMinRectWidth)
                .setDuration(ANIMATION_MIN);
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(this, "circleRadius",
                mMinCircleRadius, mMaxCircleRadius)
                .setDuration(ANIMATION_MIN);
        startAnimatorSet.playTogether(cornerAnimator, rectSizeAnimator, radiusAnimator);

        ObjectAnimator circleWidthAnimator = ObjectAnimator.ofFloat(this, "circleStrokeWidth",
                mMinCircleStrokeWidth, mMaxCircleStrokeWidth, mMinCircleStrokeWidth)
                .setDuration(ANIMATION_MAX);
        circleWidthAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mStartAnimatorSet.playSequentially(startAnimatorSet, circleWidthAnimator);
        mStartAnimatorSet.start();
    }

    /**
     * 结束动画
     */
    private void stopAnimate() {
        ObjectAnimator cornerAnimator = ObjectAnimator.ofFloat(this, "corner",
                mMinCorner, mMaxCorner)
                .setDuration(ANIMATION_MIN);
        ObjectAnimator rectSizeAnimator = ObjectAnimator.ofFloat(this, "rectWidth",
                mMinRectWidth, mMaxRectWidth)
                .setDuration(ANIMATION_MIN);
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(this, "circleRadius",
                mMaxCircleRadius, mMinCircleRadius)
                .setDuration(ANIMATION_MIN);
        ObjectAnimator circleWidthAnimator = ObjectAnimator.ofFloat(this, "circleStrokeWidth",
                mMaxCircleStrokeWidth, mMinCircleStrokeWidth)
                .setDuration(ANIMATION_MIN);
        mStopAnimatorSet.playTogether(cornerAnimator, rectSizeAnimator, radiusAnimator, circleWidthAnimator);
        mStopAnimatorSet.start();
    }

    /**
     * 设置corner
     * @param corner
     */
    public void setCorner(float corner) {
        mCorner = corner;
        invalidate();
    }

    /**
     * 设置圆的半径
     * @param circleRadius
     */
    public void setCircleRadius(float circleRadius) {
        mCircleRadius = circleRadius;
    }

    /**
     * 设置圆环边宽
     * @param width 边宽
     */
    public void setCircleStrokeWidth(float width) {
        mCircleStrokeWidth = width;
        invalidate();
    }

    /**
     * 设置中心矩阵的宽度
     * @param rectWidth
     */
    public void setRectWidth(float rectWidth) {
        mRectWidth = rectWidth;
        invalidate();
    }

    class TouchRunnable implements Runnable {

        @Override
        public void run() {
            if (!mHasCancel) {
                mRecordMode = RecordMode.MODE_PRESS;
                mInitX = getX();
                mInitY = getY();
                mInfectionPoint = mInitY;
                mSwipeDirection = SwipeDirection.SWIPE_UP;
            }
        }
    }

    /**
     * 设置是否允许录制
     * @param enable false时，为点击拍照
     */
    public void setRecordEnable(boolean enable) {
        mRecordEnable = enable;
    }

    /**
     * 添加录制状态监听器
     * @param listener
     */
    public void addRecordStateListener(RecordStateListener listener) {
        mRecordStateListener = listener;
    }

    /**
     * 录制状态监听器
     */
    public interface RecordStateListener {

        /**
         * 录制开始
         */
        void onRecordStart();

        /**
         * 录制停止
         */
        void onRecordStop();

        /**
         * 放大程度
         * @param percent 缩放百分比值 0 ~ 1.0
         */
        void onZoom(float percent);
    }

    /**
     * 录制模式
     */
    private enum RecordMode {
        MODE_CLICK, // 单击状态
        MODE_PRESS, // 长按状态
        IDLE        // 默认空闲状态
    }

    /**
     * 滑动方向
     */
    private enum SwipeDirection {
        SWIPE_UP,   // 向上滑动
        SWIPE_DOWN  // 乡下滑动
    }
}
