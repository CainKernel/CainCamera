package com.cgfay.cainfilter.core;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import com.cgfay.cainfilter.type.GlFilterGroupType;
import com.cgfay.cainfilter.type.GlFilterType;

import java.lang.ref.WeakReference;

/**
 * 渲染Handler
 * Created by cain.huang on 2017/11/3.
 */

public class RenderHandler extends Handler {

    static final int MSG_SURFACE_CREATED = 0x001;
    static final int MSG_SURFACE_CHANGED = 0x002;
    static final int MSG_FRAME = 0x003;
    static final int MSG_FILTER_TYPE = 0x004;
    static final int MSG_RESET = 0x005;
    static final int MSG_SURFACE_DESTROYED = 0x006;
    static final int MSG_DESTROY = 0x008;

    static final int MSG_START_PREVIEW = 0x100;
    static final int MSG_STOP_PREVIEW = 0x101;
    static final int MSG_UPDATE_PREVIEW = 0x102;
    static final int MSG_UPDATE_PREVIEW_IMAGE_SIZE = 0x103;
    static final int MSG_SWITCH_CAMERA = 0x104;
    static final int MSG_PREVIEW_CALLBACK = 0x105;
    // 触摸区域
    static final int MSG_FOCUS_RECT = 0x106;
    // 重新打开相机
    static final int MSG_REOPEN_CAMERA = 0x107;
    // 闪光灯
    static final int MSG_SET_FLASHLIGHT = 0x108;

    // 录制状态
    static final int MSG_START_RECORDING = 0x200;
    static final int MSG_STOP_RECORDING = 0x201;
    static final int MSG_PAUSE_RECORDING = 0x202;
    static final int MSG_CONTINUE_RECORDING = 0x203;

    static final int MSG_RESET_BITRATE = 0x300;

    // 设置FpsHandler回调
    static final int MSG_SET_FPSHANDLER = 0x301;
    // 设置渲染状态变更监听器
    static final int MSG_SET_RENDER_STATE_CHANGED_LISTENER = 0x302;

    // 拍照
    static final int MSG_TAKE_PICTURE = 0x400;
    // 拍照回调
    static final int MSG_SET_CAPTURE_FRAME_CALLBACK = 0x401;

    // 切换滤镜组
    static final int MSG_FILTER_GROUP = 0x500;
    // 设置美颜等级
    static final int MSG_SET_BEAUTIFY_LEVEL = 0x501;

    private WeakReference<RenderThread> mWeakRender;


    public RenderHandler(Looper looper, RenderThread renderThread) {
        super(looper);
        mWeakRender = new WeakReference<RenderThread>(renderThread);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {

            // 销毁
            case MSG_DESTROY:

                break;

            // surfacecreated
            case MSG_SURFACE_CREATED:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().surfaceCreated((SurfaceHolder)msg.obj);
                }
                break;

            // surfaceChanged
            case MSG_SURFACE_CHANGED:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().surfaceChanged(msg.arg1, msg.arg2);
                }
                break;

            // surfaceDestroyed;
            case MSG_SURFACE_DESTROYED:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().surfaceDestoryed();
                }
                break;

            // 帧可用（考虑同步的问题）
            case MSG_FRAME:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().drawFrame();
                }
                break;

            // 设置美颜等级
            case MSG_SET_BEAUTIFY_LEVEL:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().setBeautifyLevel((Integer) msg.obj);
                }
                break;

            // 切换滤镜
            case MSG_FILTER_TYPE:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().changeFilter((GlFilterType) msg.obj);
                }
                break;

            // 切换滤镜组
            case MSG_FILTER_GROUP:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().changeFilterGroup((GlFilterGroupType) msg.obj);
                }
                break;

            // 重置
            case MSG_RESET:
                break;

            // 开始预览
            case MSG_START_PREVIEW:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().startPreview();
                }
                break;

            // 停止预览
            case MSG_STOP_PREVIEW:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().stopPreview();
                }
                break;

            // 更新预览视图大小
            case MSG_UPDATE_PREVIEW:

                break;

            // 更新预览图片的大小
            case MSG_UPDATE_PREVIEW_IMAGE_SIZE:

                break;

            // 触摸区域
            case MSG_FOCUS_RECT:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().setFocusAres((Rect) msg.obj);
                }
                break;

            // 设置是否打开闪光灯
            case MSG_SET_FLASHLIGHT:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().setFlashLight((Boolean) msg.obj);
                }
                break;

            // 重新打开相机
            case MSG_REOPEN_CAMERA:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().reopenCamera();
                }
                break;

            // 切换相机操作
            case MSG_SWITCH_CAMERA:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().switchCamera();
                }
                break;

            // PreviewCallback回调预览
            case MSG_PREVIEW_CALLBACK:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().onPreviewCallback((byte[])msg.obj);
                }
                break;

            // 开始录制
            case MSG_START_RECORDING:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().startRecording();
                }
                break;

            // 暂停录制
            case MSG_PAUSE_RECORDING:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().pauseRecording();
                }
                break;

            // 继续录制
            case MSG_CONTINUE_RECORDING:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().continueRecording();
                }
                break;

            // 停止录制
            case MSG_STOP_RECORDING:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().stopRecording();
                }
                break;

            // 重置bitrate(录制视频时使用)
            case MSG_RESET_BITRATE:
                break;

            // 设置Fps的handler回调
            case MSG_SET_FPSHANDLER:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().setFpsHandler((Handler) msg.obj);
                }
                break;
            // 设置渲染状态变更回调
            case MSG_SET_RENDER_STATE_CHANGED_LISTENER:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get()
                            .setRenderStateChangedListener((RenderStateChangedListener) msg.obj);
                }
                break;

            // 拍照
            case MSG_TAKE_PICTURE:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().takePicture();
                }
                break;

            // 设置拍照回调
            case MSG_SET_CAPTURE_FRAME_CALLBACK:
                if (mWeakRender != null && mWeakRender.get() != null) {
                    mWeakRender.get().setCaptureFrameCallback((CaptureFrameCallback) msg.obj);
                }
                break;

            default:
                throw new IllegalStateException("Can not handle message what is: " + msg.what);
        }
    }
}
