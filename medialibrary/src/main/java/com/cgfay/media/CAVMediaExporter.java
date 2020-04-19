package com.cgfay.media;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

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
    private native void _release();
    private native void _export();
    private native void _cancel();
    private native boolean _isExporting();

    @AccessedByNative
    private long mNativeContext;
    private EventHandler mEventHandler;

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
    }

    public void release() {
        mOnStartedListener = null;
        mOnCompletionListener = null;
        mOnErrorListener = null;
        mOnCurrentPositionListener = null;
        _release();
    }

    /**
     * 设置输入文件路径
     */
    public void setDataSource(String path) {

    }

    /**
     * 设置输出文件路径的
     */
    public void setOutputPath(String path) {

    }

    /**
     * 设置输出的音频数据
     */
    public void setAudioParams(int sampleRate, int channels, int sampleFormat) {

    }

    /**
     * 设置视频输出参数
     * @param width
     * @param height
     * @param frameRate
     */
    public void setVideoParams(int width, int height, int frameRate) {

    }

    /**
     * 设置处理区间
     */
    public void setRange(float start, float end) {

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
    private static final int EXPORT_STARTED = 1;    // 导出开始回调
    private static final int EXPORT_COMPLETE = 2;   // 导出完成回调
    private static final int EXPORT_CANCEL = 3;     // 导出取消回调
    private static final int EXPORT_ERROR = 100;    // 导出出错回调
    private static final int EXPORT_CURRENT = 200;  // 导致进度回调

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
                case EXPORT_STARTED: {
                    if (mOnStartedListener != null) {
                        mOnStartedListener.onStarted(mCAVMediaExporter);
                    }
                    break;
                }

                case EXPORT_COMPLETE: {
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mCAVMediaExporter);
                    }
                    break;
                }

                case EXPORT_CANCEL: {
                    Log.d(TAG, "media editor is canceled!");
                    break;
                }

                case EXPORT_ERROR: {
                    Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                    boolean error_was_handled = false;
                    if (mOnErrorListener != null) {
                        error_was_handled = mOnErrorListener.onError(mCAVMediaExporter, msg.arg1, msg.arg2);
                    }
                    if (mOnCompletionListener != null && !error_was_handled) {
                        mOnCompletionListener.onCompletion(mCAVMediaExporter);
                    }
                    break;
                }

                case EXPORT_CURRENT: {
                    if (mOnCurrentPositionListener != null) {
                        mOnCurrentPositionListener.onCurrentPosition(mCAVMediaExporter, msg.arg1, msg.arg2);
                    }
                    break;
                }

                default:{
                    Log.e(TAG, "Unknown message type " + msg.what);
                    break;
                }
            }
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
     * Interface definition for a callback to be invoked when the media
     * source is already started for edit.
     */
    public interface OnStartedListener {

        /**
         * Called when the media file is already started for edit.
         *
         * @param editor the CAVMediaExporter that is already started for edit
         */
        void onStarted(CAVMediaExporter editor);
    }

    /**
     * Register a callback to be invoked when the media source is already started
     * for edit.
     *
     * @param listener the callback that will be run
     */
    public void setOnStartedListener(OnStartedListener listener) {
        mOnStartedListener = listener;
    }

    private OnStartedListener mOnStartedListener;

    /**
     * Interface definition for a callback to be invoked when edit of
     * a media source has completed.
     */
    public interface OnCompletionListener {

        /**
         * Called when the end of a media source is reached during edit.
         *
         * @param editor the CAVMediaExporter that reached the end of the file
         */
        void onCompletion(CAVMediaExporter editor);
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during edit.
     *
     * @param listener the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /* Do not change these values without updating their counterparts
     * in CAVMediaExporter.h!
     */
    /**
     * Unspecified media player error.
     */
    public static final int EDITOR_ERROR_UNKNOWN = 1;

    /** Media server died. In this case, the application must release the
     * MediaPlayer object and instantiate a new one.
     */
    public static final int EDITOR_ERROR_SERVER_DIED = 100;

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    public interface OnErrorListener {

        /**
         * Called to indicate an error.
         *
         * @param editor  the CAVMediaExporter the error pertains to
         * @param what    the type of error that has occurred:
         * <ul>
         * <li>{@link #EDITOR_ERROR_UNKNOWN}
         * <li>{@link #EDITOR_ERROR_SERVER_DIED}
         * </ul>
         * @param extra an extra code, specific to the error. Typically
         * implementation dependant.
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the OnCompletionListener to be called.
         */
        boolean onError(CAVMediaExporter editor, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an error has happened
     * during an asynchronous operation.
     *
     * @param listener the callback that will be run
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    private OnErrorListener mOnErrorListener;

    /**
     * Interface definition of a callback to be invoked to editing position.
     */
    public interface OnCurrentPositionListener {

        void onCurrentPosition(CAVMediaExporter editor, float current, int duration);
    }

    /**
     * Register a callback to be invoked on editing position.
     * @param listener
     */
    public void setOnCurrentPositionlistener(OnCurrentPositionListener listener) {
        mOnCurrentPositionListener = listener;
    }

    private OnCurrentPositionListener mOnCurrentPositionListener;

}
