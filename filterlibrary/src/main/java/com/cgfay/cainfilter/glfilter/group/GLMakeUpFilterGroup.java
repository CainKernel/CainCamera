package com.cgfay.cainfilter.glfilter.group;

import com.cgfay.cainfilter.core.FilterManager;
import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilter;
import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilterGroup;
import com.cgfay.cainfilter.glfilter.beauty.GLRealtimeBeautyFilter;
import com.cgfay.cainfilter.type.GlFilterIndex;
import com.cgfay.cainfilter.type.GlFilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * 彩妆滤镜组
 * Created by cain.huang on 2017/10/18.
 */

public class GLMakeUpFilterGroup extends GLBaseImageFilterGroup {

    // 实时美颜层
    private static final int BeautyfyIndex = 0;
    // 颜色层
    private static final int ColorIndex = 1;
    // 瘦脸大眼层
    private static final int FaceStretchIndex = 2;
    // 美妆层
    private static final int MakeupIndex = 3;

    public GLMakeUpFilterGroup() {
        this(initFilters());
    }

    private GLMakeUpFilterGroup(List<GLBaseImageFilter> filters) {
        super(filters);
    }


    private static List<GLBaseImageFilter> initFilters() {
        List<GLBaseImageFilter> filters = new ArrayList<GLBaseImageFilter>();
        filters.add(BeautyfyIndex, FilterManager.getFilter(GlFilterType.REALTIMEBEAUTY));
        filters.add(ColorIndex, FilterManager.getFilter(GlFilterType.SKETCH));
        filters.add(FaceStretchIndex, FilterManager.getFilter(GlFilterType.FACESTRETCH));
        filters.add(MakeupIndex, FilterManager.getFilter(GlFilterType.MAKEUP));
        return filters;
    }

    @Override
    public void setBeautifyLevel(float percent) {
        ((GLRealtimeBeautyFilter)mFilters.get(BeautyfyIndex)).setSmoothOpacity(percent);
    }

    @Override
    public void changeFilter(GlFilterType type) {
        GlFilterIndex index = FilterManager.getIndex(type);
        if (index == GlFilterIndex.BeautyIndex) {
            changeBeautyFilter(type);
        } else if (index == GlFilterIndex.ColorIndex) {
            changeColorFilter(type);
        } else if (index == GlFilterIndex.FaceStretchIndex) {
            changeFaceStretchFilter(type);
        } else if (index == GlFilterIndex.MakeUpIndex) {
            changeMakeupFilter(type);
        } else if (index == GlFilterIndex.StickerIndex) {
            changeStickerFilter(type);
        }
    }

    /**
     * 切换美颜滤镜
     * @param type
     */
    private void changeBeautyFilter(GlFilterType type) {
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
    private void changeColorFilter(GlFilterType type) {
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
    private void changeFaceStretchFilter(GlFilterType type) {
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
    private void changeStickerFilter(GlFilterType type) {
        // Do nothing，贴纸滤镜在默认的滤镜组里面，不和彩妆同组
    }

    /**
     * 切换彩妆滤镜
     * @param type
     */
    private void changeMakeupFilter(GlFilterType type) {
        if (mFilters != null) {
            mFilters.get(MakeupIndex).release();
            mFilters.set(MakeupIndex, FilterManager.getFilter(type));
            // 设置宽高
            mFilters.get(MakeupIndex).onInputSizeChanged(mImageWidth, mImageHeight);
            mFilters.get(MakeupIndex).onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
    }
}
