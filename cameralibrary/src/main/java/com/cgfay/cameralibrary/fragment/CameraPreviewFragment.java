package com.cgfay.cameralibrary.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.listener.OnCaptureListener;
import com.cgfay.cameralibrary.engine.listener.OnFpsListener;
import com.cgfay.cameralibrary.engine.listener.OnCameraCallback;
import com.cgfay.cameralibrary.engine.listener.OnRecordListener;
import com.cgfay.cameralibrary.engine.model.AspectRatio;
import com.cgfay.cameralibrary.engine.model.GalleryType;
import com.cgfay.cameralibrary.engine.recorder.PreviewRecorder;
import com.cgfay.cameralibrary.engine.render.PreviewRenderer;
import com.cgfay.cameralibrary.listener.OnPageOperationListener;
import com.cgfay.cameralibrary.engine.camera.CameraEngine;
import com.cgfay.cameralibrary.utils.PathConstraints;
import com.cgfay.cameralibrary.widget.AspectFrameLayout;
import com.cgfay.cameralibrary.widget.HorizontalIndicatorView;
import com.cgfay.cameralibrary.widget.PopupSettingView;
import com.cgfay.cameralibrary.widget.RatioImageView;
import com.cgfay.cameralibrary.widget.ShutterButton;
import com.cgfay.facedetectlibrary.engine.FacePointsEngine;
import com.cgfay.facedetectlibrary.engine.FaceTracker;
import com.cgfay.facedetectlibrary.listener.FaceTrackerCallback;
import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.advanced.face.OnFacePointsListener;
import com.cgfay.filterlibrary.multimedia.VideoCombiner;
import com.cgfay.cameralibrary.widget.CainSurfaceView;
import com.cgfay.utilslibrary.fragment.PermissionConfirmDialogFragment;
import com.cgfay.utilslibrary.fragment.PermissionErrorDialogFragment;
import com.cgfay.utilslibrary.utils.BitmapUtils;
import com.cgfay.utilslibrary.utils.BrightnessUtils;
import com.cgfay.utilslibrary.utils.PermissionUtils;
import com.cgfay.utilslibrary.utils.StringUtils;

