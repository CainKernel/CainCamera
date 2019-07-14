package com.cgfay.filter.recorder;

/**
 * 媒体信息
 * @author CainHuang
 * @date 2019/7/7
 */
public class MediaInfo {

    private String fileName;
    private long duration;

    public MediaInfo(String name, long duration) {
        this.fileName = name;
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public String getFileName() {
        return fileName;
    }
}
