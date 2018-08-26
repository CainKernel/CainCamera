package com.cgfay.cameralibrary.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.cameralibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 录制控件
 * Created by cain.huang on 2017/12/28.
 */

public class ShutterButton extends View {

    // 按钮做缩放动画
    private static final int MSG_ZOOM_ANIM = 0x00;

    // 按钮的宽高
    private int mMeasuredWidth = -1;

    // 控件填充背景
    private Paint mFillPaint;
    // 进度条
    private Paint mPaintProgress;
    // 进度条宽度
    private float mStrokeWidth;
    // 外圆背景颜色
    private int mOuterBackgroundColor;
    // 设置外圆进度条颜色
    private int mOuterStrokeColor;

    // 内圆背景颜色
    private int mInnerBackgroundColor;
    // 外圆半径
    private float mOuterOvalRadius;
    // 内圆半径
    private float mInnerOvalRadius;
    // 缩放比例
    private float mZoomValue = 0.8f;// 初始化缩放比例

    // 绘制分割线的Paint
    private Paint mPaintSplit;
    // 分割线的颜色
    private int mSplitColor;

    // 是否处于删除模式
    private boolean mDeleteMode = false;
    // 绘制删除模式的Paint
    private Paint mPaintDelete;
    // 删除模式下的颜色
    private int mDeleteColor;

    // 设置进度条起始角度，默认270度
    private int mStartDegree = 270;

    // 当前进度以角度为单位
    private float mGirthPro;
    // 绘制的大小
    private RectF mOval;
    // 进度最大值(默认值为10秒)
    private int mMax = 10 * 1000;
    // 当前进度(默认值为0)
    private float mProgress = 0;

    // 手势监听器
    private OnShutterListener mOnShutterListener;
    // 动画时间
    private int mAnimDuration = 150;

    // button是否处于打开状态
    private boolean mOpenMode = false;

    // 分割列表
    private List<Float> mSplitList = new ArrayList<>();

    // 按钮动画
    private ValueAnimator mButtonAnim;

    // 编码器是否处于可用状态(包括处于非录制可用，以及可停止状态)
    private boolean mEnableEncoder = true;

    // 判断录制视频还是拍照
    private boolean mIsRecord = false;

    // 判断是否处于预览状态，主要用于页面刚打开状态的状态限定
    private boolean mPreviewing = false;

    public ShutterButton(Context context) {
        super(context);
        init();
    }

    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShutterButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        // 设置外圆背景颜色
        mOuterBackgroundColor = getResources().getColor(R.color.shutter_gray_dark);
        // 设置内圆背景
        mInnerBackgroundColor = getResources().getColor(R.color.shutter_green);

