package com.cgfay.caincamera.activity;

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
import com.cgfay.caincamera.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0;
    private Button mBtnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        initView();
    }

    /**
     * 检查权限
     */
    private void checkPermissions() {
        boolean cameraEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.CAMERA);
        boolean storageWriteEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!cameraEnable || !storageWriteEnable) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_CODE);
        }
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mBtnCamera = (Button) findViewById(R.id.btn_camera);

        mBtnCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
                break;

        }
    }
}
