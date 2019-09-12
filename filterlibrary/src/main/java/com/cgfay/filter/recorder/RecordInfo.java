package com.cgfay.filter.recorder;

/**
 * 录制一段视频/音频的信息
 * @author CainHuang
 * @date 2019/6/30
 */
public class RecordInfo {

    private String fileName;

    private long duration;

    private MediaType type;

    public RecordInfo(String fileName, long duration, MediaType type) {
        this.fileName = fileName;
        this.duration = duration;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public long getDuration() {
        return duration;
    }

    public MediaType getType() {
        return type;
    }
}
