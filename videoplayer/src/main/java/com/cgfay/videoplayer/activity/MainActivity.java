package com.cgfay.videoplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cgfay.utilslibrary.PermissionUtils;
import com.cgfay.videoplayer.R;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0;
    private Button mBtnMadiaCodec;
    private Button mBtnIJKPlayer;
    private Button mBtnFFmpeg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        mBtnMadiaCodec = (Button) findViewById(R.id.btn_mediaCodec);
        mBtnMadiaCodec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaViewAvtivity(0);
            }
        });

        mBtnIJKPlayer = (Button) findViewById(R.id.btn_ijkplayer);
        mBtnIJKPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaViewAvtivity(1);
            }
        });

        mBtnFFmpeg = (Button) findViewById(R.id.btn_ffmpeg);
        mBtnFFmpeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMediaViewAvtivity(2);
            }
        });
    }

    /**
     * 检查权限
     */
    private void checkPermissions() {
        boolean storageWriteEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!storageWriteEnable) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CODE);
        }
    }

    /**
     * 跳转至媒体库找数据
     * @param usingType 0表示使用MediaCodec，1表示使用ijkplayer，2表示使用ffmpeg
     */
    private void startMediaViewAvtivity(int usingType) {
        Intent intent = new Intent(MainActivity.this, MediaScanActivity.class);
        intent.putExtra(MediaScanActivity.USE_FFMPEG, usingType);
        startActivity(intent);
        finish();
    }
}
