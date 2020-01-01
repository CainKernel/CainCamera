package com.cgfay.picker.adapter;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.cgfay.uitls.utils.DensityUtils;

/**
 * 相册列表分割线
 */
public class AlbumItemDecoration extends RecyclerView.ItemDecoration {


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        outRect.top = DensityUtils.dp2px(view.getContext(), 12);
        if (parent.getAdapter() != null) {
            if (position == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = DensityUtils.dp2px(view.getContext(), 12);
            }
        }
    }
}
