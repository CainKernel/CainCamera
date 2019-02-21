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
import android.widget.TextView;
import android.widget.Toast;


import com.cgfay.media.CainMediaPlayer;
import com.cgfay.media.CainShortVideoEditor;
import com.cgfay.media.IMediaPlayer;
import com.cgfay.utilslibrary.fragment.BackPressedDialogFragment;
import com.cgfay.video.R;
import com.cgfay.video.activity.VideoEditActivity;
import com.cgfay.video.bean.VideoSpeed;
import com.cgfay.video.widget.VideoCropViewBar;
import com.cgfay.video.widget.VideoSpeedLevelBar;
import com.cgfay.video.widget.VideoTextureView;

import java.io.IOException;

public class VideoCropFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "VideoCropFragment";

    private String mVideoPath;
    private Activity mActivity;

    private View mContentView;
    // 播放控件
    private VideoTextureView mVideoPlayerView;
    // 倍速选择条
    private VideoSpeedLevelBar mVideoSpeedLevelBar;
    // 裁剪Bar
    private VideoCropViewBar mVideoCropViewBar;
    // 选中提示
    private TextView mTextVideoCropSelected;
    // 显示播放倍速
    private TextView mTextSpeedBarVisible;
    // 设置旋转角度
    private TextView mTextVideoRotation;

    private VideoSpeed mVideoSpeed = VideoSpeed.SPEED_L2;

    // 毫秒
    private long mCropStart = 0;
    private long mCropRange = 15000;
    private CainMediaPlayer mCainMediaPlayer;
    private AudioManager mAudioManager;
    private CainShortVideoEditor mVideoEditor;

    public static VideoCropFragment newInstance() {
        return new VideoCropFragment();
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
                mVideoSpeed = speed;
                float rate = speed.getSpeed();
                float pitch = 1.0f / rate;
                mCainMediaPlayer.setRate(rate);
                mCainMediaPlayer.setPitch(pitch);
                mCainMediaPlayer.seekTo(mCropStart);
            }
        });

        mTextVideoCropSelected = mContentView.findViewById(R.id.video_crop_selected);
        mTextSpeedBarVisible = mContentView.findViewById(R.id.video_crop_speed_bar_visible);
        mTextSpeedBarVisible.setOnClickListener(this);
        mTextVideoRotation = mContentView.findViewById(R.id.video_crop_rotation);
        mTextVideoRotation.setOnClickListener(this);


        mVideoCropViewBar = mContentView.findViewById(R.id.video_crop_view_bar);
        if (mVideoPath != null) {
            mVideoCropViewBar.setVideoPath(mVideoPath);
        }
        mVideoCropViewBar.setOnVideoCropViewBarListener(mOnVideoCropViewBarListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        initMediaPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCainMediaPlayer != null) {
            mCainMediaPlayer.pause();
        }
    }

    public void onBackPressed() {
        new BackPressedDialogFragment().show(getChildFragmentManager(), "");
    }

    @Override
    public void onDestroyView() {
        if (mVideoCropViewBar != null) {
            mVideoCropViewBar.release();
            mVideoCropViewBar = null;
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
            onBackPressed();
        } else if (id == R.id.video_crop_ok) {
            cropVideo();
        } else if (id == R.id.video_crop_speed_bar_visible) {
            if (mVideoSpeedLevelBar.getVisibility() == View.VISIBLE) {
                mVideoSpeedLevelBar.setVisibility(View.GONE);
                mTextSpeedBarVisible.setText(R.string.video_crop_speed_bar_visible);
            } else {
                mVideoSpeedLevelBar.setVisibility(View.VISIBLE);
                mTextSpeedBarVisible.setText(R.string.video_crop_speed_bar_gone);
            }
        } else if (id == R.id.video_crop_rotation) {
            // TODO it has a bug here, fix it in future.
//            float nextRotation = mVideoPlayerView.getRotation() + 90f;
//            mVideoPlayerView.setRotation(nextRotation);
        }
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
        if (mVideoCropViewBar != null) {
            mVideoCropViewBar.setVideoPath(mVideoPath);
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
                mp.start();
            }
        });
        mCainMediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mediaPlayer, int width, int height) {
                mVideoPlayerView.setVideoSize(width, height);
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
            mCainMediaPlayer.setOption(CainMediaPlayer.OPT_CATEGORY_PLAYER, "vcodec", "h264_mediacodec");
            mCainMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private VideoCropViewBar.OnVideoCropViewBarListener mOnVideoCropViewBarListener = new VideoCropViewBar.OnVideoCropViewBarListener() {
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
     * 裁剪视频
     */
    private void cropVideo() {
        // TODO crop video
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCainMediaPlayer != null) {
                    mCainMediaPlayer.pause();
                }
                if (mVideoEditor == null) {
                    mVideoEditor = new CainShortVideoEditor();
                    mVideoEditor.setOnVideoEditorProcessListener(mProcessListener);
                }
                Log.d(TAG, "run: start = " + mCropStart + "duration = " + mCropRange);
                String outPath = mVideoEditor.videoCut(mVideoPath, mCropStart / 1000f, mCropRange / 1000f);
                // 成功则直接退出播放器，并跳转至编辑页面
                if (outPath != null) {
                    if (mCainMediaPlayer != null) {
                        mCainMediaPlayer.reset();
                        mCainMediaPlayer = null;
                    }
                    mVideoEditor.release();
                    mVideoEditor = null;
                    Intent intent = new Intent(mActivity, VideoEditActivity.class);
                    intent.putExtra(VideoEditActivity.PATH, outPath);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "video cut's error!");
                    if (mCainMediaPlayer != null) {
                        mCainMediaPlayer.start();
                    }
                }
            }
        }).start();
    }

    private CainShortVideoEditor.OnVideoEditorProcessListener mProcessListener = new CainShortVideoEditor.OnVideoEditorProcessListener() {

        @Override
        public void onProcessing(int time) {
            Log.d(TAG, "onProcessing: time = " + time + "s");
        }

        @Override
        public void onError() {
            Toast.makeText(mActivity, "processing error", Toast.LENGTH_SHORT).show();
        }
    };
}
