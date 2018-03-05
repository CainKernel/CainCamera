package com.cgfay.caincamera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.bean.Music;
import com.cgfay.utilslibrary.StringUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/3/5.
 */

public class MusicAdapter extends BaseAdapter {

    private Context context;
    private List<Music> musics;

    public MusicAdapter(Context context, List<Music> musics) {
        this.context = context;
        this.musics = musics;
    }

    @Override
    public int getCount() {
        return musics.size();
    }

    @Override
    public Music getItem(int i) {
        return musics.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final MusicHolder holder;
        if(view == null){
            holder = new MusicHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_music,null);
            holder.mName = (TextView) view.findViewById(R.id.name);
            holder.mSingerName = (TextView) view.findViewById(R.id.singer_name);
            holder.mDuration = (TextView) view.findViewById(R.id.duration);
            view.setTag(holder);
        }else{
            holder = (MusicHolder) view.getTag();
        }
        Music m = getItem(i);
        holder.mName.setText(m.getName());
        holder.mSingerName.setText(m.getSingerName());
        holder.mDuration.setText(StringUtils.generateStandardTime(m.getDuration()));
        return view;
    }
    private class MusicHolder{
        TextView mName;
        TextView mSingerName;
        TextView mDuration;
    }
}