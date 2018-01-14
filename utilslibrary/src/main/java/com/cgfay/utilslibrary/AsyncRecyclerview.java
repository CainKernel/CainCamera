package com.cgfay.utilslibrary;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;

/**
 * 异步加载图片的RecyclerView，滑动过程不加载图片
 * Created by cain.huang on 2017/8/9.
 */
public class AsyncRecyclerview extends RecyclerView {
    public AsyncRecyclerview(Context context) {
        super(context);
        init();
    }

    public AsyncRecyclerview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AsyncRecyclerview(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addOnScrollListener(new ImageAutoLoadScrollListener());
    }

    private class ImageAutoLoadScrollListener extends OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {

                case RecyclerView.SCROLL_STATE_IDLE: //  当屏幕停止滚动，滑动器处于空闲状态，加载图片
                    if (getContext() != null) {
                        Glide.with(getContext()).resumeRequests();
                    }
                    break;

                case RecyclerView.SCROLL_STATE_DRAGGING: //  当屏幕滚动并且处于触摸状态
                case RecyclerView.SCROLL_STATE_SETTLING: // 松开屏幕，但屏幕产生惯性滑动
                    if (getContext() != null) {
                        Glide.with(getContext()).pauseRequests();
                    }
                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    }
}
