package com.cgfay.filterlibrary.glfilter.stickers.bean;

/**
 * 默认动态贴纸类型
 */
public class StaticStickerNormalData extends DynamicStickerData {

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
