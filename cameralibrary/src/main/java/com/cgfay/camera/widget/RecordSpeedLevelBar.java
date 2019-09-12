package com.cgfay.camera.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;

/**
 * 录制速度条
 */
public class RecordSpeedLevelBar extends LinearLayout {

    private String[] mSpeedStr;
    private SparseArray<TextView> mSpeedTexts;
    private int mCurrentPosition = 2;
    private boolean touchEnable = true;

    public RecordSpeedLevelBar(Context context) {
        this(context, null);
    }

    public RecordSpeedLevelBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordSpeedLevelBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSpeedStr = context.getResources().getStringArray(R.array.record_speed_texts);
        mSpeedTexts = new SparseArray<>();
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.drawable.bg_record_speed);
        mSpeedTexts.clear();
        for (int i = 0; i < mSpeedStr.length; i++) {
            TextView view = new TextView(context);
            view.setTextSize(15);
            view.setTextColor(0x80FFFFFF);
            view.setGravity(Gravity.CENTER);
            view.setText(mSpeedStr[i]);

            final int index = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!touchEnable || mCurrentPosition == index) {
                        return;
                    }
                    mCurrentPosition = index;
                    invalidateSpeed();
                    if (mOnSpeedChangedListener != null) {
                        mOnSpeedChangedListener.onSpeedChanged(getSpeed());
                    }
                }
            });

            LayoutParams textViewParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            textViewParams.gravity = Gravity.CENTER;
            this.addView(view, textViewParams);
            mSpeedTexts.append(i, view);
            invalidateSpeed();
        }
    }

    public void invalidateSpeed() {
        for (int i = 0; i < mSpeedTexts.size(); i++) {
            TextView textView = mSpeedTexts.get(i);
            if (i == mCurrentPosition) {
                textView.setTextColor(0x80000000);
                textView.setBackgroundResource(R.drawable.bg_record_speed_select);
            } else {
                textView.setBackgroundColor(0x000000);
                textView.setTextColor(0x80FFFFFF);
            }
        }
    }

    public void setTouchEnable(boolean enable) {
        touchEnable = enable;
    }

    public RecordSpeed getSpeed() {
        switch (mCurrentPosition) {
            case 0:
                return RecordSpeed.SPEED_L0;
            case 1:
                return RecordSpeed.SPEED_L1;
            case 2:
                return RecordSpeed.SPEED_L2;
            case 3:
                return RecordSpeed.SPEED_L3;
            case 4:
                return RecordSpeed.SPEED_L4;
        }
        return RecordSpeed.SPEED_L2;
    }

    public void setSpeed(RecordSpeed speed) {
        switch (speed) {
            case SPEED_L0:
                mCurrentPosition = 0;
                break;

            case SPEED_L1:
                mCurrentPosition = 1;
                break;

            case SPEED_L2:
                mCurrentPosition = 2;
                break;

            case SPEED_L3:
                mCurrentPosition = 3;
                break;

            case SPEED_L4:
                mCurrentPosition = 4;
                break;
        }
        invalidateSpeed();
    }

    public interface OnSpeedChangedListener {

        void onSpeedChanged(RecordSpeed speed);
    }

    public void setOnSpeedChangedListener(OnSpeedChangedListener listener) {
        mOnSpeedChangedListener = listener;
    }

    private OnSpeedChangedListener mOnSpeedChangedListener;

    /**
     * 录制速度枚举
     */
    public enum RecordSpeed {

        SPEED_L0(-2, 1/3f),     // L0 : 极慢 (倍速: 1/3)
        SPEED_L1(-1, 1/2f),     // L1 : 较慢 (倍速: 1/2)
        SPEED_L2(0, 1.0f),      // L2 : 标准 (倍速: 1.0, 默认速度)
        SPEED_L3(1, 2.0f),      // L3 : 较快 (倍速: 2.0)
        SPEED_L4(2, 3.0f);      // L4 : 极快 (倍速: 3.0)

        private int type;
        private float speed;

        public int getType() {
            return type;
        }

        public float getSpeed() {
            return speed;
        }

        RecordSpeed(int type, float speed) {
            this.type = type;
            this.speed = speed;
        }
    }
}
