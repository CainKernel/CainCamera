package com.cgfay.image.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.imagelibrary.R;
import com.cgfay.uitls.utils.BitmapUtils;

import java.util.List;

/**
 * 滤镜列表适配器
 */
public class ImageFilterAdapter extends RecyclerView.Adapter<ImageFilterAdapter.ImageHolder> {

    private Context mContext;
    // 滤镜选中索引
    private int mSelected = 0;
    private List<ResourceData> mFilterDataList;

    public ImageFilterAdapter(Context context, List<ResourceData> filterDataList) {
        mContext = context;
        mFilterDataList = filterDataList;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_filter_view, parent, false);
        ImageHolder viewHolder = new ImageHolder(view);
        viewHolder.filterRoot = (LinearLayout) view.findViewById(R.id.item_filter_root);
        viewHolder.filterPanel = (FrameLayout) view.findViewById(R.id.item_filter_panel);
        viewHolder.filterName = (TextView) view.findViewById(R.id.item_filter_name);
        viewHolder.filterImage = (ImageView) view.findViewById(R.id.item_filter_image);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, final int position) {
        ResourceData resourceData = mFilterDataList.get(position);
        if (resourceData.thumbPath.startsWith("assets://")) {
            holder.filterImage.setImageBitmap(BitmapUtils.getImageFromAssetsFile(mContext,
                    resourceData.thumbPath.substring("assets://".length())));
        } else {

        }
        holder.filterName.setText(mFilterDataList.get(position).name);
        if (position == mSelected) {
            holder.filterPanel.setBackgroundResource(R.drawable.image_filter_select_background);
        } else {
            holder.filterPanel.setBackgroundResource(0);
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
                if (mFilterChangeListener != null) {
                    mFilterChangeListener.onFilterChanged(mFilterDataList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mFilterDataList == null) ? 0 : mFilterDataList.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {
        // 根布局
        public LinearLayout filterRoot;
        // 背景框
        public FrameLayout filterPanel;
        // 预览缩略图
        public ImageView filterImage;
        // 预览文字
        public TextView filterName;

        public ImageHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnFilterChangeListener {
        void onFilterChanged(ResourceData resourceData);
    }

    private OnFilterChangeListener mFilterChangeListener;

    public void addOnFilterChangeListener(OnFilterChangeListener listener) {
        mFilterChangeListener = listener;
    }

    /**
     * 滚动到当前选中位置
     * @param selected
     */
    public void scrollToCurrentFilter(int selected) {
        int lastSelected = mSelected;
        mSelected = selected;
        notifyItemChanged(lastSelected, 0);
        notifyItemChanged(mSelected, 0);
    }

    /**
     * 获取选中索引
     * @return
     */
    public int getSelectedPosition() {
        return mSelected;
    }
}
