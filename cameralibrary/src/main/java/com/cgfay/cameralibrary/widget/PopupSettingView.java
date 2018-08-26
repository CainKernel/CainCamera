package com.cgfay.cameralibrary.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;

/**
 * 设置弹窗界面
 * Created by cain.huang on 2017/12/15.
 */

public class PopupSettingView extends BasePopupWindow
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // 触屏拍摄
    private LinearLayout mLayoutTouchTake;
    private ImageView mImageTouchTake;
    private TextView mTextTouchTake;
    // 闪光灯
    private LinearLayout mLayoutFlash;
    private ImageView mImageFlash;
    private TextView mTextFlash;
    // 延时拍摄
    private LinearLayout mLayoutTimeLapse;
    private ImageView mImageTimeLapse;
    private TextView mTextTimeLapse;
    // 相机设置
    private LinearLayout mLayoutCameraSetting;
    // 自动保存
    private Switch mLuminousCompensation;

    // 是否允许切换闪光灯状态，没有闪关灯时不允许切换，默认不允许
    private boolean mEnableChangeFlash = false;
    // 是否允许闪光灯，默认关闭
    private boolean mEnableFlash = false;
    // 默认不开启延时拍摄
    private boolean mDelayTakePicture = false;
    // 夜光补偿
    private boolean canLuminousCompensation = false;
    // 触屏拍照
    private boolean canTouchTake = false;

    // 按钮状态监听器
    private StateChangedListener mListener;

    public PopupSettingView(Context context) {
        super(context);
        init();
    }

    private void init() {

        View contentView = LayoutInflater.from(mContext).inflate(R.layout.view_preview_pop_setting, null);
        setContentView(contentView);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        mLayoutFlash = (LinearLayout) contentView.findViewById(R.id.layout_flash);
        mImageFlash = (ImageView) contentView.findViewById(R.id.iv_flash);
        mTextFlash = (TextView) contentView.findViewById(R.id.tv_flash);

        mLayoutTouchTake = (LinearLayout) contentView.findViewById(R.id.layout_touch_take);
        mImageTouchTake = (ImageView) contentView.findViewById(R.id.iv_touch_take);
        mTextTouchTake = (TextView) contentView.findViewById(R.id.tv_touch_take);

        mLayoutCameraSetting = (LinearLayout) contentView.findViewById(R.id.layout_camera_setting);

        mLayoutTimeLapse = (LinearLayout) contentView.findViewById(R.id.layout_time_lapse);
        mImageTimeLapse = (ImageView) contentView.findViewById(R.id.iv_time_lapse);
        mTextTimeLapse = (TextView) contentView.findViewById(R.id.tv_time_lapse);

        mLuminousCompensation = (Switch) contentView.findViewById(R.id.sw_luminous_compensation);

        mLayoutFlash.setOnClickListener(this);
        mLayoutTouchTake.setOnClickListener(this);
        mLayoutCameraSetting.setOnClickListener(this);
        mLayoutTimeLapse.setOnClickListener(this);

        mLuminousCompensation.setOnCheckedChangeListener(this);

        updateFlashUI();
        updateTouchTakenUI();
        updateTimeLapseUI();
        updateLuminousCompensationUI();

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.layout_flash) {
            processFlash();
        } else if (i == R.id.layout_touch_take) {
            processTouchTake();
        } else if (i == R.id.layout_camera_setting) {
            if (mListener != null) {
                mListener.onOpenCameraSetting();
            }
        } else if (i == R.id.layout_time_lapse) {
            processTimeLapse();
        }
    }

    /**
     * 处理闪光灯
     */
    private void processFlash() {
        if (!mEnableChangeFlash) {
            return;
        }
        mEnableFlash = !mEnableFlash;
        updateFlashUI();
        if (mListener != null) {
            mListener.flashStateChanged(mEnableFlash);
        }
    }

    /**
     * 更新设置闪光灯值
     */
    private void updateFlashUI() {
        if (mEnableFlash) {
            mImageFlash.setBackgroundResource(R.drawable.ic_camera_flash_on);
            mTextFlash.setTextColor(mContext.getResources().getColor(R.color.white));
        } else {
            mImageFlash.setBackgroundResource(R.drawable.ic_camera_flash_off);
            mTextFlash.setTextColor(mContext.getResources().getColor(R.color.popup_text_normal));
        }
    }

    /**
     * 处理触屏拍照
     */
    private void processTouchTake() {
        canTouchTake = !canTouchTake;
        updateTouchTakenUI();
        if (mListener != null) {
            mListener.touchTakenChanged(canTouchTake);
        }
    }

    /**
     * 更新触屏拍摄UI
     */
    private void updateTouchTakenUI() {
        if (canTouchTake) {
            mImageTouchTake.setBackgroundResource(R.drawable.ic_camera_setting_more_light);
            mTextTouchTake.setTextColor(mContext.getResources().getColor(R.color.white));
        } else {
            mImageTouchTake.setBackgroundResource(R.drawable.ic_camera_setting_more_dark);
            mTextTouchTake.setTextColor(mContext.getResources().getColor(R.color.popup_text_normal));
        }
    }

    /**
     * 处理延时拍摄
     */
    private void processTimeLapse() {
        mDelayTakePicture = !mDelayTakePicture;
        updateTimeLapseUI();
        if (mListener != null) {
            mListener.delayTakenChanged(mDelayTakePicture);
        }
    }

    /**
     * 更新延时拍摄UI
     */
    private void updateTimeLapseUI() {
        mImageTimeLapse.setBackgroundResource(mDelayTakePicture
                ? R.drawable.ic_camera_setting_more_light
                : R.drawable.ic_camera_setting_more_dark);
        mTextTimeLapse.setTextColor(mDelayTakePicture
                ? mContext.getResources().getColor(R.color.white)
                : mContext.getResources().getColor(R.color.popup_text_normal));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int i = buttonView.getId();
        if (i == R.id.sw_luminous_compensation) {
            processLuminousCompensation(isChecked);
        }
    }

    /**
     * 处理自动保存
     * @param enable
     */
    private void processLuminousCompensation(boolean enable) {
        canLuminousCompensation = enable;
        if (mListener != null) {
            mListener.luminousCompensationChanged(canLuminousCompensation);
        }
    }

    /**
     * 更新夜光补偿UI
     */
    private void updateLuminousCompensationUI() {
        mLuminousCompensation.setChecked(canLuminousCompensation);
    }

    /**
     * 设置是否允许闪光灯状态变更
     * @param enable
     */
    public void setEnableChangeFlash(boolean enable) {
        mEnableChangeFlash = enable;
    }

    /**
     * 添加状态变更回调接口
     * @param listener
     */
    public void addStateChangedListener(StateChangedListener listener) {
        mListener = listener;
    }

    /**
     * 状态变更接口
     */
    public interface StateChangedListener {

        // 触摸拍照状态变更
        void touchTakenChanged(boolean touchTake);

        // 延时拍摄状态变更
        void delayTakenChanged(boolean enable);

        // 闪光灯状态变更
        void flashStateChanged(boolean flashOn);

        // 打开相机设置
        void onOpenCameraSetting();

        // 夜光补偿状态变更
        void luminousCompensationChanged(boolean enable);
    }
}
