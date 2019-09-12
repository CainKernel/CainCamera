package com.cgfay.camera.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cgfay.camera.presenter.CameraPreviewPresenter;
import com.cgfay.camera.widget.CainTextureView;
import com.cgfay.cameralibrary.R;
import com.cgfay.camera.engine.camera.CameraEngine;
import com.cgfay.camera.engine.camera.CameraParam;
import com.cgfay.camera.engine.model.GalleryType;
import com.cgfay.camera.engine.recorder.PreviewRecorder;
import com.cgfay.camera.utils.PathConstraints;
import com.cgfay.camera.widget.AspectFrameLayout;
import com.cgfay.camera.widget.HorizontalIndicatorView;
import com.cgfay.camera.widget.PopupSettingView;
import com.cgfay.camera.widget.RecordSpeedLevelBar;
import com.cgfay.camera.widget.ShutterButton;
import com.cgfay.filter.multimedia.VideoCombiner;
import com.cgfay.uitls.fragment.PermissionErrorDialogFragment;
import com.cgfay.uitls.utils.BrightnessUtils;
import com.cgfay.uitls.utils.DisplayUtils;
import com.cgfay.uitls.utils.NotchUtils;
import com.cgfay.uitls.utils.PermissionUtils;
import com.cgfay.uitls.utils.StatusBarUtils;
import com.cgfay.uitls.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 相机预览页面
 */
public class CameraPreviewFragment extends Fragment implements View.OnClickListener,
        HorizontalIndicatorView.OnIndicatorListener {

    private static final String TAG = "CameraPreviewFragment";
    private static final boolean VERBOSE = true;

    private static final String FRAGMENT_DIALOG = "dialog";

    // 对焦大小
    private static final int FocusSize = 100;

    // 相机权限使能标志
    private boolean mCameraEnable = false;
    // 存储权限使能标志
    private boolean mStorageWriteEnable = false;
    // 是否需要等待录制完成再跳转
    private boolean mNeedToWaitStop = false;
    // 显示贴纸页面
    private boolean isShowingStickers = false;
    // 显示滤镜页面
    private boolean isShowingFilters = false;

    // 处于延时拍照状态
    private boolean mDelayTaking = false;

    // 预览参数
    private CameraParam mCameraParam;

    // Fragment主页面
    private View mContentView;
    // 预览部分
    private AspectFrameLayout mAspectLayout;
//    private CainSurfaceView mCameraSurfaceView;
    private CainTextureView mCameraTextureView;
    // 顶部布局
    private RelativeLayout mPreviewTop;
    // fps显示
    private TextView mFpsView;
    // 对比按钮
    private Button mBtnCompare;
    // 右上角列表
    private LinearLayout mPreviewRightTop;
    // 顶部Button
    private LinearLayout mBtnSetting;
    // 翻转按钮
    private LinearLayout mBtnSwitch;
    // 速度按钮
    private LinearLayout mBtnSpeed;
    // 滤镜按钮
    private LinearLayout mBtnEffect;
    // 设置的PopupView
    private PopupSettingView mSettingView;

    private LinearLayout mLayoutBottom;
    // 速度选择条
    private RecordSpeedLevelBar mSpeedBar;
    private boolean mSpeedBarShowing;
    // 倒计时
    private TextView mCountDownView;
    // 贴纸按钮
    private Button mBtnStickers;
    // 快门按钮
    private ShutterButton mBtnShutter;
    // 媒体库按钮
    private Button mBtnViewPhoto;
    // 视频删除按钮
    private Button mBtnRecordDelete;
    // 视频预览按钮
    private Button mBtnRecordPreview;
    // 相机类型指示器
    private HorizontalIndicatorView mBottomIndicator;
    // 相机类型指示文字
    private List<String> mIndicatorText = new ArrayList<String>();
    // 合并对话框
    private CombineVideoDialogFragment mCombineDialog;
    // 主线程Handler
    private Handler mMainHandler;
    // 持有该Fragment的Activity，onAttach/onDetach中绑定/解绑，主要用于解决getActivity() = null的情况
    private Activity mActivity;
    // 贴纸资源页面
    private PreviewResourceFragment mResourcesFragment;
    // 滤镜页面
    private PreviewEffectFragment mEffectFragment;

    private CameraPreviewPresenter mPreviewPresenter;

    public CameraPreviewFragment() {
        mCameraParam = CameraParam.getInstance();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mMainHandler = new Handler(context.getMainLooper());
        mCameraEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA);
        mStorageWriteEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mPreviewPresenter = new CameraPreviewPresenter(this);
        mPreviewPresenter.onAttach(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_camera_preview, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCameraEnable) {
            initView(mContentView);
        } else {
            PermissionUtils.requestCameraPermission(this);
        }
    }

    /**
     * 初始化页面
     * @param view
     */
    private void initView(View view) {
        mAspectLayout = (AspectFrameLayout) view.findViewById(R.id.layout_aspect);
//        mCameraSurfaceView = new CainSurfaceView(mActivity);
//        mCameraSurfaceView.addOnTouchScroller(mTouchScroller);
//        mCameraSurfaceView.addMultiClickListener(mMultiClickListener);
//        mAspectLayout.addView(mCameraSurfaceView);
        mCameraTextureView = new CainTextureView(mActivity);
        mCameraTextureView.addOnTouchScroller(mTouchScroller);
        mCameraTextureView.addMultiClickListener(mMultiClickListener);
        mCameraTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mAspectLayout.addView(mCameraTextureView);

        // 全面屏比例处理
        if (DisplayUtils.isAllScreenDevice(mActivity)) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
            int widthPixel = outMetrics.widthPixels;
            int heightPixel = outMetrics.heightPixels;
            float height = mActivity.getResources().getDimension(R.dimen.bottom_indicator_height);
            // 是否刘海屏
            if (NotchUtils.hasNotchScreen(mActivity)) {
                height += StatusBarUtils.getStatusBarHeight(mActivity);
            }
            mAspectLayout.setAspectRatio(widthPixel / (heightPixel - height));
        }
        mAspectLayout.requestLayout();
