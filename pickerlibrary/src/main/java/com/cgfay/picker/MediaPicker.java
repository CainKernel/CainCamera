package com.cgfay.picker;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.lang.ref.WeakReference;

public final class MediaPicker {

    public static final String PICKER_PARAMS = "PICKER_PARAMS";

    private WeakReference<FragmentActivity> mWeakActivity;

    private WeakReference<Fragment> mWeakFragment;

    private MediaPicker(FragmentActivity activity) {
        this(activity, null);
    }

    private MediaPicker(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private MediaPicker(FragmentActivity activity, Fragment fragment) {
        mWeakActivity = new WeakReference<>(activity);
        mWeakFragment = new WeakReference<>(fragment);
    }

    public static MediaPickerBuilder from(FragmentActivity activity) {
        return new MediaPickerBuilder(new MediaPicker(activity));
    }

    public static MediaPickerBuilder from(Fragment fragment) {
        return new MediaPickerBuilder(new MediaPicker(fragment));
    }

    public FragmentActivity getActivity() {
        return mWeakActivity.get();
    }

    public Fragment getFragment() {
        return mWeakFragment.get();
    }
}
