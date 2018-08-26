package com.cgfay.imagelibrary.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.imagelibrary.R;
import com.cgfay.utilslibrary.utils.RectUtils;

/**
 * 文字贴图控件
 * Created by cain.huang on 2017/12/15.
 */
public class TextStickerView extends View {

    public static final float TEXT_SIZE_DEFAULT = 80;
    public static final int PADDING = 30;
    public static final int STICKER_BTN_HALF_SIZE = 30;

    private static final int IDLE_MODE = 0;     // 空闲模式
    private static final int MOVE_MODE = 1;     // 移动模式
    private static final int ROTATE_MODE = 2;   // 旋转模式
    private static final int DELETE_MODE = 3;   // 删除模式
    private static final int EDIT_MODE = 4;     // 编辑模式

    private TextPaint mPaint = new TextPaint(); // 绘制文字的Paint
    private Paint mBorderPaint = new Paint();   // 绘制边框的Paint

    private Rect mTextRect = new Rect();        // 文字位置
    private RectF mBorderRect = new RectF();    // 边框位置

    private Rect mDeleteRect = new Rect();      // 删除按钮的位置
    private Rect mScaleRect = new Rect();       // 缩放按钮的位置
    private Rect mEditRect = new Rect();        // 编辑按钮的位置

    private RectF mDeleteDstRect;               // 删除按钮目标位置
    private RectF mScaleDstRect;                // 缩放按钮目标位置
    private RectF mEditDstRect;                 // 编辑按钮目标位置

    private Bitmap mDeleteBitmap;               // 删除按钮图片
    private Bitmap mScaleBitmap;                // 缩放按钮图片
    private Bitmap mEditBitmap;                 // 编辑按钮图片

    private int mCurrentMode = IDLE_MODE;       // 当前模式

    private float mRotateAngle = 0;             // 旋转角度
    private float mScale = 1.0f;                // 缩放倍数

    private int layout_x = 0;
    private int layout_y = 0;

    private float last_x = 0;                   // 记录上一次的位置x
    private float last_y = 0;                   // 记录上一次的位置y

    private boolean isDrawBorder = true;       // 显示边框

    private String mText;                       // 文字

    private TextStickerEditListener mEditListener;  // 编辑回调

    public TextStickerView(Context context) {
        super(context);
        initView(context);
    }

