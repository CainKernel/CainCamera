package com.cgfay.scan.engine;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.cgfay.scan.activity.MediaScanActivity;
import com.cgfay.scan.listener.OnCaptureListener;
import com.cgfay.scan.listener.OnMediaSelectedListener;
import com.cgfay.scan.loader.MediaLoader;
import com.cgfay.scan.model.MimeType;

import java.util.Set;

public final class MediaScanBuilder {

    private MediaScanEngine mMediaScanEngine;
    private MediaScanParam mMediaScanParam;

    public MediaScanBuilder(MediaScanEngine engine, Set<MimeType> mimeTypes) {
        mMediaScanEngine = engine;
        mMediaScanParam = MediaScanParam.getInstance();
        mMediaScanParam.mimeTypes = mimeTypes;
    }

    /**
     * 是否显示拍照item列表
     * @param show
     * @return
     */
    public MediaScanBuilder showCapture(boolean show) {
        mMediaScanParam.showCapture = show;
        return this;
    }

    /**
     * 是否显示视频
     * @param show
     * @return
     */
    public MediaScanBuilder showVideo(boolean show) {
        mMediaScanParam.showVideo = show;
        return this;
    }

    /**
     * 是否显示图片
     * @param show
     * @return
     */
    public MediaScanBuilder showImage(boolean show) {
        mMediaScanParam.showImage = show;
        return this;
    }

    /**
     * 是否显示Gif图片
     * @param enable
     * @return
     */
    public MediaScanBuilder enableSelectGif(boolean enable) {
        mMediaScanParam.enableSelectGif = enable;
        return this;
    }

    /**
     * 一行的item数目
     * @param spanCount
     * @return
     */
    public MediaScanBuilder spanCount(int spanCount) {
        if (spanCount < 0) {
            throw new IllegalArgumentException("spanCount cannot be less than zero");
        }
        mMediaScanParam.spanCount = spanCount;
        return this;
    }

    /**
     * 分割线大小
     * @param spaceSize
     * @return
     */
    public MediaScanBuilder spaceSize(int spaceSize) {
        if (spaceSize < 0) {
            throw new IllegalArgumentException("spaceSize cannot be less than zero");
        }
        mMediaScanParam.spaceSize = spaceSize;
        return this;
    }

    /**
     * 每个item的期望大小
     * @param expectedItemSize
     * @return
     */
    public MediaScanBuilder expectedItemSize(int expectedItemSize) {
        if (expectedItemSize < 1) {
            throw new IllegalArgumentException("expectedItemSize cannot be less than 1");
        }
        mMediaScanParam.expectedItemSize = expectedItemSize;
        return this;
    }

    /**
     * 设置缩略图缩放大小
     * @param scale
     * @return
     */
    public MediaScanBuilder thumbnailScale(float scale) {
        if (scale <= 0f || scale > 1f) {
            throw new IllegalArgumentException("Thumbnail scale must between 0~1");
        }
        mMediaScanParam.thumbnailScale = scale;
        return this;
    }

    /**
     * 图片加载器
     * @param loader
     * @return
     */
    public MediaScanBuilder ImageLoader(MediaLoader loader) {
        mMediaScanParam.mediaLoader = loader;
        return this;
    }

    /**
     * 相机拍照监听器
     * @param listener
     */
    public MediaScanBuilder setCaptureListener(OnCaptureListener listener) {
        mMediaScanParam.captureListener = listener;
        return this;
    }

    /**
     * 设置媒体选择监听器
     * @param listener
     */
    public MediaScanBuilder setMediaSelectedListener(OnMediaSelectedListener listener) {
        mMediaScanParam.mediaSelectedListener = listener;
        return this;
    }

    /**
     * 开始扫描
     * @param requestCode
     */
    public void scanMediaForResult(int requestCode) {
        Activity activity = mMediaScanEngine.getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, MediaScanActivity.class);
        Fragment fragment = mMediaScanEngine.getFragment();
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * 开始扫描
     */
    public void scanMedia() {
        Activity activity = mMediaScanEngine.getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, MediaScanActivity.class);
        Fragment fragment = mMediaScanEngine.getFragment();
        if (fragment != null) {
            fragment.startActivity(intent);
        } else {
            activity.startActivity(intent);
        }
    }
}
