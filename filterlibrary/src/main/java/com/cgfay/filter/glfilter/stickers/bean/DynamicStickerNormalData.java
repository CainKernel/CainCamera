package com.cgfay.filter.glfilter.stickers.bean;

import java.util.Arrays;

/**
 * 默认动态贴纸类型
 */
public class DynamicStickerNormalData extends DynamicStickerData {

    public int[] centerIndexList;   // 中心坐标索引列表，有可能是多个关键点计算中心点
    public float offsetX;           // 相对于贴纸中心坐标的x轴偏移像素
    public float offsetY;           // 相对于贴纸中心坐标的y轴偏移像素
    public float baseScale;         // 贴纸基准缩放倍数
    public int startIndex;          // 人脸起始索引，用于计算人脸的宽度
    public int endIndex;            // 人脸结束索引，用于计算人脸的宽度

    @Override
    public String toString() {
        return "DynamicStickerNormalData{" +
                "centerIndexList=" + Arrays.toString(centerIndexList) +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", baseScale=" + baseScale +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
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
