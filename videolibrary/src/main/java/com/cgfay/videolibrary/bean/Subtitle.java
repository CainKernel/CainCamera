package com.cgfay.videolibrary.bean;

/**
 * 字幕数据
 */
public class Subtitle {
    private long start;         // 字幕开始位置(毫秒)
    private long duration;      // 时长(毫秒)
    private String content;     // 字幕内容
    private String fontPath;    // 字体路径
    private boolean assetFont;  // asset目录中的字体

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFontPath() {
        return fontPath;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public boolean isAssetFont() {
        return assetFont;
    }

    public void setAssetFont(boolean assetFont) {
        this.assetFont = assetFont;
    }
}
