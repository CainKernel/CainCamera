package com.cgfay.caincamera.multimedia;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.cgfay.caincamera.bean.SubVideo;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 多媒体播放器管理器
 * Created by cain.huang on 2017/12/5.
 */

public final class MediaPlayerManager {

    private static final String TAG = "MediaPlayerManager";

    private static final String APP_NAME = "CainCamera";
    
    private static MediaPlayerManager mInstance;

    // 播放器
    private SimpleExoPlayer mPlayer;

    public static MediaPlayerManager getInstance() {
        if (mInstance == null) {
            mInstance = new MediaPlayerManager();
        }
        return mInstance;
    }

    private MediaPlayerManager() {}


    /**
     * 创建播放器
     * @param context
     */
    public void createMediaPlayer(Context context) {

        // 创建带宽
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        // 创建轨道选择工厂
        TrackSelection.Factory factory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

        // 创建轨道选择器实例
        TrackSelector trackSelector = new DefaultTrackSelector(factory);

        // 加载控制器
        LoadControl loadControl = new DefaultLoadControl();

        // 创建播放器
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
    }


    /**
     * 设置重复模式
     * @param mode REPEAT_MODE_OFF, REPEAT_MODE_ONE, REPEAT_MODE_ALL 其中一种
     */
    public void setRepeatMode(@Player.RepeatMode int mode) {
        if (mPlayer != null) {
            mPlayer.setRepeatMode(mode);
        }
    }

    /**
     * 设置播放器的输出目标
     * @param surface
     */
    public void setPlayerSurface(SurfaceView surface) {
        if (mPlayer != null) {
            mPlayer.setVideoSurfaceView(surface);
        }
    }

    /**
     * 设置播放器的输出目标
     * @param surface
     */
    public void setPlayerSurface(Surface surface) {
        if (mPlayer != null) {
            mPlayer.setVideoSurface(surface);
        }
    }

    /**
     * 设置播放器的输出目标
     * @param holder
     */
    public void setPlayerSurface(SurfaceHolder holder) {
        if (mPlayer != null) {
            mPlayer.setVideoSurfaceHolder(holder);
        }
    }

    /**
     * 设置播放器的输出目标
     * @param textureView
     */
    public void setPlayerSurface(TextureView textureView) {
        if (mPlayer != null) {
            mPlayer.setVideoTextureView(textureView);
        }
    }

    /**
     * 准备数据源
     * @param context
     * @param videoUri
     */
    public void preparePlayer(Context context, Uri videoUri) {
        if (mPlayer != null) {
            // 测量播放带宽， 如果不需要可以传null
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

            // 创建加载数据工厂
            DataSource.Factory factory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, APP_NAME), bandwidthMeter);

            // 创建解析数据工厂
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            // 传入Uri、加载数据的工厂、解析数据的工厂
            MediaSource videoSource = new ExtractorMediaSource(videoUri, factory,
                    extractorsFactory, null, null);

            mPlayer.prepare(videoSource);
        }
    }

    /**
     * 准备数据源
     * @param context
     * @param videoList
     */
    public void preparePlayer(Context context, List<SubVideo> videoList) {
        if (mPlayer != null && !videoList.isEmpty()) {
            // 测量播放带宽， 如果不需要可以传null
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

            // 创建加载数据工厂
            DataSource.Factory factory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, APP_NAME), bandwidthMeter);

            // 创建解析数据工厂
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            // MediaSource数据源列表
            List<MediaSource> sourceList = new ArrayList<MediaSource>();
            for (int i = 0; i < videoList.size(); i++) {
                String mediaPath = videoList.get(i).getMediaPath();
                if (!TextUtils.isEmpty(videoList.get(i).getMediaPath())) {
                    MediaSource source = new ExtractorMediaSource(Uri.parse(mediaPath), factory,
                            extractorsFactory, null, null);
                    sourceList.add(source);
                }
            }
            // 添加多个数据源
            ConcatenatingMediaSource mediaSource = new ConcatenatingMediaSource(
                    sourceList.toArray(new MediaSource[sourceList.size()]));

            mPlayer.prepare(mediaSource);
        }
    }

    /**
     * 设置事件监听器
     * @param listener
     */
    public void setPlayerListener(ExoPlayer.EventListener listener) {
        if (mPlayer != null) {
            mPlayer.addListener(listener);
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(true);
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(true);
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mPlayer != null) {
            mPlayer.addListener(null);
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * 获取播放器
     * @return
     */
    public ExoPlayer getPlayer() {
        return mPlayer;
    }


}
