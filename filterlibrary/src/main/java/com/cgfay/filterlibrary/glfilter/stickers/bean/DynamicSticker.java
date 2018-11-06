package com.cgfay.filterlibrary.glfilter.stickers.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态贴纸
 */
public class DynamicSticker {
    // 贴纸解压的文件夹路径
    public String unzipPath;
    // 贴纸列表
    public List<DynamicStickerData> dataList;

    public DynamicSticker() {
        unzipPath = null;
        dataList = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "DynamicSticker{" +
                "unzipPath='" + unzipPath + '\'' +
                ", dataList=" + dataList +
                '}';
    }
}
