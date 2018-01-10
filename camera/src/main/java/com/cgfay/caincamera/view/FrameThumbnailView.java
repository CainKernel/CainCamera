package com.cgfay.caincamera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.caincamera.R;

/**
 * 帧缩略图
 * Created by cain on 2017/12/28.
 */

public class FrameThumbnailView extends View {

    // 宽高
    private int mWidth;
    private int mHeight;

    private Paint mPaint;
    private RectF mRectF;
    private RectF mRectF2;
    private int mRectWidth;
    private Bitmap mBitmap;
    // 左右两个图片最小像素间隔
    private int mMinPixel;

    private float mTouchX;          // 按下位置
    private boolean mScrollLeft;    // 是否左边滑动
    private boolean mScrollRight;   // 是否右边滑动
    private boolean mScrollChange;  // 是否滑动变更

    // 边沿滑动监听器
    private BorderScrollListener mBorderScrollListener;

    public FrameThumbnailView(Context context) {
        super(context);
        init();
    }

    public FrameThumbnailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrameThumbnailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        int dp5 = (int) getResources().getDimension(R.dimen.dp5);
        mPaint.setStrokeWidth(dp5);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.video_thumbnail);

        mRectWidth = (int) getResources().getDimension(R.dimen.dp10);
        mMinPixel = mRectWidth;
    }

    /**
     * 设置最小时长
     * @param min
     */
    public void setMinInterval(int min) {
        if (mWidth > 0 && min > mWidth) {
            min = mWidth;
        }
        mMinPixel = min;
    }

    /**
     * 边框滑动监听器
     */
    public interface BorderScrollListener {
        void OnBorderScroll(float start, float end);
        void onScrollStateChange();
    }

    /**
     * 设置边框滑动监听
     * @param listener
     */
    public void setOnBorderScrollListener(BorderScrollListener listener) {
        mBorderScrollListener = listener;
    }

    /**
     * 获取左边的时长
     * @return
     */
    public float getLeftInterval(){
        return mRectF.left;
    }

    /**
     * 获取右边的时长
     * @return
     */
    public float getRightInterval(){
        return mRectF2.right;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mWidth == 0) {
            mWidth = getWidth();
            mHeight = getHeight();

            mRectF = new RectF();
            mRectF.left = 0;
            mRectF.top = 0;
            mRectF.right = mRectWidth;
            mRectF.bottom = mHeight;

            mRectF2 = new RectF();
            mRectF2.left = mWidth - mRectWidth;
            mRectF2.top = 0;
            mRectF2.right = mWidth;
            mRectF2.bottom = mHeight;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        move(event);
        return mScrollLeft || mScrollRight;
    }

    private boolean move(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                // 左边滑动
                if (mTouchX > mRectF.left - mRectWidth / 2
                        && mTouchX < mRectF.right + mRectWidth / 2) {
                    mScrollLeft = true;
                }
                // 右边滑动
                if (mTouchX > mRectF2.left - mRectWidth / 2
                        && mTouchX < mRectF2.right + mRectWidth / 2) {
                    mScrollRight = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float currentX = event.getX();
                float scrollX = currentX - mTouchX;
                // 如果是左边滑动
                if (mScrollLeft) {
                    mRectF.left = mRectF.left + scrollX;
                    mRectF.right = mRectF.right + scrollX;

                    if (mRectF.left < 0) {
                        mRectF.left = 0;
                        mRectF.right = mRectWidth;
                    }

                    // 判定左边是否超过了右边
                    if (mRectF.left > mRectF2.right - mMinPixel) {
                        mRectF.left = mRectF2.right - mMinPixel;
                        mRectF.right = mRectF.left+ mRectWidth;
                    }
                    mScrollChange = true;
                    invalidate();
                } else if (mScrollRight) {  // 右边滑动
                    mRectF2.left = mRectF2.left + scrollX;
                    mRectF2.right = mRectF2.right + scrollX;

                    // 范围限定
                    if (mRectF2.right > mWidth) {
                        mRectF2.right = mWidth;
                        mRectF2.left = mRectF2.right - mRectWidth;
                    }
                    // 右边滑动超过了左边
                    if (mRectF2.right < mRectF.left + mMinPixel) {
                        mRectF2.right = mRectF.left + mMinPixel;
                        mRectF2.left = mRectF2.right - mRectWidth;
                    }
                    mScrollChange = true;
                    invalidate();
                }

                // 滑动监听回调
                if (mBorderScrollListener != null) {
                    mBorderScrollListener.OnBorderScroll(mRectF.left, mRectF2.right);
                }

                mTouchX = currentX;
                break;
            // 松手之后，复位并回调监听器
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchX = 0;
                mScrollLeft = false;
                mScrollRight = false;
                if (mScrollChange && mBorderScrollListener != null) {
                    mBorderScrollListener.onScrollStateChange();
                }
                mScrollChange = false;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setColor(Color.WHITE);

        Rect rect = new Rect();
        rect.left = (int) mRectF.left;
        rect.top = (int) mRectF.top;
        rect.right = (int) mRectF.right;
        rect.bottom = (int) mRectF.bottom;
        canvas.drawBitmap(mBitmap, null, mRectF, mPaint);

        Rect rect2 = new Rect();
        rect2.left = (int) mRectF2.left;
        rect2.top = (int) mRectF2.top;
        rect2.right = (int) mRectF2.right;
        rect2.bottom = (int) mRectF2.bottom;
        canvas.drawBitmap(mBitmap, null, mRectF2, mPaint);



        canvas.drawLine(mRectF.left, 0, mRectF2.right, 0, mPaint);
        canvas.drawLine(mRectF.left, mHeight, mRectF2.right, mHeight, mPaint);

        mPaint.setColor(getResources().getColor(R.color.thumbnailColor));

        RectF rectF3 = new RectF();
        rectF3.left = 0;
        rectF3.top = 0;
        rectF3.right = mRectF.left;
        rectF3.bottom = mHeight;
        canvas.drawRect(rectF3, mPaint);

        RectF rectF4 = new RectF();
        rectF4.left = mRectF2.right;
        rectF4.top = 0;
        rectF4.right = mWidth;
        rectF4.bottom = mHeight;
        canvas.drawRect(rectF4, mPaint);
    }
}
