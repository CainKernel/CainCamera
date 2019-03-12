package com.cgfay.video.engine;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectBlackWhiteThreeFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectGlitterWhiteFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectIllusionFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectMultiBlurFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectMultiFourFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectMultiNineFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectMultiSixFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectMultiThreeFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectMultiTwoFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectScaleFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectShakeFilter;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectSoulStuffFilter;
import com.cgfay.video.bean.EffectMimeType;
import com.cgfay.video.bean.EffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * 特效助手
 * 备注：这里还没弄成动态的方式，因此，暂不放入filterblirary中
 */
public class EffectFilterHelper {

    // 滤镜特效列表
    private final List<EffectType> mEffectFilterList = new ArrayList<>();
    // 分屏特效列表
    private final List<EffectType> mEffectMultiList = new ArrayList<>();

    private static EffectFilterHelper instance;

    public static EffectFilterHelper getInstance() {
        if (instance == null) {
            instance = new EffectFilterHelper();
        }
        return instance;
    }

    private EffectFilterHelper() {
        initAssertEffect();
    }

    private void initAssertEffect() {
        mEffectFilterList.clear();
        mEffectMultiList.clear();
        // 滤镜特效
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "灵魂出窍", 0, "assets://thumbs/effect/icon_effect_soul_stuff.png"));
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "抖动", 1, "assets://thumbs/effect/icon_effect_shake.png"));
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "幻觉", 2, "assets://thumbs/effect/icon_effect_illusion.png"));
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "缩放", 3, "assets://thumbs/effect/icon_effect_scale.png"));
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "闪白", 4, "assets://thumbs/effect/icon_effect_glitter_white.png"));

        // 分屏特效
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "模糊分屏", 0, "assets://thumbs/effect/icon_frame_blur.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "黑白三屏", 1, "assets://thumbs/effect/icon_frame_bw_three.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "两屏", 2, "assets://thumbs/effect/icon_frame_two.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "三屏", 3, "assets://thumbs/effect/icon_frame_three.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "四屏", 4, "assets://thumbs/effect/icon_frame_four.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "六屏", 5, "assets://thumbs/effect/icon_frame_six.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "九屏", 6, "assets://thumbs/effect/icon_frame_nine.png"));
    }

    /**
     * 获取滤镜特效数据
     * @return
     */
    public List<EffectType> getEffectFilterData() {
        return mEffectFilterList;
    }

    /**
     * 获取分屏特效数据
     * @return
     */
    public List<EffectType> getEffectMultiData() {
        return mEffectMultiList;
    }

    /**
     * 切换特效滤镜
     * 备注：该方法需要在渲染线程中使用，这是由于滤镜基类在创建的时候就初始化OpenGL的shader
     * @param context
     * @param effectType
     * @return
     */
    public GLImageEffectFilter changeEffectFilter(Context context, EffectType effectType) {
        if (effectType == null) {
            return null;
        }
        if (effectType.getMimeType() == EffectMimeType.FILTER) {
            return getFilter(context, effectType);
        } else if (effectType.getMimeType() == EffectMimeType.MULTIFRAME) {
            return getMultiFrame(context, effectType);
        }
        return null;
    }

    /**
     * 获取滤镜特效
     * @param context
     * @param type
     * @return
     */
    private GLImageEffectFilter getFilter(Context context, EffectType type) {
        if (type.getName().equalsIgnoreCase("灵魂出窍")) {
            return new GLImageEffectSoulStuffFilter(context);
        } else if (type.getName().equalsIgnoreCase("抖动")) {
            return new GLImageEffectShakeFilter(context);
        } else if (type.getName().equalsIgnoreCase("幻觉")) {
            return new GLImageEffectIllusionFilter(context);
        } else if (type.getName().equalsIgnoreCase("缩放")) {
            return new GLImageEffectScaleFilter(context);
        } else if (type.getName().equalsIgnoreCase("闪白")) {
            return new GLImageEffectGlitterWhiteFilter(context);
        }
        return null;
    }

    /**
     * 获取分屏特效
     * @param context
     * @param type
     * @return
     */
    private GLImageEffectFilter getMultiFrame(Context context, EffectType type) {
        if (type.getName().equalsIgnoreCase("模糊分屏")) {
            return new GLImageEffectMultiBlurFilter(context);
        } else if (type.getName().equalsIgnoreCase("黑白三屏")) {
            return new GLImageEffectBlackWhiteThreeFilter(context);
        } else if (type.getName().equalsIgnoreCase("两屏")) {
            return new GLImageEffectMultiTwoFilter(context);
        } else if (type.getName().equalsIgnoreCase("三屏")) {
            return new GLImageEffectMultiThreeFilter(context);
        } else if (type.getName().equalsIgnoreCase("四屏")) {
            return new GLImageEffectMultiFourFilter(context);
        } else if (type.getName().equalsIgnoreCase("六屏")) {
            return new GLImageEffectMultiSixFilter(context);
        } else if (type.getName().equalsIgnoreCase("九屏")) {
            return new GLImageEffectMultiNineFilter(context);
        }
        return null;
    }
}
