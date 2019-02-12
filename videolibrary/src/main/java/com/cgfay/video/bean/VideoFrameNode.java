package com.cgfay.video.bean;

import android.graphics.Bitmap;

/**
 * 视频帧结点，双链表构成
 */
public class VideoFrameNode {

    private VideoFrameNode mPrev;   // 前继结点
    private VideoFrameNode mNext;   // 后继结点
    private Bitmap mBitmap;         // 帧图片
    private long mFrameTime;        // 帧时间

    public VideoFrameNode prev() {
        return mPrev;
    }

    public void setPrev(VideoFrameNode prev) {
        this.mPrev = prev;
    }

    public VideoFrameNode next() {
        return mNext;
    }

    public void setNext(VideoFrameNode next) {
        this.mNext = next;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public long getFrameTime() {
        return mFrameTime;
    }

    public void setFrameTime(long frameTime) {
        this.mFrameTime = frameTime;
    }

}
