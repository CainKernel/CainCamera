package com.cgfay.caincamera.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.type.StateType;
import com.cgfay.caincamera.view.VideoTextureView;

public class VideoEditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VideoEditActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";

    private String mVideoPath;
    private VideoTextureView mTextureView;

    // 视频宽高
    private int mVideoWidth;
    private int mVideoHeight;
    // 视图实际宽高
    private int mViewWidth;
    private int mViewHeight;

    // 导航栏按钮
    private Button mBtnCancel;
    private Button mBtnDone;

    // 底部功能选择栏按钮
    private Button mBtnFilters;
    private Button mBtnEdit;
    private Button mBtnMusic;
    private Button mBtnVolume;

    private int mVideoDuration; // 视频时长
    private int mStartTime; // 开始位置
    private int mEndTime; // 结束位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_edit);
        mVideoPath = getIntent().getStringExtra(PATH);
        initView();
        initVideoPlayer();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnDone = (Button) findViewById(R.id.btn_done);
        mBtnFilters = (Button) findViewById(R.id.btn_filters);
        mBtnEdit = (Button) findViewById(R.id.btn_edit);
        mBtnMusic = (Button) findViewById(R.id.btn_music);
        mBtnVolume = (Button) findViewById(R.id.btn_volume);

        mBtnCancel.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
        mBtnFilters.setOnClickListener(this);
        mBtnEdit.setOnClickListener(this);
        mBtnMusic.setOnClickListener(this);
        mBtnVolume.setOnClickListener(this);

        mTextureView = (VideoTextureView) findViewById(R.id.video_view);

    }

    /**
     * 初始化播放器
     */
    private void initVideoPlayer() {
        mTextureView.setVideoPath(mVideoPath);
        mTextureView.setPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 设定时长
                mVideoDuration = mTextureView.getDuration();
                mStartTime = 0;
                mEndTime = mVideoDuration;
                // 开始播放
                mTextureView.setLooping(true);
                mTextureView.start();
            }
        });

        mTextureView.setPlayStateListener(new VideoTextureView.PlayStateListener() {
            @Override
            public void onStateChanged(StateType state) {
                if (state == StateType.PLAYING) {
                    mVideoWidth = mTextureView.getVideoWidth();
                    mVideoHeight = mTextureView.getVideoHeight();
                    mViewWidth = mTextureView.getWidth();
                    mViewHeight = mTextureView.getHeight();

                    float ratio = mVideoWidth * 1.0f / mVideoHeight;
                    double viewAspectRatio = (double) mViewWidth / mViewHeight;
                    // 如果视频的长宽比小于视图的长宽比，则要以视图高度为基准，用视频长宽比算实际视图宽度
                    // 如果视频长裤阿比大于视图的长宽比，则要以视图宽度为基准，用视频长宽比算计时视图高度
                    if (ratio < viewAspectRatio) {
                        mViewWidth = (int) (mViewHeight * ratio);
                    } else {
                        mViewHeight = (int) (mViewWidth / ratio);
                    }
                    ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
                    layoutParams.width = mViewWidth;
                    layoutParams.height = mViewHeight;
                    mTextureView.setLayoutParams(layoutParams);

                }
            }

            @Override
            public void onPlayChangedError(StateType state, String message) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 取消
            case R.id.btn_cancel:
                operationCancel();
                break;

            // 完成
            case R.id.btn_done:
                operationDone();
                break;

            // 滤镜
            case R.id.btn_filters:
                showFilters();
                break;

            // 编辑
            case R.id.btn_edit:
                showEditView();
                break;

            // 音乐选择
            case R.id.btn_music:
                showMusicSeletView();
                break;

            // 声音设置
            case R.id.btn_volume:
                showVolumeChangeView();
                break;

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mTextureView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureView.start();
    }

    /**
     * 取消
     */
    private void operationCancel() {
        finish();
    }

    /**
     * 编辑完成，进行处理
     */
    private void operationDone() {
        // TODO 将特效合成到视频中

        finish();
    }

    /**
     * 显示滤镜列表
     */
    private void showFilters() {

    }

    /**
     * 显示编辑页
     */
    private void showEditView() {

    }

    /**
     * 显示音乐选择页面
     */
    private void showMusicSeletView() {

    }

    /**
     * 显示声音调整页面
     */
    private void showVolumeChangeView() {

    }
}
