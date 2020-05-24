package com.cgfay.avfoundation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeRange;

import java.util.List;

/**
 * 媒体组合，用于视频编辑状态描述
 */
public class AVComposition extends AVAsset<AVCompositionTrack> {

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
        AVCompositionTrack track = new AVCompositionTrack();
        track.mTrackID = trackID;
        track.mMediaType = type;
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
    @Nullable
    public AVCompositionTrack mutableTrackCompatibleWithTrack(AVAssetTrack track) {
        return null;
    }

}
