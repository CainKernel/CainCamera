package com.cgfay.cainfilter.image_processor;

/**
 * 处理回调接口
 * Created by cain on 2017/8/12.
 */

public interface ProcessCallBack {
    // 处理开始
    void onProcessStart();
    // 处理完成
    void onProcessFinish();
    // 取消处理
    void onProcessCancel();
}
