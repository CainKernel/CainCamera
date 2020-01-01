package com.cgfay.camera.activity;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.camera.camera.CameraParam;

/**
 * 相机设置页面
 */
public class CameraSettingActivity extends AppCompatActivity {

    private RelativeLayout mLayoutSelectWatermark;
    private RelativeLayout mLayoutShowFacePoints;
    private TextView mTextFacePoints;
    private RelativeLayout mLayoutShowFps;
    private TextView mTextFps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_setting);
        initView();
    }

    private void initView() {
        mLayoutSelectWatermark = (RelativeLayout) findViewById(R.id.layout_select_watermark);
        mLayoutShowFacePoints = (RelativeLayout) findViewById(R.id.layout_show_face_points);
        mTextFacePoints = (TextView) findViewById(R.id.tv_show_face_points);
        processShowFacePoints();

        mLayoutShowFps = (RelativeLayout) findViewById(R.id.layout_show_fps);
        mTextFps = (TextView) findViewById(R.id.tv_show_fps);
        processShowFps();

        mLayoutSelectWatermark.setOnClickListener(mClickListener);
        mLayoutShowFacePoints.setOnClickListener(mClickListener);
        mLayoutShowFps.setOnClickListener(mClickListener);
    }


    /**
     * 监听事件回调
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.layout_select_watermark) {
                processSelectWatermark();
            } else if (id == R.id.layout_show_face_points) {
                CameraParam.getInstance().drawFacePoints = !CameraParam.getInstance().drawFacePoints;
                processShowFacePoints();
            } else if (id == R.id.layout_show_fps) {
                CameraParam.getInstance().showFps = !CameraParam.getInstance().showFps;
                processShowFps();
            }
        }
    };

    private void processSelectWatermark() {
        Intent intent = new Intent(CameraSettingActivity.this, WatermarkActivity.class);
        startActivity(intent);
    }

    private void processShowFacePoints() {
        mTextFacePoints.setText(CameraParam.getInstance().drawFacePoints
                ? getString(R.string.show_face_points) : getString(R.string.hide_face_points));
    }

    private void processShowFps() {
        mTextFps.setText(CameraParam.getInstance().showFps
                ? getString(R.string.show_fps) : getString(R.string.hide_fps));
    }
}
