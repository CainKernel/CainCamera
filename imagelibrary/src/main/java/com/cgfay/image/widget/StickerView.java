package com.cgfay.image.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.imagelibrary.R;
import com.cgfay.uitls.utils.BitmapUtils;
import com.cgfay.uitls.utils.RectUtils;

/**
 * 贴纸控件
 * Created by cain.huang on 2017/12/15.
 */
public class StickerView extends View {

    private static final float MIN_SCALE = 0.15f;
    private static final int HELP_BOX_PAD = 25;
    private static final int STICKER_BTN_HALF_SIZE = 30;

    private static final int IDLE_MODE = 0;     // 空闲模式
    private static final int MOVE_MODE = 1;     // 移动模式
    private static final int ROTATE_MODE = 2;   // 旋转模式
    private static final int DELETE_MODE = 3;   // 删除模式
    private static final int FLIP_MODE = 4;     // 翻转模式



    private Paint mBorderPaint;     // 绘制边框用的Paint

    public Rect mSrcRect;           // 贴纸原始位置
    public RectF mDstRect;          // 贴纸的目标位置

    private Rect mBorderRect;       // 边框位置
    private RectF mDeleteRect;      // 删除按钮位置
    private RectF mRotateRect;      // 旋转按钮位置
    private RectF mFlipRect;        // 翻转按钮位置

    private RectF mBorderDstRect;   // 边框目标位置
    private RectF mDeleteDstRect;   // 删除按钮目位置
    private RectF mScaleDstRect;    // 缩放按钮目标位置
    private RectF mFlipDstRect;     // 翻转按钮目标位置

    private Bitmap mBitmap;         // 贴纸图片
    private Bitmap mDeleteBitmap;   // 删除按钮
    private Bitmap mScaleBitmap;    // 缩放按钮
    private Bitmap mHorizonBitmap;  // 横向翻转按钮
    private Bitmap mVerticalBitmap; // 纵向翻转按钮
    private Bitmap mFlipBitmap;     // 翻转按钮

    public Matrix matrix;           // 变换矩阵
    private float mRotateAngle = 0; // 旋转角度

    private int mCurrentMode;       // 当前状态
    private float layout_x = 0;
    private float layout_y = 0;

    private boolean isDrawBorder;   // 是否绘制边框

    private boolean isHorizontal;   // 横向还是纵向

    private float initWidth;        // 加入屏幕时原始宽度

    // 贴纸编辑监听器
    private StickerEditListener mEditListener;

    public StickerView(Context context) {
        super(context);
        init();
    }

    public StickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCurrentMode = IDLE_MODE;
        isDrawBorder = false;
        isHorizontal = false;
        // 绘制边框用的Paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.WHITE);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStrokeWidth(4);

