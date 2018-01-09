package com.cgfay.cainfilter.core;

/**
 * 渲染状态变更监听器
 * Created by cain.huang on 2018/1/2.
 */

public interface RenderStateChangedListener {
    // 是否处于预览状态
    void onPreviewing(boolean previewing);
}
