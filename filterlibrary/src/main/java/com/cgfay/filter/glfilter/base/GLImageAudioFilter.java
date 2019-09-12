package com.cgfay.filter.glfilter.base;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.cgfay.filter.audioplayer.AutoFocusPlayer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 带音乐播放滤镜
 */
public class GLImageAudioFilter extends GLImageFilter {

    private Uri mAudioUri;
    private boolean mLooping = false;
    private boolean mPlayerInit = false;
    private PlayerStatus mPlayerStatus = PlayerStatus.RELEASE;
    private MediaPlayer mAudioPlayer = null;
    private Set<MediaPlayer> mPlayerSet = new HashSet<>();

    public GLImageAudioFilter(Context context) {
        super(context);
    }

    public GLImageAudioFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void release() {
        super.release();
        destroyPlayer();
    }

    /**
     * 设置音乐路径
     * @param path
     */
    public void setAudioPath(String path) {
        setAudioPath(Uri.parse(path));
    }

    /**
     * 设置音乐路径
     * @param uri
     */
    public void setAudioPath(Uri uri) {
        mAudioUri = uri;
    }

    /**
     * 设置是否循环播放
     * @param looping
     */
    public void setLooping(boolean looping) {
        mLooping = looping;
    }

    /**
     * 是否循环播放
     * @return
     */
    public boolean isLooping() {
        return mLooping;
    }

    /**
     * 开始播放
     */
    public void startPlayer() {
        if (mAudioUri == null) {
            return;
        }
        if (mPlayerStatus == PlayerStatus.RELEASE) {
            initPlayer();
        } else if (mPlayerStatus == PlayerStatus.PREPARED) {
            mAudioPlayer.start();
            mAudioPlayer.seekTo(0);
            mPlayerStatus = PlayerStatus.PLAYING;
        } else if (mPlayerStatus == PlayerStatus.INIT) {
            mPlayerInit = true;
        }
    }

    /**
     * 停止播放
     */
    public void stopPlayer() {
        if (mAudioPlayer != null && mPlayerStatus == PlayerStatus.PLAYING) {
            mAudioPlayer.pause();
            mPlayerStatus = PlayerStatus.PREPARED;
        }
        mPlayerInit = false;
    }

    /**
     * 重新播放
     */
    public void restartPlayer() {
        if (mAudioPlayer != null && mPlayerStatus == PlayerStatus.PLAYING) {
            mAudioPlayer.seekTo(0);
        }
    }

    /**
     * 销毁音乐播放暖气
     */
    public void destroyPlayer() {
        stopPlayer();
        if (mAudioPlayer != null && mPlayerStatus == PlayerStatus.PREPARED) {
            mAudioPlayer.stop();
            mAudioPlayer.release();
            mPlayerSet.remove(mAudioPlayer);
        }
        mAudioPlayer = null;
        mPlayerStatus = PlayerStatus.RELEASE;
    }

    /**
     * 初始化音乐
     */
    public void initPlayer() {
        mAudioPlayer = new AudioPlayer(mContext, this);
        try {
            mAudioPlayer.setDataSource(mContext, mAudioUri);
            mAudioPlayer.setOnPreparedListener(mPreparedListener);
            mPlayerSet.add(mAudioPlayer);
            mAudioPlayer.prepareAsync();
            mAudioPlayer.setLooping(mLooping);
            mPlayerStatus = PlayerStatus.INIT;
            mPlayerInit = true;
        } catch (IOException e) {
            Log.e(TAG, "initPlayer: ", e);
        }
    }

    /**
     * 准备监听器
     */
    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer player) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    if (mPlayerInit && mPlayerStatus == PlayerStatus.INIT && mAudioPlayer != null) {
                        mAudioPlayer.start();
                        mPlayerStatus = PlayerStatus.PLAYING;
                    } else if (mPlayerStatus == PlayerStatus.INIT) {
                        mPlayerStatus = PlayerStatus.PREPARED;
                    }
                    // 如果准备的播放器跟滤镜当前的音乐播放器不是一个，则需要释放准备好的这个播放器
                    // 这里不一致的原因是，存在张嘴眨眼等动作的音乐跟默认状态的音乐不一致等情况
                    if (mAudioPlayer != player && mPlayerSet.contains(player)) {
                        player.stop();
                        player.release();
                    }
                }
            });
        }
    };


    /**
     * 音乐播放器
     */
    private class AudioPlayer extends AutoFocusPlayer {

        private final GLImageAudioFilter mFilter;

        public AudioPlayer(Context context, GLImageAudioFilter filter) {
            super(context);
            mFilter = filter;
        }

        @Override
        protected void lossFocus() {
            super.lossFocus();
            if (isPlaying()) {
                mFilter.stopPlayer();
            }
        }
    }


    /**
     * 播放器状态枚举
     */
    private enum PlayerStatus {

        RELEASE("release", 0),
        INIT("init", 1),
        PREPARED("prepared", 2),
        PLAYING("playing", 3);

        private String name;
        private int index;

        PlayerStatus(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
