package com.cgfay.cavfoundation.player;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.cavfaudio.AVAudioMix;
import com.cgfay.cavfoundation.AVAsset;
import com.cgfay.cavfoundation.AVAssetTrack;
import com.cgfay.cavfoundation.AVComposition;
import com.cgfay.cavfoundation.AVCompositionTrack;
import com.cgfay.cavfoundation.AVVideoComposition;
import com.cgfay.cavfoundation.CAVAssetTrack;
import com.cgfay.cavfoundation.CAVUriAsset;
import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 播放一个AVAsset数据
 */
public class AVPlayerItem {

    /**
     * 默认视频帧率
     */
    public static final float DEFAULT_FRAME_RATE = 30;

    /**
     * 播放状态
     */
    private AVPlayerItemStatus mStatus;

    /**
     * 播放路径
     */
    private Uri mUri;

    /**
     * 媒体数据
     */
    private AVAsset mAsset;

    /**
     * 播放器媒体轨道
     */
    private final List<AVPlayerItemTrack> mTracks = new ArrayList<>();

    /**
     * 播放时长
     */
    private AVTime mDuration;

    /**
     * 播放器展现大小
     */
    private CGSize mPresentationSize;

    /**
     * 视频播放行为描述，用于描述播放期间的行为
     */
    private AVVideoComposition mVideoComposition;

    /**
     * 是否需要等待渲染完成再跳转
     */
    private boolean seekingWaitsForVideoCompositionRendering;

    /**
     * 音频混合参数
     */
    @Nullable
    private AVAudioMix mAudioMix;

    /**
     * 缓冲区是否已满了
     */
    private boolean mPlaybackBufferFull;

    /**
     * 缓冲区是否已空了
     */
    private boolean mPlaybackBufferEmpty;

    /**
     * 前向缓冲时长，用于提前缓冲一定时长
     */
    private double mForwardBufferDuration;

    /**
     * 输出最大分辨率
     */
    @NonNull
    private CGSize mMaximumResolution;

    public AVPlayerItem() {
        reset();
    }

    public AVPlayerItem(@NonNull String path) {
        reset();
        mUri = Uri.fromFile(new File(path));
        mAsset = AVAsset.assetWithPath(path);
        mDuration = mAsset.getDuration();
        initPlayerTrack(mAsset);
    }

    public AVPlayerItem(@NonNull Context context,  @NonNull Uri uri) {
        reset();
        mUri = uri;
        mAsset = AVAsset.assetWithUri(context, uri);
        mDuration = mAsset.getDuration();
        initPlayerTrack(mAsset);
    }

    public AVPlayerItem(@NonNull AVAsset asset) {
        reset();
        mUri = asset.getUri();
        mAsset = asset;
        mDuration = asset.getDuration();
    }

    private void reset() {
        mStatus = AVPlayerItemStatus.AVPlayerItemStatusUnknown;
        mUri = null;
        mAsset = null;
        mDuration = AVTime.kAVTimeZero;
        mPresentationSize = CGSize.kSizeZero;
        mVideoComposition = null;
        seekingWaitsForVideoCompositionRendering = false;
        mAudioMix = null;
        mPlaybackBufferFull = false;
        mPlaybackBufferEmpty = true;
        mForwardBufferDuration = 0L;
        mMaximumResolution = CGSize.kSizeZero;
    }

    /**
     * 创建一个播放item
     * @param context
     * @param uri
     * @return
     */
    public AVPlayerItem playerItemWithUri(@NonNull Context context, @NonNull Uri uri) {
        AVAsset asset = AVAsset.assetWithUri(context, uri);
        AVPlayerItem item = new AVPlayerItem();
        item.initPlayerTrack(asset);
        return item;
    }

    /**
     * 使用uri初始化一个播放item
     * @param context
     * @param uri
     */
    public void initWidthUri(@NonNull Context context, @NonNull Uri uri) {

    }

    /**
     * 使用一个媒体数据对象初始化播放item
     * @param asset
     */
    public void initWidthAsset(@NonNull AVAsset asset) {

    }

    /**
     * 初始化播放Item
     */
    public void initPlayerTrack(@Nullable AVAsset asset) {
        if (asset != null) {
            mDuration = asset.getDuration();
            if (asset instanceof CAVUriAsset) {
                initPlayerTrack((CAVUriAsset) asset);
            } else if (asset instanceof AVComposition) {
                initPlayerTrack((AVComposition) asset);
            }
        }
    }

    /**
     * 使用固定的媒体资源初始化播放器轨道
     */
    private void initPlayerTrack(@NonNull CAVUriAsset asset) {
        List<CAVAssetTrack> tracks = asset.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            CAVAssetTrack track = tracks.get(i);
            AVPlayerItemTrack playerTrack = new AVPlayerItemTrack(track, DEFAULT_FRAME_RATE);
            mTracks.add(playerTrack);
        }
    }

    /**
     * 使用组合媒体资源初始化播放器轨道
     */
    private void initPlayerTrack(@NonNull AVComposition composition) {
        List<AVCompositionTrack> tracks = composition.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            AVCompositionTrack track = tracks.get(i);
            AVPlayerItemTrack playerTrack = new AVPlayerItemTrack(track, DEFAULT_FRAME_RATE);
            mTracks.add(playerTrack);
        }
    }

    /**
     * 跳转到某个时间
     * @param time 当前item的时间
     */
    public void seekToTime(AVTime time) {

    }

    /**
     * 取消待定的跳转命令
     */
    public void cancelPendingSeeks() {

    }

    /**
     * 设置视频行为描述对象
     */
    public void setVideoComposition(@Nullable AVVideoComposition composition) {
        mVideoComposition = composition;
    }

    /**
     * 获取视频在时间轴上的描述
     */
    public AVVideoComposition getVideoComposition() {
        return mVideoComposition;
    }

    /**
     * 获取当前时间
     */
    public AVTime getCurrentTime() {
        return null;
    }

}
