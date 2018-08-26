package com.cgfay.filterlibrary.glfilter;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.advanced.adjust.GLImageBrightnessFilter;
import com.cgfay.filterlibrary.glfilter.advanced.adjust.GLImageContrastFilter;
import com.cgfay.filterlibrary.glfilter.advanced.adjust.GLImageExposureFilter;
import com.cgfay.filterlibrary.glfilter.advanced.adjust.GLImageHueFilter;
import com.cgfay.filterlibrary.glfilter.advanced.adjust.GLImageMirrorFilter;
import com.cgfay.filterlibrary.glfilter.advanced.adjust.GLImageSaturationFilter;
import com.cgfay.filterlibrary.glfilter.advanced.adjust.GLImageSharpnessFilter;
import com.cgfay.filterlibrary.glfilter.advanced.beauty.GLImageBeautyFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageAmaroFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageAnitqueFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageBlackCatFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageBlackWhiteFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageBrooklynFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageCalmFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageCoolFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageEarlyBirdFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageEmeraldFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageEvergreenFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageFairyTaleFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageFreudFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageHealthyFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageHefeFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageHudsonFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageKevinFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageLatteFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageLomoFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageNostalgiaFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageRomanceFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageSakuraFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageSketchFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageSunsetFilter;
import com.cgfay.filterlibrary.glfilter.advanced.colors.GLImageWhiteCatFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterIndex;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Filter管理类
 * Created by cain on 17-7-25.
 */

public final class GLImageFilterManager {

    private static HashMap<GLImageFilterType, GLImageFilterIndex> mIndexMap = new HashMap<GLImageFilterType, GLImageFilterIndex>();
    static {
        mIndexMap.put(GLImageFilterType.NONE, GLImageFilterIndex.NoneIndex);

        // 图片编辑
        mIndexMap.put(GLImageFilterType.BRIGHTNESS, GLImageFilterIndex.ImageEditIndex);
        mIndexMap.put(GLImageFilterType.CONTRAST, GLImageFilterIndex.ImageEditIndex);
        mIndexMap.put(GLImageFilterType.EXPOSURE, GLImageFilterIndex.ImageEditIndex);
        mIndexMap.put(GLImageFilterType.GUASS, GLImageFilterIndex.ImageEditIndex);
        mIndexMap.put(GLImageFilterType.HUE, GLImageFilterIndex.ImageEditIndex);
        mIndexMap.put(GLImageFilterType.MIRROR, GLImageFilterIndex.ImageEditIndex);
        mIndexMap.put(GLImageFilterType.SATURATION, GLImageFilterIndex.ImageEditIndex);
        mIndexMap.put(GLImageFilterType.SHARPNESS, GLImageFilterIndex.ImageEditIndex);

        // 水印
        mIndexMap.put(GLImageFilterType.WATERMASK, GLImageFilterIndex.WaterMaskIndex);

        // 美颜
        mIndexMap.put(GLImageFilterType.REALTIMEBEAUTY, GLImageFilterIndex.BeautyIndex);

        // 瘦脸大眼
        mIndexMap.put(GLImageFilterType.FACESTRETCH, GLImageFilterIndex.FaceStretchIndex);

        // 贴纸
        mIndexMap.put(GLImageFilterType.STICKER, GLImageFilterIndex.StickerIndex);

        // 彩妆
        mIndexMap.put(GLImageFilterType.MAKEUP, GLImageFilterIndex.MakeUpIndex);


        // 颜色滤镜
        mIndexMap.put(GLImageFilterType.AMARO, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.ANTIQUE, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.BLACKCAT, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.BLACKWHITE, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.BROOKLYN, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.CALM, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.COOL, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.EARLYBIRD, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.EMERALD, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.EVERGREEN, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.FAIRYTALE, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.FREUD, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.HEALTHY, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.HEFE, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.HUDSON, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.KEVIN, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.LATTE, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.LOMO, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.NOSTALGIA, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.ROMANCE, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.SAKURA, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.SKETCH, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.SOURCE, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.SUNSET, GLImageFilterIndex.ColorIndex);
        mIndexMap.put(GLImageFilterType.WHITECAT, GLImageFilterIndex.ColorIndex);
    }

    private GLImageFilterManager() {}

