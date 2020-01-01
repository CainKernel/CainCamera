package com.cgfay.picker.scanner;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import android.util.Log;

import com.cgfay.picker.loader.MediaDataLoader;
import com.cgfay.picker.model.AlbumData;
import com.cgfay.picker.model.MediaData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class MediaDataScanner implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MediaDataScanner";

    protected static final String ALBUM_ARGS = "album_args";

    protected static final Object mLock = new Object();

    public static final int PAGE_SIZE = 50;
    protected static final int MAX_CACHE_SIZE = PAGE_SIZE * 3;
    protected static final int VIDEO_LOADER_ID = 2;
    protected static final int IMAGE_LOADER_ID = 3;

    protected final Context mContext;
    protected AlbumData mCurrentAlbum;
    protected LoaderManager mLoaderManager;
    protected IMediaDataReceiver mDataReceiver;
    protected boolean mPause;
    protected Disposable mLoadDisposable;
    protected Disposable mPreScanDisposable;
    protected final Handler mMainHandler;
    protected boolean mLoadFinish;
    protected boolean mUserVisible;

    protected WeakReference<Cursor> mWeakCursor;
    protected List<MediaData> mCacheMediaData = new ArrayList<>();
    protected boolean mPageScan;

    public MediaDataScanner(@NonNull Context context, @NonNull LoaderManager manager, IMediaDataReceiver dataReceiver) {
        mContext = context;
        mLoaderManager = manager;
        mDataReceiver = dataReceiver;
        mCurrentAlbum = null;
        mMainHandler = new Handler(Looper.getMainLooper());
        mUserVisible = false;
        mWeakCursor = null;
        mPageScan = false;
    }

    public void setUserVisible(boolean visible) {
        mUserVisible = visible;
        Log.d(TAG, "setUserVisible: " + visible + getMimeType(getMediaType()));
        if (mLoadFinish) {
            preScanMediaData();
        }
    }

    public void resume() {
        if (mPause) {
            mPause = false;
        } else {
            loadMedia();
        }
    }

    public void pause() {
        mPause = true;
        if (mLoadDisposable != null) {
            mLoadDisposable.dispose();
            mLoadDisposable = null;
        }
        if (mPreScanDisposable != null) {
            mPreScanDisposable.dispose();
            mPreScanDisposable = null;
        }
    }

    public void destroy() {
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(getLoaderId());
            mLoaderManager = null;
        }
        mDataReceiver = null;
        mMainHandler.removeCallbacksAndMessages(null);
    }

    public void loadAlbumMedia(@NonNull AlbumData album) {
        if (album.equals(mCurrentAlbum) || (mCurrentAlbum == null && album.isAll())) {
            mCurrentAlbum = album;
            return;
        }
        if (mLoaderManager != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(ALBUM_ARGS, album);
            mLoadFinish = false;
            mCurrentAlbum = album;
            if (mLoadDisposable != null) {
                mLoadDisposable.dispose();
                mLoadDisposable = null;
            }
            if (mPreScanDisposable != null) {
                mPreScanDisposable.dispose();
                mPreScanDisposable = null;
            }
            synchronized (mLock) {
                mCacheMediaData.clear();
            }
            mLoaderManager.restartLoader(getLoaderId(), bundle, this);
        }
    }

    private void loadMedia() {
        if (mLoaderManager != null) {
            if (mCurrentAlbum == null) {
                mLoaderManager.initLoader(getLoaderId(), null, this);
            } else {
                Bundle bundle = new Bundle();
                bundle.putParcelable(ALBUM_ARGS, mCurrentAlbum);
                mLoaderManager.initLoader(getLoaderId(), bundle, this);
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (args == null) {
            return MediaDataLoader.createMediaDataLoader(mContext, getMediaType());
        }
        AlbumData albumData = args.getParcelable(ALBUM_ARGS);
        if (albumData == null) {
            return MediaDataLoader.createMediaDataLoader(mContext, getMediaType());
        }
        return MediaDataLoader.createMediaDataLoader(mContext, albumData, getMediaType());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        synchronized (mLock) {
            if (mWeakCursor != null) {
                mWeakCursor.clear();
            }
            mWeakCursor = new WeakReference<>(data);
        }
        if (isCursorEnable(data) && !mLoadFinish) {
            mLoadFinish = true;
            if (data.getCount() <= PAGE_SIZE) {
                mPageScan = false;
                scanAllMedia(data);
            } else {
                mPageScan = true;
                scanPageMedia(data);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: " + getMimeType(getMediaType()));
    }

    private void scanAllMedia(@NonNull Cursor cursor) {
        if (mLoadDisposable == null) {
            mLoadDisposable = Observable.just(0)
                    .subscribeOn(Schedulers.io())
                    .map(integer -> scanAllMediaData(cursor))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mediaDatas -> {
                        mLoadDisposable = null;
                        if (mDataReceiver != null) {
                            mDataReceiver.onMediaDataObserve(mediaDatas);
                        }
                    });
        }
    }

    private void scanPageMedia(@NonNull Cursor cursor) {
        if (mLoadDisposable == null) {
            mLoadDisposable = Observable.just(0)
                    .subscribeOn(Schedulers.io())
                    .map(integer -> scanPageMediaData(cursor))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> {
                        mLoadDisposable = null;
                    });
        }
    }

    private void preScanMediaData() {
        if (mWeakCursor != null && mWeakCursor.get() != null && isCursorEnable(mWeakCursor.get())) {
            if (mPreScanDisposable == null) {
                mPreScanDisposable = Observable.just(0)
                        .subscribeOn(Schedulers.io())
                        .map(integer -> preScanMediaData(mWeakCursor.get()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> {
                            mPreScanDisposable = null;
                        });
            }
        } else {
            Log.i(TAG, "Cursor cache is invalid!");
        }
    }

    private List<MediaData> scanAllMediaData(@NonNull Cursor cursor) {
        List<MediaData> mediaDatas = new ArrayList<>();
        try {
            while (isCursorEnable(cursor) && cursor.moveToNext()) {
                MediaData media = buildMediaData(cursor);
                if (media != null) {
                    mediaDatas.add(media);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return mediaDatas;
    }

    private int scanPageMediaData(@NonNull Cursor cursor) {
        try {
            List<MediaData> mediaDataList = new ArrayList<>();
            int size = 0;
            while (isCursorEnable(cursor) && cursor.moveToNext() && size < PAGE_SIZE) {
                MediaData media = buildMediaData(cursor);
                if (media != null) {
                    mediaDataList.add(media);
                }
                size++;
            }
            mMainHandler.post(() -> {
                if (mDataReceiver != null) {
                    mDataReceiver.onMediaDataObserve(mediaDataList);
                }
            });
            if (mUserVisible) {
                if (isCursorEnable(cursor) && !cursor.isLast()) {
                    preScanMediaData(cursor);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int preScanMediaData(@NonNull Cursor cursor) {
        List<MediaData> mediaDataList = new ArrayList<>();
        int size = 0;
        while (isCursorEnable(cursor) && cursor.moveToNext() && size < PAGE_SIZE * 2) {
            MediaData media = buildMediaData(cursor);
            if (media != null) {
                mediaDataList.add(media);
            }
            size++;
        }

        synchronized (mLock) {
            mCacheMediaData.addAll(mediaDataList);
        }
        return 0;
    }

    private MediaData buildMediaData(@NonNull Cursor cursor) {
        return MediaData.valueOf(cursor);
    }

    public List<MediaData> getCacheMediaData() {
        List<MediaData> cacheList = new ArrayList<>();
        synchronized (mLock) {
            if (mCacheMediaData.size() > PAGE_SIZE) {
                List<MediaData> subCache = mCacheMediaData.subList(0, PAGE_SIZE);
                cacheList.addAll(subCache);
                mCacheMediaData.subList(0, PAGE_SIZE).clear();
            } else {
                cacheList.addAll(mCacheMediaData);
                mCacheMediaData.clear();
            }
        }
        int size;
        synchronized (mLock) {
            size = mCacheMediaData.size();
        }
        if (size < MAX_CACHE_SIZE) {
            preScanMediaData();
        }
        return cacheList;
    }

    protected boolean isCursorEnable(Cursor cursor) {
        return cursor != null && !cursor.isClosed() && !cursor.isLast();
    }

    public boolean isPageScan() {
        return mPageScan;
    }

    protected abstract int getLoaderId();

    protected abstract @MediaDataLoader.LoadMimeType int getMediaType();

    protected String getMimeType(@MediaDataLoader.LoadMimeType int mimeType) {
        if (mimeType == MediaDataLoader.LOAD_IMAGE) {
            return "images";
        } else if (mimeType == MediaDataLoader.LOAD_VIDEO) {
            return "video";
        } else {
            return "all image and video";
        }
    }
}
