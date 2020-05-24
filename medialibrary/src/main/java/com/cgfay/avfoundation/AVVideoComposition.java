package com.cgfay.avfoundation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频在时间轴上的描述，记录刷新间隔、渲染大小、缩放倍数以及视频在时间轴上的行为描述指令
 */
public class AVVideoComposition {

    /**
     * 30fps帧间间隔，默认时间刻度是600，则默认value值为20
     */
    private static final long DEFAULT_FRAME_TIME = 20;

    /**
     * 帧间间隔时长
     */
    private AVTime mFrameDuration;

    /**
     * 显示渲染大小
     */
    private CGSize mRenderSize;

    /**
     * 渲染缩放倍数
     */
    private float mRenderScale;

    /**
     * 视频对象格式定义的指令列表，组合对象时间轴内的时间范围信息，其实就是表明这个视频组合的开始时间和持续时间
     */
    private List<AVVideoCompositionInstruction> mInstructions;

    public AVVideoComposition() {
        mFrameDuration = new AVTime(DEFAULT_FRAME_TIME, AVTime.DEFAULT_TIME_SCALE);
        mRenderSize = CGSize.kSizeZero;
        mRenderScale = 1.0f;
        mInstructions = new ArrayList<>();
    }

    /**
     * 通过AVAsset创建AVVideoComposition
     * @param asset 媒体资产
     * @return      AVVideoComposition
     */
    public static AVVideoComposition videoCompositionWithPropertiesOfAsset(AVAsset asset) {
        AVVideoComposition composition = new AVVideoComposition();
        composition.mFrameDuration = asset.getDuration();
        composition.mRenderSize = asset.getNaturalSize();
        return composition;
    }

    /**
     * 设置视频帧间隔
     * @param time 帧间间隔
     */
    public void setFrameDuration(@NonNull AVTime time) {
        mFrameDuration = time;
    }

    /**
     * 获取视频帧间隔
     */
    public AVTime getFrameDuration() {
        return mFrameDuration;
    }

    /**
     * 设置视频渲染大小
     * @param size 渲染size
     */
    public void setRenderSize(@NonNull CGSize size) {
        mRenderSize = size;
    }

    /**
     * 获取视频渲染大小
     */
    public CGSize getRenderSize() {
        return mRenderSize;
    }

    /**
     * 设置渲染缩放倍数
     * @param scale 缩放倍数
     */
    public void setRenderScale(float scale) {
        mRenderScale = scale;
    }

    /**
     * 获取渲染缩放倍数
     * @return 渲染缩放倍数
     */
    public float getRenderScale() {
        return mRenderScale;
    }

    /**
     * 添加视频渲染指令
     * @param instruction 视频渲染指令
     */
    public void addInstruction(AVVideoCompositionInstruction instruction) {
        mInstructions.add(instruction);
    }

    /**
     * 设置视频渲染指令
     * @param instructions 视频渲染指令
     */
    public void setInstructions(List<AVVideoCompositionInstruction> instructions) {
        mInstructions.addAll(instructions);
    }

    /**
     * 获取视频渲染指令列表
     */
    public List<AVVideoCompositionInstruction> getInstructions() {
        return mInstructions;
    }
}
