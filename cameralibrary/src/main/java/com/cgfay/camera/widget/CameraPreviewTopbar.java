package com.cgfay.camera.widget;

import android.content.Context;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

public class CameraPreviewTopbar extends ConstraintLayout implements View.OnClickListener {

    public static final int PanelMusic = 0;
    public static final int PanelSpeedBar = 1;
    public static final int PanelFilter = 2;
    public static final int PanelSetting = 3;
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {PanelMusic, PanelSpeedBar, PanelFilter, PanelSetting})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PanelType {}

    private static final int ANIMATION_DURATION = 400;

    private Button mBtnClose;       // 关闭按钮
    private View mBtnMusic;         // 音乐按钮
    private TextView mTextMusic;    // 音乐文字
    private View mBtnSwitch;        // 切换相机
    private ImageView mIconSwitch;  // 相机icon
    private View mBtnSpeed;         // 速度
    private TextView mTextSpeed;    // 速度装填
    private View mBtnEffect;        // 滤镜
    private View mBtnSetting;       // 设置

    private OnCameraCloseListener mCameraCloseListener;
    private OnCameraSwitchListener mCameraSwitchListener;
    private OnShowPanelListener mShowPanelListener;

    public CameraPreviewTopbar(Context context) {
        this(context, null);
    }

    public CameraPreviewTopbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreviewTopbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_preview_topbar, this);
        initView();
    }

    private void initView() {
        mBtnClose = findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(this);
        mBtnMusic = findViewById(R.id.btn_select_music);
        mBtnMusic.setOnClickListener(this);
        mTextMusic = findViewById(R.id.tv_music_name);
        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);
        mIconSwitch = findViewById(R.id.iv_switch);
        mBtnSpeed = findViewById(R.id.btn_speed);
        mBtnSpeed.setOnClickListener(this);
        mTextSpeed = findViewById(R.id.tv_speed_text);
        mBtnEffect = findViewById(R.id.btn_effect);
        mBtnEffect.setOnClickListener(this);
        mBtnSetting = findViewById(R.id.btn_setting);
        mBtnSetting.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_close) {
            closeCamera();
        } else if (id == R.id.btn_select_music) {
            selectMusic();
        } else if (id == R.id.btn_switch) {
            switchCamera();
        } else if (id == R.id.btn_speed) {
            showSpeedLevelBar();
        } else if (id == R.id.btn_effect) {
            showEffectPanel();
        } else if (id == R.id.btn_setting) {
            showSettingPanel();
        }
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        if (mCameraCloseListener != null) {
            mCameraCloseListener.onCameraClose();
        }
    }

    /**
     * 选择音乐
     */
    private void selectMusic() {
        if (mShowPanelListener != null) {
            mShowPanelListener.onShowPanel(PanelMusic);
        }
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (mCameraSwitchListener != null) {
            mIconSwitch.setPivotX(mIconSwitch.getWidth() / 2f);
            mIconSwitch.setPivotY(mIconSwitch.getHeight() / 2f);
            RotateAnimation rotateAnimation = new RotateAnimation(0, 180,
                    mIconSwitch.getWidth() / 2f, mIconSwitch.getHeight() / 2f);
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setDuration(ANIMATION_DURATION);
            mIconSwitch.startAnimation(rotateAnimation);
            mBtnSwitch.setEnabled(false);
            mBtnSwitch.postDelayed(() -> {
                mBtnSwitch.setEnabled(true);
            }, ANIMATION_DURATION);
            mCameraSwitchListener.onCameraSwitch();
        }
    }

    /**
     * 打开速度选择条
     */
    private void showSpeedLevelBar() {
        if (mShowPanelListener != null) {
            mShowPanelListener.onShowPanel(PanelSpeedBar);
        }
    }

    /**
     * 显示特效面板
     */
    private void showEffectPanel() {
        if (mShowPanelListener != null) {
            mShowPanelListener.onShowPanel(PanelFilter);
        }
    }

    /**
     * 显示设置面板
     */
    private void showSettingPanel() {
        if (mShowPanelListener != null) {
            mShowPanelListener.onShowPanel(PanelSetting);
        }
    }

    /**
     * 隐藏所有控件
     */
    public void hideAllView() {
        setVisibility(GONE);
    }

    public void resetAllView() {
        setVisibility(VISIBLE);
        mBtnClose.setVisibility(VISIBLE);
        mBtnMusic.setVisibility(VISIBLE);
        mBtnSpeed.setVisibility(VISIBLE);
        mBtnEffect.setVisibility(VISIBLE);
        mBtnSetting.setVisibility(VISIBLE);
        mBtnSwitch.setVisibility(VISIBLE);
    }

    /**
     * 隐藏除切换相机外的所有控件
     */
    public void hideWithoutSwitch() {
        mBtnClose.setVisibility(GONE);
        mBtnMusic.setVisibility(GONE);
        mBtnSpeed.setVisibility(GONE);
        mBtnEffect.setVisibility(GONE);
        mBtnSetting.setVisibility(GONE);
    }

    /**
     * 速度条是否打开
     * @param open
     */
    public void setSpeedBarOpen(boolean open) {
        if (mTextSpeed != null) {
            mTextSpeed.setText(open ? "速度开" : "速度关");
        }
    }

    /**
     * 设置音乐名称
     * @param musicName
     */
    public void setSelectedMusic(@Nullable String musicName) {
        if (mTextMusic != null) {
            if (!TextUtils.isEmpty(musicName)) {
                mTextMusic.setText(musicName);
            } else {
                mTextMusic.setText(R.string.tv_select_music);
            }
        }
    }

    /**
     * 相机关闭监听器
     */
    public interface OnCameraCloseListener {

        void onCameraClose();
    }

    public CameraPreviewTopbar addOnCameraCloseListener(OnCameraCloseListener listener) {
        mCameraCloseListener = listener;
        return this;
    }

    /**
     * 相机切换监听器
     */
    public interface OnCameraSwitchListener {

        void onCameraSwitch();
    }

    public CameraPreviewTopbar addOnCameraSwitchListener(OnCameraSwitchListener listener) {
        mCameraSwitchListener = listener;
        return this;
    }

    /**
     * 显示面板监听器
     */
    public interface OnShowPanelListener {

        void onShowPanel(@PanelType int type);
    }

    public CameraPreviewTopbar addOnShowPanelListener(OnShowPanelListener listener) {
        mShowPanelListener = listener;
        return this;
    }

}
