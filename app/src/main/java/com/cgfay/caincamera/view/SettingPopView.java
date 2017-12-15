package com.cgfay.caincamera.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.type.TimeLapseType;

/**
 * 设置弹窗界面
 * Created by cain on 2017/12/15.
 */

public class SettingPopView extends BasePopupWindow
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // 闪光灯
    private LinearLayout mLayoutFlash;
    private ImageView mImageFlash;
    // 人脸模式
    private LinearLayout mLayoutFaceMode;
    private ImageView mImageFaceMode;
    private TextView mTextFaceMode;
    // 美颜
    private LinearLayout mLayoutBeautify;
    private ImageView mImageBeautify;
    private TextView mTextBeautify;
    // 延时拍摄
    private LinearLayout mLayoutTimeLapse;
    private ImageView mImageTimeLapse;
    // 自动保存
    private Switch mAutoSave;
    // 触摸拍照
    private Switch mTouchTake;

    // 是否允许闪光灯，默认关闭
    private boolean mEnableFlash = false;
    // 是否允许多人脸检测，默认多脸模式
    private boolean mEnableMultiFace = true;
    // 是否允许美颜
    private boolean mEnableBeautify = true;
    // 默认不开启延时拍摄
    private TimeLapseType mTimeLapseType = TimeLapseType.TimeLapse_off;

    // 是否自动保存
    private boolean canAutoSave = false;
    private boolean canTouchTake = false;

    private StateChangedListener mListener;


    public SettingPopView(Context context) {
        super(context);
        init();
    }

    private void init() {

        View contentView = LayoutInflater.from(mContext).inflate(R.layout.view_pop_setting, null);
        setContentView(contentView);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        mLayoutFlash = (LinearLayout) contentView.findViewById(R.id.layout_flash);
        mImageFlash = (ImageView) contentView.findViewById(R.id.iv_flash);

        mLayoutFaceMode = (LinearLayout) contentView.findViewById(R.id.layout_face_mode);
        mImageFaceMode = (ImageView) contentView.findViewById(R.id.iv_face_mode);
        mTextFaceMode = (TextView) contentView.findViewById(R.id.tv_face_mode);

        mLayoutBeautify = (LinearLayout) contentView.findViewById(R.id.layout_beautify);
        mImageBeautify = (ImageView) contentView.findViewById(R.id.iv_beautify);
        mTextBeautify = (TextView) contentView.findViewById(R.id.tv_beautify);

        mLayoutTimeLapse = (LinearLayout) contentView.findViewById(R.id.layout_time_lapse);
        mImageTimeLapse = (ImageView) contentView.findViewById(R.id.iv_time_lapse);

        mAutoSave = (Switch) contentView.findViewById(R.id.sw_auto_save);
        mTouchTake = (Switch) contentView.findViewById(R.id.sw_touch_take);

        mLayoutFlash.setOnClickListener(this);
        mLayoutFaceMode.setOnClickListener(this);
        mLayoutBeautify.setOnClickListener(this);
        mLayoutTimeLapse.setOnClickListener(this);

        mAutoSave.setOnCheckedChangeListener(this);
        mTouchTake.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.layout_flash:
                processFlash();
                break;

            case R.id.layout_face_mode:
                processFaceMode();
                break;

            case R.id.layout_beautify:
                processBeautify();
                break;

            case R.id.layout_time_lapse:
                processTimeLapse();
                break;
        }
    }

    /**
     * 处理闪光灯
     */
    private void processFlash() {
        mEnableFlash = !mEnableFlash;
        if (mEnableFlash) {

        } else {

        }

        if (mListener != null) {
            mListener.flashStateChanged(mEnableFlash);
        }
    }

    /**
     * 处理人脸模式
     */
    private void processFaceMode() {
        mEnableMultiFace = !mEnableMultiFace;
        if (mEnableMultiFace) {

        } else {

        }

        if (mListener != null) {
            mListener.faceModeStateChanged(mEnableMultiFace);
        }
    }

    /**
     * 处理美颜
     */
    private void processBeautify() {
        mEnableBeautify = !mEnableBeautify;
        if (mEnableBeautify) {

        } else {

        }

        if (mListener != null) {
            mListener.beautifyStateChanged(mEnableBeautify);
        }
    }

    /**
     * 处理延时拍摄
     */
    private void processTimeLapse() {
        if (mTimeLapseType == TimeLapseType.TimeLapse_off) {
            mTimeLapseType = TimeLapseType.TimeLapse_3;
//            mImageTimeLapse.setBackgroundResource();
        } else if (mTimeLapseType == TimeLapseType.TimeLapse_3) {
            mTimeLapseType = TimeLapseType.TimeLapse_6;
//            mImageTimeLapse.setBackgroundResource();
        } else if (mTimeLapseType == TimeLapseType.TimeLapse_6) {
            mTimeLapseType = TimeLapseType.TimeLapse_off;
//            mImageTimeLapse.setBackgroundResource();
        }

        if (mListener != null) {
            mListener.timeLapseStateChanged(mTimeLapseType);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            // 自动保存
            case R.id.sw_auto_save:
                processAutoSave(isChecked);
                break;

            // 触摸拍照
            case R.id.sw_touch_take:
                processTouchTake(isChecked);
                break;
        }
    }

    /**
     * 处理自动保存
     * @param enable
     */
    private void processAutoSave(boolean enable) {
        canAutoSave = enable;
        if (mListener != null) {
            mListener.autoSaveStateChanged(canAutoSave);
        }
    }

    /**
     * 处理触摸拍照
     * @param enable
     */
    private void processTouchTake(boolean enable) {
        canTouchTake = enable;
        if (mListener != null) {
            mListener.touchTakeStateChanged(canTouchTake);
        }
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
        // 闪光灯状态变更
        void flashStateChanged(boolean flashOn);

        // 人脸模式变更
        void faceModeStateChanged(boolean multiFace);

        // 美颜状态变更
        void beautifyStateChanged(boolean enable);

        // 延时拍摄状态变更
        void timeLapseStateChanged(TimeLapseType type);

        // 自动保存状态变更
        void autoSaveStateChanged(boolean autoSave);

        // 触摸拍照状态变更
        void touchTakeStateChanged(boolean touchTake);
    }
}
