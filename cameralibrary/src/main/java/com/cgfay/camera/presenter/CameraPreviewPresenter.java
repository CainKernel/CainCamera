package com.cgfay.camera.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import com.cgfay.camera.activity.CameraSettingActivity;
import com.cgfay.camera.engine.camera.CameraParam;
import com.cgfay.camera.engine.listener.OnCameraCallback;
import com.cgfay.camera.engine.listener.OnCaptureListener;
import com.cgfay.camera.engine.listener.OnFpsListener;
import com.cgfay.camera.engine.listener.OnRecordListener;
import com.cgfay.camera.engine.model.GalleryType;
import com.cgfay.camera.engine.recorder.PreviewRecorder;
import com.cgfay.camera.engine.render.PreviewRenderer;
import com.cgfay.camera.fragment.CameraPreviewFragment;
import com.cgfay.camera.utils.PathConstraints;
import com.cgfay.facedetect.engine.FaceTracker;
import com.cgfay.facedetect.listener.FaceTrackerCallback;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.ResourceHelper;
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.filter.glfilter.resource.bean.ResourceType;
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.landmark.LandmarkEngine;
import com.cgfay.uitls.utils.BitmapUtils;
import com.cgfay.uitls.utils.BrightnessUtils;
import com.cgfay.uitls.utils.PermissionUtils;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * 预览的presenter
 * @author CainHuang
 * @date 2019/7/3
 */
