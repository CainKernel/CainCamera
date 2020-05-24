package com.cgfay.avfoundation;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体资产对象
 */
public class AVAsset {

    /**
     * 源文件Uri路径，处于编辑状态时，可以为空
     */
    @Nullable
    protected Uri mUri;

    /**
     * 媒体时长
     */
    @NonNull
    protected AVTime mDuration;

    /**
     * 默认播放速度，通常是1.0
     */
    protected float mPreferredRate;

    /**
     * 默认音量
     */
    protected float mPreferredVolume;

    /**
     * 默认转换对象
     */
    protected AffineTransform mPreferredTransform;

    /**
     * 默认大小
     */
    protected CGSize mNaturalSize;

    /**
     * 媒体轨道列表
     */
    protected List<AVAssetTrack> mTracks;

    /**
     * 根据Uri获取媒体资产
     * @param uri   uri路径
     * @return      AVAsset对象
     */
    public static AVAsset assetWithUri(@NonNull Uri uri) {
        AVAsset asset = new AVAsset();
        asset.mUri = uri;
        return asset;
    }

    protected AVAsset() {
        mTracks = new ArrayList<>();
        mPreferredRate = 1.0f;
        mPreferredVolume = 1.0f;
        mPreferredTransform = AffineTransform.kAffineTransformIdentity;
        mNaturalSize = CGSize.kSizeZero;
    }

    /**
     * 根据轨道ID获取轨道对象
     * @param trackID 轨道ID
     * @return 轨道对象，如果找不到则返回null
     */
    @Nullable
    public AVAssetTrack getTrackWithTrackID(int trackID) {
        if (mTracks != null) {
            AVAssetTrack result = null;
            for (AVAssetTrack track : mTracks) {
                if (track.getTrackID() == trackID) {
                    result = track;
                    break;
                }
            }
            return result;
        }
        return null;
    }

    /**
     * 根据媒体类型获取轨道列表
     * @param type  媒体类型
     * @return      轨道列表
     */
    public List<AVAssetTrack> getTrackWithMediaType(AVMediaType type) {
        List<AVAssetTrack> trackList = new ArrayList<>();
        if (mTracks != null) {
            for (AVAssetTrack track : mTracks) {
                if (track.getMediaType() == type) {
                    trackList.add(track);
                }
            }
        }
        return trackList;
    }

    /**
     * 取消加载所有数值
     */
    public void cancelLoading() {

    }

    /**
     * 获取源数据路径
     * @return 源路径Uri
     */
    @Nullable
    public Uri getUri() {
        return mUri;
    }

    /**
     * 获取时长
     */
    public AVTime getDuration() {
        return mDuration;
    }

    /**
     * 获取速度
     */
    public float getPreferredRate() {
        return mPreferredRate;
    }

    /**
     * 获取音量
     */
    public float getPreferredVolume() {
        return mPreferredVolume;
    }

    /**
     * 获取转换大小对象
     */
    public AffineTransform getPreferredTransform() {
        return mPreferredTransform;
    }

    /**
     * 获取视频帧大小
     */
    public CGSize getNaturalSize() {
        return mNaturalSize;
    }

    /**
     * 获取媒体轨道列表
     * @return
     */
    public List<AVAssetTrack> getTracks() {
        return mTracks;
    }
}
