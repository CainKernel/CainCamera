package com.cgfay.caincamera.renderer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cgfay.caincamera.presenter.FFMediaRecordPresenter;
import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.base.GLImageOESInputFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.filter.glfilter.utils.TextureRotationUtils;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * FFmpeg录制渲染器
 */
public class FFRecordRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "FFRecordRenderer";
    private static final boolean VERBOSE = false;

    private GLImageOESInputFilter mInputFilter; // 相机输入滤镜
    private GLImageFilter mImageFilter; // 输出滤镜
    // 顶点坐标缓冲
    private FloatBuffer mVertexBuffer;
    // 纹理坐标缓冲
    private FloatBuffer mTextureBuffer;
    // 预览顶点坐标缓冲
    private FloatBuffer mDisplayVertexBuffer;
    // 预览纹理坐标缓冲
    private FloatBuffer mDisplayTextureBuffer;
    // 输入纹理大小
    protected int mTextureWidth;
    protected int mTextureHeight;
    // 控件视图大小
    protected int mViewWidth;
    protected int mViewHeight;
    // 输入纹理
    private int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
    private float[] mMatrix = new float[16];
    private volatile boolean mNeedToAttach;
    private WeakReference<SurfaceTexture> mWeakSurfaceTexture;

    private final WeakReference<FFMediaRecordPresenter> mWeakPresenter;

    public FFRecordRenderer(FFMediaRecordPresenter presenter) {
        mWeakPresenter = new WeakReference<>(presenter);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (VERBOSE) {
            Log.d(TAG, "onSurfaceCreated: ");
        }
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);

        GLES30.glDisable(GL10.GL_DITHER);
        GLES30.glClearColor(0,0, 0, 0);
        GLES30.glEnable(GL10.GL_CULL_FACE);
        GLES30.glEnable(GL10.GL_DEPTH_TEST);
        initFilters();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (VERBOSE) {
            Log.d(TAG, "onSurfaceChanged: ");
        }
        mViewWidth = width;
        mViewHeight = height;
        onFilterSizeChanged();
        adjustCoordinateSize();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mWeakSurfaceTexture == null || mWeakSurfaceTexture.get() == null) {
            return;
        }
        synchronized (this) {
            final SurfaceTexture texture = mWeakSurfaceTexture.get();
            updateSurfaceTexture(texture);
        }
        GLES30.glClearColor(0,0, 0, 0);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        if (mInputFilter == null || mImageFilter == null) {
            return;
        }
        mInputFilter.setTextureTransformMatrix(mMatrix);
        // 将OES纹理绘制到FBO中
        int currentTexture = mInputTexture;
        currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        // 将最终的结果会是预览
        mImageFilter.drawFrame(currentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer);
    }

    /**
     * 更新输出纹理数据
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
        if (VERBOSE) {
            Log.d(TAG, "updateSurfaceTexture: ");
        }
    }

    /**
     * 绑定纹理
     * @param surfaceTexture
     */
    public void bindSurfaceTexture(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            mWeakSurfaceTexture = new WeakReference<>(surfaceTexture);
            mNeedToAttach = true;
        }
    }

    /**
     * 设置输入纹理大小
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
        if (mViewWidth != 0 && mViewHeight != 0) {
            onFilterSizeChanged();
            adjustCoordinateSize();
        }
        if (VERBOSE) {
            Log.d(TAG, "setTextureSize: width = " + width + ", height = " + height);
        }
    }

    /**
     * 初始化滤镜
     */
    private void initFilters() {
        mInputFilter = new GLImageOESInputFilter(mWeakPresenter.get().getActivity());
        mImageFilter = new GLImageFilter(mWeakPresenter.get().getActivity());
    }

    /**
     * 初始化FBO等
     */
    private void onFilterSizeChanged() {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mInputFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mInputFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mImageFilter != null) {
            mImageFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mImageFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
    }

    /**
     * 调整由于surface的大小与SurfaceView大小不一致带来的显示问题
     */
    private void adjustCoordinateSize() {
        float[] textureCoord;
        float[] vertexCoord = TextureRotationUtils.CubeVertices;
        float[] textureVertices = TextureRotationUtils.TextureVertices;
        float ratioMax = Math.max((float) mViewWidth / mTextureWidth,
                (float) mViewHeight / mTextureHeight);
        // 新的宽高
        float imageWidth = mTextureWidth * ratioMax;
        float imageHeight = mTextureHeight * ratioMax;
        // 获取视图跟texture的宽高比
        float ratioWidth = imageWidth / (float) mViewWidth;
        float ratioHeight = imageHeight / (float) mViewHeight;
        float distHorizontal = (1 - 1 / ratioWidth) / 2;
        float distVertical = (1 - 1 / ratioHeight) / 2;
        textureCoord = new float[] {
                addDistance(textureVertices[0], distHorizontal), addDistance(textureVertices[1], distVertical),
                addDistance(textureVertices[2], distHorizontal), addDistance(textureVertices[3], distVertical),
                addDistance(textureVertices[4], distHorizontal), addDistance(textureVertices[5], distVertical),
                addDistance(textureVertices[6], distHorizontal), addDistance(textureVertices[7], distVertical),
        };
        // 更新VertexBuffer 和 TextureBuffer
        mDisplayVertexBuffer.clear();
        mDisplayVertexBuffer.put(vertexCoord).position(0);
        mDisplayTextureBuffer.clear();
        mDisplayTextureBuffer.put(textureCoord).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    /**
     * 清理一下任务数据
     */
    public void clear() {
        if (mWeakSurfaceTexture != null) {
            mWeakSurfaceTexture.clear();
        }
    }

}
