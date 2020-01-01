package com.cgfay.uitls.adapter;

import android.database.Cursor;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 本地游标适配器
 * @param <VH>
 */
public abstract class LocalCursorAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private Cursor mCursor;
    private int mColumnID;

    public LocalCursorAdapter(Cursor cursor) {
        setHasStableIds(true);
        setCursor(cursor);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (!isCursorValid(mCursor)) {
            throw new IllegalStateException("Failed to bind view holder when cursor is invalid!");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Failed to move cursor to position "
                    + position + " when trying to bind view holder!");
        }
        onBindViewHolder(holder, mCursor);
    }

    protected abstract void onBindViewHolder(VH holder, Cursor cursor);

    @Override
    public int getItemCount() {
        if (isCursorValid(mCursor)) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (!isCursorValid(mCursor)) {
            throw new IllegalStateException("Failed to lookup item id when cursor is invalid!");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Failed to move cursor to position "
                    + position + " when trying to get an item id");
        }
        return mCursor.getLong(mColumnID);
    }

    public void setCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return;
        }
        if (cursor != null) {
            mCursor = cursor;
            mColumnID = mCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            notifyDataSetChanged();
        } else {
            notifyItemRangeChanged(0, getItemCount());
            mCursor = null;
            mColumnID = -1;
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    private boolean isCursorValid(Cursor cursor) {
        return cursor != null && !cursor.isClosed();
    }
}
