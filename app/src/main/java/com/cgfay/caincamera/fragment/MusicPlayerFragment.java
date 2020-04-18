package com.cgfay.caincamera.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cgfay.caincamera.R;
import com.cgfay.media.CAVAudioPlayer;

import java.io.IOException;

public class MusicPlayerFragment extends Fragment {

    private static final String TAG = "MusicPlayerFragment";

    private static final String PATH = "PATH";

    private View mContentView;
    private TextView mBtnPlay;
    private SeekBar mSpeedBar;
    private SeekBar mProgressBar;
    private TextView mTextPath;
    private CAVAudioPlayer mMusicPlayer;

    private Handler mHandler;

    public static MusicPlayerFragment newInstance(@NonNull String path) {
        MusicPlayerFragment fragment = new MusicPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PATH, path);
        fragment.setArguments(bundle);
        return fragment;
    }

    public MusicPlayerFragment() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_music_player, container, false);
        initView(mContentView);
        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMusicPlayer != null) {
            mMusicPlayer.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMusicPlayer != null) {
            mMusicPlayer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMusicPlayer != null) {
            mMusicPlayer.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMusicPlayer != null) {
            mMusicPlayer.stop();
        }
    }

    @Override
    public void onDestroy() {
        if (mMusicPlayer != null) {
            mMusicPlayer.release();
            mMusicPlayer = null;
        }
        super.onDestroy();
    }

    private void initView(@NonNull View rootView) {
        mBtnPlay = rootView.findViewById(R.id.btn_stop_play);
        mBtnPlay.setOnClickListener(v -> {
            if (mMusicPlayer != null) {
                if (mMusicPlayer.isPlaying()) {
                    mMusicPlayer.pause();
                    mBtnPlay.setText("play");
                } else {
                    mMusicPlayer.start();
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
        mMusicPlayer = new CAVAudioPlayer();
        mMusicPlayer.setSpeed(1.0f);
        mMusicPlayer.setLooping(true);
        mMusicPlayer.setOnCurrentPositionListener((mp, current, duration) -> {
            final int progress = (int)(100.0f * current / duration);
            mHandler.post(() -> {
                if (mProgressBar != null) {
                    mProgressBar.setProgress(progress);
                }
            });
        });
        mMusicPlayer.setOnCompletionListener(mp -> {
            Log.d(TAG, "onCompletion: ");
        });
        mMusicPlayer.setOnSeekCompleteListener(mp -> {
            Log.d(TAG, "onSeekComplete: ");
        });
        mMusicPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.d(TAG, "onError: " + what + ", extra - " + extra);
            return true;
        });
        try {
            mMusicPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMusicPlayer.prepare();
    }

    private void seekTo(float progress) {
        mHandler.post(() -> {
            if (mMusicPlayer != null) {
                float pts = progress / 100.0f * mMusicPlayer.getDuration();
                Log.d(TAG, "seekTo: " + pts + ", duration: " + mMusicPlayer.getDuration());
                if (pts <= mMusicPlayer.getDuration()) {
                    mMusicPlayer.seekTo(pts);
                } else {
                    mMusicPlayer.seekTo(0);
                }
            }
        });
    }

    private void setSpeed(float speed) {
        Log.d(TAG, "setSpeed: " + speed);
        if (mMusicPlayer != null) {
            mMusicPlayer.setSpeed(speed);
        }
    }
}
