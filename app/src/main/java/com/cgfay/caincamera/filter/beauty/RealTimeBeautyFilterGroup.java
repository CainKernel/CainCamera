package com.cgfay.caincamera.filter.beauty;

import com.cgfay.caincamera.core.FilterManager;
import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.filter.base.BaseImageFilterGroup;
import com.cgfay.caincamera.type.FilterIndex;
import com.cgfay.caincamera.type.FilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * 实时美颜滤镜组
 * Created by cain on 2017/7/30.
 */
public class RealTimeBeautyFilterGroup extends BaseImageFilterGroup {
    // 实时美颜层
    private static final int BeautyfyIndex = 0;
    // 颜色层
    private static final int ColorIndex = 1;
    // 瘦脸大眼层
    private static final int FaceStretchIndex = 2;

    public RealTimeBeautyFilterGroup() {
        this(initFilters());
    }

    private RealTimeBeautyFilterGroup(List<BaseImageFilter> filters) {
        mFilters = filters;
    }

    public static List<BaseImageFilter> initFilters() {
        List<BaseImageFilter> filters = new ArrayList<BaseImageFilter>();
        filters.add(BeautyfyIndex, FilterManager.getFilter(FilterType.REALTIMEBEAUTY));
        filters.add(ColorIndex, FilterManager.getFilter(FilterType.SKETCH));
        return filters;
    }

    @Override
    public void changeFilter(FilterType type) {
        FilterIndex index = FilterManager.getIndex(type);
        if (index == FilterIndex.BeautyIndex) {
            changeBeautyFilter(type);
        } else if (index == FilterIndex.ColorIndex) {
            changeColorFilter(type);
        } else if (index == FilterIndex.FaceStretchIndex) {
            changeFaceStretchFilter(type);
        } else if (index == FilterIndex.MakeUpIndex) {
            changeMakeupFilter(type);
        } else if (index == FilterIndex.StickerIndex) {
            changeStickerFilter(type);
        }
    }

    /**
     * 切换美颜滤镜
     * @param type
     */
    private void changeBeautyFilter(FilterType type) {
        if (mFilters != null) {
            mFilters.get(BeautyfyIndex).release();
            mFilters.set(BeautyfyIndex, FilterManager.getFilter(type));
            // 设置宽高
            mFilters.get(BeautyfyIndex).onInputSizeChanged(mImageWidth, mImageHeight);
            mFilters.get(BeautyfyIndex).onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
    }

    /**
     * 切换颜色滤镜
     * @param type
     */
    private void changeColorFilter(FilterType type) {
        if (mFilters != null) {
            mFilters.get(ColorIndex).release();
            mFilters.set(ColorIndex, FilterManager.getFilter(type));
            // 设置宽高
            mFilters.get(ColorIndex).onInputSizeChanged(mImageWidth, mImageHeight);
            mFilters.get(ColorIndex).onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
    }

    /**
     * 切换瘦脸大眼滤镜
     * @param type
     */
    private void changeFaceStretchFilter(FilterType type) {

    }

    /**
     * 切换贴纸滤镜
     * @param type
     */
    private void changeStickerFilter(FilterType type) {

    }

    /**
     * 切换彩妆滤镜
     * @param type
     */
    private void changeMakeupFilter(FilterType type) {

    }
}
