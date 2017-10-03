package com.cgfay.caincamera.view.preview;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by cain on 2017/10/4.
 */

public abstract class PreviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    // 路径
    protected String mPath;

    protected SurfaceHolder mHolder;

    public PreviewSurfaceView(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void setPath(String path) {
        mPath = path;
    }

}
