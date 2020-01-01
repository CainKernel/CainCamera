package com.cgfay.camera;

import android.app.Activity;
import androidx.fragment.app.Fragment;

import com.cgfay.camera.model.AspectRatio;

import java.lang.ref.WeakReference;

/**
 * 相机预览引擎
 */
public final class PreviewEngine {

    private WeakReference<Activity> mWeakActivity;
    private WeakReference<Fragment> mWeakFragment;

    private PreviewEngine(Activity activity) {
        this(activity, null);
    }

    private PreviewEngine(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private PreviewEngine(Activity activity, Fragment fragment) {
        mWeakActivity = new WeakReference<>(activity);
        mWeakFragment = new WeakReference<>(fragment);
    }

    public static PreviewEngine from(Activity activity) {
        return new PreviewEngine(activity);
    }

    public static PreviewEngine from(Fragment fragment) {
        return new PreviewEngine(fragment);
    }

    /**
     * 设置长宽比
     * @param ratio
     * @return
     */
    public PreviewBuilder setCameraRatio(AspectRatio ratio) {
        return new PreviewBuilder(this, ratio);
    }

    public Activity getActivity() {
        return mWeakActivity.get();
    }

    public Fragment getFragment() {
        return mWeakFragment.get();
    }


}
