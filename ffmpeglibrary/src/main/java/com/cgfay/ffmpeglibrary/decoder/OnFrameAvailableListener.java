package com.cgfay.ffmpeglibrary.decoder;

/**
 * 帧可用回调监听
 */
public interface OnFrameAvailableListener {

    /**
     * 帧可用回调
     * @param frame     帧数据
     * @param width     宽度
     * @param height    高度
     */
    void onFrameAvailable(byte[] frame, int width, int height);

}
