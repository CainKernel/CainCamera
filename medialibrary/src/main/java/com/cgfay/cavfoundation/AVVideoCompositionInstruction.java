package com.cgfay.cavfoundation;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.cgfay.coremedia.AVTimeRange;

import java.util.List;

/**
 * 视频行为描述指令，包含时间区间、背景颜色、渲染描述指令
 */
public class AVVideoCompositionInstruction {

    /**
     * 行为区间
     */
    private AVTimeRange mTimeRange;

    /**
     * 背景颜色
     */
    private  @ColorInt int mBackgroundColor;

    /**
     * 渲染描述指令列表，用于定义给定视频轨道应用的模糊、变形和裁剪效果
     */
    private List<AVVideoCompositionLayerInstruction> mLayerInstructions;

    /**
     * 来自视频源的的轨道ID列表
     * List of video track IDs required to compose frames for this instruction.
     * If the value of this property is nil, all source tracks will be considered required for composition
     */
    private List<Integer> mRequiredSourceTrackIDs;

    public AVVideoCompositionInstruction() {
        mTimeRange = AVTimeRange.kAVTimeRangeInvalid;
        mBackgroundColor = Color.BLACK;
    }

    /**
     * 设置时间区间
     * @param range 时间区间
     */
    public void setTimeRange(@NonNull AVTimeRange range) {
        mTimeRange = range;
    }

    /**
     * 获取时间区间
     */
    public AVTimeRange getTimeRange() {
        return mTimeRange;
    }

    /**
     * 设置背景颜色值
     */
    public void setBackgroundColor(@ColorInt int color) {
        mBackgroundColor = color;
    }

    /**
     * 获取背景颜色值
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * 设置渲染指令描述列表
     * @param instructions
     */
    public void setLayerInstructions(@NonNull List<AVVideoCompositionLayerInstruction> instructions) {
        mLayerInstructions.addAll(instructions);
    }

    /**
     * 获取视频渲染指令描述列表
     */
    public List<AVVideoCompositionLayerInstruction> getLayerInstructions() {
        return mLayerInstructions;
    }
}
