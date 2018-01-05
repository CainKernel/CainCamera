package com.cgfay.caincamera.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.bean.CameraInfo;
import com.cgfay.caincamera.bean.Size;
import com.cgfay.caincamera.core.ParamsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by cain on 2017/7/9.
 */

public class CameraUtils {

    // 默认宽高不存在则重新计算比这个值稍微大一点的宽高
    // 16:9的默认宽高（理想值），相机的宽度和高度跟屏幕坐标不一样，手机屏幕的宽度和高度是反过来的。
    public static final int DEFAULT_16_9_WIDTH = 1280;
    public static final int DEFAULT_16_9_HEIGHT = 720;
    // 4:3的默认宽高(理想值)
    public static final int DEFAULT_4_3_WIDTH = 1024;
    public static final int DEFAULT_4_3_HEIGHT = 768;

    // 期望fps
    public static final int DESIRED_PREVIEW_FPS = 30;

    // 对焦区域的weight
    private static final int Weight = 1000;

    // 这里反过来是因为相机的分辨率跟屏幕的分辨率宽高刚好反过来
    public static final float Ratio_4_3 = 0.75f;
    public static final float Ratio_16_9 = 0.5625f;

    private static int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private static Camera mCamera;
    // 相机帧率
    private static int mCameraPreviewFps;
    // 相机预览角度
    private static int mOrientation = 0;

    // 当前的宽高比
    private static float mCurrentRatio = Ratio_16_9;

    // 当前摄像头是否支持闪光灯
    private static boolean mCurrentCameraSupportFlash = false;

    /**
     * 打开相机，默认打开前置相机
     * @param expectFps
     */
    public static void openCamera(int expectFps) {
        openCamera(mCameraID, expectFps);
    }

