package com.cgfay.caincamera.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.caincamera.R;

/**
 * 帧裁剪视图
 * Created by cain on 2017/12/28.
 */

public class FrameCropView extends View {

    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private Paint mPaint;
    private int mStrokeWidth;
    
    // 边角长度
    private int mCornerLength;
    private float mMarginLeft;
    private float mMarginRight;
    private float mMarginTop;
    private float mMarginBottom;
    private int mStrokeWidthNew;

    // 按下坐标
    private float mTouchX;
    private float mTouchY;
    private float mLastX = 0;
    private float mLastY = 0;

    private boolean mTouchLeft;     // 左边触摸标志
    private boolean mTouchRight;    // 右边触摸标志
    private boolean mTouchTop;      // 上边触摸标志
    private boolean mTouchBottom;   // 下边触摸标志
    private boolean mMoving;        // 拖拽模式

    // 上下左右四个坐标
    private float mLeft;
    private float mRight;
    private float mTop;
    private float mBottom;


    public FrameCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FrameCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrameCropView(Context context) {
        super(context);
        init();
    }

    private void init() {

        mMarginLeft = (int) getResources().getDimension(R.dimen.dp30);
        mMarginRight = (int) getResources().getDimension(R.dimen.dp30);
        mMarginTop = (int) getResources().getDimension(R.dimen.dp30);
        mMarginBottom = (int) getResources().getDimension(R.dimen.dp80);
        mStrokeWidth = (int) getResources().getDimension(R.dimen.dp3);
        mStrokeWidthNew = (int) getResources().getDimension(R.dimen.dp1);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
    }


    
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                mTouchX = event.getX();
                mTouchY = event.getY();

