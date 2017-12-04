package com.cgfay.caincamera.view.preview;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;
import com.cgfay.caincamera.multimedia.MoviePlayer;
import com.cgfay.caincamera.multimedia.SpeedControlCallback;
import com.cgfay.caincamera.view.AspectFrameLayout;

import java.io.File;
import java.io.IOException;

/**
 * 预览视频
 * Created by cain on 2017/10/3.
 */

public class VideoSurfaceView extends PreviewSurfaceView implements MoviePlayer.PlayerFeedback {

    private static final String TAG = "VideoSurfaceView";
    private static final boolean VERBOSE = false;

    private SurfaceHolder mHolder;
    // 播放器
    private MoviePlayer.PlayTask mPlayTask;

    private AspectFrameLayout mPreviewLayout;

    public VideoSurfaceView(Context context, AspectFrameLayout layout) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPreviewLayout = layout;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (VERBOSE) {
            Log.d(TAG, "surfaceCreated");
        }
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (VERBOSE) {
            Log.d(TAG, "surfaceChanged");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (VERBOSE) {
            Log.d(TAG, "surfaceDestroyed");
        }
        if (mPlayTask != null) {
            stopPlayback();
            mPlayTask.waitForStop();
            mPlayTask = null;
        }
    }

    @Override
    public void playbackStopped() {
        mPlayTask = null;
    }

    /**
     * 停止回放
     */
    private void stopPlayback() {
        if (mPlayTask != null) {
            mPlayTask.requestStop();
        }
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        if (mPlayTask != null) {
            if (VERBOSE) {
                Log.d(TAG, "movie already playing");
            }
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "starting movie");
        }
        SpeedControlCallback callback = new SpeedControlCallback();
        Surface surface = mHolder.getSurface();
        clearSurface(surface);
        MoviePlayer player = null;
        try {
            player = new MoviePlayer(new File(mPath.get(0)), surface, callback);
        } catch (IOException e) {
            if (VERBOSE) {
                Log.d(TAG, "unable to play movie", e);
            }
            surface.release();
            return;
        }
        int width = player.getVideoWidth();
        int height = player.getVideoHeight();
        mPreviewLayout.setAspectRatio((double) width / height);
        mPlayTask = new MoviePlayer.PlayTask(player, this);
        mPlayTask.setLoopMode(true);
        mPlayTask.execute();
    }

    /**
     * 清除Surface
     * @param surface
     */
    private void clearSurface(Surface surface) {
        EglCore eglCore = new EglCore();
        WindowSurface windowSurface = new WindowSurface(eglCore, surface, false);
        windowSurface.makeCurrent();
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        windowSurface.swapBuffers();
        windowSurface.release();
        eglCore.release();
    }

}
