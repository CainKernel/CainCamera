package com.cgfay.caincamera.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cgfay.caincamera.R;
import com.cgfay.media.VideoPlayer;


public class VideoPlayerFragment  extends Fragment {

    private static final String TAG = "VideoPlayerFragment";

    private static final String PATH = "PATH";

    private View mContentView;
    private SurfaceView mSurfaceView;
    private TextView mBtnPlay;
    private SeekBar mSpeedBar;
    private SeekBar mProgressBar;
    private TextView mTextPath;

    private VideoPlayer mVideoPlayer;
    private Handler mHandler;

    public static VideoPlayerFragment newInstance(@NonNull String path) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PATH, path);
        fragment.setArguments(bundle);
        return fragment;
    }

    public VideoPlayerFragment() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_video_player, container, false);
        initView(mContentView);
        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mVideoPlayer != null) {
            mVideoPlayer.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoPlayer != null) {
            mVideoPlayer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mVideoPlayer != null) {
            mVideoPlayer.stop();
        }
    }

    @Override
    public void onDestroy() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
        super.onDestroy();
    }

    private void initView(@NonNull View rootView) {
        mSurfaceView = rootView.findViewById(R.id.video_view);
        ViewGroup.LayoutParams params = mSurfaceView.getLayoutParams();
        params.width = 1080;
        params.height = 853;
        mSurfaceView.setLayoutParams(params);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mVideoPlayer != null) {
                    mVideoPlayer.setSurface(holder.getSurface());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        mBtnPlay = rootView.findViewById(R.id.btn_stop_play);
        mBtnPlay.setOnClickListener(v -> {
            if (mVideoPlayer != null) {
                if (mVideoPlayer.isPlaying()) {
                    mVideoPlayer.pause();
                    mBtnPlay.setText("play");
                } else {
                    mVideoPlayer.start();
                    mBtnPlay.setText("pause");
                }
            }
        });

        mSpeedBar = rootView.findViewById(R.id.player_speed);
        mSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = 1.0f;
                    if (progress > 50f) {
                        speed = 1.0f + (progress - 50) / 50f;
                    } else {
                        speed = 0.5f + progress / 100f;
                    }
                    setSpeed(speed);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mProgressBar = rootView.findViewById(R.id.player_progress);
        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTextPath = rootView.findViewById(R.id.tv_path);
        Bundle bundle = getArguments();
        String path = bundle.getString(PATH);
        if (!TextUtils.isEmpty(path)) {
            mTextPath.setText(path);
            initPlayer(path);
        }
    }

    private void initPlayer(@NonNull String path) {
        mVideoPlayer = new VideoPlayer();
        mVideoPlayer.setDataSource(path);
        mVideoPlayer.setSpeed(1.0f);
        mVideoPlayer.setLooping(true);
        mVideoPlayer.setOnPlayListener(new VideoPlayer.OnPlayListener() {
            @Override
            public void onPlaying(float pts) {

            }

            @Override
            public void onSeekComplete() {

            }

            @Override
            public void onCompletion() {

            }

            @Override
            public void onError(int errorCode, String msg) {

            }
        });
        mVideoPlayer.setSurface(mSurfaceView.getHolder().getSurface());
    }

    private void seekTo(float progress) {
        mHandler.post(() -> {
            if (mVideoPlayer != null) {
                float pts = progress / 100.0f * mVideoPlayer.getDuration();
                Log.d(TAG, "seekTo: " + pts + ", duration: " + mVideoPlayer.getDuration());
                if (pts <= mVideoPlayer.getDuration()) {
                    mVideoPlayer.seekTo(pts);
                } else {
                    mVideoPlayer.seekTo(0);
                }
            }
        });
    }

    private void setSpeed(float speed) {
        Log.d(TAG, "setSpeed: " + speed);
        if (mVideoPlayer != null) {
            mVideoPlayer.setSpeed(speed);
        }
    }
}
