package com.cgfay.media;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.avfoundation.AVComposition;
import com.cgfay.avfoundation.AVTime;
import com.cgfay.media.annotations.AccessedByNative;

import java.lang.ref.WeakReference;

/**
 * 媒体导出器
 */
public class CAVMediaExporter {

    private static final String TAG = "CAVMediaExporter";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("yuv");
        System.loadLibrary("media_exporter");
        native_init();
    }

    private static native void native_init();

    private native void native_setup(Object editor_this);
    private native void native_finalize();
    private native void _setMediaComposition(AVComposition composition);
    private native void _setOutputPath(String path);
    private native void _release();
    private native void _export();
    private native void _cancel();
    private native boolean _isExporting();

    @AccessedByNative
    private long mNativeContext;
    private EventHandler mEventHandler;

    // 源媒体数据组合对象
    private AVComposition mMediaComposition;

    // 时长
    private AVTime mDuration;

    // 输出路径
    private String mOutputPath;

    public CAVMediaExporter() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        /* Native setup requires a weak reference to our object.
         * It's easier to create it here than in C++.
         */
        native_setup(new WeakReference<CAVMediaExporter>(this));
        mMediaComposition = null;
    }

    /**
     * 释放资源
     */
    public void release() {
        mListener = null;
        _release();
    }

    /**
     * 设置视频组合对象的
     * @param composition 视频组合兑现
     */
    public void setMediaComposition(@NonNull AVComposition composition) {
        mMediaComposition = composition;
        _setMediaComposition(composition);
    }

    /**
     * 设置输出文件路径的
     */
    public void setOutputPath(String path) {
        _setOutputPath(path);
    }

    /**
     * 开始导出
     */
    public void export() throws IllegalStateException {
        _export();
    }

    /**
     * 取消
     */
    public void cancel() throws IllegalStateException {
        _cancel();
    }

    /**
     * 是否处于编辑状态
     */
    public boolean isExporting() throws IllegalStateException {
        return _isExporting();
    }

    @Override
    protected void finalize() throws Throwable {
        native_finalize();
        super.finalize();
    }

    /* Do not change these values without updating their counterparts
     * in CAVMediaExporter.h!
     */
    private static final int EXPORT_PREPARED = 1;   // 导出准备回调
    private static final int EXPORT_COMPLETE = 2;   // 导出完成回调
    private static final int EXPORT_CANCEL = 3;     // 导出取消回调
    private static final int EXPORT_CURRENT = 4;    // 导致进度回调
    private static final int EXPORT_ERROR = 100;    // 导出出错回调

    /**
     * 事件handler
     */
    private class EventHandler extends Handler {

        private final CAVMediaExporter mCAVMediaExporter;

        public EventHandler(CAVMediaExporter editor, Looper looper) {
            super(looper);
            mCAVMediaExporter = editor;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {

            if (mCAVMediaExporter.mNativeContext == 0) {
                Log.w(TAG, "media editor went away with unhandled events");
                return;
            }

            switch (msg.what) {
                case EXPORT_PREPARED: {
                    Log.d(TAG, "onExportStart ");
                    mDuration = new AVTime(msg.arg1, msg.arg2);
                    break;
                }

                case EXPORT_COMPLETE: {
                    if (mListener != null) {
                        mListener.onExportSuccess();
                    }
                    break;
                }

                case EXPORT_CANCEL: {
                    Log.d(TAG, "media editor is canceled!");
                    break;
                }

                case EXPORT_ERROR: {
                    Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                    if (mListener != null) {
                        mListener.onExportError(getErrorMessage(msg.arg1, msg.arg2));
                    }
                    break;
                }

                case EXPORT_CURRENT: {
                    if (mListener != null) {
                        mListener.onExporting(new AVTime(msg.arg1, msg.arg2), mDuration);
                    }
                    break;
                }

                default: {
                    Log.e(TAG, "Unknown message type " + msg.what);
                    break;
                }
            }
        }

        /**
         * 获取出错信息回调
         * @param errorCode     出错码
         * @param errorSubCode  出错子码
         * @return 出错信息
         */
        @Nullable
        private String getErrorMessage(int errorCode, int errorSubCode) {
            return null;
        }
    }

    /**
     * Called from native code when an interesting event happens.  This method
     * just uses the EventHandler system to post the event back to the main app thread.
     * We use a weak reference to the original CAVMediaExporter object so that the native
     * code is safe from the object disappearing from underneath it.  (This is
     * the cookie passed to native_setup().)
     */
    private static void postEventFromNative(Object mediaplayer_ref,
                                            int what, int arg1, int arg2, Object obj) {
        final CAVMediaExporter editor = (CAVMediaExporter)((WeakReference) mediaplayer_ref).get();
        if (editor == null) {
            return;
        }

        if (editor.mEventHandler != null) {
            Message m = editor.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            editor.mEventHandler.sendMessage(m);
        }
    }

    /**
     * 导出回调
     */
    public interface OnExportListener {

        /**
         * 导出过程回调
         * @param current 当前的导出时间
         * @param duration 导出时长
         */
        void onExporting(AVTime current, AVTime duration);

        /**
         * 导出成功回调
         */
        void onExportSuccess();

        /**
         * 出错回调
         * @param error 出错信息
         */
        void onExportError(@Nullable String error);
    }

    /**
     * 设置导出监听器
     * @param listener 监听器
     */
    public void setOnExportListener(@Nullable OnExportListener listener) {
        mListener = listener;
    }

    private OnExportListener mListener;
}