        // 填充背景的Paint
        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);

        // 设置进度条颜色
        mOuterStrokeColor = getResources().getColor(R.color.shutter_blue);
        // 设置进度条宽度
        mStrokeWidth = getResources().getDimension(R.dimen.dp6);
        // 进度条的Paint
        mPaintProgress = new Paint();
        mPaintProgress.setAntiAlias(true);
        mPaintProgress.setColor(mOuterStrokeColor);
        mPaintProgress.setStrokeWidth(mStrokeWidth);
        mPaintProgress.setStyle(Paint.Style.STROKE);

        // 分割线的颜色
        mSplitColor = getResources().getColor(R.color.white);
        // 分割线的Paint
        mPaintSplit = new Paint();
        mPaintSplit.setAntiAlias(true);
        mPaintSplit.setColor(mSplitColor);
        mPaintSplit.setStrokeWidth(mStrokeWidth);
        mPaintSplit.setStyle(Paint.Style.STROKE);

        // 删除模式的颜色
        mDeleteColor = getResources().getColor(R.color.red);
        // 删除的段落
        mPaintDelete = new Paint();
        mPaintDelete.setAntiAlias(true);
        mPaintDelete.setColor(mDeleteColor);
        mPaintDelete.setStrokeWidth(mStrokeWidth);
        mPaintDelete.setStyle(Paint.Style.STROKE);

        // 设置绘制大小
        mOval = new RectF();
    }

    private Handler mButtonHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 按钮缩放动画
                case MSG_ZOOM_ANIM:
                    if ((Boolean)msg.obj) {
                        startZoomAnim(0, 1 - mZoomValue);
                    } else {
                        startZoomAnim(1 - mZoomValue, 0);
                    }
                    break;

            }

        }
    };

    // 点击位置
    private float firstX;
    private float firstY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 如果只是拍照，则录制视频不需要处理触摸事件
        if (!mIsRecord) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            // 按下时缩小
            case MotionEvent.ACTION_DOWN:

                firstX = event.getRawX();
                firstY = event.getRawY();
                break;

            // 松开手时，先复位按钮初始状态，如果开始录制，则放大，否则复位
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                // 记录当前位置
                float upX = event.getRawX();
                float upY = event.getRawY();

                // 如果按钮处于打开状态，并且编码器处于可用状态，则关闭按钮
                if (mOpenMode) {
                    if (mEnableEncoder) {
                        mEnableEncoder = false;
                        if (mOnShutterListener != null) {
                            mOnShutterListener.onStopRecord();
                        }
                        closeButton();
                    }
                } else { // 如果处于关闭状态，并且编码器处于可用状态，则打开按钮，排除满进度状态
                    if (mEnableEncoder && mPreviewing && !(mProgress >= mMax)) {
                        mEnableEncoder = false;
                        if (Math.abs(upX - firstX) < mStrokeWidth
                                && Math.abs(upY - firstY) < mStrokeWidth) {
                            if (mOnShutterListener != null) {
                                mOnShutterListener.onStartRecord();
                            }
                            openButton();
                        }
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 打开按钮
     */
    public void openButton() {
        if (!mOpenMode) {
            mOpenMode = true;
            // 放大动画
            mButtonHandler.sendMessageDelayed(
                    mButtonHandler.obtainMessage(MSG_ZOOM_ANIM, true), mAnimDuration);
        }
    }

    /**
     * 关闭按钮
     */
    public void closeButton() {
        if (mOpenMode) {
            mOpenMode = false;
            // 缩小动画
            mButtonHandler.sendMessageDelayed(
                    mButtonHandler.obtainMessage(MSG_ZOOM_ANIM, false), mAnimDuration);
        }
    }

    /**
     * 开始缩放动画
     * @param start 起始值
     * @param end   结束值
     */
    private void startZoomAnim(float start, float end) {
        if (mButtonAnim == null || !mButtonAnim.isRunning()) {
            mButtonAnim = ValueAnimator.ofFloat(start, end).setDuration(mAnimDuration);
            mButtonAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mOuterOvalRadius = mMeasuredWidth * (mZoomValue + value) / 2;
                    mInnerOvalRadius = mMeasuredWidth * (mZoomValue - value) / 2 - mStrokeWidth;

                    value = 1 - mZoomValue - value;
                    mOval.left = mMeasuredWidth * value / 2 + mStrokeWidth / 2;
                    mOval.top = mMeasuredWidth * value / 2 + mStrokeWidth / 2;
                    mOval.right = mMeasuredWidth * (1 - value / 2) - mStrokeWidth / 2;
                    mOval.bottom = mMeasuredWidth * (1 - value / 2) - mStrokeWidth / 2;

                    invalidate();
                }
            });
            mButtonAnim.start();
        }
    }

    /**
     * 设置段点
     */
    public void addSplitView() {
        Log.d("addSplitView", "mGrithPro = " + mGirthPro);
        mSplitList.add(mGirthPro);
        invalidate();
    }

    /**
     * 删除最后一个段点
     */
    public void deleteSplitView() {
        if (mDeleteMode && mSplitList.size() > 0) {
            mSplitList.remove(mSplitList.size()-1);
            mDeleteMode = false;
            invalidate();
        }
    }

    /**
     * 清除断点
     */
    public void cleanSplitView() {
        if(mSplitList.size() > 0) {
            mSplitList.clear();
            invalidate();
        }
    }

    /**
     * 设置删除模式
     */
    public void setDeleteMode(boolean deleteMode) {
        mDeleteMode = deleteMode;
        invalidate();
    }

    /**
     * 是否处于删除模式
     */
    public boolean isDeleteMode(){
        return mDeleteMode;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mMeasuredWidth == -1) {
            mMeasuredWidth = getMeasuredWidth();

            mOuterOvalRadius = mMeasuredWidth * mZoomValue /2;
            mInnerOvalRadius = mMeasuredWidth * mZoomValue /2 - mStrokeWidth;

            // 设置绘制的带下
            mOval.left = mStrokeWidth /2;
            mOval.top = mStrokeWidth /2;
            mOval.right = mMeasuredWidth - mStrokeWidth /2;
            mOval.bottom = mMeasuredWidth - mStrokeWidth /2;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mMeasuredWidth = right - left;
        mOuterOvalRadius = mMeasuredWidth * mZoomValue /2;
        mInnerOvalRadius = mMeasuredWidth * mZoomValue /2 - mStrokeWidth;

        // 设置绘制的带下
        mOval.left = mStrokeWidth /2;
        mOval.top = mStrokeWidth /2;
        mOval.right = mMeasuredWidth - mStrokeWidth /2;
        mOval.bottom = mMeasuredWidth - mStrokeWidth /2;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 绘制外圆背景
        mFillPaint.setColor(mOuterBackgroundColor);
        canvas.drawCircle(mMeasuredWidth /2, mMeasuredWidth /2, mOuterOvalRadius, mFillPaint);

        // 绘制内圆颜色
        mFillPaint.setColor(mInnerBackgroundColor);
        canvas.drawCircle(mMeasuredWidth /2, mMeasuredWidth /2, mInnerOvalRadius, mFillPaint);

        // 绘制进度
        canvas.drawArc(mOval, mStartDegree, mGirthPro, false, mPaintProgress);

        // 绘制断点
        for (int i = 0; i < mSplitList.size(); i++) {
            if(i != 0) {
                canvas.drawArc(mOval, mStartDegree + mSplitList.get(i),
                        1, false, mPaintSplit);
            }
        }

        // 绘制删除模式的段落
        if (mDeleteMode && mSplitList.size() > 0) {
            float split = mSplitList.get(mSplitList.size() - 1);
            canvas.drawArc(mOval, mStartDegree + split,
                    mGirthPro - split, false, mPaintDelete);
        }
    }

    /**
     * 快门监听回调
     */
    public interface OnShutterListener {
        // 开始录制
        void onStartRecord();
        // 退出录制
        void onStopRecord();
        // 进度条结束
        void onProgressOver();
    }

    /**
     * 设置手势监听器
     * @param listener
     */
    public void setOnShutterListener(OnShutterListener listener){
        mOnShutterListener = listener;
    }

    //-----------------------------------------  setter and getter ---------------------------------

    /**
     * 设置进度条颜色
     * @param colorRes
     */
    public void setStrokeColor(@ColorRes int colorRes) {
        mOuterStrokeColor = getResources().getColor(colorRes);
        mPaintProgress.setColor(mOuterStrokeColor);
    }

    /**
     * 设置进度条宽度
     * @param dimenRes
     */
    public void setStrokeWidth(@DimenRes int dimenRes) {
        mStrokeWidth = getResources().getDimension(dimenRes);
        mPaintProgress.setStrokeWidth(mStrokeWidth);
        mPaintSplit.setStrokeWidth(mStrokeWidth);
        mPaintDelete.setStrokeWidth(mStrokeWidth);
    }

    /**
     * 设置分割线颜色
     * @param colorRes
     */
    public void setSplitColor(@ColorRes int colorRes) {
        mSplitColor = getResources().getColor(colorRes);
        mPaintSplit.setColor(mSplitColor);
    }

    /**
     * 设置删除颜色
     * @param colorRes
     */
    public void setDeleteColor(@ColorRes int colorRes) {
        mDeleteColor = getResources().getColor(colorRes);
        mPaintDelete.setColor(mDeleteColor);
    }

    /**
     * 设置内圆背景颜色
     * @param colorRes
     */
    public void setInnerBackgroundColor(@ColorRes int colorRes) {
        mInnerBackgroundColor = getResources().getColor(colorRes);
    }

    /**
     * 设置外圆圈背景颜色
     * @param colorRes
     */
    public void setOuterBackgroundColor(@ColorRes int colorRes) {
        mOuterBackgroundColor = getResources().getColor(colorRes);
        invalidate();
    }

    /**
     * 设置外圆半径
     * @param radius
     */
    public void setOuterOvalRadius(float radius) {
        mOuterOvalRadius = radius;
    }

    /**
     * 设置内圆半径
     * @param radius
     */
    public void setInnerOvalRadius(float radius) {
        mInnerOvalRadius = radius;
    }

    /**
     * 设置动画时间
     * @param duration
     */
    public void setAnimDuration(int duration) {
        mAnimDuration = duration;
    }

    /**
     * 设置缩放比例
     * @param zoomValue
     */
    public void setZoomValue(float zoomValue) {
        mZoomValue = zoomValue;
    }

    /**
     * 设置起始角度
     * @param degree
     */
    public void setStartDegree(int degree) {
        mStartDegree = degree;
    }

    /**
     * 设置进度最大值
     * @param max
     */
    public void setProgressMax(int max){
        mMax = max;
    }

    /**
     * 设置当前进度
     */
    public void setProgress(float progress) {
        mProgress = progress;
        float ratio = progress / mMax;
        mGirthPro = 360 * ratio;
        invalidate();
        // 满进度回调
        if (ratio >= 1) {
            if(mOnShutterListener != null) {
                mOnShutterListener.onProgressOver();
            }
        }
    }

    /**
     * 获取分割线数目
     * @return
     */
    public int getSplitCount() {
        return mSplitList.size();
    }

    /**
     * 获取进度
     * @return
     */
    public float getProgress(){
        return mProgress;
    }


    /**
     * 设置编码器处于可用状态(准备状态和释放状态都不可用)
     * @param enable
     */
    public void setEnableEncoder(boolean enable) {
        mEnableEncoder = enable;
    }

    /**
     * 设置是否录制视频
     * @param enable
     */
    public void setIsRecorder(boolean enable) {
        mIsRecord = enable;
    }

    /**
     * 设置是否打开
     * @param previewing
     */
    public void setEnableOpened(boolean previewing) {
        mPreviewing = previewing;
    }

    /**
     * 是否允许打开
     * @return
     */
    public boolean isEnableOpened() {
        return mPreviewing;
    }
}
