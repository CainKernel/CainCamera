package com.cgfay.videolibrary.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.videolibrary.bean.Music;
import com.cgfay.videolibrary.R;

/**
 * 音乐页面
 */
public class VideoMusicFragment extends BaseVideoPageFragment {

    private TextView mSourceVoiceView;
    private TextView mMusicVoiceView;
    private SeekBar mVoiceProgress;

    private TextView mMusicTitleView;
    private LinearLayout mLayoutDelete;

    private Handler mHandler = new Handler();

    private OnMusicEditListener mMusicEditListener;

    private Music mMusic;

    public VideoMusicFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_music, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMusic != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mMusicTitleView != null) {
                        mMusicTitleView.setText(mMusic.getName());
                    }
                    if (mMusic != null) {
                        mLayoutDelete.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void initView(View view) {
        mSourceVoiceView = (TextView) view.findViewById(R.id.tv_source_voice);
        mMusicVoiceView = (TextView) view.findViewById(R.id.tv_music_voice);
        mVoiceProgress = (SeekBar) view.findViewById(R.id.voice_progress);
        mVoiceProgress.setMax(100);
        mVoiceProgress.setProgress(100);
        mVoiceProgress.setOnSeekBarChangeListener(mSeekBarChangeListener);

        mMusicTitleView = (TextView) view.findViewById(R.id.tv_music_title);
        mMusicTitleView.setOnClickListener(mOnClickListener);
        mLayoutDelete = (LinearLayout) view.findViewById(R.id.layout_delete);
        mLayoutDelete.setOnClickListener(mOnClickListener);
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mSourceVoiceView.setText(getText(R.string.source_voice) + "\n" + mVoiceProgress.getProgress() + "%");
                mMusicVoiceView.setText(getText(R.string.music_voice) + "\n" + (100 - mVoiceProgress.getProgress()) + "%");
                if (mMusicEditListener != null) {
                    mMusicEditListener.onMusicVoiceChange(mVoiceProgress.getProgress(), 100 - mVoiceProgress.getProgress());
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mSourceVoiceView.setText(getText(R.string.source_voice) + "\n" + mVoiceProgress.getProgress() + "%");
            mMusicVoiceView.setText(getText(R.string.music_voice) + "\n" + (100 - mVoiceProgress.getProgress()) + "%");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mSourceVoiceView.setText(getText(R.string.source_voice));
            mMusicVoiceView.setText(getText(R.string.music_voice));
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.tv_music_title) {
                if (mMusicEditListener != null) {
                    mMusicEditListener.onMusicSelect();
                }
            } else if (v.getId() == R.id.layout_delete) {
                if (mMusicEditListener != null) {
                    mMusicEditListener.onMusicRemove();
                }
                if (mMusicTitleView != null) {
                    mMusic = null;
                    mMusicTitleView.setText(getText(R.string.add_music));
                }
                mLayoutDelete.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 设置路径
     * @param music
     */
    public void setSelectedMusic(Music music) {
        mMusic = music;
        if (mMusicTitleView != null) {
            mMusicTitleView.setText(mMusic.getName());
        }
        if (mMusic != null && mLayoutDelete != null) {
            mLayoutDelete.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 添加音乐编辑监听器
     * @param listener
     */
    public void addOnMusicEditListener(OnMusicEditListener listener) {
        mMusicEditListener = listener;
    }

    /**
     * 音乐编辑监听器
     */
    public interface OnMusicEditListener {
        // 点击选择音乐
        void onMusicSelect();

        // 改变音乐声音
        void onMusicVoiceChange(int sourcePercent, int musicPercent);

        // 移除音乐
        void onMusicRemove();
    }
}
