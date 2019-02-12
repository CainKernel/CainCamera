package com.cgfay.video.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cgfay.video.R;
import com.cgfay.video.bean.VideoSpeed;

/**
 * 倍速控制条
 */
public class VideoSpeedLevelBar extends LinearLayout {

    private String[] mSpeedStr;
    private SparseArray<TextView> mSpeedTexts;
    private int mCurrentPosition = 2;
    private boolean touchEnable = true;

    public VideoSpeedLevelBar(Context context) {
        this(context, null);
    }

    public VideoSpeedLevelBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoSpeedLevelBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSpeedStr = context.getResources().getStringArray(R.array.video_speed_texts);
        mSpeedTexts = new SparseArray<>();
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.drawable.bg_video_speed);
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
                if (i == 0) {
                    textView.setBackgroundResource(R.drawable.bg_video_speed_select_left);
                } else if (i == mSpeedTexts.size() - 1) {
                    textView.setBackgroundResource(R.drawable.bg_video_speed_select_right);
                } else {
                    textView.setBackgroundColor(0xFFFFFFFF);
                }
            } else {
                textView.setBackgroundColor(0x000000);
                textView.setTextColor(0x80FFFFFF);
            }
        }
    }

    public void setTouchEnable(boolean enable) {
        touchEnable = enable;
    }

    public VideoSpeed getSpeed() {
        switch (mCurrentPosition) {
            case 0:
                return VideoSpeed.SPEED_L0;
            case 1:
                return VideoSpeed.SPEED_L1;
            case 2:
                return VideoSpeed.SPEED_L2;
            case 3:
                return VideoSpeed.SPEED_L3;
            case 4:
                return VideoSpeed.SPEED_L4;
        }
        return VideoSpeed.SPEED_L2;
    }

    public void setSpeed(VideoSpeed speed) {
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

        void onSpeedChanged(VideoSpeed speed);
    }

    public void setOnSpeedChangedListener(OnSpeedChangedListener listener) {
        mOnSpeedChangedListener = listener;
    }

    private OnSpeedChangedListener mOnSpeedChangedListener;

}
