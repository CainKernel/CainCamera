package com.cgfay.picker;

import com.cgfay.picker.loader.MediaLoader;

/**
 * 选图器管理器，用于管理监听器的
 */
public class MediaPickerManager {

    private static volatile MediaPickerManager sInstance;

    public static MediaPickerManager getInstance() {
        if (sInstance == null) {
            synchronized (MediaPickerManager.class) {
                if (sInstance == null) {
                    sInstance = new MediaPickerManager();
                }
            }
        }
        return sInstance;
    }

    private MediaLoader mMediaLoader;

    private MediaPickerManager() {
        reset();
    }

    public void reset() {
        mMediaLoader = new PickerMediaLoader();
    }

    public MediaPickerManager setMediaLoader(MediaLoader loader) {
        mMediaLoader = loader;
        return this;
    }

    public MediaLoader getMediaLoader() {
        return mMediaLoader;
    }
}
