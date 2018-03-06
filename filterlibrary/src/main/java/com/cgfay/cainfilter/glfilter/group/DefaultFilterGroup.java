package com.cgfay.cainfilter.glfilter.group;

import com.cgfay.cainfilter.core.FilterManager;
import com.cgfay.cainfilter.glfilter.base.BaseImageFilter;
import com.cgfay.cainfilter.glfilter.base.BaseImageFilterGroup;
import com.cgfay.cainfilter.glfilter.beauty.RealtimeBeautify;
import com.cgfay.cainfilter.type.FilterIndex;
import com.cgfay.cainfilter.type.FilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认实时美颜滤镜组
 * Created by cain on 2017/7/30.
 */
public class DefaultFilterGroup extends BaseImageFilterGroup {
    // 实时美颜层
    private static final int BeautyfyIndex = 0;
    // 颜色层
    private static final int ColorIndex = 1;
    // 瘦脸大眼层
    private static final int FaceStretchIndex = 2;
    // 贴纸
    private static final int StickersIndex = 3;

    public DefaultFilterGroup() {
        this(initFilters());
    }

    private DefaultFilterGroup(List<BaseImageFilter> filters) {
        mFilters = filters;
    }

    private static List<BaseImageFilter> initFilters() {
        List<BaseImageFilter> filters = new ArrayList<BaseImageFilter>();
        filters.add(BeautyfyIndex, FilterManager.getFilter(FilterType.REALTIMEBEAUTY));
        filters.add(ColorIndex, FilterManager.getFilter(FilterType.SOURCE));
        filters.add(FaceStretchIndex, FilterManager.getFilter(FilterType.FACESTRETCH));
        filters.add(StickersIndex, FilterManager.getFilter(FilterType.STICKER));
        return filters;
    }

    @Override
    public void setBeautifyLevel(float percent) {
        ((RealtimeBeautify)mFilters.get(BeautyfyIndex)).setSmoothOpacity(percent);
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
        if (mFilters != null) {
            mFilters.get(FaceStretchIndex).release();
            mFilters.set(FaceStretchIndex, FilterManager.getFilter(type));
            // 设置宽高
            mFilters.get(FaceStretchIndex).onInputSizeChanged(mImageWidth, mImageHeight);
            mFilters.get(FaceStretchIndex).onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
    }

    /**
     * 切换贴纸滤镜
     * @param type
     */
    private void changeStickerFilter(FilterType type) {
        if (mFilters != null) {
            mFilters.get(StickersIndex).release();
            mFilters.set(StickersIndex, FilterManager.getFilter(type));
            // 设置宽高
            mFilters.get(StickersIndex).onInputSizeChanged(mImageWidth, mImageHeight);
            mFilters.get(StickersIndex).onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
    }

    /**
     * 切换彩妆滤镜
     * @param type
     */
    private void changeMakeupFilter(FilterType type) {
        // Do nothing, 彩妆滤镜放在彩妆滤镜组里面
    }
}
