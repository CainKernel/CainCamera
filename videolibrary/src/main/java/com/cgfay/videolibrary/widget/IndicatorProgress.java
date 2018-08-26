package com.cgfay.videolibrary.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.cgfay.videolibrary.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 指示器进度条
 */
public class IndicatorProgress extends View {

    /**
     * 颜色轮询
     */
    private static final int[] Colors = new int[] {
            R.color.yellow,
            R.color.purple,
            R.color.cyan,
            R.color.red,
            R.color.green,
            R.color.blue
    };

    // 宽高
    private int mWidth;
    private int mHeight;

    private Paint mPaint;
    private RectF mRectF;
    private int mRectWidth;
    private Bitmap mBitmap;

    // 背景颜色框
    private RectF mBackgroundColorF = new RectF();

    // 绘制颜色列表
    private List<ColorInfo> mVideoDrawColors = new ArrayList<>();
    // 准备颜色
    private List<ColorInfo> mPreparedColors = new ArrayList<>();
    // 删除缓存
    private Stack<ColorInfo> mDeleteTempColors = new Stack<>();

    // 是否处于编辑状态（预览视频还是给视频添加滤镜）
    private volatile boolean mVideoEdit;

    private float mTouchX;          // 按下位置
    private boolean mScrolled;      // 是否滑动
    private boolean mScrollChange;  // 是否滑动变更

    // 滑动监听器
    private IndicatorScrollListener mIndicatorScrollListener;

    public IndicatorProgress(Context context) {
        super(context);
        init();
    }

    public IndicatorProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        int dp5 = (int) getResources().getDimension(R.dimen.dp5);
        mPaint.setStrokeWidth(dp5);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_video_thumbnail);
        mRectWidth = (int) getResources().getDimension(R.dimen.dp10);
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
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                // 左边滑动
                if (mTouchX > mRectF.left - mRectWidth / 2
                        && mTouchX < mRectF.right + mRectWidth / 2) {
                    mScrolled = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float currentX = event.getX();
                float scrollX = currentX - mTouchX;
                // 如果是左边滑动
                if (mScrolled) {
                    mRectF.left = mRectF.left + scrollX;
                    mRectF.right = mRectF.right + scrollX;
                    if (mRectF.left < 0) {
                        mRectF.left = 0;
                        mRectF.right = mRectWidth;
                    } else if (mRectF.left > mWidth - mRectWidth) {
                        mRectF.left = mWidth - mRectWidth;
                        mRectF.right = mWidth;
                    }
                    mScrollChange = true;
                    invalidate();
                }
                // 滑动监听回调
                if (mIndicatorScrollListener != null) {
                    mIndicatorScrollListener.onScrollChanged(mRectF.left / (mWidth - mRectWidth));
                }

                mTouchX = currentX;
                break;
            // 松手之后，复位并回调监听器
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchX = 0;
                mScrolled = false;
                if (mScrollChange && mIndicatorScrollListener != null) {
                    mIndicatorScrollListener.onScrollFinish();
                }
                mScrollChange = false;
                break;
        }

        return mScrolled;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制背景颜色
        mPaint.setColor(getResources().getColor(android.R.color.transparent));
        mBackgroundColorF.left = 0;
        mBackgroundColorF.top = 0;
        mBackgroundColorF.right = mRectF.left;
        mBackgroundColorF.bottom = mHeight;
        canvas.drawRect(mBackgroundColorF, mPaint);

        for (int i = 0; i < mVideoDrawColors.size(); i++) {
            ColorInfo colorInfo = mVideoDrawColors.get(i);
            mPaint.setColor(getResources().getColor(colorInfo.color));
            canvas.drawRect(colorInfo.rectF, mPaint);
        }

        if (mVideoEdit) {
            for (int i = 0; i < mPreparedColors.size(); i++) {
                ColorInfo colorInfo = mPreparedColors.get(i);
                mPaint.setColor(getResources().getColor(colorInfo.color));
                colorInfo.rectF.right = mBackgroundColorF.right;
                canvas.drawRect(colorInfo.rectF, mPaint);
            }
        }

        // 绘制进度标志
        mPaint.setColor(Color.WHITE);
        canvas.drawBitmap(mBitmap, null, mRectF, mPaint);
    }

    /**
     * 设置进度百分比
     * @param percent 0.0f ~ 1.0f
     */
    public void setCurrentPercent(float percent) {
        if (percent < 0.0f) {
            percent = 0.0f;
        } else if (percent > 1.0f) {
            percent = 1.0f;
        }
        mRectF.left = (mWidth - mRectWidth) * percent;
        mRectF.right = mRectF.left + mRectWidth;
        invalidate();
    }

    /**
     * 边框滑动监听器
     */
    public interface IndicatorScrollListener {
        // 当前位置百分比
        void onScrollChanged(float percent);
        // 状态发生改变
        void onScrollFinish();
    }

    /**
     * 设置边框滑动监听
     * @param listener
     */
    public void setIndicatorScrollListener(IndicatorScrollListener listener) {
        mIndicatorScrollListener = listener;
    }

    /**
     * 设置是否处于编辑状态
     * @param edit
     */
    public void setVideoEdit(boolean edit) {
        mVideoEdit = edit;
    }

    /**
     * 清空准备好的颜色
     */
    public void clearPreparedColor() {
        mPreparedColors.clear();
    }

    /**
     * 准备颜色
     * @param percent
     */
    public void preparedColor(float percent) {
        ColorInfo colorInfo = new ColorInfo();
        // 设置颜色
        colorInfo.color = Colors[mVideoDrawColors.size() % Colors.length];
        // 计算起始位置
        if (percent < 0.0f) {
            percent = 0.0f;
        } else if (percent > 1.0f) {
            percent = 1.0f;
        }
        colorInfo.rectF.top = 0;
        colorInfo.rectF.bottom = mHeight;
        colorInfo.rectF.left = (mWidth - mRectWidth) * percent;
        colorInfo.rectF.right = mRectF.left;
        mPreparedColors.add(colorInfo);
    }

    /**
     * 添加颜色数据
     * @param percent
     */
    public void addColorData(float percent) {
        mDeleteTempColors.clear();
        for (int i = 0; i < mPreparedColors.size(); i++) {
            ColorInfo colorInfo = mPreparedColors.get(i);
            // 计算结束位置
            if (percent < 0.0f) {
                percent = 0.0f;
            } else if (percent > 1.0f) {
                percent = 1.0f;
            }
            colorInfo.rectF.right = (mWidth - mRectWidth) * percent;
            mVideoDrawColors.add(colorInfo);
        }
    }

    /**
     * 删除颜色
     */
    public void deleteColorRect() {
        int position = mVideoDrawColors.size() - 1;
        ColorInfo colorInfo = mVideoDrawColors.remove(position);
        mDeleteTempColors.push(colorInfo);
        invalidate();
    }

    /**
     * 撤销删除颜色
     */
    public void undoDeleteColorRect() {
        if (mDeleteTempColors.size() > 0) {
            ColorInfo colorInfo = mDeleteTempColors.pop();
            mVideoDrawColors.add(colorInfo);
            invalidate();
        }
    }

    /**
     * 颜色信息
     */
    private class ColorInfo  {
        RectF rectF;
        int color;
        public ColorInfo() {
            rectF = new RectF();
        }
    }
}