//        mCameraSurfaceView.getHolder().addCallback(mSurfaceCallback);

        mPreviewTop = (RelativeLayout) view.findViewById(R.id.layout_preview_top);
        view.findViewById(R.id.btn_close).setOnClickListener(this);
        view.findViewById(R.id.btn_select_music).setOnClickListener(this);

        mFpsView = (TextView) view.findViewById(R.id.tv_fps);
        mBtnCompare = (Button) view.findViewById(R.id.btn_compare);
        mBtnCompare.setVisibility(View.GONE);
        mBtnCompare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPreviewPresenter.showCompare(true);
                        mBtnCompare.setBackgroundResource(R.drawable.ic_camera_compare_pressed);
                        break;

                    case MotionEvent.ACTION_UP:
                        mPreviewPresenter.showCompare(false);
                        mBtnCompare.setBackgroundResource(R.drawable.ic_camera_compare_normal);
                        break;
                }
                return true;
            }
        });

        mPreviewRightTop = (LinearLayout) view.findViewById(R.id.layout_preview_right_top);
        mBtnSetting = (LinearLayout)view.findViewById(R.id.btn_setting);
        mBtnSetting.setOnClickListener(this);
        mBtnSwitch = (LinearLayout) view.findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);
        mBtnSpeed = (LinearLayout) view.findViewById(R.id.btn_speed);
        mBtnSpeed.setOnClickListener(this);
        mBtnEffect = (LinearLayout) view.findViewById(R.id.btn_effects);
        mBtnEffect.setOnClickListener(this);

        mLayoutBottom = (LinearLayout) view.findViewById(R.id.layout_bottom);
        mSpeedBar = (RecordSpeedLevelBar) view.findViewById(R.id.record_speed_bar);
        mSpeedBar.setOnSpeedChangedListener((speed) -> {
            mPreviewPresenter.setSpeed(speed.getSpeed());
        });

        mCountDownView = (TextView) view.findViewById(R.id.tv_countdown);
        mBtnStickers = (Button) view.findViewById(R.id.btn_stickers);
        mBtnStickers.setOnClickListener(this);
        mBtnViewPhoto = (Button) view.findViewById(R.id.btn_view_photo);
        mBtnViewPhoto.setOnClickListener(this);
        mBottomIndicator = (HorizontalIndicatorView) view.findViewById(R.id.bottom_indicator);
        String[] galleryIndicator = getResources().getStringArray(R.array.gallery_indicator);
        mIndicatorText.addAll(Arrays.asList(galleryIndicator));
        mBottomIndicator.setIndicators(mIndicatorText);
        mBottomIndicator.addIndicatorListener(this);

        mBtnShutter = (ShutterButton) view.findViewById(R.id.btn_shutter);
        mBtnShutter.setOnShutterListener(mShutterListener);
        mBtnShutter.setOnClickListener(this);

        mBtnRecordDelete = (Button) view.findViewById(R.id.btn_record_delete);
        mBtnRecordDelete.setOnClickListener(this);
        mBtnRecordPreview = (Button) view.findViewById(R.id.btn_record_preview);
        mBtnRecordPreview.setOnClickListener(this);

        adjustBottomView();
    }

    /**
     * 调整底部视图
     */
    private void adjustBottomView() {
        boolean result = mCameraParam.currentRatio < CameraParam.Ratio_4_3;
        mBtnStickers.setBackgroundResource(result ? R.drawable.ic_camera_sticker_light : R.drawable.ic_camera_sticker_dark);
        mBtnRecordDelete.setBackgroundResource(result ? R.drawable.ic_camera_record_delete_light : R.drawable.ic_camera_record_delete_dark);
        mBtnRecordPreview.setBackgroundResource(result ? R.drawable.ic_camera_record_done_light : R.drawable.ic_camera_record_done_dark);
        mBtnShutter.setOuterBackgroundColor(result ? R.color.shutter_gray_light : R.color.shutter_gray_dark);
    }

    @Override
    public void onResume() {
        super.onResume();
        enhancementBrightness();
        mBtnShutter.setEnableOpened(false);
    }

    /**
     * 增强光照
     */
    private void enhancementBrightness() {
        BrightnessUtils.setWindowBrightness(mActivity, mCameraParam.luminousEnhancement
                ? BrightnessUtils.MAX_BRIGHTNESS : mCameraParam.brightness);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideStickerView();
        hideEffectView();
        mBtnShutter.setEnableOpened(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentView = null;
    }

    @Override
    public void onDestroy() {
        mPreviewPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        mPreviewPresenter.onDetach();
        mPreviewPresenter = null;
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        if (mSettingView != null && mSettingView.isShowing()) {
            setShowingSpeedBar(mSpeedBarShowing);
        }
        int i = v.getId();
        if (i == R.id.btn_close) {
            mActivity.finish();
            mActivity.overridePendingTransition(0, R.anim.anim_slide_down);
        } else if (i == R.id.btn_select_music) {
            mPreviewPresenter.onOpenMusicSelectPage();
        } else if (i == R.id.btn_switch) {
            mPreviewPresenter.switchCamera();
        } else if (i == R.id.btn_speed) {
            setShowingSpeedBar(mSpeedBar.getVisibility() != View.VISIBLE);
        } else if (i == R.id.btn_effects) {
            showEffectView();
        } else if (i == R.id.btn_setting) {
            showSettingPopView();
        } else if (i == R.id.btn_stickers) {
            showStickers();
        } else if (i == R.id.btn_view_photo) {
            mPreviewPresenter.onOpenGalleryPage();
        } else if (i == R.id.btn_shutter) {
            takePicture();
        } else if (i == R.id.btn_record_delete) {
            deleteRecordedVideo(false);
        } else if (i == R.id.btn_record_preview) {
            stopRecordOrPreviewVideo();
        }
    }

    @Override
    public void onIndicatorChanged(int currentIndex) {
        if (currentIndex == 0) {
            mCameraParam.mGalleryType = GalleryType.GIF;
            mBtnShutter.setIsRecorder(false);
        } else if (currentIndex == 1) {
            mCameraParam.mGalleryType = GalleryType.PICTURE;
            // 拍照状态
            mBtnShutter.setIsRecorder(false);
            if (!mStorageWriteEnable) {
                PermissionUtils.requestStoragePermission(this);
            }
        } else if (currentIndex == 2) {
            mCameraParam.mGalleryType = GalleryType.VIDEO;
            // 录制视频状态
            mBtnShutter.setIsRecorder(true);
            // 请求录音权限
            if (!mCameraParam.audioPermitted) {
                PermissionUtils.requestRecordSoundPermission(this);
            }
        }
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (!mCameraEnable) {
            PermissionUtils.requestCameraPermission(this);
            return;
        }
        mPreviewPresenter.switchCamera();
    }

    /**
     * 是否显示速度条
     * @param show
     */
    private void setShowingSpeedBar(boolean show) {
        mSpeedBarShowing = show;
        mSpeedBar.setVisibility(show ? View.VISIBLE : View.GONE);
        ((TextView)mContentView.findViewById(R.id.tv_speed_status)).setText(show ? "速度开" : "速度关");
        if (show) {
            if (mSettingView != null) {
                mSettingView.dismiss();
            }
            hideStickerView();
            hideEffectView();
        }
    }

    /**
     * 显示下拉设置页面
     */
    private void showSettingPopView() {
        if (mSettingView == null) {
            mSettingView = new PopupSettingView(mActivity);
        }
        mSettingView.addStateChangedListener(mStateChangedListener);
        mSettingView.showAsDropDown(mBtnSetting, Gravity.BOTTOM, 0, 0);
        mSettingView.setEnableChangeFlash(mCameraParam.supportFlash);

        mSpeedBar.setVisibility(View.GONE);
        ((TextView)mContentView.findViewById(R.id.tv_speed_status)).setText("速度关");
    }

    /**
     * 显示动态贴纸页面
     */
    private void showStickers() {
        isShowingStickers = true;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mResourcesFragment == null) {
            mResourcesFragment = new PreviewResourceFragment();
            mResourcesFragment.addOnChangeResourceListener((data) -> {
                mPreviewPresenter.changeResource(data);
            });
            ft.add(R.id.fragment_container, mResourcesFragment);
        } else {
            mResourcesFragment.addOnChangeResourceListener((data) -> {
                mPreviewPresenter.changeResource(data);
            });
            ft.show(mResourcesFragment);
        }
        ft.commit();
        hideBottomLayout();

    }

    /**
     * 显示滤镜页面
     */
    private void showEffectView() {
        mBtnCompare.setVisibility(View.VISIBLE);
        isShowingFilters = true;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mEffectFragment == null) {
            mEffectFragment = new PreviewEffectFragment();
            mEffectFragment.addOnFilterChangeListener(color -> {
                mPreviewPresenter.changeDynamicFilter(color);
            });
            mEffectFragment.addOnMakeupChangeListener(makeup -> {
                mPreviewPresenter.changeDynamicMakeup(makeup);
            });
            ft.add(R.id.fragment_container, mEffectFragment);
        } else {
            mEffectFragment.addOnFilterChangeListener(color -> {
                mPreviewPresenter.changeDynamicFilter(color);
            });
            mEffectFragment.addOnMakeupChangeListener(makeup -> {
                mPreviewPresenter.changeDynamicMakeup(makeup);
            });
            ft.show(mEffectFragment);
        }
        ft.commit();
        mEffectFragment.scrollToCurrentFilter(mPreviewPresenter.getFilterIndex());
        hideBottomLayout();
    }

    /**
     * 隐藏动态贴纸页面
     */
    private void hideStickerView() {
        if (isShowingStickers) {
            isShowingStickers = false;
            if (mResourcesFragment != null) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.hide(mResourcesFragment);
                ft.commit();
            }
        }
        resetBottomLayout();
    }

    /**
     * 隐藏滤镜页面
     */
    private void hideEffectView() {
        mBtnCompare.setVisibility(View.GONE);
        if (isShowingFilters) {
            isShowingFilters = false;
            if (mEffectFragment != null) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.hide(mEffectFragment);
                ft.commit();
            }
        }
        resetBottomLayout();

        mPreviewRightTop.setVisibility(View.VISIBLE);
        mPreviewTop.setVisibility(View.VISIBLE);
        mSpeedBar.setVisibility(mSpeedBarShowing ? View.VISIBLE : View.GONE);
    }

    /**
     * 隐藏底部布局按钮
     */
    private void hideBottomLayout() {
        mLayoutBottom.setVisibility(View.GONE);
        mPreviewRightTop.setVisibility(View.GONE);
        mPreviewTop.setVisibility(View.GONE);
    }

    /**
     * 恢复底部布局
     */
    private void resetBottomLayout() {
        mLayoutBottom.setVisibility(View.VISIBLE);
        mPreviewRightTop.setVisibility(View.VISIBLE);
        mPreviewTop.setVisibility(View.VISIBLE);
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mStorageWriteEnable) {
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                if (mCameraParam.takeDelay && !mDelayTaking) {
                    mDelayTaking = true;
                    mMainHandler.postDelayed(() -> {
                        mDelayTaking = false;
                        mPreviewPresenter.takePicture();
                    }, 3000);
                } else {
                    mPreviewPresenter.takePicture();
                }
            }
        } else {
            PermissionUtils.requestStoragePermission(this);
        }
    }

    /**
     * 取消录制
     */
    public void cancelRecordIfNeeded() {
        // 停止录制
        if (mPreviewPresenter.isRecording()) {
            // 取消录制
            mPreviewPresenter.cancelRecord();
            // 重置进入条
            mBtnShutter.setProgress((int) mPreviewPresenter.getVideoVisibleDuration());
            // 删除分割线
            mBtnShutter.deleteSplitView();
            // 关闭按钮
            mBtnShutter.closeButton();
            // 更新时间
            mCountDownView.setText(mPreviewPresenter.getVideoVisibleTimeString());
        }
    }

    public void setMusicPath(String path) {
        mPreviewPresenter.setMusicPath(path);
    }

    // ------------------------------- SurfaceView 滑动、点击回调 ----------------------------------