public class CameraPreviewPresenter extends PreviewPresenter<CameraPreviewFragment>
        implements OnRecordListener, OnCameraCallback, FaceTrackerCallback, OnCaptureListener, OnFpsListener {

    private static final String TAG = "CameraPreviewPresenter";

    // 当前索引
    private int mFilterIndex = 0;

    // 预览参数
    private CameraParam mCameraParam;

    private Activity mActivity;

    public CameraPreviewPresenter(CameraPreviewFragment target) {
        super(target);
        mCameraParam = CameraParam.getInstance();
    }

    public void onAttach(Activity activity) {
        mActivity = activity;
        int currentMode = BrightnessUtils.getSystemBrightnessMode(mActivity);
        if (currentMode == 1) {
            mCameraParam.brightness = -1;
        } else {
            mCameraParam.brightness = BrightnessUtils.getSystemBrightness(mActivity);
        }
        mCameraParam.audioPermitted = PermissionUtils.permissionChecking(mActivity, Manifest.permission.RECORD_AUDIO);

        // 初始化相机渲染引擎
        PreviewRenderer.getInstance()
                .setCameraCallback(this)
                .setCaptureFrameCallback(this)
                .setFpsCallback(this)
                .initRenderer(mActivity);

        // 初始化检测器
        FaceTracker.getInstance()
                .setFaceCallback(this)
                .previewTrack(true)
                .initTracker();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 销毁人脸检测器
        FaceTracker.getInstance().destroyTracker();
        // 关闭渲染引擎
        PreviewRenderer.getInstance().destroyRenderer();
        // 清理关键点
        LandmarkEngine.getInstance().clearAll();
    }

    public void onDetach() {
        mActivity = null;
    }

    /**
     * 绑定Surface
     * @param surface
     */
    public void bindSurface(Surface surface) {
        PreviewRenderer.getInstance().bindSurface(surface);
    }

    /**
     * 绑定SurfaceTexture
     * @param surfaceTexture
     */
    public void bindSurface(SurfaceTexture surfaceTexture) {
        PreviewRenderer.getInstance().bindSurface(surfaceTexture);
    }

    /**
     * 改变预览尺寸
     * @param width
     * @param height
     */
    public void changePreviewSize(int width, int height) {
        PreviewRenderer.getInstance().changePreviewSize(width, height);
    }

    /**
     * 解绑Surface
     */
    public void unBindSurface() {
        PreviewRenderer.getInstance().unbindSurface();
    }

    /**
     * 相机打开回调
     */
    @Override
    public void onCameraOpened() {
        if (getTarget() != null) {
            getTarget().enableShutter(true);
        }
        FaceTracker.getInstance()
                .setBackCamera(mCameraParam.backCamera)
                .prepareFaceTracker(mActivity, mCameraParam.orientation,
                        mCameraParam.previewWidth, mCameraParam.previewHeight);
    }

    /**
     * 相机预览回调
     * @param data
     */
    @Override
    public void onPreviewCallback(byte[] data) {
        // 人脸检测
        FaceTracker.getInstance().trackFace(data,
                mCameraParam.previewWidth, mCameraParam.previewHeight);
    }

    /**
     * 人脸检测完成回调
     */
    @Override
    public void onTrackingFinish() {
        PreviewRenderer.getInstance().requestRender();
    }

    /**
     * 截屏回调
     * @param buffer
     * @param width
     * @param height
     */
    @Override
    public void onCapture(final ByteBuffer buffer, final int width, final int height) {
        String filePath = PathConstraints.getImageCachePath(mActivity);
        BitmapUtils.saveBitmap(filePath, buffer, width, height);
        if (mCameraParam.captureListener != null) {
            mCameraParam.captureListener.onMediaSelectedListener(filePath, GalleryType.PICTURE);
        }
    }

    /**
     * fps数值回调
     * @param fps
     */
    @Override
    public void onFpsCallback(float fps) {
        if (getTarget() != null) {
            getTarget().showFps(fps);
        }
    }

    /**
     * 录制开始
     */
    @Override
    public void onRecordStarted() {
        if (getTarget() != null) {
            getTarget().setShutterEnableEncoder(true);
        }
    }

    /**
     * 录制发生变化
     * @param duration
     */
    @Override
    public void onRecordProgressChanged(final long duration) {
        if (getTarget() != null) {
            getTarget().updateRecordProgress(duration);
        }
    }

    /**
     * 录制结束
     */
    @Override
    public void onRecordFinish() {
        if (getTarget() != null) {
            getTarget().updateRecordFinish();
        }
    }

    /**
     * 开始录制
     * @param width
     * @param height
     * @param enableAudio
     */
    public void startRecord(int width, int height, boolean enableAudio) {
        PreviewRecorder.getInstance()
                .setRecordType(mCameraParam.mGalleryType == GalleryType.VIDEO ? PreviewRecorder.RecordType.Video
                        : PreviewRecorder.RecordType.Gif)
                .setOutputPath(PathConstraints.getVideoCachePath(mActivity))
                .enableAudio(enableAudio)
                .setRecordSize(width, height)
                .setOnRecordListener(this)
                .startRecord();
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        PreviewRecorder.getInstance().stopRecord();
    }

    /**
     * 停止录制
     * @param stopTimer 是否停止倒计时
     */
    public void stopRecord(boolean stopTimer) {
        PreviewRecorder.getInstance().stopRecord(stopTimer);
    }

    /**
     * 取消录制
     */
    public void cancelRecord() {
        PreviewRecorder.getInstance().cancelRecord();
    }

    /**
     * 销毁录制器
     */
    public void destroyRecorder() {
        PreviewRecorder.getInstance().destroyRecorder();
    }

    /**
     * 是否正处于录制过程
     * @return
     */
    public boolean isRecording() {
        return PreviewRecorder.getInstance().isRecording();
    }

    /**
     * 移除所有分段视频
     */
    public void removeAllSubVideo() {
        PreviewRecorder.getInstance().removeAllSubVideo();
        PreviewRecorder.getInstance().deleteRecordDuration();
    }

    /**
     * 移除上一段视频
     */
    public void removeLastSubVideo() {
        PreviewRecorder.getInstance().removeLastSubVideo();
        PreviewRecorder.getInstance().deleteRecordDuration();
    }

    /**
     * 获取分段视频数量
     * @return
     */
    public int getNumberOfSubVideo() {
       return PreviewRecorder.getInstance().getNumberOfSubVideo();
    }

    /**
     * 获视频显示的时长
     * @return
     */
    public long getVideoVisibleDuration() {
        return PreviewRecorder.getInstance().getVisibleDuration();
    }

    /**
     * 获取时间字符串
     * @return
     */
    public String getVideoVisibleTimeString() {
        return PreviewRecorder.getInstance().getVisibleDurationString();
    }

    /**
     * 是否在最后一秒停止
     * @return
     */
    public boolean isLastSecondStop() {
        return PreviewRecorder.getInstance().isLastSecondStop();
    }

    /**
     * 获取录制
     * @return
     */
    public long getMaxRecordMilliSeconds() {
        return PreviewRecorder.getInstance().getMaxMilliSeconds();
    }

    /**
     * 打开相册
     */
    public void onOpenGalleryPage() {
        if (mCameraParam.gallerySelectedListener != null) {
            mCameraParam.gallerySelectedListener.onGalleryClickListener(GalleryType.WITHOUT_GIF);
        }
    }

    /**
     * 打开视频编辑页面
     * @param path
     */
    public void onOpenVideoEditPage(String path) {
        if (mCameraParam.captureListener != null) {
            mCameraParam.captureListener.onMediaSelectedListener(path, GalleryType.VIDEO);
        }
    }

    /**
     * 打开相机设置页面
     */
    public void onOpenCameraSettingPage() {
        if (mActivity != null) {
            Intent intent = new Intent(mActivity, CameraSettingActivity.class);
            mActivity.startActivity(intent);
        }
    }

    /**
     * 打开音乐选择页面
     */
    public void onOpenMusicSelectPage() {

    }

    /**
     * 切换相机
     */
    public void switchCamera() {
        PreviewRenderer.getInstance().switchCamera();
    }

    /**
     * 拍照
     */
    public void takePicture() {
        PreviewRenderer.getInstance().takePicture();
    }

    /**
     * 是否允许比较
     * @param enable
     */
    public void showCompare(boolean enable) {
        PreviewRenderer.getInstance().enableCompare(enable);
    }

    /**
     * 获取当前的滤镜索引
     * @return
     */
    public int getFilterIndex() {
        return mFilterIndex;
    }

    /**
     * 上一个滤镜
     * @return
     */
    public int lastFilter() {
        mFilterIndex--;
        if (mFilterIndex < 0) {
            int count = FilterHelper.getFilterList().size();
            mFilterIndex = count > 0 ? count - 1 : 0;
        }
        changeDynamicFilter(mFilterIndex);
        return mFilterIndex;
    }

    /**
     * 下一个滤镜
     */
    public int nextFilter() {
        mFilterIndex++;
        mFilterIndex = mFilterIndex % FilterHelper.getFilterList().size();
        changeDynamicFilter(mFilterIndex);
        return mFilterIndex;
    }

    /**
     * 设置倍速
     * @param speed
     */
    public void setSpeed(float speed) {

    }

    /**
     * 是否允许边框模糊
     * @param enable
     */
    public void enableEdgeBlurFilter(boolean enable) {
        PreviewRenderer.getInstance().changeEdgeBlurFilter(enable);
    }

    /**
     * 切换滤镜
     * @param filterIndex
     */
    public void changeDynamicFilter(int filterIndex) {
        if (mActivity == null) {
            return;
        }
        String folderPath = FilterHelper.getFilterDirectory(mActivity) + File.separator +
                FilterHelper.getFilterList().get(filterIndex).unzipFolder;
        DynamicColor color = null;
        if (!FilterHelper.getFilterList().get(filterIndex).unzipFolder.equalsIgnoreCase("none")) {
            try {
                color = ResourceJsonCodec.decodeFilterData(folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PreviewRenderer.getInstance().changeDynamicFilter(color);
    }

    /**
     * 切换滤镜
     * @param color
     */
    public void changeDynamicFilter(DynamicColor color) {
        PreviewRenderer.getInstance().changeDynamicFilter(color);
    }

    /**
     * 切换彩妆
     * @param makeup
     */
    public void changeDynamicMakeup(DynamicMakeup makeup) {
        PreviewRenderer.getInstance().changeDynamicMakeup(makeup);
    }

    /**
     * 解码资源
     * @param resourceData 资源数据
     */
    public void changeResource(@NonNull ResourceData resourceData) {
        ResourceType type = resourceData.type;
        String unzipFolder = resourceData.unzipFolder;
        if (type == null) {
            return;
        }
        try {
            switch (type) {
                // 单纯的滤镜
                case FILTER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                    PreviewRenderer.getInstance().changeDynamicResource(color);
                    break;
                }

                // 贴纸
                case STICKER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicSticker sticker = ResourceJsonCodec.decodeStickerData(folderPath);
                    PreviewRenderer.getInstance().changeDynamicResource(sticker);
                    break;
                }

                // TODO 多种结果混合
                case MULTI: {
                    break;
                }

                // 所有数据均为空
                case NONE: {
                    PreviewRenderer.getInstance().changeDynamicResource((DynamicSticker) null);
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "parseResource: ", e);
        }
    }

}
