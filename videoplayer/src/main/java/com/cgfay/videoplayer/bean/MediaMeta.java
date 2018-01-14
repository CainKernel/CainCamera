package com.cgfay.videoplayer.bean;

/**
 * Created by cain on 2018/1/14.
 */

/**
 * 图片元数据(包括视频)
 * Created by cain.huang on 2017/8/9.
 */
public class MediaMeta {
    // id
    private int id;
    // 名称
    private String name;
    // 类型
    private String mimeType;
    // 路径
    private String path;
    // 宽度
    private int width;
    // 高度
    private int height;
    // 旋转角度
    private int orientation;
    // 拍照时间
    private long time;
    // 大小
    private long size;

    // 用于记录是否被选中
    private boolean selected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
