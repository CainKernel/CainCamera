package com.cgfay.cavfoundation;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeMapping;
import com.cgfay.coremedia.AVTimeRange;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体组合，用于视频编辑状态描述
 */
public class AVComposition implements AVAsset {

    /**
     * 源文件Uri路径，处于编辑状态时，可以为空
     */
    @Nullable
    private Uri mUri;

    /**
     * 媒体时长
     */
    @NonNull
    private AVTime mDuration;

    /**
     * 默认播放速度，通常是1.0
     */
    private float mPreferredRate;

    /**
     * 默认音量
     */
    private float mPreferredVolume;

    /**
     * 默认转换对象
     */
    @NonNull
    private AffineTransform mPreferredTransform;

    /**
     * 默认大小
     */
    @NonNull
    private CGSize mNaturalSize;

    /**
     * 组合轨道列表
     */
    @NonNull
    private List<AVCompositionTrack> mTracks = new ArrayList<>();

    public AVComposition() {
        super();
        mUri = null;
        mDuration = AVTime.kAVTimeZero;
        mPreferredRate = 1.0f;
        mPreferredVolume = 1.0f;
        mPreferredTransform = new AffineTransform().idt();
        mNaturalSize = CGSize.kSizeZero;
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
        track.setTrackID(trackID);
        mTracks.add(track);
        return track;
    }

    /**
     * 将媒体数据插入到组合媒体中
     * @param timeRange 要插入的媒体的时间区间
     * @param asset     媒体对象
     * @param startTime 插入区间在轨道上的起始位置
     * @return          插入结果
     * 该方法可能会插入新的轨道以保证asset中的所有轨道都被展示在插入的时间区间中
     */
    public boolean insertTimeRange(@NonNull AVTimeRange timeRange, @NonNull AVAsset asset,
                                   @NonNull AVTime startTime) {
        if (asset instanceof CAVUriAsset) {
            CAVUriAsset uriAsset = (CAVUriAsset) asset;
            return insertTimeRange(timeRange, uriAsset, startTime);
        } else if (asset instanceof AVComposition) {
            AVComposition composition = (AVComposition) asset;
            return insertTimeRange(timeRange, composition, startTime);
        } else {
            return false;
        }
    }