import java.nio.ByteBuffer;
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
    // 当前索引
    private int mFilterIndex = 0;

    // 处于延时拍照状态
    private boolean mDelayTaking = false;

    // 预览参数
    private CameraParam mCameraParam;

    // Fragment主页面
    private View mContentView;
    // 预览部分
    private AspectFrameLayout mAspectLayout;
    private CainSurfaceView mCameraSurfaceView;
    // fps显示
    private TextView mFpsView;
    // 对比按钮
    private Button mBtnCompare;
    // 顶部Button
    private Button mBtnSetting;
    private Button mBtnViewPhoto;
    private Button mBtnSwitch;
    // 预览尺寸切换
    private RatioImageView mRatioView;
    // 设置的PopupView
    private PopupSettingView mSettingView;
    // 倒计时
    private TextView mCountDownView;
    // 贴纸按钮
    private Button mBtnStickers;
    // 快门按钮
    private ShutterButton mBtnShutter;
    // 滤镜按钮
    private Button mBtnEffect;
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
    // 页面跳转监听器
    private OnPageOperationListener mPageListener;
    // 贴纸页面
    private PreviewStickerFragment mStickerFragment;
    // 滤镜页面
    private PreviewEffectFragment mEffectFragment;

    public CameraPreviewFragment() {
        mCameraParam = CameraParam.getInstance();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        int currentMode = BrightnessUtils.getSystemBrightnessMode(mActivity);
        if (currentMode == 1) {
            mCameraParam.brightness = -1;
        } else {
            mCameraParam.brightness = BrightnessUtils.getSystemBrightness(mActivity);
        }
        mMainHandler = new Handler(context.getMainLooper());
        mCameraEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA);
        mStorageWriteEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mCameraParam.audioPermitted = PermissionUtils.permissionChecking(mActivity, Manifest.permission.RECORD_AUDIO);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化相机渲染引擎
        PreviewRenderer.getInstance()
                .setCameraCallback(mCameraCallback)
                .setCaptureFrameCallback(mCaptureCallback)
                .setFpsCallback(mFpsListener)
                .setFacePointsListener(mFacePointsListener)
                .initRenderer(mActivity);
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
            requestCameraPermission();
        }
        initTracker();
    }

    /**
     * 初始化页面
     * @param view
     */
    private void initView(View view) {
        mAspectLayout = (AspectFrameLayout) view.findViewById(R.id.layout_aspect);
        mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
        mCameraSurfaceView = new CainSurfaceView(mActivity);
        mCameraSurfaceView.addOnTouchScroller(mTouchScroller);
        mCameraSurfaceView.addMultiClickListener(mMultiClickListener);
        mAspectLayout.addView(mCameraSurfaceView);
        mAspectLayout.requestLayout();
        // 绑定需要渲染的SurfaceView
        PreviewRenderer.getInstance().setSurfaceView(mCameraSurfaceView);

        mFpsView = (TextView) view.findViewById(R.id.tv_fps);
        mBtnCompare = (Button) view.findViewById(R.id.btn_compare);
        mBtnCompare.setVisibility(View.GONE);
        mBtnCompare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        PreviewRenderer.getInstance().enableCompare(true);
                        mBtnCompare.setBackgroundResource(R.drawable.ic_camera_compare_pressed);
                        break;

                    default:
                        PreviewRenderer.getInstance().enableCompare(false);
                        mBtnCompare.setBackgroundResource(R.drawable.ic_camera_compare_normal);
                        break;
                }
                return true;
            }
        });
        mBtnSetting = (Button)view.findViewById(R.id.btn_setting);
        mBtnSetting.setOnClickListener(this);
        mBtnViewPhoto = (Button) view.findViewById(R.id.btn_view_photo);
        mBtnViewPhoto.setOnClickListener(this);
        mBtnSwitch = (Button) view.findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);
        mRatioView = (RatioImageView) view.findViewById(R.id.iv_ratio);
        mRatioView.setRatioType(mCameraParam.aspectRatio);
        mRatioView.addRatioChangedListener(mRatioChangedListener);

        mCountDownView = (TextView) view.findViewById(R.id.tv_countdown);
        mBtnStickers = (Button) view.findViewById(R.id.btn_stickers);
        mBtnStickers.setOnClickListener(this);
        mBtnEffect = (Button) view.findViewById(R.id.btn_effects);
        mBtnEffect.setOnClickListener(this);
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
        mBtnEffect.setBackgroundResource(result ? R.drawable.ic_camera_effect_light : R.drawable.ic_camera_effect_dark);
        mBtnRecordDelete.setBackgroundResource(result ? R.drawable.ic_camera_record_delete_light : R.drawable.ic_camera_record_delete_dark);
        mBtnRecordPreview.setBackgroundResource(result ? R.drawable.ic_camera_record_done_light : R.drawable.ic_camera_record_done_dark);
        mBtnShutter.setOuterBackgroundColor(result ? R.color.shutter_gray_light : R.color.shutter_gray_dark);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerHomeReceiver();
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
        unRegisterHomeReceiver();
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
        mPageListener = null;
        // 销毁人脸检测器
        releaseFaceTracker();
        // 关掉渲染引擎
        PreviewRenderer.getInstance().destroyRenderer();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    /**
     * 处理返回事件
     * @return
     */
    public boolean onBackPressed() {
        if (isShowingFilters) {
            hideEffectView();
            return true;
        } else if (isShowingStickers) {
            isShowingStickers = false;
            hideStickerView();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (mSettingView != null) {
            mSettingView.dismiss();
        }
        int i = v.getId();
        if (i == R.id.btn_view_photo) {
            openGallery();
        } else if (i == R.id.btn_switch) {
            switchCamera();
        } else if (i == R.id.btn_setting) {
            showSettingPopView();
        } else if (i == R.id.btn_stickers) {
            showStickers();
        } else if (i == R.id.btn_effects) {
            showEffectView();
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
                requestStoragePermission();
            }
        } else if (currentIndex == 2) {
            mCameraParam.mGalleryType = GalleryType.VIDEO;
            // 录制视频状态
            mBtnShutter.setIsRecorder(true);
            // 请求录音权限
            if (!mCameraParam.audioPermitted) {
                requestRecordSoundPermission();
            }
        }
        // 显示时间
        if (currentIndex == 2) {
            mCountDownView.setVisibility(View.VISIBLE);
        } else {
            mCountDownView.setVisibility(View.GONE);
        }
    }

    /**
     * 打开图库
     */
    private void openGallery() {
        if (mPageListener != null) {
            mPageListener.onOpenGalleryPage();
        }
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (!mCameraEnable) {
            requestCameraPermission();
            return;
        }
        PreviewRenderer.getInstance().switchCamera();
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
    }

    /**
     * 显示动态贴纸页面
     */
    private void showStickers() {
        isShowingStickers = true;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mStickerFragment == null) {
            mStickerFragment = new PreviewStickerFragment();
            ft.add(R.id.fragment_container, mStickerFragment);
        } else {
            ft.show(mStickerFragment);
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
            ft.add(R.id.fragment_container, mEffectFragment);
        } else {
            ft.show(mEffectFragment);
        }
        ft.commit();
        mEffectFragment.scrollToCurrentFilter(mFilterIndex);
        hideBottomLayout();
    }

    /**
     * 隐藏动态贴纸页面
     */
    private void hideStickerView() {
        if (isShowingStickers) {
            isShowingStickers = false;
            if (mStickerFragment != null) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.hide(mStickerFragment);
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
    }

    /**
     * 隐藏底部布局按钮
     */
    private void hideBottomLayout() {
        mBtnEffect.setVisibility(View.GONE);
        mBtnStickers.setVisibility(View.GONE);
        mBottomIndicator.setVisibility(View.GONE);
        ViewGroup.LayoutParams layoutParams = mBtnShutter.getLayoutParams();
        layoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                60, mActivity.getResources().getDisplayMetrics());
        layoutParams.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                60, mActivity.getResources().getDisplayMetrics());
        mBtnShutter.setLayoutParams(layoutParams);
    }

    /**
     * 恢复底部布局
     */
    private void resetBottomLayout() {
        mBtnShutter.setOuterBackgroundColor(mCameraParam.currentRatio < CameraParam.Ratio_4_3
                ? R.color.shutter_gray_light : R.color.shutter_gray_dark);
        ViewGroup.LayoutParams layoutParams = mBtnShutter.getLayoutParams();
        layoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                100, mActivity.getResources().getDisplayMetrics());
        layoutParams.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                100, mActivity.getResources().getDisplayMetrics());
        mBtnShutter.setLayoutParams(layoutParams);
        mBtnEffect.setVisibility(View.VISIBLE);
        mBtnStickers.setVisibility(View.VISIBLE);
        mBottomIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mStorageWriteEnable) {
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                if (mCameraParam.takeDelay && !mDelayTaking) {
                    mDelayTaking = true;
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDelayTaking = false;
                            PreviewRenderer.getInstance().takePicture();
                        }
                    }, 3000);
                } else {
                    PreviewRenderer.getInstance().takePicture();
                }
            }
        } else {
            requestStoragePermission();
        }
    }


    // ------------------------------- SurfaceView 滑动、点击回调 ----------------------------------
    private CainSurfaceView.OnTouchScroller mTouchScroller = new CainSurfaceView.OnTouchScroller() {

        @Override
        public void swipeBack() {
            if (mEffectFragment != null) {
                mFilterIndex = mEffectFragment.getCurrentFilterIndex();
            }
            mFilterIndex++;
            mFilterIndex = mFilterIndex % GLImageFilterManager.getFilterTypes().size();
            PreviewRenderer.getInstance()
                    .changeFilterType(GLImageFilterManager.getFilterTypes().get(mFilterIndex));
            if (mEffectFragment != null) {
                mEffectFragment.scrollToCurrentFilter(mFilterIndex);
            }
        }

        @Override
        public void swipeFrontal() {
            if (mEffectFragment != null) {
                mFilterIndex = mEffectFragment.getCurrentFilterIndex();
            }
            mFilterIndex--;
            if (mFilterIndex < 0) {
                int count = GLImageFilterManager.getFilterTypes().size();
                mFilterIndex = count > 0 ? count - 1 : 0;
            }
            PreviewRenderer.getInstance()
                    .changeFilterType(GLImageFilterManager.getFilterTypes().get(mFilterIndex));

            if (mEffectFragment != null) {
                mEffectFragment.scrollToCurrentFilter(mFilterIndex);
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

    /**
     * 单双击回调监听
     */
    private CainSurfaceView.OnMultiClickListener mMultiClickListener = new CainSurfaceView.OnMultiClickListener() {

        @Override
        public void onSurfaceSingleClick(final float x, final float y) {
            // 单击隐藏贴纸和滤镜页面
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    hideStickerView();
                    hideEffectView();
                }
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
                            mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight(), FocusSize));
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCameraSurfaceView.showFocusAnimation();
                        }
                    });
                }
            }
        }

        @Override
        public void onSurfaceDoubleClick(float x, float y) {
            switchCamera();
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
            if (mPageListener != null) {
                mPageListener.onOpenCameraSettingPage();
            }
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

    };

    // ------------------------------------- 长宽比改变回调 --------------------------------------
    private RatioImageView.OnRatioChangedListener mRatioChangedListener = new RatioImageView.OnRatioChangedListener() {
        @Override
        public void onRatioChanged(AspectRatio type) {
            mCameraParam.setAspectRatio(type);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
                    PreviewRenderer.getInstance().reopenCamera();
                    adjustBottomView();
                    PreviewRenderer.getInstance().surfaceSizeChanged(mCameraSurfaceView.getWidth(),
                            mCameraSurfaceView.getHeight());
                }
            });
        }
    };

    // -------------------------------------- fps回调 -------------------------------------------
    private OnFpsListener mFpsListener = new OnFpsListener() {
        @Override
        public void onFpsCallback(final float fps) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCameraParam.showFps) {
                        mFpsView.setText("fps = " + fps);
                        mFpsView.setVisibility(View.VISIBLE);
                    } else {
                        mFpsView.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    // ------------------------------------ 拍照回调 ---------------------------------------------
    private OnCaptureListener mCaptureCallback = new OnCaptureListener() {
        @Override
        public void onCapture(final ByteBuffer buffer, final int width, final int height) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    String filePath = PathConstraints.getMediaPath();
                    BitmapUtils.saveBitmap(filePath, buffer, width, height);
                    if (mPageListener != null) {
                        mPageListener.onOpenImageEditPage(filePath);
                    }
                }
            });
        }
    };

    // ------------------------------------ 预览回调 ---------------------------------------------
    private OnCameraCallback mCameraCallback = new OnCameraCallback() {

        @Override
        public void onCameraOpened() {
            // 相机打开之后准备检测器
            prepareTracker();
        }

        @Override
        public void onPreviewCallback(byte[] data) {
            if (mBtnShutter != null && !mBtnShutter.isEnableOpened()) {
                mBtnShutter.setEnableOpened(true);
            }
            // 人脸检测
            FaceTracker.getInstance().trackFace(data,
                    mCameraParam.previewWidth, mCameraParam.previewHeight);
            // 请求刷新
            requestRender();
        }


    };

    /**
     * 请求渲染
     */
    private void requestRender() {
        PreviewRenderer.getInstance().requestRender();
    }

    /**
     * 初始化人脸检测器
     */
    private void initTracker() {
        FaceTracker.getInstance()
                .setFaceCallback(mFaceTrackerCallback)
                .previewTrack(true)
                .initTracker();
    }

    /**
     * 销毁人脸检测器
     */
    private void releaseFaceTracker() {
        FaceTracker.getInstance().destroyTracker();
    }

    /**
     * 准备人脸检测器
     */
    private void prepareTracker() {
        FaceTracker.getInstance()
                .setBackCamera(mCameraParam.backCamera)
                .prepareFaceTracker(mActivity, mCameraParam.orientation,
                        mCameraParam.previewWidth, mCameraParam.previewHeight);
    }

    /**
     * 检测完成回调
     */
    private FaceTrackerCallback mFaceTrackerCallback = new FaceTrackerCallback() {
        @Override
        public void onTrackingFinish(boolean hasFaces, ArrayList<ArrayList> debugFacePoints) {
            synchronized (this) {
                mDebugFacePoints.clear();
                if (debugFacePoints != null && debugFacePoints.size() > 0) {
                    mDebugFacePoints.addAll(debugFacePoints);
                }
            }
            // 检测完成需要请求刷新
            requestRender();
        }
    };

    private ArrayList<ArrayList> mDebugFacePoints = new ArrayList<ArrayList>();

    // ------------------------------------- 人脸关键点调试回调 --------------------------------------
    private OnFacePointsListener mFacePointsListener = new OnFacePointsListener() {
        @Override
        public boolean showFacePoints() {
            return mCameraParam.drawFacePoints;
        }

        @Override
        public ArrayList<ArrayList> getDebugFacePoints() {
            return mDebugFacePoints;
        }
    };


    // ------------------------------------ 录制回调 -------------------------------------------
    private ShutterButton.OnShutterListener mShutterListener = new ShutterButton.OnShutterListener() {

        @Override
        public void onStartRecord() {
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                return;
            }

            // 隐藏删除按钮
            if (mCameraParam.mGalleryType == GalleryType.VIDEO) {
                mBtnRecordPreview.setVisibility(View.GONE);
                mBtnRecordDelete.setVisibility(View.GONE);
            }
            mBtnShutter.setProgressMax((int) PreviewRecorder.getInstance().getMaxMilliSeconds());
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
            // 开始录制
            PreviewRecorder.getInstance()
                    .setRecordType(mCameraParam.mGalleryType == GalleryType.VIDEO ? PreviewRecorder.RecordType.Video : PreviewRecorder.RecordType.Gif)
                    .setOutputPath(PathConstraints.getVideoPath())
                    .enableAudio(enableAudio)
                    .setRecordSize(width, height)
                    .setOnRecordListener(mRecordListener)
                    .startRecord();
        }

        @Override
        public void onStopRecord() {
            PreviewRecorder.getInstance().stopRecord();
        }

        @Override
        public void onProgressOver() {
            // 如果最后一秒内点击停止录制，则仅仅关闭录制按钮，因为前面已经停止过了，不做跳转
            // 如果最后一秒内没有停止录制，否则停止录制并跳转至预览页面
            if (PreviewRecorder.getInstance().isLastSecondStop()) {
                // 关闭录制按钮
                mBtnShutter.closeButton();
            } else {
                stopRecordOrPreviewVideo();
            }
        }
    };

    /**
     * 录制监听器
     */
    private OnRecordListener mRecordListener = new OnRecordListener() {

        @Override
        public void onRecordStarted() {
            // 编码器已经进入录制状态，则快门按钮可用
            mBtnShutter.setEnableEncoder(true);
        }

        @Override
        public void onRecordProgressChanged(final long duration) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 设置进度
                    mBtnShutter.setProgress(duration);
                    // 设置时间
                    mCountDownView.setText(StringUtils.generateMillisTime((int) duration));
                }
            });
        }

        @Override
        public void onRecordFinish() {
            // 编码器已经完全释放，则快门按钮可用
            mBtnShutter.setEnableEncoder(true);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
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
                }
            });
        }
    };

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
                PreviewRecorder.getInstance().removeAllSubVideo();
            } else {
                // 删除分割线
                mBtnShutter.deleteSplitView();
                PreviewRecorder.getInstance().removeLastSubVideo();
            }

            // 删除一段已记录的时间
            PreviewRecorder.getInstance().deleteRecordDuration();

            // 更新进度
            mBtnShutter.setProgress(PreviewRecorder.getInstance().getVisibleDuration());
            // 更新时间
            mCountDownView.setText(PreviewRecorder.getInstance().getVisibleDurationString());
            // 如果此时没有了视频，则恢复初始状态
            if (PreviewRecorder.getInstance().getNumberOfSubVideo() <= 0) {
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
        if (PreviewRecorder.getInstance().isRecording()) {
            mNeedToWaitStop = true;
            PreviewRecorder.getInstance().stopRecord(false);
        } else {
            mNeedToWaitStop = false;
            // 销毁录制线程
            PreviewRecorder.getInstance().destroyRecorder();
            combinePath = PathConstraints.getVideoPath();
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
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCombineDialog != null) {
                        mCombineDialog.dismiss();
                        mCombineDialog = null;
                    }
                    mCombineDialog = CombineVideoDialogFragment.newInstance(mActivity.getString(R.string.combine_video_message));
                    mCombineDialog.show(getChildFragmentManager(), FRAGMENT_DIALOG);
                }
            });
        }

        @Override
        public void onCombineProcessing(final int current, final int sum) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCombineDialog != null && mCombineDialog.getShowsDialog()) {
                        mCombineDialog.setProgressMessage(mActivity.getString(R.string.combine_video_message));
                    }
                }
            });
        }

        @Override
        public void onCombineFinished(final boolean success) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCombineDialog != null) {
                        mCombineDialog.dismiss();
                        mCombineDialog = null;
                    }
                }
            });
            if (mPageListener != null)  {
                mPageListener.onOpenVideoEditPage(combinePath);
            }
        }
    };

    /**
     * 请求相机权限
     */
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_camera_permission), PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{ Manifest.permission.CAMERA},
                    PermissionUtils.REQUEST_CAMERA_PERMISSION);
        }
    }

    /**
     * 请求存储权限
     */
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtils.REQUEST_STORAGE_PERMISSION);
        }
    }

    /**
     * 请求录音权限
     */
    private void requestRecordSoundPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO},
                    PermissionUtils.REQUEST_SOUND_PERMISSION);
        }
    }

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

    /**
     * 注册服务
     */
    private void registerHomeReceiver() {
        if (mActivity != null) {
            IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            mActivity.registerReceiver(mHomePressReceiver, homeFilter);
        }
    }

    /**
     * 注销服务
     */
    private void unRegisterHomeReceiver() {
        if (mActivity != null) {
            mActivity.unregisterReceiver(mHomePressReceiver);
        }
    }

    /**
     * Home按键监听服务
     */
    private BroadcastReceiver mHomePressReceiver = new BroadcastReceiver() {
        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (TextUtils.isEmpty(reason)) {
                    return;
                }
                // 当点击了home键时需要停止预览，防止后台一直持有相机
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    // 停止录制
                    if (PreviewRecorder.getInstance().isRecording()) {
                        // 取消录制
                        PreviewRecorder.getInstance().cancelRecording();
                        // 重置进入条
                        mBtnShutter.setProgress((int) PreviewRecorder.getInstance().getVisibleDuration());
                        // 删除分割线
                        mBtnShutter.deleteSplitView();
                        // 关闭按钮
                        mBtnShutter.closeButton();
                        // 更新时间
                        mCountDownView.setText(PreviewRecorder.getInstance().getVisibleDurationString());
                    }
                }
            }
        }
    };

    /**
     * 设置页面监听器
     * @param listener
     */
    public void setOnPageOperationListener(OnPageOperationListener listener) {
        mPageListener = listener;
    }
}
