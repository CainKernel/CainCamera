package com.cgfay.camera.presenter;

/**
 * @author CainHuang
 * @date 2019/7/3
 */
public class IPresenter <T> {

    private T mTarget;

    public IPresenter(T target) {
        mTarget = target;
    }

    public T getTarget() {
        return mTarget;
    }

    public void onCreate() {

    }

    public void onStart() {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {

    }

    public void onDestroy() {

    }
}
