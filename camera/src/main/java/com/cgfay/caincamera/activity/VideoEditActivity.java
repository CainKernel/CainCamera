package com.cgfay.caincamera.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.type.StateType;
import com.cgfay.caincamera.view.VideoTextureView;
import com.cgfay.utilslibrary.AsyncRecyclerview;
import com.cgfay.utilslibrary.StringUtils;

public class VideoEditActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "VideoEditActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";

    private String mVideoPath;
    private VideoTextureView mTextureView;
    private SeekBar mPlayProgressBar;
    private TextView mCurrentPositionView;
    private TextView mDurationView;

    // 更新进度条线程停止标志
    private volatile boolean mStopThread;
    private volatile boolean mPauseThread;

    // 视频宽高
    private int mVideoWidth;
    private int mVideoHeight;
    // 视图实际宽高
    private int mViewWidth;
    private int mViewHeight;

    // 导航栏按钮
    private Button mBtnBack;
    private Button mBtnNext;
    private TextView mEditTitle;

    // 播放按钮
    private Button mBtnPlayIndicator;

    // 底部功能选择栏按钮
    private Button mBtnFilters;
    private Button mBtnEdit;
    private Button mBtnMusic;
    private Button mBtnVolume;

    // 编辑列表
    private HorizontalScrollView mEditList;
    // 编辑栏的按钮
    private Button mBtnSpeed;
    private Button mBtnSubtitle;
    private Button mBtnCut;
    private Button mBtnGraffiti;
    private Button mBtnHipHop;

    // 滤镜列表
    private AsyncRecyclerview mFilterList;
    private LinearLayoutManager mFilterListManager;


    // 调整播放速度布局
    private FrameLayout mLayoutSpeed;

    // 底部确认按钮布局
    private LinearLayout mLayoutBottomConfirm;
    // 底部确认按钮
    private Button mBtnCancel;
    private Button mBtnConfirm;

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
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mEditTitle = (TextView) findViewById(R.id.edit_title);

        mBtnFilters = (Button) findViewById(R.id.btn_filters);
        mBtnFilters.setTextColor(getResources().getColor(R.color.red));
        mBtnEdit = (Button) findViewById(R.id.btn_edit);
        mBtnMusic = (Button) findViewById(R.id.btn_music);
        mBtnVolume = (Button) findViewById(R.id.btn_volume);
        mBtnPlayIndicator = (Button) findViewById(R.id.btn_play_indicator);

        mEditList = (HorizontalScrollView) findViewById(R.id.edit_list);
        mEditList.setVisibility(View.GONE);
        mBtnSpeed = (Button) findViewById(R.id.btn_speed);
        mBtnSubtitle = (Button) findViewById(R.id.btn_subtitle);
        mBtnCut = (Button) findViewById(R.id.btn_cut);
        mBtnGraffiti = (Button) findViewById(R.id.btn_graffiti);
        mBtnHipHop = (Button) findViewById(R.id.btn_hip_hop);

        mLayoutBottomConfirm = (LinearLayout) findViewById(R.id.layout_bottom_confirm);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);

        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnFilters.setOnClickListener(this);
        mBtnEdit.setOnClickListener(this);
        mBtnMusic.setOnClickListener(this);
        mBtnVolume.setOnClickListener(this);
        mBtnPlayIndicator.setOnClickListener(this);

        mBtnSpeed.setOnClickListener(this);
        mBtnSubtitle.setOnClickListener(this);
        mBtnCut.setOnClickListener(this);
        mBtnGraffiti.setOnClickListener(this);
        mBtnHipHop.setOnClickListener(this);

        mLayoutSpeed = (FrameLayout) findViewById(R.id.layout_speed);
        mBtnCancel.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);

        // 滤镜列表
        mFilterList = (AsyncRecyclerview) findViewById(R.id.filter_list);
        mFilterListManager = new LinearLayoutManager(this);
        mFilterListManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterList.setLayoutManager(mFilterListManager);
        // TODO 滤镜适配器

    }

    /**
     * 初始化播放器
     */
    private void initVideoPlayer() {
        // 进度条
        mPlayProgressBar = (SeekBar) findViewById(R.id.play_progress);
        mPlayProgressBar.setOnSeekBarChangeListener(this);
        mCurrentPositionView = (TextView) findViewById(R.id.tv_current);
        mDurationView = (TextView) findViewById(R.id.tv_duration);

        mTextureView = (VideoTextureView) findViewById(R.id.video_view);
        mTextureView.setVideoPath(mVideoPath);
        mTextureView.setPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 开始播放
                mTextureView.setLooping(true);
                mTextureView.start();
                // 设置时长
                mPlayProgressBar.setMax(mp.getDuration());
                mDurationView.setText(StringUtils.generateStandardTime(mp.getDuration()));
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
                    mBtnPlayIndicator.setVisibility(View.GONE);
                } else if (state == StateType.STOP
                        || state == StateType.PAUSED) {
                    mBtnPlayIndicator.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPlayChangedError(StateType state, String message) {

            }
        });

        mTextureView.setCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mBtnPlayIndicator.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mTextureView.seekTo(progress);
            mPlayProgressBar.setProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 取消
            case R.id.btn_back:
                operationBack();
                break;

            // 完成
            case R.id.btn_next:
                operationNext();
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

            // 播放视频
            case R.id.btn_play_indicator:
                playVideo();
                break;

            // 加速
            case R.id.btn_speed:
                showSpeedChangeView();
                break;

            // 字母
            case R.id.btn_subtitle:
                showSubtitleView();
                break;

            // 剪辑
            case R.id.btn_cut:
                showCutView();
                break;

            // 魔法涂鸦
            case R.id.btn_graffiti:
                showGraffitiView();
                break;

            // 嘻哈特效
            case R.id.btn_hip_hop:
                showHiphopView();
                break;

            // 取消
            case R.id.btn_cancel:
                operationCancel();
                break;

            // 确定
            case R.id.btn_confirm:
                operationConfirm();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureView.pause();
        mStopThread = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureView.start();
        mStopThread = true;
        mPauseThread = false;
        // 开始新的刷新线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mStopThread) {
                    if (mPauseThread) {
                        continue;
                    }
                    try {
                        if (mTextureView != null) {
                            int currentPosition = mTextureView.getCurrentPosition();
                            Message msg = new Message();
                            msg.what = currentPosition;
                            mRefreshHandler.sendMessage(msg);
                        }
                        Thread.sleep(100);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // 更新进度条回调
    private Handler mRefreshHandler = new Handler() {
        public void handleMessage(Message msg){
            mPlayProgressBar.setProgress(msg.what);
            mCurrentPositionView.setText(StringUtils.generateStandardTime(msg.what));
        }
    };


    /**
     * 取消
     */
    private void operationBack() {
        finish();
    }

    /**
     * 编辑完成，进行处理
     */
    private void operationNext() {
        // TODO 将特效合成到视频中

        finish();
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        if (mTextureView != null) {
            mTextureView.start();
        }
    }

    /**
     * 显示滤镜列表
     */
    private void showFilters() {
        mBtnFilters.setTextColor(getResources().getColor(R.color.red));
        mBtnEdit.setTextColor(getResources().getColor(R.color.white));
        mFilterList.setVisibility(View.VISIBLE);
        mEditList.setVisibility(View.GONE);
    }

    /**
     * 显示编辑页
     */
    private void showEditView() {
        mBtnFilters.setTextColor(getResources().getColor(R.color.white));
        mBtnEdit.setTextColor(getResources().getColor(R.color.red));
        mFilterList.setVisibility(View.GONE);
        mEditList.setVisibility(View.VISIBLE);
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
        mBtnBack.setVisibility(View.GONE);
        mBtnNext.setVisibility(View.GONE);
        mEditTitle.setText(getResources().getText(R.string.edit_title_volume));
        mLayoutBottomConfirm.setVisibility(View.VISIBLE);
        mLayoutSpeed.setVisibility(View.VISIBLE);
    }

    /**
     * 显示音频加速页面
     */
    private void showSpeedChangeView() {
        mBtnBack.setVisibility(View.GONE);
        mBtnNext.setVisibility(View.GONE);
        mEditTitle.setText(getResources().getText(R.string.edit_title_speed));
        mLayoutBottomConfirm.setVisibility(View.VISIBLE);
        mLayoutSpeed.setVisibility(View.VISIBLE);
    }

    /**
     * 显示字幕页面
     */
    private void showSubtitleView() {

    }

    /**
     * 显示剪辑页面
     */
    private void showCutView() {

    }

    /**
     * 显示魔法涂鸦页面
     */
    private void showGraffitiView() {

    }

    /**
     * 显示嘻哈特效页面
     */
    private void showHiphopView() {

    }

    /**
     * 取消操作
     */
    private void operationCancel() {
        mBtnBack.setVisibility(View.VISIBLE);
        mBtnNext.setVisibility(View.VISIBLE);
        mEditTitle.setText(getResources().getText(R.string.edit_title));
        mLayoutBottomConfirm.setVisibility(View.GONE);
        mLayoutSpeed.setVisibility(View.GONE);
    }

    /**
     * 确定操作
     */
    private void operationConfirm() {
        mBtnBack.setVisibility(View.VISIBLE);
        mBtnNext.setVisibility(View.VISIBLE);
        mEditTitle.setText(getResources().getText(R.string.edit_title));
        mLayoutBottomConfirm.setVisibility(View.GONE);
        mLayoutSpeed.setVisibility(View.GONE);
    }
}