                // 手指触摸的是左边还是右边
                if (Math.abs(mLeft - mTouchX) < mCornerLength) {
                    mTouchLeft = true;
                } else if (Math.abs(mRight - mTouchX) < mCornerLength) {
                    mTouchRight = true;
                }
                // 手指触摸是上边还是下边
                if (Math.abs(mTop - mTouchY) < mCornerLength) {
                    mTouchTop = true;
                } else if (Math.abs(mBottom - mTouchY) < mCornerLength) {
                    mTouchBottom = true;
                }
                // 如果手指范围没有在任何边界位置, 那么我们就认为用户是想拖拽框体
                if (!mTouchLeft && !mTouchTop && !mTouchRight && !mTouchBottom) {
                    mMoving = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                // 得到手指移动距离
                float slideX = moveX- mTouchX + mLastX;
                float slideY = moveY- mTouchY + mLastY;

                // 判断是否是拖拽模式
                if (mMoving) {
                    mLeft += slideX;
                    mRight += slideX;
                    mTop += slideY;
                    mBottom += slideY;
                    //同时改变left和right值, 达到左右移动的效果
                    if(mLeft < mMarginLeft || mRight > mMeasuredWidth - mMarginRight) {//判断x轴的移动边界
                        mLeft -= slideX;
                        mRight -= slideX;
                    }
                    //同时改变top和bottom值, 达到上下移动的效果
                    if(mTop < mMarginTop || mBottom > mMeasuredHeight - mMarginBottom){//判断y轴的移动边界
                        mTop -= slideY;
                        mBottom -= slideY;
                    }
                } else { //更改边框大小模式
                    //改变边框的宽度
                    if(mTouchLeft){
                        mLeft += slideX;
                        if(mLeft < mMarginLeft) mLeft = mMarginLeft;
                        if(mLeft > mRight - mCornerLength *2) mLeft = mRight - mCornerLength *2;
                    }else if(mTouchRight){
                        mRight += slideX;
                        if(mRight > mMeasuredWidth - mMarginRight) mRight = mMeasuredWidth - mMarginRight;
                        if(mRight < mLeft + mCornerLength *2) mRight = mLeft + mCornerLength *2;
                    }
                    //改变边框的高度, 如果两个都满足(比如手指在边角位置),那么就呈现一种缩放状态
                    if(mTouchTop){
                        mTop += slideY;
                        if(mTop < mMarginTop) mTop = mMarginTop;
                        if(mTop > mBottom - mCornerLength *2) mTop = mBottom - mCornerLength *2;
                    }else if(mTouchBottom){
                        mBottom += slideY;
                        if(mBottom > mMeasuredHeight - mMarginBottom) mBottom = mMeasuredHeight - mMarginBottom;
                        if(mBottom < mTop + mCornerLength *2) mBottom = mTop + mCornerLength *2;
                    }
                }
                //实时触发onDraw()方法
                invalidate();

                mTouchX = moveX;
                mTouchY = moveY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchLeft = false;
                mTouchRight = false;
                mTouchTop = false;
                mTouchBottom = false;
                mMoving = false;
                break;
        }
        return true;
    }

    /**
     * 得到裁剪区域的margin值
     */
    public float[] getCutArea() {
        float[] arr = new float[4];
        arr[0] = mLeft - mMarginLeft;
        arr[1] = mTop - mMarginTop;
        arr[2] = mRight - mMarginLeft;
        arr[3] = mBottom - mMarginTop;
        return arr;
    }

    /**
     * 获取裁剪宽度
     * @return
     */
    public int getRectWidth(){
        return (int) (mMeasuredWidth - mMarginLeft - mMarginRight);
    }

    /**
     * 获取裁剪高度
     * @return
     */
    public int getRectHeight(){
        return (int) (mMeasuredHeight - mMarginTop - mMarginBottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mMeasuredWidth == 0) {
            initParams();
        }
    }

    /**
     * 设置Margin
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setMargin(float left, float top, float right, float bottom) {

        mMarginLeft = left;
        mMarginTop = top;
        mMarginRight = right;
        mMarginBottom = bottom;
        initParams();
        invalidate();
    }

    /**
     * 初始化参数
     */
    private void initParams() {

        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();
        mCornerLength = mMeasuredWidth / 10;

        mLeft = mMarginLeft;
        mRight = mMeasuredWidth - mMarginRight;
        mTop = mMarginTop;
        mBottom = mMeasuredHeight - mMarginBottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 宽度
        mPaint.setStrokeWidth(mStrokeWidthNew);
        // 绘制裁剪区域的矩形, 传入margin值来确定大小
        canvas.drawRect(mLeft, mTop, mRight, mBottom, mPaint);
        //绘制四条分割线和四个角
        drawLine(canvas, mLeft, mTop, mRight, mBottom);
    }

    /**
     * 绘制四条分割线和四个角
     */
    private void drawLine(Canvas canvas, float left, float top, float right, float bottom){

        // 设置Stroke宽度
        mPaint.setStrokeWidth(1);
        // 绘制四条分割线
        float startX = (right - left) / 3 + left;
        float startY = top;
        float endX = (right - left) / 3 + left;
        float endY = bottom;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = (right - left) / 3 * 2 + left;
        startY = top;
        endX = (right - left) / 3 * 2 + left;
        endY = bottom;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = left;
        startY = (bottom - top) / 3 + top;
        endX = right;
        endY = (bottom - top) / 3 + top;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = left;
        startY = (bottom - top) / 3 * 2 + top;
        endX = right;
        endY = (bottom - top) / 3 * 2 + top;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        mPaint.setStrokeWidth(mStrokeWidth);
        // 绘制四个角
        startX = left - mStrokeWidth / 2;
        startY = top;
        endX = left + mCornerLength;
        endY = top;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = left;
        startY = top;
        endX = left;
        endY = top + mCornerLength;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = right + mStrokeWidth / 2;
        startY = top;
        endX = right - mCornerLength;
        endY = top;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = right;
        startY = top;
        endX = right;
        endY = top + mCornerLength;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = left;
        startY = bottom;
        endX = left;
        endY = bottom - mCornerLength;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = left - mStrokeWidth / 2;
        startY = bottom;
        endX = left + mCornerLength;
        endY = bottom;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = right + mStrokeWidth / 2;
        startY = bottom;
        endX = right - mCornerLength;
        endY = bottom;
        canvas.drawLine(startX, startY, endX, endY, mPaint);

        startX = right;
        startY = bottom;
        endX = right;
        endY = bottom - mCornerLength;
        canvas.drawLine(startX, startY, endX, endY, mPaint);
    }
}
