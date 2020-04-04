package com.cgfay.camera.camera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 相机控制器
 */
public class CameraController implements ICameraController, Camera.PreviewCallback {

    private static final String TAG = "CameraController";
    // 16:9的默认宽高(理想值)
    private static final int DEFAULT_16_9_WIDTH = 1280;
    private static final int DEFAULT_16_9_HEIGHT = 720;

    // 期望的fps
    private int mExpectFps = CameraParam.DESIRED_PREVIEW_FPS;
    // 预览宽度
    private int mPreviewWidth = DEFAULT_16_9_WIDTH;
    // 预览高度
    private int mPreviewHeight = DEFAULT_16_9_HEIGHT;
    // 预览角度
    private int mOrientation;
    // 相机对象
    private Camera mCamera;
    // 摄像头id
    private int mCameraId;
    // SurfaceTexture成功回调
    private OnSurfaceTextureListener mSurfaceTextureListener;
    // 预览数据回调
    private PreviewCallback mPreviewCallback;
    // 输出纹理更新回调
    private OnFrameAvailableListener mFrameAvailableListener;
    // 相机输出的SurfaceTexture
    private SurfaceTexture mOutputTexture;
    private HandlerThread mOutputThread;
    // 上下文
    private final Activity mActivity;

    public CameraController(@NonNull Activity activity) {
        Log.d(TAG, "CameraController: created！");
        mActivity = activity;
        mCameraId = CameraApi.hasFrontCamera(activity) ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    @Override
    public void openCamera() {
        closeCamera();
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        mCamera = Camera.open(mCameraId);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        CameraParam cameraParam = CameraParam.getInstance();
        cameraParam.cameraId = mCameraId;
        Camera.Parameters parameters = mCamera.getParameters();
        cameraParam.supportFlash = checkSupportFlashLight(parameters);
        cameraParam.previewFps = chooseFixedPreviewFps(parameters, mExpectFps * 1000);
        parameters.setRecordingHint(true);
        // 后置摄像头自动对焦
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK
                && supportAutoFocusFeature(parameters)) {
            mCamera.cancelAutoFocus();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(parameters);
        setPreviewSize(mCamera, mPreviewWidth, mPreviewHeight);
        setPictureSize(mCamera, mPreviewWidth, mPreviewHeight);
        mOrientation = calculateCameraPreviewOrientation(mActivity);
        mCamera.setDisplayOrientation(mOrientation);
        releaseSurfaceTexture();
        mOutputTexture = createDetachedSurfaceTexture();
        try {
            mCamera.setPreviewTexture(mOutputTexture);
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        if (mSurfaceTextureListener != null) {
            mSurfaceTextureListener.onSurfaceTexturePrepared(mOutputTexture);
        }
    }

    /**
     * 创建一个SurfaceTexture并
     * @return
     */
    private SurfaceTexture createDetachedSurfaceTexture() {
        // 创建一个新的SurfaceTexture并从解绑GL上下文
        SurfaceTexture surfaceTexture = new SurfaceTexture(0);
        surfaceTexture.detachFromGLContext();
        if (Build.VERSION.SDK_INT >= 21) {
            if (mOutputThread != null) {
                mOutputThread.quit();
                mOutputThread = null;
            }
            mOutputThread = new HandlerThread("FrameAvailableThread");
            mOutputThread.start();
            surfaceTexture.setOnFrameAvailableListener(texture -> {
                if (mFrameAvailableListener != null) {
                    mFrameAvailableListener.onFrameAvailable(texture);
                }
            }, new Handler(mOutputThread.getLooper()));
        } else {
            surfaceTexture.setOnFrameAvailableListener(texture -> {
                if (mFrameAvailableListener != null) {
                    mFrameAvailableListener.onFrameAvailable(texture);
                }
            });
        }
        return surfaceTexture;
    }

    /**
     * 释放资源
     */
    private void releaseSurfaceTexture() {
        if (mOutputTexture != null) {
            mOutputTexture.release();
            mOutputTexture = null;
        }
        if (mOutputThread != null) {
            mOutputThread.quitSafely();
            mOutputThread = null;
        }
    }

    @Override
    public void closeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.addCallbackBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        releaseSurfaceTexture();
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

    @Override
    public void switchCamera() {
        boolean front = !isFront();
        front = front && CameraApi.hasFrontCamera(mActivity);
        // 期望值不一致
        if (front != isFront()) {
            setFront(front);
            openCamera();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mPreviewCallback != null) {
            mPreviewCallback.onPreviewFrame(data);
        }
    }

    @Override
    public void setFront(boolean front) {
        if (front) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
    }

    @Override
    public boolean isFront() {
        return (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    @Override
    public int getOrientation() {
        return mOrientation;
    }

    @Override
    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    @Override
    public int getPreviewHeight() {
        return mPreviewHeight;
    }

    @Override
    public boolean canAutoFocus() {
        List<String> focusModes = mCamera.getParameters().getSupportedFocusModes();
        return (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO));
    }

    @Override
    public void setFocusArea(Rect rect) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters(); // 先获取当前相机的参数配置对象
            if (supportAutoFocusFeature(parameters)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO); // 设置聚焦模式
            }
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(rect, CameraParam.Weight));
                // 设置聚焦区域
                if (parameters.getMaxNumFocusAreas() > 0) {
                    parameters.setFocusAreas(focusAreas);
                }
                // 设置计量区域
                if (parameters.getMaxNumMeteringAreas() > 0) {
                    parameters.setMeteringAreas(focusAreas);
                }
                // 取消掉进程中所有的聚焦功能
                mCamera.setParameters(parameters);
                mCamera.autoFocus((success, camera) -> {
                    Camera.Parameters params = camera.getParameters();
                    // 设置自动对焦
                    if (supportAutoFocusFeature(params)) {
                        camera.cancelAutoFocus();
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }
                    camera.setParameters(params);
                    camera.autoFocus(null);
                });
            }
        }
    }

    @Override
    public Rect getFocusArea(float x, float y, int width, int height, int focusSize) {
        return calculateTapArea(x, y, width, height, focusSize, 1.0f);
    }

    @Override
    public boolean isSupportFlashLight(boolean front) {
        if (front) {
            return false;
        }
        return checkSupportFlashLight(mCamera);
    }

    @Override
    public void setFlashLight(boolean on) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (on) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCamera.setParameters(parameters);
        }
    }

    /**
     * 设置预览大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(),
                expectWidth, expectHeight, CalculateType.Lower);
        parameters.setPreviewSize(size.width, size.height);
        mPreviewWidth = size.width;
        mPreviewHeight = size.height;
        camera.setParameters(parameters);
    }

    /**
     * 设置拍摄的照片大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private void setPictureSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPictureSizes(),
                expectWidth, expectHeight, CalculateType.Max);
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     * @param activity
     */
    private int calculateCameraPreviewOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CameraParam.getInstance().cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 计算点击区域
     * @param x
     * @param y
     * @param width
     * @param height
     * @param focusSize
     * @param coefficient
     * @return
     */
    private static Rect calculateTapArea(float x, float y, int width, int height,
                                         int focusSize, float coefficient) {
        int areaSize = Float.valueOf(focusSize * coefficient).intValue();
        int left = clamp(Float.valueOf((y / height) * 2000 - 1000).intValue(), areaSize);
        int top = clamp(Float.valueOf(((height - x) / width) * 2000 - 1000).intValue(), areaSize);
        return new Rect(left, top, left + areaSize, top + areaSize);
    }

    /**
     * 确保所选区域在在合理范围内
     * @param touchCoordinateInCameraReper
     * @param focusAreaSize
     * @return
     */
    private static int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize  > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize ;
            } else {
                result = -1000 + focusAreaSize ;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    /**
     * 判断是否支持自动对焦
     * @param parameters
     * @return
     */
    private boolean supportAutoFocusFeature(@NonNull Camera.Parameters parameters) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            return true;
        }
        return false;
    }

    /**
     * 检查摄像头(前置/后置)是否支持闪光灯
     * @param camera   摄像头
     * @return
     */
    private static boolean checkSupportFlashLight(Camera camera) {
        if (camera == null) {
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();

        return checkSupportFlashLight(parameters);
    }

    /**
     * 检查摄像头(前置/后置)是否支持闪光灯
     * @param parameters 摄像头参数
     * @return
     */
    private static boolean checkSupportFlashLight(Camera.Parameters parameters) {
        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null
                || supportedFlashModes.isEmpty()
                || (supportedFlashModes.size() == 1
                && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF))) {
            return false;
        }

        return true;
    }

    /**
     * 选择合适的FPS
     * @param parameters
     * @param expectedThoudandFps 期望的FPS
     * @return
     */
    private static int chooseFixedPreviewFps(Camera.Parameters parameters, int expectedThoudandFps) {
        List<int[]> supportedFps = parameters.getSupportedPreviewFpsRange();
        for (int[] entry : supportedFps) {
            if (entry[0] == entry[1] && entry[0] == expectedThoudandFps) {
                parameters.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }
        int[] temp = new int[2];
        int guess;
        parameters.getPreviewFpsRange(temp);
        if (temp[0] == temp[1]) {
            guess = temp[0];
        } else {
            guess = temp[1] / 2;
        }
        return guess;
    }


    /**
     * 计算最完美的Size
     * @param sizes
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    private static Camera.Size calculatePerfectSize(List<Camera.Size> sizes, int expectWidth,
                                                    int expectHeight, CalculateType calculateType) {
        sortList(sizes); // 根据宽度进行排序

        // 根据当前期望的宽高判定
        List<Camera.Size> bigEnough = new ArrayList<>();
        List<Camera.Size> noBigEnough = new ArrayList<>();
        for (Camera.Size size : sizes) {
            if (size.height * expectWidth / expectHeight == size.width) {
                if (size.width > expectWidth && size.height > expectHeight) {
                    bigEnough.add(size);
                } else {
                    noBigEnough.add(size);
                }
            }
        }
        // 根据计算类型判断怎么如何计算尺寸
        Camera.Size perfectSize = null;
        switch (calculateType) {
            // 直接使用最小值
            case Min:
                // 不大于期望值的分辨率列表有可能为空或者只有一个的情况，
                // Collections.min会因越界报NoSuchElementException
                if (noBigEnough.size() > 1) {
                    perfectSize = Collections.min(noBigEnough, new CompareAreaSize());
                } else if (noBigEnough.size() == 1) {
                    perfectSize = noBigEnough.get(0);
                }
                break;

            // 直接使用最大值
            case Max:
                // 如果bigEnough只有一个元素，使用Collections.max就会因越界报NoSuchElementException
                // 因此，当只有一个元素时，直接使用该元素
                if (bigEnough.size() > 1) {
                    perfectSize = Collections.max(bigEnough, new CompareAreaSize());
                } else if (bigEnough.size() == 1) {
                    perfectSize = bigEnough.get(0);
                }
                break;

            // 小一点
            case Lower:
                // 优先查找比期望尺寸小一点的，否则找大一点的，接受范围在0.8左右
                if (noBigEnough.size() > 0) {
                    Camera.Size size = Collections.max(noBigEnough, new CompareAreaSize());
                    if (((float)size.width / expectWidth) >= 0.8
                            && ((float)size.height / expectHeight) > 0.8) {
                        perfectSize = size;
                    }
                } else if (bigEnough.size() > 0) {
                    Camera.Size size = Collections.min(bigEnough, new CompareAreaSize());
                    if (((float)expectWidth / size.width) >= 0.8
                            && ((float)(expectHeight / size.height)) >= 0.8) {
                        perfectSize = size;
                    }
                }
                break;

            // 大一点
            case Larger:
                // 优先查找比期望尺寸大一点的，否则找小一点的，接受范围在0.8左右
                if (bigEnough.size() > 0) {
                    Camera.Size size = Collections.min(bigEnough, new CompareAreaSize());
                    if (((float)expectWidth / size.width) >= 0.8
                            && ((float)(expectHeight / size.height)) >= 0.8) {
                        perfectSize = size;
                    }
                } else if (noBigEnough.size() > 0) {
                    Camera.Size size = Collections.max(noBigEnough, new CompareAreaSize());
                    if (((float)size.width / expectWidth) >= 0.8
                            && ((float)size.height / expectHeight) > 0.8) {
                        perfectSize = size;
                    }
                }
                break;
        }
        // 如果经过前面的步骤没找到合适的尺寸，则计算最接近expectWidth * expectHeight的值
        if (perfectSize == null) {
            Camera.Size result = sizes.get(0);
            boolean widthOrHeight = false; // 判断存在宽或高相等的Size
            // 辗转计算宽高最接近的值
            for (Camera.Size size : sizes) {
                // 如果宽高相等，则直接返回
                if (size.width == expectWidth && size.height == expectHeight
                        && ((float) size.height / (float) size.width) == CameraParam.getInstance().currentRatio) {
                    result = size;
                    break;
                }
                // 仅仅是宽度相等，计算高度最接近的size
                if (size.width == expectWidth) {
                    widthOrHeight = true;
                    if (Math.abs(result.height - expectHeight) > Math.abs(size.height - expectHeight)
                            && ((float) size.height / (float) size.width) == CameraParam.getInstance().currentRatio) {
                        result = size;
                        break;
                    }
                }
                // 高度相等，则计算宽度最接近的Size
                else if (size.height == expectHeight) {
                    widthOrHeight = true;
                    if (Math.abs(result.width - expectWidth) > Math.abs(size.width - expectWidth)
                            && ((float) size.height / (float) size.width) == CameraParam.getInstance().currentRatio) {
                        result = size;
                        break;
                    }
                }
                // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
                else if (!widthOrHeight) {
                    if (Math.abs(result.width - expectWidth) > Math.abs(size.width - expectWidth)
                            && Math.abs(result.height - expectHeight) > Math.abs(size.height - expectHeight)
                            && ((float) size.height / (float) size.width) == CameraParam.getInstance().currentRatio) {
                        result = size;
                    }
                }
            }
            perfectSize = result;
        }
        return perfectSize;
    }

    /**
     * 分辨率由大到小排序
     * @param list
     */
    private static void sortList(List<Camera.Size> list) {
        Collections.sort(list, new CompareAreaSize());
    }

    /**
     * 比较器
     */
    private static class CompareAreaSize implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size pre, Camera.Size after) {
            return Long.signum((long) pre.width * pre.height -
                    (long) after.width * after.height);
        }
    }
}
