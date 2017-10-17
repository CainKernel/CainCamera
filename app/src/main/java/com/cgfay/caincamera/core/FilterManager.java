package com.cgfay.caincamera.core;

import com.cgfay.caincamera.filter.advanced.SketchFilter;
import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.filter.base.BaseImageFilterGroup;
import com.cgfay.caincamera.filter.base.DisplayFilter;
import com.cgfay.caincamera.filter.beauty.RealTimeBeautyFilterGroup;
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
import com.cgfay.caincamera.type.FilterIndex;
import com.cgfay.caincamera.type.FilterType;

import java.util.HashMap;

/**
 * Filter管理类
 * Created by cain on 17-7-25.
 */

public class FilterManager {

    private static HashMap<FilterType, FilterIndex> mIndexMap = new HashMap<FilterType, FilterIndex>();
    static {
        mIndexMap.put(FilterType.NONE, FilterIndex.NoneIndex);

        // 图片编辑
        mIndexMap.put(FilterType.BRIGHTNESS, FilterIndex.ImageEditIndex);
        mIndexMap.put(FilterType.CONTRAST, FilterIndex.ImageEditIndex);
        mIndexMap.put(FilterType.EXPOSURE, FilterIndex.ImageEditIndex);
        mIndexMap.put(FilterType.GUASS, FilterIndex.ImageEditIndex);
        mIndexMap.put(FilterType.HUE, FilterIndex.ImageEditIndex);
        mIndexMap.put(FilterType.MIRROR, FilterIndex.ImageEditIndex);
        mIndexMap.put(FilterType.SATURATION, FilterIndex.ImageEditIndex);
        mIndexMap.put(FilterType.SHARPNESS, FilterIndex.ImageEditIndex);


        // 美颜
        mIndexMap.put(FilterType.REALTIMEBEAUTY, FilterIndex.BeautyIndex);

        // 瘦脸大眼
        mIndexMap.put(FilterType.FACESTRETCH, FilterIndex.FaceStretchIndex);

        // 贴纸
        mIndexMap.put(FilterType.STICKER, FilterIndex.StickerIndex);

        // 彩妆
        mIndexMap.put(FilterType.MAKEUP, FilterIndex.MakeUpIndex);


        // 颜色滤镜
        mIndexMap.put(FilterType.WHITENORREDDEN, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.SKETCH, FilterIndex.ColorIndex);
    }

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

    /**
     * 获取滤镜组
     * @return
     */
    public static BaseImageFilterGroup getFilterGroup() {
        return new RealTimeBeautyFilterGroup();
    }

    /**
     * 获取层级
     * @param Type
     * @return
     */
    public static FilterIndex getIndex(FilterType Type) {
        FilterIndex index = mIndexMap.get(Type);
        if (index != null) {
            return index;
        }
        return FilterIndex.NoneIndex;
    }
}
