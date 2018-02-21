package com.cgfay.videoplayer.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cgfay.videoplayer.R;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IJKPlayerActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnBufferingUpdateListener  {

    private static final String TAG = "IJKPlayerActivity";

    public static final String PATH = "path";
    public static final String ORIENTATION = "orientation";

    private String mVideoPath;
    private int mOrientation;

    private boolean visible = false;
    private LinearLayout mLayoutOperation;
    private Button mBtnPause;
    private Button mBtnPlay;

    private SurfaceView mSurfaceView;

    IjkMediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijkplayer);

        mVideoPath = getIntent().getStringExtra(PATH);
        mOrientation = getIntent().getIntExtra(ORIENTATION, 0);

        initButton();
        initMediaPlayer();
        initSurfaceView();
    }

    private void initButton() {
        mLayoutOperation = (LinearLayout) findViewById(R.id.layout_operation);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnPause.setOnClickListener(this);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);
    }

    private void initMediaPlayer() {
        mediaPlayer = new IjkMediaPlayer();

        try {
            mediaPlayer.setDataSource(mVideoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 使用MediaCodec
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        // 准备监听
        mediaPlayer.setOnPreparedListener(this);
        // 播放完成监听
        mediaPlayer.setOnCompletionListener(this);
        //当前加载进度的监听
        mediaPlayer.setOnBufferingUpdateListener(this);
        // 允许设置变声变速
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
        // 设置播放速度
        mediaPlayer.setSpeed(1.0f);
    }

    private void initSurfaceView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==(MotionEvent.ACTION_DOWN))
                {
                    if (visible)
                    {
                        mLayoutOperation.setVisibility(View.VISIBLE);
                    } else {
                        mLayoutOperation.setVisibility(View.GONE);
                    }
                    visible = !visible;
                }

                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_pause:
                mediaPlayer.pause();
                break;

            case R.id.btn_play:
                mediaPlayer.start();
                break;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //连接ijkPlayer 和surfaceHOLDER
        mediaPlayer.setDisplay(holder);
        //开启异步准备
        mediaPlayer.prepareAsync();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        iMediaPlayer.seekTo(0);
        iMediaPlayer.start();
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        iMediaPlayer.start();
    }
}
