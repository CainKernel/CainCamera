package com.cgfay.cainfilter.filter.group;

import com.cgfay.cainfilter.core.FilterManager;
import com.cgfay.cainfilter.filter.base.BaseImageFilter;
import com.cgfay.cainfilter.filter.base.BaseImageFilterGroup;
import com.cgfay.cainfilter.type.FilterType;
import com.cgfay.cainfilter.filter.image.BrightnessFilter;
import com.cgfay.cainfilter.filter.image.ContrastFilter;
import com.cgfay.cainfilter.filter.image.ExposureFilter;
import com.cgfay.cainfilter.filter.image.HueFilter;
import com.cgfay.cainfilter.filter.image.SaturationFilter;
import com.cgfay.cainfilter.filter.image.SharpnessFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片编辑滤镜组，主要用来编辑图片的色温、亮度、饱和度、对比度等
 * Created by cain on 17-7-25.
 */
public class ImageEditFilterGroup extends BaseImageFilterGroup {

    private static final int BRIGHTNESS = 0;
    private static final int CONTRAST = 1;
    private static final int EXPOSURE = 2;
    private static final int HUE = 3;
    private static final int SATURATION = 4;
    private static final int SHARPNESS = 5;

    public ImageEditFilterGroup() {
        this(initFilters());
    }

    public ImageEditFilterGroup(List<BaseImageFilter> filters) {
        super(filters);
    }

    /**
     * 初始化滤镜
     * @return
     */
    private static List<BaseImageFilter> initFilters() {
        List<BaseImageFilter> filters = new ArrayList<BaseImageFilter>();

        filters.add(BRIGHTNESS, FilterManager.getFilter(FilterType.BRIGHTNESS)); // 亮度
        filters.add(CONTRAST, FilterManager.getFilter(FilterType.CONTRAST)); // 对比度
        filters.add(EXPOSURE, FilterManager.getFilter(FilterType.EXPOSURE)); // 曝光
        filters.add(HUE, FilterManager.getFilter(FilterType.HUE)); // 色调
        filters.add(SATURATION, FilterManager.getFilter(FilterType.SATURATION)); // 饱和度
        filters.add(SHARPNESS, FilterManager.getFilter(FilterType.SHARPNESS)); // 锐度

        return filters;
    }

    /**
     * 设置图片亮度
     */
    public void setBrightness(float brightness) {
        ((BrightnessFilter)mFilters.get(BRIGHTNESS)).setBrightness(brightness);
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        ((ContrastFilter)mFilters.get(CONTRAST)).setContrast(contrast);
    }

    /**
     * 设置曝光值
     * @param exposure
     */
    public void setExposure(float exposure) {
        ((ExposureFilter)mFilters.get(EXPOSURE)).setExposure(exposure);
    }

    /**
     * 设置色调
     * @param hue
     */
    public void setHue(float hue) {
        ((HueFilter)mFilters.get(HUE)).setHue(hue);
    }

    /**
     * 设置图片饱和度
     * @param saturation
     */
    public void setSaturation(float saturation) {
        ((SaturationFilter)mFilters.get(SATURATION)).setSaturationLevel(saturation);
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        ((SharpnessFilter)mFilters.get(SHARPNESS)).setSharpness(sharpness);
    }

    @Override
    public void setBeautifyLevel(float percent) {

    }

    @Override
    public void changeFilter(FilterType type) {

    }
}
