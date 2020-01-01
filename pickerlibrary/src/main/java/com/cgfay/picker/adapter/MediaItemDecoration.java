package com.cgfay.picker.adapter;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * 媒体item分割线
 */
public class MediaItemDecoration extends RecyclerView.ItemDecoration {

    // 每行item的数量
    private int mSpanCount;
    // 分割线大小
    private int mSpacing;
    // 边沿是否存在
    private boolean mHasEdge;

    public MediaItemDecoration(int spanCount, int spacing) {
        this(spanCount, spacing, true);
    }

    public MediaItemDecoration(int spanCount, int spacing, boolean hasEdge) {
        this.mSpanCount = spanCount;
        this.mSpacing = spacing;
        this.mHasEdge = hasEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % mSpanCount;

        if (mHasEdge) {
            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            outRect.right = (column + 1) * mSpacing / mSpanCount;
            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing;
        } else {
            outRect.left = column * mSpacing / mSpanCount;
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount;
            if (position >= mSpanCount) {
                outRect.top = mSpacing;
            }
        }
    }
}
