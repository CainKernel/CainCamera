package com.cgfay.video.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.uitls.utils.DensityUtils;
import com.cgfay.video.R;

/**
 * 音乐裁剪控件
 */
public class WaveCutView extends View {

    private final int WAVE_COUNT = 45;
    private int[] mHeights;

    private int mMaxCount = 50;
    private int mSelectedCount = 15;

    private int mSelectedColor = 0x80F8CE17;
    private int mDefaultColor = 0x80FFFFFF;


    private boolean isDragging;


    private int mImageWidth;
    private int mImageHeight;
    private int mImagePositonX;
    private int mImagePositonY;

    private Bitmap mImageBitmap;

    private int mProgress;

    private OnDragListener mOnDragListener;

    public WaveCutView(Context context) {
        this(context, null);
    }

    public WaveCutView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveCutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHeights = new int[] {
                DensityUtils.dp2px(context, 20), DensityUtils.dp2px(context,27),
                DensityUtils.dp2px(context,23), DensityUtils.dp2px(context,34),
                DensityUtils.dp2px(context,42), DensityUtils.dp2px(context,36),
                DensityUtils.dp2px(context,32), DensityUtils.dp2px(context,41),
                DensityUtils.dp2px(context,21),DensityUtils.dp2px(context,27),
                DensityUtils.dp2px(context,16)
        };
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveCutView, defStyleAttr,0);
        mSelectedColor = array.getColor(R.styleable.WaveCutView_selectedColor, 0x80F8CE17);
        mDefaultColor = array.getColor(R.styleable.WaveCutView_defaultColor,0x80FFFFFF);
        mImageWidth = array.getDimensionPixelOffset(R.styleable.WaveCutView_width, DensityUtils.dp2px(context, 44));
        mImageHeight = array.getDimensionPixelOffset(R.styleable.WaveCutView_height, DensityUtils.dp2px(context, 22));
        mImageBitmap = BitmapFactory.decodeResource(context.getResources(),
                array.getResourceId(R.styleable.WaveCutView_res,
                        R.drawable.icon_video_cut_music_selected));
        array.recycle();
    }

    public void setMax(int maxCount) {
        mMaxCount = maxCount;
        mImagePositonX = 0;
        invalidate();
    }

    public void setSelectedCount(int selectedCount){
        mSelectedCount = selectedCount;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        mImagePositonX = (int) (mProgress/(float)mMaxCount* mWaveWidth);
        invalidate();
    }

    public int getProgress() {
        return mProgress;
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        mOnDragListener = onDragListener;
    }

    int mParentMargin;
    float mWaveWidth;
    private float mMoveX;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mParentMargin = mImageWidth / 2;
        mWaveWidth = getMeasuredWidth() - mParentMargin * 2;
        mImagePositonX = (int) (mProgress/(float)mMaxCount * mWaveWidth);
        mImagePositonY = getMeasuredHeight()-mImageHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eX = event.getX();
        float eY = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && (eX > mImagePositonX && eX < mImagePositonX+mImageWidth)
                && (eY > mImagePositonY && eY < mImagePositonY+mImageHeight)) {
            isDragging = true;
            mMoveX = eX;
        }

        if (isDragging) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mImagePositonX += event.getX() - mMoveX;
                    if (mImagePositonX < 0) {
                        mImagePositonX = 0;
                    }
                    if (mOnDragListener != null) {
                        // 选中区域的宽度
                        float selectedWidth = (getMeasuredWidth()-mParentMargin*2)*mSelectedCount/(float)mMaxCount;
                        // 超过右边区域，限制位置
                        if (mImagePositonX + selectedWidth + mParentMargin > mWaveWidth + mParentMargin) {
                            mProgress = (int) ((mWaveWidth - selectedWidth)/ mWaveWidth *mMaxCount);
                            mOnDragListener.onDragging(mProgress);
                        } else {
                            mProgress = (int) (mImagePositonX/ mWaveWidth *mMaxCount);
                            mOnDragListener.onDragging(mProgress);
                        }
                    }
                    mMoveX = event.getX();
                    invalidate();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    isDragging = false;
                    // 选中区域的宽度
                    float selectedWidth = (getMeasuredWidth()-mParentMargin*2)*mSelectedCount/(float)mMaxCount;
                    // 超过右边区域，限制位置
                    if (mImagePositonX + selectedWidth + mParentMargin > mWaveWidth + mParentMargin) {
                        mImagePositonX = (int) (mWaveWidth - selectedWidth);
                    }
                    if (mOnDragListener != null) {
                        mOnDragListener.onDragFinish(mImagePositonX/ mWaveWidth *mMaxCount);
                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 整个选中区域的宽度
        float selectedWidth = (getMeasuredWidth() - mParentMargin * 2) * mSelectedCount / (float)mMaxCount;

        int tempImagePositionX = mImagePositonX;

        if (tempImagePositionX + selectedWidth + mParentMargin > mWaveWidth + mParentMargin) {
            tempImagePositionX = (int) (mWaveWidth - selectedWidth);
        }

        // 每个波形刻度应该分配的大小
        float baseWaveWidth = mWaveWidth / WAVE_COUNT;

        // 每个波形刻度分配的大小的间距
        float waveRectMargin = mWaveWidth / WAVE_COUNT /4f;

        int centerY = (getMeasuredHeight() - mImageHeight) / 2;

        Rect rect = new Rect(tempImagePositionX, mImagePositonY,
                tempImagePositionX + mImageWidth, mImagePositonY + mImageHeight);
        canvas.drawBitmap(mImageBitmap,null, rect, new Paint());

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 绘制刻度
        for (int i = 0; i < WAVE_COUNT; i++) {
            int index = i % mHeights.length;

            float positionPx = baseWaveWidth * i + mParentMargin;
            RectF rectF = new RectF(positionPx,centerY - mHeights[index] / 2,
                    positionPx + baseWaveWidth - waveRectMargin,centerY + mHeights[index]/2);
            float centerPositionX = tempImagePositionX + mParentMargin;

            // 绘制选中区域的颜色
            if (rectF.left < centerPositionX && rectF.right > centerPositionX) {
                float colorDivision = (centerPositionX-rectF.left)/(baseWaveWidth-waveRectMargin);
                Shader shader = new LinearGradient(rectF.left, rectF.bottom, rectF.right, rectF.bottom,
                        new int[] { mDefaultColor, mSelectedColor},
                        new float[]{colorDivision,colorDivision}, Shader.TileMode.CLAMP);
                paint = new Paint();
                paint.setAntiAlias(true);
                paint.setShader(shader);
            } else if (rectF.left < centerPositionX+selectedWidth && rectF.right > centerPositionX+selectedWidth) {
                float colorDivision = (centerPositionX+selectedWidth-rectF.left)/(baseWaveWidth-waveRectMargin);
                Shader shader = new LinearGradient(rectF.left, rectF.bottom, rectF.right, rectF.bottom,
                        new int[] { mSelectedColor,mDefaultColor},
                        new float[]{colorDivision,colorDivision}, Shader.TileMode.CLAMP);
                paint = new Paint();
                paint.setAntiAlias(true);
                paint.setShader(shader);
            } else if (rectF.left >= centerPositionX && rectF.right <= centerPositionX + selectedWidth) {
                paint.setShader(null);
                paint.setColor(mSelectedColor);
            } else {
                // 没选中的区域
                paint.setShader(null);
                paint.setColor(mDefaultColor);
            }
            canvas.drawRoundRect(rectF,positionPx/2,positionPx/2,paint);
        }
    }

    /**
     * 拖拽监听器
     */
    public interface OnDragListener {

        // 正在拖拽
        void onDragging(int position);

        // 拖拽结束
        void onDragFinish(float position);
    }

}
