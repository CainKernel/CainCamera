package com.cgfay.caincamera.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.core.AspectRatioType;
import com.cgfay.caincamera.core.DrawerManager;
import com.cgfay.caincamera.core.ParamsManager;
import com.cgfay.caincamera.type.FilterType;
import com.cgfay.caincamera.type.GalleryType;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.utils.PermissionUtils;
import com.cgfay.caincamera.utils.TextureRotationUtils;
import com.cgfay.caincamera.utils.faceplus.ConUtil;
import com.cgfay.caincamera.utils.faceplus.Util;
import com.cgfay.caincamera.view.AspectFrameLayout;
import com.cgfay.caincamera.view.CameraSurfaceView;
import com.cgfay.caincamera.view.HorizontalIndicatorView;
import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener,
        CameraSurfaceView.OnClickListener, CameraSurfaceView.OnTouchScroller,
        HorizontalIndicatorView.IndicatorListener {

    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CAMERA = 0x01;
    private static final int REQUEST_STORAGE_READ = 0x02;
    private static final int REQUEST_STORAGE_WRITE = 0x03;
    private static final int REQUEST_RECORD = 0x04;
    private static final int REQUEST_LOCATION = 0x05;

    private static final int FocusSize = 100;
    // 权限使能标志
    private boolean mCameraEnable = false;
    private boolean mStorageWriteEnable = false;
    private boolean mRecordSoundEnable = false;
    private boolean mLocationEnable = false;

    // 状态标志
    private boolean mOnPreviewing = false;
    private boolean mOnRecording = false;

    private AspectFrameLayout mAspectLayout;
    private CameraSurfaceView mCameraSurfaceView;
    // 顶部Button
    private Button mBtnSetting;
    private Button mBtnViewPhoto;
    private Button mBtnSwitch;
    // 底部layout 和 Button
    private LinearLayout mBottomLayout;
    private Button mBtnStickers;
    private Button mBtnTake;
    private Button mBtnFilters;

    // 底部指示器
    private List<String> mIndicatorText = new ArrayList<String>();
    private HorizontalIndicatorView mBottomIndicator;

    private AspectRatioType[] mAspectRatio = {
            AspectRatioType.RATIO_4_3,
            AspectRatioType.RATIO_1_1,
            AspectRatioType.Ratio_16_9
    };
    private int mRatioIndex = 0;
    private AspectRatioType mCurrentRatio = mAspectRatio[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 持有当前上下文
        ParamsManager.context = this;
        String phoneName = Build.MODEL;
        if (phoneName.toLowerCase().contains("bullhead")
                || phoneName.toLowerCase().contains("nexus 5x")) {
            TextureRotationUtils.setBackReverse(true);
            ParamsManager.mBackReverse = true;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        mCameraEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.CAMERA);
        mStorageWriteEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mRecordSoundEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.RECORD_AUDIO);
        ParamsManager.canRecordingAudio = mRecordSoundEnable;
        if (mCameraEnable && mStorageWriteEnable) {
            initView();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_CAMERA);
        }
        requestFaceNetwork();
    }

    private void initView() {
        mAspectLayout = (AspectFrameLayout) findViewById(R.id.layout_aspect);
        mAspectLayout.setAspectRatio(CameraUtils.getCurrentRatio());
        mCameraSurfaceView = new CameraSurfaceView(this);
        mCameraSurfaceView.addScroller(this);
        mCameraSurfaceView.addClickListener(this);
        mAspectLayout.addView(mCameraSurfaceView);
        mAspectLayout.requestLayout();
        mBtnSetting = (Button)findViewById(R.id.btn_setting);
        mBtnSetting.setOnClickListener(this);
        mBtnViewPhoto = (Button) findViewById(R.id.btn_view_photo);
        mBtnViewPhoto.setOnClickListener(this);
        mBtnTake = (Button) findViewById(R.id.btn_take);
        mBtnTake.setOnClickListener(this);
        mBtnSwitch = (Button) findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);

        mBottomLayout = (LinearLayout) findViewById(R.id.layout_bottom);
        if (CameraUtils.getCurrentRatio() < 0.75f) {
            mBottomLayout.setBackgroundResource(R.drawable.bottom_background_glow);
        } else {
            mBottomLayout.setBackgroundResource(R.drawable.bottom_background);
        }
        mBtnStickers = (Button) findViewById(R.id.btn_stickers);
        mBtnStickers.setOnClickListener(this);
        mBtnFilters = (Button) findViewById(R.id.btn_filters);
        mBtnFilters.setOnClickListener(this);
        mBottomIndicator = (HorizontalIndicatorView) findViewById(R.id.bottom_indicator);
        String[] galleryIndicator = getResources().getStringArray(R.array.gallery_indicator);
        for (String text : galleryIndicator) {
            mIndicatorText.add(text);
        }
        mBottomIndicator.setIndicators(mIndicatorText);
        mBottomIndicator.addIndicatorListener(this);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{ Manifest.permission.CAMERA }, REQUEST_CAMERA);
    }

    private void requestStorageWritePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_STORAGE_WRITE);
    }

    private void requestRecordPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{ Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                },
                REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // 相机权限
            case REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraEnable = true;
                    initView();
                }
                break;

            // 存储权限
            case REQUEST_STORAGE_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mStorageWriteEnable = true;
                }
                break;

            // 录音权限
            case REQUEST_RECORD:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRecordSoundEnable = true;
                    ParamsManager.canRecordingAudio = true;
                }
                break;

            // 位置权限
            case REQUEST_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationEnable = true;
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerHomeReceiver();
        if (mCameraEnable) {
            DrawerManager.getInstance().startPreview();
            mOnPreviewing = true;
        } else {
            requestCameraPermission();
        }
        // 判断是否允许写入权限
        if (PermissionUtils.permissionChecking(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            mStorageWriteEnable = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterHomeReceiver();
        if (mCameraEnable) {
            DrawerManager.getInstance().stopPreview();
            mOnPreviewing = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在停止时需要释放上下文，防止内存泄漏
        ParamsManager.context = null;
    }

    private void registerHomeReceiver() {
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomePressReceiver, homeFilter);
    }

    private void unRegisterHomeReceiver() {
        unregisterReceiver(mHomePressReceiver);
    }

    /**
     * 监听点击home键
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
                    if (mOnPreviewing) {
                        DrawerManager.getInstance().stopPreview();
                    }
                }
            }
        }
    };

    /**
     * Face++SDK联网请求
     */
    private void requestFaceNetwork() {
        if (Facepp.getSDKAuthType(ConUtil.getFileContent(this, R.raw
                .megviifacepp_0_4_7_model)) == 2) {// 非联网授权
            ParamsManager.canFaceTrack = true;
            return;
        }
        final LicenseManager licenseManager = new LicenseManager(this);
        licenseManager.setExpirationMillis(Facepp.getApiExpirationMillis(this, ConUtil.getFileContent(this, R.raw
                .megviifacepp_0_4_7_model)));

        String uuid = ConUtil.getUUIDString(this);
        long apiName = Facepp.getApiName();

        licenseManager.setAuthTimeBufferMillis(0);

        licenseManager.takeLicenseFromNetwork(uuid, Util.API_KEY, Util.API_SECRET, apiName,
                LicenseManager.DURATION_30DAYS, "Landmark", "1", true, new LicenseManager.TakeLicenseCallback() {
                    @Override
                    public void onSuccess() {
                        ParamsManager.canFaceTrack = true;
                    }

                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        Log.d("LicenseManager", "Failed to register license!");
                        ParamsManager.canFaceTrack = false;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_view_photo:
                viewPhoto();
                break;

            case R.id.btn_take:
                takePictureOrVideo();
                break;

            case R.id.btn_switch:
                switchCamera();
                break;

            case R.id.btn_setting:
                showSettingPopView();
                break;

            case R.id.btn_stickers:
                showStickers();
                break;

            case R.id.btn_filters:
                showFilters();
                break;
        }
    }

    @Override
    public void swipeBack() {
        Log.d(TAG, "swipeBack");
        DrawerManager.getInstance().changeFilterType(FilterType.SKETCH);
    }

    @Override
    public void swipeFrontal() {
        Log.d(TAG, "swipeFrontal");
        DrawerManager.getInstance().changeFilterType(FilterType.WHITENORREDDEN);
    }

    @Override
    public void swipeUpper(boolean startInLeft) {
        Log.d(TAG, "swipeUpper, startInLeft ? " + startInLeft);
    }

    @Override
    public void swipeDown(boolean startInLeft) {
        Log.d(TAG, "swipeDown, startInLeft ? " + startInLeft);
    }

    @Override
    public void onClick(float x, float y) {
        surfaceViewClick(x, y);
    }

    @Override
    public void doubleClick(float x, float y) {
    }

    /**
     * 点击SurfaceView
     * @param x x轴坐标
     * @param y y轴坐标
     */
    private void surfaceViewClick(float x, float y) {
        DrawerManager.getInstance().setFocusAres(focusOnTouch((int)x, (int)y));
    }

    /**
     * 查看媒体库中的图片
     */
    private void viewPhoto() {
        startActivity(new Intent(CameraActivity.this, PhotoViewActivity.class));
    }

    /**
     * 拍照
     */
    private void takePictureOrVideo() {
        if (!mOnPreviewing) {
            return;
        }
        if (mStorageWriteEnable
                || PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (ParamsManager.mGalleryType == GalleryType.GIF) {

            } else if (ParamsManager.mGalleryType == GalleryType.PICTURE) {
                DrawerManager.getInstance().takePicture();
            } else if (ParamsManager.mGalleryType == GalleryType.VIDEO) {
                if (mOnRecording) {
                    DrawerManager.getInstance().stopRecording();
                    mBtnTake.setBackgroundResource(R.drawable.round_green);
                } else {
                    DrawerManager.getInstance().startRecording();
                    mBtnTake.setBackgroundResource(R.drawable.round_red);
                }
                mOnRecording = !mOnRecording;
            }
        } else {
            requestStorageWritePermission();
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
        if (mCameraSurfaceView != null) {
            DrawerManager.getInstance().switchCamera();
        }
    }

    /**
     * 显示设置更多视图
     */
    private void showSettingPopView() {

    }

    /**
     * 显示贴纸列表
     */
    private void showStickers() {

    }

    /**
     * 显示滤镜
     */
    private void showFilters() {

    }

    @Override
    public void onIndicatorChanged(int currentIndex) {
        if (currentIndex == 0) {
            ParamsManager.mGalleryType = GalleryType.GIF;
        } else if (currentIndex == 1) {
            ParamsManager.mGalleryType = GalleryType.PICTURE;
        } else if (currentIndex == 2) {
            ParamsManager.mGalleryType = GalleryType.VIDEO;
            // 请求录音权限
            if (!mRecordSoundEnable) {
                ActivityCompat.requestPermissions(this,
                        new String[]{ Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD);
            }
        }
    }

    /**
     * 计算触摸区域
     * @param x
     * @param y
     * @return
     */
    private Rect focusOnTouch(int x, int y) {
        Rect rect = new Rect(x - FocusSize, y - FocusSize,
                x + FocusSize, y + FocusSize);
        int left = rect.left * 2000 / mCameraSurfaceView.getWidth() - 1000;
        int top = rect.top * 2000 / mCameraSurfaceView.getHeight() - 1000;
        int right = rect.right * 2000 / mCameraSurfaceView.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / mCameraSurfaceView.getHeight() - 1000;
        // 归整到(-1000, -1000) 到 (1000, 1000)的区域内
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        return new Rect(left, top, right, bottom);
    }
}
