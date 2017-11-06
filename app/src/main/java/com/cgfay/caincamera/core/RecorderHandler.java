package com.cgfay.caincamera.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 视频录制handler
 * Created by cain on 2017/11/5.
 */

public class RecorderHandler extends Handler {

    static final int MSG_RECORDING_SIZE = 0x01;

    static final int MSG_DESTROY = 0x02;

    static final int MSG_INIT_FILTER = 0x03;

    static final int MSG_FRAME_RENDER = 0x10;

    private WeakReference<RecorderThread> mWeakRecorder;

    public RecorderHandler(Looper looper, RecorderThread thread) {
        super(looper);
        mWeakRecorder = new WeakReference<RecorderThread>(thread);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            // 设置录制的大小
            case MSG_RECORDING_SIZE:
                if (mWeakRecorder != null && mWeakRecorder.get() != null) {
                    mWeakRecorder.get().setRecordingSize(msg.arg1, msg.arg2);
                }
                break;

            // 初始化滤镜
            case MSG_INIT_FILTER:
                if (mWeakRecorder != null && mWeakRecorder.get() != null) {
                    mWeakRecorder.get().initFilter();
                }
                break;

            // 录制渲染
            case MSG_FRAME_RENDER:
                if (mWeakRecorder != null && mWeakRecorder.get() != null) {
                    mWeakRecorder.get().renderFrame();
                }
                break;

            // 销毁
            case MSG_DESTROY:
                if (mWeakRecorder != null && mWeakRecorder.get() != null) {
                    mWeakRecorder.get().release();
                }
                break;
        }
    }
}
