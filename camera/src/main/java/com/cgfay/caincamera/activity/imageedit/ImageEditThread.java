package com.cgfay.caincamera.activity.imageedit;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.os.HandlerThread;

import com.cgfay.cainfilter.camerarender.FilterManager;
import com.cgfay.cainfilter.gles.EglCore;
import com.cgfay.cainfilter.gles.OffscreenSurface;
import com.cgfay.cainfilter.glfilter.base.GLImageFilter;
import com.cgfay.cainfilter.glfilter.group.GLImageEditFilterGroup;
import com.cgfay.cainfilter.glfilter.image.GLImageInputFilter;
import com.cgfay.cainfilter.type.GLFilterGroupType;
import com.cgfay.cainfilter.type.GLFilterType;
import com.cgfay.cainfilter.utils.GlUtil;
import com.cgfay.utilslibrary.BitmapUtils;

/**
 * 图片编辑线程
 * Created by Administrator on 2018/3/13.
 */

public class ImageEditThread extends HandlerThread {

    private static final String TAG = "ImageEditThread";

    // EGL共享上下文
    private EglCore mEglCore;
    // EGLSurface
    private OffscreenSurface mOffscreenSurface;
    // 输入Texture
    private int mTextureId = GlUtil.GL_NOT_INIT;
    // 当前Texture
    private int mCurrentTextureId;
    // 输入滤镜
    private GLImageInputFilter mInputFilter;
    // 滤镜组
    private GLImageEditFilterGroup mImageFilter;
    // 输出图片
    private GLImageFilter mDisplayFilter;
    // 图片宽度
    private int mImageWidth = -1;
    // 图片高度
    private int mImageHeight = -1;
    // 渲染Handler回调
    private ImageEditHandler mHandler;

    private OnImageEditListener mListener;

    private Bitmap mBitmap;

    public ImageEditThread(String name, OnImageEditListener listener) {
        super(name);
        mListener = listener;
    }

    public void setImageEditHandler(ImageEditHandler handler) {
        mHandler = handler;
    }

    public void setImageEditListener(OnImageEditListener listener) {
        mListener = listener;
    }

    /**
     * 渲染图片
     */
    void drawImage() {
        mOffscreenSurface.makeCurrent();
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        mCurrentTextureId = mTextureId;
        if (mInputFilter != null) {
            mCurrentTextureId = mInputFilter.drawFrameBuffer(mCurrentTextureId);
        }

        if (mImageFilter != null) {
            mCurrentTextureId = mImageFilter.drawFrameBuffer(mCurrentTextureId);
        }

        if (mDisplayFilter != null) {
            GLES30.glViewport(0, 0, mImageWidth, mImageHeight);
            mDisplayFilter.drawFrame(mCurrentTextureId);
        }
        if (mListener != null) {
            mListener.onSaveImageListener(mOffscreenSurface.getCurrentFrame(),
                    mOffscreenSurface.getWidth(), mOffscreenSurface.getHeight());
        }
        mOffscreenSurface.swapBuffers();
    }

