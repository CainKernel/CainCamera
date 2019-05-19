package com.cgfay.caincamera.fragment;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.widget.VideoClipSeekBar;
import com.cgfay.media.CainShortVideoEditor;
import com.cgfay.video.widget.CircleProgressView;

public class VideoGifMakeFragment extends Fragment {

    private static final String TAG = "VideoGifMakeFragment";

    private Activity mActivity;

    private String mVideoPath;
    private View mContentView;
    private VideoView mVideoPlayerView;

    // 选择转码成gif的部分
    private TextView mTvProgressStart;
    private TextView mTvProgressEnd;
    private VideoClipSeekBar mVideoCutBar;
    private String mStartTips;
    private String mEndTips;
    private float mGifStart;
    private float mGifEnd;

    // 执行进度提示
    private LinearLayout mLayoutProgress;
    // 圆形进度条
    private CircleProgressView mCvCropProgress;
    private TextView mTvCropProgress;

    private AudioManager mAudioManager;
    private CainShortVideoEditor mVideoEditor;

    public static VideoGifMakeFragment newInstance() {
        return new VideoGifMakeFragment();
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
        mContentView =  inflater.inflate(R.layout.fragment_video_gif_make, container, false);
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

        mTvProgressStart = mContentView.findViewById(R.id.tv_progress_start);
        mTvProgressEnd = mContentView.findViewById(R.id.tv_progress_end);
        mVideoCutBar = mContentView.findViewById(R.id.video_progress);
        mVideoCutBar.addCutBarChangeListener(mCutBarChangeListener);

        mStartTips = getResources().getString(R.string.tv_test_gif_start);
        mEndTips = getResources().getString(R.string.tv_test_gif_end);

        mLayoutProgress = mContentView.findViewById(R.id.layout_progress);
        mLayoutProgress.setVisibility(View.GONE);
        mCvCropProgress = mLayoutProgress.findViewById(R.id.cv_crop_progress);
        mTvCropProgress = mLayoutProgress.findViewById(R.id.tv_crop_progress);


        mContentView.findViewById(R.id.btn_test_gif_make).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoConvertGif();
            }
        });

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

    public void setVideoPath(String path) {
        mVideoPath = path;
        if (mVideoPlayerView != null) {
            mVideoPlayerView.setVideoPath(mVideoPath);
        }
    }

    private VideoClipSeekBar.OnCutBarChangeListener mCutBarChangeListener = new VideoClipSeekBar.OnCutBarChangeListener() {

        @Override
        public void onStartProgressChanged(float screenStartX, int progress) {
            mGifStart = ((float) progress / mVideoCutBar.getMax()) * mVideoPlayerView.getDuration();
            mTvProgressStart.setText(String.format(mStartTips, mGifStart));
        }

        @Override
        public void onEndProgressChanged(float screenEndX, int progress) {
            mGifEnd = ((float) progress / mVideoCutBar.getMax()) * mVideoPlayerView.getDuration();
            mTvProgressEnd.setText(String.format(mEndTips, mGifEnd));
        }

        @Override
        public void onTouchFinish(int start, int end) {
            mGifStart = ((float) start / mVideoCutBar.getMax()) * mVideoPlayerView.getDuration();
            mTvProgressStart.setText(String.format(mStartTips, mGifStart));
            mGifEnd = ((float) end / mVideoCutBar.getMax()) * mVideoPlayerView.getDuration();
            mTvProgressEnd.setText(String.format(mEndTips, mGifEnd));
        }
    };


    private void videoConvertGif() {
        if (mGifEnd == 0) {
            if (mVideoPath != null) {
                mGifEnd = mVideoPlayerView.getDuration();
            }
        }
        mLayoutProgress.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mVideoPlayerView != null) {
                    mVideoPlayerView.pause();
                }
                if (mVideoEditor == null) {
                    mVideoEditor = new CainShortVideoEditor();
                }

                String gifPath = CainShortVideoEditor.VideoEditorUtil.createFileInBox("gif");
                final int ret = mVideoEditor.videoConvertGif(mVideoPath, gifPath, mGifStart, mGifEnd - mGifStart);

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 成功则释放播放器并跳转至编辑页面
                        Toast.makeText(mActivity, ret >= 0 ? "成功生成GIF" : "生成GIF失败", Toast.LENGTH_SHORT).show();
                        if (mVideoPlayerView != null) {
                            mVideoPlayerView.start();
                        }
                        mLayoutProgress.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }
}
