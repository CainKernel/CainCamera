package com.cgfay.avfoundation;

import androidx.annotation.NonNull;

import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGRect;

/**
 * 视频渲染描述指
 */
public abstract class AVVideoRenderInstruction {

    /**
     * 轨道ID
     */
    protected int mTrackID;

    public AVVideoRenderInstruction() {

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
    public abstract boolean opacityRampForTime(@NonNull AVTimeRange timeRange,
                                               @NonNull AVTime startTime,
                                               float startOpacity, float endOpacity);

    /**
     * 设置某个时间区间二维形变变换，即仿射变换处理
     * @param timeRange         时间区间
     * @param startTime         开始时间
     * @param startTransform    起始变换矩阵
     * @param endTransform      结束变换矩阵
     * @return
     */
    public abstract boolean transformRampForTime(@NonNull AVTimeRange timeRange,
                                                 @NonNull AVTime startTime,
                                                 @NonNull AffineTransform startTransform,
                                                 @NonNull AffineTransform endTransform);

    /**
     * 设置某个时间区间内裁剪变换处理
     * @param timeRange             时间区间
     * @param startTime             起始时间
     * @param startCrop    起始裁剪区间
     * @param endCrop      结束裁剪区间
     * @return
     */
    public abstract boolean cropRectangleRampForTime(@NonNull AVTimeRange timeRange,
                                                     @NonNull AVTime startTime,
                                                     @NonNull CGRect startCrop,
                                                     @NonNull CGRect endCrop);

    /**
     * 设置某个时间的不透明度
     * @param opacity   不透明度
     * @param time      时间
     */
    public abstract void setOpacity(float opacity, @NonNull AVTime time);

    /**
     * 设置某个时间的变换矩阵
     * @param transform 变换矩阵
     * @param time      时间
     */
    public abstract void setTransform(@NonNull AffineTransform transform, @NonNull AVTime time);

    /**
     * 设置某个时间的裁剪矩阵
     * @param cropRectangle 裁剪区域
     * @param time  时间
     */
    public abstract void setCropRectangle(@NonNull CGRect cropRectangle, @NonNull AVTime time);

}
