package com.cgfay.media;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.cgfay.media.annotations.AccessedByNative;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * 多媒体合成器，用于将短视频文件、逆序视频文件、背景音乐文件重新合成新的视频文件
 */
public class CainMediaSynthesizer {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("soundtouch");
        System.loadLibrary("media_synthesizer");
        native_init();
    }

    private static final String TAG = "CainMediaSynthesizer";

    @AccessedByNative
    private long mNativeContext;
    private EventHandler mEventHandler;

    public CainMediaSynthesizer() {
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
        native_setup(new WeakReference<CainMediaSynthesizer>(this));
    }

    /**
     * Update the MediaSynthesizer SurfaceTexture.
     * Call after setting a new display surface.
     */
    public void setVideoSurface(Surface surface) {
        _setVideoSurface(surface);
    }

    private native void _setVideoSurface(Surface surface);

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     *
     * @param path the path of the file, or the http/rtsp URL of the stream you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        _setDataSource(path);
    }

    private native void _setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * Sets the reverse data source to use.
     * @param path
     */
    public void setReverseDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        _setReverseDataSource(path);
    }

    private native void _setReverseDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * Sets the background data source to use.
     * @param path
     */
    public void setBackgroundDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        _setBackgroundDataSource(path);
    }

    private native void _setBackgroundDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * Prepares the synthesizer for synthesize a new media, synchronously.
     *
     * After setting the datasource, you need to either call prepare() or prepareAsync().
     * For files, it is OK to call prepare(),
     * which blocks until MediaSynthesizer is ready for synthesize.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    public void prepare() throws IOException, IllegalStateException {
        _prepare();
    }

    private native void _prepare() throws IOException, IllegalStateException;

    /**
     * Starts or resumes synthesize
     * @throws IllegalStateException
     */
    public void start() throws IllegalStateException {
        _start();
    }

    private native void _start() throws IllegalStateException;

    /**
     * Stops synthesize after synthesize has been stopped or paused
     * @throws IllegalStateException
     */
    public void stop() throws IllegalStateException {
        _stop();
    }

    private native void _stop() throws IllegalStateException;

    /**
     * Gets the current synthesize position.
     *
     * @return the current position in milliseconds
     */
    public long getCurrentPosition() {
        return _getCurrentPosition();
    }

    private native long _getCurrentPosition();

    /**
     * Gets the duration of the file.
     *
     * @return the duration in milliseconds
     */
    public long getDuration() {
        return _getDuration();
    }

    private native long _getDuration();


    /**
     * Sets the filter type for video.
     */
    public void setFilterType(int type) {
        _setFilterType(type);
    }

    private native void _setFilterType(int type);


    public void release() {
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnErrorListener = null;
        mOnProcessUpdateListener = null;
        _release();
    }

    private native void _release();

    /**
     * Resets the MediaSynthesizer to its uninitialized state.
     */
    public void reset() {
        _reset();
    }

    private native void _reset();

    /**
     * Sets the media and background data source volume percent to synthesize.
     * @param foregroundPercent
     * @param backgroundPercent
     */
    public void setVolume(float foregroundPercent, float backgroundPercent) {
        _setVolume(foregroundPercent, backgroundPercent);
    }

    private native void _setVolume(float foregroundPercent, float backgroundPercent);


    private static native void native_init();
    private native void native_setup(Object synthesizer_this);
    private native void native_finalize();

    @Override
    protected void finalize() throws Throwable {
        native_finalize();
        super.finalize();
    }

    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_SYNTHESIZE_COMPLETE = 2;
    private static final int MEDIA_PROCESSING = 3;
    private static final int MEDIA_ERROR = 100;

    private class EventHandler extends Handler {

        private CainMediaSynthesizer mMediaSynthesizer;

        public EventHandler(CainMediaSynthesizer ms, Looper looper) {
            super(looper);
            mMediaSynthesizer = ms;
        }

        @Override
        public void handleMessage(Message msg) {

            if (mMediaSynthesizer.mNativeContext == 0) {
                Log.w(TAG, "mediaplayer went away with unhandled events");
                return;
            }

            switch (msg.what) {

                case MEDIA_PREPARED: {
                    if (mOnPreparedListener != null) {
                        mOnPreparedListener.onPrepared(mMediaSynthesizer);
                    }
                    return;
                }

                case MEDIA_SYNTHESIZE_COMPLETE: {
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaSynthesizer);
                    }
                    return;
                }

                case MEDIA_PROCESSING: {
                    if (mOnProcessUpdateListener != null) {
                        mOnProcessUpdateListener.onProcessing(mMediaSynthesizer, msg.arg1, msg.arg2);
                    }
                    return;
                }

                case MEDIA_ERROR: {
                    Log.e(TAG, "Error ( " + msg.arg1 + "," + msg.arg2 + ")");
                    boolean error_was_handled = false;
                    if (mOnErrorListener != null) {
                        error_was_handled = mOnErrorListener.onError(mMediaSynthesizer, msg.arg1, msg.arg2);
                    }
                    if (mOnCompletionListener != null && !error_was_handled) {
                        mOnCompletionListener.onCompletion(mMediaSynthesizer);
                    }
                    return;
                }

                case MEDIA_NOP: {
                    break;
                }
                
                default: {
                    Log.e(TAG, "Unknown message type " + msg.what);
                    return;
                }
            }
        }
    }

    /**
     * Called from native code when an interesting event happens.  This method
     * just uses the EventHandler system to post the event back to the main app thread.
     * We use a weak reference to the original MediaSynthesizer object so that the native
     * code is safe from the object disappearing from underneath it.  (This is
     * the cookie passed to native_setup().)
     */
    private static void postEventFromNative(Object synthesizer_ref,
                                            int what, int arg1, int arg2, Object obj) {
        final CainMediaSynthesizer synthesizer = (CainMediaSynthesizer)((WeakReference) synthesizer_ref).get();
        if (synthesizer == null) {
            return;
        }

        if (synthesizer.mEventHandler != null) {
            Message m = synthesizer.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            synthesizer.mEventHandler.sendMessage(m);
        }
    }

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for synthesize.
     */
    public interface OnPreparedListener {
        /**
         * Called when the media file is ready for synthesize.
         *
         * @param ms the MediaSynthesizer that is ready for synthesize
         */
        void onPrepared(CainMediaSynthesizer ms);
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    private OnPreparedListener mOnPreparedListener;

    /**
     * Interface definition for a callback to be invoked when synthesize of
     * a media source has completed.
     */
    public interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during synthesize.
         *
         * @param ms the MediaSynthesizer that is ready for synthesize
         */
        void onCompletion(CainMediaSynthesizer ms);
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during synthesize.
     *
     * @param listener the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    public interface OnErrorListener {
        /**
         * Called to indicate an error.
         *
         * @param ms      the MediaSynthesizer the error pertains to
         * @param what    the type of error that has occurred:
         * <ul>
         * </ul>
         * @param extra an extra code, specific to the error. Typically
         * implementation dependant.
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the OnCompletionListener to be called.
         */
        boolean onError(CainMediaSynthesizer ms, int what, int extra);
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
     * Interface definition for a callback to be invoked when synthesize of
     * a media source has processing.
     */
    public interface OnProcessUpdateListener {

        /**
         * Called when processing synthesize.
         * @param ms                the MediaSynthesizer that is ready for synthesize
         * @param currentPosition   the current position is processing.
         * @param duration          the duration for a media file.
         */
        void onProcessing(CainMediaSynthesizer ms, long currentPosition, long duration);
    }

    /**
     * Register a callback to be invoked when a media source has been processing synthesize.
     * @param listener
     */
    public void setOnProcessUpdateListener(OnProcessUpdateListener listener) {
        mOnProcessUpdateListener = listener;
    }

    private OnProcessUpdateListener mOnProcessUpdateListener;
}
