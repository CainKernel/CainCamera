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
import com.cgfay.media.CAVMediaPlayer;
import com.cgfay.uitls.utils.DisplayUtils;
import com.cgfay.uitls.utils.StringUtils;

import java.io.IOException;


public class VideoPlayerFragment  extends Fragment {

    private static final String TAG = "VideoPlayerFragment";

    private static final String PATH = "PATH";

    private View mContentView;
    private SurfaceView mSurfaceView;
    private TextView mBtnPlay;
    private SeekBar mSpeedBar;
    private SeekBar mProgressBar;
    private TextView mTextPath;
    private TextView mCurrentPosition;

    private volatile boolean mSeeking;
    private CAVMediaPlayer mVideoPlayer;
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
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mVideoPlayer != null) {
            mVideoPlayer.stop();
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
        super.onDestroy();
    }

    private void initView(@NonNull View rootView) {
        mSurfaceView = rootView.findViewById(R.id.video_view);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("CainMedia", "surfaceCreated: ");
                if (mVideoPlayer != null) {
                    mVideoPlayer.setSurface(holder.getSurface());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("CainMedia", "surfaceChanged: ");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("CainMedia", "surfaceDestroyed: ");
                if (mVideoPlayer != null) {
                    mVideoPlayer.setSurface(null);
                }
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
                if (mVideoPlayer != null) {
                    mVideoPlayer.pause();
                    mVideoPlayer.setDecodeOnPause(true);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVideoPlayer != null) {
                    mVideoPlayer.start();
                    mVideoPlayer.setDecodeOnPause(false);
                }
            }
        });

        mTextPath = rootView.findViewById(R.id.tv_path);
        Bundle bundle = getArguments();
        if (bundle  != null) {
            String path = bundle.getString(PATH);
            if (!TextUtils.isEmpty(path)) {
                mTextPath.setText(path);
                initPlayer(path);
            }
        }
        mCurrentPosition = rootView.findViewById(R.id.tv_current_position);
    }

    private void initPlayer(@NonNull String path) {
        mVideoPlayer = new CAVMediaPlayer();
        mVideoPlayer.setSpeed(1.0f);
        mVideoPlayer.setLooping(true);
        mVideoPlayer.setOnPreparedListener(mp -> {
            Log.d(TAG, "onPrepared: ");
            if (getContext() != null) {
                ViewGroup.LayoutParams params = mSurfaceView.getLayoutParams();
                params.width = DisplayUtils.getScreenWidth(getContext());
                params.height = mVideoPlayer.getVideoHeight() * params.width / mVideoPlayer.getVideoWidth();
                Log.d(TAG, "onPlaying: width " + params.width + ", height " + params.height);
                mSurfaceView.setLayoutParams(params);
                mProgressBar.setMax((int) mVideoPlayer.getDuration());
            }
        });
        mVideoPlayer.setOnCurrentPositionListener((mp, current, duration) -> {
            if (mSeeking) {
                return;
            }
            mProgressBar.setProgress((int) current);
            mCurrentPosition.setText(StringUtils.generateStandardTime((int)current));
        });
        mVideoPlayer.setOnCompletionListener(mp -> Log.d(TAG, "onCompletion: "));
        mVideoPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.d(TAG, "onError: what - " + what + ", extra - " + extra);
            return false;
        });
        mVideoPlayer.setOnSeekCompleteListener(mp -> {
            Log.d(TAG, "onSeekComplete: ");
            mHandler.postDelayed(()-> {
                mSeeking = false;
            }, 100);
        });
        mVideoPlayer.setSurface(mSurfaceView.getHolder().getSurface());
        try {
            mVideoPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mVideoPlayer.prepare();
    }

    private void seekTo(float progress) {
        mHandler.post(() -> {
            if (mVideoPlayer != null) {
                Log.d(TAG, "seekTo: " + progress);
                mSeeking = true;
                mVideoPlayer.seekTo(progress);
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
