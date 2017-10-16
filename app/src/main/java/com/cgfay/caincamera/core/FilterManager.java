package com.cgfay.caincamera.core;

import com.cgfay.caincamera.filter.advanced.SketchFilter;
import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.filter.base.DisplayFilter;
import com.cgfay.caincamera.filter.beauty.RealtimeBeautify;
import com.cgfay.caincamera.filter.beauty.WhitenOrReddenFilter;
import com.cgfay.caincamera.filter.image.BrightnessFilter;
import com.cgfay.caincamera.filter.image.ContrastFilter;
import com.cgfay.caincamera.filter.image.ExposureFilter;
import com.cgfay.caincamera.filter.image.GuassFilter;
import com.cgfay.caincamera.filter.image.HueFilter;
import com.cgfay.caincamera.filter.image.MirrorFilter;
import com.cgfay.caincamera.filter.image.SaturationFilter;
import com.cgfay.caincamera.filter.image.SharpnessFilter;
import com.cgfay.caincamera.filter.sticker.StickerFilter;
import com.cgfay.caincamera.type.FilterType;

/**
 * Filter管理类
 * Created by cain on 17-7-25.
 */

public class FilterManager {

    private FilterManager() {}

    public static BaseImageFilter getFilter(FilterType type) {
        switch (type) {
            //  颜色滤镜相关
            case SKETCH:
                return new SketchFilter();

            case STICKER:
                return new StickerFilter();
            // 美颜滤镜
            // 白皙还是红润
            case WHITENORREDDEN:
                return new WhitenOrReddenFilter();
            case REALTIMEBEAUTY:
                return new RealtimeBeautify();

            // 图片基本属性编辑滤镜
            // 饱和度
            case SATURATION:
                return new SaturationFilter();
            // 镜像翻转
            case MIRROR:
                return new MirrorFilter();
            // 高斯模糊
            case GUASS:
                return new GuassFilter();
            // 亮度
            case BRIGHTNESS:
                return new BrightnessFilter();
            // 对比度
            case CONTRAST:
                return new ContrastFilter();
            // 曝光
            case EXPOSURE:
                return new ExposureFilter();
            // 色调
            case HUE:
                return new HueFilter();
            // 锐度
            case SHARPNESS:
                return new SharpnessFilter();
            default:
                return new DisplayFilter();
        }
    }

}
