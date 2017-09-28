package com.cgfay.caincamera.view;

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

import com.cgfay.caincamera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 横向指示器
 * Created by cain on 2017/9/28.
 */

//public class HorizontalIndicatorView extends View {
//
//    private Context mContext;
//    private List<String> mStrings = new ArrayList<String>();
//
//    // 属性s
//    private int mSeeSize = 5; // 可见个数
//    private float mSelectedTextSize;
//    private int mSelectedTextColor;
//    private float mTextSize;
//    private int mTextColor;
//
//    private int mFontSize; // 每个字母所占大小
//    private TextPaint mTextPaint;
//    private boolean firstVisible = true;
//    private int mWidth;
//    private int mHeight;
//    private Paint mSelectedPaint;// 画笔
//    private int mTextWidth = 0;
//    private int mTextHeight = 0;
//
//    private int mCenterTextWidth = 0;
//    private int mCenterTextHeight = 0;
//
//    private int mCurrentIndex;
//    private float mPreviewX; // 按下的坐标
//    private float mOffset; // 偏移量
//    private Rect mRect = new Rect();
//
//    private IndicatorListener mIndicatorListener;
//
//
//    public void addIndicatorListener(IndicatorListener listener) {
//        mIndicatorListener = listener;
//    }
//
//    public HorizontalIndicatorView(Context context) {
//        super(context);
//        init(context, null);
//    }
//
//    public HorizontalIndicatorView(Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        init(context, attrs);
//    }
//
//    public HorizontalIndicatorView(Context context, @Nullable AttributeSet attrs,
//                                   int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init(context, attrs);
//    }
//
//    private void init(Context context, AttributeSet attrs) {
//        mContext = context;
//        setWillNotDraw(false);
//        initAttrs(attrs);
//        initPaint();
//    }
//
//    private void initAttrs(AttributeSet attrs) {
//        TintTypedArray tta = TintTypedArray.obtainStyledAttributes(mContext, attrs,
//                R.styleable.HorizontalIndicatorView);
//        mSeeSize = tta.getInteger(R.styleable.HorizontalIndicatorView_SeeSize, 5);
//        mTextSize = tta.getFloat(R.styleable.HorizontalIndicatorView_TextSize, 40);
//        mTextColor = tta.getColor(R.styleable.HorizontalIndicatorView_TextColor,
//                mContext.getResources().getColor(android.R.color.darker_gray));
//        mSelectedTextSize = tta.getFloat(R.styleable.HorizontalIndicatorView_SelectedTextSize, 50);
//        mSelectedTextColor = tta.getColor(R.styleable.HorizontalIndicatorView_SelectedTextColor,
//                mContext.getResources().getColor(R.color.colorHorizontalIndicator));
//    }
//
//    private void initPaint() {
//        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
//        mTextPaint.setTextSize(mTextSize);
//        mTextPaint.setColor(mTextColor);
//        mSelectedPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
//        mSelectedPaint.setTextSize(mSelectedTextSize);
//        mSelectedPaint.setColor(mSelectedTextColor);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        if (firstVisible) {
//            mWidth = getWidth();
//            mHeight = getHeight();
//            mFontSize = mWidth / mSeeSize;
//            firstVisible = false;
//        }
//        if (mCurrentIndex >= 0 && mCurrentIndex <= mStrings.size() - 1) {
//            String string = mStrings.get(mCurrentIndex);
//            mSelectedPaint.getTextBounds(string, 0, string.length(), mRect);
//            mCenterTextWidth = mRect.width();
//            mCenterTextHeight = mRect.height();
//            canvas.drawText(mStrings.get(mCurrentIndex), getWidth() / 2 - mCenterTextWidth / 2 + mOffset,
//                    getHeight() / 2 + mCenterTextHeight / 2, mSelectedPaint);
//            for (int i = 0; i < mStrings.size(); i++) {
//                // 求得选中文字的左右两个文字的平均值
//                if (mCurrentIndex > 0 && mCurrentIndex < mStrings.size() - 1) {
//                    mTextPaint.getTextBounds(mStrings.get(mCurrentIndex - 1), 0,
//                            mStrings.get(mCurrentIndex - 1).length(), mRect);
//                    int width = mRect.width();
//                    mTextPaint.getTextBounds(mStrings.get(mCurrentIndex + 1), 0,
//                            mStrings.get(mCurrentIndex + 1).length(), mRect);
//                    mTextWidth = (width + mRect.width()) / 2;
//                }
//                if (i == 0) {
//                    mTextPaint.getTextBounds(mStrings.get(0), 0, mStrings.get(0).length(), mRect);
//                    mTextHeight = mRect.height();
//                }
//                if (i != mCurrentIndex) {
//                    canvas.drawText(mStrings.get(i), (i - mCurrentIndex) * mFontSize
//                                    + getWidth() / 2 - mTextWidth / 2 + mOffset,
//                            getHeight() / 2 + mTextHeight / 2, mTextPaint);
//                }
//            }
//        }
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mPreviewX = event.getX();
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                Log.d("hahaha", "move");
//                float scrollx = event.getX();
//                if (mCurrentIndex != 0 && mCurrentIndex != mStrings.size() - 1) {
//                    mOffset = scrollx - mPreviewX;
//                } else {
//                    mOffset = (float) ((scrollx - mPreviewX) / 1.5);
//                }
//                if (scrollx > mPreviewX && (scrollx - mPreviewX) >= mFontSize
//                        && mCurrentIndex > 0) {
//                    mOffset = 0;
//                    mCurrentIndex--;
//                    mPreviewX = scrollx;
//                    if (mIndicatorListener != null) {
//                        mIndicatorListener.onIndicatorChanged(mCurrentIndex);
//                    }
//                }
//                invalidate();
//                break;
//
//
//            case MotionEvent.ACTION_UP:
//                mOffset = 0;
//                invalidate();
//                break;
//
//            default:
//                break;
//        }
//        return super.onTouchEvent(event);
//    }
//
//    /**
//     * 设置可见文件数目
//     * @param seeSize
//     */
//    public void setSeeSize(int seeSize) {
//        if (mSeeSize > 0) {
//            mSeeSize = seeSize;
//            invalidate();
//        }
//    }
//
//    /**
//     * 向左滑动一个单元
//     */
//    public void slideLeftOffset() {
//        if (mCurrentIndex < mStrings.size() - 1) {
//            mCurrentIndex++;
//            invalidate();
//        }
//    }
//
//    /**
//     * 向右滑动一个单元
//     */
//    public void slideRightOffset() {
//        if (mCurrentIndex > 0) {
//            mCurrentIndex--;
//            invalidate();
//        }
//    }
//
//    /**
//     * 设置字符串
//     * @param strings
//     */
//    public void setStrings(List<String> strings) {
//        mStrings = strings;
//        mCurrentIndex = mStrings.size() / 2;
//        invalidate();
//    }
//
//    /**
//     * 指示器监听
//     */
//    public interface IndicatorListener {
//        void onIndicatorChanged(int currentIndex);
//    }
//}

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

    private IndicatorListener mIndicatorListener;

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
                            if (mIndicatorListener != null) {
                                mIndicatorListener.onIndicatorChanged(mCurrentIndex);
                            }
                        }
                    }
                } else {
                    if (mPreviewX - scrollX >= mFontSize) {
                        if (mCurrentIndex < mIndicators.size() - 1) {
                            mOffset = 0;
                            mCurrentIndex = mCurrentIndex + 1;
                            mPreviewX = scrollX;
                            if (mIndicatorListener != null) {
                                mIndicatorListener.onIndicatorChanged(mCurrentIndex);
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
    public interface IndicatorListener {
        void onIndicatorChanged(int currentIndex);
    }

    public void addIndicatorListener(IndicatorListener listener) {
        mIndicatorListener = listener;
    }
}