package com.cgfay.caincamera.activity.camera;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;
import com.cgfay.caincamera.utils.GlUtil;

/**
 * 渲染线程
 * Created by cain.huang on 2017/11/1.
 */

public class RenderTestThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "RenderTestThread";

    private final Handler mHandler;

    private EglCore mEglCore;
    private WindowSurface mDisplaySurface;
    private SurfaceTexture mSurfaceTexture;
    private final float[] mMatrix = new float[16];

    private int mCameraTexture;

    private int mImageWidth;
    private int mImageHeight;
    // 预览的角度
    private int mOrientation;

    public RenderTestThread() {
        super(TAG);
        start();
        mHandler = new Handler(getLooper());
    }

    public RenderTestThread(String name) {
        super(name);
        start();
        mHandler = new Handler(getLooper());
    }

    /**
     * 销毁线程
     */
    public void destoryThread() {
        internalRelease();
        mHandler.removeCallbacksAndMessages(null);
        quitSafely();
    }

    /**
     * 检查handler是否可用
     */
    private void checkHandleAvailable() {
        if (mHandler == null) {
            throw new NullPointerException("Handler is not available!");
        }
    }

    /**
     * 等待
     */
    private void waitUntilReady() {
        try {
            wait();
        } catch (InterruptedException e) {
            Log.w(TAG, "wait was interrupted");
        }
    }

    synchronized public void surfaceCreated(final SurfaceHolder holder) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSurfaceCreated(holder);
                notifySurfaceProcessed();
            }
        });
        waitUntilReady();
    }

    synchronized public void surfaceChanged(final int width, final int height) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSurfaceChanged(width, height);
                notifySurfaceProcessed();
            }
        });
        waitUntilReady();
    }

    synchronized public void surfaceDestory() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSurfaceDestory();
                notifySurfaceProcessed();
            }
        });
        waitUntilReady();
    }

    /**
     * Surface变化处理完成通知
     */
    synchronized private void notifySurfaceProcessed() {
        notify();
    }

    /**
     * 设置图片大小
     * @param width         宽度
     * @param height        高度
     * @param orientation   角度
     */
    public void setImageSize(int width, int height, int orientation) {
        mImageWidth = width;
        mImageHeight = height;
        mOrientation = orientation;
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalupdateImageSize();
            }
        });
    }

    /**
     * 更新帧
     */
    public void updateFrame() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalRendering();
            }
        });
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        updateFrame();
    }

    // ------------------------------ 内部方法 -------------------------------------

    private void internalSurfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE | EglCore.FLAG_TRY_GLES3);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();
        mCameraTexture = GlUtil.createTextureOES();
        mSurfaceTexture = new SurfaceTexture(mCameraTexture);
        RenderManager.getInstance().init();
    }

    private void internalSurfaceChanged(int width, int height) {
        RenderManager.getInstance().onDisplaySizeChanged(width, height);
    }

    private void internalSurfaceDestory() {
        internalRelease();
    }

    /**
     * 释放所有资源
     */
    private void internalRelease() {
        if (mEglCore != null) {
            mEglCore.release();
        }
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }
        RenderManager.getInstance().release();
    }

    /**
     * 更新图片大小(相机流大小)
     */
    private void internalupdateImageSize() {
        calculateImageSize();
        RenderManager.getInstance().onInputSizeChanged(mImageWidth, mImageHeight);
    }

    /**
     * 计算image的宽高
     */
    private void calculateImageSize() {
        Log.d("calculateImageSize", "orientation = " + mOrientation);
        if (mOrientation == 90 || mOrientation == 270) {
            int temp = mImageWidth;
            mImageWidth = mImageHeight;
            mImageHeight = temp;
        }
    }
    /**
     * 渲染
     */
    private void internalRendering() {
        mDisplaySurface.makeCurrent();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
        renderFrame();
        mDisplaySurface.swapBuffers();
    }

    /**
     * 渲染一帧
     */
    private void renderFrame() {
        mSurfaceTexture.getTransformMatrix(mMatrix);
        RenderManager.getInstance().setTransformMatrix(mMatrix);
        RenderManager.getInstance().drawFrame(mCameraTexture);
    }

    // ------------------------------- setter and getter ---------------------------

    /**
     * 获取SurfaceTexture
     * @return
     */
    public SurfaceTexture getSurafceTexture() {
        return mSurfaceTexture;
    }

}
