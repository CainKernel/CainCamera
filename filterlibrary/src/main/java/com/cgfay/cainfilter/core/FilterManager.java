package com.cgfay.cainfilter.core;

import com.cgfay.cainfilter.glfilter.advanced.GLSketchFilter;
import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilter;
import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilterGroup;
import com.cgfay.cainfilter.glfilter.base.GLDisplayFilter;
import com.cgfay.cainfilter.glfilter.beauty.GLRealtimeBeautyFilter;
import com.cgfay.cainfilter.glfilter.beauty.WhitenOrReddenFilter;
import com.cgfay.cainfilter.glfilter.color.GLAmaroFilter;
import com.cgfay.cainfilter.glfilter.color.GLAnitqueFilter;
import com.cgfay.cainfilter.glfilter.color.GLBlackCatFilter;
import com.cgfay.cainfilter.glfilter.color.GLBlackWhiteFilter;
import com.cgfay.cainfilter.glfilter.color.GLBrooklynFilter;
import com.cgfay.cainfilter.glfilter.color.GLCalmFilter;
import com.cgfay.cainfilter.glfilter.color.GLCoolFilter;
import com.cgfay.cainfilter.glfilter.color.GLEarlyBirdFilter;
import com.cgfay.cainfilter.glfilter.color.GLEmeraldFilter;
import com.cgfay.cainfilter.glfilter.color.GLEvergreenFilter;
import com.cgfay.cainfilter.glfilter.color.GLFairyTaleFilter;
import com.cgfay.cainfilter.glfilter.color.GLFreudFilter;
import com.cgfay.cainfilter.glfilter.color.GLHealthyFilter;
import com.cgfay.cainfilter.glfilter.color.GLHefeFilter;
import com.cgfay.cainfilter.glfilter.color.GLHudsonFilter;
import com.cgfay.cainfilter.glfilter.color.GLKevinFilter;
import com.cgfay.cainfilter.glfilter.color.GLLatteFilter;
import com.cgfay.cainfilter.glfilter.color.GLLomoFilter;
import com.cgfay.cainfilter.glfilter.color.GLNostalgiaFilter;
import com.cgfay.cainfilter.glfilter.color.GLRomanceFilter;
import com.cgfay.cainfilter.glfilter.color.GLSakuraFilter;
import com.cgfay.cainfilter.glfilter.color.GLSunsetFilter;
import com.cgfay.cainfilter.glfilter.color.GLWhiteCatFilter;
import com.cgfay.cainfilter.glfilter.group.GLDefaultFilterGroup;
import com.cgfay.cainfilter.glfilter.group.GLMakeUpFilterGroup;
import com.cgfay.cainfilter.glfilter.image.GLBrightnessFilter;
import com.cgfay.cainfilter.glfilter.image.GLContrastFilter;
import com.cgfay.cainfilter.glfilter.image.GLExposureFilter;
import com.cgfay.cainfilter.glfilter.image.GLGuassFilter;
import com.cgfay.cainfilter.glfilter.image.GLHueFilter;
import com.cgfay.cainfilter.glfilter.image.GLMirrorFilter;
import com.cgfay.cainfilter.glfilter.image.GLSaturationFilter;
import com.cgfay.cainfilter.glfilter.image.GLSharpnessFilter;
import com.cgfay.cainfilter.glfilter.sticker.GLStickerFilter;
import com.cgfay.cainfilter.type.GlFilterGroupType;
import com.cgfay.cainfilter.type.GlFilterIndex;
import com.cgfay.cainfilter.type.GlFilterType;

import java.util.HashMap;

/**
 * Filter管理类
 * Created by cain on 17-7-25.
 */

public final class FilterManager {

    private static HashMap<GlFilterType, GlFilterIndex> mIndexMap = new HashMap<GlFilterType, GlFilterIndex>();
    static {
        mIndexMap.put(GlFilterType.NONE, GlFilterIndex.NoneIndex);

        // 图片编辑
        mIndexMap.put(GlFilterType.BRIGHTNESS, GlFilterIndex.ImageEditIndex);
        mIndexMap.put(GlFilterType.CONTRAST, GlFilterIndex.ImageEditIndex);
        mIndexMap.put(GlFilterType.EXPOSURE, GlFilterIndex.ImageEditIndex);
        mIndexMap.put(GlFilterType.GUASS, GlFilterIndex.ImageEditIndex);
        mIndexMap.put(GlFilterType.HUE, GlFilterIndex.ImageEditIndex);
        mIndexMap.put(GlFilterType.MIRROR, GlFilterIndex.ImageEditIndex);
        mIndexMap.put(GlFilterType.SATURATION, GlFilterIndex.ImageEditIndex);
        mIndexMap.put(GlFilterType.SHARPNESS, GlFilterIndex.ImageEditIndex);

        // 水印
        mIndexMap.put(GlFilterType.WATERMASK, GlFilterIndex.WaterMaskIndex);

        // 美颜
        mIndexMap.put(GlFilterType.REALTIMEBEAUTY, GlFilterIndex.BeautyIndex);

        // 瘦脸大眼
        mIndexMap.put(GlFilterType.FACESTRETCH, GlFilterIndex.FaceStretchIndex);

        // 贴纸
        mIndexMap.put(GlFilterType.STICKER, GlFilterIndex.StickerIndex);

        // 彩妆
        mIndexMap.put(GlFilterType.MAKEUP, GlFilterIndex.MakeUpIndex);


        // 颜色滤镜
        mIndexMap.put(GlFilterType.AMARO, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.ANTIQUE, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.BLACKCAT, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.BLACKWHITE, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.BROOKLYN, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.CALM, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.COOL, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.EARLYBIRD, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.EMERALD, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.EVERGREEN, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.FAIRYTALE, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.FREUD, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.HEALTHY, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.HEFE, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.HUDSON, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.KEVIN, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.LATTE, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.LOMO, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.NOSTALGIA, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.ROMANCE, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.SAKURA, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.SKETCH, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.SOURCE, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.SUNSET, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.WHITECAT, GlFilterIndex.ColorIndex);
        mIndexMap.put(GlFilterType.WHITENORREDDEN, GlFilterIndex.ColorIndex);
    }