//    private CainSurfaceView.OnTouchScroller mTouchScroller = new CainSurfaceView.OnTouchScroller() {
//
//        @Override
//        public void swipeBack() {
//            int index = mPreviewPresenter.nextFilter();
//            if (mEffectFragment != null) {
//                mEffectFragment.scrollToCurrentFilter(index);
//            }
//        }
//
//        @Override
//        public void swipeFrontal() {
//            int index = mPreviewPresenter.lastFilter();
//            if (mEffectFragment != null) {
//                mEffectFragment.scrollToCurrentFilter(index);
//            }
//        }
//
//        @Override
//        public void swipeUpper(boolean startInLeft, float distance) {
//            if (VERBOSE) {
//                Log.d(TAG, "swipeUpper, startInLeft ? " + startInLeft + ", distance = " + distance);
//            }
//        }
//
//        @Override
//        public void swipeDown(boolean startInLeft, float distance) {
//            if (VERBOSE) {
//                Log.d(TAG, "swipeDown, startInLeft ? " + startInLeft + ", distance = " + distance);
//            }
//        }
//
//    };

    private CainTextureView.OnTouchScroller mTouchScroller = new CainTextureView.OnTouchScroller() {

        @Override
        public void swipeBack() {
            int index = mPreviewPresenter.nextFilter();
            if (mEffectFragment != null) {
                mEffectFragment.scrollToCurrentFilter(index);
            }
        }

        @Override
        public void swipeFrontal() {
            int index = mPreviewPresenter.lastFilter();
            if (mEffectFragment != null) {
                mEffectFragment.scrollToCurrentFilter(index);
            }
        }

        @Override
        public void swipeUpper(boolean startInLeft, float distance) {
            if (VERBOSE) {
                Log.d(TAG, "swipeUpper, startInLeft ? " + startInLeft + ", distance = " + distance);
            }
        }

        @Override
        public void swipeDown(boolean startInLeft, float distance) {
            if (VERBOSE) {
                Log.d(TAG, "swipeDown, startInLeft ? " + startInLeft + ", distance = " + distance);
            }
        }

    };