    public TextStickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TextStickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mDeleteBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_dialog_del_normal);

        mScaleBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_dialog_scale_normal);

        mEditBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_dialog_edit_normal);

        mDeleteRect.set(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());
        mScaleRect.set(0, 0, mScaleBitmap.getWidth(), mScaleBitmap.getHeight());
        mEditRect.set(0, 0, mEditBitmap.getWidth(), mEditBitmap.getHeight());

        mDeleteDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
        mScaleDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
        mEditDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);

        // 绘制文字的Paint
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(TEXT_SIZE_DEFAULT);
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.LEFT);

        // 绘制边框的Paint
        mBorderPaint.setColor(Color.WHITE);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStrokeWidth(4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mDeleteDstRect.contains(x, y)) {        // 删除模式
                    isDrawBorder = true;
                    mCurrentMode = DELETE_MODE;
                    ret = true;
                } else if (mScaleDstRect.contains(x, y)) {  // 旋转按钮
                    isDrawBorder = true;
                    mCurrentMode = ROTATE_MODE;
                    last_x = mScaleDstRect.centerX();
                    last_y = mScaleDstRect.centerY();
                    ret = true;
                } else if (mEditDstRect.contains(x, y)) {   // 编辑按钮
                    isDrawBorder = true;
                    mCurrentMode = EDIT_MODE;
                    ret = true;
                } else if (mBorderRect.contains(x, y)) {    // 移动模式
                    isDrawBorder = true;
                    mCurrentMode = MOVE_MODE;
                    last_x = x;
                    last_y = y;
                    ret = true;
                } else {
                    isDrawBorder = false;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                ret = true;
                if (mCurrentMode == MOVE_MODE) {            // 移动贴图
                    mCurrentMode = MOVE_MODE;
                    float dx = x - last_x;
                    float dy = y - last_y;

                    layout_x += dx;
                    layout_y += dy;

                    invalidate();

                    last_x = x;
                    last_y = y;
                } else if (mCurrentMode == ROTATE_MODE) {   // 旋转 缩放文字操作
                    mCurrentMode = ROTATE_MODE;
                    float dx = x - last_x;
                    float dy = y - last_y;

                    calculateRotationAndScale(dx, dy);

                    invalidate();
                    last_x = x;
                    last_y = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mCurrentMode == EDIT_MODE) {            // 编辑模式
                    if (mEditDstRect.contains(x, y)) {
                        if (mEditListener != null) {
                            mEditListener.onEdit(this);
                        }
                    }
                } else if (mCurrentMode == DELETE_MODE) {   // 处于删除状态
                    // 判断此时松手是否仍处于删除按钮，即表示点击删除
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
        float centerX = mBorderRect.centerX();
        float centerY = mBorderRect.centerY();

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

        mScale *= scale;
        float newWidth = mBorderRect.width() * mScale;

        if (newWidth < 70) {
            mScale /= scale;
            return;
        }

        // 计算旋转方向和角度
        double cos = (diffX * newDiffX + diffY * newDiffY) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        // 方向
        float calMatrix = diffX * newDiffY - newDiffX * diffY;
        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;
        mRotateAngle += angle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(mText)) {
            return;
        }
        drawContent(canvas);
    }

    /**
     * 绘制内容
     * @param canvas
     */
    private void drawContent(Canvas canvas) {
        // 绘制文字
        drawText(canvas);

        // 计算删除按钮中心位置
        int deleteOffset = ((int) mDeleteDstRect.width()) >> 1;
        mDeleteDstRect.offsetTo(mBorderRect.left - deleteOffset, mBorderRect.top - deleteOffset);

        // 计算缩放按钮中心位置
        int scaleOffset = ((int) mScaleDstRect.width()) >> 1;
        mScaleDstRect.offsetTo(mBorderRect.right - scaleOffset, mBorderRect.bottom - scaleOffset);

        // 计算编辑按钮中心位置
        int editOffset = ((int) mEditDstRect.width()) >> 1;
        mEditDstRect.offsetTo(mBorderRect.left - editOffset, mBorderRect.bottom - editOffset);

        // 计算旋转后删除按钮的位置
        RectUtils.rotate(mDeleteDstRect, mBorderRect.centerX(), mBorderRect.centerY(), mRotateAngle);

        // 计算旋转后缩放按钮的位置
        RectUtils.rotate(mScaleDstRect, mBorderRect.centerX(), mBorderRect.centerY(), mRotateAngle);

        // 计算旋转后编辑按钮的位置
        RectUtils.rotate(mEditDstRect, mBorderRect.centerX(), mBorderRect.centerY(), mRotateAngle);

        // 是否显示边框
        if (!isDrawBorder) {
            return;
        }

        // 绘制边框
        canvas.save();
        canvas.rotate(mRotateAngle, mBorderRect.centerX(), mBorderRect.centerY());
        canvas.drawRoundRect(mBorderRect, 10, 10, mBorderPaint);
        canvas.restore();

        // 绘制删除按钮、缩放按钮、编辑按钮
        canvas.drawBitmap(mDeleteBitmap, mDeleteRect, mDeleteDstRect, null);
        canvas.drawBitmap(mScaleBitmap, mScaleRect, mScaleDstRect, null);
        canvas.drawBitmap(mEditBitmap, mEditRect, mEditDstRect, null);
    }

    /**
     * 绘制文字
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        drawText(canvas, layout_x, layout_y, mScale, mRotateAngle);
    }

    /**
     * 绘制文字
     * @param canvas
     * @param x
     * @param y
     * @param scale
     * @param rotate
     */
    private void drawText(Canvas canvas, int x, int y, float scale, float rotate) {

        mTextRect.set(0, 0, 0, 0);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        // 字体高度
        int charHeight = Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom);
        // 获取字体的大小
        mPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
        // 平移到当前位置
        mTextRect.offset(x, y);

        // 计算边框位置
        mBorderRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                , mTextRect.right + PADDING, mTextRect.bottom + PADDING);
        // 缩放边框
        RectUtils.scale(mBorderRect, scale);

        // 计算绘制位置和旋转方向
        canvas.save();
        canvas.scale(scale, scale, mBorderRect.centerX(), mBorderRect.centerY());
        canvas.rotate(rotate, mBorderRect.centerX(), mBorderRect.centerY());

        // 绘制文字
        canvas.drawText(mText, x, y + (charHeight >> 1) + PADDING, mPaint);
        canvas.restore();
    }

    /**
     * 重置视图
     */
    public void resetView() {
        layout_x = getMeasuredWidth() / 2;
        layout_y = getMeasuredHeight() / 2;
        mRotateAngle = 0;
        mScale = 1;
        mText = null;
    }

    /**
     * 设置文字
     * @param text
     */
    public void setText(String text) {
        mText = text;
        invalidate();
    }

    /**
     * 设置文字颜色
     * @param newColor
     */
    public void setTextColor(@ColorInt int newColor) {
        mPaint.setColor(newColor);
        invalidate();
    }

    /**
     * 获取缩放比例
     * @return
     */
    public float getScale() {
        return mScale;
    }

    /**
     * 文字编辑回调
     */
    public interface TextStickerEditListener {

        // 删除
        void onDelete(TextStickerView textStickerView);

        // 编辑
        void onEdit(TextStickerView textStickerView);
    }

    /**
     * 设置文字编辑回调
     * @param listener
     */
    public void setStickerEditListener(TextStickerEditListener listener) {
        mEditListener = listener;
    }
}
