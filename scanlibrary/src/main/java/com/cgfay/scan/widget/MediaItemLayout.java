package com.cgfay.scan.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cgfay.scan.R;
import com.cgfay.scan.model.MediaItem;
import com.cgfay.scan.engine.MediaScanParam;

/**
 * 媒体item布局
 */
public class MediaItemLayout extends SquareFrameLayout implements View.OnClickListener {

    private ImageView mThumbnail;
    private ImageView mVideoIndicator;
    private TextView mVideoDuration;
    private ImageView mImagePreview;
    private ItemBindInfo mItemBindInfo;
    private MediaItem mMediaItem;
    private OnMediaItemClickListener mListener;


    public MediaItemLayout(Context context) {
        super(context);
        init(context);
    }

    public MediaItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.media_content_view, this, true);
        mThumbnail = (ImageView) findViewById(R.id.iv_thumbnail);
        mVideoIndicator = (ImageView) findViewById(R.id.video_indicator);
        mVideoDuration = (TextView) findViewById(R.id.video_duration);
        mImagePreview = (ImageView) findViewById(R.id.iv_preview);
        mThumbnail.setOnClickListener(this);
        mImagePreview.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            if (v.getId() == R.id.iv_preview) {
                mListener.onMediaItemClicked(mThumbnail, mMediaItem, mItemBindInfo.mViewHolder, true);
            } else if (v.getId() == R.id.iv_thumbnail) {
                mListener.onMediaItemClicked(mThumbnail, mMediaItem, mItemBindInfo.mViewHolder, false);
            }
        }
    }

    /**
     * 设置图片
     */
    private void setImage() {
        if (mMediaItem.isGif()) {
            MediaScanParam.getInstance().mediaLoader.loadGifThumbnail(getContext(),
                    mItemBindInfo.mResize, mItemBindInfo.mPlaceholder, mThumbnail,
                    mMediaItem.getContentUri());
        } else {
            MediaScanParam.getInstance().mediaLoader.loadThumbnail(getContext(),
                    mItemBindInfo.mResize, mItemBindInfo.mPlaceholder, mThumbnail,
                    mMediaItem.getContentUri());
        }
    }

    /**
     * 计算视频时长
     */
    private void calculateVideoDuration() {
        if (mMediaItem.isVideo()) {
            mVideoIndicator.setVisibility(VISIBLE);
            mVideoDuration.setVisibility(VISIBLE);
            mVideoDuration.setText(DateUtils.formatElapsedTime(mMediaItem.duration / 1000));
        } else {
            mVideoDuration.setVisibility(GONE);
            mVideoIndicator.setVisibility(GONE);
        }
    }

    /**
     * 设置列表绑定信息
     * @param info
     */
    public void setItemBindInfo(ItemBindInfo info) {
        mItemBindInfo = info;
    }

    /**
     * 设置媒体item
     * @param item
     */
    public void setMediaItem(MediaItem item) {
        mMediaItem = item;
        setImage();
        calculateVideoDuration();
    }

    /**
     * 获取媒体item对象
     * @return
     */
    public MediaItem getMediaItem() {
        return mMediaItem;
    }


    /**
     * 添加item点击监听器
     * @param listener
     */
    public void addOnMediaItemClickListener(OnMediaItemClickListener listener) {
        mListener = listener;
    }

    /**
     * 媒体item点击监听器
     */
    public interface OnMediaItemClickListener {

        void onMediaItemClicked(ImageView thumbnail, MediaItem item, RecyclerView.ViewHolder holder, boolean preview);

    }

    /**
     * item绑定信息
     */
    public static class ItemBindInfo {
        int mResize;
        Drawable mPlaceholder;
        RecyclerView.ViewHolder mViewHolder;

        public ItemBindInfo(int resize, Drawable placeholder, RecyclerView.ViewHolder viewHolder) {
            mResize = resize;
            mPlaceholder = placeholder;

            mViewHolder = viewHolder;
        }
    }

}
