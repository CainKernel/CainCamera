package com.cgfay.cainfilter.glfilter.group;

import com.cgfay.cainfilter.core.FilterManager;
import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilter;
import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilterGroup;
import com.cgfay.cainfilter.type.GlFilterType;
import com.cgfay.cainfilter.glfilter.image.GLBrightnessFilter;
import com.cgfay.cainfilter.glfilter.image.GLContrastFilter;
import com.cgfay.cainfilter.glfilter.image.GLExposureFilter;
import com.cgfay.cainfilter.glfilter.image.GLHueFilter;
import com.cgfay.cainfilter.glfilter.image.GLSaturationFilter;
import com.cgfay.cainfilter.glfilter.image.GLSharpnessFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片编辑滤镜组，主要用来编辑图片的色温、亮度、饱和度、对比度等
 * Created by cain on 17-7-25.
 */
public class GLImageFilterGroup extends GLBaseImageFilterGroup {

    private static final int BRIGHTNESS = 0;
    private static final int CONTRAST = 1;
    private static final int EXPOSURE = 2;
    private static final int HUE = 3;
    private static final int SATURATION = 4;
    private static final int SHARPNESS = 5;

    public GLImageFilterGroup() {
        this(initFilters());
    }

    public GLImageFilterGroup(List<GLBaseImageFilter> filters) {
        super(filters);
    }

    /**
     * 初始化滤镜
     * @return
     */
    private static List<GLBaseImageFilter> initFilters() {
        List<GLBaseImageFilter> filters = new ArrayList<GLBaseImageFilter>();

        filters.add(BRIGHTNESS, FilterManager.getFilter(GlFilterType.BRIGHTNESS)); // 亮度
        filters.add(CONTRAST, FilterManager.getFilter(GlFilterType.CONTRAST)); // 对比度
        filters.add(EXPOSURE, FilterManager.getFilter(GlFilterType.EXPOSURE)); // 曝光
        filters.add(HUE, FilterManager.getFilter(GlFilterType.HUE)); // 色调
        filters.add(SATURATION, FilterManager.getFilter(GlFilterType.SATURATION)); // 饱和度
        filters.add(SHARPNESS, FilterManager.getFilter(GlFilterType.SHARPNESS)); // 锐度

        return filters;
    }

    /**
     * 设置图片亮度
     */
    public void setBrightness(float brightness) {
        ((GLBrightnessFilter)mFilters.get(BRIGHTNESS)).setBrightness(brightness);
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        ((GLContrastFilter)mFilters.get(CONTRAST)).setContrast(contrast);
    }

    /**
     * 设置曝光值
     * @param exposure
     */
    public void setExposure(float exposure) {
        ((GLExposureFilter)mFilters.get(EXPOSURE)).setExposure(exposure);
    }

    /**
     * 设置色调
     * @param hue
     */
    public void setHue(float hue) {
        ((GLHueFilter)mFilters.get(HUE)).setHue(hue);
    }

    /**
     * 设置图片饱和度
     * @param saturation
     */
    public void setSaturation(float saturation) {
        ((GLSaturationFilter)mFilters.get(SATURATION)).setSaturationLevel(saturation);
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        ((GLSharpnessFilter)mFilters.get(SHARPNESS)).setSharpness(sharpness);
    }

    @Override
    public void setBeautifyLevel(float percent) {

    }

    @Override
    public void changeFilter(GlFilterType type) {

    }
}
