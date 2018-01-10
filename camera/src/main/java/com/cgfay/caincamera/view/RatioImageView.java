package com.cgfay.caincamera.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.type.AspectRatioType;

/**
 * 长宽比图片视图
 * Created by cain on 2017/12/16.
 */

@SuppressLint("AppCompatCustomView")
public class RatioImageView extends ImageView implements View.OnClickListener {

    private int[] mImageIds = {
            R.drawable.preview_radio_1_1,
            R.drawable.preview_radio_4_3,
            R.drawable.preview_radio_16_9
    };

    private RatioChangedListener mListener;

    private AspectRatioType mRatioType = AspectRatioType.Ratio_16_9;

    public RatioImageView(Context context) {
        super(context);
        init();
    }

    public RatioImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RatioImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setRatioType(mRatioType);
        setOnClickListener(this);
    }

    /**
     * 添加长宽比变更监听
     * @param listener
     */
    public void addRatioChangedListener(RatioChangedListener listener) {
        mListener = listener;
    }

    /**
     * 设置当前的比例
     * @param type
     */
    public void setRatioType(AspectRatioType type) {
        mRatioType = type;
        updateImageUI();
    }

    private void updateImageUI() {
        if (mRatioType == AspectRatioType.RATIO_1_1) {
            setBackgroundResource(mImageIds[0]);
        } else if (mRatioType == AspectRatioType.RATIO_4_3) {
            setBackgroundResource(mImageIds[1]);
        } else if (mRatioType == AspectRatioType.Ratio_16_9) {
            setBackgroundResource(mImageIds[2]);
        }
    }

    @Override
    public void onClick(View v) {
        changeRatioTypeNext();
        if (mListener != null) {
            mListener.ratioChanged(mRatioType);
        }
    }

    /**
     * 切换到下一状态
     */
    private void changeRatioTypeNext() {
        if (mRatioType == AspectRatioType.RATIO_1_1) {
            mRatioType = AspectRatioType.RATIO_4_3;
        } else if (mRatioType == AspectRatioType.RATIO_4_3) {
            mRatioType = AspectRatioType.Ratio_16_9;
        } else if (mRatioType == AspectRatioType.Ratio_16_9) {
            mRatioType = AspectRatioType.RATIO_1_1;
        }
        updateImageUI();
    }

    /**
     * 获取当前的比例
     * @return
     */
    public AspectRatioType getRatioType() {
        return mRatioType;
    }


    public interface RatioChangedListener {
        void ratioChanged(AspectRatioType type);
    }

}
