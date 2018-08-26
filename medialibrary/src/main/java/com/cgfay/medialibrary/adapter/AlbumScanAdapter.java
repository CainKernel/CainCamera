package com.cgfay.medialibrary.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.cgfay.medialibrary.R;
import com.cgfay.medialibrary.model.AlbumItem;

/**
 * 相册扫描适配器
 */
public class AlbumScanAdapter extends CursorAdapter {

    public AlbumScanAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public AlbumScanAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_album_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AlbumItem albumItem = AlbumItem.valueOf(cursor);
        ((TextView) view.findViewById(R.id.tv_album_name)).setText(albumItem.getDisplayName(context));
        ((TextView) view.findViewById(R.id.tv_album_media_count)).setText(String.valueOf(albumItem.getCount()));
    }
}
