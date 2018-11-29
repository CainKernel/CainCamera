package com.cgfay.ffmpeglibrary.activity;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.ffmpeglibrary.Metadata.AVMediaMetadataRetriever;
import com.cgfay.ffmpeglibrary.R;
import com.cgfay.ffmpeglibrary.MusicPlayer.AVMusicPlayer;
import com.cgfay.utilslibrary.utils.StringUtils;

/**
 * 音乐播放器测试页面
 */
/**
 * 音乐播放器测试页面
 */
public class AVMusicPlayerActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener  {

    public static final String PATH = "Path";
    private static final String TAG = "MusicPlayActivity";
    private String mPath;

    private AVMusicPlayer mMusicPlayer;

    private TextView mTvCurrent;
    private TextView mTvDuration;

    private SeekBar mSeekProcess;

    private Button mBtnPlay;

    private Button mBtnReset;

    private SeekBar mSbSpeed;
    private SeekBar mSbPitch;
    private SeekBar mSbTempo;
    private SeekBar mSbPitchOctaves;
    private SeekBar mSbPitchSemiTones;

    private TextView mTvMusicValue;
    private TextView mTvMetadata;
    private ImageView mIvCover;

    private int mCurrent = 0;
    private int mDuration = 0;
    private float mSpeed = 1.0f;
    private float mPitch = 1.0f;
    private float mTempo = 1.0f;
    private float mPitchOctaves = 0.0f;
    private float mPitchSemiTones = 0.0f;

    private Handler mMainHandler;

