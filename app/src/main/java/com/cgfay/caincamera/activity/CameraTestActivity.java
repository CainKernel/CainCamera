package com.cgfay.caincamera.activity;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.bean.Size;
import com.cgfay.caincamera.camera.CameraHandlerThread;
import com.cgfay.caincamera.camera.CameraManager;
import com.cgfay.caincamera.render.RenderThread;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.view.AspectFrameLayout;

public class CameraTestActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    private AspectFrameLayout mAspectLayout;

    private SurfaceView mSurfaceView;
    // 相机控制线程
    private CameraHandlerThread mCameraThread;
    // 渲染控制线程
    private RenderThread mRenderThread;

    private byte[] mPreviewBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);

        mAspectLayout = (AspectFrameLayout) findViewById(R.id.layout_aspect);
        mAspectLayout.setAspectRatio(CameraManager.getInstance().getCurrentRatio());

        mSurfaceView = new SurfaceView(this);
        mSurfaceView.getHolder().addCallback(this);

        mAspectLayout.addView(mSurfaceView);
        mAspectLayout.requestLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraThread = new CameraHandlerThread();
        mCameraThread.calculatePreviewOrientation(this);
        mRenderThread = new RenderThread();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCameraThread.openCamera();
        Size previewSize = mCameraThread.getPreviewSize();
        int size = previewSize.getWidth() * previewSize.getHeight() * 3 / 2;
        mPreviewBuffer = new byte[size];

        mRenderThread.surfaceCreated(holder);
        mRenderThread.setImageSize(previewSize.getWidth(), previewSize.getHeight());

        // 设置回调
        mCameraThread.setPreviewCallbackWithBuffer(CameraTestActivity.this, mPreviewBuffer);
        mCameraThread.startPreview(mRenderThread.getSurafceTexture());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mRenderThread.surfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraThread.destoryThread();
        mRenderThread.destoryThread();
    }

    long time = 0;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d("onPreviewFrame", "update time = " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        mRenderThread.updateFrame();

        if (mPreviewBuffer != null) {
            camera.addCallbackBuffer(mPreviewBuffer);
        }
    }
}
