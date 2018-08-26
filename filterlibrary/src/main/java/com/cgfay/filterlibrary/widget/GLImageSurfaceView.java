package com.cgfay.filterlibrary.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageInputFilter;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 图片GL渲染视图
 */
public class GLImageSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    // 输入纹理
    protected int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
    // 图片输入滤镜
    protected GLImageInputFilter mInputFilter;
    // 颜色滤镜
    protected GLImageFilter mColorFilter;
    // 显示输出
    protected GLImageFilter mDisplayFilter;

    // 输入纹理大小
    protected int mTextureWidth;
    protected int mTextureHeight;
    // 控件视图大小
    protected int mViewWidth;
    protected int mViewHeight;

    // 输入图片
    private Bitmap mBitmap;

    // 记录当前滤镜类型，用于暂停重新渲染的结果
    protected GLImageFilterType mFilterType = GLImageFilterType.NONE;

    // UI线程Handler，主要用于更新UI等
    protected Handler mMainHandler;

    public GLImageSurfaceView(Context context) {
        this(context, null);
    }

    public GLImageSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onPause() {
        super.onPause();
        mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
        mColorFilter = null;
        mDisplayFilter = null;
        mInputFilter = null;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glDisable(GL10.GL_DITHER);
        GLES30.glClearColor(0,0, 0, 0);
        GLES30.glEnable(GL10.GL_CULL_FACE);
        GLES30.glEnable(GL10.GL_DEPTH_TEST);
        initFilters();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        GLES30.glViewport(0,0,width, height);
        if (mInputTexture == OpenGLUtils.GL_NOT_TEXTURE) {
            mInputTexture = OpenGLUtils.createTexture(mBitmap, mInputTexture);
        }
        // Note: 如果此时显示输出滤镜对象为空，则表示调用了onPause方法销毁了所有GL对象资源，需要重新初始化滤镜
        if (mDisplayFilter == null) {
            initFilters();
        }
        onFilterSizeChanged();
    }

    /**
     * 初始化滤镜
     */
    private void initFilters() {
        if (mInputFilter == null) {
            mInputFilter = new GLImageInputFilter(getContext());
        } else {
            mInputFilter.initProgramHandle();
        }
        if (mColorFilter == null && mFilterType != GLImageFilterType.NONE) {
            mColorFilter = GLImageFilterManager.getFilter(getContext(), mFilterType);
        } else if (mColorFilter != null) {
            mColorFilter.initProgramHandle();
        }
        if (mDisplayFilter == null) {
            mDisplayFilter = new GLImageFilter(getContext());
        } else {
            mDisplayFilter.initProgramHandle();
        }

        if (mBitmap != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    calculateViewSize();
                }
            });
        }
    }

    /**
     * 滤镜大小发生变化
     */
    private void onFilterSizeChanged() {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mInputFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mInputFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mColorFilter != null) {
            mColorFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mColorFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mColorFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mDisplayFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClearColor(0,0, 0, 0);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        if (mDisplayFilter == null) {
            return;
        }
        int currentTexture = mInputTexture;
        if (mInputFilter != null) {
            currentTexture = mInputFilter.drawFrameBuffer(currentTexture);
        }
        if (mColorFilter != null) {
            currentTexture = mColorFilter.drawFrameBuffer(currentTexture);
        }
        mDisplayFilter.drawFrame(currentTexture);
    }

    /**
     * 设置滤镜
     * @param type
     */
    public void setFilter(final GLImageFilterType type) {
        mFilterType = type;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mColorFilter != null) {
                    mColorFilter.release();
                    mColorFilter = null;
                }
                mColorFilter = GLImageFilterManager.getFilter(getContext(), type);
                onFilterSizeChanged();
                requestRender();
            }
        });
    }

    /**
     * 设置滤镜
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mTextureWidth = mBitmap.getWidth();
        mTextureHeight = mBitmap.getHeight();
        requestRender();
    }

    /**
     * 计算视图大小
     */
    private void calculateViewSize() {
        if (mTextureWidth == 0 || mTextureHeight == 0) {
            return;
        }
        if (mViewWidth == 0 || mViewHeight == 0) {
            mViewWidth = getWidth();
            mViewHeight = getHeight();
        }
        float ratio = mTextureWidth * 1.0f / mTextureHeight;
        double viewAspectRatio = (double) mViewWidth / mViewHeight;
        if (ratio < viewAspectRatio) {
            mViewWidth = (int) (mViewHeight * ratio);
        } else {
            mViewHeight = (int) (mViewWidth / ratio);
        }
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = mViewWidth;
        layoutParams.height = mViewHeight;
        setLayoutParams(layoutParams);
    }
}
