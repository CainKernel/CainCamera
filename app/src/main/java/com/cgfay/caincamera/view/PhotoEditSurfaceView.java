package com.cgfay.caincamera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.cgfay.caincamera.bean.ImageMeta;
import com.cgfay.caincamera.core.FilterManager;
import com.cgfay.caincamera.core.FilterType;
import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.filter.image.BrightnessFilter;
import com.cgfay.caincamera.filter.image.OriginalFilter;
import com.cgfay.caincamera.utils.GlUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 编辑图片用的GLSurfaceView
 * Created by cain.huang on 2017/8/10.
 */
public class PhotoEditSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {


    // 图片元数据
    private ImageMeta mImageMeta;
    // 图片
    private Bitmap mBitmap;
    // Texture
    private int mTextureId = GlUtil.GL_NOT_INIT;
    // 原始图片滤镜
    private OriginalFilter mImageFilter;
    // 滤镜
    private BaseImageFilter mFilter;
    // 图片宽度
    private int mImageWidth;
    // 图片高度
    private int mImageHeight;
    // 视图宽度
    private int mViewWidth;
    // 视图高度
    private int mViewHeight;

    public PhotoEditSurfaceView(Context context) {
        super(context);
        init();
    }

    public PhotoEditSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mImageFilter = new OriginalFilter();
        mFilter = new BrightnessFilter();
        ((BrightnessFilter)mFilter).setBrightness(0.5f);
        createBitmapTexture();
        mFilter.onInputSizeChanged(mImageWidth, mImageHeight);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        onFilterChanged();
        mFilter.onDisplayChanged(mViewWidth, mViewHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mBitmap == null || mBitmap.isRecycled()) {
            return;
        }
        // 销毁以前的Texture
        if (mTextureId == GlUtil.GL_NOT_INIT) {
            GLES30.glDeleteTextures(1, new int[]{mTextureId}, 0);
            mTextureId = GlUtil.GL_NOT_INIT;
        }
        mTextureId = GlUtil.createTexture(mBitmap);
        // 绘制流程
        mImageFilter.drawFrame(mTextureId);
        if (mFilter == null) {
            mImageFilter.drawFrame(mTextureId);
        } else {
            int id = mImageFilter.drawToTexture(mTextureId);
            mFilter.drawFrame(id);
        }
    }

    /**
     * 设置滤镜类型
     * @param type
     */
    public void setFilter(FilterType type) {
        if (mFilter != null) {
            mFilter.release();
        }
        mFilter = FilterManager.getFilter(type);
        onFilterChanged();
    }

    /**
     * 设置图片元数据
     * @param imageMeta
     */
    public void setImageMeta(ImageMeta imageMeta) {
        if (mImageMeta != imageMeta) {
            mImageMeta = imageMeta;
            createBitmapTexture();
            requestRender();
        }
    }

    /**
     * 滤镜或视图发生变化时调用
     */
    private void onFilterChanged() {
        mImageFilter.onDisplayChanged(mViewWidth, mViewHeight);
        if (mFilter != null) {
            mImageFilter.initFramebuffer(mImageWidth, mImageHeight);
            mFilter.onInputSizeChanged(mImageWidth, mImageHeight);
            mFilter.onDisplayChanged(mViewWidth, mViewHeight);
        } else {
            mImageFilter.destroyFramebuffer();
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
        // 重新创建Bitmap 和Texture
        mBitmap = BitmapFactory.decodeFile(mImageMeta.getPath());
        mImageWidth = mBitmap.getWidth();
        mImageHeight = mBitmap.getHeight();
    }
}