    /**
     * 根据ID打开相机
     * @param cameraID
     * @param expectFps
     */
    public static void openCamera(int cameraID, int expectFps) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        mCamera = Camera.open(cameraID);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        mCameraID = cameraID;
        Camera.Parameters parameters = mCamera.getParameters();
        mCurrentCameraSupportFlash = checkSupportFlashLight(parameters);
        mCameraPreviewFps = chooseFixedPreviewFps(parameters, expectFps * 1000);
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);
        int width = DEFAULT_16_9_WIDTH;
        int height = DEFAULT_16_9_HEIGHT;
        if (mCurrentRatio == Ratio_4_3) {
            width = DEFAULT_4_3_WIDTH;
            height = DEFAULT_4_3_HEIGHT;
        }
        setPreviewSize(mCamera, width, height);
        setPictureSize(mCamera, width, height);
        calculateCameraPreviewOrientation((Activity) ParamsManager.context);
        mCamera.setDisplayOrientation(mOrientation);
    }

    /**
     *  打开相机
     * @param cameraID
     * @param expectFps
     * @param expectWidth
     * @param expectHeight
     */
    public static void openCamera(int cameraID, int expectFps, int expectWidth, int expectHeight) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        mCamera = Camera.open(cameraID);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        mCameraID = cameraID;
        Camera.Parameters parameters = mCamera.getParameters();
        mCurrentCameraSupportFlash = checkSupportFlashLight(parameters);
        mCameraPreviewFps = chooseFixedPreviewFps(parameters, expectFps * 1000);
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);
        setPreviewSize(mCamera, expectWidth, expectHeight);
        setPictureSize(mCamera, expectWidth, expectHeight);
        mCamera.setDisplayOrientation(mOrientation);
    }

    /**
     * 开始预览
     * @param holder
     * 部分手机预览会失效，慎用
     */
    public static void startPreview(SurfaceHolder holder) {
        if (mCamera == null) {
            throw new IllegalStateException("Camera must be set when start preview");
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始预览
     * @param texture
     * 部分手机预览会失效，慎用
     */
    public static void startPreview(SurfaceTexture texture) {
        if (mCamera == null) {
            throw new IllegalStateException("Camera must be set when start preview");
        }
        // 先停止预览
        stopPreview();
        try {
            mCamera.setPreviewTexture(texture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置预览Surface
     * @param holder
     */
    public static void setPreviewSurface(SurfaceHolder holder) {
        if (mCamera == null) {
            throw new IllegalStateException("Camera must be set when start preview");
        }
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置预览Surface
     * @param texture
     */
    public static void setPreviewSurface(SurfaceTexture texture) {
        if (mCamera == null) {
            throw new IllegalStateException("Camera must be set when start preview");
        }
        try {
            mCamera.setPreviewTexture(texture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始预览
     */
    public static void startPreview() {
        if (mCamera == null) {
            throw new IllegalStateException("Camera must be set when start preview");
        }
        mCamera.startPreview();
    }

    /**
     * 切换相机
     * @param cameraID 相机Id
     * @param holder 绑定的SurfaceHolder
     * 备注：此时没有回调，部分手机预览会失效，慎用
     */
    public static void switchCamera(int cameraID, SurfaceHolder holder) {
        if (mCameraID == cameraID) {
            return;
        }
        mCameraID = cameraID;
        // 释放原来的相机
        releaseCamera();
        // 打开相机
        openCamera(cameraID, DESIRED_PREVIEW_FPS);
        // 打开预览
        startPreview(holder);
    }

    /**
     * 切换相机
     * @param cameraId 相机Id
     * @param texture 绑定的SurfaceTexture
     * 备注：此时没有回调，部分手机预览会失效，慎用
     */
    public static void switchCamera(int cameraId, SurfaceTexture texture) {
        if (mCameraID == cameraId) {
            return;
        }
        stopPreview();
        mCameraID = cameraId;
        releaseCamera();
        openCamera(cameraId, DESIRED_PREVIEW_FPS);
        startPreview(texture);
    }

    /**
     * 切换相机
     * @param cameraId
     * @param holder
     * @param callback
     * @param buffer
     */
    public static void switchCamera(int cameraId, SurfaceHolder holder,
                                    Camera.PreviewCallback callback, byte[] buffer) {
        if (mCameraID == cameraId) {
            return;
        }
        stopPreview();
        mCameraID = cameraId;
        releaseCamera();
        openCamera(cameraId, DESIRED_PREVIEW_FPS);
        setPreviewCallbackWithBuffer(callback, buffer);
        startPreview(holder);
    }

    /**
     * 切换相机
     * @param cameraId
     * @param texture
     * @param callback
     * @param buffer
     */
    public static void switchCamera(int cameraId, SurfaceTexture texture,
                                    Camera.PreviewCallback callback, byte[] buffer) {
        if (mCameraID == cameraId) {
            return;
        }
        stopPreview();
        mCameraID = cameraId;
        releaseCamera();
        openCamera(cameraId, DESIRED_PREVIEW_FPS);
        setPreviewCallbackWithBuffer(callback, buffer);
        startPreview(texture);
    }

    /**
     * 重新打开相机
     * @param holder
     * 备注：此时没有回调，部分手机预览会失效，慎用
     */
    public static void reopenCamera(SurfaceHolder holder) {
        releaseCamera();
        openCamera(mCameraID, DESIRED_PREVIEW_FPS);
        startPreview(holder);
    }

    /**
     * 重新打开相机
     * @param texture
     * 备注：此时没有回调，部分手机预览会失效，慎用
     */
    public static void reopenCamera(SurfaceTexture texture) {
        releaseCamera();
        openCamera(mCameraID, DESIRED_PREVIEW_FPS);
        startPreview(texture);
    }

    /**
     * 重新打开相机
     * @param holder
     * @param callback
     * @param buffer
     */
    public static void reopenCamera(SurfaceHolder holder,
                                    Camera.PreviewCallback callback, byte[] buffer) {
        releaseCamera();
        openCamera(mCameraID, DESIRED_PREVIEW_FPS);
        setPreviewSurface(holder);
        setPreviewCallbackWithBuffer(callback, buffer);
        startPreview();
    }

    /**
     * 重新打开相机
     * @param texture
     * @param callback
     * @param buffer
     */
    public static void reopenCamera(SurfaceTexture texture,
                                    Camera.PreviewCallback callback, byte[] buffer) {
        releaseCamera();
        openCamera(mCameraID, DESIRED_PREVIEW_FPS);
        setPreviewSurface(texture);
        setPreviewCallbackWithBuffer(callback, buffer);
        startPreview();
    }

    /**
     * 停止预览
     */
    public static void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * 释放相机
     */
    public static void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.addCallbackBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mCurrentCameraSupportFlash = false;
    }

    /**
     * 添加预览回调
     * @param callback
     * @param previewBuffer
     */
    public static void setPreviewCallbackWithBuffer(Camera.PreviewCallback callback, byte[] previewBuffer) {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(callback);
            mCamera.addCallbackBuffer(previewBuffer);
        }
    }

    /**
     * 添加预览回调
     * @param callback
     */
    public static void setPreviewCallback(Camera.PreviewCallback callback) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(callback);
        }
    }


    /**
     * 拍照
     */
    public static void takePicture(Camera.ShutterCallback shutterCallback,
                                   Camera.PictureCallback rawCallback,
                                   Camera.PictureCallback pictureCallback) {
        if (mCamera != null) {
            mCamera.takePicture(shutterCallback, rawCallback, pictureCallback);
        }
    }

    /**
     * 设置预览大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private static void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(),
                expectWidth, expectHeight);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    /**
     * 设置拍摄的照片大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private static void setPictureSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPictureSizes(),
                expectWidth, expectHeight);
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
    public static int calculateCameraPreviewOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
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
        mOrientation = result;
        return result;
    }

    /**
     * 设置对焦区域
     * @param rect      已经调整好的区域
     * @param callback  自动对焦回调
     */
    public static void setFocusArea(Rect rect, Camera.AutoFocusCallback callback) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters(); // 先获取当前相机的参数配置对象
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(rect, Weight));
                parameters.setFocusAreas(focusAreas);
                // 取消掉进程中所有的聚焦功能
                mCamera.cancelAutoFocus();
                mCamera.setParameters(parameters);
                mCamera.autoFocus(callback);
            }
        }
    }

    /**
     * 设置打开闪光灯
     * @param on
     */
    public static void setFlashLight(boolean on) {
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
     * 检查摄像头(前置/后置)是否支持闪光灯
     * @param camera   摄像头
     * @return
     */
    public static boolean checkSupportFlashLight(Camera camera) {
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
    public static boolean checkSupportFlashLight(Camera.Parameters parameters) {
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

    //-------------------------------- setter and getter start -------------------------------------

    /**
     * 获取相机对象
     * @return
     */
    public static Camera getCamera() {
        return mCamera;
    }

    /**
     * 获取当前的Camera ID
     * @return
     */
    public static int getCameraID() {
        return mCameraID;
    }

    /**
     * 获取照片大小
     * @return
     */
    public static Size getPictureSize() {
        if (mCamera != null) {
            Camera.Size size = mCamera.getParameters().getPictureSize();
            Size result = new Size(size.width, size.height);
            return result;
        }
        return new Size(0, 0);
    }

    /**
     * 获取预览大小
     * @return
     */
    public static Size getPreviewSize() {
        if (mCamera != null) {
            Camera.Size size = mCamera.getParameters().getPreviewSize();
            Size result = new Size(size.width, size.height);
            return result;
        }
        return new Size(0, 0);
    }

    /**
     * 获取相机信息
     * @return
     */
    public static CameraInfo getCameraInfo() {
        if (mCamera != null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraID, info);
            CameraInfo result = new CameraInfo(info.facing, info.orientation);
            return result;
        }
        return null;
    }

    /**
     * 获取当前预览的角度
     * @return
     */
    public static int getPreviewOrientation() {
        return mOrientation;
    }

    /**
     * 获取FPS（千秒值）
     * @return
     */
    public static int getCameraPreviewThousandFps() {
        return mCameraPreviewFps;
    }

    /**
     * 设置预览尺寸类型
     * @param ratio
     */
    public static void setCurrentRatio(float ratio) {
        mCurrentRatio = ratio;
    }

    /**
     * 获取当前的宽高比
     * @return
     */
    public static float getCurrentRatio() {
        return mCurrentRatio;
    }

    /**
     * 是否支持闪光灯
     * @return
     */
    public static boolean getSupportFlashLight() {
        return mCurrentCameraSupportFlash;
    }

    //---------------------------------- setter and getter end -------------------------------------

    /**
     * 计算最完美的Size
     * @param sizes
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    private static Camera.Size calculatePerfectSize(List<Camera.Size> sizes, int expectWidth,
                                                    int expectHeight) {
        sortList(sizes); // 根据宽度进行排序

        // 根据当前期望的宽高判定
        List<Camera.Size> bigEnough = new ArrayList<>();
        List<Camera.Size> noBigEnough = new ArrayList<>();
        for (Camera.Size size : sizes) {
            if (size.height * expectWidth / expectHeight == size.width) {
                if (size.width > expectWidth && size.height >= expectHeight) {
                    bigEnough.add(size);
                } else {
                    noBigEnough.add(size);
                }
            }
        }
        // 优先使用不大于期望值的预览尺寸，否则4:3的尺寸下，有些机器设置的尺寸是1440 x 1080，会比较卡
        if (noBigEnough.size() > 0) {
            return Collections.max(noBigEnough, new CompareAreaSize());
        } else if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareAreaSize());
        } else { // 如果不存在满足要求的数值，则辗转计算宽高最接近的值
            Camera.Size result = sizes.get(0);
            boolean widthOrHeight = false; // 判断存在宽或高相等的Size
            // 辗转计算宽高最接近的值
            for (Camera.Size size : sizes) {
                // 如果宽高相等，则直接返回
                if (size.width == expectWidth && size.height == expectHeight
                        && ((float) size.height / (float) size.width) == mCurrentRatio) {
                    result = size;
                    break;
                }
                // 仅仅是宽度相等，计算高度最接近的size
                if (size.width == expectWidth) {
                    widthOrHeight = true;
                    if (Math.abs(result.height - expectHeight)
                            > Math.abs(size.height - expectHeight)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                        break;
                    }
                }
                // 高度相等，则计算宽度最接近的Size
                else if (size.height == expectHeight) {
                    widthOrHeight = true;
                    if (Math.abs(result.width - expectWidth)
                            > Math.abs(size.width - expectWidth)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                        break;
                    }
                }
                // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
                else if (!widthOrHeight) {
                    if (Math.abs(result.width - expectWidth)
                            > Math.abs(size.width - expectWidth)
                            && Math.abs(result.height - expectHeight)
                            > Math.abs(size.height - expectHeight)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                    }
                }
            }
            return result;
        }
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
}
