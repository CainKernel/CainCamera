package com.cgfay.caincamera.filter.beauty;

import com.cgfay.caincamera.core.FilterManager;
import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.filter.base.BaseImageFilterGroup;
import com.cgfay.caincamera.type.FilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * 实时美颜滤镜组
 * Created by cain on 2017/7/30.
 */
public class RealTimeBeautyFilterGroup extends BaseImageFilterGroup {

    private static final int mBeautyFilterIndex = 0;
    private static final int mWhitenOrReddenFilterIndex = 1;
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
        filters.add(mBeautyFilterIndex, FilterManager.getFilter(FilterType.REALTIMEBEAUTY));
        filters.add(mWhitenOrReddenFilterIndex, FilterManager.getFilter(FilterType.WHITENORREDDEN));
        return filters;
    }

    @Override
    public void changeFilter(FilterType type) {

    }
}
