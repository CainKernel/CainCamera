package com.cgfay.camera.presenter;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.media.recorder.SpeedMode;

/**
 * 预览画面的presenter
 * @author CainHuang
 * @date 2019/7/3
 */
public abstract class PreviewPresenter<T extends Fragment> extends IPresenter<T> {

    PreviewPresenter(T target) {
        super(target);
    }

    /**
     * 绑定SharedContext
     * @param context SharedContext
     */
    public abstract void onBindSharedContext(EGLContext context);

    /**
     * 录制帧可用
     * @param texture
     * @param timestamp
     */
    public abstract void onRecordFrameAvailable(int texture, long timestamp);

    /**
     * SurfaceTexture 创建
     * @param surfaceTexture
     */
    public abstract void onSurfaceCreated(SurfaceTexture surfaceTexture);

    /**
     * SurfaceTexture 发生变化
     * @param width
     * @param height
     */
    public abstract void onSurfaceChanged(int width, int height);

    /**
     * SurfaceTexture 销毁
     */
    public abstract void onSurfaceDestroyed();

    /**
     * 切换道具资源
     * @param resourceData
     */
    public abstract void changeResource(@NonNull ResourceData resourceData);

    /**
     * 切换滤镜
     * @param color
     */
    public abstract void changeDynamicFilter(DynamicColor color);

    /**
     * 切换彩妆
     * @param makeup
     */
    public abstract void changeDynamicMakeup(DynamicMakeup makeup);

    /**
     * 切换滤镜
     * @param filterIndex
     */
    public abstract void changeDynamicFilter(int filterIndex);

    /**
     * 前一个滤镜
     */
    public abstract int previewFilter();

    /**
     * 下一个滤镜
     */
    public abstract int nextFilter();

    /**
     * 获取当前的滤镜索引
     * @return
     */
    public abstract int getFilterIndex();

    /**
     * 是否允许比较
     * @param enable
     */
    public abstract void showCompare(boolean enable);

    /**
     * 拍照
     */
    public abstract void takePicture();

    /**
     * 切换相机
     */
    public abstract void switchCamera();

    /**
     * 开始录制
     */
    public abstract void startRecord();

    /**
     * 停止录制
     */
    public abstract void stopRecord();

    /**
     * 取消录制
     */
    public abstract void cancelRecord();

    /**
     * 是否正处于录制过程
     * @return true：正在录制，false：非录制状态
     */
    public abstract boolean isRecording();

    /**
     * 设置是否允许录制音频
     * @param enable
     */
    public abstract void setRecordAudioEnable(boolean enable);

    /**
     * 设置录制时长
     * @param seconds 录制视频时长(秒)
     */
    public abstract void setRecordSeconds(int seconds);

    /**
     * 设置速度模式
     * @param mode 设置录制的速度模式
     */
    public abstract void setSpeedMode(SpeedMode mode);

    /**
     * 删除上一段视频
     */
    public abstract void deleteLastVideo();

    /**
     * 获取录制的视频段数
     * @return 录制的视频段数
     */
    public abstract int getRecordedVideoSize();

    /**
     * 是否打开闪光灯
     * @param on    打开闪光灯
     */
    public abstract void changeFlashLight(boolean on);

    /**
     * 是否允许边框模糊
     * @param enable true:允许边框模糊
     */
    public abstract void enableEdgeBlurFilter(boolean enable);

    /**
     * 选择音乐
     * @param path
     */
    public abstract void setMusicPath(String path);

    /**
     * 打开相机设置
     */
    public abstract void onOpenCameraSettingPage();
}
