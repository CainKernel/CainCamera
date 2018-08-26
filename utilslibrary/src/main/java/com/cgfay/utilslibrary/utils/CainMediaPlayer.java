package com.cgfay.utilslibrary.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;


/**
 * MediaPlayer播放器封装
 */
public class CainMediaPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener  {


    private static final int MSG_PAUSE = 0x01;
    private static final int MSG_LOOP = 0x02;

    private MediaPlayer.OnCompletionListener mCompletionListener;
    private MediaPlayer.OnPreparedListener mPreparedListener;
    private MediaPlayer.OnErrorListener mErrorListener;
    private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener;
    private PlayStateListener mPlayStateListener;
    private MediaPlayer mMediaPlayer = null;

    private String mVideoPath;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mDuration;
    private int mVolume;
    private SurfaceTexture mSurfaceTexture = null;

    private StateType mCurrentState = StateType.IDLE;
    private StateType mTargetState = StateType.IDLE;

    private Context mContext;

    public CainMediaPlayer(Context context) {
        mContext = context;
        try {
            AudioManager mAudioManager = (AudioManager) mContext
                    .getSystemService(Context.AUDIO_SERVICE);
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } catch (UnsupportedOperationException e) {

        }
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentState = StateType.COMPLETED;
        if (mCompletionListener != null) {
            mCompletionListener.onCompletion(mp);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mCurrentState = StateType.ERROR;
        if (mErrorListener != null) {
            mErrorListener.onError(mp, what, extra);
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mCurrentState == StateType.PREPARING) {
            mCurrentState = StateType.PREPARED;
            mDuration = mp.getDuration();

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            switch (mTargetState) {

                case PREPARED:
                    if (mPreparedListener != null) {
                        mPreparedListener.onPrepared(mMediaPlayer);
                    }
                    break;

                case PLAYING:
                    start();
                    break;
            }
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mSeekCompleteListener != null) {
            mSeekCompleteListener.onSeekComplete(mp);
        }
    }

    /**
     * 重新打开
     */
    public void reopenVideo() {
        mTargetState = StateType.PREPARED;
        if (mSurfaceTexture == null) {
            openVideo(mVideoPath, null);
        } else {
            Surface surface = new Surface(mSurfaceTexture);
            openVideo(mVideoPath, surface);
            surface.release();
        }
    }

    /**
     * 打开视频
     * @param path
     * @param surface
     */
    public void openVideo(String path, Surface surface) {
        mTargetState = StateType.PREPARED;
        if (TextUtils.isEmpty(path) || surface == null || mContext == null) {
            return;
        }
        mDuration = 0;
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            } else {
                mMediaPlayer.setSurface(null);
                mMediaPlayer.reset();
            }
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setVolume(mVolume, mVolume);
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
            mCurrentState = StateType.PREPARING;
        } catch (Exception e) {
            mCurrentState = StateType.ERROR;
            if (mErrorListener != null) {
                mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            }
        }
    }

    /**
     * 开始
     */
    public void start() {
        mTargetState = StateType.PLAYING;
        if (mMediaPlayer == null) {
            return;
        }
        if (checkStateEnable()) {
            try {
                if (!isPlaying()) {
                    mMediaPlayer.start();
                }
                mCurrentState = StateType.PLAYING;
                if (mPlayStateListener != null) {
                    mPlayStateListener.onStateChanged(StateType.PLAYING);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mCurrentState = StateType.ERROR;
                if (mPlayStateListener != null) {
                    mPlayStateListener.onPlayChangedError(mTargetState, "start error");
                }
            }
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        mTargetState = StateType.PAUSED;
        if (mMediaPlayer == null) {
            return;
        }
        if (mCurrentState == StateType.PLAYING || mCurrentState == StateType.PAUSED) {
            try {
                mMediaPlayer.pause();
                mCurrentState = StateType.PAUSED;
                if (mPlayStateListener != null) {
                    mPlayStateListener.onStateChanged(StateType.PAUSED);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mCurrentState = StateType.ERROR;
                if (mPlayStateListener != null) {
                    mPlayStateListener.onPlayChangedError(mTargetState, "pause error");
                }
            }
        }
    }

    /**
     * 重置
     */
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(null);
            mMediaPlayer.reset();
        }
    }

    /**
     * 定时暂停
     * @param delayMillis
     */
    public void pauseDelay(int delayMillis) {
        if (mPlayHandler.hasMessages(MSG_PAUSE)) {
            mPlayHandler.removeMessages(MSG_PAUSE);
        }
        mPlayHandler.sendEmptyMessageDelayed(MSG_PAUSE, delayMillis);
    }

    /**
     * 停止
     */
    public void stop() {
        mTargetState = StateType.STOP;
        if (mMediaPlayer == null) {
            return;
        }
        if (mCurrentState == StateType.PLAYING || mCurrentState == StateType.PAUSED) {
            try {
                mMediaPlayer.stop();
                if (mPlayStateListener != null) {
                    mPlayStateListener.onStateChanged(StateType.STOP);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mCurrentState = StateType.ERROR;
                if (mPlayStateListener != null) {
                    mPlayStateListener.onPlayChangedError(mTargetState, "stop error");
                }
            }
        }
    }

    /**
     * 设置音量
     * @param volume
     */
    public void setVolume(float volume) {
        if (mMediaPlayer == null) {
            return;
        }
        if (checkStateEnable()) {
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    /**
     * 是否自动循环
     * @param loop
     */
    public void setLooping(boolean loop) {
        if (mMediaPlayer == null) {
            return;
        }
        if (checkStateEnable()) {
            mMediaPlayer.setLooping(loop);
        }
    }

    /**
     * 区域内循环 （单位毫秒）
     * @param startTime
     * @param endTime
     */
    public void loopRegion(int startTime, int endTime) {
        // 限定范围，超出范围无效
        if (startTime >= endTime - 1 || startTime >= getDuration() || endTime >= getDuration()) {
            return;
        }
        int delayMillis = endTime - startTime;
        seekTo(startTime);
        if (!isPlaying()) {
            start();
        }
        if (mPlayHandler.hasMessages(MSG_LOOP)) {
            mPlayHandler.removeMessages(MSG_LOOP);
        }
        mPlayHandler.sendMessageDelayed(mPlayHandler
                .obtainMessage(MSG_LOOP, getCurrentPosition(), delayMillis), delayMillis);
    }

    /**
     * 跳转
     * @param msec
     */
    public void seekTo(int msec) {
        if (mMediaPlayer == null) {
            return;
        }
        if (checkStateEnable()) {
            try {
                if (msec < 0) {
                    msec = 0;
                } else if (msec >= mDuration) {
                    msec = mDuration;
                }
                mMediaPlayer.seekTo(msec);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                if (mPlayStateListener != null) {
                    mPlayStateListener.onStateChanged(StateType.ERROR);
                }
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        mTargetState = StateType.RELEASED;
        mCurrentState = StateType.RELEASED;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * 已经准备好
     * @return
     */
    public boolean isPrepared() {
        return mMediaPlayer != null && (mCurrentState == StateType.PREPARED);
    }

    /**
     * 是否正在播放
     * @return
     */
    public boolean isPlaying() {
        if (mMediaPlayer != null && mCurrentState == StateType.PLAYING) {
            try {
                return mMediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 检查当前状态是否可用
     * @return
     */
    private boolean checkStateEnable() {
        return mCurrentState == StateType.PREPARED || mCurrentState == StateType.PLAYING
                || mCurrentState == StateType.PAUSED || mCurrentState == StateType.COMPLETED;
    }

    /**
     * 获取当前的播放位置
     */
    public int getCurrentPosition() {
        int position = 0;
        if (mMediaPlayer != null) {
            switch (mCurrentState) {
                case COMPLETED:
                    position = getDuration();
                    break;
                case PLAYING:
                case PAUSED:
                    try {
                        position = mMediaPlayer.getCurrentPosition();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        return position;
    }

    public void setPreparedListener(MediaPlayer.OnPreparedListener listener) {
        mPreparedListener = listener;
    }

    public void setErrorListener(MediaPlayer.OnErrorListener listener) {
        mErrorListener = listener;
    }

    public void setSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener) {
        mSeekCompleteListener = listener;
    }

    public void setCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    public void setPlayStateListener(PlayStateListener listener) {
        mPlayStateListener = listener;
    }

    public void setVideoPath(String path) {
        mVideoPath = path;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public int getDuration() {
        return mDuration;
    }

    /**
     * 播放状态监听
     */
    public interface PlayStateListener {
        void onStateChanged(StateType state);
        void onPlayChangedError(StateType state, String message);
    }

    @SuppressLint("HandlerLeak")
    private Handler mPlayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PAUSE:
                    pause();
                    break;

                case MSG_LOOP:
                    if (isPlaying()) {
                        seekTo(msg.arg1);
                        sendMessageDelayed(obtainMessage(MSG_LOOP, msg.arg1, msg.arg2), msg.arg2);
                    }
                    break;
            }
        }
    };

}
