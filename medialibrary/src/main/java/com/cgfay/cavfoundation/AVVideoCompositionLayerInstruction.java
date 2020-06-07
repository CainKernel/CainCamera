package com.cgfay.cavfoundation;

import androidx.annotation.NonNull;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGRect;
import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeRange;

/**
 * 视频渲染描述指令，用于定义给定视频轨道应用的模糊、变形和裁剪效果
 */
public class AVVideoCompositionLayerInstruction {

    /**
     * 源轨道ID
     */
    protected int mTrackID;

    public AVVideoCompositionLayerInstruction() {

    }

    /**
     * The track identifier of the source track to which the compositor will apply the instruction.
     * 获取需要应用渲染指令的媒体源轨道ID
     * @return track identifier
     */
    public int getTrackID() {
        return mTrackID;
    }


    /**
     * Obtains the opacity ramp that includes a specified time.
     * 设置某个时间区间的不透明度渐变
     * @param timeRange     时间区间
     * @param startTime     起始时间
     * @param startOpacity  起始不透明度
     * @param endOpacity    结束不透明度
     * @return  是否设置成功
     */
    public boolean getOpacity(@NonNull AVTimeRange timeRange, @NonNull AVTime startTime,
                              float startOpacity, float endOpacity) {
        return false;
    }

    /**
     * 设置某个时间区间二维形变变换，即仿射变换处理
     * @param timeRange         时间区间
     * @param startTime         开始时间
     * @param startTransform    起始变换矩阵
     * @param endTransform      结束变换矩阵
     * @return
     */
    public boolean getTransform(@NonNull AVTimeRange timeRange,
                                @NonNull AVTime startTime,
                                @NonNull AffineTransform startTransform,
                                @NonNull AffineTransform endTransform) {
        return false;
    }

    /**
     * 设置某个时间区间内裁剪变换处理
     * @param timeRange             时间区间
     * @param startTime             起始时间
     * @param startCrop    起始裁剪区间
     * @param endCrop      结束裁剪区间
     * @return
     */
    public boolean getCropRectangle(@NonNull AVTimeRange timeRange,
                                    @NonNull AVTime startTime,
                                    @NonNull CGRect startCrop,
                                    @NonNull CGRect endCrop) {
        return false;
    }

    public void setTransform(@NonNull AffineTransform startTransform,
                             @NonNull AffineTransform endTransform,
                             @NonNull AVTimeRange timeRange) {

    }

    public void setTransform(@NonNull AffineTransform transform, @NonNull AVTime time) {

    }

    public void setOpacityRamp(float startOpacity, float toOpacity, @NonNull AVTimeRange range) {

    }

    public void setOpacity(float opacity, @NonNull AVTime time) {

    }

    public void setCropRectangle(@NonNull CGRect startCropRect, @NonNull CGRect endCropRect,
                                 @NonNull AVTimeRange timeRange) {

    }

    public void setCropRectangle(@NonNull CGRect rect, @NonNull AVTime time) {

    }

}
