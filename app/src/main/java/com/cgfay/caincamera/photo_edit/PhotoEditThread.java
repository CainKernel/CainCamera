package com.cgfay.caincamera.photo_edit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.bean.ImageMeta;
import com.cgfay.caincamera.filter.group.ImageEditFilterGroup;
import com.cgfay.caincamera.filter.image.OriginalFilter;
import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;
import com.cgfay.caincamera.utils.GlUtil;

/**
 * 图片编辑线程
 * Created by cain on 2017/11/15.
 */

public class PhotoEditThread extends HandlerThread {

    private static final String TAG = "PhotoEditThread";

    // EGL共享上下文
    private EglCore mEglCore;
    // EGLSurface
    private WindowSurface mDisplaySurface;

    // 图片元数据
    private ImageMeta mImageMeta;
    // 图片
    private Bitmap mBitmap;
    // Texture
    private int mTextureId = GlUtil.GL_NOT_INIT;
    // 原始图片滤镜
    private OriginalFilter mInputFilter;
    // 滤镜
    private ImageEditFilterGroup mImageEditFilter;
    // 图片宽度
    private int mImageWidth;
    // 图片高度
    private int mImageHeight;
    // 视图宽度
    private int mViewWidth;
    // 视图高度
    private int mViewHeight;

    private PhotoEditHandler mHandler;


    public PhotoEditThread(String name) {
        super(name);
    }

    public void setPhotoEditHandler(PhotoEditHandler handler) {
        mHandler = handler;
    }

    void surfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();
        // 创建图像的Texture
        createBitmapTexture();
        initImageEditFilter();
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
        if (mImageEditFilter != null) {
            mImageEditFilter.release();
            mImageEditFilter = null;
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
        if (mTextureId != GlUtil.GL_NOT_INIT) {
            mInputFilter.drawFrame(mTextureId);
            if (mImageEditFilter == null) {
                mInputFilter.drawFrame(mTextureId);
            } else {
                int id = mInputFilter.drawToTexture(mTextureId);
                mImageEditFilter.drawFrame(id);
            }
        }
        mDisplaySurface.swapBuffers();
    }

    /**
     * 请求刷新
     */
    private void requestRender() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(PhotoEditHandler.MSG_DRAW_IMAGE));
        }
    }

    /**
     * 初始化图片滤镜组
     */
    private void initImageEditFilter() {
        mInputFilter = new OriginalFilter();
        mImageEditFilter = new ImageEditFilterGroup();
        onInputSizeChanged(mImageWidth, mImageHeight);
    }
    /**
     * 设置图片元数据
     * @param imageMeta
     */
    public void setImageMeta(ImageMeta imageMeta) {
        if (mImageMeta != imageMeta) {
            mImageMeta = imageMeta;
        }
    }

    /**
     * 输入图像大小发生变化
     * @param width
     * @param height
     */
    private void onInputSizeChanged(int width, int height) {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(width, height);
        }
        if (mImageEditFilter != null) {
            mImageEditFilter.onInputSizeChanged(width, height);
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
        if (mImageEditFilter != null) {
            mInputFilter.initFramebuffer(mImageWidth, mImageHeight);
            mImageEditFilter.onDisplayChanged(width, height);
        } else {
            mInputFilter.destroyFramebuffer();
        }
    }


    /**
     * 根据图片创建新的Texture
     */
    private void createBitmapTexture() {
        if (mImageMeta == null) {
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
        mBitmap = BitmapFactory.decodeFile(mImageMeta.getPath());
        mImageWidth = mBitmap.getWidth();
        mImageHeight = mBitmap.getHeight();
        if (mImageEditFilter != null) {
            mImageEditFilter.onInputSizeChanged(mImageWidth, mImageHeight);
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
        if (mImageEditFilter != null) {
            mImageEditFilter.setBrightness(brightness);
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
        if (mImageEditFilter != null) {
            mImageEditFilter.setContrast(contrast);
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
        if (mImageEditFilter != null) {
            mImageEditFilter.setExposure(exposure);
            requestRender();
        }
    }

    /**
     * 设置色调 0 ~ 360度
     * @param hue
     */
    public void setHue(float hue) {
        if (mImageEditFilter != null) {
            mImageEditFilter.setHue(hue);
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
        if (mImageEditFilter != null) {
            mImageEditFilter.setSaturation(saturation);
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
        if (mImageEditFilter != null) {
            mImageEditFilter.setSharpness(sharpness);
            requestRender();
        }
    }
}
