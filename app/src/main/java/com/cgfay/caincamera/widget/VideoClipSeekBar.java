package com.cgfay.caincamera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cgfay.caincamera.R;
import com.cgfay.utilslibrary.utils.DensityUtils;

/**
 * 视频剪辑SeekBar
 */
public class VideoClipSeekBar extends View {

    private static final int MAX = 100;

    private int width;
    private int height;
    private RectF bar;
    private RectF rectStart;
    private RectF rectEnd;
    private Paint paintBar;
    private Paint paintTouchBar;
    private float startPosition = -1;
    private float endPosition = -1;
    private float tempX;
    private int touchBarWidth;
    private int margin;
    private int roundCorner;
    private boolean moveStart;
    private boolean moveEnd;
    private int mMax = MAX;

    private OnCutBarChangeListener callBack;

    public VideoClipSeekBar(Context context) {
        this(context, null);
    }

    public VideoClipSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoClipSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void addCutBarChangeListener(OnCutBarChangeListener listener){
        this.callBack = listener;
    }

    public interface OnCutBarChangeListener {

        // 调节起始位置
        void onStartProgressChanged(float screenStartX , int progress);

        // 调节结束位置
        void onEndProgressChanged(float screenEndX , int progress);

        // 处理完成
        void onTouchFinish(int start, int end);
    }

    private void init(Context context) {
        paintBar = new Paint();
        paintBar.setAntiAlias(true);
        paintBar.setColor(getResources().getColor(R.color.blue));

        paintTouchBar = new Paint();
        paintTouchBar.setAntiAlias(true);
        paintTouchBar.setColor(getResources().getColor(R.color.yellow));

        margin = DensityUtils.dp2px(context, 5);

        touchBarWidth = DensityUtils.dp2px(context, 15);
        roundCorner = DensityUtils.dp2px(context, 10);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        bar = new RectF(0, margin, width, height - margin);
        if (startPosition == -1) {
            startPosition = 0;
        }
        if (endPosition == -1) {
            endPosition = width - touchBarWidth;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tempX = event.getX();
                if (isTouchStartBar(tempX)) {
                    moveStart = true;
                    moveEnd = false;
                } else if (isTouchEndBar(tempX)) {
                    moveStart = false;
                    moveEnd = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                tempX = event.getX();
                if (moveStart) {
                    if ((tempX + touchBarWidth) < endPosition && tempX >= 0) {
                        startPosition = tempX;
                        invalidate();
                        if(callBack != null){
                            callBack.onStartProgressChanged(event.getRawX() , (int)((startPosition / width) * mMax));
                        }

                    }
                } else if (moveEnd) {
                    if ((tempX - touchBarWidth) > startPosition && (tempX + touchBarWidth) <= width) {
                        endPosition = tempX;
                        invalidate();
                        if(callBack != null){
                            callBack.onEndProgressChanged(event.getRawX() , (int)(((endPosition + touchBarWidth) / width) * mMax));
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                tempX = event.getX();
                if (moveStart) {

                } else if (moveEnd) {

                }
                if(callBack != null){
                    callBack.onTouchFinish((int)((startPosition / width) * mMax) ,  (int)(((endPosition + touchBarWidth) / width) * mMax));
                }

                moveStart = false;
                moveEnd = false;
                break;
        }

        return true;
    }

    private boolean isTouchStartBar(float x) {
        return (x >= startPosition && x <= (startPosition + touchBarWidth));
    }

    private boolean isTouchEndBar(float x) {
        return (x > endPosition && x <= (endPosition + touchBarWidth));
    }

    public int getMax() {
        return mMax;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawBar(canvas);
        drawStart(canvas);
        drawEnd(canvas);

    }

    private void drawStart(Canvas canvas) {
        rectStart = new RectF(startPosition, 0, touchBarWidth + startPosition, height);
        canvas.drawRoundRect(rectStart, roundCorner, roundCorner, paintTouchBar);
    }

    private void drawEnd(Canvas canvas) {
        rectEnd = new RectF(endPosition, 0, touchBarWidth + endPosition, height);
        canvas.drawRoundRect(rectEnd, roundCorner, roundCorner, paintTouchBar);
    }

    private void drawBar(Canvas canvas) {
        canvas.drawRoundRect(bar, roundCorner, roundCorner, paintBar);
    }
}
