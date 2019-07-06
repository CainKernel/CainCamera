package com.cgfay.caincamera.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cgfay.caincamera.R;
import com.cgfay.media.CainShortVideoEditor;
import com.cgfay.uitls.utils.FileUtils;
import com.cgfay.video.activity.VideoEditActivity;
import com.cgfay.video.widget.CircleProgressView;

/**
 * 视频音乐合成
 */
public class MusicMergeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MusicMergeFragment";

    private Activity mActivity;

    private String mVideoPath;
    private String mMusicPath;
    private long mMusicDuration;

    private View mContentView;
    private VideoView mVideoPlayerView;
    private TextView mTvMusic;

    private SeekBar mSourceProgress;
    private SeekBar mMusicProgress;
    private float mVideoVolume = 0.5f;
    private float mMusicVolume = 0.5f;

    // 执行进度提示
    private LinearLayout mLayoutProgress;
    // 圆形进度条
    private CircleProgressView mCvCropProgress;
    private TextView mTvCropProgress;

    private AudioManager mAudioManager;
    private CainShortVideoEditor mVideoEditor;

    public static MusicMergeFragment newInstance() {
        return new MusicMergeFragment();
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
        mContentView =  inflater.inflate(R.layout.fragment_music_merge, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        mVideoPlayerView = mContentView.findViewById(R.id.video_view);
        if (mVideoPath != null) {
            mVideoPlayerView.setVideoPath(mVideoPath);
        }
        mTvMusic = mContentView.findViewById(R.id.music_path);
        if (mMusicPath != null) {
            mTvMusic.setText("music path - " + mMusicPath);
        }

        mSourceProgress = mContentView.findViewById(R.id.video_voice_progress);
        mSourceProgress.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mMusicProgress = mContentView.findViewById(R.id.music_voice_progress);
        mMusicProgress.setOnSeekBarChangeListener(mSeekBarChangeListener);

        mLayoutProgress = mContentView.findViewById(R.id.layout_progress);
        mLayoutProgress.setVisibility(View.GONE);
        mCvCropProgress = mLayoutProgress.findViewById(R.id.cv_crop_progress);
        mTvCropProgress = mLayoutProgress.findViewById(R.id.tv_crop_progress);

        mContentView.findViewById(R.id.btn_test_music_merge).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mVideoPlayerView != null) {
            mVideoPlayerView.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoPlayerView != null) {
            mVideoPlayerView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoPlayerView != null) {
            mVideoPlayerView.pause();
        }
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mVideoPlayerView != null) {
            mVideoPlayerView.pause();
            mVideoPlayerView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test_music_merge) {
            musicMergeCommand();
        }
    }

    public void setVideoPath(String path) {
        mVideoPath = path;
        if (mVideoPlayerView != null) {
            mVideoPlayerView.setVideoPath(path);
        }
    }

    public void setMusicPath(String path, long duration) {
        mMusicPath = path;
        mMusicDuration = duration;
        if (mTvMusic != null) {
            mTvMusic.setText("music path:\n" + mMusicPath);
        }
    }

    /**
     * 给视频添加音乐
     */
    private void musicMerge() {
        // TODO native层用代码实现音频混合
    }

    /**
     * 命令行混合，这里直接用mp3 和 视频合成会崩溃，会提示找不到aac编码器，需要先将音频文件转码成aac文件
     * 这里是混合处理的代码
     */
    private void musicMergeCommand() {
        mLayoutProgress.setVisibility(View.VISIBLE);
        // TODO crop video
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long startTime = System.currentTimeMillis();
                if (mVideoPlayerView != null) {
                    mVideoPlayerView.pause();
                }
                if (mVideoEditor == null) {
                    mVideoEditor = new CainShortVideoEditor();
                }

                // 1、将音频文件转码为aac文件
                long duration = mMusicDuration;
                if (mMusicDuration > mVideoPlayerView.getDuration()) {
                    duration = mVideoPlayerView.getDuration();
                }
                String tmpAACPath = CainShortVideoEditor.VideoEditorUtil.createPathInBox("aac");
                int ret = mVideoEditor.audioCut(mMusicPath, tmpAACPath, 0, duration);
                if (ret < 0) {
                    FileUtils.deleteFile(tmpAACPath);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "aac转码失败，合成失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                // 2、音视频混合
                String videoPath = mVideoEditor.audioVideoMix(mVideoPath, tmpAACPath, mVideoVolume, mMusicVolume);
                FileUtils.deleteFile(tmpAACPath);
                // 成功则释放播放器并跳转至编辑页面
                if (FileUtils.fileExists(videoPath)) {
                    // 需要释放销毁播放器，后面要用到播放器，防止内存占用过大
                    if (mVideoPlayerView != null) {
                        mVideoPlayerView.pause();
                    }
                    if (mVideoEditor != null) {
                        mVideoEditor.release();
                        mVideoEditor = null;
                    }
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long processTime = System.currentTimeMillis() - startTime;
                            Toast.makeText(mActivity,
                                    "音频混合处理耗时: " + (processTime / 1000f) + "秒",
                                    Toast.LENGTH_SHORT)
                                    .show();
                            Intent intent = new Intent(mActivity, VideoEditActivity.class);
                            intent.putExtra(VideoEditActivity.VIDEO_PATH, videoPath);
                            startActivity(intent);
                            mActivity.finish();
                        }
                    });
                } else {
                    Log.e(TAG, "video cut's error!");
                    if (mVideoPlayerView != null) {
                        mVideoPlayerView.start();
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

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {

                if (seekBar.getId() == R.id.video_voice_progress) {
                    mVideoVolume = (float) progress / seekBar.getMax();
                } else if (seekBar.getId() == R.id.music_voice_progress) {
                    mMusicVolume = (float) progress / seekBar.getMax();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
