package com.cgfay.filterlibrary.glfilter.color;

import android.content.Context;
import android.text.TextUtils;

import com.cgfay.filterlibrary.glfilter.base.GLImageGroupFilter;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;

/**
 * 颜色滤镜
 */
public class GLImageDynamicColorFilter extends GLImageGroupFilter {

    public GLImageDynamicColorFilter(Context context, DynamicColor dynamicColor) {
        super(context);
        // 判断数据是否存在
        if (dynamicColor == null || dynamicColor.filterList == null
                || TextUtils.isEmpty(dynamicColor.unzipPath)) {
            return;
        }
        // 添加滤镜
        for (int i = 0; i < dynamicColor.filterList.size(); i++) {
            mFilters.add(new DynamicColorFilter(context, dynamicColor.filterList.get(i), dynamicColor.unzipPath));
        }
    }

    /**
     * 设置滤镜强度
     * @param strength
     */
    public void setStrength(float strength) {
        for (int i = 0; i < mFilters.size(); i++) {
            if (mFilters.get(i) != null && mFilters.get(i) instanceof DynamicColorBaseFilter) {
                ((DynamicColorBaseFilter) mFilters.get(i)).setStrength(strength);
            }
        }
    }
}
