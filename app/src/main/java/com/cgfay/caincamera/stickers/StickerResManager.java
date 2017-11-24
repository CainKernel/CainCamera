package com.cgfay.caincamera.stickers;

import com.cgfay.caincamera.bean.Sticker;

import java.util.ArrayList;

/**
 * 贴纸资源管理器
 * Created by cain.huang on 2017/11/24.
 */

public final class StickerResManager {

    private static final String TAG = "StickerResManager";

    private static StickerResManager mInstance;

    // 一个贴纸压缩包所包含的所有贴纸
    ArrayList<ArrayList> mStickerLists = new ArrayList<ArrayList>();

    // 当前索引
    private int mIndex = -1;

    // 当前路径
    private String mCurrentPath;

    public static StickerResManager getInstance() {
        if (mInstance == null) {
            mInstance = new StickerResManager();
        }
        return mInstance;
    }

    private StickerResManager() {}


    // 获取贴纸一帧所有的图片，返回的集合包含了头、脸、鼻子、耳朵、胡子等类型和图片
    ArrayList<Sticker> getStickerFrame() {
        if (mStickerLists == null || mStickerLists.size() <= 0) {
            return null;
        }
        mIndex++;
        mIndex = mIndex % mStickerLists.size();
        ArrayList<Sticker> frames = new ArrayList<Sticker>();
        // 取出一帧所有的图片
        for (int i = 0; i < mStickerLists.size(); i++) {
            ArrayList<Sticker> mStickerFrames = mStickerLists.get(i);
            frames.add(mStickerFrames.get(mIndex));
        }
        return frames;
    }

    /**
     * 设置当年前选中的贴纸
     * @param path
     */
    public void setCurrentSticker(String path) {
        if (mCurrentPath != null && mCurrentPath.equals(path)) {
            return;
        }
        // 清空
        clearResources();
        mCurrentPath = path;
        loadResources();
    }

    /**
     * 释放资源
     */
    public void clearResources() {
        for (int i = 0; i < mStickerLists.size(); i++) {
            ArrayList<Sticker> frames = mStickerLists.get(i);
            for (int j = 0; j < frames.size(); j++) {
                frames.get(i).clear();
            }
            frames.clear();
        }
        mStickerLists.clear();
        mStickerLists = null;
        mIndex = -1;
    }

    /**
     * 加载资源
     */
    public void loadResources() {
        // 判断是否存在贴纸
        
    }
}
