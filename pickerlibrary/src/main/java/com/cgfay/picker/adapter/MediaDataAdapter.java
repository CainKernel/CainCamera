package com.cgfay.picker.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cgfay.picker.MediaPickerManager;
import com.cgfay.picker.utils.MediaMetadataUtils;
import com.cgfay.scan.R;
import com.cgfay.picker.model.MediaData;
import com.cgfay.uitls.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体列表适配器
 */
public class MediaDataAdapter extends RecyclerView.Adapter<MediaDataAdapter.ThumbnailViewHolder> {

    private static final String TAG = "MediaDataAdapter";

    private final Object mLock = new Object();

    // 是否显示选中数字checkbox
    private boolean mShowCheckbox;
    // 缩略图的调节大小
    private int mResize;

    // 选中回调
    private OnMediaDataChangeListener mMediaDataChangeListener;

    // 媒体数据列表
    private final List<MediaData> mMediaDataList = new ArrayList<>();


    public MediaDataAdapter() {
        mResize = -1;
        mShowCheckbox = true;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_view, parent, false);
        return new ThumbnailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        final MediaData mediaData = mMediaDataList.get(position);
        if (mediaData != null) {
            holder.mThumbnailView.setAdjustViewBounds(false);
            if (mResize > 0) {
                MediaPickerManager.getInstance().getMediaLoader()
                        .loadThumbnail(holder.itemView.getContext(),
                        holder.mThumbnailView, mediaData.getContentUri(), mResize,
                        R.color.white, R.color.white);
            } else {
                MediaPickerManager.getInstance().getMediaLoader()
                        .loadThumbnail(holder.itemView.getContext(),
                        holder.mThumbnailView, mediaData.getContentUri(),
                        R.color.white, R.color.white);
            }

            if (mediaData.isVideo()) {
                holder.mDurationView.setVisibility(View.VISIBLE);
                holder.mDurationView.setText(StringUtils.generateStandardTime((int)mediaData.getDurationMs()));
            } else {
                holder.mDurationView.setVisibility(View.GONE);
            }
            holder.mLayoutCheckbox.setVisibility(mShowCheckbox ? View.VISIBLE : View.GONE);
            holder.mLayoutCheckbox.setOnClickListener(v -> {
                if (mediaData.isImage()) {
                    MediaMetadataUtils.buildImageMetadata(holder.itemView.getContext(), mediaData);
                } else {
                    MediaMetadataUtils.buildVideoMetadata(holder.itemView.getContext(), mediaData);
                }
                if (mMediaDataChangeListener != null) {
                    mMediaDataChangeListener.onMediaSelectedChange(mediaData);
                }
            });
            if (mMediaDataChangeListener != null) {
                if (mMediaDataChangeListener.getSelectedIndex(mediaData) >= 0) {
                    holder.mCheckboxView.setText(String.valueOf(mMediaDataChangeListener.getSelectedIndex(mediaData) + 1));
                    holder.mCheckboxView.setSelected(true);
                } else {
                    holder.mCheckboxView.setText("");
                    holder.mCheckboxView.setSelected(false);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                if (mediaData.isImage()) {
                    MediaMetadataUtils.buildImageMetadata(holder.itemView.getContext(), mediaData);
                } else {
                    MediaMetadataUtils.buildVideoMetadata(holder.itemView.getContext(), mediaData);
                }
                if (mMediaDataChangeListener != null) {
                    mMediaDataChangeListener.onMediaPreview(mediaData);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMediaDataList.size();
    }

    /**
     * 设置媒体数据
     * @param datas
     */
    public void setMediaData(@NonNull List<MediaData> datas) {
        synchronized (mLock) {
            mMediaDataList.clear();
            if (datas.size() > 0) {
                mMediaDataList.addAll(datas);
            }
        }
    }

    /**
     * 获取媒体数据
     * @return
     */
    public List<MediaData> getMediaDataList() {
        return mMediaDataList;
    }

    /**
     * 将媒体数据追加到尾部
     * @param datas
     */
    public void appendNewMediaData(@NonNull List<MediaData> datas) {
        if (datas.size() > 0) {
            synchronized (mLock) {
                mMediaDataList.addAll(datas);
            }
            notifyItemRangeInserted(getItemCount() - datas.size(), datas.size());
        }
    }

    /**
     * 是否显示选中态
     * @param show
     */
    public void setShowCheckbox(boolean show) {
        mShowCheckbox = show;
    }

    /**
     * 设置缩略图的大小
     * @param resize
     */
    public void setThumbnailResize(int resize) {
        mResize = resize;
    }

    public static class ThumbnailViewHolder extends RecyclerView.ViewHolder {

        ImageView mThumbnailView;
        View mLayoutCheckbox;
        TextView mCheckboxView;
        TextView mDurationView;

        public ThumbnailViewHolder(View itemView) {
            super(itemView);
            mThumbnailView = itemView.findViewById(R.id.iv_thumbnail);
            mLayoutCheckbox = itemView.findViewById(R.id.layout_checkbox);
            mCheckboxView = itemView.findViewById(R.id.tv_checkbox);
            mDurationView = itemView.findViewById(R.id.video_duration);
        }
    }

    /**
     * 媒体选择监听器
     */
    public interface OnMediaDataChangeListener {

        // 获取选中索引
        int getSelectedIndex(@NonNull MediaData mediaData);

        // 预览画面
        void onMediaPreview(@NonNull MediaData mediaData);

        // 选中状态变更回调
        void onMediaSelectedChange(@NonNull MediaData mediaData);
    }

    /**
     * 添加媒体选择监听器
     * @param listener
     */
    public void addOnMediaDataChangeListener(OnMediaDataChangeListener listener) {
        mMediaDataChangeListener = listener;
    }
}
