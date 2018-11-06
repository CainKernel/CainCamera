package com.cgfay.filterlibrary.glfilter.stickers.bean;

/**
 * 某个部位的动态贴纸数据
 */
public class DynamicStickerData {
    public int width;               // 贴纸宽度
    public int height;              // 贴纸高度
    public int frames;              // 贴纸帧数
    public int action;              // 动作，0表示默认显示，这里用来处理贴纸音乐、动作等
    public String stickerName;      // 贴纸名称，用于标记贴纸所在文件夹以及png文件的
    public int duration;            // 贴纸帧显示间隔
    public boolean stickerLooping;  // 贴纸是否循环渲染
    public String audioPath;        // 音乐路径，不存在时，路径为空字符串
    public boolean audioLooping;    // 音乐是否循环播放
    public int maxCount;            // 最大贴纸渲染次数

    @Override
    public String toString() {
        return "DynamicStickerData{" +
                "width=" + width +
                ", height=" + height +
                ", frames=" + frames +
                ", action=" + action +
                ", stickerName='" + stickerName + '\'' +
                ", duration=" + duration +
                ", stickerLooping=" + stickerLooping +
                ", audioPath='" + audioPath + '\'' +
                ", audioLooping=" + audioLooping +
                ", maxCount=" + maxCount +
                '}';
    }
}
