package com.cgfay.cavfoundation.reader;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.AVAsset;
import com.cgfay.cavfoundation.AVMediaType;
import com.cgfay.coremedia.AVTimeRange;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体资源数据读取器
 */
public class AVAssetReader {

    /**
     * 媒体对象
     */
    @NonNull
    private final AVAsset mAVAsset;

    /**
     * 读取状态
     */
    @NonNull
    protected AVAssetReaderStatus mStatus;

    /**
     * 读取的时间区间，默认kAVTimeRangeZero，表示全部读取
     */
    @NonNull
    private AVTimeRange mTimeRange;

    /**
     * 媒体读取输出接口
     */
    @NonNull
    private List<AVAssetReaderOutput> mOutputs = new ArrayList<>();

    /**
     * 媒体读取器内部实现
     */
    @NonNull
    private final CAVAssetReader mAssetReader;

    private AVAssetReader(@NonNull AVAsset asset) {
        mAVAsset = asset;
        mTimeRange = AVTimeRange.kAVTimeRangeZero;
        mStatus = AVAssetReaderStatus.AVAssetReaderStatusUnknown;
        mAssetReader = new CAVAssetReader(this);
    }

    @NonNull
    public static AVAssetReader assetReaderWithAsset(@NonNull AVAsset asset) {
        return new AVAssetReader(asset);
    }

    /**
     * 判断是否可以添加输出，目前仅支持音频和视频输出
     */
    public boolean canAddOutput(@NonNull AVAssetReaderOutput output) {
        if (output.getMediaType() == AVMediaType.AVMediaTypeAudio
                || output.getMediaType() == AVMediaType.AVMediaTypeVideo) {
            return true;
        }
        return false;
    }

    /**
     * 添加提取输出对象
     * @param output 输出接口
     */
    public void addOutput(@NonNull AVAssetReaderOutput output) {
        if (canAddOutput(output)) {
            mOutputs.add(output);
        }
    }

    /**
     * 开始读取数据
     * @return 是否成功启动读取数据处理
     */
    public boolean startReading() {
        // 如果没有可用的轨道，直接退出
        if (mAVAsset.getTracks().isEmpty()) {
            mStatus = AVAssetReaderStatus.AVAssetReaderStatusUnknown;
            return false;
        }
        return mAssetReader.startReading();
    }

    /**
     * 取消提取数据
     */
    public void cancelReading() {
        if (mAVAsset.getTracks().isEmpty()) {
            return;
        }
        mAssetReader.cancelReading();
    }

    /**
     * 获取时钟区间
     */
    @NonNull
    public AVTimeRange getTimeRange() {
        return mTimeRange;
    }

    /**
     * 设置时钟区间
     */
    public void setTimeRange(@NonNull AVTimeRange timeRange) {
        mTimeRange = timeRange;
    }

    /**
     * 获取媒体输出列表
     */
    @NonNull
    public List<AVAssetReaderOutput> getOutputs() {
        return mOutputs;
    }

    /**
     * 获取媒体资源对象
     */
    @NonNull
    public AVAsset getAVAsset() {
        return mAVAsset;
    }

}
