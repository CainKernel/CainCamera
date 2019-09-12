package com.cgfay.camera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.cgfay.cameralibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 录制进度条
 * @author CainHuang
 * @date 2019/7/7
 */
public class RecordProgressView extends View {

    private static final int RADIUS = 4;

    private static final int DIVIDER_WIDTH = 2;

    private static final int BACKGROUND_COLOR = Color.parseColor("#22000000");

    private static final int CONTENT_COLOR = Color.parseColor("#face15");

    private static final int DIVIDER_COLOR = Color.WHITE;

    private Paint mPaint;

    private float mRadius = RADIUS;

    private int mBackgroundColor = BACKGROUND_COLOR;

    private int mContentColor = CONTENT_COLOR;

    private int mDividerColor = DIVIDER_COLOR;

    private int mDividerWidth = DIVIDER_WIDTH;

    private float mProgress;

    private List<Float> mProgressList = new ArrayList<>();

    public RecordProgressView(Context context) {
        super(context);
        init();
    }

    public RecordProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RecordProgressView);
        try {
            mRadius = ta.getDimensionPixelSize(R.styleable.RecordProgressView_radius, RADIUS);
            mBackgroundColor = ta.getColor(R.styleable.RecordProgressView_bg_color, BACKGROUND_COLOR);
            mContentColor = ta.getColor(R.styleable.RecordProgressView_content_color, CONTENT_COLOR);
            mDividerColor = ta.getColor(R.styleable.RecordProgressView_divider_color, DIVIDER_COLOR);
            mDividerWidth = ta.getDimensionPixelSize(R.styleable.RecordProgressView_divider_width, DIVIDER_WIDTH);
        } finally {
            ta.recycle();
        }
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawProgress(canvas);
        drawDivider(canvas);
    }

    /**
     * 绘制背景颜色
     * @param canvas
     */
    private void drawBackground(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);
        canvas.drawRoundRect(new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight()),
                mRadius, mRadius, mPaint);
    }

    /**
     * 绘制进度
     * @param canvas
     */
    private void drawProgress(Canvas canvas) {
        float total = 0;
        for (float progress : mProgressList) {
            total += progress;
        }
        total += mProgress;
        int width = (int) (total * getMeasuredWidth());
        mPaint.setColor(mContentColor);
        canvas.drawRoundRect(new RectF(0, 0, width, getMeasuredHeight()),
                mRadius, mRadius, mPaint);
        if (width < mRadius) {
            return;
        }
        canvas.drawRect(new RectF(mRadius, 0, width, getMeasuredHeight()), mPaint);
    }

    /**
     * 绘制分割线
     * @param canvas
     */
    private void drawDivider(Canvas canvas) {
        mPaint.setColor(mDividerColor);
        int left = 0;
        for (float progress : mProgressList) {
            left += progress * getMeasuredWidth();
            canvas.drawRect(left - mDividerWidth, 0, left, getMeasuredHeight(), mPaint);
        }
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(float progress) {
        mProgress = progress;
        invalidate();
    }

    /**
     * 添加一段进度
     * @param progress
     */
    public void addProgressSegment(float progress) {
        mProgress = 0;
        mProgressList.add(progress);
        invalidate();
    }

    /**
     * 删除一段进度
     */
    public void deleteProgressSegment() {
        mProgress = 0;
        int index = mProgressList.size() - 1;
        if (index >= 0) {
            mProgressList.remove(index);
        }
        invalidate();
    }

    /**
     * 清空所有进度
     */
    public void clear() {
        mProgressList.clear();
        invalidate();
    }
}
