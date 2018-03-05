package com.cgfay.caincamera.activity.videoedit;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.adapter.EffectFilterAdapter;
import com.cgfay.caincamera.type.StateType;
import com.cgfay.caincamera.view.VideoTextureView;
import com.cgfay.cainfilter.core.ColorFilterManager;
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

    private LayoutInflater mInflater;

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

    private RelativeLayout mLayoutBottomMenu;
    // 底部功能选择栏按钮
    private Button mBtnFilters;
    private Button mBtnEdit;
    private Button mBtnMusic;
    private Button mBtnVolume;

    private FrameLayout mLayoutSubContent;

    // 编辑列表
    private HorizontalScrollView mEditButtonList;
    // 编辑栏的按钮
    private Button mBtnSpeed;
    private Button mBtnSubtitle;
    private Button mBtnCut;
    private Button mBtnGraffiti;
    private Button mBtnHipHop;

    // 滤镜列表
    private AsyncRecyclerview mFilterListView;
    private LinearLayoutManager mFilterListManager;

    private int mColorIndex = 0;

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
        mInflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
        initVideoPlayer();
    }

    /**
     * 初始化视图
     */
    private void initView() {

        mEditTitle = (TextView) findViewById(R.id.edit_title);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);


        // 播放指示按钮
        mBtnPlayIndicator = (Button) findViewById(R.id.btn_play_indicator);
        mBtnPlayIndicator.setOnClickListener(this);

        // 编辑栏按钮
        mLayoutBottomMenu = (RelativeLayout) findViewById(R.id.layout_bottom_menu);
        mBtnFilters = (Button) mLayoutBottomMenu.findViewById(R.id.btn_filters);
        mBtnEdit = (Button) mLayoutBottomMenu.findViewById(R.id.btn_edit);
        mBtnMusic = (Button) mLayoutBottomMenu.findViewById(R.id.btn_music);
        mBtnVolume = (Button) mLayoutBottomMenu.findViewById(R.id.btn_volume);

        mBtnFilters.setOnClickListener(this);
        mBtnEdit.setOnClickListener(this);
        mBtnMusic.setOnClickListener(this);
        mBtnVolume.setOnClickListener(this);

        // 子内容栏
        mLayoutSubContent = (FrameLayout) findViewById(R.id.layout_sub_content);
        // 编辑按钮列表
        mEditButtonList = (HorizontalScrollView) mInflater
                .inflate(R.layout.view_bottom_edit, null);
        mBtnSpeed = (Button) mEditButtonList.findViewById(R.id.btn_speed);
        mBtnSubtitle = (Button) mEditButtonList.findViewById(R.id.btn_subtitle);
        mBtnCut = (Button) mEditButtonList.findViewById(R.id.btn_cut);
        mBtnGraffiti = (Button) mEditButtonList.findViewById(R.id.btn_grafitti);
        mBtnHipHop = (Button) mEditButtonList.findViewById(R.id.btn_hip_hop);

        mBtnSpeed.setOnClickListener(this);
        mBtnSubtitle.setOnClickListener(this);
        mBtnCut.setOnClickListener(this);
        mBtnGraffiti.setOnClickListener(this);
        mBtnHipHop.setOnClickListener(this);

        // 滤镜列表
        mFilterListView = (AsyncRecyclerview) mInflater
                .inflate(R.layout.view_video_edit_filters, null);
        mFilterListManager = new LinearLayoutManager(this);
        mFilterListManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(mFilterListManager);
        // TODO 滤镜适配器
        EffectFilterAdapter adapter = new EffectFilterAdapter(this,
                ColorFilterManager.getInstance().getFilterType(),
                ColorFilterManager.getInstance().getFilterName());

        mFilterListView.setAdapter(adapter);
        adapter.addItemClickListener(new EffectFilterAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(int position) {
                mColorIndex = position;
                if (VERBOSE) {
                    Log.d("changeFilter", "index = " + mColorIndex + ", filter name = "
                            + ColorFilterManager.getInstance().getColorFilterName(mColorIndex));
                }
            }
        });

        showFilters();


        // 底部确认按钮栏
        mLayoutBottomConfirm = (LinearLayout) mInflater
                .inflate(R.layout.view_video_edit_bottom_confirm, null);
        mBtnCancel = (Button) mLayoutBottomConfirm.findViewById(R.id.btn_cancel);
        mBtnConfirm = (Button) mLayoutBottomConfirm.findViewById(R.id.btn_confirm);

        mBtnCancel.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
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
            case R.id.btn_grafitti:
                showGraffittiView();
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
        mLayoutSubContent.removeAllViews();
        mLayoutSubContent.addView(mFilterListView);
    }

    /**
     * 显示编辑页
     */
    private void showEditView() {
        mBtnFilters.setTextColor(getResources().getColor(R.color.white));
        mBtnEdit.setTextColor(getResources().getColor(R.color.red));
        mLayoutSubContent.removeAllViews();
        mLayoutSubContent.addView(mEditButtonList);
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
//        mBtnBack.setVisibility(View.GONE);
//        mBtnNext.setVisibility(View.GONE);
//        mEditTitle.setText(getResources().getText(R.string.edit_title_volume));
//        mLayoutBottomConfirm.setVisibility(View.VISIBLE);
    }

    /**
     * 显示音频加速页面
     */
    private void showSpeedChangeView() {
//        mBtnBack.setVisibility(View.GONE);
//        mBtnNext.setVisibility(View.GONE);
//        mEditTitle.setText(getResources().getText(R.string.edit_title_speed));
//        mLayoutBottomConfirm.setVisibility(View.VISIBLE);
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
    private void showGraffittiView() {

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
    }

    /**
     * 确定操作
     */
    private void operationConfirm() {
        mBtnBack.setVisibility(View.VISIBLE);
        mBtnNext.setVisibility(View.VISIBLE);
        mEditTitle.setText(getResources().getText(R.string.edit_title));
        mLayoutBottomConfirm.setVisibility(View.GONE);
    }
}
