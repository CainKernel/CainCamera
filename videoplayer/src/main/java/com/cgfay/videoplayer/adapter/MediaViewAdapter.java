package com.cgfay.videoplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.cgfay.utilslibrary.SquareImageView;
import com.cgfay.videoplayer.R;
import com.cgfay.videoplayer.bean.MediaMeta;

import java.util.List;

/**
 * recyclerview适配器
 * Created by cain.huang on 2017/8/9.
 */

public class MediaViewAdapter extends RecyclerView.Adapter<MediaViewAdapter.PhotoHolder> {

    private boolean multiSelectEnable = false;

    private Context mContext;
    private List<MediaMeta> mPhotoList;
    // 监听器
    private OnItemClickLitener mLitener;
    // 是否使能长按功能
    private boolean canLongClick = false;


    public MediaViewAdapter(Context context, List<MediaMeta> photoList) {
        mContext = context;
        mPhotoList = photoList;
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_media_view, null);
        return new PhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
        if (!TextUtils.isEmpty(mPhotoList.get(position).getPath())) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .priority(Priority.HIGH);
            Glide.with(mContext)
                    .load(mPhotoList.get(position).getPath())
                    .apply(options)
                    .into(holder.mImageView);
        }
        if (mPhotoList.get(position).getMimeType().startsWith("video/")) {
            holder.mVideoPlayView.setVisibility(View.VISIBLE);
        } else {
            holder.mVideoPlayView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (mPhotoList == null) ? 0 : mPhotoList.size();
    }

    /**
     * 是否允许多选
     * @param enable
     */
    public void setMultiSelectEnable(boolean enable) {
        multiSelectEnable = enable;
    }

    /**
     * 设置点击事件回调
     * @param litener
     */
    public void addItemClickListener(OnItemClickLitener litener) {
        mLitener = litener;
    }

    /**
     * 设置是否允许长按功能
     * @param enable
     */
    public void setLongClickEnable(boolean enable) {
        canLongClick = enable;
    }

    // 点击事件回调
    public interface OnItemClickLitener {

        void onSingleSelected(int position); // 但选

        void onMultiSelected(int position); // 多选

        void onItemLongPressed(); // 长按功能触发
    }

    class PhotoHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        // 预览缩略图
        public SquareImageView mImageView;
        // 视频显示图标
        public ImageView mVideoPlayView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (SquareImageView) itemView.findViewById(R.id.iv_photo);
            mVideoPlayView = (ImageView) itemView.findViewById(R.id.video_play);
            mImageView.setOnClickListener(this);
            mImageView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_photo:
                    selectedAction(multiSelectEnable);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                // 是否允许长按功能，用于选中多张图片
                case R.id.iv_photo:
                    if (canLongClick) {
                        multiSelectEnable = true;
                        notifyDataSetChanged();
                        if (mLitener != null) {
                            mLitener.onItemLongPressed();
                        }
                        processMultiSelected();
                    }
                    break;
            }
            return false;
        }

        /**
         * 选择动作处理
         * @param multiSelected
         */
        public void selectedAction(boolean multiSelected) {
            // 是否多选
            if (!multiSelected) {
                processSingleSelected();
            } else {
                processMultiSelected();
            }
        }

        /**
         * 处理单选
         */
        private void processSingleSelected() {
            if (mLitener != null) {
                mLitener.onSingleSelected(getLayoutPosition());
            }
        }

        /**
         * 处理多选
         */
        private void processMultiSelected() {
            int index = getLayoutPosition();
            // 更新选中状态
            mPhotoList.get(index).setSelected(!mPhotoList.get(index).isSelected());
            if (mLitener != null) {
                mLitener.onMultiSelected(index);
            }
        }
    }
}
