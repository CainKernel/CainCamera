package com.cgfay.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.cgfay.media.annotations.AccessedByNative;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * 视频播放器
 */
public class VideoPlayer implements IMediaPlayer {

    private static final String TAG = "VideoPlayer";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("yuv");
        System.loadLibrary("videoplayer");
        native_init();
    }

    private static native void native_init();
    // 初始化
    private native void native_setup(Object mediaplayer_this);
    private native void native_finalize();
    // 释放资源
    private native void _release();
    // 重置
    private native void _reset();
    // 设置播放路径
    private native void _setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;
    // 设置播放路径
    private native void _setDataSource(
            String path, String[] keys, String[] values)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;
    // 设置播放文件
    private native void _setDataSource(FileDescriptor fd, long offset, long length)
            throws IOException, IllegalArgumentException, IllegalStateException;
    // 设置音频解码器名称
    private native void _setAudioDecoder(String decoder);
    // 设置视频解码器名称
    private native void _setVideoDecoder(String decoder);
    // 设置Surface
    private native void _setVideoSurface(Surface surface);
    // 设置播放速度
    private native void _setSpeed(float speed);
    // 设置是否循环播放
    private native void _setLooping(boolean looping);
    // 设置播放区间
    private native void _setRange(float start, float end);
    // 设置播放声音
    private native void _setVolume(float leftVolume, float rightVolume);
    // 设置是否静音
    private native void _setMute(boolean mute);
    // 准备
    private native void _prepare();
    // 开始播放
    private native void _start();
    // 暂停播放
    private native void _pause();
    // 继续播放
    private native void _resume();
    // 停止播放
    private native void _stop();
    // 设置处于暂停状态下，是否可以解码，用于连续seek场景使用
    private native void _setDecodeOnPause(boolean decodeOnPause);
    // 定位
    private native void _seekTo(float timeMs);
    // 获取当前播放时长
    private native long _getCurrentPosition();
    // 获取时长
    private native long _getDuration();
    // 获取旋转角度
    private native int _getRotate();
    // 获取视频宽度
    private native int _getVideoWidth();
    // 获取视频高度
    private native int _getVideoHeight();
    // 是否循环播放
    private native boolean _isLooping();
    // 是否正在播放中
    private native boolean _isPlaying();

    @AccessedByNative
    private long mNativeContext;
    private SurfaceHolder mSurfaceHolder;
    private EventHandler mEventHandler;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;

    /**
     * Default constructor. Consider using one of the create() methods for
     * synchronously instantiating a MediaPlayer from a Uri or resource.
     * <p>When done with the MediaPlayer, you should call  {@link #release()},
     * to free the resources. If not released, too many MediaPlayer instances may
     * result in an exception.</p>
     */
    public VideoPlayer() {
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
        native_setup(new WeakReference<VideoPlayer>(this));
    }

    /**
     * Sets the {@link SurfaceHolder} to use for displaying the video
     * portion of the media.
     *
     * Either a surface holder or surface must be set if a display or video sink
     * is needed.  Not calling this method or {@link #setSurface(Surface)}
     * when playing back a video will result in only the audio track being played.
     * A null surface holder or surface will result in only the audio track being
     * played.
     *
     * @param sh the SurfaceHolder to use for video display
     */
    public void setDisplay(SurfaceHolder sh) {
        mSurfaceHolder = sh;
        Surface surface;
        if (sh != null) {
            surface = sh.getSurface();
        } else {
            surface = null;
        }
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    /**
     * Sets the {@link Surface} to be used as the sink for the video portion of
     * the media. This is similar to {@link #setDisplay(SurfaceHolder)}, but
     * does not support {@link #setScreenOnWhilePlaying(boolean)}.  Setting a
     * Surface will un-set any Surface or SurfaceHolder that was previously set.
     * A null surface will result in only the audio track being played.
     *
     * If the Surface sends frames to a {@link SurfaceTexture}, the timestamps
     * returned from {@link SurfaceTexture#getTimestamp()} will have an
     * unspecified zero point.  These timestamps cannot be directly compared
     * between different media sources, different instances of the same media
     * source, or multiple runs of the same program.  The timestamp is normally
     * monotonically increasing and is unaffected by time-of-day adjustments,
     * but it is reset when the position is set.
     *
     * @param surface The {@link Surface} to be used for the video portion of
     * the media.
     */
    @Override
    public void setSurface(Surface surface) {
        if (mScreenOnWhilePlaying && surface != null) {
            Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        mSurfaceHolder = null;
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    /**
     * Convenience method to create a MediaPlayer for a given Uri.
     * On success, {@link #prepare()} will already have been called and must not be called again.
     * <p>When done with the MediaPlayer, you should call  {@link #release()},
     * to free the resources. If not released, too many MediaPlayer instances will
     * result in an exception.</p>
     *
     * @param context the Context to use
     * @param uri the Uri from which to get the datasource
     * @return a MediaPlayer object, or null if creation failed
     */
    public static VideoPlayer create(Context context, Uri uri) {
        return create (context, uri, null);
    }

    /**
     * Convenience method to create a MediaPlayer for a given Uri.
     * On success, {@link #prepare()} will already have been called and must not be called again.
     * <p>When done with the MediaPlayer, you should call  {@link #release()},
     * to free the resources. If not released, too many MediaPlayer instances will
     * result in an exception.</p>
     *
     * @param context the Context to use
     * @param uri the Uri from which to get the datasource
     * @param holder the SurfaceHolder to use for displaying the video
     * @return a MediaPlayer object, or null if creation failed
     */
    public static VideoPlayer create(Context context, Uri uri, SurfaceHolder holder) {

        try {
            VideoPlayer mp = new VideoPlayer();
            mp.setDataSource(context, uri);
            if (holder != null) {
                mp.setDisplay(holder);
            }
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        } catch (SecurityException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        }

        return null;
    }

    // Note no convenience method to create a MediaPlayer with SurfaceTexture sink.

    /**
     * Convenience method to create a MediaPlayer for a given resource id.
     * On success, {@link #prepare()} will already have been called and must not be called again.
     * <p>When done with the MediaPlayer, you should call  {@link #release()},
     * to free the resources. If not released, too many MediaPlayer instances will
     * result in an exception.</p>
     *
     * @param context the Context to use
     * @param resid the raw resource id (<var>R.raw.&lt;something></var>) for
     *              the resource to use as the datasource
     * @return a MediaPlayer object, or null if creation failed
     */
    public static VideoPlayer create(Context context, int resid) {
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null) return null;

            VideoPlayer mp = new VideoPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        } catch (SecurityException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        }
        return null;
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri the Content URI of the data you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, null);
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri the Content URI of the data you want to play
     * @param headers the headers to be sent together with the request for the data
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {

        String scheme = uri.getScheme();
        if(scheme == null || scheme.equals("file")) {
            setDataSource(uri.getPath());
            return;
        }

        AssetFileDescriptor fd = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            fd = resolver.openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return;
            }
            // Note: using getDeclaredLength so that our behavior is the same
            // as previous versions when the content provider is returning
            // a full file.
            if (fd.getDeclaredLength() < 0) {
                setDataSource(fd.getFileDescriptor());
            } else {
                setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
            }
            return;
        } catch (SecurityException ex) {

        } catch (IOException ex) {

        } finally {
            if (fd != null) {
                fd.close();
            }
        }

        Log.d(TAG, "Couldn't open file on client side, trying server side");
        setDataSource(uri.toString(), headers);
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     *
     * @param path the path of the file, or the http/rtsp URL of the stream you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    public void setDataSource(@NonNull String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        _setDataSource(path);
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     *
     * @param path the path of the file, or the http/rtsp URL of the stream you want to play
     * @param headers the headers associated with the http request for the stream you want to play
     * @throws IllegalStateException if it is called in an invalid state
     * @hide pending API council
     */
    @Override
    public void setDataSource(@NonNull String path, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String[] keys = null;
        String[] values = null;

        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];

            int i = 0;
            for (Map.Entry<String, String> entry: headers.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                ++i;
            }
        }
        _setDataSource(path, keys, values);
    }

    /**
     * Sets the data source (FileDescriptor) to use. It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd the FileDescriptor for the file you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Override
    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        // intentionally less than LONG_MAX
        setDataSource(fd, 0, 0x7ffffffffffffffL);
    }

    /**
     * Sets the data source (FileDescriptor) to use.  The FileDescriptor must be
     * seekable (N.B. a LocalSocket is not seekable). It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd the FileDescriptor for the file you want to play
     * @param offset the offset into the file where the data to be played starts, in bytes
     * @param length the length in bytes of the data to be played
     * @throws IllegalStateException if it is called in an invalid state
     */
    public void setDataSource(FileDescriptor fd, long offset, long length)
            throws IOException, IllegalArgumentException, IllegalStateException {
        _setDataSource(fd, offset, length);
    }

    /**
     * Prepares the player for playback, synchronously.
     *
     * After setting the datasource and the display surface, you need to either
     * call prepare() or prepareAsync(). For files, it is OK to call prepare(),
     * which blocks until MediaPlayer is ready for playback.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Override
    public void prepare() throws IllegalStateException {
        _prepare();
    }

    /**
     * Prepares the player for playback, asynchronously.
     *
     * After setting the datasource and the display surface, you need to either
     * call prepare() or prepareAsync(). For streams, you should call prepareAsync(),
     * which returns immediately, rather than blocking until enough data has been
     * buffered.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Override
    public void prepareAsync() throws IllegalStateException {
        _prepare();
    }

    /**
     * Starts or resumes playback. If playback had previously been paused,
     * playback will continue from where it was paused. If playback had
     * been stopped, or never started before, playback will start at the
     * beginning.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Override
    public void start() throws IllegalStateException {
        stayAwake(true);
        _start();
    }

    /**
     * Stops playback after playback has been stopped or paused.
     *
     * @throws IllegalStateException if the internal player engine has not been
     * initialized.
     */
    @Override
    public void stop() throws IllegalStateException {
        stayAwake(false);
        _stop();
    }

    /**
     * Pauses playback. Call pause() to pause.
     *
     * @throws IllegalStateException if the internal player engine has not been
     * initialized.
     */
    @Override
    public void pause() throws IllegalStateException {
        stayAwake(false);
        _pause();
    }

    /**
     * Pauses playback. Call resume() to resume.
     *
     * @throws IllegalStateException if the internal player engine has not been
     * initialized.
     */
    @Override
    public void resume() throws IllegalStateException {
        stayAwake(true);
        _resume();
    }

    /**
     * Set the low-level power management behavior for this MediaPlayer.  This
     * can be used when the MediaPlayer is not playing through a SurfaceHolder
     * set with {@link #setDisplay(SurfaceHolder)} and thus can use the
     * high-level {@link #setScreenOnWhilePlaying(boolean)} feature.
     *
     * <p>This function has the MediaPlayer access the low-level power manager
     * service to control the device's power usage while playing is occurring.
     * The parameter is a combination of {@link android.os.PowerManager} wake flags.
     * Use of this method requires {@link android.Manifest.permission#WAKE_LOCK}
     * permission.
     * By default, no attempt is made to keep the device awake during playback.
     *
     * @param context the Context to use
     * @param mode    the power/wake mode to set
     * @see android.os.PowerManager
     */
    @Override
    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode| PowerManager.ON_AFTER_RELEASE, VideoPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }

    /**
     * Control whether we should use the attached SurfaceHolder to keep the
     * screen on while video playback is occurring.  This is the preferred
     * method over {@link #setWakeMode} where possible, since it doesn't
     * require that the application have permission for low-level wake lock
     * access.
     *
     * @param screenOn Supply true to keep the screen on, false to allow it
     * to turn off.
     */
    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }
    }

    /**
     * Returns the rotate of the video.
     *
     * @return the rotate of the video, or o if there is no video
     */
    @Override
    public int getRotate() {
        return _getRotate();
    }

    /**
     * Returns the width of the video.
     *
     * @return the width of the video, or 0 if there is no video,
     * no display surface was set, or the width has not been determined
     * yet. The OnVideoSizeChangedListener can be registered via
     * {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)}
     * to provide a notification when the width is available.
     */
    @Override
    public int getVideoWidth() {
        return _getVideoWidth();
    }

    /**
     * Returns the height of the video.
     *
     * @return the height of the video, or 0 if there is no video,
     * no display surface was set, or the height has not been determined
     * yet. The OnVideoSizeChangedListener can be registered via
     * {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)}
     * to provide a notification when the height is available.
     */
    @Override
    public int getVideoHeight() {
        return _getVideoHeight();
    }

    /**
     * Checks whether the MediaPlayer is playing.
     *
     * @return true if currently playing, false otherwise
     */
    @Override
    public boolean isPlaying() {
        return _isPlaying();
    }

    /**
     * Seeks to specified time position.
     *
     * @param msec the offset in milliseconds from the start to seek to
     * @throws IllegalStateException if the internal player engine has not been
     * initialized
     */
    @Override
    public void seekTo(float msec) throws IllegalStateException {
        _seekTo(msec);
    }

    /**
     * Gets the current playback position.
     *
     * @return the current position in milliseconds
     */
    @Override
    public long getCurrentPosition() {
        return _getCurrentPosition();
    }

    /**
     * Gets the duration of the file.
     *
     * @return the duration in milliseconds
     */
    @Override
    public long getDuration() {
        return _getDuration();
    }

    /**
     * Releases resources associated with this MediaPlayer object.
     * It is considered good practice to call this method when you're
     * done using the MediaPlayer. In particular, whenever an Activity
     * of an application is paused (its onPause() method is called),
     * or stopped (its onStop() method is called), this method should be
     * invoked to release the MediaPlayer object, unless the application
     * has a special need to keep the object around. In addition to
     * unnecessary resources (such as memory and instances of codecs)
     * being held, failure to call this method immediately if a
     * MediaPlayer object is no longer needed may also lead to
     * continuous battery consumption for mobile devices, and playback
     * failure for other applications if no multiple instances of the
     * same codec are supported on a device. Even if multiple instances
     * of the same codec are supported, some performance degradation
     * may be expected when unnecessary multiple instances are used
     * at the same time.
     */
    @Override
    public void release() {
        stayAwake(false);
        updateSurfaceScreenOn();
        mOnPreparedListener = null;
        mOnBufferingUpdateListener = null;
        mOnCompletionListener = null;
        mOnSeekCompleteListener = null;
        mOnErrorListener = null;
        mOnInfoListener = null;
        mOnVideoSizeChangedListener = null;
        mOnCurrentPositionListener = null;
        mOnVideoPositionListener = null;
        _release();
    }

    /**
     * Resets the MediaPlayer to its uninitialized state. After calling
     * this method, you will have to initialize it again by setting the
     * data source and calling prepare().
     */
    @Override
    public void reset() {
        stayAwake(false);
        _reset();
        // make sure none of the listeners get called anymore
        mEventHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Sets the audio stream type for this MediaPlayer. See {@link AudioManager}
     * for a list of stream types. Must call this method before prepare() or
     * prepareAsync() in order for the target stream type to become effective
     * thereafter.
     *
     * @param streamtype the audio stream type
     * @see android.media.AudioManager
     */
    @Override
    public void setAudioStreamType(int streamtype) {
        // do nothing
    }

    /**
     * Sets the player to be looping or non-looping.
     *
     * @param looping whether to loop or not
     */
    @Override
    public void setLooping(boolean looping) {
        _setLooping(looping);
    }

    /**
     * Checks whether the MediaPlayer is looping or non-looping.
     *
     * @return true if the MediaPlayer is currently looping, false otherwise
     */
    @Override
    public boolean isLooping() {
        return _isLooping();
    }

    /**
     * Sets the volume on this player.
     * This API is recommended for balancing the output of audio streams
     * within an application. Unless you are writing an application to
     * control user settings, this API should be used in preference to
     * {@link AudioManager#setStreamVolume(int, int, int)} which sets the volume of ALL streams of
     * a particular type. Note that the passed volume values are raw scalars.
     * UI controls should be scaled logarithmically.
     *
     * @param leftVolume left volume scalar
     * @param rightVolume right volume scalar
     */
    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        _setVolume(leftVolume, rightVolume);
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId the audio session ID.
     * The audio session ID is a system wide unique identifier for the audio stream played by
     * this MediaPlayer instance.
     * The primary use of the audio session ID  is to associate audio effects to a particular
     * instance of MediaPlayer: if an audio session ID is provided when creating an audio effect,
     * this effect will be applied only to the audio content of media players within the same
     * audio session and not to the output mix.
     * When created, a MediaPlayer instance automatically generates its own audio session ID.
     * However, it is possible to force this player to be part of an already existing audio session
     * by calling this method.
     * This method must be called before one of the overloaded <code> setDataSource </code> methods.
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Override
    public void setAudioSessionId(int sessionId)  throws IllegalArgumentException, IllegalStateException {
        // do nothing
        mSessionId = sessionId;
    }

    private int mSessionId;
    /**
     * Returns the audio session ID.
     *
     * @return the audio session ID. {@see #setAudioSessionId(int)}
     * Note that the audio session ID is 0 only if a problem occured when the MediaPlayer was contructed.
     */
    @Override
    public int getAudioSessionId() {
        // do nothing
        return mSessionId;
    }

    /**
     * Sets mute state on this player.
     * @param mute
     */
    @Override
    public void setMute(boolean mute) {
        _setMute(mute);
    }

    /**
     * Sets audio decoder on this player
     * @param decoder decoder name
     */
    public void setAudioDecoder(String decoder) {
        _setAudioDecoder(decoder);
    }

    /**
     * Sets video decoder on this player. if you want to decode video using hardware decoder，
     * please set "h264_mediacodec" to enable hardware decoding
     * @param decoder decoder name
     */
    public void setVideoDecoder(String decoder) {
        _setVideoDecoder(decoder);
    }

    /**
     * Sets playback speed on this player.
     * @param speed
     */
    public void setSpeed(float speed) {
        _setSpeed(speed);
    }

    /**
     * Sets playback range on this player.
     * @param startMs
     * @param endMs
     */
    public void setRange(float startMs, float endMs) {
        _setRange(startMs, endMs);
    }

    /**
     * Sets can decode on pause state on this player.
     * @param decodeOnPause
     */
    public void setDecodeOnPause(boolean decodeOnPause) {
        _setDecodeOnPause(decodeOnPause);
    }

    @Override
    protected void finalize() throws Throwable {
        native_finalize();
        super.finalize();
    }

    /* Do not change these values without updating their counterparts
     * in AVMediaPlayer.h!
     */
    private static final int MEDIA_NOP = 0; // interface test message
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_STARTED = 2;
    private static final int MEDIA_PLAYBACK_COMPLETE = 3;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_BUFFERING_UPDATE = 5;
    private static final int MEDIA_SET_VIDEO_SIZE = 6;
    private static final int MEDIA_ERROR = 100;
    private static final int MEDIA_INFO = 200;
    private static final int MEDIA_CURRENT = 300;
    private static final int MEDIA_VIDEO_CURRENT = 301;

    private class EventHandler extends Handler {
        private VideoPlayer mVideoPlayer;

        public EventHandler(VideoPlayer mp, Looper looper) {
            super(looper);
            mVideoPlayer = mp;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mVideoPlayer.mNativeContext == 0) {
                Log.w(TAG, "videoplayer went away with unhandled events");
                return;
            }

            switch (msg.what) {
                // 准备完成
                case MEDIA_PREPARED: {
                    if (mOnPreparedListener != null) {
                        mOnPreparedListener.onPrepared(mVideoPlayer);
                    }
                    break;
                }

                // 播放完成回调
                case MEDIA_PLAYBACK_COMPLETE: {
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mVideoPlayer);
                    }
                    break;
                }

                // 播放开始回调
                case MEDIA_STARTED: {
                    Log.d(TAG, "music player is started!");
                    break;
                }

                // 跳转完成回调
                case MEDIA_SEEK_COMPLETE: {
                    if (mOnSeekCompleteListener != null) {
                        mOnSeekCompleteListener.onSeekComplete(mVideoPlayer);
                    }
                    break;
                }

                // 缓冲更新回调
                case MEDIA_BUFFERING_UPDATE: {
                    if (mOnBufferingUpdateListener != null) {
                        mOnBufferingUpdateListener.onBufferingUpdate(mVideoPlayer, msg.arg1);
                    }
                    return;
                }

                // 视频宽高变化回调
                case MEDIA_SET_VIDEO_SIZE: {
                    if (mOnVideoSizeChangedListener != null) {
                        mOnVideoSizeChangedListener.onVideoSizeChanged(mVideoPlayer, msg.arg1, msg.arg2);
                    }
                    return;
                }

                // 播放出错回调
                case MEDIA_ERROR: {
                    Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                    boolean error_was_handled = false;
                    if (mOnErrorListener != null) {
                        error_was_handled = mOnErrorListener.onError(mVideoPlayer, msg.arg1, msg.arg2);
                    }
                    if (mOnCompletionListener != null && !error_was_handled) {
                        mOnCompletionListener.onCompletion(mVideoPlayer);
                    }
                    break;
                }

                // 媒体信息回调
                case MEDIA_INFO: {
                    if (msg.arg1 != MEDIA_INFO_VIDEO_TRACK_LAGGING) {
                        Log.i(TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                    }
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mVideoPlayer, msg.arg1, msg.arg2);
                    }
                    // No real default action so far.
                    return;
                }

                // 当前播放进度回调
                case MEDIA_CURRENT: {
                    if (mOnCurrentPositionListener != null) {
                        mOnCurrentPositionListener.onCurrentPosition(mVideoPlayer, msg.arg1, msg.arg2);
                    }
                    break;
                }

                // 当前视频播放进度回调
                case MEDIA_VIDEO_CURRENT: {
                    if (mOnVideoPositionListener != null) {
                        mOnVideoPositionListener.onVideoPosition(mVideoPlayer, msg.arg1, msg.arg2);
                    }
                    break;
                }

                default: {
                    Log.e(TAG, "Unknown message type " + msg.what);
                    break;
                }
            }
        }
    }

    /**
     * Called from native code when an interesting event happens.  This method
     * just uses the EventHandler system to post the event back to the main app thread.
     * We use a weak reference to the original MusicPlayer object so that the native
     * code is safe from the object disappearing from underneath it.  (This is
     * the cookie passed to native_setup().)
     */
    private static void postEventFromNative(Object mediaplayer_ref,
                                            int what, int arg1, int arg2, Object obj) {
        final VideoPlayer mp = (VideoPlayer)((WeakReference) mediaplayer_ref).get();
        if (mp == null) {
            return;
        }

        if (mp.mEventHandler != null) {
            Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mp.mEventHandler.sendMessage(m);
        }
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    private OnPreparedListener mOnPreparedListener;


    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * Register a callback to be invoked when the status of a network
     * stream's buffer has changed.
     *
     * @param listener the callback that will be run.
     */
    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    private OnBufferingUpdateListener mOnBufferingUpdateListener;

    /**
     * Register a callback to be invoked when a seek operation has been
     * completed.
     *
     * @param listener the callback that will be run
     */
    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    private OnSeekCompleteListener mOnSeekCompleteListener;

    /**
     * Register a callback to be invoked when the video size is
     * known or updated.
     *
     * @param listener the callback that will be run
     */
    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;


    /**
     * Register a callback to be invoked when an error has happened
     * during an asynchronous operation.
     *
     * @param listener the callback that will be run
     */
    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    private OnErrorListener mOnErrorListener;

    /**
     * Register a callback to be invoked when an info/warning is available.
     *
     * @param listener the callback that will be run
     */
    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    private OnInfoListener mOnInfoListener;

    /**
     * Register a callback to be invoked on playing position.
     * @param listener the callback that will be run
     */
    @Override
    public void setOnCurrentPositionListener(OnCurrentPositionListener listener) {
        mOnCurrentPositionListener = listener;
    }

    private OnCurrentPositionListener mOnCurrentPositionListener;


    /**
     * Interface definition of a callback to be invoked to video stream playing position.
     */
    public interface OnVideoPositionListener {

        void onVideoPosition(IMediaPlayer mp, float current, float duration);
    }

    /**
     * Register a callback to be invoked on video stream playing position.
     * @param listener the callback that will be run
     */
    public void setVideoPositionListener(OnVideoPositionListener listener) {
        mOnVideoPositionListener = listener;
    }

    private OnVideoPositionListener mOnVideoPositionListener;
}
