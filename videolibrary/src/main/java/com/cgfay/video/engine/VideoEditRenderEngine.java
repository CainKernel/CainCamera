package com.cgfay.video.engine;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;

import com.cgfay.filterlibrary.gles.EglCore;
import com.cgfay.filterlibrary.gles.WindowSurface;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.video.bean.EffectType;

import java.lang.ref.WeakReference;

/**
 * 视频编辑渲染引擎
 *
 * 数据流如下：
 * -> YUV/RGBA数据 -> Buffer -> 输入
 * -> VideoRenderManager.drawCurrentFrame -> 渲染特效
 * -> OutputSurface/OutputSurfaceTexture -> 输出渲染的结果
 * -> 最后显示/合成
 */
public class VideoEditRenderEngine extends HandlerThread {

    // 当前时钟
    private long mCurrentPosition;

    private RenderHandler mHandler;

    public VideoEditRenderEngine(Context context) {
        super("VideoEditGLThread");
        mRenderManager = new VideoRenderManager(context);
        start();
        mHandler = new RenderHandler(this);
        mHandler.sendEmptyMessage(RenderHandler.MSG_EGL_INIT);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }

    /**
     * 释放资源
     */
    public synchronized void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendEmptyMessage(RenderHandler.MSG_EGL_RELEASE);
        }
        quitSafely();
        mRenderManager = null;
    }

    /**
     * 绑定输出的Surface/SurfaceTexture，用于显示或者ImageReader取出纹理数据
     * @param surface
     */
    public synchronized void bindOutputSurface(Object surface) {
        if (mHandler == null || !(surface instanceof SurfaceTexture || surface instanceof Surface)) {
            return;
        }
        Message message = new Message();
        message.what = RenderHandler.MSG_BIND_SURFACE;
        message.obj = surface;
        mHandler.sendMessage(message);
    }

    /**
     * 设置纹理大小
     * @param textureWidth
     * @param textureHeight
     */
    public synchronized void setVideoSize(int textureWidth, int textureHeight) {
        if (mHandler != null) {
            Message message = new Message();
            message.what = RenderHandler.MSG_TEXTURE_SIZE;
            message.arg1 = textureWidth;
            message.arg2 = textureHeight;
            mHandler.sendMessage(message);
        }
    }

    /**
     * 设置当前渲染时间
     * @param position
     */
    public synchronized void requestRender(long position) {
        mCurrentPosition = position;
        if (mHandler != null) {
            mHandler.sendEmptyMessage(RenderHandler.MSG_REQUEST_RENDER);
        }
    }

    /**
     * 更新YUV数据
     * @param ydata
     * @param udata
     * @param vdata
     * @param yLinesize
     * @param uLinesize
     * @param vLinesize
     */
    public synchronized void updateYUVData(byte[] ydata, byte[] udata, byte[] vdata,
                                           int yLinesize, int uLinesize, int vLinesize) {
        if (mRenderManager != null) {
            mRenderManager.updateYUVData(ydata, udata, vdata, yLinesize, uLinesize, vLinesize);
        }
    }

    /**
     * 更新BGRA数据
     * @param data
     * @param linesize
     */
    public synchronized void setBGRAData(byte[] data, int linesize) {
        if (mRenderManager != null) {
            mRenderManager.updateBGRAData(data, linesize);
        }
    }

    /**
     * 切换滤镜
     * @param color
     */
    public synchronized void changeDynamicFilter(DynamicColor color) {
        if (mHandler != null) {
            Message message = new Message();
            message.what = RenderHandler.MSG_CHANGE_FILTER;
            message.obj = color;
            mHandler.sendMessage(message);
        }
    }

    /**
     * 切换特效
     * @param effectType
     */
    public synchronized void changeEffectFilter(EffectType effectType) {
        if (mHandler != null) {
            Message message = new Message();
            message.what = RenderHandler.MSG_CHANGE_EFFECT;
            message.obj = effectType;
            mHandler.sendMessage(message);
        }
    }

    // ------------------------------------ GL线程处理逻辑 ------------------------------------------

    /**
     * 渲染线程Handler
     */
    static class RenderHandler extends Handler {
        // 创建EGLContext
        private static final int MSG_EGL_INIT = 0x01;
        // 释放EGLContext
        private static final int MSG_EGL_RELEASE = 0x02;
        // 创建EGLSurface
        private static final int MSG_BIND_SURFACE = 0x03;
        // 请求渲染
        private static final int MSG_REQUEST_RENDER = 0x04;
        // 设置纹理大小
        private static final int MSG_TEXTURE_SIZE = 0x05;
        // 切换滤镜
        private static final int MSG_CHANGE_FILTER = 0x06;
        // 切换特效
        private static final int MSG_CHANGE_EFFECT = 0x07;

        private WeakReference<VideoEditRenderEngine> mWeakEngine;

        public RenderHandler(VideoEditRenderEngine engine) {
            super(engine.getLooper());
            mWeakEngine = new WeakReference<>(engine);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakEngine == null || mWeakEngine.get() == null) {
                return;
            }
            VideoEditRenderEngine engine = mWeakEngine.get();
            switch (msg.what) {
                case MSG_EGL_INIT: {
                    engine.eglInit();
                    break;
                }

                case MSG_EGL_RELEASE: {
                    engine.eglRelease();
                    break;
                }

                case MSG_BIND_SURFACE: {
                    engine.bindEGLSurface(msg.obj);
                    break;
                }

                case MSG_REQUEST_RENDER: {
                    engine.onDrawFrame();
                    break;
                }

                case MSG_TEXTURE_SIZE: {
                    engine.onTextureSize(msg.arg1, msg.arg2);
                    break;
                }

                case MSG_CHANGE_FILTER: {
                    engine.onChangeDynamicFilter((DynamicColor)msg.obj);
                    break;
                }

                case MSG_CHANGE_EFFECT: {
                    engine.onChangeEffectFilter((EffectType)msg.obj);
                    break;
                }
            }
        }
    }

    // EGL共享上下文
    private EglCore mEglCore;
    // 渲染特效、滤镜的EGLSurface
    private WindowSurface mDisplaySurface;
    // 渲染管理器
    private VideoRenderManager mRenderManager;

    /**
     * 初始化EGLContext
     */
    private synchronized void eglInit() {
        if (mEglCore == null) {
            mEglCore = new EglCore();
        }
        mRenderManager.init();
    }

    /**
     * 释放EGLContext
     */
    private synchronized void eglRelease() {
        if (mRenderManager != null) {
            mRenderManager.release();
            mRenderManager = null;
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
     * 绑定EGLSurface
     * @param window
     */
    private synchronized void bindEGLSurface(Object window) {
        if (!(window instanceof SurfaceTexture || window instanceof Surface)) {
            return;
        }
        // 释放旧的EGLSurface，这里主要用于绑定TextureView 和 ImageReader的Surface
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
            mDisplaySurface = null;
        }
        if (window instanceof SurfaceTexture) {
            mDisplaySurface = new WindowSurface(mEglCore, (SurfaceTexture) window);
        } else {
            mDisplaySurface = new WindowSurface(mEglCore, (Surface) window, false);
        }
        mDisplaySurface.makeCurrent();
    }

    /**
     * 渲染滤镜特效
     */
    private synchronized void onDrawFrame() {
        if (mRenderManager == null) {
            return;
        }
        mDisplaySurface.makeCurrent();
        if (mRenderManager != null) {
            mRenderManager.drawCurrentFrame(mCurrentPosition);
        }
        mDisplaySurface.swapBuffers();
    }

    /**
     * 设置纹理大小
     * @param textureWidth
     * @param textureHeight
     */
    private synchronized void onTextureSize(int textureWidth, int textureHeight) {
        if (mRenderManager != null) {
            mRenderManager.setTextureSize(textureWidth, textureHeight);
        }
    }

    /**
     * 切换滤镜
     * @param color
     */
    private synchronized void onChangeDynamicFilter(DynamicColor color) {
        if (mRenderManager != null) {
            mRenderManager.changeDynamicFilter(color);
        }
    }

    /**
     * 切换特效滤镜
     * @param effectType
     */
    private synchronized void onChangeEffectFilter(EffectType effectType) {
        if (mRenderManager != null) {
            mRenderManager.changeEffectFilter(effectType);
        }
    }

}
