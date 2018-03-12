package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.cgfay.caincamera.R;

/**
 * 调整编辑器
 * Created by Administrator on 2018/3/12.
 */

public class AdjustEditer extends BaseEditer implements View.OnClickListener, SeekBar.OnSeekBarChangeListener  {

    // 原始滤镜值
    private static final float[] OriginalValues = new float[] { 0, 1.0f, 0, 0, 1.0f, 0 };

    // 滤镜值索引
    private static final int BrightnessIndex = 0;
    private static final int ContrastIndex = 1;
    private static final int ExposureIndex = 2;
    private static final int HueIndex = 3;
    private static final int SaturationIndex = 4;
    private static final int SharpnessIndex = 5;
    // Seekbar的最大值
    private static final int SeekBarMax = 100;

    // 调节视图
    private LinearLayout mLayoutAdjust;
    private SeekBar mSeekbar;
    private Button mBtnBrightness;
    private Button mBtnContrast;
    private Button mBtnExposure;
    private Button mBtnHue;
    private Button mBtnSaturation;
    private Button mBtnSharpness;

    // 用于记录六种选项值
    private float[] mValues = OriginalValues;
    private int mCurrentFilterIndex;


    public AdjustEditer(Context context) {
        super(context);
    }

    /**
     * 初始化调节视图
     */
    protected void initView() {
        mLayoutAdjust = (LinearLayout) mInflater.inflate(R.layout.view_image_edit_adjust, null);
        mSeekbar = (SeekBar) mLayoutAdjust.findViewById(R.id.edit_value);
        mBtnBrightness = (Button) mLayoutAdjust.findViewById(R.id.btn_brightness);
        mBtnContrast = (Button) mLayoutAdjust.findViewById(R.id.btn_contrast);
        mBtnExposure = (Button) mLayoutAdjust.findViewById(R.id.btn_exposure);
        mBtnHue = (Button) mLayoutAdjust.findViewById(R.id.btn_hue);
        mBtnSaturation = (Button) mLayoutAdjust.findViewById(R.id.btn_saturation);
        mBtnSharpness = (Button) mLayoutAdjust.findViewById(R.id.btn_sharpness);

        mSeekbar.setMax(SeekBarMax);
        mSeekbar.setOnSeekBarChangeListener(this);
        mBtnBrightness.setOnClickListener(this);
        mBtnContrast.setOnClickListener(this);
        mBtnExposure.setOnClickListener(this);
        mBtnHue.setOnClickListener(this);
        mBtnSaturation.setOnClickListener(this);
        mBtnSharpness.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 亮度
            case R.id.btn_brightness: {
                resetAdjustButtonColor();
//                ImageRenderManager.getInstance().setBrightness(mValues[BrightnessIndex]);
                mCurrentFilterIndex = BrightnessIndex;
                int progress = (int) (mValues[BrightnessIndex] * SeekBarMax);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnBrightness.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 对比度
            case R.id.btn_contrast: {
                resetAdjustButtonColor();
//                ImageRenderManager.getInstance().setContrast(mValues[ContrastIndex]);
                mCurrentFilterIndex = ContrastIndex;
                int progress = (int) (mValues[ContrastIndex] * SeekBarMax / 2.0f);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnContrast.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 曝光
            case R.id.btn_exposure: {
                resetAdjustButtonColor();
//                ImageRenderManager.getInstance().setExposure(mValues[ExposureIndex]);
                mCurrentFilterIndex = ExposureIndex;
                int progress = (int) (mValues[ExposureIndex] * SeekBarMax);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnExposure.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 色调
            case R.id.btn_hue: {
                resetAdjustButtonColor();
//                ImageRenderManager.getInstance().setHue(mValues[HueIndex]);
                mCurrentFilterIndex = HueIndex;
                int progress = (int) (mValues[HueIndex] * SeekBarMax / 360f);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnHue.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 饱和度
            case R.id.btn_saturation: {
                resetAdjustButtonColor();
//                ImageRenderManager.getInstance().setSaturation(mValues[SaturationIndex]);
                mCurrentFilterIndex = SaturationIndex;
                int progress = (int) (mValues[SaturationIndex] * SeekBarMax / 2.0f);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnSaturation.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 锐度
            case R.id.btn_sharpness: {
                resetAdjustButtonColor();
//                ImageRenderManager.getInstance().setSharpness(mValues[SharpnessIndex]);
                mCurrentFilterIndex = SharpnessIndex;
                int progress = (int) (mValues[SharpnessIndex] * SeekBarMax);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnSharpness.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
                break;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            setFilterValues(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mWeakTextView != null && mWeakTextView.get() != null) {
            mWeakTextView.get().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mWeakTextView != null && mWeakTextView.get() != null) {
            mWeakTextView.get().setVisibility(View.GONE);
        }
    }

    /**
     * 重置按钮颜色
     */
    private void resetAdjustButtonColor() {
        mBtnBrightness.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnContrast.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnExposure.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnHue.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnSaturation.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnSharpness.setTextColor(mContext.getResources().getColor(R.color.white));
    }

    /**
     * 获取调整视图
     * @return
     */
    public LinearLayout getLayoutAdjust() {
        return mLayoutAdjust;
    }


    /**
     * 设置滤镜值
     * @param progress seekbar的进度值
     */
    private void setFilterValues(int progress) {
        float value = (float) progress / (float) SeekBarMax;
        // 计算百分比
        float text = (float)Math.round(value * 100);
        String string = text + "%";
        // 设置显示的值
        if (mWeakTextView != null && mWeakTextView.get() != null) {
            mWeakTextView.get().setText(string);
        }
        // 调整实际的率净值
        if (mCurrentFilterIndex == HueIndex) {
            value = value * 360f; // 色调在0 ~ 360度之间变化
        } else if (mCurrentFilterIndex == SaturationIndex
                || mCurrentFilterIndex == ContrastIndex) {
            value = value * 2.0f; // 对比度在0 ~ 2之间变化
        }

        // 缓存当前的滤镜值
        mValues[mCurrentFilterIndex] = value;
        switch (mCurrentFilterIndex) {
            // 亮度
            case BrightnessIndex:
                break;

            // 对比度
            case ContrastIndex:
                break;

            // 曝光
            case ExposureIndex:
                break;

            // 色调
            case HueIndex:
                break;

            // 饱和度
            case SaturationIndex:
                break;

            // 锐度
            case SharpnessIndex:
                break;
        }
    }

}
