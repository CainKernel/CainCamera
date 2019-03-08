package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 时间特效基类
 */
public class GLImageEffectFilter extends GLImageFilter {

    // 当前时钟值，毫秒为单位
    long mCurrentPosition;

    public GLImageEffectFilter(Context context) {
        super(context);
    }

    public GLImageEffectFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    /**
     * 绑定当前时间，单位ms
     * @param timeMs
     */
    public void setCurrentPosition(long timeMs) {
        mCurrentPosition = timeMs;
        calculateInterval();
    }

    /**
     * 计算步进
     */
    protected void calculateInterval() {

    }
}
