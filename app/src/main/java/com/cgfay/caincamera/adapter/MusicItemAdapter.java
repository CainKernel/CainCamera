package com.cgfay.caincamera.adapter;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cgfay.caincamera.R;

import com.cgfay.caincamera.bean.MusicItem;
import com.cgfay.utilslibrary.utils.StringUtils;

/**
 * 本地音乐适配器
 */
public class MusicItemAdapter extends CursorAdapter<RecyclerView.ViewHolder> {

    private OnMusicItemSelectedListener mListener;

    public MusicItemAdapter(Cursor cursor) {
        super(cursor);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_scan_view, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        MusicViewHolder viewHolder = (MusicViewHolder) holder;
        final MusicItem music = MusicItem.valueof(cursor);
        viewHolder.mTextName.setText(music.getName());
        viewHolder.mTexDuration.setText(StringUtils.generateMillisTime((int) music.getDuration()));
        viewHolder.mLayoutMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onMusicItemSelected(music);
                }
            }
        });
    }

    /**
     * 设置选中音乐监听器
     * @param listener
     */
    public void setOnMusicSelectedListener(OnMusicItemSelectedListener listener) {
        mListener = listener;
    }

    /**
     * 音乐选中监听器
     */
    public interface OnMusicItemSelectedListener {
        // 选中音乐
        void onMusicItemSelected(MusicItem music);
    }


    class MusicViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLayoutMusic;
        private TextView mTextName;
        private TextView mTexDuration;

        public MusicViewHolder(View itemView) {
            super(itemView);
            mLayoutMusic = (LinearLayout) itemView.findViewById(R.id.layout_item_music);
            mTextName = (TextView) itemView.findViewById(R.id.tv_music_name);
            mTexDuration = (TextView) itemView.findViewById(R.id.tv_music_duration);
        }
    }

}
