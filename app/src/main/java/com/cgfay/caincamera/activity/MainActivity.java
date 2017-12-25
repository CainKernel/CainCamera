package com.cgfay.caincamera.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cgfay.caincamera.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnCamera;
    private Button mBtnVideoRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnCamera = (Button) findViewById(R.id.btn_camera);
        mBtnVideoRecord = (Button) findViewById(R.id.btn_video_record);

        mBtnCamera.setOnClickListener(this);
        mBtnVideoRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
                break;

            case R.id.btn_video_record:
                startActivity(new Intent(MainActivity.this, VideoRecordActivity.class));
                break;
        }
    }
}
