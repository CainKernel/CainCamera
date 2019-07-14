package com.cgfay.caincamera.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;

import com.cgfay.caincamera.presenter.RecordPresenter;
import com.cgfay.camera.engine.render.RenderIndex;
import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.base.GLImageOESInputFilter;
import com.cgfay.filter.glfilter.color.GLImageDynamicColorFilter;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.multiframe.GLImageDrosteFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.filter.glfilter.utils.TextureRotationUtils;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 录制线程
 * @author CainHuang
 * @date 2019/7/13
 */
public class RecordRenderer implements GLSurfaceView.Renderer {

    // 录制状态
    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;

    private GLImageOESInputFilter mInputFilter; // 相机输入滤镜
    private GLImageFilter mColorFilter;  // 颜色滤镜
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
    private int mInputTexture;
    private SurfaceTexture mSurfaceTexture;
    private float[] mMatrix = new float[16];
    // presenter
    private final WeakReference<RecordPresenter> mWeakPresenter;

    public RecordRenderer(RecordPresenter presenter) {
        mWeakPresenter = new WeakReference<>(presenter);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);

        mInputTexture = OpenGLUtils.createOESTexture();
        mSurfaceTexture = new SurfaceTexture(mInputTexture);
        GLES30.glDisable(GL10.GL_DITHER);
        GLES30.glClearColor(0,0, 0, 0);
        GLES30.glEnable(GL10.GL_CULL_FACE);
        GLES30.glEnable(GL10.GL_DEPTH_TEST);
        initFilters();
        if (mWeakPresenter.get() != null) {
            mWeakPresenter.get().onBindSurfaceTexture(mSurfaceTexture);
            mWeakPresenter.get().onBindSharedContext(EGL14.eglGetCurrentContext());
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        onFilterSizeChanged();
        adjustCoordinateSize();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mMatrix);
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
        // 将德罗斯特滤镜绘制到FBO中
        currentTexture = mColorFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        // 将最终的结果会是预览
        mImageFilter.drawFrame(currentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer);
        // 录制视频
        if (mWeakPresenter.get() != null) {
            mWeakPresenter.get().onRecordFrameAvailable(currentTexture, mSurfaceTexture.getTimestamp());
        }
    }

    public void setTextureSize(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
    }

    private void initFilters() {
        mInputFilter = new GLImageOESInputFilter(mWeakPresenter.get().getActivity());
        mColorFilter = new GLImageDrosteFilter(mWeakPresenter.get().getActivity());
        mImageFilter = new GLImageFilter(mWeakPresenter.get().getActivity());
    }

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
        if (mImageFilter != null) {
            mImageFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mImageFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
    }

    /**
     * 切换滤镜
     * @param context
     * @param color
     */
    public synchronized void changeDynamicFilter(@NonNull Context context, DynamicColor color) {
        if (mColorFilter != null) {
            mColorFilter.release();
            mColorFilter = null;
        }
        if (color == null) {
            return;
        }
        mColorFilter = new GLImageDynamicColorFilter(context, color);
        mColorFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        mColorFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
        mColorFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
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
}
