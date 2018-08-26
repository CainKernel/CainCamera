package com.cgfay.cameralibrary.engine.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cgfay.cameralibrary.engine.model.CalculateType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 相机引擎
 * 备注：这里遇到一个问题 —— 单例对象会导致某些手机的帧率大幅度降低
 * 没测试出来是怎么回事
 * Created by cain on 2017/7/9.
 */

public class CameraEngine {

    // 相机对象
    private static Camera mCamera;

    public static void openCamera(Context context) {
        openCamera(context, CameraParam.DESIRED_PREVIEW_FPS);
    }

    /**
     * 根据ID打开相机
     * @param expectFps
     */
    public static void openCamera(Context context, int expectFps) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        CameraParam cameraParam = CameraParam.getInstance();
        mCamera = Camera.open(cameraParam.cameraId);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        Camera.Parameters parameters = mCamera.getParameters();
        cameraParam.supportFlash = checkSupportFlashLight(parameters);
        cameraParam.previewFps = chooseFixedPreviewFps(parameters, expectFps * 1000);
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);
        int width = CameraParam.DEFAULT_16_9_WIDTH;
        int height = CameraParam.DEFAULT_16_9_HEIGHT;
        if (cameraParam.currentRatio == CameraParam.Ratio_4_3) {
            width = CameraParam.DEFAULT_4_3_WIDTH;
            height = CameraParam.DEFAULT_4_3_HEIGHT;
        }
        setPreviewSize(mCamera, width, height);
        setPictureSize(mCamera, width, height);
        calculateCameraPreviewOrientation((Activity) context);
        mCamera.setDisplayOrientation(cameraParam.orientation);
    }

    /**
     * 打开相机
     * @param context
     * @param cameraID
     * @param expectFps
     * @param expectWidth
     * @param expectHeight
     */
    public static void openCamera(Context context, int cameraID, int expectFps, int expectWidth, int expectHeight) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        mCamera = Camera.open(cameraID);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        CameraParam cameraParam = CameraParam.getInstance();
        cameraParam.cameraId = cameraID;
        Camera.Parameters parameters = mCamera.getParameters();
        cameraParam.supportFlash = checkSupportFlashLight(parameters);
        cameraParam.previewFps = chooseFixedPreviewFps(parameters, expectFps * 1000);
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);
        setPreviewSize(mCamera, expectWidth, expectHeight);
        setPictureSize(mCamera, expectWidth, expectHeight);
        calculateCameraPreviewOrientation((Activity) context);
        mCamera.setDisplayOrientation(cameraParam.orientation);
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
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.addCallbackBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        CameraParam.getInstance().supportFlash = false;
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
    public static void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(),
                expectWidth, expectHeight, CalculateType.Lower);
        parameters.setPreviewSize(size.width, size.height);
        CameraParam.getInstance().previewWidth = size.width;
        CameraParam.getInstance().previewHeight = size.height;
        camera.setParameters(parameters);
    }

    /**
     * 设置预览大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    public static void setPreviewSize(Camera camera, int expectWidth, int expectHeight, float ratio) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(),
                expectWidth, expectHeight, CalculateType.Lower);
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
    public static int calculateCameraPreviewOrientation(Activity activity) {
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
        CameraParam.getInstance().orientation = result;
        return result;
    }

    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     * @param activity
     */
    public static int calculateCameraPreviewOrientation(Activity activity, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
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
        CameraParam.getInstance().orientation = result;
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
                focusAreas.add(new Camera.Area(rect, CameraParam.Weight));
                parameters.setFocusAreas(focusAreas);
                // 取消掉进程中所有的聚焦功能
                mCamera.cancelAutoFocus();
                mCamera.setParameters(parameters);
                mCamera.autoFocus(callback);
            }
        }
    }

    /**
     * 设置对焦
     * @param rect
     */
    public static void setFocusArea(Rect rect) {
        if (mCamera != null) {
            final String focusMode = mCamera.getParameters().getFocusMode();
            Camera.Parameters parameters = mCamera.getParameters(); // 先获取当前相机的参数配置对象
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
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
                mCamera.cancelAutoFocus();
                mCamera.setParameters(parameters);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Camera.Parameters parame = camera.getParameters();
                        parame.setFocusMode(focusMode);
                        camera.setParameters(parame);
                    }
                });
            }
        }
    }

    /**
     * 计算触摸区域
     * @param x
     * @param y
     * @return
     */
    public static Rect getFocusArea(float x, float y, int width, int height, int focusSize) {
        return calculateTapArea(x, y, width, height, focusSize, 1.0f);
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

    /**
     * 获取相机对象
     * @return
     */
    public static Camera getCamera() {
        return mCamera;
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

    /**
     * 选择合适的FPS
     * @param parameters
     * @param expectedThoudandFps 期望的FPS
     * @return
     */
    public static int chooseFixedPreviewFps(Camera.Parameters parameters, int expectedThoudandFps) {
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
