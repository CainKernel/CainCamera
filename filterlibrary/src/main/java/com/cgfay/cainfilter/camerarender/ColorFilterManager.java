package com.cgfay.cainfilter.camerarender;

import com.cgfay.cainfilter.type.GLFilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * 色彩滤镜管理器
 * Created by cain on 2017/11/15.
 */

public final class ColorFilterManager {

    private static ColorFilterManager mInstance;


    private ArrayList<GLFilterType> mGlFilterType;
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
        mGlFilterType = new ArrayList<GLFilterType>();

        mGlFilterType.add(GLFilterType.SOURCE); // 原图
        mGlFilterType.add(GLFilterType.AMARO);
        mGlFilterType.add(GLFilterType.ANTIQUE);
        mGlFilterType.add(GLFilterType.BLACKCAT);
        mGlFilterType.add(GLFilterType.BLACKWHITE);
        mGlFilterType.add(GLFilterType.BROOKLYN);
        mGlFilterType.add(GLFilterType.CALM);
        mGlFilterType.add(GLFilterType.COOL);
        mGlFilterType.add(GLFilterType.EARLYBIRD);
        mGlFilterType.add(GLFilterType.EMERALD);
        mGlFilterType.add(GLFilterType.EVERGREEN);
        mGlFilterType.add(GLFilterType.FAIRYTALE);
        mGlFilterType.add(GLFilterType.FREUD);
        mGlFilterType.add(GLFilterType.HEALTHY);
        mGlFilterType.add(GLFilterType.HEFE);
        mGlFilterType.add(GLFilterType.HUDSON);
        mGlFilterType.add(GLFilterType.KEVIN);
        mGlFilterType.add(GLFilterType.LATTE);
        mGlFilterType.add(GLFilterType.LOMO);
        mGlFilterType.add(GLFilterType.NOSTALGIA);
        mGlFilterType.add(GLFilterType.ROMANCE);
        mGlFilterType.add(GLFilterType.SAKURA);
        mGlFilterType.add(GLFilterType.SKETCH);
        mGlFilterType.add(GLFilterType.SUNSET);
        mGlFilterType.add(GLFilterType.WHITECAT);



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
    public GLFilterType getColorFilterType(int index) {
        if (mGlFilterType == null || mGlFilterType.isEmpty()) {
            return GLFilterType.SOURCE;
        }
        int i = index % mGlFilterType.size();
        return mGlFilterType.get(i);
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
        return mGlFilterType == null ? 0 : mGlFilterType.size();
    }

    /**
     * 获取滤镜类型
     * @return
     */
    public List<GLFilterType> getFilterType() {
        return mGlFilterType;
    }

    /**
     * 获取滤镜名称
     * @return
     */
    public List<String> getFilterName() {
        return mFilterName;
    }

}
