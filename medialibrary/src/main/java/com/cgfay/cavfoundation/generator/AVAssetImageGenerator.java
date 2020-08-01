package com.cgfay.cavfoundation.generator;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.AVAsset;
import com.cgfay.cavfoundation.AVVideoComposition;
import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 缩略图读取器
 */
public class AVAssetImageGenerator {

    /**
     * 源媒体对象
     */
    @NonNull
    private AVAsset mAsset;

    /**
     * 是否应用轨道的mPreferredTransform，默认为false，仅支持90、180、270度
     */
    private boolean mAppliesPreferredTrackTransform;

    /**
     * 指定生成的图像最大尺寸。默认值CGSizeZero表示提取原始尺寸不做缩放
     */
    private CGSize mMaximumSize;

    /**
     * 渲染描述指令对象
     * 未指定mVideoComposition，则仅使用第一个启用的视频轨道
     * 如果指定了mVideoComposition，则会忽略mAppliesPreferredTrackTransform的值
     */
    private AVVideoComposition mVideoComposition;

    /**
     * 完成回调接口
     */
    private AVAssetImageGeneratorCompletionHandler mCompletionHandler;

    /**
     * 构造器
     */
    public AVAssetImageGenerator(@NonNull AVAsset asset) {
        mAsset = asset;
        mAppliesPreferredTrackTransform = false;
        mMaximumSize = CGSize.kSizeZero;
        mCompletionHandler = null;
    }

    /**
     * 异步生成一系列图片
     * @param requestedTimes    时间列表
     * @param handler           读取图片回调
     */
    public void generateImageAsyncForTimes(List<Long> requestedTimes,
                                           AVAssetImageGeneratorCompletionHandler handler) {
        mCompletionHandler = handler;
    }

    /**
     * 取消提取图片数据
     */
    public void cancelAllImageGeneration() {
        mCompletionHandler = null;
    }

    /**
     * 是否应用轨道的mPreferredTransform，默认为false，仅支持90、180、270度
     */
    public boolean isAppliesPreferredTrackTransform() {
        return mAppliesPreferredTrackTransform;
    }

    /**
     * 设置是否应用轨道的mPreferredTransform，默认为false，仅支持90、180、270度
     */
    public void setAppliesPreferredTrackTransform(boolean appliesPreferredTrackTransform) {
        mAppliesPreferredTrackTransform = appliesPreferredTrackTransform;
    }

    /**
     * 设置指定生成的图像最大尺寸
     */
    public void setMaximumSize(@NonNull CGSize size) {
        mMaximumSize = size;
    }

    /**
     * 获取指定生成的图像最大尺寸
     */
    public CGSize getMaximumSize() {
        return mMaximumSize;
    }

    @NonNull
    public AVAsset getAsset() {
        return mAsset;
    }

    /**
     * 设置渲染描述指令
     */
    public void setVideoComposition(AVVideoComposition videoComposition) {
        mVideoComposition = videoComposition;
    }

    /**
     * 获取渲染指令描述对象
     */
    public AVVideoComposition getVideoComposition() {
        return mVideoComposition;
    }

    /**
     * 图片提取完成回调接口
     */
    public interface AVAssetImageGeneratorCompletionHandler {

        /**
         * 创建完成回调
         * @param requestedTime 请求的时间
         * @param buffer        回调的缓冲区
         * @param actualTime    准确的时间
         * @param result        缩略图提取器
         * @param error         出错信息
         */
        void onGenerateCompletion(AVTime requestedTime, ByteBuffer buffer, AVTime actualTime,
                                  AVAssetImageGeneratorResult result, String error);
    }

}
