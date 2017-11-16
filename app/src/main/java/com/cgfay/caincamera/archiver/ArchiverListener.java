package com.cgfay.caincamera.archiver;

/**
 * 压缩/解压进度回调
 * Created by cain on 2017/11/16.
 */

public interface ArchiverListener {

    /**
     * 开始压缩/解压
     */
    void onStartArchiving();

    /**
     * 压缩/解压过程
     * @param current 当前数量
     * @param total   总数量
     */
    void onProgressArchiving(int current, int total);

    /**
     * 结束压缩/解压
     */
    void onFinishArchiving();
}
