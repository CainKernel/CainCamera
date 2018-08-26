package com.cgfay.medialibrary.engine;

import com.cgfay.medialibrary.listener.OnCaptureListener;
import com.cgfay.medialibrary.listener.OnMediaSelectedListener;
import com.cgfay.medialibrary.loader.MediaLoader;
import com.cgfay.medialibrary.loader.impl.GlideMediaLoader;
import com.cgfay.medialibrary.model.MimeType;

import java.util.Set;

/**
 * 媒体扫描参数
 */
public final class MediaScanParam {

    private static final MediaScanParam mInstance = new MediaScanParam();

    // 是否允许长按
    public boolean longClickEnable;
    // 是否允许多选
    public boolean multiSelectEnable;
    // 是否显示拍照Item
    public boolean showCapture;
    // 是否显示图片Item
    public boolean showImage;
    // 是否显示视频Item
    public boolean showVideo;
    // 是否允许选择GIF图片
    public boolean enableSelectGif;
    // 横向item的数量
    public int spanCount = 3;
    // 分割线大小
    public int spaceSize;
    // 期望的item大小
    public int expectedItemSize;
    // 缩略图缩放比例
    public float thumbnailScale;
    // 图片加载器
    public MediaLoader mediaLoader;
    // 加载类型集合
    public Set<MimeType> mimeTypes;
    // 拍照监听器
    public OnCaptureListener captureListener;
    // 媒体选择监听器
    public OnMediaSelectedListener mediaSelectedListener;

    private MediaScanParam() {
        reset();
    }

    /**
     * 获取初始单例对象
     * @return
     */
    public static MediaScanParam getInitialInstance() {
        MediaScanParam mediaScanParam = getInstance();
        mediaScanParam.reset();
        return mediaScanParam;
    }

    /**
     * 重置为初始状态
     */
    private void reset() {
        showCapture = true;
        showImage = true;
        showVideo = true;
        enableSelectGif = true;
        longClickEnable = false;
        multiSelectEnable = false;
        spanCount = 3;
        thumbnailScale = 0.5f;
        mediaLoader = new GlideMediaLoader();
    }

    /**
     * 获取当前单例
     * @return
     */
    public static MediaScanParam getInstance() {
        return mInstance;
    }

    /**
     * 仅显示图片
     * @return
     */
    public boolean showImageOnly() {
        return showImage && !showVideo;
    }

    /**
     * 仅显示视频
     * @return
     */
    public boolean showVideoOnly() {
        return showVideo && !showImage;
    }

    /**
     * 是否允许长按
     * @return
     */
    public boolean isLongClickEnable() {
        return longClickEnable;
    }

    /**
     * 是否允许多选
     * @return
     */
    public boolean isMultiSelectEnable() {
        return multiSelectEnable;
    }
}
