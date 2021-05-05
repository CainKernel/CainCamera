package com.cgfay.picker;

import java.io.Serializable;

/**
 * 媒体扫描参数
 */
public final class MediaPickerParam implements Serializable {

    // 是否显示拍照Item
    private boolean mShowCapture;
    // 是否显示图片Item
    private boolean mShowImage;
    // 是否显示视频Item
    private boolean mShowVideo;
    // 横向item的数量
    private int mSpanCount;
    // 分割线大小
    private int mSpaceSize;
    // 是否显示边沿分割线
    private boolean mHasEdge;
    // 是否自动销毁
    private boolean mAutoDismiss;

    public MediaPickerParam() {
        reset();
    }

    /**
     * 重置为初始状态
     */
    private void reset() {
        mShowCapture = true;
        mShowImage = true;
        mShowVideo = true;
        mSpanCount = 4;
        mSpaceSize = 4;
        mHasEdge = true;
        mAutoDismiss = false;
    }

    public void setShowCapture(boolean show) {
        mShowCapture = show;
    }

    public boolean isShowCapture() {
        return mShowCapture;
    }

    public void setShowImage(boolean show) {
        mShowImage = show;
    }

    public boolean isShowImage() {
        return mShowImage;
    }

    public void setShowVideo(boolean show) {
        mShowVideo = show;
    }

    public boolean isShowVideo() {
        return mShowVideo;
    }

    public void setSpanCount(int count) {
        mSpanCount = count;
    }

    public int getSpanCount() {
        return mSpanCount;
    }

    public void setSpaceSize(int size) {
        mSpaceSize = size;
    }

    public int getSpaceSize() {
        return mSpaceSize;
    }

    public void setItemHasEdge(boolean hasEdge) {
        mHasEdge = hasEdge;
    }

    public boolean isHasEdge() {
        return mHasEdge;
    }

    public boolean isAutoDismiss() {
        return mAutoDismiss;
    }

    public void setAutoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
    }

    /**
     * 仅显示图片
     * @return
     */
    public boolean showImageOnly() {
        return mShowImage && !mShowVideo;
    }

    /**
     * 仅显示视频
     * @return
     */
    public boolean showVideoOnly() {
        return mShowVideo && !mShowImage;
    }

}
