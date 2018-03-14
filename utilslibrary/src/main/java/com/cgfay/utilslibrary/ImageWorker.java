package com.cgfay.utilslibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * 加载图片
 * Created by Administrator on 2018/3/14.
 */

public abstract class ImageWorker {

    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    protected Resources mResources;

    private static final int MSG_CLEAR = 0;
    private static final int MSG_INIT_DISK_CACHE = 1;
    private static final int MSG_FLUSH = 2;
    private static final int MSG_CLOSE = 3;

    protected ImageWorker(Context context) {
        mResources = context.getResources();
    }

    /**
     * 加载图片
     * @param data
     * @param imageView
     * @param listener
     */
    public void loadImage(Object data, ImageView imageView, OnImageLoadedListener listener) {
        if (data == null) {
            return;
        }
        Bitmap bitmap = null;
        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }

        if (bitmap != null) {
            setImageBitmap(imageView, bitmap);
        } else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(data, imageView, listener);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR);
        }
    }

    /**
     * 设置placeholder
     * @param bitmap
     */
    public void setPlaceHolder(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * 设置placeholder
     * @param resId
     */
    public void setPlaceHolder(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * 添加缓存
     * @param cacheParams
     */
    public void addImageCache(ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        mImageCache = new ImageCache(mImageCacheParams);
        new CacheAsyncTask().execute(MSG_INIT_DISK_CACHE);
    }

    /**
     * 添加缓存
     * @param context
     * @param diskCacheDirectoryName
     */
    public void addImageCache(Context context, String diskCacheDirectoryName) {
        mImageCacheParams = new ImageCache.ImageCacheParams(context, diskCacheDirectoryName);
        mImageCache = new ImageCache(mImageCacheParams);
        new CacheAsyncTask().execute(MSG_INIT_DISK_CACHE);
    }

    /**
     * 设置图片
     * @param imageView
     * @param bitmap
     */
    private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(bitmap);
        setImageDrawable(imageView, drawable);
    }

    /**
     * 设置图片
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drawable and the final drawable
            @SuppressLint("ResourceAsColor")
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(android.R.color.transparent),
                            drawable
                    });
            // Set background to loading bitmap
            imageView.setBackgroundDrawable(
                    new BitmapDrawable(mResources, mLoadingBitmap));

            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * 处理图片
     * @param data
     * @return
     */
    protected abstract Bitmap processBitmap(Object data);

    /**
     * 获取ImageCache
     * @return
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * 取消加载
     * @param imageView
     */
    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (BuildConfig.DEBUG) {
                final Object bitmapData = bitmapWorkerTask.mData;
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
            }
        }
    }

    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mData;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class BitmapWorkerTask extends AsyncTask<Void, Void, BitmapDrawable> {
        private Object mData;
        private final WeakReference<ImageView> imageViewReference;
        private final OnImageLoadedListener mOnImageLoadedListener;

        public BitmapWorkerTask(Object data, ImageView imageView) {
            mData = data;
            imageViewReference = new WeakReference<ImageView>(imageView);
            mOnImageLoadedListener = null;
        }

        public BitmapWorkerTask(Object data, ImageView imageView, OnImageLoadedListener listener) {
            mData = data;
            imageViewReference = new WeakReference<ImageView>(imageView);
            mOnImageLoadedListener = listener;
        }

        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(Void... params) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }

            final String dataString = String.valueOf(mData);
            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(dataString);
            }

            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = processBitmap(mData);
            }

            if (bitmap != null) {
                drawable = new BitmapDrawable(mResources, bitmap);

                if (mImageCache != null) {
                    mImageCache.addBitmapToCache(dataString, bitmap);
                }
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work");
            }
            return drawable;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable value) {
            boolean success = false;
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final ImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostExecute - setting bitmap");
                }
                success = true;
                setImageDrawable(imageView, value);
            }
            if (mOnImageLoadedListener != null) {
                mOnImageLoadedListener.onImageLoaded(success);
            }
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    public interface OnImageLoadedListener {
        void onImageLoaded(boolean success);
    }

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MSG_CLEAR:
                    clearCacheInternal();
                    break;
                case MSG_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MSG_FLUSH:
                    flushCacheInternal();
                    break;
                case MSG_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MSG_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MSG_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MSG_CLOSE);
    }
}
