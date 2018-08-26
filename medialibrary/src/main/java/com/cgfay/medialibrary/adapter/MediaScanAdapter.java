package com.cgfay.medialibrary.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cgfay.medialibrary.R;
import com.cgfay.medialibrary.model.AlbumItem;
import com.cgfay.medialibrary.model.MediaItem;
import com.cgfay.medialibrary.engine.MediaScanParam;
import com.cgfay.medialibrary.widget.MediaItemLayout;

/**
 * 媒体扫描适配器
 */
public class MediaScanAdapter extends RecyclerCursorAdapter<RecyclerView.ViewHolder>
        implements MediaItemLayout.OnMediaItemClickListener {

    private static final int TYPE_CAPTURE = 0x01;
    private static final int TYPE_MEDIA = 0x02;

    // 扫描参数
    private MediaScanParam mMediaScanParam;
    // 占位图
    private final Drawable mPlaceholder;
    private RecyclerView mRecyclerView;
    // 重新定义大小
    private int mThumbnailResize;
    // 拍照选中监听器
    private OnCaptureClickListener mCaptureClickListener;
    // 媒体选中监听器
    private OnMediaItemSelectedListener mMediaItemClickListener;

    public MediaScanAdapter(Context context, RecyclerView recyclerView) {
        super(null);
        mPlaceholder = context.getDrawable(R.drawable.ic_media_thumbnail_placeholder);
        mMediaScanParam = MediaScanParam.getInstance();
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CAPTURE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_capture_view, parent, false);
            CaptureViewHolder holder = new CaptureViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCaptureClickListener != null) {
                        mCaptureClickListener.onCapture();
                    }
                }
            });
            return holder;
        } else if (viewType == TYPE_MEDIA) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_view, parent, false);
            return new ThumbnailViewHolder(view);
        }
        return null;
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        if (holder instanceof CaptureViewHolder) {
            // do nothing
        } else if (holder instanceof ThumbnailViewHolder) {
            ThumbnailViewHolder viewHolder = (ThumbnailViewHolder) holder;
            final MediaItem item = MediaItem.valueOf(cursor);
            viewHolder.mMediaItemLayout.setItemBindInfo(new MediaItemLayout.ItemBindInfo(
                    getThumbnailReSize(viewHolder.mMediaItemLayout.getContext()),
                    mPlaceholder, holder));
            viewHolder.mMediaItemLayout.setMediaItem(item);
            viewHolder.mMediaItemLayout.addOnMediaItemClickListener(this);

        }
    }

    @Override
    protected int getItemViewType(int position, Cursor cursor) {
        return MediaItem.valueOf(cursor).isCapture() ? TYPE_CAPTURE : TYPE_MEDIA;
    }

    @Override
    public void onMediaItemClicked(ImageView thumbnail, MediaItem item, RecyclerView.ViewHolder holder, boolean preview) {
        if (preview) {
            if (mMediaItemClickListener != null) {
                mMediaItemClickListener.onMediaItemPreview(null, item, holder.getAdapterPosition());
            }
        } else {
            if (mMediaItemClickListener != null) {
                mMediaItemClickListener.onMediaItemSelected(null, item, holder.getAdapterPosition());
            }
        }
    }

    /**
     * 获取缩略图的大小
     * @param context
     * @return
     */
    private int getThumbnailReSize(Context context) {
        if (mThumbnailResize == 0) {
            RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
            int spanCount = ((GridLayoutManager) lm).getSpanCount();
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int availableWidth = screenWidth - context.getResources().getDimensionPixelSize(
                    R.dimen.media_item_spacing) * (spanCount - 1);
            mThumbnailResize = availableWidth / spanCount;
            mThumbnailResize = (int) (mThumbnailResize * mMediaScanParam.thumbnailScale);
        }
        return mThumbnailResize;
    }

    /**
     * 添加相机选中回调
     * @param cameraCapture
     */
    public void addCaptureClickListener(OnCaptureClickListener cameraCapture) {
        mCaptureClickListener = cameraCapture;
    }

    /**
     * 相机拍照监听器
     */
    public interface OnCaptureClickListener {
        void onCapture();
    }

    /**
     * 添加媒体选中监听器
     * @param listener
     */
    public void addOnMediaSelectedListener(OnMediaItemSelectedListener listener) {
        mMediaItemClickListener = listener;
    }

    /**
     * 选中监听器
     */
    public interface OnMediaItemSelectedListener {
        // 选择媒体
        void onMediaItemSelected(AlbumItem albumItem, MediaItem mediaItem, int position);

        // 预览媒体
        void onMediaItemPreview(AlbumItem albumItem, MediaItem mediaItem, int position);
    }

    /**
     * 拍照ViewHolder
     */
    class CaptureViewHolder extends RecyclerView.ViewHolder {

        private ImageView capture;

        public CaptureViewHolder(View itemView) {
            super(itemView);
            capture = (ImageView) itemView.findViewById(R.id.capture_view);
        }

    }

    /**
     * 缩略图ViewHolder
     */
    class ThumbnailViewHolder extends RecyclerView.ViewHolder {

        private MediaItemLayout mMediaItemLayout;
        public ThumbnailViewHolder(View itemView) {
            super(itemView);
            mMediaItemLayout = (MediaItemLayout) itemView;
        }

    }
}
