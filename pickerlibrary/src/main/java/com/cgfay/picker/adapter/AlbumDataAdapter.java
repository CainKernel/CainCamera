package com.cgfay.picker.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cgfay.picker.MediaPickerManager;
import com.cgfay.scan.R;
import com.cgfay.picker.model.AlbumData;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册列表适配器
 */
public class AlbumDataAdapter extends RecyclerView.Adapter<AlbumDataAdapter.AlbumViewHolder> {

    private OnAlbumSelectedListener mAlbumSelectedListener;

    private List<AlbumData> mAlbumDataList = new ArrayList<>();


    public AlbumDataAdapter() {

    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_view, parent, false);
        AlbumViewHolder holder = new AlbumViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        final AlbumData album = mAlbumDataList.get(position);
        holder.mAlbumName.setText(album.getDisplayName());
        holder.mAlbumMediaCount.setText(String.valueOf(album.getCount()));
        MediaPickerManager.getInstance().getMediaLoader()
                .loadThumbnail(holder.itemView.getContext(), holder.mAlbumThumbnail,
                        album.getCoverPath(), R.color.black, R.color.black);
        holder.itemView.setOnClickListener(v -> {
            if (mAlbumSelectedListener != null) {
                mAlbumSelectedListener.onAlbumSelected(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAlbumDataList.size();
    }

    public void reset() {
        mAlbumDataList.clear();
        notifyDataSetChanged();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {

        TextView mAlbumName;
        TextView mAlbumMediaCount;
        ImageView mAlbumThumbnail;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            mAlbumName = itemView.findViewById(R.id.tv_album_name);
            mAlbumMediaCount = itemView.findViewById(R.id.tv_album_media_count);
            mAlbumThumbnail = itemView.findViewById(R.id.iv_album_thumbnail);
        }
    }

    public void setAlbumDataList(@NonNull List<AlbumData> albumDataList) {
        mAlbumDataList.clear();
        mAlbumDataList.addAll(albumDataList);
        notifyDataSetChanged();
    }

    /**
     * 相册选中监听器
     */
    public interface OnAlbumSelectedListener {

        void onAlbumSelected(AlbumData album);
    }

    /**
     * 添加相册选中监听器
     * @param listener
     */
    public void addOnAlbumSelectedListener(OnAlbumSelectedListener listener) {
        mAlbumSelectedListener = listener;
    }
}
