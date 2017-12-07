package com.cgfay.caincamera.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.core.ParamsManager;
import com.cgfay.caincamera.multimedia.MediaPlayerManager;
import com.cgfay.caincamera.type.GalleryType;
import com.cgfay.caincamera.utils.FileUtils;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.io.File;
import java.util.ArrayList;

public class CapturePreviewActivity extends AppCompatActivity
        implements View.OnClickListener, ExoPlayer.EventListener {
    private static final String TAG = "CapturePreviewActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";

    // 路径
    private ArrayList<String> mPath;

    // layout
    private FrameLayout mPreviewLayout;
    // 预览图片
    private ImageView mImageView;
    // 预览视频或者GIF视频
    private SurfaceView mSurfaceView;
    // 取消
    private Button mBtnCancel;
    // 保存
    private Button mBtnSave;
    // 分享
    private Button mBtnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capture_preview);
        mPath = getIntent().getStringArrayListExtra(PATH);
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mPreviewLayout = (FrameLayout) findViewById(R.id.layout_preview);

        if (ParamsManager.mGalleryType == GalleryType.PICTURE) {
            mImageView = new ImageView(this);
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mPreviewLayout.addView(mImageView);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            mImageView.setLayoutParams(params);

            if (mPath != null && mPath.size() > 0) {
                Bitmap bitmap = BitmapFactory.decodeFile(mPath.get(0));
                mImageView.setImageBitmap(bitmap);
            }

        } else if (ParamsManager.mGalleryType == GalleryType.VIDEO
                || ParamsManager.mGalleryType == GalleryType.GIF) {
            mSurfaceView = new SurfaceView(this);
            mPreviewLayout.addView(mSurfaceView);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            mSurfaceView.setLayoutParams(params);
            initMediaPlayer();
        }

        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mBtnShare = (Button) findViewById(R.id.btn_share);

        mBtnCancel.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnShare.setOnClickListener(this);
    }

    /**
     * 初始化视频播放器
     */
    private void initMediaPlayer() {
        MediaPlayerManager.getInstance().createMediaPlayer(this);
        MediaPlayerManager.getInstance().setPlayerSurface(mSurfaceView);
        MediaPlayerManager.getInstance().setPlayerListener(this);
        MediaPlayerManager.getInstance().preparePlayer(this, mPath);
        MediaPlayerManager.getInstance().setRepeatMode(Player.REPEAT_MODE_ALL);
        MediaPlayerManager.getInstance().start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                executeDeleteFile();
                break;

            case R.id.btn_save:
                executeSave();
                break;

            case R.id.btn_share:
                executeShare();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        executeDeleteFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaPlayerManager.getInstance().continuePlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaPlayerManager.getInstance().pause();
    }

    @Override
    protected void onDestroy() {
        MediaPlayerManager.getInstance().stop();
        MediaPlayerManager.getInstance().release();
        super.onDestroy();
    }

    /**
     * 执行取消动作
     */
    private void executeDeleteFile() {
        // 删除文件
        if (mPath != null) {
            for (int i = 0; i < mPath.size(); i++) {
                if (!TextUtils.isEmpty(mPath.get(i))) {
                    FileUtils.deleteFile(mPath.get(i));
                }
            }
        }
        // 关掉页面
        finish();
    }


    /**
     * 执行保存操作
     */
    private void executeSave() {
        if (mPath == null || mPath.size() <= 0) {
            finish();
            return;
        }

        // 如果是图片，则直接保存
        if (ParamsManager.mGalleryType == GalleryType.PICTURE) {
            for (int i = 0; i < mPath.size(); i++) {
                File file = new File(mPath.get(i));
                String newPath = ParamsManager.AlbumPath + file.getName();
                FileUtils.copyFile(mPath.get(i), newPath);
            }
        } else if (ParamsManager.mGalleryType == GalleryType.VIDEO) { // TODO 如果是视频，则合成视频

        } else if (ParamsManager.mGalleryType == GalleryType.GIF) { // TODO 如果是GIF，则合成GIF

        }
        // 删除旧文件
        executeDeleteFile();
        finish();
    }


    /**
     * 执行分享操作
     */
    private void executeShare() {

    }



    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        if (VERBOSE) {
            Log.d(TAG,"onTimelineChanged - " + timeline.toString());
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if (VERBOSE) {
            Log.d(TAG,"TrackGroupArray - trackGroups: " + trackGroups + ", trackSelections: " + trackSelections);
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (VERBOSE) {
            Log.d(TAG,"onLoadingChanged - isLoading ? " + isLoading);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case PlaybackState.STATE_PLAYING:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_PLAYING");
                }
                break;

            case PlaybackState.STATE_BUFFERING:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_BUFFERING");
                }
                break;

            case PlaybackState.STATE_CONNECTING:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_CONNECTING");
                }
                break;

            case PlaybackState.STATE_ERROR:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_ERROR");
                }
                break;

            case PlaybackState.STATE_FAST_FORWARDING:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_FAST_FORWARDING");
                }
                MediaPlayerManager.getInstance().pause();
                break;

            case PlaybackState.STATE_NONE:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_NONE");
                }
                break;

            case PlaybackState.STATE_PAUSED:
                Log.d(TAG,"onPlayerStateChanged - STATE_PAUSED");
                break;

            case PlaybackState.STATE_REWINDING:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_REWINDING");
                }
                break;

            case PlaybackState.STATE_SKIPPING_TO_NEXT:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_SKIPPING_TO_NEXT");
                }
                break;

            case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_SKIPPING_TO_PREVIOUS");
                }
                break;

            case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_SKIPPING_TO_QUEUE_ITEM");
                }
                break;

            case PlaybackState.STATE_STOPPED:
                if (VERBOSE) {
                    Log.d(TAG,"onPlayerStateChanged - STATE_STOPPED");
                }
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        if (VERBOSE) {
            Log.d(TAG,"onRepeatModeChanged");
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (VERBOSE) {
            Log.d(TAG,"onPlayerError  ");
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        if (VERBOSE) {
            Log.d(TAG,"onPositionDiscontinuity  ");
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
