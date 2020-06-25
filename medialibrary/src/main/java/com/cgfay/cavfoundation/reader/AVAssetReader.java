package com.cgfay.cavfoundation.reader;

import androidx.annotation.Nullable;

import com.cgfay.cavfoundation.AVAsset;
import com.cgfay.coremedia.AVTimeRange;

import java.util.List;

public class AVAssetReader {

    private AVAsset mAVAsset;

    private AVAssetReaderStatus mStatus;

    private AVTimeRange mTimeRange;

    private List<AVAssetReaderOutput> mOutputs;

    @Nullable
    public AVAssetReader assetReaderWithAsset(AVAsset asset) {

        return null;
    }

    public void initWithAsset(AVAsset asset) {

    }

    /**
     * 判断是否可以添加输出
     */
    public boolean canAddOutput(AVAssetReaderOutput output) {
        return true;
    }

    /**
     * 添加提取输出对象
     * @param output
     */
    public void addOutput(AVAssetReaderOutput output) {

    }

    /**
     * 开始读取数据
     * @return 是否成功启动读取数据处理
     */
    public boolean startReading() {

        return true;
    }

    /**
     * 取消提取数据
     */
    public void cancelReading() {

    }

    public AVTimeRange getTimeRange() {
        return mTimeRange;
    }

    public void setTimeRange(AVTimeRange timeRange) {
        mTimeRange = timeRange;
    }

    public List<AVAssetReaderOutput> getOutputs() {
        return mOutputs;
    }
}
