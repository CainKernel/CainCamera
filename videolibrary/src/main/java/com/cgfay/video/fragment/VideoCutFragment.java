package com.cgfay.video.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.cgfay.media.CainMediaEditor;
import com.cgfay.media.CainMediaPlayer;
import com.cgfay.media.VideoEditorUtil;
import com.cgfay.uitls.utils.DensityUtils;
import com.cgfay.uitls.utils.DisplayUtils;
import com.cgfay.uitls.utils.FileUtils;
import com.cgfay.uitls.widget.RoundOutlineProvider;
import com.cgfay.video.R;
import com.cgfay.video.activity.VideoEditActivity;
import com.cgfay.video.bean.VideoSpeed;
import com.cgfay.video.widget.CircleProgressView;
import com.cgfay.video.widget.VideoCutViewBar;
import com.cgfay.video.widget.VideoSpeedLevelBar;
import com.cgfay.video.widget.VideoTextureView;

import java.io.IOException;

public class VideoCutFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "VideoCutFragment";

    private String mVideoPath;
    private Activity mActivity;

    private View mContentView;
    // 播放控件
    private VideoTextureView mVideoPlayerView;
    // 倍速选择条
    private VideoSpeedLevelBar mVideoSpeedLevelBar;
    // 裁剪Bar
    private VideoCutViewBar mVideoCutViewBar;
    // 选中提示
    private TextView mTextVideoCropSelected;

    // 执行进度提示
    private LinearLayout mLayoutProgress;
    // 圆形进度条
    private CircleProgressView mCvCropProgress;
    private TextView mTvCropProgress;

    private VideoSpeed mVideoSpeed = VideoSpeed.SPEED_L2;

    // 毫秒
    private long mCutStart = 0;
    private long mCutRange = 15000;
    private long mVideoDuration;
    private boolean mSeeking = false;
    private CainMediaPlayer mCainMediaPlayer;
    private AudioManager mAudioManager;
    private CainMediaEditor mMediaEditor;

    public static VideoCutFragment newInstance() {
        return new VideoCutFragment();
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
        mContentView =  inflater.inflate(R.layout.fragment_video_crop, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        }

        mContentView.findViewById(R.id.video_crop_back).setOnClickListener(this);
        mContentView.findViewById(R.id.video_crop_ok).setOnClickListener(this);

        mVideoPlayerView = mContentView.findViewById(R.id.video_player_view);
        mVideoPlayerView.setSurfaceTextureListener(mSurfaceTextureListener);

        // 视频容器
        View layoutVideo = mContentView.findViewById(R.id.layout_video);
        ConstraintLayout.LayoutParams videoParams = (ConstraintLayout.LayoutParams) layoutVideo.getLayoutParams();
        if (DisplayUtils.isFullScreenDevice(mActivity)) {
            videoParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.layout_edit_bottom_height);
        } else {
            videoParams.bottomMargin = 0;
        }
        layoutVideo.setLayoutParams(videoParams);
        if (Build.VERSION.SDK_INT >= 21) {
            layoutVideo.setOutlineProvider(new RoundOutlineProvider(DensityUtils.dp2px(mActivity, 7.5f)));
            layoutVideo.setClipToOutline(true);
        }

        mVideoSpeedLevelBar = mContentView.findViewById(R.id.video_crop_speed_bar);
        mVideoSpeedLevelBar.setOnSpeedChangedListener(speed -> {
            if (mCainMediaPlayer != null) {
                mVideoSpeed = speed;
                float rate = speed.getSpeed();
                float pitch = 1.0f / rate;
                mCainMediaPlayer.setRate(rate);
                mCainMediaPlayer.setPitch(pitch);
                mCainMediaPlayer.seekTo(mCutStart);
                if (mVideoCutViewBar != null) {
                    mVideoCutViewBar.setSpeed(mVideoSpeed);
                }
            }
        });

        mTextVideoCropSelected = mContentView.findViewById(R.id.tv_video_cut_selected);
        mContentView.findViewById(R.id.tv_video_cut_speed_bar_visible).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_video_cut_rotation).setOnClickListener(this);
        View layoutSpeed = mContentView.findViewById(R.id.layout_speed);
        ConstraintLayout.LayoutParams speedParams = (ConstraintLayout.LayoutParams) layoutSpeed.getLayoutParams();
        if (DisplayUtils.isFullScreenDevice(mActivity)) {
            speedParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp20);
        } else {
            speedParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp100);
        }
        layoutSpeed.setLayoutParams(speedParams);

        mVideoCutViewBar = mContentView.findViewById(R.id.video_crop_view_bar);
        if (mVideoPath != null) {
            mVideoCutViewBar.setVideoPath(mVideoPath);
        }
        mVideoCutViewBar.setOnVideoCropViewBarListener(mOnVideoCropViewBarListener);
        ConstraintLayout.LayoutParams cutViewBarParams = (ConstraintLayout.LayoutParams) mVideoCutViewBar.getLayoutParams();
        if (DisplayUtils.isFullScreenDevice(mActivity)) {
            cutViewBarParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp70);
        } else {
            cutViewBarParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp20);
        }
        mVideoCutViewBar.setLayoutParams(cutViewBarParams);

        mLayoutProgress = mContentView.findViewById(R.id.layout_progress);
        mLayoutProgress.setVisibility(View.GONE);
        mCvCropProgress = mLayoutProgress.findViewById(R.id.cv_crop_progress);
        mTvCropProgress = mLayoutProgress.findViewById(R.id.tv_crop_progress);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        initMediaPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        if (mVideoCutViewBar != null) {
            mVideoCutViewBar.release();
            mVideoCutViewBar = null;
        }
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
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.video_crop_back) {
            mActivity.onBackPressed();
        } else if (id == R.id.video_crop_ok) {
            cutVideo();
        } else if (id == R.id.tv_video_cut_speed_bar_visible) {
            if (mVideoSpeedLevelBar.getVisibility() == View.VISIBLE) {
                mVideoSpeedLevelBar.setVisibility(View.GONE);
            } else {
                mVideoSpeedLevelBar.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.tv_video_cut_rotation) {
            rotateVideo();
        }
    }

    /**
     * 旋转视频
     */
    private boolean mRotating;
    private float mCurrentRotate;
    private void rotateVideo() {
        if (mRotating) {
            return;
        }
        // 原始宽高
        final int width = mVideoPlayerView.getWidth();
        final int height = mVideoPlayerView.getHeight();

        // 添加旋转动画
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(400);
        animator.addUpdateListener(animation -> {
            float rotate = (float)animation.getAnimatedValue() * 90;
            // 设置旋转矩阵
            setupMatrix(width, height, (int) (mCurrentRotate + rotate));
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                mRotating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                mRotating = false;
                mCurrentRotate += 90;
            }
        });
        animatorSet.playSequentially(animator);
        animatorSet.start();
    }

    private void setupMatrix(int width, int height, int degree) {
        Matrix matrix = new Matrix();
        RectF src = new RectF(0, 0, width, height);
        RectF dst = new RectF(0, 0, width, height);
        RectF screen = new RectF(dst);
        matrix.postRotate(degree, screen.centerX(), screen.centerY());
        matrix.mapRect(dst);

        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
        matrix.mapRect(src);

        matrix.setRectToRect(screen, src, Matrix.ScaleToFit.CENTER);
        matrix.postRotate(degree, screen.centerX(), screen.centerY());
        mVideoPlayerView.setTransform(matrix);
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
        if (mVideoCutViewBar != null) {
            mVideoCutViewBar.setVideoPath(mVideoPath);
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

    private void initMediaPlayer() {
        if (mCainMediaPlayer == null) {
            mCainMediaPlayer = new CainMediaPlayer();
        }
    }

    private void openMediaPlayer() {
        mContentView.setKeepScreenOn(true);
        mCainMediaPlayer.setOnPreparedListener(mp -> {
            mVideoDuration = mCainMediaPlayer.getDuration();
            mp.start();
        });
        mCainMediaPlayer.setOnVideoSizeChangedListener((mediaPlayer, width, height) -> {
            if (mediaPlayer.getRotate() % 180 != 0) {
                mVideoPlayerView.setVideoSize(height, width);
            } else {
                mVideoPlayerView.setVideoSize(width, height);
            }
        });
        mCainMediaPlayer.setOnCompletionListener(mp -> {
            Log.d(TAG, "openMediaPlayer: onComplete");
        });

        mCainMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.d(TAG, "onError: what = " + what + ", extra = " + extra);
            return false;
        });

        mCainMediaPlayer.setOnSeekCompleteListener(mp -> {
            mSeeking = false;
        });

        mCainMediaPlayer.setOnCurrentPositionListener((mp, current, duration) -> {
            if (!mSeeking) {
                if (current > (mCutRange + mCutStart) * mVideoSpeed.getSpeed()) {
                    mCainMediaPlayer.seekTo(mCutStart * mVideoSpeed.getSpeed());
                    mSeeking = true;
                }
            }
        });

        try {
            mCainMediaPlayer.setDataSource(mVideoPath);
            if (mSurfaceTexture != null) {
                if (mSurface != null) {
                    mSurface.release();
                }
                mSurface = new Surface(mSurfaceTexture);
                mCainMediaPlayer.setSurface(mSurface);
            }
            mCainMediaPlayer.setOption(CainMediaPlayer.OPT_CATEGORY_PLAYER, "vcodec", "h264_mediacodec");
            mCainMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCainMediaPlayer.start();
    }


    private VideoCutViewBar.OnVideoCropViewBarListener mOnVideoCropViewBarListener = new VideoCutViewBar.OnVideoCropViewBarListener() {
        @Override
        public void onTouchDown() {
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.pause();
            }
        }

        @Override
        public void onTouchUp() {
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.resume();
            }
        }

        @Override
        public void onTouchChange(long time) {
            mCutStart = time;
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.seekTo(mCutStart);
            }
        }

        @Override
        public void onRangeChange(long time, long range) {
            mCutStart = time;
            mCutRange = range;
            if (mTextVideoCropSelected != null) {
                mTextVideoCropSelected.setText(mActivity.getString(R.string.video_crop_selected_time, (int)(range/1000L)));
            }
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.seekTo(mCutStart);
            }
        }

        @Override
        public void onError(String error) {
            Log.d(TAG, "onError: " + error);
        }
    };

    /**
     * 剪辑视频
     */
    private void cutVideo() {
        mLayoutProgress.setVisibility(View.VISIBLE);
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.pause();
        }
        if (mMediaEditor == null) {
            mMediaEditor = new CainMediaEditor();
        }

        float start = mVideoSpeed.getSpeed() * mCutStart;
        float duration = mVideoSpeed.getSpeed() * mCutRange;
        if (duration > mVideoDuration) {
            duration = mVideoDuration;
        }
        String videoPath = VideoEditorUtil.createPathInBox(mActivity, "mp4");
        mMediaEditor.videoSpeedCut(mVideoPath, videoPath, start, duration, mVideoSpeed.getSpeed(),
                new CainMediaEditor.OnEditProcessListener() {
                    @Override
                    public void onProcessing(int percent) {
                        mActivity.runOnUiThread(() -> {
                            mCvCropProgress.setProgress(percent);
                            mTvCropProgress.setText(percent + "%");
                        });
                    }

                    @Override
                    public void onSuccess() {
                        mActivity.runOnUiThread(() -> {
                            mLayoutProgress.setVisibility(View.GONE);
                            // 成功则释放播放器并跳转至编辑页面
                            if (FileUtils.fileExists(videoPath)) {
                                if (mMediaEditor != null) {
                                    mMediaEditor.release();
                                    mMediaEditor = null;
                                }
                                Intent intent = new Intent(mActivity, VideoEditActivity.class);
                                intent.putExtra(VideoEditActivity.VIDEO_PATH, videoPath);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onError(String msg) {
                        mActivity.runOnUiThread(() -> {
                            Toast.makeText(mActivity, "processing error：" + msg, Toast.LENGTH_SHORT).show();
                            if (mCainMediaPlayer != null) {
                                mCainMediaPlayer.start();
                            }
                        });
                    }
                });
    }
}
