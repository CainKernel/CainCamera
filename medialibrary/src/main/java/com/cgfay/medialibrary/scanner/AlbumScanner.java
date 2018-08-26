package com.cgfay.medialibrary.scanner;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.lang.ref.WeakReference;

/**
 * 相册扫描器
 */
public class AlbumScanner implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;
    private WeakReference<Context> mWeakContext;
    private LoaderManager mLoaderManager;
    private int mCurrentSelection;
    private boolean mLoadFinished;
    private AlbumScanCallbacks mCallbacks;

    public AlbumScanner(FragmentActivity activity, AlbumScanCallbacks callbacks) {
        mWeakContext = new WeakReference<Context>(activity);
        mLoaderManager = activity.getSupportLoaderManager();
        mCallbacks = callbacks;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = mWeakContext.get();
        if (context == null) {
            return null;
        }
        mLoadFinished = false;
        return AlbumCursorLoader.newInstance(context);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Context context = mWeakContext.get();
        if (context == null) {
            return;
        }
        if (!mLoadFinished) {
            mLoadFinished = true;
            if (mCallbacks != null) {
                mCallbacks.onAlbumScanFinish(data);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Context context = mWeakContext.get();
        if (context == null) {
            return;
        }
        if (mCallbacks != null) {
            mCallbacks.onAlbumScanReset();
        }
    }

    /**
     * 销毁加载器
     */
    public void destroy() {
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(LOADER_ID);
        }
        mCallbacks = null;
    }

    /**
     * 扫描全部相册内容
     */
    public void scanAlbums() {
        mLoaderManager.initLoader(LOADER_ID, null, this);
    }

    public int getCurrentSelection() {
        return mCurrentSelection;
    }

    public void setCurrentSelection(int currentSelection) {
        mCurrentSelection = currentSelection;
    }

    /**
     * 扫描回调
     */
    public interface AlbumScanCallbacks {

        void onAlbumScanFinish(Cursor cursor);

        void onAlbumScanReset();
    }
}
