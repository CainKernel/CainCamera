package com.cgfay.videoplayer.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cgfay.utilslibrary.CainSurfaceView;
import com.cgfay.videoplayer.R;

public class FFPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static final String PATH = "path";

    private String mVideoPath;

    private CainSurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffplayer);
        mSurfaceView = (CainSurfaceView) findViewById(R.id.cv_player);
        mSurfaceView.getHolder().addCallback(this);
        mVideoPath = getIntent().getStringExtra(PATH);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
