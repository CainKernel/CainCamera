package com.cgfay.filter.glfilter.color.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜数据
 */
public class DynamicColor {

    // 滤镜解压的文件夹路径
    public String unzipPath;

    // 滤镜列表
    public List<DynamicColorData> filterList;

    public DynamicColor() {
        filterList = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("unzipPath: ");
        builder.append(unzipPath);
        builder.append("\n");

        builder.append("data: [");
        for (int i = 0; i < filterList.size(); i++) {
            builder.append(filterList.get(i).toString());
            if (i < filterList.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");

        return builder.toString();
    }

}
