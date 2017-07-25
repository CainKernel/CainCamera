package com.cgfay.caincamera.filter.base;

import com.cgfay.caincamera.filter.image.SaturationFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片编辑滤镜组，主要用来编辑图片的色温、亮度、饱和度、对比度等
 * Created by cain on 17-7-25.
 */
public class ImageEditFilterGroup extends BaseImageFilterGroup {

    private List<BaseImageFilter> initFilters() {
        List<BaseImageFilter> filters = new ArrayList<BaseImageFilter>();
        filters.add(new SaturationFilter()); // 饱和度
        return filters;
    }

    public void setSaturationLevel(float saturation) {
        ((SaturationFilter)mFilters.get(0)).setSaturationLevel(saturation);
    }

}
