package com.cgfay.videolibrary.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;
import com.cgfay.utilslibrary.utils.BitmapUtils;
import com.cgfay.videolibrary.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜列表适配器
 */
public class VideoFilterAdapter extends RecyclerView.Adapter<VideoFilterAdapter.ImageHolder> {

    private static final String TAG = "VideoFilterAdapter";

    private Context mContext;
    // 滤镜类型
    private List<GLImageFilterType> mGlFilterTypes;
    // 滤镜名称
    private List<String> mFilterNames;
    // 滤镜显示图片
    private List<WeakReference<Bitmap>> mWeakBitmaps = new ArrayList<>();

    public VideoFilterAdapter(Context context,
                              List<GLImageFilterType> glFilterTypes,
                              List<String> filterNames) {
        mContext = context;
        mGlFilterTypes = glFilterTypes;
        mFilterNames = filterNames;
        // 初始化持有的数量，解决onBindViewHolder可能会出现数组越界的情况
        for (int i = 0; i < mGlFilterTypes.size(); i++) {
            String path = "thumbs/" + mGlFilterTypes.get(i).name().toLowerCase() + ".jpg";
            Bitmap bitmap = BitmapUtils.getImageFromAssetsFile(mContext, path);
            mWeakBitmaps.add(i, new WeakReference<Bitmap>(bitmap));
        }
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video_filter_view, parent, false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
        // 设置滤镜图片，这里防止重复加载
        if (mGlFilterTypes != null && !TextUtils.isEmpty(mGlFilterTypes.get(position).toString())) {
            if (mWeakBitmaps.size() <= position
                    || mWeakBitmaps.get(position) == null
                    || mWeakBitmaps.get(position).get() == null
                    || mWeakBitmaps.get(position).get().isRecycled()) {
                String path = "thumbs/" + mGlFilterTypes.get(position).name().toLowerCase() + ".jpg";
                Bitmap bitmap = BitmapUtils.getImageFromAssetsFile(mContext, path);
                if (bitmap != null) {
                    mWeakBitmaps.add(position, new WeakReference<Bitmap>(bitmap));
                    holder.mImageView.setImageBitmap(bitmap);
                }
            } else {
                holder.mImageView.setImageBitmap(mWeakBitmaps.get(position).get());
            }

        }
        // 设置滤镜名称
        if (mFilterNames != null && !TextUtils.isEmpty(mFilterNames.get(position))) {
            holder.mTextView.setText(mFilterNames.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return (mGlFilterTypes == null) ? 0 : mGlFilterTypes.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {
        // 预览缩略图
        public ImageView mImageView;
        // 预览文字
        public TextView mTextView;

        public ImageHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.effect_name);
            mImageView = (ImageView) itemView.findViewById(R.id.effect_image);
        }
    }
}
