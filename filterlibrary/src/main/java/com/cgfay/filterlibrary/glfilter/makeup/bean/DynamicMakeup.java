package com.cgfay.filterlibrary.glfilter.makeup.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态彩妆
 */
public class DynamicMakeup {

    // 彩妆解压的文件夹路径
    public String unzipPath;
    // 彩妆列表
    public List<MakeupBaseData> makeupList;

    public DynamicMakeup() {
        unzipPath = null;
        makeupList = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "DynamicMakeup{" +
                "unzipPath='" + unzipPath + '\'' +
                ", makeupList=" + makeupList +
                '}';
    }
}
