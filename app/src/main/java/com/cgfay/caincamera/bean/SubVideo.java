package com.cgfay.caincamera.bean;

/**
 * Created by cain.huang on 2017/12/29.
 */



import com.cgfay.cainfilter.utils.FileUtils;

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
        duration = 0;
        mediaPath = null;
    }

    /**
     * 获取时长
     * @return
     */
    public int getDuration() {
        return duration;
    }

    /**
     * 获取媒体路径
     * @return
     */
    public String getMediaPath() {
        return mediaPath;
    }
}