//    /**
//     * 单双击回调监听
//     */
//    private CainSurfaceView.OnMultiClickListener mMultiClickListener = new CainSurfaceView.OnMultiClickListener() {
//
//        @Override
//        public void onSurfaceSingleClick(final float x, final float y) {
//            // 单击隐藏贴纸和滤镜页面
//            mMainHandler.post(() -> {
//                hideStickerView();
//                hideEffectView();
//            });
//
//            // 如果处于触屏拍照状态，则直接拍照，不做对焦处理
//            if (mCameraParam.touchTake) {
//                takePicture();
//                return;
//            }
//
//            // 判断是否支持对焦模式
//            if (CameraEngine.getInstance().getCamera() != null) {
//                List<String> focusModes = CameraEngine.getInstance().getCamera()
//                        .getParameters().getSupportedFocusModes();
//                if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
//                    CameraEngine.getInstance().setFocusArea(CameraEngine.getFocusArea((int)x, (int)y,
//                            mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight(), FocusSize));
//                    mMainHandler.post(() -> {
//                        mCameraSurfaceView.showFocusAnimation();
//                    });
//                }
//            }
//        }
//
//        @Override
//        public void onSurfaceDoubleClick(float x, float y) {
//            switchCamera();
//        }
//
//    };

    /**
     * 单双击回调监听
     */
    private CainTextureView.OnMultiClickListener mMultiClickListener = new CainTextureView.OnMultiClickListener() {

        @Override
        public void onSurfaceSingleClick(final float x, final float y) {
            // 单击隐藏贴纸和滤镜页面
            mMainHandler.post(() -> {
                hideStickerView();
                hideEffectView();
            });

            // 如果处于触屏拍照状态，则直接拍照，不做对焦处理
            if (mCameraParam.touchTake) {
                takePicture();
                return;
            }

            // 判断是否支持对焦模式
            if (CameraEngine.getInstance().getCamera() != null) {
                List<String> focusModes = CameraEngine.getInstance().getCamera()
                        .getParameters().getSupportedFocusModes();
                if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    CameraEngine.getInstance().setFocusArea(CameraEngine.getFocusArea((int)x, (int)y,
                            mCameraTextureView.getWidth(), mCameraTextureView.getHeight(), FocusSize));
                    mMainHandler.post(() -> {
                        mCameraTextureView.showFocusAnimation();
                    });
                }
            }
        }

        @Override
        public void onSurfaceDoubleClick(float x, float y) {
            switchCamera();
        }

    };