    private AVMediaMetadataRetriever mMetadataRetriever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        mPath = getIntent().getStringExtra(PATH);
        mMainHandler = new Handler(Looper.getMainLooper());
        initView();
        initPlayer();
        initMediaMetadataRetriever();
    }

    public void initView() {

        mTvCurrent = (TextView) findViewById(R.id.tv_current);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);
        mSeekProcess = (SeekBar) findViewById(R.id.play_progress);
        mSeekProcess.setOnSeekBarChangeListener(this);

        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);
        mBtnReset = (Button) findViewById(R.id.btn_reset);
        mBtnReset.setOnClickListener(this);

        mTvMetadata = findViewById(R.id.tv_metadata);
        mIvCover = findViewById(R.id.iv_cover);

        mTvMusicValue = (TextView) findViewById(R.id.tv_music_value);
        updateMusicTextView();
        mSbSpeed = (SeekBar) findViewById(R.id.sb_speed);
        mSbPitch = (SeekBar) findViewById(R.id.sb_pitch);
        mSbTempo = (SeekBar) findViewById(R.id.sb_tempo);
        mSbPitchOctaves = (SeekBar) findViewById(R.id.sb_pitch_octaves);
        mSbPitchSemiTones = (SeekBar) findViewById(R.id.sb_pitch_semitones);

        mSbSpeed.setOnSeekBarChangeListener(this);
        mSbSpeed.setMax(100);
        mSbSpeed.setProgress(50);

        mSbPitch.setOnSeekBarChangeListener(this);
        mSbPitch.setMax(100);
        mSbPitch.setProgress(50);

        mSbTempo.setOnSeekBarChangeListener(this);
        mSbTempo.setMax(100);
        mSbTempo.setProgress(50);

        mSbPitchOctaves.setOnSeekBarChangeListener(this);
        mSbPitchOctaves.setMax(100);
        mSbPitchOctaves.setProgress(50);

        mSbPitchSemiTones.setOnSeekBarChangeListener(this);
        mSbPitchSemiTones.setMax(100);
        mSbPitchSemiTones.setProgress(50);
    }


    private void initPlayer() {
        if (TextUtils.isEmpty(mPath)) {
            return;
        }
        mMusicPlayer = new AVMusicPlayer();
        mMusicPlayer.setDataSource(mPath);
        mMusicPlayer.setOnPreparedListener(new AVMusicPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                mMusicPlayer.start();
            }
        });
        mMusicPlayer.setOnCurrentInfoListener(new AVMusicPlayer.OnCurrentInfoListener() {
            @Override
            public void onCurrentInfo(final int current, final int duration) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCurrent = current;
                        mDuration = duration;
                        mTvCurrent.setText(StringUtils.generateStandardTime(mCurrent * 1000));
                        mTvDuration.setText(StringUtils.generateStandardTime(mDuration * 1000));
                        mSeekProcess.setMax(mDuration);
                        mSeekProcess.setProgress(mCurrent);
                        if (mMusicPlayer != null) {
                            if (mMusicPlayer.isPlaying()) {
                                mBtnPlay.setText("暂停");
                            } else {
                                mBtnPlay.setText("播放");
                            }
                        }
                    }
                });
            }
        });

        mMusicPlayer.setOnErrorListener(new AVMusicPlayer.OnErrorListener() {
            @Override
            public void onError(int code, String msg) {
                Log.d(TAG, "onError: code = " + code + ", msg = " + msg);
            }
        });
        mMusicPlayer.setOnCompletionListener(new AVMusicPlayer.OnCompletionListener() {
            @Override
            public void onCompleted() {

            }
        });
        mMusicPlayer.setOnVolumeDBListener(new AVMusicPlayer.OnVolumeDBListener() {
            @Override
            public void onVolumeDB(int db) {
                Log.d(TAG, "onDBValue: db = " + db);
            }
        });
        mMusicPlayer.setLooping(true);
        mMusicPlayer.prepare();
    }

    private void initMediaMetadataRetriever() {
        if (TextUtils.isEmpty(mPath)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 异步截屏回调
                mMetadataRetriever = new AVMediaMetadataRetriever();
                mMetadataRetriever.setDataSource(mPath);
                final Bitmap bitmap = mMetadataRetriever.getCoverPicture();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvCover.setImageBitmap(bitmap);
                        mTvMetadata.setText(mMetadataRetriever.getMetadata().toString());
                    }
                });
            }
        }).start();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mMusicPlayer != null) {
            mMusicPlayer.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMusicPlayer != null) {
            mMusicPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMusicPlayer != null) {
            mMusicPlayer.stop();
            mMusicPlayer.release();
            mMusicPlayer = null;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mMusicPlayer == null) {
            return;
        }
        if (fromUser) {
            int id = seekBar.getId();
            if (id == R.id.play_progress) {
                seekBar.setMax(mMusicPlayer.getDuration());
                mMusicPlayer.seekTo(progress);
            } else if (id == R.id.sb_speed) {
                // 0.5 ~ 2.0
                if (progress < 50) {
                    mSpeed  = 0.5f + (float) progress / 100f;
                } else {
                    mSpeed = 1.0f + (float) (progress - 50) * 2 / 100f;
                }
            } else if (id == R.id.sb_pitch) {
                // 0.5 ~ 2.0
                if (progress < 50) {
                    mPitch = 0.5f + (float) progress / 100f;
                } else {
                    mPitch = 1.0f + (float) (progress - 50) * 2 / 100;
                }
            } else if (id == R.id.sb_tempo) {
                // 0.5 ~ 2.0
                if (progress < 50) {
                    mTempo = 0.5f + (float) progress / 100f;
                } else {
                    mTempo = 1.0f + (float) (progress - 50) * 2 / 100;
                }
            } else if (id == R.id.sb_pitch_octaves) {
                // -1 ~ 1
                mPitchOctaves =  2 * (((float)progress / 100f) - 0.5f);
            } else if (id == R.id.sb_pitch_semitones) {
                // -12 ~ 12
                mPitchSemiTones = 24 * (((float)progress / 100f) - 0.5f);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        if (id == R.id.sb_speed) {
            mMusicPlayer.setSpeed(mSpeed);
        } else if (id == R.id.sb_pitch) {
            mMusicPlayer.setPitch(mPitch);
        } else if (id == R.id.sb_tempo) {
            mMusicPlayer.setTempo(mTempo);
        } else if (id == R.id.sb_pitch_octaves) {
            mMusicPlayer.setPitchOctaves(mPitchOctaves);
        } else if (id == R.id.sb_pitch_semitones) {
            mMusicPlayer.setPitchSemiTones(mPitchSemiTones);
        }
        updateMusicTextView();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_reset) {
            mSpeed = 1.0f;
            mPitch = 1.0f;
            mTempo = 1.0f;
            mPitchOctaves = 0.0f;
            mPitchSemiTones = 0.0f;
            mMusicPlayer.setSpeed(mSpeed);
            mMusicPlayer.setPitch(mPitch);
            mMusicPlayer.setTempo(mTempo);
            mMusicPlayer.setPitchOctaves(mPitchOctaves);
            mMusicPlayer.setPitchSemiTones(mPitchSemiTones);

            mSbSpeed.setProgress(50);
            mSbPitch.setProgress(50);
            mSbTempo.setProgress(50);
            mSbPitchOctaves.setProgress(50);
            mSbPitchSemiTones.setProgress(50);

            updateMusicTextView();
        } else if (view.getId() == R.id.btn_play) {
            // 没有播放
            if (!mMusicPlayer.isPlaying()) {
                if (!mMusicPlayer.isPrepared()) {
                    mMusicPlayer.prepare();
                } else {
                    mMusicPlayer.resume();
                }
                mBtnPlay.setText("暂停");
            } else { // 处于播放状态
                mMusicPlayer.pause();
                mBtnPlay.setText("播放");
            }
        }
    }

    private void updateMusicTextView() {
        String value = "速度: " + mSpeed + "    "
                + "音调：" + mPitch + "    "
                + "节拍：" + mTempo + "\n"
                + "八度音调节：" + mPitchOctaves + "    "
                + "半音阶调节：" + mPitchSemiTones + "\n";
        mTvMusicValue.setText(value);
    }
}