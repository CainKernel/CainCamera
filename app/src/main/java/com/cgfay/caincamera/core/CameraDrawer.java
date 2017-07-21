package com.cgfay.caincamera.core;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLES20;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.filter.base.GuassFilter;
import com.cgfay.caincamera.filter.base.SaturationFilter;
import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.FullFrameRect;
import com.cgfay.caincamera.gles.OffscreenSurface;
import com.cgfay.caincamera.gles.WindowSurface;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.utils.GlUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * 使用枚举方式实现单例
 * 在surfaceCreated中创建线程实例，在surfaceDestroyed中销毁线程，
 * 主要是为了防止线程未释放，会导致退出后的内存呈现锯齿状，内存泄漏。
 * Created by cain on 2017/7/9.
 */

public enum CameraDrawer implements SurfaceTexture.OnFrameAvailableListener {

    INSTANCE;

    private CameraDrawerHandler mDrawerHandler;
    private HandlerThread mHandlerThread;

    // 锁
    private final Object mSynOperation = new Object();
    private final Object mSyncIsLooping = new Object();
    private long loopingInterval; // 根据fps计算
    private boolean isPreviewing = false;   // 是否预览状态
    private boolean isRecording = false;    // 是否录制状态

    CameraDrawer() {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        create();
        if (mDrawerHandler != null) {
            mDrawerHandler.sendMessage(mDrawerHandler
                    .obtainMessage(CameraDrawerHandler.MSG_SURFACE_CREATED, holder));
        }
    }

    public void surfacrChanged(int width, int height) {
        if (mDrawerHandler != null) {
            mDrawerHandler.sendMessage(mDrawerHandler
                    .obtainMessage(CameraDrawerHandler.MSG_SURFACE_CHANGED, width, height));
        }
        startPreview();
    }

