package com.cgfay.medialibrary.engine;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.cgfay.medialibrary.model.MimeType;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * 媒体库引擎
 */
public final class MediaScanEngine {

    private WeakReference<Activity> mWeakActivity;

    private WeakReference<Fragment> mWeakFragment;

    private MediaScanEngine(Activity activity) {
        this(activity, null);
    }

    private MediaScanEngine(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private MediaScanEngine(Activity activity, Fragment fragment) {
        mWeakActivity = new WeakReference<>(activity);
        mWeakFragment = new WeakReference<>(fragment);
    }

    public static MediaScanEngine from(Activity activity) {
        return new MediaScanEngine(activity);
    }

    public static MediaScanEngine from(Fragment fragment) {
        return new MediaScanEngine(fragment);
    }

    public MediaScanBuilder setMimeTypes(Set<MimeType> mimeTypes) {
        return new MediaScanBuilder(this, mimeTypes);
    }

    public Activity getActivity() {
        return mWeakActivity.get();
    }

    public Fragment getFragment() {
        return mWeakFragment.get();
    }



}
