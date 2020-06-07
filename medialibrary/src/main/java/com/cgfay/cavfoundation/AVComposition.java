package com.cgfay.cavfoundation;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeMapping;
import com.cgfay.coremedia.AVTimeRange;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体组合，用于视频编辑状态描述
 */
public class AVComposition extends AVAsset {

    private static final int DEFAULT_SAMPLE_RATE = 44100;

    /**
     * 组合轨道列表
     */
    private final List<AVCompositionTrack> mTracks = new ArrayList<>();

    public AVComposition() {
        super();
    }

    /**
     * 添加一个可变的媒体类型
     * @param type
     * @return
     */
    public AVCompositionTrack addMutableTrackWithMediaType(AVMediaType type) {
        return addMutableTrackWithMediaType(type, AVAssetTrack.kTrackIDInvalid);
    }

    /**
     * 添加一个可变的轨道并返回该对象
     * @param type 媒体类型
     * @param trackID 轨道id
     */
    public AVCompositionTrack addMutableTrackWithMediaType(AVMediaType type, int trackID) {
        AVCompositionTrack track = new AVCompositionTrack(type);
        track.mTrackID = trackID;
        // 设置默认轨道的时间timescale，视频流使用600，音频流使用44100
        if (type == AVMediaType.AVMediaTypeVideo) {
            track.setNaturalTimeScale(AVTime.DEFAULT_TIME_SCALE);
        } else if (type == AVMediaType.AVMediaTypeAudio) {
            track.setNaturalTimeScale(DEFAULT_SAMPLE_RATE);
        }
        mTracks.add(track);
        return track;
    }

    /**
     * 将媒体插入一段数据中
     * @param timeRange 要插入的媒体的时间区间
     * @param asset     媒体对象
     * @param startTime 插入区间在轨道上的起始位置
     * @return          插入结果
     */
    public boolean insertTimeRange(@NonNull AVTimeRange timeRange, @NonNull AVAsset asset,
                                   @NonNull AVTime startTime) {
        // 如果轨道是空数据，则创建一个
        if (mTracks.size() == 0) {
            List<AVAssetTrack> tracks = asset.getTracks();
            for (int i = 0; i < tracks.size(); i++) {
                AVCompositionTrack track = mutableTrackCompatibleWithTrack(tracks.get(i));
                if (track != null) {
                    mTracks.add(track);
                }
            }
        } else {
            // 如果存在轨道，则逐个判断是否存在相同的轨道
            List<AVAssetTrack> tracks = asset.getTracks();
            for (int i = 0; i < mTracks.size(); i++) {
                if (mTracks.get(i).getAsset() == asset) {
                    continue;
                }
            }
        }
        return true;
    }

    /**
     * 插入一段空的时间区间
     * @param timeRange 时间区间
     */
    public void insertEmptyTimeRange(AVTimeRange timeRange) {
        for (AVCompositionTrack track : mTracks) {
            track.insertEmptyTimeRange(timeRange);
        }
    }

    /**
     * 移除一段时间区间
     * @param timeRange 时间区间
     */
    public void removeTimeRange(@NonNull AVTimeRange timeRange) {
        for (AVCompositionTrack track : mTracks) {
            track.removeTimeRange(timeRange);
        }
    }

    /**
     * 更改所有轨道的持续时间
     * @param timeRange 需要更改的时间区间
     * @param duration  变成新的时长
     * 受到缩放影响的每个片段的速率等于其生成的timeMapping的source.duration/target.duration
     */
    public void scaleTimeRange(@NonNull AVTimeRange timeRange, AVTime duration) {
        for (AVCompositionTrack track : mTracks) {
            track.scaleTimeRange(timeRange, duration);
        }
    }

    /**
     * 移除某个轨道
     */
    public void removeTrack(AVCompositionTrack track) {
        mTracks.remove(track);
    }

    /**
     * 利用源轨道创建一个可变轨道
     * @param track 源轨道
     * @return      返回一个可变轨道，可以为空对象
     */
    @NonNull
    public AVCompositionTrack mutableTrackCompatibleWithTrack(@NonNull AVAssetTrack track) {
        AVCompositionTrack compositionTrack = new AVCompositionTrack(track.getMediaType());
        compositionTrack.mAsset = track.getAsset();
        compositionTrack.mTrackID = track.getTrackID();
        compositionTrack.mMediaType = track.getMediaType();
        compositionTrack.mTimeRange = track.getTimeRange();
        compositionTrack.mNaturalSize = track.getNaturalSize();
        compositionTrack.mPreferredTransform.set(track.getPreferredTransform());
        compositionTrack.mFrameReordering = track.isFrameReordering();
        AVAsset asset = track.getAsset();
        Uri uri = null;
        if (asset != null) {
            uri = asset.getUri();
        }
        int trackID = track.getTrackID();
        for (int i = 0; i < track.getTrackSegments().size(); i++) {
            // 如果是组合轨道片段，则直接添加轨道片段
            if (track.getTrackSegments().get(i) instanceof AVCompositionTrackSegment) {
                AVCompositionTrackSegment segment = (AVCompositionTrackSegment) track.getTrackSegments().get(i);
                AVCompositionTrackSegment compositionTrackSegment;
                if (segment.isEmpty()) {
                    compositionTrackSegment = new AVCompositionTrackSegment(segment.getTimeMapping().getTarget());
                } else {
                    Uri sourceUri = segment.getSourceUri();
                    int sourceTrackID = segment.getSourceTrackID();
                    AVTimeMapping mapping = segment.getTimeMapping();
                    compositionTrackSegment = new AVCompositionTrackSegment(sourceUri, sourceTrackID, mapping.getSource(), mapping.getTarget());
                }
                compositionTrack.mTrackSegments.add(compositionTrackSegment);
            } else {
                AVAssetTrackSegment segment = (AVAssetTrackSegment) track.getTrackSegments().get(i);
                if (uri != null) {
                    AVTimeMapping mapping = segment.getTimeMapping();
                    AVCompositionTrackSegment compositionTrackSegment = new AVCompositionTrackSegment(uri, trackID, mapping.getSource(), mapping.getTarget());
                    compositionTrack.mTrackSegments.add(compositionTrackSegment);
                } else {
                    AVCompositionTrackSegment compositionTrackSegment = new AVCompositionTrackSegment(segment.getTimeMapping().getTarget());
                    compositionTrack.mTrackSegments.add(compositionTrackSegment);
                }
            }
        }
        return compositionTrack;
    }

    @Override
    public List<AVCompositionTrack> getTracks() {
        return mTracks;
    }
}
