package com.cgfay.caincamera.activity.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.activity.CameraActivity;
import com.cgfay.caincamera.activity.RtmpPushActivity;
import com.cgfay.caincamera.activity.facetrack.FaceTrack2Activity;
import com.cgfay.caincamera.activity.facetrack.FaceTrackActivity;
import com.cgfay.utilslibrary.PermissionUtils;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CAMERA = 0x01;
    private static final int REQUEST_STORAGE_READ = 0x02;
    private static final int REQUEST_STORAGE_WRITE = 0x03;
    private static final int REQUEST_RECORD = 0x04;
    private static final int REQUEST_LOCATION = 0x05;

    private boolean mCameraEnable = false;
    private boolean mStorageWriteEnable = false;

    private Button mBtnMulti;
    private Button mBtnFaceTest;
    private Button mBtnFaceTest2;
    private Button mBtnPush;
    private Button mBtnRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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

    private void initView() {
        mBtnMulti = (Button) findViewById(R.id.btn_multi);
        mBtnMulti.setOnClickListener(this);

        mBtnFaceTest = (Button) findViewById(R.id.btn_face_test);
        mBtnFaceTest.setOnClickListener(this);

        mBtnFaceTest2 = (Button) findViewById(R.id.btn_face_test2);
        mBtnFaceTest2.setOnClickListener(this);

        mBtnPush = (Button) findViewById(R.id.btn_push);
        mBtnPush.setOnClickListener(this);

        mBtnRecord = (Button) findViewById(R.id.btn_video_record);
        mBtnRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_multi:
                startActivity(new Intent(TestActivity.this, CameraTestActivity.class));
                break;

            case R.id.btn_face_test:
                startActivity(new Intent(TestActivity.this, FaceTrackActivity.class));
                break;

            case R.id.btn_face_test2:
                startActivity(new Intent(TestActivity.this, FaceTrack2Activity.class));
                break;

            case R.id.btn_push:
                startActivity(new Intent(TestActivity.this, RtmpPushActivity.class));
                break;

            case R.id.btn_video_record:
                startActivity(new Intent(TestActivity.this, VideoRecordActivity.class));
                break;
        }
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
}
