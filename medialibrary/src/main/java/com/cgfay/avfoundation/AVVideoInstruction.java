package com.cgfay.avfoundation;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * 视频行为描述指令，包含时间区间、背景颜色、渲染描述指令
 */
public class AVVideoInstruction {

    /**
     * 行为区间
     */
    protected AVTimeRange mTimeRange;

    /**
     * 背景颜色
     */
    protected @ColorInt int mBackgroundColor;

    /**
     * 渲染描述指令列表
     */
    protected List<AVVideoRenderInstruction> mRenderInstructions;

    /**
     * 来自视频源的的轨道ID列表
     * List of video track IDs required to compose frames for this instruction.
     * If the value of this property is nil, all source tracks will be considered required for composition
     */
    protected List<Integer> mRequiredSourceTrackIDs;

    public AVVideoInstruction() {
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
    public void setRenderInstructions(@NonNull List<AVVideoRenderInstruction> instructions) {
        mRenderInstructions.addAll(instructions);
    }

    /**
     * 获取视频渲染指令描述列表
     */
    public List<AVVideoRenderInstruction> getRenderInstructions() {
        return mRenderInstructions;
    }
}
