package com.cgfay.video.fragment;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.filterlibrary.glfilter.resource.FilterHelper;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.media.CainMediaPlayer;
import com.cgfay.media.CainShortVideoEditor;
import com.cgfay.media.IMediaPlayer;
import com.cgfay.utilslibrary.fragment.BackPressedDialogFragment;
import com.cgfay.utilslibrary.utils.DensityUtils;
import com.cgfay.utilslibrary.utils.FileUtils;
import com.cgfay.utilslibrary.utils.StringUtils;
import com.cgfay.video.R;
import com.cgfay.video.adapter.VideoEffectAdapter;
import com.cgfay.video.adapter.VideoEffectCategoryAdapter;
import com.cgfay.video.adapter.VideoFilterAdapter;
import com.cgfay.video.bean.EffectMimeType;
import com.cgfay.video.bean.EffectType;
import com.cgfay.video.widget.EffectSelectedSeekBar;
import com.cgfay.video.widget.VideoTextureView;
import com.cgfay.video.widget.WaveCutView;

import java.io.IOException;

/**
 * 特效编辑页面
 */
public class VideoEditFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "VideoEditFragment";

    private Activity mActivity;

    private String mVideoPath;                      // 视频流路径
    private String mMusicPath;                      // 背景音乐路径
    private float mSourceVolumePercent = 0.5f;      // 源音量百分比
    private float mBackgroundVolumePercent = 0.5f;  // 背景音乐音量百分比
    private long mBackgroundDuration;               // 背景音乐时长，ms

    private View mContentView;
    // 播放控件
    private RelativeLayout mLayoutPlayer;
    private VideoTextureView mVideoPlayerView;
    private ImageView mIvVideoPlay;

    // 特效选择栏
    private LinearLayout mLayoutEffect;             // 特效布局
    private TextView mTvVideoCurrent;               // 当前位置
    private EffectSelectedSeekBar mSbEffectSelected;// 带特效选中的进度条
    private TextView mTvVideoDuration;              // 视频时长
    private TextView mTvEffectTips;                 // 特效提示
    private TextView mTvEffectCancel;               // 撤销按钮
    private RecyclerView mListEffectView;           // 特效列表
    private RecyclerView mListEffectCategoryView;   // 特效目录列表
    private boolean mEffectShowing;                 // 特效页面显示状态
    private VideoEffectAdapter mEffectAdapter;      // 特效列表适配器
    private VideoEffectCategoryAdapter mEffectCategoryAdapter; // 特效目录列表适配器

    // 顶部控制栏
    private RelativeLayout mLayoutTop;
    // 顶部子控制栏
    private RelativeLayout mLayoutSubTop;
    // 底部控制栏
    private RelativeLayout mLayoutBottom;
    // 底部子控制栏
    private FrameLayout mLayoutSubBottom;

    // 音量调节页面
    private View mLayoutVolumeChange;
    private SeekBar mSbBackgroundVolume;

    // 音乐裁剪页面
    private View mLayoutCutMusic;
    private WaveCutView mWaveCutView;
    private TextView mTvMusicCurrent;
    private TextView mTvMusicDuration;

    // 滤镜列表
    private RecyclerView mListFilterView;
    private VideoFilterAdapter mFilterAdapter;


    // 播放器
    private MediaPlayer mAudioPlayer;
    private CainMediaPlayer mCainMediaPlayer;
    private AudioManager mAudioManager;
    private CainShortVideoEditor mVideoEditor;
    private int mPlayViewWidth;
    private int mPlayViewHeight;
    private int mVideoWidth;
    private int mVideoHeight;

    public static VideoEditFragment newInstance() {
        return new VideoEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView =  inflater.inflate(R.layout.fragment_video_edit, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        // 播放器显示控件
        mLayoutPlayer = mContentView.findViewById(R.id.layout_player);
        mVideoPlayerView = mContentView.findViewById(R.id.video_player_view);
        mVideoPlayerView.setSurfaceTextureListener(mSurfaceTextureListener);
        mVideoPlayerView.setOnClickListener(this);
        mIvVideoPlay = mContentView.findViewById(R.id.iv_video_play);
        mIvVideoPlay.setOnClickListener(this);

        // 特效控制栏
        mLayoutEffect = mContentView.findViewById(R.id.layout_effect);
        mTvVideoCurrent = mContentView.findViewById(R.id.tv_video_current);
        mSbEffectSelected = mContentView.findViewById(R.id.sb_select_effect);
        mSbEffectSelected.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        mTvVideoDuration = mContentView.findViewById(R.id.tv_video_duration);
        mTvEffectTips = mContentView.findViewById(R.id.tv_video_edit_effect_tips);
        mTvEffectCancel = mContentView.findViewById(R.id.tv_video_edit_effect_cancel);
        mTvEffectCancel.setOnClickListener(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.HORIZONTAL);
        // 特效列表
        mListEffectView = mContentView.findViewById(R.id.list_video_edit_effect);
        mListEffectView.setLayoutManager(layoutManager);
        mEffectAdapter = new VideoEffectAdapter(mActivity, EffectFilterHelper.getInstance().getEffectFilterData());
        mEffectAdapter.setOnEffectChangeListener(mEffectChangeListener);
        mListEffectView.setAdapter(mEffectAdapter);

        // 特效目录列表
        mListEffectCategoryView = mContentView.findViewById(R.id.list_video_edit_effect_category);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mActivity);
        ((LinearLayoutManager) manager).setOrientation(LinearLayoutManager.HORIZONTAL);
        mListEffectCategoryView.setLayoutManager(manager);
        mEffectCategoryAdapter = new VideoEffectCategoryAdapter(mActivity);
        mEffectCategoryAdapter.setOnEffectCategoryChangeListener(mEffectCategoryChangeListener);
        mListEffectCategoryView.setAdapter(mEffectCategoryAdapter);

        // 顶部控制栏
        mLayoutTop = mContentView.findViewById(R.id.layout_top);
        mContentView.findViewById(R.id.btn_edit_back).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_select_music).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_change_voice).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_cut_music).setOnClickListener(this);

        // 顶部子控制栏
        mLayoutSubTop = mContentView.findViewById(R.id.layout_sub_top);
        mContentView.findViewById(R.id.btn_sub_cancel).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_sub_save).setOnClickListener(this);

        // 底部控制栏
        mLayoutBottom = mContentView.findViewById(R.id.layout_bottom);
        mContentView.findViewById(R.id.btn_edit_effect).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_edit_cover).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_edit_filter).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_edit_stickers).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_edit_next).setOnClickListener(this);

        // 底部子控制栏
        mLayoutSubBottom = mContentView.findViewById(R.id.layout_sub_bottom);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mCainMediaPlayer == null) {
            mCainMediaPlayer = new CainMediaPlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.resume();
            mIvVideoPlay.setVisibility(View.GONE);
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.pause();
            mIvVideoPlay.setVisibility(View.VISIBLE);
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.pause();
        }
    }

    public void onBackPressed() {
        DialogFragment fragment = new BackPressedDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BackPressedDialogFragment.MESSAGE, R.string.video_edit_back_press);
        fragment.setArguments(bundle);
        fragment.show(getChildFragmentManager(), "");
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
            mAudioManager = null;
        }
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.reset();
            mCainMediaPlayer = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.reset();
            mAudioPlayer = null;
        }
        FileUtils.deleteFile(mVideoPath);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.video_player_view) {
            if (mCainMediaPlayer != null) {
                if (mCainMediaPlayer.isPlaying()) {
                    pausePlayer();
                } else {
                    resumePlayer();
                }
            }
        } else if (id == R.id.iv_video_play) {
            resumePlayer();
        } else if (id == R.id.btn_edit_back) {
            onBackPressed();
        } else if (id == R.id.btn_select_music) {
            selectMusic();
        } else if (id == R.id.btn_change_voice) {
            showVolumeChangeLayout(true);
        } else if (id == R.id.btn_cut_music) {
            showCutMusicLayout(true);
        } else if (id == R.id.btn_sub_cancel) {
            subChangeCancel();
        } else if (id == R.id.btn_sub_save) {
            subChangeSave();
        } else if (id == R.id.btn_edit_effect) {
            showChangeEffectLayout(true);
        } else if (id == R.id.btn_edit_cover) {
            showSelectCoverLayout(true);
        } else if (id == R.id.btn_edit_filter) {
            showSelectFilterLayout(true);
        } else if (id == R.id.btn_edit_stickers) {
            showSelectStickersLayout(true);
        } else if (id == R.id.btn_edit_next) {
            saveAllChange();
        } else if (id == R.id.iv_volume_change_save) {
            showVolumeChangeLayout(false);
        } else if (id == R.id.iv_cut_music_save) {
            showCutMusicLayout(false);
        } else if (id == R.id.tv_video_edit_effect_cancel) {   // 撤销特效
            // TODO 撤销上一个特效
        }
    }

    private void resetBottomView() {
        mLayoutSubBottom.removeAllViews();
        mLayoutSubBottom.setVisibility(View.GONE);
        mLayoutBottom.setVisibility(View.VISIBLE);
        mLayoutTop.setVisibility(View.VISIBLE);
        mLayoutSubTop.setVisibility(View.GONE);
    }

    private void selectMusic() {
        resetBottomView();
        if (mOnSelectMusicListener != null) {
            mOnSelectMusicListener.onOpenMusicSelectPage();
        }
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.pause();
            mIvVideoPlay.setVisibility(View.VISIBLE);
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.pause();
        }
    }

    /**
     * 是否显示音量调节布局
     * @param showSubView
     */
    private void showVolumeChangeLayout(boolean showSubView) {
        if (showSubView) {
            mLayoutBottom.setVisibility(View.GONE);
            if (mLayoutVolumeChange == null) {
                mLayoutVolumeChange = LayoutInflater.from(mActivity).inflate(R.layout.view_volume_change, null);
                mLayoutVolumeChange.findViewById(R.id.iv_volume_change_save).setOnClickListener(this);
                ((SeekBar)mLayoutVolumeChange.findViewById(R.id.sb_volume_source))
                        .setOnSeekBarChangeListener(mVolumeChangeListener);
                mSbBackgroundVolume = mLayoutVolumeChange.findViewById(R.id.sb_volume_background);
                mSbBackgroundVolume.setOnSeekBarChangeListener(mVolumeChangeListener);
                if (mMusicPath != null) {
                    mSbBackgroundVolume.setMax(100);
                    mSbBackgroundVolume.setProgress((int)(mBackgroundVolumePercent * 100));
                } else {
                    mSbBackgroundVolume.setMax(0);
                    mSbBackgroundVolume.setProgress(0);

                }
            }
            mLayoutSubBottom.removeAllViews();
            mLayoutSubBottom.addView(mLayoutVolumeChange);
            mLayoutSubBottom.setVisibility(View.VISIBLE);
            mLayoutTop.setVisibility(View.GONE);
        } else {
            resetBottomView();
        }
    }

    /**
     * 是否显示剪辑音乐布局
     * @param showSubView
     */
    private void showCutMusicLayout(boolean showSubView) {
        if (showSubView && mMusicPath != null) {
            mLayoutBottom.setVisibility(View.GONE);
            if (mLayoutCutMusic == null) {
                mLayoutCutMusic = LayoutInflater.from(mActivity).inflate(R.layout.view_music_cut, null);
                mLayoutCutMusic.findViewById(R.id.iv_cut_music_save).setOnClickListener(this);
                mTvMusicCurrent = mLayoutCutMusic.findViewById(R.id.tv_audio_current);
                mTvMusicDuration = mLayoutCutMusic.findViewById(R.id.tv_audio_duration);
                mWaveCutView = mLayoutCutMusic.findViewById(R.id.wave_cut_view);
                mWaveCutView.setOnDragListener(mCutMusicListener);
                if (mMusicPath != null) {
                    mWaveCutView.setMax((int) mBackgroundDuration);
                    mWaveCutView.setProgress(0);
                    mTvMusicDuration.setText(StringUtils.generateStandardTime((int) mBackgroundDuration));
                } else {
                    mWaveCutView.setMax(50);
                    mWaveCutView.setProgress(0);
                }
            }
            mLayoutSubBottom.removeAllViews();
            mLayoutSubBottom.addView(mLayoutCutMusic);
            mLayoutSubBottom.setVisibility(View.VISIBLE);
            mLayoutTop.setVisibility(View.GONE);
        } else {
            resetBottomView();
        }
    }

    private void subChangeCancel() {
        resetBottomView();
        if (mEffectShowing) {
            showChangeEffectLayout(false);
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.changeEffect(-1);
            }
        }
    }

    private void subChangeSave() {
        resetBottomView();
        if (mEffectShowing) {
            showChangeEffectLayout(false);
        }
    }

    /**
     * 显示特效页面
     * @param showSubView
     */
    private void showChangeEffectLayout(boolean showSubView) {
        mEffectShowing = showSubView;
        if (showSubView) {
            AnimatorSet animatorSet = new AnimatorSet();
            // 特效页面显示动画
            ValueAnimator effectShowAnimator = ValueAnimator.ofFloat(1f, 0f);
            effectShowAnimator.setDuration(400);
            final LinearLayout.LayoutParams effectParams = (LinearLayout.LayoutParams) mLayoutEffect.getLayoutParams();
            final LinearLayout.LayoutParams playerParams = (LinearLayout.LayoutParams) mLayoutPlayer.getLayoutParams();
            mPlayViewWidth = mLayoutPlayer.getWidth();
            mPlayViewHeight = mLayoutPlayer.getHeight();
            final int minPlayViewHeight = mPlayViewHeight - DensityUtils.dp2px(mActivity, 200);
            final float playerViewScale = mPlayViewWidth/(float)mPlayViewHeight;
            effectShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    effectParams.bottomMargin = (int) (-DensityUtils.dp2px(mActivity, 200) * (float)animation.getAnimatedValue());
                    mLayoutEffect.setLayoutParams(effectParams);
                    playerParams.width = (int) ((minPlayViewHeight + ((mPlayViewHeight - minPlayViewHeight)* (float)animation.getAnimatedValue())) * playerViewScale);
                    playerParams.bottomMargin = (int) (DensityUtils.dp2px(mActivity, 18) * (1f - (float)animation.getAnimatedValue()));
                    mLayoutPlayer.setLayoutParams(playerParams);
                }
            });
            animatorSet.playSequentially(effectShowAnimator);
            animatorSet.start();

            mLayoutBottom.setVisibility(View.GONE);
            mLayoutTop.setVisibility(View.GONE);
            mLayoutSubTop.setVisibility(View.VISIBLE);

            pausePlayer();
        } else {

            AnimatorSet animatorSet = new AnimatorSet();
            // 特效页面退出动画
            ValueAnimator effectExitAnimator = ValueAnimator.ofFloat(0f, 1f);
            effectExitAnimator.setDuration(400);
            final LinearLayout.LayoutParams effectParams = (LinearLayout.LayoutParams) mLayoutEffect.getLayoutParams();
            final LinearLayout.LayoutParams playerParams = (LinearLayout.LayoutParams) mLayoutPlayer.getLayoutParams();
            final int minPlayViewHeight = mPlayViewHeight - DensityUtils.dp2px(mActivity, 200);
            final float playerViewScale = mPlayViewWidth/(float)mPlayViewHeight;
            effectExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    effectParams.bottomMargin = (int) (-DensityUtils.dp2px(mActivity, 200) * (float)animation.getAnimatedValue());
                    mLayoutEffect.setLayoutParams(effectParams);
                    playerParams.width = (int) ((minPlayViewHeight + ((mPlayViewHeight - minPlayViewHeight)* (float)animation.getAnimatedValue())) * playerViewScale);
                    playerParams.bottomMargin = (int) (DensityUtils.dp2px(mActivity, 18) * (1f - (float)animation.getAnimatedValue()));
                    mLayoutPlayer.setLayoutParams(playerParams);
                }
            });
            animatorSet.playSequentially(effectExitAnimator);
            animatorSet.start();

            resumePlayer();
        }
    }

    /**
     * 切换特效目录
     * @param type
     */
    private void changeEffectCategoryView(EffectMimeType type) {
        if (type == EffectMimeType.FILTER) {

        } else if (type == EffectMimeType.TRANSITION) {

        } else if (type == EffectMimeType.MULTIFRAME) {

        } else if (type == EffectMimeType.TIME) {

        }
    }

    /**
     * 显示选择封面页面
     * @param showSubView
     */
    private void showSelectCoverLayout(boolean showSubView) {
        if (showSubView) {

        } else {
            resetBottomView();
        }
    }

    /**
     * 显示选择滤镜页面
     * @param showSubView
     */
    private void showSelectFilterLayout(boolean showSubView) {
        if (showSubView) {
            if (mListFilterView == null) {
                mListFilterView = new RecyclerView(mActivity);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
                ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.HORIZONTAL);
                mListFilterView.setLayoutManager(layoutManager);
            }
            if (mFilterAdapter == null) {
                mFilterAdapter = new VideoFilterAdapter(mActivity, FilterHelper.getFilterList());
            }
            mFilterAdapter.setOnFilterChangeListener(mFilterChangeListener);
            mListFilterView.setAdapter(mFilterAdapter);
            mLayoutSubBottom.removeAllViews();
            mLayoutSubBottom.addView(mListFilterView);
            mLayoutSubBottom.setVisibility(View.VISIBLE);

            mLayoutBottom.setVisibility(View.GONE);
            mLayoutTop.setVisibility(View.GONE);
            mLayoutSubTop.setVisibility(View.VISIBLE);
        } else {
            resetBottomView();
        }
    }

    /**
     * 选择贴纸页面
     * @param showSubView
     */
    private void showSelectStickersLayout(boolean showSubView) {
        if (showSubView) {

        } else {
            resetBottomView();
        }
    }

    /**
     * 保存所有变更，合成视频
     */
    private void saveAllChange() {

    }

    private void resumePlayer() {
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.resume();
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.start();
        }
        mIvVideoPlay.setVisibility(View.GONE);
    }

    private void seekTo(long timeMs) {
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.resume();
            mCainMediaPlayer.seekTo(timeMs);
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.start();
            mAudioPlayer.seekTo((int)timeMs);
        }
        mIvVideoPlay.setVisibility(View.GONE);
    }

    private void pausePlayer() {
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.pause();
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.pause();
        }
        mIvVideoPlay.setVisibility(View.VISIBLE);
    }

    /**
     * 设置视频流路径
     * @param videoPath
     */
    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
    }

    /**
     * 设置背景音乐路径
     * @param musicPath
     * @param duration
     */
    public void setSelectedMusic(String musicPath, long duration) {
        mMusicPath = musicPath;
        mBackgroundDuration = duration;
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.resume();
            mIvVideoPlay.setVisibility(View.GONE);
        }
        if (mAudioPlayer == null) {
            mAudioPlayer = new MediaPlayer();
        } else {
            mAudioPlayer.reset();
        }
        try {
            mAudioPlayer.setDataSource(mMusicPath);
            mAudioPlayer.setLooping(true);
            mAudioPlayer.prepare();
            mAudioPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAudioPlayer.setVolume(mBackgroundVolumePercent, mBackgroundVolumePercent);
        if (mSbBackgroundVolume != null) {
            mSbBackgroundVolume.setMax(100);
            mSbBackgroundVolume.setProgress((int)(mBackgroundVolumePercent * 100));
        }
        if (mWaveCutView != null) {
            mWaveCutView.setMax((int) mBackgroundDuration);
            mWaveCutView.setProgress(0);
            mTvMusicDuration.setText(StringUtils.generateStandardTime((int) mBackgroundDuration));
        }
    }

    // 视频显示监听
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mSurfaceTexture == null) {
                mSurfaceTexture = surface;
                openMediaPlayer();
            } else {
                mVideoPlayerView.setSurfaceTexture(mSurfaceTexture);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return mSurfaceTexture == null;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**
     * 打开视频播放器
     */
    private void openMediaPlayer() {
        mContentView.setKeepScreenOn(true);
        mCainMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final IMediaPlayer mp) {
                mp.start();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSbEffectSelected != null) {
                            mSbEffectSelected.setMax(mp.getDuration());
                            mSbEffectSelected.setProgress(mp.getCurrentPosition());
                        }
                        if (mTvVideoCurrent != null) {
                            mTvVideoCurrent.setText(StringUtils.generateStandardTime((int)mp.getCurrentPosition()));
                        }
                        if (mTvVideoDuration != null) {
                            mTvVideoDuration.setText(StringUtils.generateStandardTime((int)mp.getDuration()));
                        }
                    }
                });
            }
        });
        mCainMediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mediaPlayer, int width, int height) {
                if (mediaPlayer.getRotate() % 180 != 0) {
                    mVideoPlayerView.setVideoSize(height, width);
                } else {
                    mVideoPlayerView.setVideoSize(width, height);
                }
            }
        });
        mCainMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {

            }
        });

        mCainMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onError: what = " + what + ", extra = " + extra);
                return false;
            }
        });

        mCainMediaPlayer.setOnCurrentPositionListener(new CainMediaPlayer.OnCurrentPositionListener() {
            @Override
            public void onCurrentPosition(long current, long duration) {
                if (mTvVideoCurrent != null) {
                    mTvVideoCurrent.setText(StringUtils.generateStandardTime((int)current));
                }
                if (mSbEffectSelected != null) {
                    mSbEffectSelected.setProgress((float)current);
                }
            }
        });
        try {
            mCainMediaPlayer.setDataSource(mVideoPath);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mCainMediaPlayer.setSurface(mSurface);
            mCainMediaPlayer.setVolume(mSourceVolumePercent, mSourceVolumePercent);
            mCainMediaPlayer.setOption(CainMediaPlayer.OPT_CATEGORY_PLAYER, "vcodec", "h264_mediacodec");
            mCainMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 带特效选中的滑动监听
     */
    private EffectSelectedSeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new EffectSelectedSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgress(int progress, boolean fromUser) {
            if (fromUser) {
                Log.d(TAG, "onProgress: progress = " + progress);
            }
        }

        @Override
        public void onStopTrackingTouch(int progress) {
            seekTo(progress);
        }

        @Override
        public void onStartTrackingTouch() {
            pausePlayer();
        }
    };


    /**
     * 音量调节监听器
     */
    private SeekBar.OnSeekBarChangeListener mVolumeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getId() == R.id.sb_volume_source && fromUser) {
                mSourceVolumePercent = (float) progress  / (float) seekBar.getMax();
            } else if (seekBar.getId() == R.id.sb_volume_background && fromUser) {
                if (seekBar.getMax() > 0) {
                    mBackgroundVolumePercent = (float) progress / (float) seekBar.getMax();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (seekBar.getId() == R.id.sb_volume_background) {
                if (mAudioPlayer != null) {
                    mAudioPlayer.setVolume(mBackgroundVolumePercent, mBackgroundVolumePercent);
                }
            } else if (seekBar.getId() == R.id.sb_volume_source) {
                if (mCainMediaPlayer != null) {
                    Log.d(TAG, "onStopTrackingTouch: volume = " + mSourceVolumePercent);
                    mCainMediaPlayer.setVolume(mSourceVolumePercent, mSourceVolumePercent);
                }
            }
        }
    };

    /**
     * 裁剪音乐监听器
     */
    private WaveCutView.OnDragListener mCutMusicListener = new WaveCutView.OnDragListener() {
        @Override
        public void onDragging(int position) {
            mTvMusicCurrent.setText(StringUtils.generateStandardTime(position));
        }

        @Override
        public void onDragFinish(float position) {
            if (mAudioPlayer != null) {
                mAudioPlayer.seekTo((int)position);
            }
        }
    };

    /**
     * 滤镜列表改变回调
     */
    private VideoFilterAdapter.OnFilterChangeListener mFilterChangeListener = new VideoFilterAdapter.OnFilterChangeListener() {
        @Override
        public void onFilterChanged(ResourceData resourceData) {

        }
    };

    /**
     * 特效列表切换
     */
    private VideoEffectAdapter.OnEffectChangeListener mEffectChangeListener = new VideoEffectAdapter.OnEffectChangeListener() {
        @Override
        public void onEffectChanged(EffectType effectType) {
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.changeEffect(effectType.getName());
            }
        }
    };

    /**
     * 特效目录切换
     */
    private VideoEffectCategoryAdapter.OnEffectCategoryChangeListener mEffectCategoryChangeListener = new VideoEffectCategoryAdapter.OnEffectCategoryChangeListener() {
        @Override
        public void onCategoryChange(EffectMimeType mimeType) {
            if (mimeType == EffectMimeType.FILTER) {
                mEffectAdapter.changeEffectData(EffectFilterHelper.getInstance().getEffectFilterData());
            } else if (mimeType == EffectMimeType.MULTIFRAME) {
                mEffectAdapter.changeEffectData(EffectFilterHelper.getInstance().getEffectMultiData());
            } else if (mimeType == EffectMimeType.TRANSITION) {
                mEffectAdapter.changeEffectData(EffectFilterHelper.getInstance().getEffectTransitionData());
            } else {
                mEffectAdapter.changeEffectData(null);
            }
        }
    };

    /**
     * 页面操作监听器
     */
    public interface OnSelectMusicListener {

        void onOpenMusicSelectPage();
    }

    /**
     * 添加页面操作监听器
     * @param listener
     */
    public void setOnSelectMusicListener(OnSelectMusicListener listener) {
        mOnSelectMusicListener = listener;
    }

    private OnSelectMusicListener mOnSelectMusicListener;
}
