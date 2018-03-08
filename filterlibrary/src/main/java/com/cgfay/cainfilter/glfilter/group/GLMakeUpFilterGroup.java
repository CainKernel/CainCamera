package com.cgfay.cainfilter.glfilter.group;

import com.cgfay.cainfilter.camerarender.FilterManager;
import com.cgfay.cainfilter.glfilter.base.GLImageFilter;
import com.cgfay.cainfilter.glfilter.base.GLImageFilterGroup;
import com.cgfay.cainfilter.glfilter.beauty.GLRealtimeBeautyFilter;
import com.cgfay.cainfilter.type.GLFilterIndex;
import com.cgfay.cainfilter.type.GLFilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * 彩妆滤镜组
 * Created by cain.huang on 2017/10/18.
 */

public class GLMakeUpFilterGroup extends GLImageFilterGroup {

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

    private GLMakeUpFilterGroup(List<GLImageFilter> filters) {
        super(filters);
    }


    private static List<GLImageFilter> initFilters() {
        List<GLImageFilter> filters = new ArrayList<GLImageFilter>();
        filters.add(BeautyfyIndex, FilterManager.getFilter(GLFilterType.REALTIMEBEAUTY));
        filters.add(ColorIndex, FilterManager.getFilter(GLFilterType.SKETCH));
        filters.add(FaceStretchIndex, FilterManager.getFilter(GLFilterType.FACESTRETCH));
        filters.add(MakeupIndex, FilterManager.getFilter(GLFilterType.MAKEUP));
        return filters;
    }

    @Override
    public void setBeautifyLevel(float percent) {
        ((GLRealtimeBeautyFilter)mFilters.get(BeautyfyIndex)).setSmoothOpacity(percent);
    }

    @Override
    public void changeFilter(GLFilterType type) {
        GLFilterIndex index = FilterManager.getIndex(type);
        if (index == GLFilterIndex.BeautyIndex) {
            changeBeautyFilter(type);
        } else if (index == GLFilterIndex.ColorIndex) {
            changeColorFilter(type);
        } else if (index == GLFilterIndex.FaceStretchIndex) {
            changeFaceStretchFilter(type);
        } else if (index == GLFilterIndex.MakeUpIndex) {
            changeMakeupFilter(type);
        } else if (index == GLFilterIndex.StickerIndex) {
            changeStickerFilter(type);
        }
    }

    /**
     * 切换美颜滤镜
     * @param type
     */
    private void changeBeautyFilter(GLFilterType type) {
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
    private void changeColorFilter(GLFilterType type) {
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
    private void changeFaceStretchFilter(GLFilterType type) {
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
    private void changeStickerFilter(GLFilterType type) {
        // Do nothing，贴纸滤镜在默认的滤镜组里面，不和彩妆同组
    }

    /**
     * 切换彩妆滤镜
     * @param type
     */
    private void changeMakeupFilter(GLFilterType type) {
        if (mFilters != null) {
            mFilters.get(MakeupIndex).release();
            mFilters.set(MakeupIndex, FilterManager.getFilter(type));
            // 设置宽高
            mFilters.get(MakeupIndex).onInputSizeChanged(mImageWidth, mImageHeight);
            mFilters.get(MakeupIndex).onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
    }
}
