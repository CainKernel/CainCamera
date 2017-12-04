package com.cgfay.caincamera.view.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 显示图片
 * Created by cain on 2017/10/4.
 */

public class PictureSurfaceView extends PreviewSurfaceView {

    private static final String TAG = "PictureSurfaceView";
    private static final boolean VERBOSE = false;

    private Handler mHandler;
    private int mWidth;
    private int mHeight;
    private Bitmap mBitmap;
    private float mScale;

    public PictureSurfaceView(Context context) {
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
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    public void setPath(List<String> path) {
        super.setPath(path);
        if (mPath != null && mPath.size() > 0) {
            mBitmap = BitmapFactory.decodeFile(mPath.get(0));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mBitmap != null && !mBitmap.isRecycled()) {
            // gif宽高
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();
            // 缩放
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            mScale = (float) initialWidth / mWidth;;
            mWidth = (int)(mWidth * mScale);
            mHeight = (int)(mHeight * mScale);
            setMeasuredDimension(mWidth, mHeight);
            mHandler.post(mRunnable);
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Canvas canvas = mHolder.lockCanvas();
            if (canvas != null && mBitmap != null && !mBitmap.isRecycled()) {
                canvas.save();
                Paint paint = new Paint();
                paint.setColor(Color.rgb(255, 255, 255));
                Rect rect = new Rect(0, 0, mWidth, mHeight);
                canvas.drawBitmap(mBitmap, null, rect, paint);
                canvas.restore();
                mHolder.unlockCanvasAndPost(canvas);
            }
            mHandler.postDelayed(mRunnable, 100);
        }
    };

}
