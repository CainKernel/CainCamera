package com.cgfay.cameralibrary.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.cameralibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 相机模式指示器
 * Created by cain.huang on 2017/12/28.
 */

public class HorizontalIndicatorView extends View {

    private Context mContext;
    private List<String> mIndicators = new ArrayList<String>();//数据源字符串数组

    private boolean isFirst = true;
    private int mWidth;//控件宽度
    private int mHeight;//控件高度


    private int mFontSize;//每个字母所占的大小；
    private TextPaint mTextPaint;
    private Paint mSelectedPaint;//被选中文字的画笔


    private int mCurrentIndex;
    private float mPreviewX;
    private float mOffset;

    // 属性
    private int mSeeSize = 5;
    private float mTextSize;
    private int mTextColor;
    private float mSelectedTextSize;
    private int mSelectedColor;

    private Rect mRect = new Rect();

    private int mTextWidth = 0;
    private int mTextHeight = 0;
    private int mCenterTextWidth = 0;
    private int mCenterTextHeight = 0;

    private OnIndicatorListener mOnIndicatorListener;

    public HorizontalIndicatorView(Context context) {
        this(context, null);
    }

    public HorizontalIndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setWillNotDraw(false);
        setClickable(true);
        initAttrs(attrs);//初始化属性
        initPaint();//初始化画笔
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mSelectedPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mSelectedPaint.setColor(mSelectedColor);
        mSelectedPaint.setTextSize(mSelectedTextSize);
    }

    /**
     * 初始化属性
     * @param attrs
     */
    @SuppressLint("RestrictedApi")
    private void initAttrs(AttributeSet attrs) {
        TintTypedArray tta = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                R.styleable.HorizontalIndicatorView);
        //两种字体颜色和字体大小
        mSeeSize = tta.getInteger(R.styleable.HorizontalIndicatorView_SeeSize, 5);
        mSelectedTextSize = tta.getFloat(R.styleable.HorizontalIndicatorView_SelectedTextSize, 50);
        mSelectedColor = tta.getColor(R.styleable.HorizontalIndicatorView_SelectedTextColor,
                mContext.getResources().getColor(android.R.color.black));
        mTextSize = tta.getFloat(R.styleable.HorizontalIndicatorView_TextSize, 40);
        mTextColor = tta.getColor(R.styleable.HorizontalIndicatorView_TextColor,
                mContext.getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPreviewX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float scrollX = event.getX();
                if (mCurrentIndex != 0 && mCurrentIndex != mIndicators.size() - 1) {
                    mOffset = scrollX - mPreviewX;
                } else {
                    mOffset = (float) ((scrollX - mPreviewX) / 1.5);
                }
                if (scrollX > mPreviewX) {
                    if (scrollX - mPreviewX >= mFontSize) {
                        if (mCurrentIndex > 0) {
                            mOffset = 0;
                            mCurrentIndex = mCurrentIndex - 1;
                            mPreviewX = scrollX;
                            if (mOnIndicatorListener != null) {
                                mOnIndicatorListener.onIndicatorChanged(mCurrentIndex);
                            }
                        }
                    }
                } else {
                    if (mPreviewX - scrollX >= mFontSize) {
                        if (mCurrentIndex < mIndicators.size() - 1) {
                            mOffset = 0;
                            mCurrentIndex = mCurrentIndex + 1;
                            mPreviewX = scrollX;
                            if (mOnIndicatorListener != null) {
                                mOnIndicatorListener.onIndicatorChanged(mCurrentIndex);
                            }
                        }
                    }
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                mOffset = 0;
                invalidate();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFirst) {
            mWidth = getWidth();
            mHeight = getHeight();
            mFontSize = mWidth / mSeeSize;
            isFirst = false;
        }
        if (mCurrentIndex >= 0 && mCurrentIndex <= mIndicators.size() - 1) {
            String indicator = mIndicators.get(mCurrentIndex);
            mSelectedPaint.getTextBounds(indicator, 0, indicator.length(), mRect);
            mCenterTextWidth = mRect.width();
            mCenterTextHeight = mRect.height();
            canvas.drawText(mIndicators.get(mCurrentIndex), getWidth() / 2 - mCenterTextWidth / 2
                            + mOffset, getHeight() / 2 + mCenterTextHeight / 2, mSelectedPaint);
            for (int i = 0; i < mIndicators.size(); i++) {
                if (mCurrentIndex > 0 && mCurrentIndex < mIndicators.size() - 1) {
                    mTextPaint.getTextBounds(mIndicators.get(mCurrentIndex - 1), 0,
                            mIndicators.get(mCurrentIndex - 1).length(), mRect);
                    int width = mRect.width();
                    mTextPaint.getTextBounds(mIndicators.get(mCurrentIndex + 1), 0,
                            mIndicators.get(mCurrentIndex + 1).length(), mRect);
                    mTextWidth = (width + mRect.width()) / 2;
                }
                if (i == 0) {
                    mTextPaint.getTextBounds(mIndicators.get(0), 0,
                            mIndicators.get(0).length(), mRect);
                    mTextHeight = mRect.height();
                }
                if (i != mCurrentIndex) {
                    canvas.drawText(mIndicators.get(i), (i - mCurrentIndex) * mFontSize
                            + getWidth() / 2 - mTextWidth / 2 + mOffset,
                            getHeight() / 2 + mTextHeight / 2, mTextPaint);
                }

            }


        }

    }


    /**
     * 设置指示器所有制
     * @param indicators
     */
    public void setIndicators(List<String> indicators) {
        mIndicators = indicators;
        mCurrentIndex = mIndicators.size() / 2;
        invalidate();
    }

    /**
     * 指示器监听
     */
    public interface OnIndicatorListener {
        void onIndicatorChanged(int currentIndex);
    }

    /**
     * 添加指示器监听回调
     * @param listener
     */
    public void addIndicatorListener(OnIndicatorListener listener) {
        mOnIndicatorListener = listener;
    }
}