    public void surfaceDestroyed() {
        stopPreview();
        if (mDrawerHandler != null) {
            mDrawerHandler.sendMessage(mDrawerHandler
                    .obtainMessage(CameraDrawerHandler.MSG_SURFACE_DESTROYED));
        }
        destory();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mDrawerHandler != null) {
            mDrawerHandler.addNewFrame();
        }
    }

    /**
     * 创建HandlerThread和Handler
     */
    private void create() {
        mHandlerThread = new HandlerThread("CameraDrawer Thread");
        mHandlerThread.start();
        mDrawerHandler = new CameraDrawerHandler(mHandlerThread.getLooper());
        mDrawerHandler.sendEmptyMessage(CameraDrawerHandler.MSG_INIT);
        loopingInterval = CameraUtils.DESIRED_PREVIEW_FPS;
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        if (mDrawerHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mDrawerHandler.sendMessage(mDrawerHandler
                    .obtainMessage(CameraDrawerHandler.MSG_START_PREVIEW));
            synchronized (mSyncIsLooping) {
                if (!isPreviewing && !isRecording) {
                    mDrawerHandler.removeMessages(CameraDrawerHandler.MSG_DRAW);
                    mDrawerHandler.sendMessageDelayed(mDrawerHandler.obtainMessage(
                            CameraDrawerHandler.MSG_DRAW,
                            SystemClock.uptimeMillis() + loopingInterval), loopingInterval);
                }
                isPreviewing = true;
            }
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mDrawerHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mDrawerHandler.sendMessage(mDrawerHandler
                    .obtainMessage(CameraDrawerHandler.MSG_STOP_PREVIEW));
            synchronized (mSyncIsLooping) {
                isPreviewing = false;
            }
        }
    }

    /**
     * 更新预览大小
     * @param width
     * @param height
     */
    public void updatePreview(int width, int height) {
        if (mDrawerHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mDrawerHandler.sendMessage(mDrawerHandler
                    .obtainMessage(CameraDrawerHandler.MSG_UPDATE_PREVIEW, width, height));
        }
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        if (mDrawerHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mDrawerHandler.sendMessage(mDrawerHandler
                    .obtainMessage(CameraDrawerHandler.MSG_START_RECORDING));
            synchronized (mSyncIsLooping) {
                if (!isPreviewing && !isRecording) {
                    mDrawerHandler.removeMessages(CameraDrawerHandler.MSG_DRAW);
                    mDrawerHandler.sendMessageDelayed(mDrawerHandler.obtainMessage(
                            CameraDrawerHandler.MSG_DRAW,
                            SystemClock.uptimeMillis() + loopingInterval), loopingInterval);
                }
                isRecording = true;
            }
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mDrawerHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mDrawerHandler.sendEmptyMessage(CameraDrawerHandler.MSG_STOP_RECORDING);
            synchronized (mSyncIsLooping) {
                isRecording = false;
            }
        }
    }

    /**
     * 拍照
     */
    public void takePicture() {
        if (mDrawerHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            // 发送拍照命令
            mDrawerHandler.sendMessage(mDrawerHandler
                    .obtainMessage(CameraDrawerHandler.MSG_TAKE_PICTURE));
            synchronized (mSyncIsLooping) {
                // 如果当前不处于预览和录制状态，则发送绘制命令
                if (!isPreviewing && !isRecording) {
                    mDrawerHandler.removeMessages(CameraDrawerHandler.MSG_DRAW);
                    mDrawerHandler.sendMessageDelayed(mDrawerHandler.obtainMessage(
                            CameraDrawerHandler.MSG_DRAW,
                            SystemClock.uptimeMillis() + loopingInterval), loopingInterval);
                }
            }
        }
    }

    /**
     * 销毁当前持有的Looper 和 Handler
     */
    public void destory() {
        // Handler不存在时，需要销毁当前线程，否则可能会出现重新打开不了的情况
        if (mDrawerHandler == null) {
            if (mHandlerThread != null) {
                mHandlerThread.quitSafely();
                try {
                    mHandlerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandlerThread = null;
            }
            return;
        }
        synchronized (mSynOperation) {
            mDrawerHandler.sendEmptyMessage(CameraDrawerHandler.MSG_DESTROY);
            mHandlerThread.quitSafely();
            try {
                mHandlerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mHandlerThread = null;
            mDrawerHandler = null;
        }
    }

    private class CameraDrawerHandler extends Handler {

        private static final String TAG = "CameraDrawer";

        static final int MSG_SURFACE_CREATED = 0x001;
        static final int MSG_SURFACE_CHANGED = 0x002;
        static final int MSG_FRAME = 0x003;
        static final int MSG_DRAW = 0x004;
        static final int MSG_RESET = 0x005;
        static final int MSG_SURFACE_DESTROYED = 0x006;
        static final int MSG_INIT = 0x007;
        static final int MSG_DESTROY = 0x008;

        static final int MSG_START_PREVIEW = 0x100;
        static final int MSG_STOP_PREVIEW = 0x101;
        static final int MSG_UPDATE_PREVIEW = 0x102;

        static final int MSG_START_RECORDING = 0x200;
        static final int MSG_STOP_RECORDING = 0x201;

        static final int MSG_RESET_BITRATE = 0x300;

        static final int MSG_TAKE_PICTURE = 0x400;

        // EGL共享上下文
        private EglCore mEglCore;
        // EGLSurface
        private WindowSurface mDisplaySurface;
        // 离屏渲染EGLSurface
        private OffscreenSurface mOffScreenSurface;
        // ImageReader
        private ImageReader mImageReader;
        private WindowSurface mImageReaderWindowSurface;
        private Bitmap mBitmap;
        // CameraTexture对应的Id
        private int mTextureId;
        private SurfaceTexture mCameraTexture;
        // 绘制的实体
        private FullFrameRect mFullFrameRect;
        // 矩阵
        private final float[] mTmpMatrix = new float[16];
        // 视图宽高
        private int mViewWidth, mViewHeight;

        // 离屏渲染FBO 和 Texture
        private int mInputFramebuffer;
        private int mInputFramebufferTexture;
        private int mFramebuffer;
        private int mFramebufferTexture;
        // 更新帧的锁
        private final Object mSyncFrameNum = new Object();
        private final Object mSyncTexture = new Object();
        private int mFrameNum = 0;
        private boolean hasNewFrame = false;
        public boolean dropNextFrame = false;
        private boolean isTakePicture = false;
        private boolean mSaveFrame = false;
        private int mSkipFrame = 0;

        public CameraDrawerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                // 初始化
                case MSG_INIT:
                    break;

                // 销毁
                case MSG_DESTROY:

                    break;

                // surfacecreated
                case MSG_SURFACE_CREATED:
                    onSurfaceCreated((SurfaceHolder)msg.obj);
                    break;

                // surfaceChanged
                case MSG_SURFACE_CHANGED:
                    onSurfaceChanged(msg.arg1, msg.arg2);
                    break;

                // surfaceDestroyed;
                case MSG_SURFACE_DESTROYED:
                    onSurfaceDestoryed();
                    break;

                // 帧可用（考虑同步的问题）
                case MSG_FRAME:
                    drawFrame();
                    break;

                // 绘制滤镜
                case MSG_DRAW:
                    synchronized (mSyncIsLooping) {
                        long time = (Long)msg.obj;
                        long interval = time + loopingInterval - SystemClock.uptimeMillis();
                        if (isPreviewing || isRecording) {
                            mDrawerHandler.sendMessageDelayed(mDrawerHandler.obtainMessage(
                                    CameraDrawerHandler.MSG_DRAW,
                                    SystemClock.uptimeMillis() + interval), interval);
                        } else {
                            mDrawerHandler.sendMessage(mDrawerHandler.obtainMessage(
                                    CameraDrawerHandler.MSG_DRAW,
                                    SystemClock.uptimeMillis() + loopingInterval));
                        }
                    }
                    // 有新的帧时就进行绘制
                    if (hasNewFrame) {
                        // 绘制特效
                        drawEffect();
                        // 绘制录制
                        drawMediaCodecOrCaptureFrame();
                        // 绘制到屏幕
                        drawToScreen();
                        hasNewFrame = false;
                    }
                    break;

                // 重置
                case MSG_RESET:
                    if (mOffScreenSurface != null) {
                        mOffScreenSurface.makeCurrent();
                        resetFramebuffer();
                    }
                    break;

                // 开始预览
                case MSG_START_PREVIEW:
                    break;

                // 停止预览
                case MSG_STOP_PREVIEW:
                    break;

                // 更新预览大小
                case MSG_UPDATE_PREVIEW:
                    synchronized (mSyncIsLooping) {
                        mViewWidth = msg.arg1;
                        mViewHeight = msg.arg2;
                    }
                    break;

                // 开始录制
                case MSG_START_RECORDING:
                    break;

                // 停止录制
                case MSG_STOP_RECORDING:
                    break;

                // 重置bitrate(录制视频时使用)
                case MSG_RESET_BITRATE:
                    break;

                // 拍照
                case MSG_TAKE_PICTURE:
                    isTakePicture = true;
                    break;

                default:
                    throw new IllegalStateException("Can not handle message what is: " + msg.what);
            }
        }

        private void onSurfaceCreated(SurfaceHolder holder) {
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
            mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
            CameraUtils.openFrontalCamera(CameraUtils.DESIRED_PREVIEW_FPS);
            // 禁用深度测试和背面绘制
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }

        private void onSurfaceChanged(int width, int height) {
            mViewWidth = width;
            mViewHeight = height;
            mOffScreenSurface = new OffscreenSurface(mEglCore, width, height);
            mOffScreenSurface.makeCurrent();
            mFullFrameRect = new FullFrameRect();
            mTextureId = mFullFrameRect.createTextureOES();
            mFullFrameRect.addFilter(new SaturationFilter());
            mCameraTexture = new SurfaceTexture(mTextureId);
            mCameraTexture.setOnFrameAvailableListener(CameraDrawer.this);
            CameraUtils.startPreviewTexture(mCameraTexture);
            resetFramebuffer();
            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
            mImageReader.setOnImageAvailableListener(mImageReaderOnImageAvailable, null);
            mImageReaderWindowSurface = new WindowSurface(mEglCore, mImageReader.getSurface(), false);
        }

        private void onSurfaceDestoryed() {
            CameraUtils.releaseCamera();
            if (mCameraTexture != null) {
                mCameraTexture.release();
                mCameraTexture = null;
            }
            if (mDisplaySurface != null) {
                mDisplaySurface.release();
                mDisplaySurface = null;
            }
            if (mOffScreenSurface != null) {
                mOffScreenSurface.release();
                mOffScreenSurface = null;
            }
            if (mImageReaderWindowSurface != null) {
                mImageReaderWindowSurface.release();
                mImageReaderWindowSurface = null;
            }
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
            if (mFullFrameRect != null) {
                mFullFrameRect.release();
                mFullFrameRect = null;
            }
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
            uninitFramebuffer();
        }

        /**
         * 绘制帧
         */
        private void drawFrame() {
            synchronized (mSyncFrameNum) {
                synchronized (mSyncTexture) {
                    if (mCameraTexture != null) {
                        // 如果存在新的帧，则更新帧
                        while (mFrameNum != 0) {
                            mOffScreenSurface.makeCurrent();
                            mCameraTexture.updateTexImage();
                            --mFrameNum;
                            // 是否舍弃下一帧
                            if (!dropNextFrame) {
                                hasNewFrame = true;
                            } else {
                                dropNextFrame = false;
                                hasNewFrame = false;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
            drawCameraTexture();
        }

        /**
         * 将Camera的OES的纹理读入到FBO中
         */
        private void drawCameraTexture() {
            if (mInputFramebuffer <= 0) {
                return;
            }
            mOffScreenSurface.makeCurrent();
            mCameraTexture.getTransformMatrix(mTmpMatrix);
            if (mFullFrameRect != null) {
                GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
                mFullFrameRect.drawCameraTexture(mInputFramebuffer, mTextureId, mTmpMatrix);
            }
        }

        /**
         * 绘制特效到FBO
         */
        private void drawEffect() {
            if (mFullFrameRect != null) {
                mOffScreenSurface.makeCurrent();
                mFullFrameRect.drawFramebuffer(mFramebuffer, mInputFramebufferTexture, mTmpMatrix);
            }
        }

        /**
         * 使用MediaCodec进行录制或者拍照
         */
        private void drawMediaCodecOrCaptureFrame() {
            if (isRecording) {
                // TODO: 录制视频

            } else if (isTakePicture) {
                isTakePicture = false;
                mSaveFrame = true;
                // 切换到保存图片的上下文
                mImageReaderWindowSurface.makeCurrent();
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glViewport(0, 0, mImageReaderWindowSurface.getWidth(),
                        mImageReaderWindowSurface.getHeight());
                if (mSkipFrame == 0) {
                    // 绘制到Framebuffer
                    if (mFullFrameRect != null) {
                        mFullFrameRect.drawScreen(mFramebufferTexture, mTmpMatrix);
                    }
                }
                mImageReaderWindowSurface.swapBuffers();
                // 恢复上下文
                GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
                mOffScreenSurface.makeCurrent();
            }
        }

        /**
         * 绘制到屏幕
         */
        private void drawToScreen() {
            if (mFullFrameRect != null) {
                mDisplaySurface.makeCurrentReadFrom(mOffScreenSurface);
                GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
                mFullFrameRect.drawScreen(mFramebufferTexture, mTmpMatrix);
                mDisplaySurface.swapBuffers();
            }
        }

        /**
         * 添加新的一帧
         */
        public void addNewFrame() {
            synchronized (mSyncFrameNum) {
                ++mFrameNum;
                removeMessages(MSG_FRAME);
                sendMessageAtFrontOfQueue(obtainMessage(MSG_FRAME));
            }
        }

        /**
         * 重置Framebuffer
         */
        public void resetFramebuffer() {
            uninitFramebuffer();
            initFramebuffer();
        }

        /**
         * 初始化Framebuffer
         */
        private void initFramebuffer() {
            int[] fb = new int[1], fbt = new int[1];
            GlUtil.createSampler2DFrameBuff(fb, fbt, mViewWidth, mViewHeight);
            mInputFramebuffer = fb[0];
            mInputFramebufferTexture = fbt[0];
            GlUtil.createSampler2DFrameBuff(fb, fbt, mViewWidth, mViewHeight);
            mFramebuffer = fb[0];
            mFramebufferTexture = fbt[0];
        }

        /**
         * 删除Framebuffer
         */
        private void uninitFramebuffer() {
            GLES20.glDeleteFramebuffers(1, new int[]{mFramebuffer}, 0);
            GLES20.glDeleteTextures(1, new int[]{mFramebufferTexture}, 0);
            GLES20.glDeleteFramebuffers(1, new int[]{mInputFramebuffer}, 0);
            GLES20.glDeleteTextures(1, new int[]{mInputFramebufferTexture}, 0);
        }

        /**
         * ImageReader 监听器
         */
        private ImageReader.OnImageAvailableListener mImageReaderOnImageAvailable =
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Image image = imageReader.acquireLatestImage();
                        ByteBuffer[] byteBuffers = new ByteBuffer[1];
                        byteBuffers[0] = cloneByteBuffer(image.getPlanes()[0].getBuffer());
                        // TODO: 这里保存的照片右边存在黑边，需要做些修改
                        if (mBitmap == null) {
                            int pixelStride = image.getPlanes()[0].getPixelStride();
                            int rowStride = image.getPlanes()[0].getRowStride();
                            int rowPadding = rowStride - pixelStride * mImageReader.getWidth();
                            mBitmap = Bitmap.createBitmap(
                                    mImageReader.getWidth() + rowPadding / pixelStride,
                                    mImageReader.getHeight(), Bitmap.Config.ARGB_8888);
                        }
                        byteBuffers[0].rewind();
                        mBitmap.copyPixelsFromBuffer(byteBuffers[0]);
                        // 保存图片
                        if (mSaveFrame) {
                            mSaveFrame = false;
                            if (mBitmap != null) {
                                String path = Environment.getExternalStorageDirectory() + "/DCIM/Camera/"
                                        + System.currentTimeMillis() + ".jpg";
                                try {
                                    FileOutputStream fout = new FileOutputStream(path);
                                    BufferedOutputStream bos = new BufferedOutputStream(fout);
                                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                                    bos.flush();
                                    bos.close();
                                    fout.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    image.close();
                                }
                            }
                        } else {
                            image.close();
                        }
                    }
                };

        /**
         * 复制字节数据
         * @param byteBuffer
         * @return
         */
        private ByteBuffer cloneByteBuffer(final ByteBuffer byteBuffer) {
            ByteBuffer clone = ByteBuffer.allocate(byteBuffer.capacity());
            byteBuffer.rewind();
            clone.put(byteBuffer);
            byteBuffer.rewind();
            clone.flip();
            return clone;
        }
    }
}
