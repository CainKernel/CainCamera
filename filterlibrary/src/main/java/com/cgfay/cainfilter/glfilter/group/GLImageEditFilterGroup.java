package com.cgfay.cainfilter.glfilter.group;

import com.cgfay.cainfilter.camerarender.FilterManager;
import com.cgfay.cainfilter.glfilter.base.GLImageFilter;
import com.cgfay.cainfilter.type.GLFilterIndex;
import com.cgfay.cainfilter.type.GLFilterType;
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
public class GLImageEditFilterGroup extends com.cgfay.cainfilter.glfilter.base.GLImageFilterGroup {

    private static final int BRIGHTNESS = 0;
    private static final int CONTRAST = 1;
    private static final int EXPOSURE = 2;
    private static final int HUE = 3;
    private static final int SATURATION = 4;
    private static final int SHARPNESS = 5;
    private static final int FILTERS = 6;

    public GLImageEditFilterGroup() {
        this(initFilters());
    }

    public GLImageEditFilterGroup(List<GLImageFilter> filters) {
        super(filters);
    }

    /**
     * 初始化滤镜
     * @return
     */
    private static List<GLImageFilter> initFilters() {
        List<GLImageFilter> filters = new ArrayList<GLImageFilter>();

        filters.add(BRIGHTNESS, FilterManager.getFilter(GLFilterType.BRIGHTNESS)); // 亮度
        filters.add(CONTRAST, FilterManager.getFilter(GLFilterType.CONTRAST)); // 对比度
        filters.add(EXPOSURE, FilterManager.getFilter(GLFilterType.EXPOSURE)); // 曝光
        filters.add(HUE, FilterManager.getFilter(GLFilterType.HUE)); // 色调
        filters.add(SATURATION, FilterManager.getFilter(GLFilterType.SATURATION)); // 饱和度
        filters.add(SHARPNESS, FilterManager.getFilter(GLFilterType.SHARPNESS)); // 锐度
        filters.add(FILTERS, FilterManager.getFilter(GLFilterType.NONE)); // 滤镜

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
        // do nothing
    }

    @Override
    public void changeFilter(GLFilterType type) {
        GLFilterIndex index = FilterManager.getIndex(type);
        if (index == GLFilterIndex.ColorIndex) {
            if (mFilters != null) {
                mFilters.get(FILTERS).release();
                mFilters.set(FILTERS, FilterManager.getFilter(type));
                mFilters.get(FILTERS).onInputSizeChanged(mImageWidth, mImageHeight);
                mFilters.get(FILTERS).onDisplayChanged(mDisplayWidth, mDisplayHeight);
            }
        }
    }
}
