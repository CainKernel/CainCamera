package com.cgfay.cavfoundation;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体资产对象
 */
public class AVAsset<T extends AVAssetTrack> {

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
    @NonNull
    protected AffineTransform mPreferredTransform;

    /**
     * 默认大小
     */
    @NonNull
    protected CGSize mNaturalSize;

    /**
     * 媒体轨道列表
     */
    protected List<T> mTracks = new ArrayList<>();

    /**
     * 根据路径创建媒体资源对象，耗时约50~100ms
     * @param path
     * @return
     */
    public static AVAsset assetWithPath(@NonNull String path) {
        CAVUriAsset asset = new CAVUriAsset();
        Uri uri = Uri.fromFile(new File(path));
        try {
            asset.mUri = uri;
            asset.setDataSource(path);
            asset.createAssetTrack(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return asset;
    }

    /**
     * 根据Uri获取媒体资源对象，耗时约50~100ms
     * @param uri   uri路径
     * @return      AVAsset对象
     */
    public static AVAsset assetWithUri(@NonNull Context context, @NonNull Uri uri) {
        CAVUriAsset asset = new CAVUriAsset();
        try {
            asset.mUri = uri;
            asset.setDataSource(context, uri, null);
            asset.createAssetTrack(uri);
            return asset;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return asset;
    }

    protected AVAsset() {
        mUri = null;
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
    public T getTrackWithTrackID(int trackID) {
        if (mTracks != null) {
            T result = null;
            for (T track : mTracks) {
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
    public List<T> getTrackWithMediaType(AVMediaType type) {
        List<T> trackList = new ArrayList<>();
        if (mTracks != null) {
            for (T track : mTracks) {
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
        // do nothing
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
    @NonNull
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
    @NonNull
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
    public List<? extends AVAssetTrack> getTracks() {
        return mTracks;
    }
}
