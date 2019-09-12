package com.cgfay.camera.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.cgfay.cameralibrary.R;
import com.cgfay.camera.engine.model.AspectRatio;

/**
 * 长宽比图片视图
 * Created by cain.huang on 2017/12/16.
 */

@SuppressLint("AppCompatCustomView")
public class RatioImageView extends ImageView implements View.OnClickListener {

    private int[] mImageIds = {
            R.drawable.ic_camera_ratio_11_light,
            R.drawable.ic_camera_ratio_34_light,
            R.drawable.ic_camera_ratio_916_light
    };

    private OnRatioChangedListener mListener;

    private AspectRatio mRatioType = AspectRatio.RATIO_4_3;

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
    public void addRatioChangedListener(OnRatioChangedListener listener) {
        mListener = listener;
    }

    /**
     * 设置当前的比例
     * @param type
     */
    public void setRatioType(AspectRatio type) {
        mRatioType = type;
        updateImageUI();
    }

    private void updateImageUI() {
        if (mRatioType == AspectRatio.RATIO_1_1) {
            setBackgroundResource(mImageIds[0]);
        } else if (mRatioType == AspectRatio.RATIO_4_3) {
            setBackgroundResource(mImageIds[1]);
        } else if (mRatioType == AspectRatio.Ratio_16_9) {
            setBackgroundResource(mImageIds[2]);
        }
    }

    @Override
    public void onClick(View v) {
        changeRatioTypeNext();
        if (mListener != null) {
            mListener.onRatioChanged(mRatioType);
        }
    }

    /**
     * 切换到下一状态
     */
    private void changeRatioTypeNext() {
        if (mRatioType == AspectRatio.RATIO_1_1) {
            mRatioType = AspectRatio.RATIO_4_3;
        } else if (mRatioType == AspectRatio.RATIO_4_3) {
            mRatioType = AspectRatio.Ratio_16_9;
        } else if (mRatioType == AspectRatio.Ratio_16_9) {
            mRatioType = AspectRatio.RATIO_1_1;
        }
        updateImageUI();
    }

    /**
     * 获取当前的比例
     * @return
     */
    public AspectRatio getRatioType() {
        return mRatioType;
    }


    /**
     * 长宽比改变监听器
     */
    public interface OnRatioChangedListener {
        void onRatioChanged(AspectRatio type);
    }

}