        // 删除按钮
        mDeleteBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_dialog_del_normal);

        // 旋转按钮
        mScaleBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_dialog_scale_normal);

        // 横向翻转按钮
        mHorizonBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_dialog_horizontal_normal);

        // 纵向翻转按钮
        mVerticalBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_dialog_vertical_normal);

        mFlipBitmap = mHorizonBitmap;
        isHorizontal = true;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        mSrcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        // 计算贴纸存放的目标位置
        int min = Math.min(bitmap.getWidth(), getWidth() >> 1);
        int height = (int) min * bitmap.getHeight() / bitmap.getWidth();
        int left = (getWidth() >> 1) - (min >> 1);
        int top = (getHeight() >> 1) - (height >> 1);
        mDstRect = new RectF(left, top, left + min, top + height);

        // 初始化矩阵
        matrix = new Matrix();
        matrix.postTranslate(mDstRect.left, mDstRect.top);
        matrix.postScale((float) min / bitmap.getWidth(),
                (float) height / bitmap.getHeight(),
                mDstRect.left, mDstRect.top);

        // 记录原始宽度
        initWidth = mDstRect.width();
        isDrawBorder = true;
        mBorderDstRect = new RectF(mDstRect);
        updateBorderDestRect();

        // 计算删除位置
        mBorderRect = new Rect(0, 0, mDeleteBitmap.getWidth(),
                mDeleteBitmap.getHeight());

        // 删除按钮位置
        mDeleteRect = new RectF(mBorderDstRect.left - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.top - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.left + STICKER_BTN_HALF_SIZE,
                mBorderDstRect.top + STICKER_BTN_HALF_SIZE);

        // 旋转按钮位置
        mRotateRect = new RectF(mBorderDstRect.right - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.bottom - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.right + STICKER_BTN_HALF_SIZE,
                mBorderDstRect.bottom + STICKER_BTN_HALF_SIZE);

        // 翻转按钮位置
        mFlipRect = new RectF(mBorderDstRect.right - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.top - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.right + STICKER_BTN_HALF_SIZE,
                mBorderDstRect.top - STICKER_BTN_HALF_SIZE);

        mDeleteDstRect = new RectF(mDeleteRect);
        mScaleDstRect = new RectF(mRotateRect);
        mFlipDstRect = new RectF(mFlipRect);
    }

    /**
     * 更新边框目标位置
     */
    private void updateBorderDestRect() {
        mBorderDstRect.left -= HELP_BOX_PAD;
        mBorderDstRect.right += HELP_BOX_PAD;
        mBorderDstRect.top -= HELP_BOX_PAD;
        mBorderDstRect.bottom += HELP_BOX_PAD;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mDeleteDstRect.contains(x, y)) {        // 删除模式
                    mCurrentMode = DELETE_MODE;
                    ret = true;
                } else if (mScaleDstRect.contains(x, y)) {  // 缩放模式
                    mCurrentMode = ROTATE_MODE;
                    layout_x = x;
                    layout_y = y;
                    ret = true;
                } else if (mFlipDstRect.contains(x, y)) {   // 翻转模式
                    mCurrentMode = FLIP_MODE;
                    ret = true;
                } else if (mDstRect.contains(x, y)) {       // 移动模式
                    // 被选中一张贴图
                    ret = true;
                    mCurrentMode = MOVE_MODE;
                    layout_x = x;
                    layout_y = y;
                } else {
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                ret = true;
                if (mCurrentMode == MOVE_MODE) {            // 移动贴图
                    float dx = x - layout_x;
                    float dy = y - layout_y;

                    matrix.postTranslate(dx, dy);
                    mDstRect.offset(dx, dy);

                    mBorderDstRect.offset(dx, dy);
                    mDeleteRect.offset(dx, dy);
                    mRotateRect.offset(dx, dy);
                    mFlipRect.offset(dx, dy);

                    mDeleteDstRect.offset(dx, dy);
                    mScaleDstRect.offset(dx, dy);
                    mFlipDstRect.offset(dx, dy);

                    layout_x = x;
                    layout_y = y;
                } else if (mCurrentMode == ROTATE_MODE) {   // 旋转 缩放图片操作
                    float dx = x - layout_x;
                    float dy = y - layout_y;
                    calculateRotationAndScale(dx, dy);
                    layout_x = x;
                    layout_y = y;
                }
                break;

            case MotionEvent.ACTION_UP:

                if (mCurrentMode == FLIP_MODE) {            // 翻转模式
                    // 判断是否点击了翻转按钮
                    if (mFlipDstRect.contains(x, y)) {
                        if (isHorizontal) {
                            mFlipBitmap = mVerticalBitmap;
                            mBitmap = BitmapUtils.flipBitmap(mBitmap, true, false, true);
                        } else {
                            mFlipBitmap = mHorizonBitmap;
                            mBitmap = BitmapUtils.flipBitmap(mBitmap, false, true, true);
                        }
                        isHorizontal = !isHorizontal;
                    }
                    invalidate();
                } else if (mCurrentMode == DELETE_MODE) {   // 处于删除模式
                    if (mDeleteDstRect.contains(x, y)) {
                        if (mEditListener != null) {
                            mEditListener.onDelete(this);
                        }
                    }
                    invalidate();
                }
                ret = false;
                mCurrentMode = IDLE_MODE;
                break;

            case MotionEvent.ACTION_CANCEL:
                ret = false;
                mCurrentMode = IDLE_MODE;
                break;



        }
        return ret;
    }

    /**
     * 计算旋转缩放
     * @param dx
     * @param dy
     */
    public void calculateRotationAndScale(final float dx, final float dy) {
        float centerX = mDstRect.centerX();
        float centerY = mDstRect.centerY();

        float scaleCenterX = mScaleDstRect.centerX();
        float scaleCenterY = mScaleDstRect.centerY();

        float newCenterX = scaleCenterX + dx;
        float newCenterY = scaleCenterY + dy;

        float diffX = scaleCenterX - centerX;
        float diffY = scaleCenterY - centerY;

        float newDiffX = newCenterX - centerX;
        float newDiffY = newCenterY - centerY;

        float srcLen = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        float curLen = (float) Math.sqrt(newDiffX * newDiffX + newDiffY * newDiffY);


        float scale = curLen / srcLen;

        float newWidth = mDstRect.width() * scale;
        if (newWidth / initWidth < MIN_SCALE) {
            return;
        }
        matrix.postScale(scale, scale, mDstRect.centerX(), mDstRect.centerY());
        RectUtils.scale(mDstRect, scale);

        // 重新计算边框的位置
        mBorderDstRect.set(mDstRect);
        updateBorderDestRect();

        // 更新翻转按钮、删除按钮、旋转按钮的位置
        mRotateRect.offsetTo(mBorderDstRect.right - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.bottom - STICKER_BTN_HALF_SIZE);
        mDeleteRect.offsetTo(mBorderDstRect.left - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.top - STICKER_BTN_HALF_SIZE);
        mFlipRect.offsetTo(mBorderRect.right - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.top - STICKER_BTN_HALF_SIZE);

        // 平移到最终的位置
        mDeleteDstRect.offsetTo(mBorderDstRect.right - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.bottom - STICKER_BTN_HALF_SIZE);
        mScaleDstRect.offsetTo(mBorderDstRect.left - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.top - STICKER_BTN_HALF_SIZE);
        mFlipDstRect.offsetTo(mBorderRect.right - STICKER_BTN_HALF_SIZE,
                mBorderDstRect.top - STICKER_BTN_HALF_SIZE);

        double cos = (diffX * newDiffX + diffY * newDiffY) / (srcLen * curLen);
        if (cos > 1 || cos < -1) {
            return;
        }
        float angle = (float) Math.toDegrees(Math.acos(cos));

        // 选定旋转方向
        float calMatrix = diffX * newDiffY - newDiffX * diffY;
        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        // 计算旋转角度
        mRotateAngle += angle;
        matrix.postRotate(angle, mDstRect.centerX(), mDstRect.centerY());

        // 计算旋转后删除按钮的位置
        RectUtils.rotate(mDeleteDstRect, mDstRect.centerX(), mDstRect.centerY(), mRotateAngle);

        // 计算旋转后缩放按钮的位置
        RectUtils.rotate(mScaleDstRect, mDstRect.centerX(), mDstRect.centerY(), mRotateAngle);

        // 计算旋转后翻转按钮的位置
        RectUtils.rotate(mFlipDstRect, mDstRect.centerX(), mDstRect.centerY(), mRotateAngle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制贴纸
        canvas.drawBitmap(mBitmap, matrix, null);

        // 绘制边框
        if (isDrawBorder) {
            canvas.save();

            // 绘制边框
            canvas.rotate(mRotateAngle, mBorderDstRect.centerX(), mBorderDstRect.centerY());
            canvas.drawRoundRect(mBorderDstRect, 10, 10, mBorderPaint);

            // 绘制删除按钮
            canvas.drawBitmap(mDeleteBitmap, mBorderRect, mDeleteDstRect, null);
            // 绘制缩放按钮
            canvas.drawBitmap(mScaleBitmap, mBorderRect, mScaleDstRect, null);
            // 绘制翻转按钮
            canvas.drawBitmap(mFlipBitmap, mBorderRect, mFlipDstRect, null);
            canvas.restore();
        }
    }

    /**
     * 贴纸编辑
     */
    public interface StickerEditListener {

        // 删除回调
        void onDelete(StickerView stickerView);

    }

    /**
     * 设置贴纸编辑监听器
     * @param listener
     */
    public void setStickerEditListener(StickerEditListener listener) {
        mEditListener = listener;
    }

}
