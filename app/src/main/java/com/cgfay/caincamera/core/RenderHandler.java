package com.cgfay.caincamera.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.type.FilterGroupType;
import com.cgfay.caincamera.type.FilterType;

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
    static final int MSG_INIT = 0x007;
    static final int MSG_DESTROY = 0x008;

    static final int MSG_START_PREVIEW = 0x100;
    static final int MSG_STOP_PREVIEW = 0x101;
    static final int MSG_UPDATE_PREVIEW = 0x102;
    static final int MSG_UPDATE_PREVIEW_IMAGE_SIZE = 0x103;
    static final int MSG_SWITCH_CAMERA = 0x104;
    static final int MSG_PREVIEW_CALLBACK = 0x105;

    static final int MSG_START_RECORDING = 0x200;
    static final int MSG_STOP_RECORDING = 0x201;

    static final int MSG_RESET_BITRATE = 0x300;

    static final int MSG_TAKE_PICTURE = 0x400;

    // 切换滤镜组
    static final int MSG_FILTER_GROUP = 0x500;

    private WeakReference<DrawerManager> mWeakDrawer;


    public RenderHandler(Looper looper, DrawerManager drawer) {
        super(looper);
        mWeakDrawer = new WeakReference<DrawerManager>(drawer);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {

            // 初始化
            case MSG_INIT:
                break;

            // 销毁
            case MSG_DESTROY:

                break;

            // surfacecreated
            case MSG_SURFACE_CREATED:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().onSurfaceCreated((SurfaceHolder)msg.obj);
                }
                break;

            // surfaceChanged
            case MSG_SURFACE_CHANGED:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().onSurfaceChanged(msg.arg1, msg.arg2);
                }
                break;

            // surfaceDestroyed;
            case MSG_SURFACE_DESTROYED:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().onSurfaceDestoryed();
                }
                break;

            // 帧可用（考虑同步的问题）
            case MSG_FRAME:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalDrawFrame();
                }
                break;

            // 切换滤镜
            case MSG_FILTER_TYPE:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalChangeFilter((FilterType) msg.obj);
                }
                break;

            // 切换滤镜组
            case MSG_FILTER_GROUP:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalChangeFilterGroup((FilterGroupType) msg.obj);
                }
                break;

            // 重置
            case MSG_RESET:
                break;

            // 开始预览
            case MSG_START_PREVIEW:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalStartPreview();
                }
                break;

            // 停止预览
            case MSG_STOP_PREVIEW:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalStopPreview();
                }
                break;

            // 更新预览视图大小
            case MSG_UPDATE_PREVIEW:

                break;

            // 更新预览图片的大小
            case MSG_UPDATE_PREVIEW_IMAGE_SIZE:

                break;

            // 切换相机操作
            case MSG_SWITCH_CAMERA:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalSwitchCamera();
                }
                break;

            // PreviewCallback回调预览
            case MSG_PREVIEW_CALLBACK:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalPreviewCallback((byte[])msg.obj);
                }
                break;

            // 开始录制
            case MSG_START_RECORDING:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalStartRecording();
                }
                break;

            // 停止录制
            case MSG_STOP_RECORDING:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalStopRecording();
                }
                break;

            // 重置bitrate(录制视频时使用)
            case MSG_RESET_BITRATE:
                break;

            // 拍照
            case MSG_TAKE_PICTURE:
                if (mWeakDrawer != null && mWeakDrawer.get() != null) {
                    mWeakDrawer.get().internalTakePicture();
                }
                break;

            default:
                throw new IllegalStateException("Can not handle message what is: " + msg.what);
        }
    }
}
