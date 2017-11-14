package com.cgfay.caincamera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cgfay.caincamera.photo_edit.PhotoEditManager;

/**
 * Created by cain on 2017/11/15.
 */

public class PhotoEditSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public PhotoEditSurfaceView(Context context) {
        super(context);
        init();
    }

    public PhotoEditSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        PhotoEditManager.getInstance().surfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        PhotoEditManager.getInstance().surfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        PhotoEditManager.getInstance().surfaceDestoryed();
    }
}