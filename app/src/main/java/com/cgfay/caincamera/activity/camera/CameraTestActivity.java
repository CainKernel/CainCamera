package com.cgfay.caincamera.activity.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.bean.Size;
import com.cgfay.caincamera.core.ParamsManager;
import com.cgfay.caincamera.utils.PermissionUtils;
import com.cgfay.caincamera.view.AspectFrameLayout;

/**
 * 双HandlerThread 测试, MTK的CPU在双HandlerThread模型上表现非常糟糕
 * onPreviewFrame回调帧率只有单一HandlerThread模型的一半左右
 */
public class CameraTestActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    private static final int REQUEST_CAMERA = 0x01;
    private static final int REQUEST_STORAGE_READ = 0x02;
    private static final int REQUEST_STORAGE_WRITE = 0x03;
    private static final int REQUEST_RECORD = 0x04;
    private static final int REQUEST_LOCATION = 0x05;

    private AspectFrameLayout mAspectLayout;

    private SurfaceView mSurfaceView;
    // 相机控制线程
    private CameraHandlerThread mCameraThread;
    // 渲染控制线程
    private RenderTestThread mRenderThread;

    private byte[] mPreviewBuffer;

    private boolean mCameraEnable = false;
    private boolean mStorageWriteEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);

        mCameraEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.CAMERA);
        mStorageWriteEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (mCameraEnable && mStorageWriteEnable) {
            initView();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_CAMERA);
        }

    }

    /**
     * 初始化视图
     */
    private void initView() {
        mAspectLayout = (AspectFrameLayout) findViewById(R.id.layout_aspect);
        mAspectLayout.setAspectRatio(CameraManager.getInstance().getCurrentRatio());

        mSurfaceView = new SurfaceView(this);
        mSurfaceView.getHolder().addCallback(this);

        mAspectLayout.addView(mSurfaceView);
        mAspectLayout.requestLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraThread = new CameraHandlerThread();
        mCameraThread.calculatePreviewOrientation(this);
        mRenderThread = new RenderTestThread();
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
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // CameraThread是一个HandlerThread，调用Camera.open相机，onPreviewFrame回调所绑定的线程是CameraThread，
        // 如果使用new Thread的方式调用Camera.open相机，onPreviewFrame回调将绑定到到MainLooper，也就是回调到主线程
        // 这里使用单独的HandlerThread控制Camera逻辑
        mCameraThread.openCamera();

        Size previewSize = mCameraThread.getPreviewSize();
        int size = previewSize.getWidth() * previewSize.getHeight() * 3 / 2;
        mPreviewBuffer = new byte[size];

        mRenderThread.surfaceCreated(holder);
        mRenderThread.setImageSize(previewSize.getWidth(), previewSize.getHeight(),
                mCameraThread.getPreviewOrientation());

        // 设置预览SurfaceTexture
        mCameraThread.setPreviewSurface(mRenderThread.getSurafceTexture());
        // 设置回调
        mCameraThread.setPreviewCallbackWithBuffer(CameraTestActivity.this, mPreviewBuffer);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCameraThread.startPreview();
        mRenderThread.surfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraThread.destoryThread();
        mRenderThread.destoryThread();
    }

    /**
     测试情况：
     高通：
     VIVO X9i，高通625
     红米Note 4X，高通625
     Nexus 5X，高通808
     onPreviewFrame回调时间： 单一 HandlerThread， 30~40ms； 双HandlerThread，30~40ms
     preview size： 1280 x 720
     单一HandlerThread的情况，请参考CameraActivity里面的情况

     联发科:
     红米Note4， 联发科 Helio X20
     魅蓝Note 2，联发科 MTK6573
     乐视 X620，联发科X20
     onPreviewFrame回调时间：单一HandlerThread， 30 ~ 40ms；双HandlerThread，60~70ms
     preview size： 1280 x 720

     操作：
     Camera数据流渲染到SurfaceTexture显示到SurfaceView上，设置setPreviewCallbackWithBuffer，查看onPreviewFrame的帧率
     */
    long time = 0;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d("onPreviewFrame", "update time = " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        mRenderThread.updateFrame();

        if (mPreviewBuffer != null) {
            camera.addCallbackBuffer(mPreviewBuffer);
        }
    }

}