    /**
     * 请求刷新
     */
    private void requestRender() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_DRAW_IMAGE));
        }
    }

    /**
     * 设置亮度
     * @param brightness
     */
    public void setBrightness(float brightness) {
        if (brightness < 0) {
            brightness = 0;
        } else if (brightness > 1) {
            brightness = 1;
        }
        if (mImageFilter != null) {
            mImageFilter.setBrightness(brightness);
            requestRender();
        }
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        if (contrast < 0) {
            contrast = 0;
        } else if (contrast > 2) {
            contrast = 2;
        }
        if (mImageFilter != null) {
            mImageFilter.setContrast(contrast);
            requestRender();
        }
    }

    /**
     * 设置曝光
     * @param exposure
     */
    public void setExposure(float exposure) {
        if (exposure < 0) {
            exposure = 0;
        } else if (exposure > 1) {
            exposure = 1;
        }
        if (mImageFilter != null) {
            mImageFilter.setExposure(exposure);
            requestRender();
        }
    }

    /**
     * 设置色调 0 ~ 360度
     * @param hue
     */
    public void setHue(float hue) {
        if (mImageFilter != null) {
            mImageFilter.setHue(hue);
            requestRender();
        }
    }

    /**
     * 设置饱和度 0.0 ~ 2.0之间
     * @param saturation
     */
    public void setSaturation(float saturation) {
        if (saturation < 0) {
            saturation = 0;
        } else if (saturation > 2) {
            saturation = 2;
        }
        if (mImageFilter != null) {
            mImageFilter.setSaturation(saturation);
            requestRender();
        }
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        if (sharpness < 0) {
            sharpness = 0;
        } else if (sharpness > 1) {
            sharpness = 1;
        }
        if (mImageFilter != null) {
            mImageFilter.setSharpness(sharpness);
            requestRender();
        }
    }

    /**
     * 切换滤镜
     * @param type
     */
    public void changeFilter(GLFilterType type) {
        if (mImageFilter != null) {
            mImageFilter.changeFilter(type);
            requestRender();
        }
    }

    /**
     * 切换滤镜组
     * @param type
     */
    public void changeFilterGroup(GLFilterGroupType type) {
        // TODO 切换滤镜组
    }

    /**
     * 创建Texture
     * @param bitmap
     */
    public void createBitmapTexture(Bitmap bitmap) {

        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }

        // 销毁旧数据
        if (mTextureId != GlUtil.GL_NOT_INIT) {
            GLES30.glDeleteTextures(1, new int[]{mTextureId}, 0);
            mTextureId = GlUtil.GL_NOT_INIT;
        }

        mBitmap = BitmapUtils.flipBitmap(bitmap, false, true);
        // 当宽高发生变化时，需要重新创建离屏渲染Surface
        if (mImageWidth != mBitmap.getWidth() || mImageHeight != mBitmap.getHeight()) {
            initSurfaceAndFilter(mBitmap.getWidth(), mBitmap.getHeight());
        }

        mTextureId = GlUtil.createTexture(mBitmap);

        // Texture创建完成回调
        if (mListener != null) {
            mListener.onTextureCreated();
        }
    }

    /**
     * 初始化EGLSurface 和 filter
     * @param width
     * @param height
     */
    void initSurfaceAndFilter(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
        if (mEglCore == null) {
            mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        }
        if (mOffscreenSurface != null) {
            mOffscreenSurface.release();
        }
        mOffscreenSurface = new OffscreenSurface(mEglCore, width, height);
        mOffscreenSurface.makeCurrent();
        if (mInputFilter == null) {
            mInputFilter = new GLImageInputFilter();
        }
        if (mImageFilter == null) {
            mImageFilter = new GLImageEditFilterGroup();
        }
        if (mDisplayFilter == null) {
            mDisplayFilter = FilterManager.getFilter(GLFilterType.NONE);
        }
        onSizeChange(mImageWidth, mImageHeight);
    }

    /**
     * 销毁EGLSurface和滤镜
     */
    public void destroySurfaceAndFilter() {
        if (mListener != null) {
            mListener = null;
        }
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }

        if (mImageFilter != null) {
            mImageFilter.release();
            mImageFilter = null;
        }

        if (mOffscreenSurface != null) {
            mOffscreenSurface.release();
            mOffscreenSurface = null;
        }

        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    /**
     * 输入图像发生变化
     * @param width
     * @param height
     */
    private void onSizeChange(int width, int height) {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(width, height);
            mInputFilter.onDisplayChanged(width, height);
            mInputFilter.initFramebuffer(width, height);
        }
        if (mImageFilter != null) {
            mImageFilter.onInputSizeChanged(width, height);
            mImageFilter.onDisplayChanged(width, height);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(width, height);
            mDisplayFilter.onDisplayChanged(width, height);
        }
    }
}
