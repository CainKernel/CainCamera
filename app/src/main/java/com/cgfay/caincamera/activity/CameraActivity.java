package com.cgfay.caincamera.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.core.CameraDrawer;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.view.AspectFrameLAyout;
import com.cgfay.caincamera.view.CameraSurfaceView;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private AspectFrameLAyout mAspectLayout;
    private CameraSurfaceView mCameraSurfaceView;
    private Button mBtnViewPhoto;
    private Button mBtnTake;
    private Button mBtnSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        mAspectLayout = (AspectFrameLAyout) findViewById(R.id.layout_aspect);
        mAspectLayout.setAspectRatio(3 / 4);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.view_camera);
        CameraUtils.calculateCameraPreviewOrientation(CameraActivity.this);
        mBtnViewPhoto = (Button) findViewById(R.id.btn_view_photo);
        mBtnViewPhoto.setOnClickListener(this);
        mBtnTake = (Button) findViewById(R.id.btn_take);
        mBtnTake.setOnClickListener(this);
        mBtnSwitch = (Button) findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraUtils.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraUtils.stopPreview();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_view_photo:
                break;

            case R.id.btn_take:
                takePicture();
                break;

            case R.id.btn_switch:
                switchCamera();
                break;
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        CameraDrawer.INSTANCE.takePicture();
    }


    /**
     * 切换相机
     */
    private void switchCamera() {
        if (mCameraSurfaceView != null) {
            CameraUtils.switchCamera(1 - CameraUtils.getCameraID());
        }
    }
}
