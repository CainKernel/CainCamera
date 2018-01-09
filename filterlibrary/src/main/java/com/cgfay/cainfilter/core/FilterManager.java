package com.cgfay.cainfilter.core;

import com.cgfay.cainfilter.filter.advanced.SketchFilter;
import com.cgfay.cainfilter.filter.base.BaseImageFilter;
import com.cgfay.cainfilter.filter.base.BaseImageFilterGroup;
import com.cgfay.cainfilter.filter.base.DisplayFilter;
import com.cgfay.cainfilter.filter.beauty.RealtimeBeautify;
import com.cgfay.cainfilter.filter.beauty.WhitenOrReddenFilter;
import com.cgfay.cainfilter.filter.color.AmaroFilter;
import com.cgfay.cainfilter.filter.color.AnitqueFilter;
import com.cgfay.cainfilter.filter.color.BlackCatFilter;
import com.cgfay.cainfilter.filter.color.BlackWhiteFilter;
import com.cgfay.cainfilter.filter.color.BrooklynFilter;
import com.cgfay.cainfilter.filter.color.CalmFilter;
import com.cgfay.cainfilter.filter.color.CoolFilter;
import com.cgfay.cainfilter.filter.color.EarlyBirdFilter;
import com.cgfay.cainfilter.filter.color.EmeraldFilter;
import com.cgfay.cainfilter.filter.color.EvergreenFilter;
import com.cgfay.cainfilter.filter.color.FairyTaleFilter;
import com.cgfay.cainfilter.filter.color.FreudFilter;
import com.cgfay.cainfilter.filter.color.HealthyFilter;
import com.cgfay.cainfilter.filter.color.HefeFilter;
import com.cgfay.cainfilter.filter.color.HudsonFilter;
import com.cgfay.cainfilter.filter.color.KevinFilter;
import com.cgfay.cainfilter.filter.color.LatteFilter;
import com.cgfay.cainfilter.filter.color.LomoFilter;
import com.cgfay.cainfilter.filter.color.NostalgiaFilter;
import com.cgfay.cainfilter.filter.color.RomanceFilter;
import com.cgfay.cainfilter.filter.color.SakuraFilter;
import com.cgfay.cainfilter.filter.color.SunsetFilter;
import com.cgfay.cainfilter.filter.color.WhiteCatFilter;
import com.cgfay.cainfilter.filter.group.DefaultFilterGroup;
import com.cgfay.cainfilter.filter.group.MakeUpFilterGroup;
import com.cgfay.cainfilter.filter.image.BrightnessFilter;
import com.cgfay.cainfilter.filter.image.ContrastFilter;
import com.cgfay.cainfilter.filter.image.ExposureFilter;
import com.cgfay.cainfilter.filter.image.GuassFilter;
import com.cgfay.cainfilter.filter.image.HueFilter;
import com.cgfay.cainfilter.filter.image.MirrorFilter;
import com.cgfay.cainfilter.filter.image.SaturationFilter;
import com.cgfay.cainfilter.filter.image.SharpnessFilter;
import com.cgfay.cainfilter.type.FilterGroupType;
import com.cgfay.cainfilter.type.FilterIndex;
import com.cgfay.cainfilter.type.FilterType;

import java.util.HashMap;

/**
 * Filter管理类
 * Created by cain on 17-7-25.
 */

public final class FilterManager {

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

        // 水印
        mIndexMap.put(FilterType.WATERMASK, FilterIndex.WaterMaskIndex);

        // 美颜
        mIndexMap.put(FilterType.REALTIMEBEAUTY, FilterIndex.BeautyIndex);

        // 瘦脸大眼
        mIndexMap.put(FilterType.FACESTRETCH, FilterIndex.FaceStretchIndex);

        // 贴纸
        mIndexMap.put(FilterType.STICKER, FilterIndex.StickerIndex);

        // 彩妆
        mIndexMap.put(FilterType.MAKEUP, FilterIndex.MakeUpIndex);


        // 颜色滤镜
        mIndexMap.put(FilterType.AMARO, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.ANTIQUE, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.BLACKCAT, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.BLACKWHITE, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.BROOKLYN, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.CALM, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.COOL, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.EARLYBIRD, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.EMERALD, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.EVERGREEN, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.FAIRYTALE, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.FREUD, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.HEALTHY, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.HEFE, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.HUDSON, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.KEVIN, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.LATTE, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.LOMO, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.NOSTALGIA, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.ROMANCE, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.SAKURA, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.SKETCH, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.SOURCE, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.SUNSET, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.WHITECAT, FilterIndex.ColorIndex);
        mIndexMap.put(FilterType.WHITENORREDDEN, FilterIndex.ColorIndex);
    }

    private FilterManager() {}

    public static BaseImageFilter getFilter(FilterType type) {
        switch (type) {

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

            // TODO 贴纸滤镜需要人脸关键点计算得到
            case STICKER:
                return new DisplayFilter();
//                return new StickerFilter();

            // 白皙还是红润
            case WHITENORREDDEN:
                return new WhitenOrReddenFilter();
            // 实时磨皮
            case REALTIMEBEAUTY:
                return new RealtimeBeautify();

            // AMARO
            case AMARO:
                return new AmaroFilter();
            // 古董
            case ANTIQUE:
                return new AnitqueFilter();

            // 黑猫
            case BLACKCAT:
                return new BlackCatFilter();

            // 黑白
            case BLACKWHITE:
                return new BlackWhiteFilter();

            // 布鲁克林
            case BROOKLYN:
                return new BrooklynFilter();

            // 冷静
            case CALM:
                return new CalmFilter();

            // 冷色调
            case COOL:
                return new CoolFilter();

            // 晨鸟
            case EARLYBIRD:
                return new EarlyBirdFilter();

            // 翡翠
            case EMERALD:
                return new EmeraldFilter();

            // 常绿
            case EVERGREEN:
                return new EvergreenFilter();

            // 童话
            case FAIRYTALE:
                return new FairyTaleFilter();

            // 佛洛伊特
            case FREUD:
                return new FreudFilter();

            // 健康
            case HEALTHY:
                return new HealthyFilter();

            // 酵母
            case HEFE:
                return new HefeFilter();

            // 哈德森
            case HUDSON:
                return new HudsonFilter();

            // 凯文
            case KEVIN:
                return new KevinFilter();

            // 拿铁
            case LATTE:
                return new LatteFilter();

            // LOMO
            case LOMO:
                return new LomoFilter();

            // 怀旧之情
            case NOSTALGIA:
                return new NostalgiaFilter();

            // 浪漫
            case ROMANCE:
                return new RomanceFilter();

            // 樱花
            case SAKURA:
                return new SakuraFilter();

            //  素描
            case SKETCH:
                return new SketchFilter();

            // 日落
            case SUNSET:
                return new SunsetFilter();

            // 白猫
            case WHITECAT:
                return new WhiteCatFilter();

            case NONE:      // 没有滤镜
            case SOURCE:    // 原图
            default:
                return new DisplayFilter();
        }
    }

    /**
     * 获取滤镜组
     * @return
     */
    public static BaseImageFilterGroup getFilterGroup() {
        return new DefaultFilterGroup();
    }

    public static BaseImageFilterGroup getFilterGroup(FilterGroupType type) {
        switch (type) {
            // 彩妆滤镜组
            case MAKEUP:
                return new MakeUpFilterGroup();

            // 默认滤镜组
            case DEFAULT:
            default:
                return new DefaultFilterGroup();
        }
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
