package com.cgfay.caincamera.core;

import com.cgfay.caincamera.bean.SubVideo;
import com.cgfay.caincamera.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 视频文件管理器
 * Created by cain.huang on 2017/12/29.
 */

public class VideoListManager {

    // 十秒时长
    public static final int DURATION_TEN_SECOND = 10 * 1000;
    // 三分钟时长
    public static final int DURATION_THREE_MINUTE = 180 * 1000;


    private static VideoListManager mInstance;

    // 视频最大时长，默认10秒
    private int mMaxDuration = DURATION_TEN_SECOND;

    // 获取所有分块
    private LinkedList<SubVideo> mVideoList = new LinkedList<SubVideo>();

    public static VideoListManager getInstance() {
        if (mInstance == null) {
            mInstance = new VideoListManager();
        }
        return mInstance;
    }

    /**
     * 设置最大时长，必须大于1秒
     * @param duration
     */
    public void setMaxDuration(int duration) {
        if (duration >= 1000) {
            mMaxDuration = duration;
        }
    }

    /**
     * 获取视频最大长度
     * @return
     */
    public int getMaxDuration() {
        return mMaxDuration;
    }

    /**
     * 获取录制的总时长
     * @return
     */
    public int getDuration() {
        int duration = 0;
        if (mVideoList != null) {
            for (SubVideo subVideo : mVideoList) {
                duration += subVideo.getDuration();
            }
        }
        return duration;
    }

    /**
     * 添加分段视频
     * @param path      视频路径
     * @param duration  视频时长
     */
    public void addSubVideo(String path, int duration) {
        if (mVideoList == null) {
            mVideoList = new LinkedList<SubVideo>();
        }
        SubVideo subVideo = new SubVideo();
        subVideo.mediaPath = path;
        subVideo.duration = duration;
        mVideoList.add(subVideo);
    }

    /**
     * 添加分段视频
     * @param subVideo
     */
    public void addSubVideo(SubVideo subVideo) {
        if (mVideoList == null) {
            mVideoList = new LinkedList<SubVideo>();
        }
        mVideoList.add(subVideo);
    }

    /**
     * 移除分段视频
     * @param subVideo     分段视频
     * @param deleteFile   是否删除文件
     */
    public void removeSubVideo(SubVideo subVideo, boolean deleteFile) {
        if (mVideoList != null) {
            mVideoList.remove(subVideo);
        }
        if (subVideo != null) {
            // 删除文件
            if (deleteFile) {
                subVideo.delete();
            }
            mVideoList.remove(subVideo);
        }
    }

    /**
     * 移除分段视频
     * @param subVideo
     */
    public void removeSubVideo(SubVideo subVideo) {
        removeSubVideo(subVideo, true);
    }

    /**
     * 移除分段视频
     * @param index
     */
    public void removeSubVideo(int index) {

        if (mVideoList == null || mVideoList.size() <= index) {
            return;
        }
        removeSubVideo(mVideoList.get(index));
    }

    /**
     * 移除当前分段视频
     */
    public void removeLastSubVideo() {
        if (mVideoList == null) {
            return;
        }
        removeSubVideo(getSubVideo(mVideoList.size() - 1), true);
    }

    /**
     * 获取视频文件
     * @param index
     * @return
     */
    public SubVideo getSubVideo(int index) {
        if (index >= 0 && index < mVideoList.size()) {
            return mVideoList.get(index);
        }
        return null;
    }

    /**
     * 删除所有分段视频
     */
    public void removeAllSubVideo() {
        if (mVideoList != null) {
            for (SubVideo part : mVideoList) {
                part.delete();
            }
            mVideoList.clear();
        }
    }

    /**
     * 获取分段视频列表
     * @return
     */
    public LinkedList<SubVideo> getSubVideoList() {
        return mVideoList;
    }


    /**
     * 获取分段视频路径
     * @return
     */
    public List<String> getSubVideoPathList() {
        if (mVideoList == null || mVideoList.isEmpty()) {
            return new ArrayList<String>();
        }
        List<String> mediaPaths = new ArrayList<String>();
        for (int i = 0; i < mVideoList.size(); i++) {
            mediaPaths.add(i, mVideoList.get(i).getMediaPath());
        }
        return mediaPaths;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        if (mVideoList != null) {
            result.append("[" + mVideoList.size() + "]");
            for (SubVideo part : mVideoList) {
                result.append(part.mediaPath + ":" + part.duration + "\n");
            }
        }
        return result.toString();
    }

}

