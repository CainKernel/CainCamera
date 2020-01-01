package com.cgfay.camera.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;

/**
 * 预览设置Fragment
 */
public class PreviewSettingFragment extends Fragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "PreviewSettingFragment";

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
    private Switch mEdgeBlur;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview_setting, container, false);
        initView(view);
        return view;
    }

    private void initView(@NonNull View view) {
        mLayoutFlash = view.findViewById(R.id.layout_flash);
        mImageFlash = view.findViewById(R.id.iv_flash);
        mTextFlash = view.findViewById(R.id.tv_flash);

        mLayoutTouchTake = view.findViewById(R.id.layout_touch_take);
        mImageTouchTake = view.findViewById(R.id.iv_touch_take);
        mTextTouchTake = view.findViewById(R.id.tv_touch_take);

        mLayoutCameraSetting = view.findViewById(R.id.layout_camera_setting);

        mLayoutTimeLapse = view.findViewById(R.id.layout_time_lapse);
        mImageTimeLapse = view.findViewById(R.id.iv_time_lapse);
        mTextTimeLapse = view.findViewById(R.id.tv_time_lapse);

        mLuminousCompensation = view.findViewById(R.id.sw_luminous_compensation);
        mEdgeBlur = view.findViewById(R.id.sw_edge_blur);

        mLayoutFlash.setOnClickListener(this);
        mLayoutTouchTake.setOnClickListener(this);
        mLayoutCameraSetting.setOnClickListener(this);
        mLayoutTimeLapse.setOnClickListener(this);

        mLuminousCompensation.setOnCheckedChangeListener(this);
        mEdgeBlur.setOnCheckedChangeListener(this);

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
            mTextFlash.setTextColor(getResources().getColor(R.color.white));
        } else {
            mImageFlash.setBackgroundResource(R.drawable.ic_camera_flash_off);
            mTextFlash.setTextColor(getResources().getColor(R.color.popup_text_normal));
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
            mTextTouchTake.setTextColor(getResources().getColor(R.color.white));
        } else {
            mImageTouchTake.setBackgroundResource(R.drawable.ic_camera_setting_more_dark);
            mTextTouchTake.setTextColor(getResources().getColor(R.color.popup_text_normal));
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
                ? getResources().getColor(R.color.white)
                : getResources().getColor(R.color.popup_text_normal));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int i = buttonView.getId();
        if (i == R.id.sw_luminous_compensation) {
            processLuminousCompensation(isChecked);
        } else if (i == R.id.sw_edge_blur) {
            if (mListener != null) {
                mListener.changeEdgeBlur(isChecked);
            }
        }
    }

    /**
     * 处理夜光补偿
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

        // 是否是能边框模糊
        void changeEdgeBlur(boolean enable);
    }

}