    /**
     * 将媒体数据插入到组合媒体中
     * @param timeRange 要插入的媒体的时间区间
     * @param asset     媒体对象，目前仅支持CAVUriAsset的AVAssetTrack，后续会扩展支持AVComposition的轨道
     * @param startTime 插入区间在轨道上的起始位置
     * @return          插入结果
     * 该方法可能会插入新的轨道以保证asset中的所有轨道都被展示在插入的时间区间中
     */
    private boolean insertTimeRange(@NonNull AVTimeRange timeRange, @NonNull CAVUriAsset asset,
                                    @NonNull AVTime startTime) {
        // 组合轨道为空的情况，将利用asset的轨道创建新的组合轨道并插入其中
        if (mTracks.size() == 0) {
            insertAllTracksFromCAVUriAsset(timeRange, asset, startTime);
            return true;
        }

        // 如果已经存在轨道，则判断是否轨道类型是否存在
        List<CAVAssetTrack> tracks = asset.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            CAVAssetTrack sourceTrack = tracks.get(i);
            // 判断是否需要加入一个媒体类型
            int index = -1;
            for (int j = 0; j < mTracks.size(); j++) {
                if (mTracks.get(j).getMediaType() == sourceTrack.getMediaType()) {
                    index = j;
                    break;
                }
            }

            // 如果遍历整个轨道列表都没找到该媒体类型，则要添加一个新的媒体类型
            if (index == -1) {
                // 创建一个轨道
                AVCompositionTrack track = addMutableTrackWithMediaType(sourceTrack.getMediaType());
                track.setNaturalTimeScale(sourceTrack.getNaturalTimeScale());
                // 插入一段位置
                track.insertTimeRange(timeRange, sourceTrack, startTime);
                mTracks.add(track);
            } else {
                // 直接在轨道中插入一个片段
                AVCompositionTrack track = mTracks.get(index);
                track.insertTimeRange(timeRange, sourceTrack, startTime);
            }
        }
        return true;
    }

    /**
     * 将源媒体所有轨道插入到组合媒体对象
     * @param timeRange
     * @param asset
     * @param startTime
     */
    private void insertAllTracksFromCAVUriAsset(@NonNull AVTimeRange timeRange,
                                                @NonNull CAVUriAsset asset,
                                                @NonNull AVTime startTime) {
        List<CAVAssetTrack> tracks = asset.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            CAVAssetTrack sourceTrack = tracks.get(i);
            // 创建一个轨道
            AVCompositionTrack track = addMutableTrackWithMediaType(sourceTrack.getMediaType());
            track.setNaturalTimeScale(sourceTrack.getNaturalTimeScale());
            // 插入一个轨道片段
            track.insertTimeRange(timeRange, sourceTrack, startTime);
            mTracks.add(track);
        }
        // 计算出插入之后的组合媒体时长
        mDuration = new AVTime(asset.getDuration().getValue(), asset.getDuration().getTimescale());
    }

    /**
     * 将一个组合媒体对象的的轨道插入到时间区间中
     * @param timeRange     要插入的媒体轨道时间区间
     * @param composition   组合媒体对象
     * @param startTime     插入区间在轨道上的起始位置
     * @return              返回插入的结果
     */
    private boolean insertTimeRange(@NonNull AVTimeRange timeRange,
                                    @NonNull AVComposition composition,
                                    @NonNull AVTime startTime) {

        // 组合轨道为空的情况，将composition的所有轨道复制并插入到组合轨道中
        if (mTracks.size() == 0) {
            insertAllTracksFromAVComposition(timeRange, composition, startTime);
            return true;
        }

        //
        List<AVCompositionTrack> tracks = composition.getTracks();
        for (int i = 0; i < tracks.size(); i++) {

        }

        return false;
    }

    /**
     * 将组合媒体对象中所有轨道数据复制到组合媒体轨道中
     * @param timeRange     要插入的媒体轨道时间区间
     * @param composition   组合媒体对象
     * @param startTime     插入区间在轨道上的起始位置
     */
    private void insertAllTracksFromAVComposition(@NonNull AVTimeRange timeRange,
                                                  @NonNull AVComposition composition,
                                                  @NonNull AVTime startTime) {
        List<AVCompositionTrack> tracks = composition.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            AVCompositionTrack sourceTrack = tracks.get(i);
            // 创建一个轨道
            AVCompositionTrack track = addMutableTrackWithMediaType(sourceTrack.getMediaType());
            track.setNaturalTimeScale(sourceTrack.getNaturalTimeScale());
            // 插入一个轨道片段
            track.insertTimeRange(timeRange, sourceTrack, startTime);
            mTracks.add(track);
        }

        // 计算出插入之后的组合媒体时长
        mDuration = new AVTime(composition.getDuration().getValue(),
                composition.getDuration().getTimescale());
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
    public static AVCompositionTrack mutableTrackCompatibleWithTrack(@NonNull AVAssetTrack track) {
        if (track instanceof CAVAssetTrack) {
            return mutableTrackWithTrack((CAVAssetTrack) track);
        } else if (track instanceof AVCompositionTrack) {
            return mutableTrackWithTrack((AVCompositionTrack) track);
        } else throw new IllegalArgumentException("unsupported media track!");
    }

    /**
     * 利用源媒体轨道创建一个组合媒体轨道
     * @param track 源媒体轨道
     * @return      组合媒体轨道
     */
    private static AVCompositionTrack mutableTrackWithTrack(@NonNull CAVAssetTrack track) {
        AVCompositionTrack compositionTrack = new AVCompositionTrack(track.getMediaType());
        compositionTrack.setAsset(track.getAsset());
        compositionTrack.setTrackID(track.getTrackID());
        compositionTrack.setTimeRange(track.getTimeRange());
        compositionTrack.setNaturalSize(track.getNaturalSize());
        compositionTrack.getPreferredTransform().set(track.getPreferredTransform());
        compositionTrack.setFrameReordering(track.isFrameReordering());
        // 将源媒体轨道片段数据插入到组合媒体中
        for (int i = 0; i < track.getTrackSegments().size(); i++) {
            AVAssetTrackSegment segment = track.getTrackSegments().get(i);
            AVTimeMapping mapping = segment.getTimeMapping();
            AVCompositionTrackSegment trackSegment = new AVCompositionTrackSegment(track.getUri(),
                    track.getTrackID(), mapping.getSource(), mapping.getTarget());
            compositionTrack.getTrackSegments().add(trackSegment);
        }
        return compositionTrack;
    }

    /**
     * 利用组合媒体轨道创建一个新的组合媒体轨道
     * @param track
     * @return
     */
    private static AVCompositionTrack mutableTrackWithTrack(@NonNull AVCompositionTrack track) {
        AVCompositionTrack compositionTrack = new AVCompositionTrack(track.getMediaType());
        compositionTrack.setAsset(track.getAsset());
        compositionTrack.setTrackID(track.getTrackID());
        compositionTrack.setTimeRange(track.getTimeRange());
        compositionTrack.setNaturalSize(track.getNaturalSize());
        compositionTrack.getPreferredTransform().set(track.getPreferredTransform());
        compositionTrack.setFrameReordering(track.isFrameReordering());
        // 如果这是一个空的媒体轨道，则直接插入一个空的轨道片段
        if (track.getTrackSegments().size() == 0) {
            compositionTrack.insertEmptyTimeRange(track.getTimeRange());
            return compositionTrack;
        }

        // 将组合轨道的片段数据复制并插入到组合媒体轨道中
        for (int i = 0; i < track.getTrackSegments().size(); i++) {
            AVCompositionTrackSegment segment = track.getTrackSegments().get(i);
            // 如果源媒体文件uri是空的，则插入一个空的轨道片段
            if (segment.getSourceUri() == null) {
                compositionTrack.getTrackSegments().add(
                        new AVCompositionTrackSegment(segment.getTimeMapping().getTarget()));
            } else {
                // 利用源媒体轨道构建一个新的媒体轨道片段
                Uri sourceUri = segment.getSourceUri();
                int sourceTrackID = segment.getSourceTrackID();
                AVTimeMapping mapping = segment.getTimeMapping();
                AVCompositionTrackSegment trackSegment = new AVCompositionTrackSegment(
                        sourceUri, sourceTrackID, mapping.getSource(), mapping.getTarget());
                compositionTrack.getTrackSegments().add(trackSegment);
            }
        }
        return compositionTrack;
    }

    /**
     * 根据轨道ID获取轨道对象
     * @param trackID 轨道ID
     * @return 轨道对象，如果找不到则返回null
     */
    @Nullable
    @Override
    public AVCompositionTrack getTrackWithTrackID(int trackID) {
        AVCompositionTrack result = null;
        for (AVCompositionTrack track : mTracks) {
            if (track.getTrackID() == trackID) {
                result = track;
                break;
            }
        }
        return result;
    }

    /**
     * 根据媒体类型获取轨道列表
     * @param type  媒体类型
     * @return      轨道列表
     */
    @Override
    public List<AVCompositionTrack> getTrackWithMediaType(AVMediaType type) {
        List<AVCompositionTrack> trackList = new ArrayList<>();
        for (AVCompositionTrack track : mTracks) {
            if (track.getMediaType() == type) {
                trackList.add(track);
            }
        }
        return trackList;
    }

    /**
     * 取消加载所有数值
     */
    @Override
    public void cancelLoading() {

    }

    /**
     * 获取源数据路径
     *
     * @return 源路径Uri
     */
    @Nullable
    @Override
    public Uri getUri() {
        return mUri;
    }

    /**
     * 设置组合媒体时长
     * @param duration  设置组合媒体时长
     */
    public void setDuration(@NonNull AVTime duration) {
        mDuration = duration;
    }

    /**
     * 获取时长
     */
    @NonNull
    @Override
    public AVTime getDuration() {
        return mDuration;
    }

    /**
     * 获取速度
     */
    @Override
    public float getPreferredRate() {
        return mPreferredRate;
    }

    /**
     * 获取音量
     */
    @Override
    public float getPreferredVolume() {
        return mPreferredVolume;
    }

    /**
     * 获取转换大小对象
     */
    @NonNull
    @Override
    public AffineTransform getPreferredTransform() {
        return mPreferredTransform;
    }

    /**
     * 获取视频帧大小
     */
    @NonNull
    @Override
    public CGSize getNaturalSize() {
        return mNaturalSize;
    }

    /**
     * 获取轨道信息
     */
    @NonNull
    @Override
    public List<AVCompositionTrack> getTracks() {
        return mTracks;
    }
}
