package com.cgfay.videoplayer.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.cgfay.utilslibrary.AspectFrameLayout;
import com.cgfay.utilslibrary.CainSurfaceView;
import com.cgfay.videoplayer.R;
import com.cgfay.videoplayer.multimedia.IPlayStateListener;
import com.cgfay.videoplayer.multimedia.SimplePlayer;

public class MediaPlayerActivity extends AppCompatActivity
        implements SurfaceHolder.Callback, IPlayStateListener {

    private static final String TAG = "MediaPlayerActivity";

    public static final String PATH = "path";
    public static final String ORIENTATION = "orientation";

    private String mVideoPath;
    private int mOrientation;

    private CainSurfaceView mVideoSurfaceView;

    private AspectFrameLayout mLayoutPlayer;
    private SimplePlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        mLayoutPlayer = (AspectFrameLayout) findViewById(R.id.layout_aspect);
        mVideoSurfaceView = (CainSurfaceView) findViewById(R.id.cv_player);
        mVideoSurfaceView.getHolder().addCallback(this);
        mVideoPath = getIntent().getStringExtra(PATH);
        mOrientation = getIntent().getIntExtra(ORIENTATION, 0);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mVideoPath != null) {
            mPlayer = new SimplePlayer(holder.getSurface(), mVideoPath);
            mPlayer.setPlayStateListener(MediaPlayerActivity.this);
            mPlayer.play();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mPlayer != null) {
            mPlayer.destroy();
        }
    }

    @Override
    public void videoAspect(final int width, final int height, float time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 判断旋转角度，有些视频拍出来的orientation是90/270度的
                if (mOrientation % 180 == 0) {
                    mLayoutPlayer.setAspectRatio((float) width / height);
                } else {
                    mLayoutPlayer.setAspectRatio((float) height / width);
                }
            }
        });
    }
}
