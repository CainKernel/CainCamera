package com.cgfay.cavfoundation.reader;

import androidx.annotation.NonNull;

import com.cgfay.coremedia.AVTimeRange;

import java.lang.ref.WeakReference;

/**
 * 媒体读取器实现类
 */
class CAVAssetReader {

    /**
     * 媒体读取器
     */
    private final WeakReference<AVAssetReader> mWeakReader;

    public CAVAssetReader(@NonNull AVAssetReader reader) {
        mWeakReader = new WeakReference<>(reader);
    }

    /**
     * 开始读取
     */
    public boolean startReading() {
        if (mWeakReader.get() == null) {
            return false;
        }
        AVAssetReader reader = mWeakReader.get();
        reader.mStatus = AVAssetReaderStatus.AVAssetReaderStatusReading;
        return true;
    }

    /**
     * 取消读取
     */
    public void cancelReading() {
        if (mWeakReader.get() == null) {
            return;
        }
        AVAssetReader reader = mWeakReader.get();
        reader.mStatus = AVAssetReaderStatus.AVAssetReaderStatusCancelled;
    }

    /**
     * 设置时钟区间
     */
    public void setTimeRange(@NonNull AVTimeRange timeRange) {

    }

}
