package com.cgfay.caincamera.core;

import com.cgfay.caincamera.type.FilterType;

import java.util.ArrayList;

/**
 * 色彩滤镜管理器
 * Created by cain on 2017/11/15.
 */

public final class ColorFilterManager {

    private static ColorFilterManager mInstance;


    private ArrayList<FilterType> mFilterType;

    public static ColorFilterManager getInstance() {
        if (mInstance == null) {
            mInstance = new ColorFilterManager();
        }
        return mInstance;
    }

    private ColorFilterManager() {
        initColorFilters();
    }


    /**
     * 初始化颜色滤镜
     */
    public void initColorFilters() {
        mFilterType = new ArrayList<FilterType>();

        mFilterType.add(FilterType.NONE);
        mFilterType.add(FilterType.ANTIQUE);
        mFilterType.add(FilterType.BLACKCAT);
        mFilterType.add(FilterType.BLACKWHITE);
        mFilterType.add(FilterType.COOL);
        mFilterType.add(FilterType.EMERALD);
        mFilterType.add(FilterType.EVERGREEN);
        mFilterType.add(FilterType.LATTE);
        mFilterType.add(FilterType.NOSTALGIA);
        mFilterType.add(FilterType.ROMANCE);
        mFilterType.add(FilterType.SAKURA);
        mFilterType.add(FilterType.SKETCH);
        mFilterType.add(FilterType.WHITECAT);
        mFilterType.add(FilterType.WHITENORREDDEN);
    }

    /**
     * 获取颜色滤镜类型
     * @param index
     * @return
     */
    public FilterType getColorFilterType(int index) {
        if (mFilterType == null || mFilterType.isEmpty()) {
            return FilterType.NONE;
        }
        int i = index % mFilterType.size();
        return mFilterType.get(i);
    }

    /**
     * 获取颜色滤镜数目
     * @return
     */
    public int getColorFilterCount() {
        return mFilterType == null ? 0 : mFilterType.size();
    }


}
