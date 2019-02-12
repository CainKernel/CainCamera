package com.cgfay.scan.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cgfay.scan.R;
import com.cgfay.scan.adapter.MediaScanAdapter;
import com.cgfay.scan.model.AlbumItem;
import com.cgfay.scan.model.MediaItem;
import com.cgfay.scan.engine.MediaScanParam;
import com.cgfay.scan.scanner.MediaScanner;
import com.cgfay.scan.utils.SpanCountUtils;
import com.cgfay.scan.widget.MediaItemDecoration;

public class MediaScanFragment extends Fragment implements MediaScanner.MediaScanCallbacks,
        MediaScanAdapter.OnCaptureClickListener, MediaScanAdapter.OnMediaItemSelectedListener {

    private static final String CURRENT_ALBUM = "current_album";

    private MediaScanner mMediaScanner;
    private RecyclerView mRecyclerView;
    private MediaScanAdapter mAdapter;
    private MediaScanAdapter.OnCaptureClickListener mCaptureClickListener;
    private MediaScanAdapter.OnMediaItemSelectedListener mMediaItemSelectedListener;

    // 出错提示
    private Toast mErrorTips;

    public static final MediaScanFragment newInstance(AlbumItem albumItem) {
        MediaScanFragment fragment = new MediaScanFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CURRENT_ALBUM, albumItem);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.media_view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AlbumItem albumItem = getArguments().getParcelable(CURRENT_ALBUM);
        mAdapter = new MediaScanAdapter(getContext(), mRecyclerView);
        mAdapter.addCaptureClickListener(this);
        mAdapter.addOnMediaSelectedListener(this);
        mRecyclerView.setHasFixedSize(true);

        int spanCount;
        MediaScanParam mediaScanParam = MediaScanParam.getInstance();
        if (mediaScanParam.expectedItemSize > 0) {
            spanCount = SpanCountUtils.calculateSpanCount(getContext(), mediaScanParam.expectedItemSize);
        } else {
            spanCount = mediaScanParam.spanCount;
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        int spacing = mediaScanParam.spaceSize;
        if (spacing <= 0) {
            spacing = getResources().getDimensionPixelSize(R.dimen.media_item_spacing);
        }
        mRecyclerView.addItemDecoration(new MediaItemDecoration(spanCount, spacing, false));

        mRecyclerView.setAdapter(mAdapter);
        mMediaScanner = new MediaScanner(getActivity(), this);
        mMediaScanner.scanAlbum(albumItem, MediaScanParam.getInstance().showCapture);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMediaScanner.destroy();
    }

    /**
     * 更新相册
     * @param item
     */
    public void updateAlbum(AlbumItem item) {
        if (mMediaScanner != null) {
            mMediaScanner.reScanAlbum(item, MediaScanParam.getInstance().showCapture);
        }
    }

    /**
     * 混动到当前位置
     * @param position
     */
    public void scrollToPosition(int position) {
        mRecyclerView.scrollToPosition(position);
    }

    /**
     * 更新媒体数据
     */
    public void updateMediaData() {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 更新媒体数据
     * @param cursor
     */
    public void updateMediaData(Cursor cursor) {
        mAdapter.setCursor(cursor);
    }



    @Override
    public void onMediaScanFinish(Cursor cursor) {
       updateMediaData(cursor);
    }

    @Override
    public void onMediaScanReset() {
        updateMediaData(null);
    }

    @Override
    public void onCapture() {
        if (mCaptureClickListener != null) {
            mCaptureClickListener.onCapture();
        }
    }

    @Override
    public void onMediaItemSelected(AlbumItem albumItem, MediaItem mediaItem, int position) {
        if (mediaItem.isGif() && !MediaScanParam.getInstance().enableSelectGif) {
            if (mErrorTips != null) {
                mErrorTips.cancel();
            }
            mErrorTips = Toast.makeText(getContext(), "不支持Gif格式图片", Toast.LENGTH_SHORT);
            mErrorTips.show();
            return;
        }
        if (mMediaItemSelectedListener != null) {
            mMediaItemSelectedListener.onMediaItemSelected((AlbumItem) getArguments().getParcelable(CURRENT_ALBUM), mediaItem, position);
        }
    }

    @Override
    public void onMediaItemPreview(AlbumItem albumItem, MediaItem mediaItem, int position) {
        if (mMediaItemSelectedListener != null) {
            mMediaItemSelectedListener.onMediaItemPreview((AlbumItem) getArguments().getParcelable(CURRENT_ALBUM), mediaItem, position);
        }
    }

    /**
     * 添加拍照item选中监听器
     * @param listener
     */
    public void addCaptureClickListener(MediaScanAdapter.OnCaptureClickListener listener) {
        mCaptureClickListener = listener;
    }

    /**
     * 添加媒体item选中监听器
     * @param listener
     */
    public void addMediaItemClickListener(MediaScanAdapter.OnMediaItemSelectedListener listener) {
        mMediaItemSelectedListener = listener;
    }
}