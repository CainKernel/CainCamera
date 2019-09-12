package com.cgfay.filter.glfilter.effect.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态特效
 */
public class DynamicEffect {

    // 滤镜解压的文件夹路径
    public String unzipPath;

    public List<DynamicEffectData> effectList;

    public DynamicEffect() {
        effectList = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("unzipPath: ");
        builder.append(unzipPath);
        builder.append("\n");

        builder.append("data: [");
        for (int i = 0; i < effectList.size(); i++) {
            builder.append(effectList.get(i).toString());
            if (i < effectList.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");

        return builder.toString();
    }
}
