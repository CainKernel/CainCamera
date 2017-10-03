package com.cgfay.caincamera.view.preview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 播放Gif动画
 * Created by cain on 2017/10/3.
 */
public class GifSurfaceView extends PreviewSurfaceView {
    private static final String TAG = "GifSurfaceView";
    private static final boolean VERBOSE = false;

    private Movie mMovie;
    // 执行动画
    private Handler mHandler;
    // 放大倍数
    private float mScale = 1.0f;

    private int mWidth;
    private int mHeight;

    public GifSurfaceView(Context context) {
        super(context);
        mHandler = new Handler();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (VERBOSE) {
            Log.d(TAG, "surfaceCreated");
        }
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
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!TextUtils.isEmpty(mPath)) {
            try {
                File file = new File(mPath);
                InputStream stream = new FileInputStream(file);
                mMovie = Movie.decodeStream(stream);
                // gif宽高
                mWidth = mMovie.width();
                mHeight = mMovie.height();
                // 缩放（宽度最大）
                int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
                mScale = (float) initialWidth / mWidth;
                setMeasuredDimension((int)(mWidth * mScale), (int)(mHeight * mScale));
                mHandler.post(mRunnable);

            } catch (IOException e) {
                if (VERBOSE) {
                    Log.d(TAG, "unable to play movie", e);
                }
                return;
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Canvas canvas = mHolder.lockCanvas();
            if (canvas != null) {
                canvas.save();
                canvas.scale(mScale, mScale);
                mMovie.draw(canvas, 0, 0);
                mMovie.setTime((int) (System.currentTimeMillis() % mMovie.duration()));
                canvas.restore();
                mHolder.unlockCanvasAndPost(canvas);
            }
            mHandler.postDelayed(mRunnable, 50);
        }
    };

}
