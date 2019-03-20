package com.cgfay.video.fragment;

import com.cgfay.video.bean.EffectMimeType;
import com.cgfay.video.bean.EffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * 特效助手
 * 备注：这里的特效滤镜都放到了Native层，名称和id要跟Native层GLFilter中的滤镜对应。
 */
public class EffectFilterHelper {

    // 滤镜特效列表
    private final List<EffectType> mEffectFilterList = new ArrayList<>();
    // 转场特效列表
    private final List<EffectType> mEffectTransitionList = new ArrayList<>();
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
        mEffectTransitionList.clear();
        mEffectMultiList.clear();
        // 滤镜特效
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "灵魂出窍", 0x000, "assets://thumbs/effect/icon_effect_soul_stuff.png"));
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "抖动", 0x001, "assets://thumbs/effect/icon_effect_shake.png"));
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "幻觉", 0x002, "assets://thumbs/effect/icon_effect_illusion.png"));
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "缩放", 0x003, "assets://thumbs/effect/icon_effect_scale.png"));
        mEffectFilterList.add(new EffectType(EffectMimeType.FILTER, "闪白", 0x004, "assets://thumbs/effect/icon_effect_glitter_white.png"));

        // 分屏特效
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "模糊分屏", 0x200, "assets://thumbs/effect/icon_frame_blur.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "黑白三屏", 0x201, "assets://thumbs/effect/icon_frame_bw_three.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "两屏", 0x202, "assets://thumbs/effect/icon_frame_two.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "三屏", 0x203, "assets://thumbs/effect/icon_frame_three.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "四屏", 0x204, "assets://thumbs/effect/icon_frame_four.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "六屏", 0x205, "assets://thumbs/effect/icon_frame_six.png"));
        mEffectMultiList.add(new EffectType(EffectMimeType.MULTIFRAME, "九屏", 0x206, "assets://thumbs/effect/icon_frame_nine.png"));
    }

    /**
     * 获取滤镜特效数据
     * @return
     */
    public List<EffectType> getEffectFilterData() {
        return mEffectFilterList;
    }

    /**
     * 获取转场特效数据
     * @return
     */
    public List<EffectType> getEffectTransitionData() {
        return mEffectTransitionList;
    }

    /**
     * 获取分屏特效数据
     * @return
     */
    public List<EffectType> getEffectMultiData() {
        return mEffectMultiList;
    }

}
