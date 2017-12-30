package com.cgfay.caincamera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 手势视图，旋转放大缩小等功能
 * Created by cain on 2017/12/28.
 */

public class GestureView extends View {

    private float mTouchX;
    private float mTouchY;
    private float firstX;
    private float firstY;
    private OnClickListener mListener;
    // 是否可点击
    private boolean mClickable = true;

    private int mMinX = -1;
    private int mMinY = -1;
    private int mMaxX = -1;
    private int mMaxY = -1;

    private OnLimitsListener mLimitsListener;
    private OnTouchListener mTouchListener;

    // 是否超出范围
    private boolean mOuterLimits;
    // 长宽比
    private float mRatio;

    // 最小、最大宽高
    private int mMinWidth;
    private int mMaxWidth;
    private int mMinHeight;
    private int mMaxHeight;

    // 临时视图
    private View mTempView;
    // 上一次的旋转角度
    private float mLastRotation;

    // 中心坐标
    private float mCenterX;
    private float mCenterY;
    // 双指触摸移动
    private boolean mDoubleFingerMove = false;
    // 上一次的距离
    private float mLastDistance;

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureView(Context context) {
        super(context);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    public void setClickable(boolean clickable) {
        mClickable = clickable;
    }

    /**
     * 设置限定的大小
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    public void setLimitsSize(int minX, int minY, int maxX, int maxY) {
        mMinX = minX;
        mMinY = minY;
        mMaxX = maxX;
        mMaxY = maxY;
    }


    /**
     * 超出边界的回调
     */
    public interface OnLimitsListener {
        void OnOuterLimits(float x, float y);
        void OnInnerLimits(float x, float y);
    }

    public void setOnLimitsListener(OnLimitsListener onLimitsListener){
        this.mLimitsListener = onLimitsListener;
    }

    /**
     * 手指触摸事件
     */
    public interface OnTouchListener {
        void onTouchDown(GestureView view, MotionEvent event);
        void onTouchMove(GestureView view, MotionEvent event);
        void onTouchUp(GestureView view, MotionEvent event);
    }

    /**
     * 设置触摸监听器
     * @param listener
     */
    public void setOnTouchListener(OnTouchListener listener){
        mTouchListener = listener;
    }

    /**
     * 是否超出范围
     */
    public boolean isOutLimits(){
        return mOuterLimits;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mMinWidth == 0) {
            mRatio = getWidth() * 1.0f / getHeight();
            mMinWidth = getWidth() / 2;
            ViewGroup parent = (ViewGroup) getParent();
            mMaxWidth = parent.getWidth();
            mMinHeight = getHeight() / 2;
            mMaxHeight = (int) (mMaxWidth / mRatio);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mTouchListener != null) {
                    mTouchListener.onTouchDown(this, event);
                }
                firstX = mTouchX = event.getRawX();
                firstY = mTouchY = event.getRawY();
                mCenterX = getWidth() / 2 + getX();//view的中心点坐标
                mCenterY = getHeight() / 2 + getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 获取触摸的数量
                int pointerCount = event.getPointerCount();
                if (pointerCount >= 2) { // 双指触摸事件
                    mDoubleFingerMove = true;
                    float distance = getSlideDistance(event);
                    float spaceRotation = getRotation(event);
                    if (mTempView == null) { // 创建视图
                        mTempView = new View(getContext());
                        mTempView.setX(getX());
                        mTempView.setY(getY());
                        mTempView.setRotation(getRotation());
                        mTempView.setBackground(getBackground());
                        mTempView.setLayoutParams
                                (new ViewGroup.LayoutParams(getWidth(), getHeight()));
                        ViewGroup parent = (ViewGroup) getParent();
                        parent.addView(mTempView);
                        setAlpha(0);
                    } else {
                        float slide = mLastDistance - distance;
                        ViewGroup.LayoutParams layoutParams = getLayoutParams();
                        layoutParams.width = (int) (layoutParams.width - slide);
                        float slide2 = slide / mRatio;
                        layoutParams.height = (int) (layoutParams.height - slide2);

                        if (layoutParams.width > mMaxWidth
                                || layoutParams.height > mMaxHeight) {
                            layoutParams.width = mMaxWidth;
                            layoutParams.height = mMaxHeight;
                        } else if (layoutParams.width < mMinWidth
                                || layoutParams.height < mMinHeight) {
                            layoutParams.width = mMinWidth;
                            layoutParams.height = mMinHeight;
                        }
                        setLayoutParams(layoutParams);

                        // 更新视图位置
                        float x = mCenterX - getWidth() / 2;
                        float y = mCenterY - getHeight() / 2;
                        setX(x);
                        setY(y);

                        mTempView.setX(x);
                        mTempView.setY(y);

                        ViewGroup.LayoutParams tempViewParams = mTempView.getLayoutParams();
                        tempViewParams.width = layoutParams.width;
                        tempViewParams.height = layoutParams.height;
                        mTempView.setLayoutParams(tempViewParams);
                        // 更新旋转角度
                        if (mLastRotation != 0) {
                            float diff = mLastRotation - spaceRotation;
                            mTempView.setRotation(mTempView.getRotation() - diff);
                        }
                    }
                    mLastRotation = spaceRotation;
                    mLastDistance = distance;
                } else if (!mDoubleFingerMove && pointerCount == 1) { // 单指移动事件
                    if (mTouchListener != null) {
                        mTouchListener.onTouchMove(this, event);
                    }
                    float moveX = event.getRawX();
                    float moveY = event.getRawY();
                    if (moveX != -1 && moveY != -1) {
                        if (moveX <= mMinX || moveX >= mMaxX
                                || moveY <= mMinY || moveY >= mMaxY) {
                            if (mLimitsListener != null) {
                                mLimitsListener.OnOuterLimits(moveX, moveY);
                            }
                            mOuterLimits = true;
                        } else if (moveX > mMinX && moveX < mMaxX
                                && moveY > mMinY && moveY < mMaxY) {
                            if(mLimitsListener != null) {
                                mLimitsListener.OnInnerLimits(moveX, moveY);
                            }
                            mOuterLimits = false;
                        }
                    }
                    // 计算新的位置
                    float slideX = moveX - mTouchX + getX();
                    float slideY = moveY - mTouchY + getY();
                    setX(slideX);
                    setY(slideY);
                    mTouchX = moveX;
                    mTouchY = moveY;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_UP:

                // 移除临时视图
                if (mTempView != null) {
                    setAlpha(1);
                    setRotation(mTempView.getRotation());
                    ViewGroup parent = (ViewGroup) getParent();
                    parent.removeView(mTempView);
                }
                mLastRotation = 0;
                mTempView = null;
                mDoubleFingerMove = false;
                mLastDistance = 0;

                // 松开触摸
                if (mTouchListener != null) {
                    mTouchListener.onTouchUp(this, event);
                }

                // 判定是否单击事件
                float upX = event.getRawX();
                float upY = event.getRawY();
                if (Math.abs(upX - firstX) < 10
                        && Math.abs(upY - firstY) < 10 && mClickable) {
                    if (mListener != null) {
                        mListener.onClick(this);
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 获取手指间的旋转角度
     */
    private float getRotation(MotionEvent event) {

        double deltaX = event.getX(0) - event.getX(1);
        double deltaY = event.getY(0) - event.getY(1);
        double radians = Math.atan2(deltaY, deltaX);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 获取手指间的距离
     */
    private float getSlideDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

}
