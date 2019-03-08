package com.cgfay.video.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.cgfay.media.CainMediaPlayer;
import com.cgfay.media.CainShortVideoEditor;
import com.cgfay.media.IMediaPlayer;
import com.cgfay.utilslibrary.utils.FileUtils;
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
    private long mCropStart = 0;
    private long mCropRange = 15000;
    private long mVideoDuration;
    private CainMediaPlayer mCainMediaPlayer;
    private AudioManager mAudioManager;
    private CainShortVideoEditor mVideoEditor;

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
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        mContentView.findViewById(R.id.video_crop_back).setOnClickListener(this);
        mContentView.findViewById(R.id.video_crop_ok).setOnClickListener(this);

        mVideoPlayerView = mContentView.findViewById(R.id.video_player_view);
        mVideoPlayerView.setSurfaceTextureListener(mSurfaceTextureListener);

        mVideoSpeedLevelBar = mContentView.findViewById(R.id.video_crop_speed_bar);
        mVideoSpeedLevelBar.setOnSpeedChangedListener(new VideoSpeedLevelBar.OnSpeedChangedListener() {
            @Override
            public void onSpeedChanged(VideoSpeed speed) {
                if (mCainMediaPlayer != null) {
                    mVideoSpeed = speed;
                    float rate = speed.getSpeed();
                    float pitch = 1.0f / rate;
                    mCainMediaPlayer.setRate(rate);
                    mCainMediaPlayer.setPitch(pitch);
                    mCainMediaPlayer.seekTo(mCropStart);
                    if (mVideoCutViewBar != null) {
                        mVideoCutViewBar.setSpeed(mVideoSpeed);
                    }
                }
            }
        });

        mTextVideoCropSelected = mContentView.findViewById(R.id.tv_video_cut_selected);
        mContentView.findViewById(R.id.tv_video_cut_speed_bar_visible).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_video_cut_rotation).setOnClickListener(this);


        mVideoCutViewBar = mContentView.findViewById(R.id.video_crop_view_bar);
        if (mVideoPath != null) {
            mVideoCutViewBar.setVideoPath(mVideoPath);
        }
        mVideoCutViewBar.setOnVideoCropViewBarListener(mOnVideoCropViewBarListener);

        mLayoutProgress = mContentView.findViewById(R.id.layout_progress);
        mLayoutProgress.setVisibility(View.GONE);
        mCvCropProgress = mLayoutProgress.findViewById(R.id.cv_crop_progress);
        mTvCropProgress = mLayoutProgress.findViewById(R.id.tv_crop_progress);
    }

    @Override
    public void onStart() {
        super.onStart();
        initMediaPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        openMediaPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
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
            // TODO it has a bug here, fix it in future.
//            float nextRotation = mVideoPlayerView.getRotation() + 90f;
//            mVideoPlayerView.setRotation(nextRotation);
        }
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
        mCainMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                mVideoDuration = mCainMediaPlayer.getDuration();
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
            if (mSurfaceTexture != null) {
                if (mSurface == null) {
                    mSurface = new Surface(mSurfaceTexture);
                }
                mCainMediaPlayer.setSurface(mSurface);
            }
            mCainMediaPlayer.setOption(CainMediaPlayer.OPT_CATEGORY_PLAYER, "vcodec", "h264_mediacodec");
            mCainMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            mCropStart = time;
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.seekTo(mCropStart);
            }
        }

        @Override
        public void onRangeChange(long time, long range) {
            mCropStart = time;
            mCropRange = range;
            if (mTextVideoCropSelected != null) {
                mTextVideoCropSelected.setText(mActivity.getString(R.string.video_crop_selected_time, (int)(range/1000L)));
            }
            if (mCainMediaPlayer != null) {
                mCainMediaPlayer.seekTo(mCropStart);
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
        // TODO crop video
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCainMediaPlayer != null) {
                    mCainMediaPlayer.pause();
                }
                if (mVideoEditor == null) {
                    mVideoEditor = new CainShortVideoEditor();
                }
                mVideoEditor.setOnVideoEditorProcessListener(mProcessListener);

                float start = mVideoSpeed.getSpeed() * mCropStart;
                float duration = mVideoSpeed.getSpeed() * mCropRange;
                if (duration > mVideoDuration) {
                    duration = mVideoDuration;
                }
                // TODO videoCutSpeed方法是通过native层来实现的，但目前倍速时音频部分还没想好怎么处理才不会出现杂音问题，暂时先弄完后面的编辑页面再回来弄这个
                String videoPath = CainShortVideoEditor.VideoEditorUtil.createPathInBox("mp4");
                int ret = mVideoEditor.videoCutSpeed(mVideoPath, videoPath, start, duration, mVideoSpeed.getSpeed());
                // 成功则释放播放器并跳转至编辑页面
                if (ret == 0 && FileUtils.fileExists(videoPath)) {
                    // 需要释放销毁播放器，后面要用到播放器，防止内存占用过大
                    if (mCainMediaPlayer != null) {
                        mCainMediaPlayer.stop();
                        mCainMediaPlayer.release();
                        mCainMediaPlayer = null;
                    }
                    if (mSurface != null) {
                        mSurface.release();
                        mSurface = null;
                    }
                    if (mVideoEditor != null) {
                        mVideoEditor.release();
                        mVideoEditor = null;
                    }
                    Intent intent = new Intent(mActivity, VideoEditActivity.class);
                    intent.putExtra(VideoEditActivity.VIDEO_PATH, videoPath);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "video cut's error!");
                    if (mCainMediaPlayer != null) {
                        mCainMediaPlayer.start();
                    }
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLayoutProgress.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private CainShortVideoEditor.OnVideoEditorProcessListener mProcessListener = new CainShortVideoEditor.OnVideoEditorProcessListener() {

        @Override
        public void onProcessing(int time) {
            Log.d(TAG, "onProcessing: time = " + time + "s" + ", duration = " + mVideoDuration);
            if (mVideoSpeed.getSpeed() != 1.0) {
                float percent = time * 1000f / mCropRange * 100;
                if (percent > 100) {
                    percent = 100;
                }
                mCvCropProgress.setProgress(percent);
                mTvCropProgress.setText(percent + "%");
            }
        }

        @Override
        public void onError() {
            Toast.makeText(mActivity, "processing error", Toast.LENGTH_SHORT).show();
        }
    };
}
