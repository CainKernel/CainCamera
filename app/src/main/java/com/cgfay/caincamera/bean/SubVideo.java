package com.cgfay.caincamera.bean;

/**
 * Created by cain.huang on 2017/12/29.
 */

import com.cgfay.caincamera.utils.FileUtils;

import java.io.Serializable;

/**
 * 分段视频信息
 */
public class SubVideo implements Serializable {
    // 视频路径
    public String mediaPath;
    // 视频长度
    public int duration;

    public SubVideo() {}

    // 删除视频
    public void delete() {
        FileUtils.deleteFile(mediaPath);
    }

    /**
     * 获取时长
     * @return
     */
    public int getDuration() {
        return duration;
    }
}
