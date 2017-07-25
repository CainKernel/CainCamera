package com.cgfay.caincamera.core;

import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.filter.image.GuassFilter;
import com.cgfay.caincamera.filter.image.MirrorFilter;
import com.cgfay.caincamera.filter.image.SaturationFilter;

/**
 * Filter管理类
 * Created by cain on 17-7-25.
 */

public class FilterManager {

    private FilterManager() {}

    public static BaseImageFilter getFilter(FilterType type) {
        switch (type) {
            // 饱和度
            case SATURATION:
                return new SaturationFilter();
            // 镜像翻转
            case MIRROR:
                return new MirrorFilter();
            // 高斯模糊
            case GUASS:
                return new GuassFilter();
            default:
                return null;
        }
    }

}
