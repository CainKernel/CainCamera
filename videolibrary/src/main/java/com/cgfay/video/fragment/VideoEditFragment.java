package com.cgfay.video.fragment;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.media.CainMediaPlayer;
import com.cgfay.media.CainShortVideoEditor;
import com.cgfay.media.IMediaPlayer;
import com.cgfay.utilslibrary.fragment.BackPressedDialogFragment;
import com.cgfay.utilslibrary.utils.FileUtils;
import com.cgfay.utilslibrary.utils.StringUtils;
import com.cgfay.video.R;
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
    private VideoTextureView mVideoPlayerView;
    private ImageView mIvVideoPlay;
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
    private TextView mTvCutCurrent;
    private TextView mTvCutDuration;

    private MediaPlayer mAudioPlayer;
    private CainMediaPlayer mCainMediaPlayer;
    private AudioManager mAudioManager;
    private CainShortVideoEditor mVideoEditor;

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
        mVideoPlayerView = mContentView.findViewById(R.id.video_player_view);
        mVideoPlayerView.setSurfaceTextureListener(mSurfaceTextureListener);
        mIvVideoPlay = mContentView.findViewById(R.id.iv_video_play);
        mIvVideoPlay.setOnClickListener(this);

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
        if (id == R.id.iv_video_play) {
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.resume();
                mIvVideoPlay.setVisibility(View.GONE);
            }
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
        }
    }

    private void resetBottomView() {
        mLayoutSubBottom.removeAllViews();
        mLayoutSubBottom.setVisibility(View.GONE);
        mLayoutBottom.setVisibility(View.VISIBLE);
        mLayoutTop.setVisibility(View.VISIBLE);
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
                mTvCutCurrent = mLayoutCutMusic.findViewById(R.id.tv_audio_current);
                mTvCutDuration = mLayoutCutMusic.findViewById(R.id.tv_audio_duration);
                mWaveCutView = mLayoutCutMusic.findViewById(R.id.wave_cut_view);
                mWaveCutView.setOnDragListener(mCutMusicListener);
                if (mMusicPath != null) {
                    mWaveCutView.setMax((int) mBackgroundDuration);
                    mWaveCutView.setProgress(0);
                    mTvCutDuration.setText(StringUtils.generateStandardTime((int) mBackgroundDuration));
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
    }

    private void subChangeSave() {
        resetBottomView();
    }

    /**
     * 显示特效页面
     * @param showSubView
     */
    private void showChangeEffectLayout(boolean showSubView) {
        if (showSubView) {

        } else {
            resetBottomView();
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

        } else {
            resetBottomView();
        }
    }

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
            mTvCutDuration.setText(StringUtils.generateStandardTime((int) mBackgroundDuration));
        }
    }

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
            public void onPrepared(IMediaPlayer mp) {
                mp.start();
            }
        });
        mCainMediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mediaPlayer, int width, int height) {
                mVideoPlayerView.setVideoSize(width, height);
                mVideoPlayerView.setRotation(mediaPlayer.getRotate());
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
            mTvCutCurrent.setText(StringUtils.generateStandardTime(position));
        }

        @Override
        public void onDragFinish(float position) {
            if (mAudioPlayer != null) {
                mAudioPlayer.seekTo((int)position);
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
