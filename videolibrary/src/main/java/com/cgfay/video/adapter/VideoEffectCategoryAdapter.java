package com.cgfay.video.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cgfay.video.R;
import com.cgfay.video.bean.EffectMimeType;

import java.util.ArrayList;
import java.util.List;

/**
 * 特效目录适配器
 */
public class VideoEffectCategoryAdapter extends RecyclerView.Adapter<VideoEffectCategoryAdapter.ImageHolder> {

    private Context mContext;
    private int mSelected = 0;
    private List<EffectMimeType> mCategoryList = new ArrayList<>();

    public VideoEffectCategoryAdapter(Context context) {
        mContext = context;
        mCategoryList.add(EffectMimeType.FILTER);
        mCategoryList.add(EffectMimeType.TRANSITION);
        mCategoryList.add(EffectMimeType.MULTIFRAME);
        mCategoryList.add(EffectMimeType.TIME);
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video_effect_category_view, parent, false);
        ImageHolder viewHolder = new ImageHolder(view);
        viewHolder.filterRoot = (LinearLayout) view.findViewById(R.id.item_category_root);
        viewHolder.filterName = (TextView) view.findViewById(R.id.item_category_name);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, final int position) {
        holder.filterName.setText(mCategoryList.get(position).getName());
        if (position == mSelected) {
            holder.filterName.setTextColor(mContext.getResources().getColor(R.color.white));
        } else {
            holder.filterName.setTextColor(mContext.getResources().getColor(R.color.video_edit_effect_category_text_normal));
        }
        holder.filterRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected == position) {
                    return;
                }
                int lastSelected = mSelected;
                mSelected = position;
                notifyItemChanged(lastSelected, 0);
                notifyItemChanged(position, 0);
                if (mEffectCategoryChangeListener != null) {
                    mEffectCategoryChangeListener.onCategoryChange(mCategoryList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {
        // 布局
        public LinearLayout filterRoot;
        // 文字
        public TextView filterName;

        public ImageHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 目录切换
     */
    public interface OnEffectCategoryChangeListener {

        void onCategoryChange(EffectMimeType mimeType);
    }

    public void setOnEffectCategoryChangeListener(OnEffectCategoryChangeListener listener) {
        mEffectCategoryChangeListener = listener;
    }

    private OnEffectCategoryChangeListener mEffectCategoryChangeListener;

}
