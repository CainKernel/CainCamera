package com.cgfay.media.recorder;

import com.cgfay.avfoundation.AVMediaType;

/**
 * 录制一段视频/音频的信息
 * @author CainHuang
 * @date 2019/6/30
 */
public class RecordInfo {

    private String fileName;

    private long duration; // 暂时录音的时长信息为-1，以视频信息为准

    private AVMediaType type;

    public RecordInfo(String fileName, long duration, AVMediaType type) {
        this.fileName = fileName;
        this.duration = duration;
        this.type = type;
    }

    public RecordInfo(String fileName, AVMediaType type) {
        this.fileName = fileName;
        this.duration = -1;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public long getDuration() {
        return duration;
    }

    public AVMediaType getType() {
        return type;
    }
}
