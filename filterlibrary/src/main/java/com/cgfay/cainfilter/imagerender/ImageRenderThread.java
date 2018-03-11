package com.cgfay.cainfilter.imagerender;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.os.HandlerThread;
import android.view.SurfaceHolder;

import com.cgfay.cainfilter.camerarender.FilterManager;
import com.cgfay.cainfilter.gles.EglCore;
import com.cgfay.cainfilter.gles.WindowSurface;
import com.cgfay.cainfilter.glfilter.base.GLImageFilter;
import com.cgfay.cainfilter.glfilter.group.GLImageEditFilterGroup;
import com.cgfay.cainfilter.glfilter.image.GLImageInputFilter;
import com.cgfay.cainfilter.type.GLFilterType;
import com.cgfay.cainfilter.utils.GlUtil;
import com.cgfay.utilslibrary.BitmapUtils;

import java.io.File;

/**
 * 图片渲染线程
 * Created by Administrator on 2018/3/8.
 */

public class ImageRenderThread extends HandlerThread {

    private static final String TAG = "ImageRenderThread";

    // EGL共享上下文
    private EglCore mEglCore;
    // EGLSurface
    private WindowSurface mDisplaySurface;
    // 图片路径
    private String mImagePath;
    // 图片
    private Bitmap mBitmap;
    // 输入Texture
    private int mTextureId = GlUtil.GL_NOT_INIT;
    // 当前TextureId
    private int mCurrentTextureId;
    // 输入滤镜
    private GLImageInputFilter mInputFilter;
    // 滤镜组
    private GLImageEditFilterGroup mImageFilter;
    // 输出图片
    private GLImageFilter mDisplayFilter;
    // 图片宽度
    private int mImageWidth;
    // 图片高度
    private int mImageHeight;
    // 视图宽度
    private int mViewWidth;
    // 视图高度
    private int mViewHeight;
    // 屏幕宽度
    private int mScreenWidth;
    // 屏幕高度
    private int mScreenHeight;

    private ImageRenderHandler mHandler;


    public ImageRenderThread(String name) {
        super(name);
    }

    public void setImageEditHandler(ImageRenderHandler handler) {
        mHandler = handler;
    }

    void surfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();
        // 创建图像的Texture
        createBitmapTexture();
        initFilter();
    }

    void surfaceChanged(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        onDisplaySizeChanged(width, height);
        requestRender();
    }

    void surfaceDestoryed() {
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }
        if (mImageFilter != null) {
            mImageFilter.release();
            mImageFilter = null;
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
     * 绘制图片
     */
    void drawImage() {
        mDisplaySurface.makeCurrent();
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
            GLES30.glViewport(0, 0, mViewWidth, mViewHeight);
            mDisplayFilter.drawFrame(mCurrentTextureId);
        }
        mDisplaySurface.swapBuffers();
    }

    /**
     * 请求刷新
     */
    private void requestRender() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageRenderHandler.MSG_DRAW_IMAGE));
        }
    }

    /**
     * 初始化图片滤镜组
     */
    private void initFilter() {
        mInputFilter = new GLImageInputFilter();
        mImageFilter = new GLImageEditFilterGroup();
        mDisplayFilter = FilterManager.getFilter(GLFilterType.NONE);
        onInputSizeChanged(mImageWidth, mImageHeight);
    }
    /**
     * 设置图片路径
     * @param path
     */
    public void setImagePath(String path) {
        mImagePath = path;
    }


    /**
     * 设置屏幕大小
     * @param width
     * @param height
     */
    public void setScreenSize(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }

    /**
     * 输入图像大小发生变化
     * @param width
     * @param height
     */
    private void onInputSizeChanged(int width, int height) {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(width, height);
            mInputFilter.initFramebuffer(width, height);
        }
        if (mImageFilter != null) {
            mImageFilter.onInputSizeChanged(width, height);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(width, height);
        }
    }

    /**
     * 显示视图大小发生变化时
     * @param width
     * @param height
     */
    private void onDisplaySizeChanged(int width, int height) {
        if (mInputFilter != null) {
            mInputFilter.onDisplayChanged(width, height);
        }
        if (mImageFilter != null) {
            mImageFilter.onDisplayChanged(width, height);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onDisplayChanged(width, height);
        }
    }


    /**
     * 根据图片创建新的Texture
     */
    private void createBitmapTexture() {
        if (mImagePath == null) {
            return;
        }
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        // 销毁以前的Texture
        if (mTextureId != GlUtil.GL_NOT_INIT) {
            GLES30.glDeleteTextures(1, new int[]{mTextureId}, 0);
            mTextureId = GlUtil.GL_NOT_INIT;
        }
        // 重新创建Bitmap 和Texture
        Bitmap bitmap = BitmapUtils.getBitmapFromFile(new File(mImagePath), mScreenWidth, mScreenHeight);
        mBitmap = BitmapUtils.flipBitmap(bitmap, false, true);
        bitmap.recycle();
        mImageWidth = mBitmap.getWidth();
        mImageHeight = mBitmap.getHeight();
        if (mImageFilter != null) {
            mImageFilter.onInputSizeChanged(mImageWidth, mImageHeight);
        }
        mTextureId = GlUtil.createTexture(mBitmap);
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
     * 切换滤镜组
     * @param type
     */
    public void changeFilter(GLFilterType type) {
        if (mImageFilter != null) {
            mImageFilter.changeFilter(type);
            requestRender();
        }
    }
}
