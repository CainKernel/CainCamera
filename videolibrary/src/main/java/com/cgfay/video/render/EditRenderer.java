package com.cgfay.video.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.filter.gles.EglCore;
import com.cgfay.filter.gles.WindowSurface;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.opengles.GL10;

/**
 * 编辑渲染器
 */
public class EditRenderer extends Thread {

    private static final String TAG = "EditRenderer";

    private final Object mSync = new Object();

    private int mPriority;
    private Looper mLooper;

    private @Nullable EditRenderHandler mHandler;

    // EGL共享上下文
    private EglCore mEglCore;
    // 预览用的EGLSurface
    private WindowSurface mDisplaySurface;
    private volatile boolean mNeedToAttach;
    private WeakReference<SurfaceTexture> mWeakSurfaceTexture;
    // 矩阵
    private final float[] mMatrix = new float[16];
    // 输入OES纹理
    private int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
    // 当前纹理
    private int mCurrentTexture;
    // 渲染管理器
    private final EditRenderManager mRenderManager;

    private volatile boolean mThreadStarted;

    public EditRenderer() {
        super(TAG);
        mPriority = Process.THREAD_PRIORITY_DISPLAY;
        mRenderManager = new EditRenderManager();
        mThreadStarted = false;
    }

    /**
     * 初始化渲染器
     */
    public void initRenderer() {
        synchronized (this) {
            if (!mThreadStarted) {
                start();
                mThreadStarted = true;
            }
        }
    }

    /**
     * 销毁渲染器
     */
    public void destroyRenderer() {
        synchronized (this) {
            quit();
        }
    }

    /**
     * 暂停时释放SurfaceTexture
     */
    public void onPause() {
        if (mWeakSurfaceTexture != null) {
            mWeakSurfaceTexture.clear();
        }
    }

    /**
     * 绑定Surface
     * @param surface
     */
    public void onSurfaceCreated(Surface surface) {
        Handler handler = getHandler();
        handler.sendMessage(handler.obtainMessage(EditRenderHandler.MSG_INIT, surface));
    }

    /**
     * 绑定SurfaceTexture
     * @param surfaceTexture
     */
    public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
        Handler handler = getHandler();
        handler.sendMessage(handler.obtainMessage(EditRenderHandler.MSG_INIT, surfaceTexture));
    }

    /**
     * 设置预览大小
     * @param width
     * @param height
     */
    public void onSurfaceChanged(int width, int height) {
        Handler handler = getHandler();
        handler.sendMessage(handler.obtainMessage(EditRenderHandler.MSG_DISPLAY_CHANGE, width, height));
    }

    /**
     * 解绑Surface
     */
    public void onSurfaceDestroyed() {
        Handler handler = getHandler();
        handler.sendMessage(handler.obtainMessage(EditRenderHandler.MSG_DESTROY));
    }

    /**
     * 设置输入纹理大小
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height) {

    }

    /**
     * 绑定外部输入的SurfaceTexture
     * @param surfaceTexture
     */
    public void bindInputSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
        queueEvent(() -> {
            onBindInputSurfaceTexture(surfaceTexture);
        });
    }

    /**
     * 释放所有资源
     */
    void release() {
        Log.d(TAG, "release: ");
        if (mDisplaySurface != null) {
            mDisplaySurface.makeCurrent();
        }
        if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.deleteTexture(mInputTexture);
            mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
        if (mWeakSurfaceTexture != null) {
            mWeakSurfaceTexture.clear();
        }
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
            mDisplaySurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    /**
     * 渲染事件
     * @param runnable
     */
    public void queueEvent(@NonNull Runnable runnable) {
        getHandler().queueEvent(runnable);
    }

    /**
     * 请求渲染
     */
    public void requestRender() {
        getHandler().sendEmptyMessage(EditRenderHandler.MSG_RENDER);
    }

    /**
     * 切换滤镜
     * @param color
     */
    public void changeFilter(DynamicColor color) {
        Handler handler = getHandler();
        handler.sendMessage(handler.obtainMessage(EditRenderHandler.MSG_CHANGE_FILTER, color));
    }

    // ---------------------------------------- 渲染内部处理方法 -------------------------------------
    /**
     * 初始化渲染器
     */
    void initRender(Surface surface) {
//        if (mWeakPresenter == null || mWeakPresenter.get() == null) {
//            return;
//        }
        Log.d(TAG, "initRender: ");
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, surface, false);
        mDisplaySurface.makeCurrent();

        GLES30.glDisable(GL10.GL_DITHER);
        GLES30.glClearColor(0,0, 0, 0);
        GLES30.glEnable(GL10.GL_CULL_FACE);
        GLES30.glEnable(GL10.GL_DEPTH_TEST);

//        // 渲染器初始化
//        mRenderManager.init(mWeakPresenter.get().getContext());
//
//        if (mWeakPresenter.get() != null) {
//            mWeakPresenter.get().onBindSharedContext(mEglCore.getEGLContext());
//        }
    }

    /**
     * 初始化渲染器
     */
    void initRender(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "initRender: ");
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, surfaceTexture);
        mDisplaySurface.makeCurrent();

        GLES30.glDisable(GL10.GL_DITHER);
        GLES30.glClearColor(0,0, 0, 0);
        GLES30.glEnable(GL10.GL_CULL_FACE);
        GLES30.glEnable(GL10.GL_DEPTH_TEST);

