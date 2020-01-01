package com.cgfay.uitls.scanner;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.lang.ref.WeakReference;

/**
 * 本地音乐扫描器
 */
public class LocalMusicScanner implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 2;

    private WeakReference<Context> mWeakContext;
    private LoaderManager mLoaderManager;
    private MusicScanCallbacks mCallbacks;

    public LocalMusicScanner(FragmentActivity activity, MusicScanCallbacks callbacks) {
        mWeakContext = new WeakReference<Context>(activity);
        mLoaderManager = LoaderManager.getInstance(activity);
        mCallbacks = callbacks;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Context context = mWeakContext.get();
        if (context == null) {
            return null;
        }
        return LocalMusicCursorLoader.newInstance(context);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Context context = mWeakContext.get();
        if (context == null) {
            return;
        }
        if (mCallbacks != null) {
            mCallbacks.onMusicScanFinish(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (mCallbacks != null) {
            mCallbacks.onMusicScanReset();
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(LOADER_ID);
        }
        mCallbacks = null;
    }

    /**
     * 扫描本地音乐
     */
    public void scanLocalMusic() {
        mLoaderManager.initLoader(LOADER_ID, null, this);
    }

    /**
     * 音频扫描回调
     */
    public interface MusicScanCallbacks {

        void onMusicScanFinish(Cursor cursor);

        void onMusicScanReset();
    }
}