    private FilterManager() {}

    public static GLBaseImageFilter getFilter(GlFilterType type) {
        switch (type) {

            // 图片基本属性编辑滤镜
            // 饱和度
            case SATURATION:
                return new GLSaturationFilter();
            // 镜像翻转
            case MIRROR:
                return new GLMirrorFilter();
            // 高斯模糊
            case GUASS:
                return new GLGuassFilter();
            // 亮度
            case BRIGHTNESS:
                return new GLBrightnessFilter();
            // 对比度
            case CONTRAST:
                return new GLContrastFilter();
            // 曝光
            case EXPOSURE:
                return new GLExposureFilter();
            // 色调
            case HUE:
                return new GLHueFilter();
            // 锐度
            case SHARPNESS:
                return new GLSharpnessFilter();

            // TODO 贴纸滤镜需要人脸关键点计算得到
            case STICKER:
//                return new DisplayFilter();
                return new GLStickerFilter();

            // 白皙还是红润
            case WHITENORREDDEN:
                return new WhitenOrReddenFilter();
            // 实时磨皮
            case REALTIMEBEAUTY:
                return new GLRealtimeBeautyFilter();

            // AMARO
            case AMARO:
                return new GLAmaroFilter();
            // 古董
            case ANTIQUE:
                return new GLAnitqueFilter();

            // 黑猫
            case BLACKCAT:
                return new GLBlackCatFilter();

            // 黑白
            case BLACKWHITE:
                return new GLBlackWhiteFilter();

            // 布鲁克林
            case BROOKLYN:
                return new GLBrooklynFilter();

            // 冷静
            case CALM:
                return new GLCalmFilter();

            // 冷色调
            case COOL:
                return new GLCoolFilter();

            // 晨鸟
            case EARLYBIRD:
                return new GLEarlyBirdFilter();

            // 翡翠
            case EMERALD:
                return new GLEmeraldFilter();

            // 常绿
            case EVERGREEN:
                return new GLEvergreenFilter();

            // 童话
            case FAIRYTALE:
                return new GLFairyTaleFilter();

            // 佛洛伊特
            case FREUD:
                return new GLFreudFilter();

            // 健康
            case HEALTHY:
                return new GLHealthyFilter();

            // 酵母
            case HEFE:
                return new GLHefeFilter();

            // 哈德森
            case HUDSON:
                return new GLHudsonFilter();

            // 凯文
            case KEVIN:
                return new GLKevinFilter();

            // 拿铁
            case LATTE:
                return new GLLatteFilter();

            // LOMO
            case LOMO:
                return new GLLomoFilter();

            // 怀旧之情
            case NOSTALGIA:
                return new GLNostalgiaFilter();

            // 浪漫
            case ROMANCE:
                return new GLRomanceFilter();

            // 樱花
            case SAKURA:
                return new GLSakuraFilter();

            //  素描
            case SKETCH:
                return new GLSketchFilter();

            // 日落
            case SUNSET:
                return new GLSunsetFilter();

            // 白猫
            case WHITECAT:
                return new GLWhiteCatFilter();

            case NONE:      // 没有滤镜
            case SOURCE:    // 原图
            default:
                return new GLDisplayFilter();
        }
    }

    /**
     * 获取滤镜组
     * @return
     */
    public static GLBaseImageFilterGroup getFilterGroup() {
        return new GLDefaultFilterGroup();
    }

    public static GLBaseImageFilterGroup getFilterGroup(GlFilterGroupType type) {
        switch (type) {
            // 彩妆滤镜组
            case MAKEUP:
                return new GLMakeUpFilterGroup();

            // 默认滤镜组
            case DEFAULT:
            default:
                return new GLDefaultFilterGroup();
        }
    }

    /**
     * 获取层级
     * @param Type
     * @return
     */
    public static GlFilterIndex getIndex(GlFilterType Type) {
        GlFilterIndex index = mIndexMap.get(Type);
        if (index != null) {
            return index;
        }
        return GlFilterIndex.NoneIndex;
    }
}