//    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//            mPreviewPresenter.bindSurface(holder.getSurface());
//        }
//
//        @Override
//        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            mPreviewPresenter.changePreviewSize(width, height);
//        }
//
//        @Override
//        public void surfaceDestroyed(SurfaceHolder holder) {
//            mPreviewPresenter.unBindSurface();
//        }
//    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.bindSurface(surface);
            mPreviewPresenter.changePreviewSize(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.changePreviewSize(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mPreviewPresenter.unBindSurface();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    // ----------------------------------- 顶部状态栏点击回调 ------------------------------------
    private PopupSettingView.StateChangedListener mStateChangedListener = new PopupSettingView.StateChangedListener() {

        @Override
        public void flashStateChanged(boolean flashOn) {
            CameraEngine.getInstance().setFlashLight(flashOn);
        }

        @Override
        public void onOpenCameraSetting() {
            mPreviewPresenter.onOpenCameraSettingPage();
        }

        @Override
        public void delayTakenChanged(boolean enable) {
            mCameraParam.takeDelay = enable;
        }

        @Override
        public void luminousCompensationChanged(boolean enable) {
            mCameraParam.luminousEnhancement = enable;
            enhancementBrightness();
        }

        @Override
        public void touchTakenChanged(boolean touchTake) {
            mCameraParam.touchTake = touchTake;
        }

        @Override
        public void changeEdgeBlur(boolean enable) {
            mPreviewPresenter.enableEdgeBlurFilter(enable);
        }
    };

    /**
     * 显示fps
     * @param fps
     */
    public void showFps(final float fps) {
        mMainHandler.post(() -> {
            if (mCameraParam.showFps) {
                mFpsView.setText("fps = " + fps);
                mFpsView.setVisibility(View.VISIBLE);
            } else {
                mFpsView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 允许录制按钮
     * @param enable
     */
    public void enableShutter(final boolean enable) {
        mMainHandler.post(() -> {
            if (mBtnShutter != null) {
                mBtnShutter.setEnableOpened(enable);
            }
        });
    }

    // ------------------------------------ 录制回调 -------------------------------------------
    private ShutterButton.OnShutterListener mShutterListener = new ShutterButton.OnShutterListener() {

        @Override
        public void onStartRecord() {
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                return;
            }

            // 隐藏顶部视图
            mPreviewTop.setVisibility(View.GONE);
            mPreviewRightTop.setVisibility(View.GONE);
            mSpeedBar.setVisibility(View.GONE);

            // 隐藏删除按钮
            if (mCameraParam.mGalleryType == GalleryType.VIDEO) {
                mBtnRecordPreview.setVisibility(View.GONE);
                mBtnRecordDelete.setVisibility(View.GONE);
            }
            mBtnShutter.setProgressMax((int)mPreviewPresenter.getMaxRecordMilliSeconds());
            // 添加分割线
            mBtnShutter.addSplitView();

            // 是否允许录制音频
            boolean enableAudio = mCameraParam.audioPermitted && mCameraParam.recordAudio
                    && mCameraParam.mGalleryType == GalleryType.VIDEO;

            // 计算输入纹理的大小
            int width = mCameraParam.previewWidth;
            int height = mCameraParam.previewHeight;
            if (mCameraParam.orientation == 90 || mCameraParam.orientation == 270) {
                width = mCameraParam.previewHeight;
                height = mCameraParam.previewWidth;
            }
            mPreviewPresenter.startRecord(width, height, enableAudio);
        }

        @Override
        public void onStopRecord() {
            mPreviewPresenter.stopRecord();
            // 隐藏顶部视图
            mPreviewTop.setVisibility(View.VISIBLE);
            mPreviewRightTop.setVisibility(View.VISIBLE);
            setShowingSpeedBar(mSpeedBarShowing);
        }

        @Override
        public void onProgressOver() {
            // 如果最后一秒内点击停止录制，则仅仅关闭录制按钮，因为前面已经停止过了，不做跳转
            // 如果最后一秒内没有停止录制，否则停止录制并跳转至预览页面
            if (mPreviewPresenter.isLastSecondStop()) {
                // 关闭录制按钮
                mBtnShutter.closeButton();
            } else {
                stopRecordOrPreviewVideo();
            }
        }
    };

    /**
     * 设置快门是否允许可用
     * @param enable
     */
    public void setShutterEnableEncoder(boolean enable) {
        if (mBtnShutter != null) {
            mBtnShutter.setEnableEncoder(enable);
        }
    }

    /**
     * 更新录制时间
     * @param duration
     */
    public void updateRecordProgress(final long duration) {
        mMainHandler.post(() -> {
            // 设置进度
            mBtnShutter.setProgress(duration);
            // 设置时间
            mCountDownView.setText(StringUtils.generateMillisTime((int) duration));
        });
    }

    /**
     * 录制完成更新状态
     */
    public void updateRecordFinish() {
        mMainHandler.post(() -> {
            setShutterEnableEncoder(true);
            // 处于录制状态点击了预览按钮，则需要等待完成再跳转， 或者是处于录制GIF状态
            if (mNeedToWaitStop || mCameraParam.mGalleryType == GalleryType.GIF) {
                // 开始预览
                stopRecordOrPreviewVideo();
            }
            // 显示删除按钮
            if (mCameraParam.mGalleryType == GalleryType.VIDEO) {
                mBtnRecordPreview.setVisibility(View.VISIBLE);
                mBtnRecordDelete.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 删除已录制的视频
     * @param clearAll
     */
    private void deleteRecordedVideo(boolean clearAll) {
        // 处于删除模式，则删除文件
        if (mBtnShutter.isDeleteMode()) {
            // 删除视频，判断是否清除所有
            if (clearAll) {
                // 清除所有分割线
                mBtnShutter.cleanSplitView();
                mPreviewPresenter.removeAllSubVideo();
            } else {
                // 删除分割线
                mBtnShutter.deleteSplitView();
                mPreviewPresenter.removeLastSubVideo();

            }
            // 更新进度
            mBtnShutter.setProgress(mPreviewPresenter.getVideoVisibleDuration());
            // 更新时间
            mCountDownView.setText(mPreviewPresenter.getVideoVisibleTimeString());
            // 如果此时没有了视频，则恢复初始状态
            if (mPreviewPresenter.getNumberOfSubVideo() <= 0) {
                mCountDownView.setText("");
                mBtnRecordDelete.setVisibility(View.GONE);
                mBtnRecordPreview.setVisibility(View.GONE);
                mNeedToWaitStop = false;
            }
        } else { // 没有进入删除模式则进入删除模式
            mBtnShutter.setDeleteMode(true);
        }
    }

    /**
     * 停止录制或者预览视频
     */
    private void stopRecordOrPreviewVideo() {
        mBtnShutter.closeButton();
        if (mPreviewPresenter.isRecording()) {
            mNeedToWaitStop = true;
            mPreviewPresenter.stopRecord(false);
        } else {
            mNeedToWaitStop = false;
            mPreviewPresenter.destroyRecorder();

            combinePath = PathConstraints.getVideoCachePath(mActivity);
            PreviewRecorder.getInstance().combineVideo(combinePath, mCombineListener);
        }
    }

    // -------------------------------------- 短视频合成监听器 ---------------------------------
    // 合成输出路径
    private String combinePath;
    // 合成监听器
    private VideoCombiner.CombineListener mCombineListener = new VideoCombiner.CombineListener() {
        @Override
        public void onCombineStart() {
            if (VERBOSE) {
                Log.d(TAG, "开始合并");
            }
            mMainHandler.post(() -> {
                if (mCombineDialog != null) {
                    mCombineDialog.dismiss();
                    mCombineDialog = null;
                }
                mCombineDialog = CombineVideoDialogFragment.newInstance(mActivity.getString(R.string.combine_video_message));
                mCombineDialog.show(getChildFragmentManager(), FRAGMENT_DIALOG);
            });
        }

        @Override
        public void onCombineProcessing(final int current, final int sum) {
            mMainHandler.post(() -> {
                if (mCombineDialog != null && mCombineDialog.getShowsDialog()) {
                    mCombineDialog.setProgressMessage(mActivity.getString(R.string.combine_video_message));
                }
            });
        }

        @Override
        public void onCombineFinished(final boolean success) {
            mMainHandler.post(() -> {
                if (mCombineDialog != null) {
                    mCombineDialog.dismiss();
                    mCombineDialog = null;
                }
            });
            mPreviewPresenter.onOpenVideoEditPage(combinePath);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_camera_permission), PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mCameraEnable = true;
                initView(mContentView);
            }
        } else if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mStorageWriteEnable = true;
            }
        } else if (requestCode == PermissionUtils.REQUEST_SOUND_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mCameraParam.audioPermitted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