    public static GLImageFilter getFilter(Context context, GLImageFilterType type) {
        switch (type) {

            // 图片基本属性编辑滤镜
            // 饱和度
            case SATURATION:
                return new GLImageSaturationFilter(context);
            // 镜像翻转
            case MIRROR:
                return new GLImageMirrorFilter(context);
            // 亮度
            case BRIGHTNESS:
                return new GLImageBrightnessFilter(context);
            // 对比度
            case CONTRAST:
                return new GLImageContrastFilter(context);
            // 曝光
            case EXPOSURE:
                return new GLImageExposureFilter(context);
            // 色调
            case HUE:
                return new GLImageHueFilter(context);
            // 锐度
            case SHARPNESS:
                return new GLImageSharpnessFilter(context);

            // TODO 贴纸滤镜需要人脸关键点计算得到
            case STICKER:
                return new GLImageFilter(context);

            // 实时美颜
            case REALTIMEBEAUTY:
                return new GLImageBeautyFilter(context);

            // AMARO
            case AMARO:
                return new GLImageAmaroFilter(context);
            // 古董
            case ANTIQUE:
                return new GLImageAnitqueFilter(context);

            // 黑猫
            case BLACKCAT:
                return new GLImageBlackCatFilter(context);

            // 黑白
            case BLACKWHITE:
                return new GLImageBlackWhiteFilter(context);

            // 布鲁克林
            case BROOKLYN:
                return new GLImageBrooklynFilter(context);

            // 冷静
            case CALM:
                return new GLImageCalmFilter(context);

            // 冷色调
            case COOL:
                return new GLImageCoolFilter(context);

            // 晨鸟
            case EARLYBIRD:
                return new GLImageEarlyBirdFilter(context);

            // 翡翠
            case EMERALD:
                return new GLImageEmeraldFilter(context);

            // 常绿
            case EVERGREEN:
                return new GLImageEvergreenFilter(context);

            // 童话
            case FAIRYTALE:
                return new GLImageFairyTaleFilter(context);

            // 佛洛伊特
            case FREUD:
                return new GLImageFreudFilter(context);

            // 健康
            case HEALTHY:
                return new GLImageHealthyFilter(context);

            // 酵母
            case HEFE:
                return new GLImageHefeFilter(context);

            // 哈德森
            case HUDSON:
                return new GLImageHudsonFilter(context);

            // 凯文
            case KEVIN:
                return new GLImageKevinFilter(context);

            // 拿铁
            case LATTE:
                return new GLImageLatteFilter(context);

            // LOMO
            case LOMO:
                return new GLImageLomoFilter(context);

            // 怀旧之情
            case NOSTALGIA:
                return new GLImageNostalgiaFilter(context);

            // 浪漫
            case ROMANCE:
                return new GLImageRomanceFilter(context);

            // 樱花
            case SAKURA:
                return new GLImageSakuraFilter(context);

            //  素描
            case SKETCH:
                return new GLImageSketchFilter(context);

            // 日落
            case SUNSET:
                return new GLImageSunsetFilter(context);

            // 白猫
            case WHITECAT:
                return new GLImageWhiteCatFilter(context);

            case NONE:      // 没有滤镜
            case SOURCE:    // 原图
            default:
                return new GLImageFilter(context);
        }
    }

    /**
     * 获取特效滤镜 TODO 暂未实现
     * @param context
     * @param type
     * @return
     */
    public static GLImageFilter getEffectFilter(Context context, GLImageFilterType type) {
        switch (type) {
            default:
                return new GLImageFilter(context);
        }
    }

    /**
     * 获取层级
     * @param Type
     * @return
     */
    public static GLImageFilterIndex getIndex(GLImageFilterType Type) {
        GLImageFilterIndex index = mIndexMap.get(Type);
        if (index != null) {
            return index;
        }
        return GLImageFilterIndex.NoneIndex;
    }

    /**
     * 获取滤镜类型
     * @return
     */
    public static List<GLImageFilterType> getFilterTypes() {
        List<GLImageFilterType> filterTypes = new ArrayList<>();
        filterTypes.add(GLImageFilterType.SOURCE);
        filterTypes.add(GLImageFilterType.AMARO);
        filterTypes.add(GLImageFilterType.ANTIQUE);
        filterTypes.add(GLImageFilterType.BLACKCAT);
        filterTypes.add(GLImageFilterType.BLACKWHITE);
        filterTypes.add(GLImageFilterType.BROOKLYN);
        filterTypes.add(GLImageFilterType.CALM);
        filterTypes.add(GLImageFilterType.COOL);
        filterTypes.add(GLImageFilterType.EARLYBIRD);
        filterTypes.add(GLImageFilterType.EMERALD);
        filterTypes.add(GLImageFilterType.EVERGREEN);
        filterTypes.add(GLImageFilterType.FAIRYTALE);
        filterTypes.add(GLImageFilterType.FREUD);
        filterTypes.add(GLImageFilterType.HEALTHY);
        filterTypes.add(GLImageFilterType.HEFE);
        filterTypes.add(GLImageFilterType.HUDSON);
        filterTypes.add(GLImageFilterType.KEVIN);
        filterTypes.add(GLImageFilterType.LATTE);
        filterTypes.add(GLImageFilterType.LOMO);
        filterTypes.add(GLImageFilterType.NOSTALGIA);
        filterTypes.add(GLImageFilterType.ROMANCE);
        filterTypes.add(GLImageFilterType.SAKURA);
        filterTypes.add(GLImageFilterType.SKETCH);
        filterTypes.add(GLImageFilterType.SUNSET);
        filterTypes.add(GLImageFilterType.WHITECAT);

        return filterTypes;
    }

    /**
     * 获取滤镜名称
     * @return
     */
    public static List<String> getFilterNames() {
        List<String> filterNames = new ArrayList<>();

        filterNames.add("原图");
        filterNames.add("阿马罗");
        filterNames.add("古董");
        filterNames.add("黑猫");
        filterNames.add("黑白");
        filterNames.add("布鲁克林");
        filterNames.add("冷静");
        filterNames.add("冷色调");
        filterNames.add("晨鸟");
        filterNames.add("翡翠");
        filterNames.add("常绿");
        filterNames.add("童话");
        filterNames.add("佛洛伊特");
        filterNames.add("健康");
        filterNames.add("酵母");
        filterNames.add("哈德森");
        filterNames.add("凯文");
        filterNames.add("拿铁");
        filterNames.add("LOMO");
        filterNames.add("怀旧之情");
        filterNames.add("浪漫");
        filterNames.add("樱花");
        filterNames.add("素描");
        filterNames.add("日落");
        filterNames.add("白猫");

        return filterNames;
    }
}
