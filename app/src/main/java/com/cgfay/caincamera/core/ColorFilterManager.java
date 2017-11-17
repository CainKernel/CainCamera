package com.cgfay.caincamera.core;

import com.cgfay.caincamera.type.FilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * 色彩滤镜管理器
 * Created by cain on 2017/11/15.
 */

public final class ColorFilterManager {

    private static ColorFilterManager mInstance;


    private ArrayList<FilterType> mFilterType;
    private ArrayList<String> mFilterName;

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

        mFilterType.add(FilterType.SOURCE); // 原图
        mFilterType.add(FilterType.AMARO);
        mFilterType.add(FilterType.ANTIQUE);
        mFilterType.add(FilterType.BLACKCAT);
        mFilterType.add(FilterType.BLACKWHITE);
        mFilterType.add(FilterType.BROOKLYN);
        mFilterType.add(FilterType.CALM);
        mFilterType.add(FilterType.COOL);
        mFilterType.add(FilterType.EARLYBIRD);
        mFilterType.add(FilterType.EMERALD);
        mFilterType.add(FilterType.EVERGREEN);
        mFilterType.add(FilterType.FAIRYTALE);
        mFilterType.add(FilterType.FREUD);
        mFilterType.add(FilterType.HEALTHY);
        mFilterType.add(FilterType.HEFE);
        mFilterType.add(FilterType.HUDSON);
        mFilterType.add(FilterType.KEVIN);
        mFilterType.add(FilterType.LATTE);
        mFilterType.add(FilterType.LOMO);
        mFilterType.add(FilterType.NOSTALGIA);
        mFilterType.add(FilterType.ROMANCE);
        mFilterType.add(FilterType.SAKURA);
        mFilterType.add(FilterType.SKETCH);
        mFilterType.add(FilterType.SUNSET);
        mFilterType.add(FilterType.WHITECAT);



        mFilterName = new ArrayList<String>();
        mFilterName.add("原图");
        mFilterName.add("阿马罗");
        mFilterName.add("古董");
        mFilterName.add("黑猫");
        mFilterName.add("黑白");
        mFilterName.add("布鲁克林");
        mFilterName.add("冷静");
        mFilterName.add("冷色调");
        mFilterName.add("晨鸟");
        mFilterName.add("翡翠");
        mFilterName.add("常绿");
        mFilterName.add("童话");
        mFilterName.add("佛洛伊特");
        mFilterName.add("健康");
        mFilterName.add("酵母");
        mFilterName.add("哈德森");
        mFilterName.add("凯文");
        mFilterName.add("拿铁");
        mFilterName.add("LOMO");
        mFilterName.add("怀旧之情");
        mFilterName.add("浪漫");
        mFilterName.add("樱花");
        mFilterName.add("素描");
        mFilterName.add("日落");
        mFilterName.add("白猫");

    }

    /**
     * 获取颜色滤镜类型
     * @param index
     * @return
     */
    public FilterType getColorFilterType(int index) {
        if (mFilterType == null || mFilterType.isEmpty()) {
            return FilterType.SOURCE;
        }
        int i = index % mFilterType.size();
        return mFilterType.get(i);
    }

    /**
     * 获取颜色滤镜的名称
     * @param index
     * @return
     */
    public String getColorFilterName(int index) {
        if (mFilterName == null || mFilterName.isEmpty()) {
            return "原图";
        }
        int i = index % mFilterName.size();
        return mFilterName.get(i);
    }

    /**
     * 获取颜色滤镜数目
     * @return
     */
    public int getColorFilterCount() {
        return mFilterType == null ? 0 : mFilterType.size();
    }

    /**
     * 获取滤镜类型
     * @return
     */
    public List<FilterType> getFilterType() {
        return mFilterType;
    }

    /**
     * 获取滤镜名称
     * @return
     */
    public List<String> getFilterName() {
        return mFilterName;
    }

}
