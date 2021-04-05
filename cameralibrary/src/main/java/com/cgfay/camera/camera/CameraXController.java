package com.cgfay.camera.camera;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * CameraX库封装处理
 */
public class CameraXController implements ICameraController {

    private static final String TAG = "CameraXController";
    // 16:9的默认宽高(理想值)，CameraX的预览方式与Camera1不一致，设置的预览宽高需要是实际的预览宽高
    private static final int DEFAULT_16_9_WIDTH = 720;
    private static final int DEFAULT_16_9_HEIGHT = 1280;

    // 预览宽度
    private int mPreviewWidth = DEFAULT_16_9_WIDTH;
    // 预览高度
    private int mPreviewHeight = DEFAULT_16_9_HEIGHT;
    // 预览角度
    private int mRotation;

    // 生命周期对象(Fragment/Activity)
    private final FragmentActivity mLifecycleOwner;

    // 是否打开前置摄像头
    private boolean mFacingFront;

    // Camera提供者
    private ProcessCameraProvider mCameraProvider;
    // Camera接口
    private Camera mCamera;
    // 预览配置
    private Preview mPreview;

    // 预览帧
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    private ImageAnalysis mPreviewAnalyzer;
    // 预览回调
    private PreviewCallback mPreviewCallback;
    // SurfaceTexture准备监听器
    private OnSurfaceTextureListener mSurfaceTextureListener;
    // 纹理更新监听器
    private OnFrameAvailableListener mFrameAvailableListener;
    // 相机数据输出的SurfaceTexture
    private SurfaceTexture mOutputTexture;

    public CameraXController(@NonNull FragmentActivity lifecycleOwner) {
        Log.d(TAG, "CameraXController: created!");
        mLifecycleOwner = lifecycleOwner;
        mFacingFront = true;
        mRotation = 90;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(mLifecycleOwner);
        cameraProviderFuture.addListener(() -> {
            try {
                mCameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(mLifecycleOwner));
    }

    /**
     * 初始化相机配置
     */
    private void bindCameraUseCases() {
        if (mCameraProvider == null) {
            return;
        }

        // 解除绑定
        mCameraProvider.unbindAll();

        // 预览画面
        mPreview = new Preview
                .Builder()
                .setTargetResolution(new Size(mPreviewWidth, mPreviewHeight))
                .build();

        // 预览绑定SurfaceTexture
        mPreview.setSurfaceProvider(surfaceRequest -> {
            // 创建SurfaceTexture
            SurfaceTexture surfaceTexture =
                    createDetachedSurfaceTexture(surfaceRequest.getResolution());
            Surface surface = new Surface(surfaceTexture);
            surfaceRequest.provideSurface(surface, mExecutor, result -> {
                surface.release();
            });
            if (mSurfaceTextureListener != null) {
                mSurfaceTextureListener.onSurfaceTexturePrepared(mOutputTexture);
            }
        });

        // 预览帧回调
        mPreviewAnalyzer = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(mPreviewWidth, mPreviewHeight))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        mPreviewAnalyzer.setAnalyzer(mExecutor, new PreviewCallbackAnalyzer(mPreviewCallback));

        // 前后置摄像头选择器
        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(mFacingFront ?
                        CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK).build();

        // 绑定输出
        mCamera = mCameraProvider.bindToLifecycle(mLifecycleOwner, cameraSelector, mPreview,
                mPreviewAnalyzer);
    }

    /**
     * 创建一个SurfaceTexture并
     */
    private SurfaceTexture createDetachedSurfaceTexture(@NonNull Size size) {
        // 创建一个新的SurfaceTexture并从解绑GL上下文
        if (mOutputTexture == null) {
            mOutputTexture = new SurfaceTexture(0);
            mOutputTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
            mOutputTexture.detachFromGLContext();
            mOutputTexture.setOnFrameAvailableListener(texture -> {
                if (mFrameAvailableListener != null) {
                    mFrameAvailableListener.onFrameAvailable(texture);
                }
            });
        }
        return mOutputTexture;
    }

    /**
     * 释放输出的SurfaceTexture，防止内存泄露
     */
    private void releaseSurfaceTexture() {
        if (mOutputTexture != null) {
            mOutputTexture.release();
            mOutputTexture = null;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void closeCamera() {
        try {
            if (mCameraProvider != null) {
                mCameraProvider.unbindAll();
                mCameraProvider = null;
            }
            releaseSurfaceTexture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOnSurfaceTextureListener(OnSurfaceTextureListener listener) {
        mSurfaceTextureListener = listener;
    }

    @Override
    public void setPreviewCallback(PreviewCallback callback) {
        mPreviewCallback = callback;
    }

    @Override
    public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
        mFrameAvailableListener = listener;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void switchCamera() {
        boolean front = isFront();
        setFront(!front);

        // 解除绑定
        mCameraProvider.unbindAll();


        // 前后置摄像头选择器
        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(mFacingFront ?
                        CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK).build();

        // 绑定输出
        mCamera = mCameraProvider.bindToLifecycle(mLifecycleOwner, cameraSelector, mPreview,
                mPreviewAnalyzer);
    }

    @Override
    public void setFront(boolean front) {
        mFacingFront = front;
    }

    @Override
    public boolean isFront() {
        return mFacingFront;
    }

    @Override
    public int getOrientation() {
        return mRotation;
    }

    @Override
    public int getPreviewWidth() {
        if (mRotation == 90 || mRotation == 270) {
            return mPreviewHeight;
        }
        return mPreviewWidth;
    }

    @Override
    public int getPreviewHeight() {
        if (mRotation == 90 || mRotation == 270) {
            return mPreviewWidth;
        }
        return mPreviewHeight;
    }

    @Override
    public boolean canAutoFocus() {
        return false;
    }

    @Override
    public void setFocusArea(Rect rect) {

    }

    @Override
    public Rect getFocusArea(float x, float y, int width, int height, int focusSize) {
        return null;
    }

    @Override
    public boolean supportTorch(boolean front) {
        if (mCamera != null) {
            return !mCamera.getCameraInfo().hasFlashUnit();
        }
        return true;
    }

    @Override
    public void setFlashLight(boolean on) {
        if (supportTorch(isFront())) {
            Log.e(TAG, "Failed to set flash light: " + on);
            return;
        }
        if (mCamera != null) {
            mCamera.getCameraControl().enableTorch(on);
        }
    }

    @Override
    public void zoomIn() {
        if (mCamera != null) {
            ZoomState zoomState =
                    Objects.requireNonNull(mCamera.getCameraInfo().getZoomState().getValue());
            float currentZoomRatio = Math.min(zoomState.getMaxZoomRatio(),
                    zoomState.getZoomRatio() + 0.1f);
            mCamera.getCameraControl().setZoomRatio(currentZoomRatio);
        }
    }

    @Override
    public void zoomOut() {
        if (mCamera != null) {
            ZoomState zoomState =
                    Objects.requireNonNull(mCamera.getCameraInfo().getZoomState().getValue());
            float currentZoomRatio = Math.max(zoomState.getMinZoomRatio(),
                    zoomState.getZoomRatio() - 0.1f);
            mCamera.getCameraControl().setZoomRatio(currentZoomRatio);
        }
    }
}
