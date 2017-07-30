package com.cgfay.caincamera.filter.beauty;

import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.filter.base.BaseImageFilterGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 实时美颜滤镜组
 * Created by cain on 2017/7/30.
 */
public class RealTimeBeautyFilterGroup extends BaseImageFilterGroup {

    private static final int mWhitenOrReddenFilterIndex = 0;
    private static final int mBeautyFilterIndex = 1;
    private static final int mNarrowFaceFilterIndex = 2;
    private static final int mBigEyeFilterIndex = 3;
    private static final int mNarrowChinFilterIndex = 4;

    public RealTimeBeautyFilterGroup() {
        this(initFilters());
    }

    private RealTimeBeautyFilterGroup(List<BaseImageFilter> filters) {
        mFilters = filters;
    }

    public static List<BaseImageFilter> initFilters() {
        List<BaseImageFilter> filters = new ArrayList<BaseImageFilter>();
        filters.add(mWhitenOrReddenFilterIndex, new WhitenOrReddenFilter());
        filters.add(mBeautyFilterIndex, new BeautyFilter());
//        filters.add(mNarrowFaceFilterIndex, new NarrowFaceFilter());
//        filters.add(mBigEyeFilterIndex, new BigEyeFilter());
//        filters.add(mNarrowChinFilterIndex, new NarrowChinFilter());
        return filters;
    }

    /**
     * 设置美颜等级
     * @param level
     */
    public void setBeautyLevel(int level) {
        BeautyFilter filter = (BeautyFilter) mFilters.get(mBeautyFilterIndex);
        filter.setBeautyLevel(level);
    }

    /**
     * 设置美白还是红润值
     * @param reddenValue
     * @param whitenValue
     * @param pinkingValue
     */
    public void setWhitenOrReddenValue(float reddenValue, float whitenValue, float pinkingValue) {
        WhitenOrReddenFilter filter = (WhitenOrReddenFilter)
                mFilters.get(mWhitenOrReddenFilterIndex);
        filter.setReddenValue(reddenValue);
        filter.setWhitenValue(whitenValue);
        filter.setPinkingValue(pinkingValue);
    }
}
