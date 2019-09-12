package com.cgfay.video.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.uitls.utils.DensityUtils;
import com.cgfay.video.bean.EffectDuration;

import java.util.ArrayList;

/**
 * 带特效选中的进度条
 */
public class EffectSelectedSeekBar extends View {

    private static final int DELAY = 150;

    private Context mContext;

    // 当前指示器宽高和颜色
    private float mIndicatorWidth;
    private float mIndicatorHeight;
    private int mIndicatorColor;

    // 进度条颜色
    private int mProgressColor;
    private int mProgressHeight;

    private float mMax = 0;
    private float mProgress = 50f;

    private int mMargin;
    private float mViewWidth;
    private int mPositionX;

    private int mDstThumbWidth = 100;

    private int mDstMargin;
    private float mDstViewWidth;
    private int mDstPositionX;
    private float mDstProgress = 0f;


    private float mAppend;
    private float mTouchWidth;
    private float mTouchHeight;


    private EffectDuration mDuration;
    private ArrayList<EffectDuration> mEffectDurationList;
    private boolean needCallback = true;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            needCallback = true;
        }
    };

    public EffectSelectedSeekBar(Context context) {
        this(context,null);
    }

    public EffectSelectedSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EffectSelectedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mAppend = DensityUtils.dp2px(context, 4);
        mTouchWidth = DensityUtils.dp2px(context, 2);
        mTouchHeight = DensityUtils.dp2px(context, 4);
        mIndicatorWidth = DensityUtils.dp2px(context, 10);
        mIndicatorHeight = DensityUtils.dp2px(context, 24);
        mIndicatorColor = 0xFFFFD217;
        mProgressColor = 0xFFFFFFFF;
        mProgressHeight = DensityUtils.dp2px(context, 4);
        mEffectDurationList = new ArrayList<>();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        } finally {
            super.finalize();
        }
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(float progress) {
        if (dragging) {
            return;
        }
        mProgress = progress;
        mPositionX = (int) (mViewWidth*mProgress/mMax);
        if (mDuration != null) {
            mDuration.setEnd((long) mProgress);
        }
        invalidate();
    }

    /**
     * 设置当前的特效区间
     * @param operating
     * @param effectDuration
     * @return
     */
    public EffectDuration setEffectDuration(boolean operating,
                                            @Nullable EffectDuration effectDuration) {
        if (operating) {
            mDuration = effectDuration;
            mDuration.setStart((long) mProgress);
        } else {
            effectDuration = mDuration;
            mDuration = null;
        }
        return effectDuration;
    }

    /**
     * 清空当前的特效选中区间
     */
    public void clearEffectDuration() {
        mDuration = null;
    }

    /**
     * 获取当前特效选中区间
     * @return
     */
    public EffectDuration getEffectDuration() {
        return mDuration;
    }

    /**
     * 设置特效选中区间列表
     * @param effectDurations
     */
    public void setEffectDurationList(ArrayList<EffectDuration> effectDurations) {
        mEffectDurationList = effectDurations;
        invalidate();
    }

    /**
     * 清空所有选中特效区间
     */
    public void clearAllEffects() {
        mDuration = null;
        mEffectDurationList = new ArrayList<>();
        setProgress(0);
    }

    /**
     * 获取当前进度
     * @return
     */
    public float getProgress() {
        return mProgress;
    }

    /**
     * 获取最大进度
     * @return
     */
    public float getMax() {
        return mMax;
    }

    /**
     * 设置最大进度
     * @param max
     */
    public void setMax(float max) {
        mMax = max;
        invalidate();
    }

    private boolean dragging;
    private float mMoveX;
    private boolean isDstDragging;
    private float mDstMoveX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eX = event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && (eX > mPositionX - mAppend && eX < mPositionX + mIndicatorWidth + mAppend)) {
            dragging = true;
            mMoveX = eX;
            actionDownAnimator();
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onStartTrackingTouch();
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN
                && (eX > mDstPositionX && eX < mDstPositionX + mDstThumbWidth)) {
            isDstDragging = true;
            mDstMoveX = eX;
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    mPositionX += event.getX() - mMoveX;
                    if (mPositionX < 0) {
                        mPositionX = 0;
                    }
                    if (mPositionX + mMargin > mViewWidth + mMargin) {
                        int progress = (int) ((mViewWidth) / mViewWidth * mMax);
                        if (mOnSeekBarChangeListener != null && progress != mProgress && needCallback) {
                            needCallback = false;
                            mHandler.sendEmptyMessageDelayed(0, DELAY);
                            mOnSeekBarChangeListener.onProgress(progress > mMax ? (int) mMax : progress, true);
                        }
                        mProgress = progress;
                    } else {
                        int progress = (int) (mPositionX / mViewWidth * mMax);
                        if (mOnSeekBarChangeListener != null && progress != mProgress && needCallback) {
                            needCallback = false;
                            mHandler.sendEmptyMessageDelayed(0, DELAY);
                            mOnSeekBarChangeListener.onProgress(progress>mMax ? (int) mMax : progress, true);
                        }
                        mProgress = progress;
                    }
                    mMoveX = event.getX();
                    invalidate();
                    return true;
                } else if(isDstDragging) {
                    mDstPositionX += event.getX() - mDstMoveX;
                    if (mDstPositionX < 0) {
                        mDstPositionX = 0;
                    }
                    if (mDstPositionX + mDstMargin > mDstViewWidth + mDstMargin) {
                        mDstProgress = (int) mMax;
                    } else {
                        mDstProgress = (int) (mDstPositionX / mDstViewWidth * mMax);
                    }
                    mDstMoveX = event.getX();
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (dragging) {
                    actionUpAnimator();
                    dragging = false;
                    if (mPositionX + mMargin > mViewWidth + mMargin) {
                        mPositionX = (int) (mViewWidth);
                    }
                    mProgress = (int) (mPositionX / mViewWidth * mMax);
                    if (mOnSeekBarChangeListener != null) {
                        int progress = (int) (mPositionX / mViewWidth * mMax);
                        mOnSeekBarChangeListener.onStopTrackingTouch(progress>mMax? (int) mMax :progress);
                    }
                    return true;
                } else if(isDstDragging) {
                    isDstDragging = false;
                    if (mDstPositionX + mDstMargin > mDstViewWidth + mDstMargin) {
                        mDstPositionX = (int) (mDstViewWidth);
                    }
                    mDstProgress = (int) (mDstPositionX / mDstViewWidth * mMax);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMargin = (int) (mIndicatorWidth/2);
        mViewWidth = getMeasuredWidth()- mMargin*2 - mTouchWidth*2;
        if (mPositionX == 0) {
            mPositionX = (int) (mViewWidth * mProgress/mMax);
        }

        mDstMargin = mDstThumbWidth/2;
        mDstViewWidth = getMeasuredWidth() - mDstMargin*2;
        if (mDstPositionX == 0) {
            mDstPositionX = (int) (mDstViewWidth * mDstProgress/mMax);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制进度条
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mProgressColor);
        paint.setStrokeWidth(mProgressHeight);
        int height = getMeasuredHeight();
        int layerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        RectF progressRect = new RectF(mMargin,height/2 - mProgressHeight /2, mMargin + mViewWidth,height/2 + mProgressHeight /2);
        canvas.drawRoundRect(progressRect, mProgressHeight/2, mProgressHeight /2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        // 逐个绘制选中的特效区间
        for (EffectDuration bean: mEffectDurationList) {
            paint.setColor(bean.getColor());
            RectF selectLineRectF = new RectF(mMargin + (mViewWidth * bean.getStart()/mMax),
                    height/2 - mProgressHeight/2,
                    mMargin + (mViewWidth * bean.getEnd()/mMax),
                    height/2 + mProgressHeight /2);
            canvas.drawRect(selectLineRectF,paint);
        }

        // 当前选中的特效区间
        if (mDuration != null) {
            paint.setColor(mDuration.getColor());
            RectF selectLineRectF = new RectF(mMargin + (mViewWidth * mDuration.getStart() / mMax),
                    height/2 - mProgressHeight/2,
                    mMargin + (mViewWidth * mDuration.getEnd() / mMax),
                    height/2 + mProgressHeight/2);
            canvas.drawRect(selectLineRectF, paint);
        }
        paint.setXfermode(null);
        canvas.restoreToCount(layerId);

        // 绘制指示器按钮
        paint.setColor(mIndicatorColor);
        RectF rect = new RectF(mViewWidth * mProgress/mMax,
                height/2 - mIndicatorHeight/2,
                mViewWidth*mProgress/mMax + mIndicatorWidth,
                height/2 + mIndicatorHeight/2);
        canvas.drawRoundRect(rect,20,20, paint);
    }

    private void actionDownAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIndicatorWidth = DensityUtils.dp2px(getContext(), 10)
                        + (float) animation.getAnimatedValue() * DensityUtils.dp2px(getContext(),2);
                mIndicatorHeight = DensityUtils.dp2px(getContext(),24)
                        + (float) animation.getAnimatedValue() * DensityUtils.dp2px(getContext(),4);
                invalidate();
            }
        });
        animator.start();
    }

    private void actionUpAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIndicatorWidth = DensityUtils.dp2px(getContext(),10)
                        + (float) animation.getAnimatedValue() * mTouchWidth;
                mIndicatorHeight = DensityUtils.dp2px(getContext(),24)
                        + (float) animation.getAnimatedValue() * mTouchHeight;
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * 滑动监听器
     */
    public interface OnSeekBarChangeListener {

        void onProgress(int progress, boolean fromUser);

        void onStopTrackingTouch(int progress);

        void onStartTrackingTouch();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        mOnSeekBarChangeListener = onSeekBarChangeListener;
    }

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

}