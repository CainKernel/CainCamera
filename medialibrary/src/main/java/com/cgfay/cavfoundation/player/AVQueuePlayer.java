package com.cgfay.cavfoundation.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AVQueuePlayer extends AVPlayer {

    public AVQueuePlayer() {
        super();
    }

    public AVQueuePlayer queuePlayerWithItems(List<AVPlayerItem> items) {
        AVQueuePlayer queuePlayer = new AVQueuePlayer();
        return queuePlayer;
    }

    public AVQueuePlayer initWithItems(List<AVPlayerItem> items) {
        return this;
    }

    /**
     * 切换到下一个播放item
     */
    public void advanceToNextItem() {

    }

    /**
     * 判断是否插入到某个播放item结尾
     * @param item
     * @param afterItem
     * @return
     */
    public boolean canInsertItem(@NonNull AVPlayerItem item, @Nullable AVPlayerItem afterItem) {
        return true;
    }

    /**
     * 插入一个播放器item
     * @param item
     * @param afterItem
     */
    public void insertItem(@NonNull AVPlayerItem item, @Nullable AVPlayerItem afterItem) {

    }

    /**
     * 移除播放器item
     */
    public void removeItem(AVPlayerItem item) {

    }

    /**
     * 移除所有播放items
     */
    public void removeAllItems() {

    }

//    /**
//     * 获取播放列表
//     * @return
//     */
//    public List<AVPlayerItem> getItems() {
//
//    }
}
