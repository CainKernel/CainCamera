package com.cgfay.filter.glfilter.stickers.bean;

/**
 * 动态贴纸前景数据
 */
public class DynamicStickerFrameData extends DynamicStickerData {
    // 对齐方式，0表示centerCrop, 1表示fitXY，2表示居中center
    public int alignMode;

    @Override
    public String toString() {
        return "DynamicStickerFrameData{" +
                "alignMode=" + alignMode +
                ", width=" + width +
                ", height=" + height +
                ", frames=" + frames +
                ", action=" + action +
                ", stickerName='" + stickerName + '\'' +
                ", duration=" + duration +
                ", stickerLooping=" + stickerLooping +
                ", audioPath='" + audioPath + '\'' +
                ", audioLooping=" + audioLooping +
                ", maxCount=" + maxCount +
                '}';
    }
}