//        // 渲染器初始化
//        mRenderManager.init(mWeakPresenter.get().getContext());
//
//        if (mWeakPresenter.get() != null) {
//            mWeakPresenter.get().onBindSharedContext(mEglCore.getEGLContext());
//        }
    }

    /**
     * 设置预览大小
     * @param width
     * @param height
     */
    void setDisplaySize(int width, int height) {
        mRenderManager.setDisplaySize(width, height);
    }

    /**
     * 渲染一帧数据
     */
    void onDrawFrame() {
        if (mDisplaySurface == null || mWeakSurfaceTexture == null || mWeakSurfaceTexture.get() == null) {
            return;
        }
        // 切换渲染上下文
        mDisplaySurface.makeCurrent();

        // 更新纹理
        long timeStamp = 0;
        synchronized (this) {
            final SurfaceTexture surfaceTexture = mWeakSurfaceTexture.get();
            updateSurfaceTexture(surfaceTexture);
            timeStamp = surfaceTexture.getTimestamp();
        }

        // 如果不存在外部输入纹理，则直接返回，不做处理
        if (mInputTexture == OpenGLUtils.GL_NOT_TEXTURE) {
            return;
        }
        // 绘制渲染
        mCurrentTexture = mRenderManager.drawFrame(mInputTexture, mMatrix);

        // 显示到屏幕
        mDisplaySurface.swapBuffers();
    }

    /**
     * 更新输入纹理
     * @param surfaceTexture
     */
    private void updateSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
        // 绑定到当前的输入纹理
        if (mNeedToAttach) {
            if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                OpenGLUtils.deleteTexture(mInputTexture);
            }
            mInputTexture = OpenGLUtils.createOESTexture();
            surfaceTexture.attachToGLContext(mInputTexture);
            mNeedToAttach = false;
        }
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mMatrix);
    }

    /**
     * 绑定外部输入的SurfaceTexture
     * @param surfaceTexture
     */
    private void onBindInputSurfaceTexture(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            mWeakSurfaceTexture = new WeakReference<>(surfaceTexture);
            mNeedToAttach = true;
        }
    }

    /**
     * 切换动态滤镜
     * @param color
     */
    void changeDynamicFilter(DynamicColor color) {
        synchronized (mSync) {
            mDisplaySurface.makeCurrent();
            mRenderManager.changeDynamicFilter(color);
        }
    }

    // -------------------------------------- HandlerThread核心 ------------------------------------
    @Override
    public void run() {
        Looper.prepare();
        synchronized (this) {
            mLooper = Looper.myLooper();
            notifyAll();
        }
        Process.setThreadPriority(mPriority);
        Looper.loop();
        // 移除所有消息并销毁所有资源
        getHandler().handleQueueEvent();
        getHandler().removeCallbacksAndMessages(null);
        release();
        mThreadStarted = false;
        Log.d(TAG, "Thread has delete!");
    }

    /**
     * 获取当前的Looper
     * @return
     */
    private Looper getLooper() {
        if (!isAlive()) {
            return null;
        }
        synchronized (this) {
            while (isAlive() && mLooper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {

                }
            }
        }
        return mLooper;
    }

    /**
     * 获取当前线程的Handler
     * @return
     */
    @NonNull
    public EditRenderHandler getHandler() {
        if (mHandler == null) {
            mHandler = new EditRenderHandler(getLooper(), this);
        }
        return mHandler;
    }

    /**
     * 退出渲染线程
     * @return
     */
    private boolean quit() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quitSafely();
            return true;
        }
        return false;
    }
}
