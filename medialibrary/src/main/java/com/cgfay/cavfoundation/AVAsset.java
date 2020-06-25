package com.cgfay.cavfoundation;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;

import java.util.List;

/**
 * 媒体资产对象
 */
public interface AVAsset {

    /**
     * 根据路径创建媒体资源对象，耗时约50~100ms
     * @param path
     * @return
     */
    static AVAsset assetWithPath(@NonNull String path) {
        return CAVUriAsset.assetWithPath(path);
    }

    /**
     * 根据Uri获取媒体资源对象，耗时约50~100ms
     * @param uri   uri路径
     * @return      AVAsset对象
     */
    static AVAsset assetWithUri(@NonNull Context context, @NonNull Uri uri) {
        return CAVUriAsset.assetWithUri(context, uri);
    }

    /**
     * 根据轨道ID获取轨道对象
     * @param trackID 轨道ID
     * @return 轨道对象，如果找不到则返回null
     */
    @Nullable
    AVAssetTrack getTrackWithTrackID(int trackID);

    /**
     * 根据媒体类型获取轨道列表
     * @param type  媒体类型
     * @return      轨道列表
     */
    List<? extends AVAssetTrack> getTrackWithMediaType(AVMediaType type);

    /**
     * 取消加载所有数值
     */
    void cancelLoading();

    /**
     * 获取源数据路径
     * @return 源路径Uri
     */
    @Nullable
    Uri getUri();

    /**
     * 获取时长
     */
    @NonNull
    AVTime getDuration();

    /**
     * 获取速度
     */
    float getPreferredRate();

    /**
     * 获取音量
     */
    float getPreferredVolume();

    /**
     * 获取转换大小对象
     */
    @NonNull
    AffineTransform getPreferredTransform();

    /**
     * 获取视频帧大小
     */
    CGSize getNaturalSize();

    /**
     * 获取媒体轨道列表
     */
    @NonNull
    List<? extends AVAssetTrack> getTracks();
}
