package com.cgfay.videolibrary.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.advanced.GLImageOESInputFilter;
import com.cgfay.filterlibrary.glfilter.advanced.beauty.GLImageBeautyFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.videolibrary.bean.Music;
import com.cgfay.utilslibrary.fragment.BackPressedDialogFragment;
import com.cgfay.utilslibrary.fragment.PermissionConfirmDialogFragment;
import com.cgfay.utilslibrary.fragment.PermissionErrorDialogFragment;
import com.cgfay.utilslibrary.utils.AudioPlayer;
import com.cgfay.utilslibrary.utils.CainMediaPlayer;
import com.cgfay.utilslibrary.utils.PermissionUtils;
import com.cgfay.utilslibrary.utils.StateType;
import com.cgfay.utilslibrary.utils.StringUtils;
import com.cgfay.videolibrary.R;
import com.cgfay.videolibrary.bean.VideoEffect;
import com.cgfay.videolibrary.widget.IndicatorProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * 视频编辑页面
 */
public class VideoEditFragment extends Fragment
        implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "VideoEditFragment";
    private static final boolean VERBOSE = true;

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int MSG_REFRESH_POSITION = 0x100;

    private static final int FRAGMENT_CUT = 0;
    private static final int FRAGMENT_MUSIC = 1;
    private static final int FRAGMENT_FILTER = 2;
    private static final int FRAGMENT_EFFECT = 3;
    private static final int FRAGMENT_FONT = 4;

    private boolean mStorageWriteEnable = false;
    // 视频总时长
    private int mDuration;
    private int mCurrentPosition;
    // 视频宽高
    private int mVideoWidth;
    private int mVideoHeight;
    // 视图实际宽高
    private int mViewWidth;
    private int mViewHeight;
    // 输入滤镜
    private GLImageOESInputFilter mInputFilter;
    // 显示输出滤镜
    private GLImageFilter mDisplayFilter;
    // 滤镜组
    private List<GLImageFilter> mColorFilters = new ArrayList<>();
    // 视频特效数据
    private List<VideoEffect> mVideoEffectLists = new ArrayList<>();
    // 编辑特效数据
    private List<VideoEffect> mEffectEditLists = new ArrayList<>();
    // 暂存特效数据（回删撤销操作）
    private Stack<VideoEffect> mEffectTempLists = new Stack<>();
    // 是否处于编辑状态（预览视频还是给视频添加滤镜）
    private volatile boolean mVideoEdit;
    // 输入文理
    private int mInputTexture;
    private int mCurrentTexture;
    private SurfaceTexture mSurfaceTexture;
    private float[] mMatrix = new float[16];
    // 视频播放器
    private CainMediaPlayer mVideoPlayer;
    private String mVideoPath;
    // 配乐播放器
    private AudioPlayer mAudioPlayer;
    private Music mMusic;
    // 系统音量
    private int mSystemVolume;

    // fragment主页面
    private View mContentView;
    // 画面预览
    private GLSurfaceView mGLSurfaceView;
    // 进度条
    private RelativeLayout mLayoutProgress;
    private SeekBar mPlayProgressBar;
    private TextView mCurrentPositionView;
    private TextView mDurationView;
    // 提示
    private Toast mToast;
    // 分类按钮
    private Button mBtnCut;     // 裁剪
    private Button mBtnMusic;   // 音乐
    private Button mBtnFilters; // 滤镜
    private Button mBtnEffect;  // 特效
    private Button mBtnFont;    // 字幕
    // 导航栏按钮
    private Button mBtnBack;
    private Button mBtnNext;
    // 播放按钮
    private Button mBtnPlayIndicator;

    // 编辑栏
    private VideoCutFragment mVideoCutFragment;
    private VideoMusicFragment mVideoMusicFragment;
    private VideoFilterFragment mVideoFilterFragment;
    private VideoEffectFragment mVideoEffectFragment;
    private VideoSubtitleFragment mVideoSubtitleFragment;
    private int mCurrentFragment = FRAGMENT_CUT;

    private Activity mActivity;
    // 选择音乐跳转监听器
    private OnPageOperationListener mStartMusicSelectListener;

    public static VideoEditFragment newInstance() {
        return new VideoEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (VERBOSE) {
            Log.d(TAG, "onAttach: ");
        }
        mActivity = getActivity();
        mStorageWriteEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERBOSE) {
            Log.d(TAG, "onCreate: ");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (VERBOSE) {
            Log.d(TAG, "onCreateView: ");
        }
        mContentView =  inflater.inflate(R.layout.fragment_video_edited, container, false);
        return mContentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (VERBOSE) {
            Log.d(TAG, "onViewCreated: ");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (VERBOSE) {
            Log.d(TAG, "onActivityCreated: ");
        }
        if (mStorageWriteEnable) {
            initView(mContentView);
        } else {
            requestStoragePermission();
        }
    }

    /**
     * 初始化页面
     * @param view
     */
    private void initView(View view) {
        mBtnBack = (Button) view.findViewById(R.id.btn_back);
        mBtnNext = (Button) view.findViewById(R.id.btn_next);
        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);

        // 进度条
        mLayoutProgress = (RelativeLayout) view.findViewById(R.id.layout_progress);
        mPlayProgressBar = (SeekBar) view.findViewById(R.id.play_progress);
        mPlayProgressBar.setOnSeekBarChangeListener(this);
        mCurrentPositionView = (TextView) view.findViewById(R.id.tv_current);
        mDurationView = (TextView) view.findViewById(R.id.tv_duration);

        mBtnPlayIndicator = (Button) view.findViewById(R.id.btn_play_indicator);
        mBtnPlayIndicator.setOnClickListener(this);

        mGLSurfaceView = (GLSurfaceView) view.findViewById(R.id.mediaView);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPlayer != null) {
                    mVideoPlayer.pause();
                    if (mAudioPlayer != null) {
                        mAudioPlayer.pause();
                    }
                } else {
                    playVideo();
                }
            }
        });

        mBtnCut = (Button) view.findViewById(R.id.btn_cut);
        mBtnMusic = (Button) view.findViewById(R.id.btn_music);
        mBtnFilters = (Button) view.findViewById(R.id.btn_filters);
        mBtnEffect = (Button) view.findViewById(R.id.btn_effect);
        mBtnFont = (Button) view.findViewById(R.id.btn_font);

        mBtnCut.setOnClickListener(this);
        mBtnMusic.setOnClickListener(this);
        mBtnFilters.setOnClickListener(this);
        mBtnEffect.setOnClickListener(this);
        mBtnFont.setOnClickListener(this);

        showFragment(mCurrentFragment);
        initVideoPlayer();
        initAudioPlayer();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (VERBOSE) {
            Log.d(TAG, "onStart: ");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (VERBOSE) {
            Log.d(TAG, "onResume: ");
        }
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onResume();
        }
        if (mRefreshHandler != null) {
            mRefreshHandler.sendEmptyMessageDelayed(MSG_REFRESH_POSITION, 500);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (VERBOSE) {
            Log.d(TAG, "onPause: ");
        }
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onPause();
        }
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }

        if (mAudioPlayer != null) {
            mAudioPlayer.pause();
        }
        if (mRefreshHandler != null) {
            mRefreshHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (VERBOSE) {
            Log.d(TAG, "onStop: ");
        }
    }

    @Override
    public void onDestroy() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
        if (mRefreshHandler != null) {
            mRefreshHandler.removeCallbacksAndMessages(null);
            mRefreshHandler = null;
        }
        mStartMusicSelectListener = null;
        super.onDestroy();
        if (VERBOSE) {
            Log.d(TAG, "onDestroy: ");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (VERBOSE) {
            Log.d(TAG, "onDestroyView: ");
        }
        mContentView = null;
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
        if (VERBOSE) {
            Log.d(TAG, "onDetach: ");
        }
    }

    public void onBackPressed() {
        new BackPressedDialogFragment().show(getChildFragmentManager(), "");
    }

    @SuppressLint("HandlerLeak")
    private Handler mRefreshHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_POSITION:
                    if (mGLSurfaceView != null && mRefreshHandler != null) {
                        if (mVideoPlayer.isPlaying()) {
                            mCurrentPosition = mVideoPlayer.getCurrentPosition();;
                        }
                        if (mPlayProgressBar != null) {
                            mPlayProgressBar.setProgress(mCurrentPosition);
                        }
                        if (mVideoEffectFragment != null && mVideoEffectFragment.getIndicatorProgress() != null) {
                            mVideoEffectFragment.getIndicatorProgress().setCurrentPercent((float) mCurrentPosition / mDuration);
                        }
                        if (mCurrentPositionView != null) {
                            mCurrentPositionView.setText(StringUtils.generateStandardTime(mCurrentPosition));
                        }
                        mRefreshHandler.sendEmptyMessageDelayed(MSG_REFRESH_POSITION, 500);
                    }
                    break;
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mVideoPlayer.seekTo(progress);
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
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLSurfaceView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        initFilters();
        mInputTexture = OpenGLUtils.createOESTexture();
        mSurfaceTexture = new SurfaceTexture(mInputTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        Surface surface = new Surface(mSurfaceTexture);
        mVideoPlayer.openVideo(mVideoPath, surface);
        surface.release();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            onInputSizeChanged(width, height);
        } else {
            onInputSizeChanged(mVideoWidth, mVideoHeight);
        }
        onDisplayChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (mSurfaceTexture == null || mInputFilter == null || mDisplayFilter == null) {
            return;
        }
        synchronized (this) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mMatrix);
        }
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        mInputFilter.setTextureTransformMatirx(mMatrix);
        mCurrentTexture = mInputTexture;
        mCurrentTexture = mInputFilter.drawFrameBuffer(mCurrentTexture);
        // 滤镜
        for (int i = 0; i < mColorFilters.size(); i++) {
            if (mColorFilters.get(i) != null) {
                mColorFilters.get(i).setTimerValue(mCurrentPosition);
                mCurrentTexture = mColorFilters.get(i).drawFrameBuffer(mCurrentTexture);
            }
        }

        if (mVideoEdit) { // 视频编辑状态
            if (mEffectEditLists.size() > 0) {
                for (int i = 0; i < mEffectEditLists.size(); i++) {
                    if (mEffectEditLists.get(i) != null && mEffectEditLists.get(i).getFilter() != null) {
                        mEffectEditLists.get(i).getFilter().setTimerValue(mCurrentPosition);
                        mCurrentTexture = mEffectEditLists.get(i).getFilter().drawFrameBuffer(mCurrentTexture);
                    }
                }
            }
        } else { // 预览状态
            // 从后面往前面数，只绘制叠加在最上层的滤镜，绘制完立即终止该帧的渲染
            for (int i = mVideoEffectLists.size() - 1; i >= 0; i--) {
                if (mVideoEffectLists.get(i) != null && mVideoEffectLists.get(i).getFilter() != null) {
                    if (mVideoEffectLists.get(i).getStartPosition() < mCurrentPosition
                            && mVideoEffectLists.get(i).getFinishPosition() > mCurrentPosition) {
                        mVideoEffectLists.get(i).getFilter().setTimerValue(mCurrentPosition);
                        mCurrentTexture = mVideoEffectLists.get(i).getFilter().drawFrameBuffer(mCurrentTexture);
                        break;
                    }
                }
            }
        }
        // 绘制显示
        mDisplayFilter.drawFrame(mCurrentTexture);
    }

    /**
     * 设置视频路径
     * @param videoPath
     */
    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
    }

    /**
     * 初始化视频播放器
     */
    private void initVideoPlayer() {
        mVideoPlayer = new CainMediaPlayer(getContext());
        mVideoPlayer.setVideoPath(mVideoPath);
        mVideoPlayer.setPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 设置循环播放
                mVideoPlayer.setLooping(true);
                // 第一次准备时，定位到开头
                mVideoPlayer.seekTo(0);
                mDuration = mp.getDuration();
                // 设置时长
                if (mPlayProgressBar != null) {
                    mPlayProgressBar.setMax(mDuration);
                }
                if (mDurationView != null) {
                    mDurationView.setText(StringUtils.generateStandardTime(mp.getDuration()));
                }
                // 计算视频显示宽高宽高
                calculateVideoViewSize();
            }
        });

        mVideoPlayer.setPlayStateListener(new CainMediaPlayer.PlayStateListener() {
            @Override
            public void onStateChanged(StateType state) {
                if (state == StateType.PLAYING) {
                    if (mBtnPlayIndicator != null
                            && mBtnPlayIndicator.getVisibility() == View.VISIBLE) {
                        mBtnPlayIndicator.setVisibility(View.GONE);
                    }
                } else if (state == StateType.STOP
                        || state == StateType.PAUSED) {
                    if (mBtnPlayIndicator != null) {
                        mBtnPlayIndicator.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onPlayChangedError(StateType state, String message) {

            }
        });

        mVideoPlayer.setCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mBtnPlayIndicator != null) {
                    mBtnPlayIndicator.setVisibility(View.VISIBLE);
                }
            }
        });

        mVideoPlayer.setSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                mCurrentPosition = mediaPlayer.getCurrentPosition();
                if (mPlayProgressBar != null) {
                    mPlayProgressBar.setProgress(mCurrentPosition);
                }
                if (mCurrentPositionView != null) {
                    mCurrentPositionView.setText(StringUtils.generateStandardTime(mCurrentPosition));
                }
            }
        });
    }

    /**
     * 计算视频视图大小
     */
    private void calculateVideoViewSize() {
        mVideoWidth = mVideoPlayer.getVideoWidth();
        mVideoHeight = mVideoPlayer.getVideoHeight();
        if (mGLSurfaceView != null) {
            mViewWidth = mGLSurfaceView.getWidth();
            mViewHeight = mGLSurfaceView.getHeight();
        }
        float ratio = mVideoWidth * 1.0f / mVideoHeight;
        double viewAspectRatio = (double) mViewWidth / mViewHeight;
        // 如果视频的长宽比小于视图的长宽比，则要以视图高度为基准，用视频长宽比算实际视图宽度
        // 如果视频长裤阿比大于视图的长宽比，则要以视图宽度为基准，用视频长宽比算计时视图高度
        if (ratio < viewAspectRatio) {
            mViewWidth = (int) (mViewHeight * ratio);
        } else {
            mViewHeight = (int) (mViewWidth / ratio);
        }
        ViewGroup.LayoutParams layoutParams = mGLSurfaceView.getLayoutParams();
        layoutParams.width = mViewWidth;
        layoutParams.height = mViewHeight;
        mGLSurfaceView.setLayoutParams(layoutParams);
        if (mBtnPlayIndicator != null) {
            mBtnPlayIndicator.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置选中的音乐
     * @param music
     */
    public void setSelectedMusic(Music music) {
        mMusic = music;
        if (mVideoMusicFragment != null) {
            mVideoMusicFragment.setSelectedMusic(mMusic);
        }
        initAudioPlayer();
    }

    /**
     * 初始化音乐播放器
     */
    private void initAudioPlayer() {
        AudioManager mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        mSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (mMusic == null) {
            return;
        }
        if (mAudioPlayer == null) {
            mAudioPlayer = new AudioPlayer(mActivity);
            mAudioPlayer.setPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mAudioPlayer.setLooping(true);
                    mAudioPlayer.seekTo(0);
                }
            });
        }
        mAudioPlayer.setAudioPath(mMusic.getSongUrl());
        mAudioPlayer.openAudio();
    }

    /**
     * 初始化滤镜s
     */
    private void initFilters() {
        mInputFilter = new GLImageOESInputFilter(getContext());

        mColorFilters.add(new GLImageBeautyFilter(getContext()));

        mDisplayFilter = new GLImageFilter(getContext());
    }

    /**
     * 调整滤镜输入大小
     * @param width
     * @param height
     */
    private void onInputSizeChanged(int width, int height) {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(width, height);
            mInputFilter.initFrameBuffer(width, height);
        }

        for (int i = 0; i < mColorFilters.size(); i++) {
            if (mColorFilters.get(i) != null) {
                mColorFilters.get(i).onInputSizeChanged(width, height);
                mColorFilters.get(i).initFrameBuffer(width, height);
            }
        }

        for (int i = 0; i < mVideoEffectLists.size(); i++) {
            if (mVideoEffectLists.get(i) != null && mVideoEffectLists.get(i).getFilter() != null) {
                mVideoEffectLists.get(i).getFilter().onInputSizeChanged(width, height);
                mVideoEffectLists.get(i).getFilter().initFrameBuffer(width, height);
            }
        }

        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(width, height);
        }
    }

    /**
     * 调整滤镜输出大小
     * @param width
     * @param height
     */
    private void onDisplayChanged(int width, int height) {
        if (mInputFilter != null) {
            mInputFilter.onDisplaySizeChanged(width, height);
        }

        for (int i = 0; i < mColorFilters.size(); i++) {
            if (mColorFilters.get(i) != null) {
                mColorFilters.get(i).onDisplaySizeChanged(width, height);
            }
        }

        for (int i = 0; i < mVideoEffectLists.size(); i++) {
            if (mVideoEffectLists.get(i) != null && mVideoEffectLists.get(i).getFilter() != null) {
                mVideoEffectLists.get(i).getFilter().onDisplaySizeChanged(width, height);
            }
        }

        if (mDisplayFilter != null) {
            mDisplayFilter.onDisplaySizeChanged(width, height);
        }
    }

    @Override
    public void onClick(View v) {
        int index = v.getId();
        if (index == R.id.btn_back) {
            onBackPressed();
        } else if (index == R.id.btn_next) {
            saveEditedVideo();
        } else if (index == R.id.btn_play_indicator) {
            playVideo();
        } else if (index == R.id.btn_filters) {
            showFragment(FRAGMENT_FILTER);
        } else if (index == R.id.btn_font) {
            showFragment(FRAGMENT_FONT);
        } else if (index == R.id.btn_effect) {
            showFragment(FRAGMENT_EFFECT);
        } else if (index == R.id.btn_music) {
            showFragment(FRAGMENT_MUSIC);
        } else if (index == R.id.btn_cut) {
            showFragment(FRAGMENT_CUT);
        }
    }

    /**
     * 保存已经编辑的视频
     */
    private void saveEditedVideo() {
        // TODO 将特效合成到视频中
        if (mActivity != null) {
            mActivity.finish();
        }
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        if (mVideoPlayer != null) {
            mVideoPlayer.start();
            mBtnPlayIndicator.setVisibility(View.GONE);
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.start();
        }
    }

    /**
     * 重置底部视图
     */
    private void resetBottomView() {
        mLayoutProgress.setVisibility(View.VISIBLE);
        mBtnCut.setBackgroundColor(Color.TRANSPARENT);
        mBtnMusic.setBackgroundColor(Color.TRANSPARENT);
        mBtnFilters.setBackgroundColor(Color.TRANSPARENT);
        mBtnEffect.setBackgroundColor(Color.TRANSPARENT);
        mBtnFont.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * 显示编辑栏页面
     * @param index
     */
    private void showFragment(int index) {
        resetBottomView();
        mCurrentFragment = index;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        hideFragment(ft);
        if (index == FRAGMENT_CUT) {
            mBtnCut.setBackgroundColor(Color.YELLOW);
            if (mVideoCutFragment == null) {
                mVideoCutFragment = new VideoCutFragment();
                ft.add(R.id.fragment_container, mVideoCutFragment);
            } else {
                ft.show(mVideoCutFragment);
            }
        } else if (index == FRAGMENT_MUSIC) {
            mBtnMusic.setBackgroundColor(Color.YELLOW);
            if (mVideoMusicFragment == null) {
                mVideoMusicFragment = new VideoMusicFragment();
                mVideoMusicFragment.addOnMusicEditListener(mMusicEditListener);
                ft.add(R.id.fragment_container, mVideoMusicFragment);
            } else {
                ft.show(mVideoMusicFragment);
            }
        } else if (index == FRAGMENT_FILTER) {
            mBtnFilters.setBackgroundColor(Color.YELLOW);
            if (mVideoFilterFragment == null) {
                mVideoFilterFragment = new VideoFilterFragment();
                mVideoFilterFragment.addFilterSelectListener(mFilterSelectListener);
                ft.add(R.id.fragment_container, mVideoFilterFragment);
            } else {
                ft.show(mVideoFilterFragment);
            }

        } else if (index == FRAGMENT_EFFECT) {
            mBtnEffect.setBackgroundColor(Color.YELLOW);
            mLayoutProgress.setVisibility(View.GONE);
            if (mVideoEffectFragment == null) {
                mVideoEffectFragment = new VideoEffectFragment();
                mVideoEffectFragment.addOnItemLongPressedListener(mEffectItemLongPressedListener);
                mVideoEffectFragment.addOnEditListener(mEditListener);
                mVideoEffectFragment.setIndicatorScrollListener(mIndicatorScrollListener);
                ft.add(R.id.fragment_container, mVideoEffectFragment);
            } else {
                ft.show(mVideoEffectFragment);
            }
        } else if (index == FRAGMENT_FONT) {
            mBtnFont.setBackgroundColor(Color.YELLOW);
            mLayoutProgress.setVisibility(View.GONE);
            if (mVideoSubtitleFragment == null) {
                mVideoSubtitleFragment = new VideoSubtitleFragment();
                ft.add(R.id.fragment_container, mVideoSubtitleFragment);
            } else {
                ft.show(mVideoSubtitleFragment);
            }
        }
        ft.commit();
    }

    /**
     * 隐藏编辑栏页面
     * @param ft
     */
    private void hideFragment(FragmentTransaction ft) {
        if (mVideoCutFragment != null) {
            ft.hide(mVideoCutFragment);
        }
        if (mVideoMusicFragment != null) {
            ft.hide(mVideoMusicFragment);
        }
        if (mVideoFilterFragment != null) {
            ft.hide(mVideoFilterFragment);
        }
        if (mVideoEffectFragment != null) {
            ft.hide(mVideoEffectFragment);
        }
        if (mVideoSubtitleFragment != null) {
            ft.hide(mVideoSubtitleFragment);
        }
    }

    /**
     * 请求存储权限
     */
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION, true)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtils.REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION, true)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mStorageWriteEnable = true;
                initView(mContentView);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private VideoMusicFragment.OnMusicEditListener mMusicEditListener = new VideoMusicFragment.OnMusicEditListener() {
        @Override
        public void onMusicSelect() {
            if (mStartMusicSelectListener != null) {
                mStartMusicSelectListener.onOpenMusicSelectPage();
            }
        }

        @Override
        public void onMusicPlayRegion(int startPosition, int finishPosition) {
            if (mAudioPlayer != null) {
                mAudioPlayer.loopRegion(startPosition, finishPosition);
            }
        }

        @Override
        public void onMusicVoiceChange(int sourcePercent, int musicPercent) {
            if (mVideoPlayer != null) {
                mVideoPlayer.setVolume(mSystemVolume * sourcePercent / 100.0f);
            }
            if (mAudioPlayer != null) {
                mAudioPlayer.setVolume(mSystemVolume * musicPercent / 100.0f);
            }
        }

        @Override
        public void onMusicRemove() {
            mMusic = null;
            if (mAudioPlayer != null) {
                mAudioPlayer.pause();
            }
        }
    };

    private VideoFilterFragment.OnFilterSelectListener mFilterSelectListener = new VideoFilterFragment.OnFilterSelectListener() {
        @Override
        public void onFilterSelected(final GLImageFilterType type) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < mColorFilters.size(); i++) {
                        if (mColorFilters.get(i) != null) {
                            mColorFilters.get(i).release();
                        }
                    }
                    mColorFilters.clear();
                    GLImageFilter filter = GLImageFilterManager.getFilter(mGLSurfaceView.getContext(), type);
                    filter.onInputSizeChanged(mVideoWidth, mVideoHeight);
                    filter.initFrameBuffer(mVideoWidth, mVideoHeight);
                    filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
                    mColorFilters.add(filter);
                }
            });
        }
    };

    private VideoEffectFragment.OnItemLongPressedListener mEffectItemLongPressedListener = new VideoEffectFragment.OnItemLongPressedListener() {
        @Override
        public void onLongPressedPrepared(int position) {
            if (mVideoEffectFragment == null) {
                return;
            }
            final GLImageFilterType type = mVideoEffectFragment.getFilterType(position);
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    // 准备滤镜数据
                    GLImageFilter filter = GLImageFilterManager.getEffectFilter(mGLSurfaceView.getContext(), type);
                    filter.initFrameBuffer(mVideoWidth, mVideoHeight);
                    filter.onInputSizeChanged(mVideoWidth, mVideoHeight);
                    filter.onDisplaySizeChanged(mViewWidth, mViewHeight);

                    VideoEffect videoDrawData = new VideoEffect();
                    videoDrawData.setStartPosition(mCurrentPosition);
                    videoDrawData.setFilter(filter);

                    clearEffectEditData();
                    mEffectEditLists.add(videoDrawData);
                }
            });
            if (mVideoEffectFragment.getIndicatorProgress() != null) {
                mVideoEffectFragment.getIndicatorProgress().clearPreparedColor();
                mVideoEffectFragment.getIndicatorProgress().preparedColor((float) (mCurrentPosition / mDuration));
            }
        }

        @Override
        public void onLongPressedCancel(int position) {
            clearToast();
            mToast = Toast.makeText(getContext(), "请长按!", Toast.LENGTH_SHORT);
            mToast.show();

            if (mVideoEffectFragment.getIndicatorProgress() != null) {
                mVideoEffectFragment.getIndicatorProgress().clearPreparedColor();
            }

            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    clearEffectEditData();
                }
            });
        }

        @Override
        public void onLongPressedStarted(int position) {
            clearToast();
            mVideoEdit = true;

            if (mVideoEffectFragment.getIndicatorProgress() != null) {
                mVideoEffectFragment.getIndicatorProgress().setVideoEdit(true);
            }

            if (mVideoPlayer != null) {
                mVideoPlayer.start();
            }
        }

        @Override
        public void onLongPressedFinished(int position) {
            if (mVideoPlayer != null) {
                mVideoPlayer.pause();
            }
            mVideoEdit = false;

            if (mVideoEffectFragment.getIndicatorProgress() != null) {
                mVideoEffectFragment.getIndicatorProgress().setVideoEdit(false);
                mVideoEffectFragment.getIndicatorProgress().addColorData((float)(mCurrentPosition / mDuration));
            }

            if (mGLSurfaceView != null) {
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        addEffectDrawData();
                    }
                });
            }
        }
    };

    private void clearToast() {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }

    private VideoEffectFragment.OnEditListener mEditListener = new VideoEffectFragment.OnEditListener() {
        @Override
        public void onEffectDelete() {
            int position = mVideoEffectLists.size() - 1;
            VideoEffect videoDrawData = mVideoEffectLists.remove(position);
            mEffectTempLists.push(videoDrawData);

            mVideoEffectFragment.showBtnUndo(mEffectTempLists.size() > 0);
            mVideoEffectFragment.showBtnDelete(mVideoEffectLists.size() > 0);
        }

        @Override
        public void onEffectUndoDelete() {
            if (mEffectTempLists.size() > 0) {
                VideoEffect videoDrawData = mEffectTempLists.pop();
                if (videoDrawData.getFilter() != null) {
                    mVideoEffectLists.add(videoDrawData);
                }
            }
            mVideoEffectFragment.showBtnUndo(mEffectTempLists.size() > 0);
            mVideoEffectFragment.showBtnDelete(mVideoEffectLists.size() > 0);
        }
    };

    private IndicatorProgress.IndicatorScrollListener mIndicatorScrollListener = new IndicatorProgress.IndicatorScrollListener() {
        @Override
        public void onScrollChanged(float percent) {
            if (mVideoPlayer != null) {
                mCurrentPosition = (int) (mDuration * percent);
                mVideoPlayer.seekTo((int) mCurrentPosition);
            }
        }

        @Override
        public void onScrollFinish() {

        }
    };

    /**
     * 清空特效编辑操作的数据
     */
    private void clearEffectEditData() {
        if (mEffectEditLists.size() > 0) {
            for (int i = 0; i < mEffectEditLists.size(); i++) {
                if (mEffectEditLists.get(i) != null && mEffectEditLists.get(i).getFilter() != null) {
                    mEffectEditLists.get(i).getFilter().release();
                }
            }
            mEffectEditLists.clear();
        }
    }

    /**
     * 将准备好的数据添加到特效数据中
     */
    private void addEffectDrawData() {
        clearDeleteTempData();
        for (int i = 0; i < mEffectEditLists.size(); i++) {
            if (mEffectEditLists.get(i) != null && mEffectEditLists.get(i).getFilter() != null) {
                VideoEffect videoDrawData = mEffectEditLists.get(i);
                videoDrawData.setFinishPosition((int)mCurrentPosition);
                mVideoEffectLists.add(videoDrawData);
            }
        }
        mEffectEditLists.clear();
        mVideoEffectFragment.showBtnUndo(mEffectTempLists.size() > 0);
        mVideoEffectFragment.showBtnDelete(mVideoEffectLists.size() > 0);
    }

    /**
     * 清空暂存的数据（回删、撤销操作之后再次添加特效完成，然后才销毁暂存的数据）
     */
    private void clearDeleteTempData() {
        if (mEffectTempLists.size() > 0) {
            VideoEffect videoDrawData;
            while (!mEffectTempLists.isEmpty() && (videoDrawData = mEffectTempLists.pop()) != null) {
                if (videoDrawData.getFilter() != null) {
                    videoDrawData.getFilter().release();
                }
            }
            mEffectTempLists.clear();
        }
    }

    /**
     * 页面操作监听器
     */
    public interface OnPageOperationListener {
        // 打开音乐选择页面
        void onOpenMusicSelectPage();
    }

    /**
     * 添加页面操作监听器
     * @param listener
     */
    public void setOnPageOperationListener(OnPageOperationListener listener) {
        mStartMusicSelectListener = listener;
    }

}
