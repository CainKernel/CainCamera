package com.cgfay.cameralibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cgfay.cameralibrary.R;

/**
 * 贴纸选择页面
 */
public class PreviewStickerFragment extends Fragment {

    private static final String TAG = "StickerSelectedFragment";

    // 内容显示列表
    private View mContentView;

    // 标题
    private LinearLayout mLayoutStickerTitle;

    // 容器
    private LinearLayout mLayoutStickerContent;

    // 布局管理器
    private LayoutInflater mInflater;
    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mInflater = LayoutInflater.from(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_preview_stickers, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(mContentView);
    }

    private void initView(View view) {
        mLayoutStickerTitle = (LinearLayout) view.findViewById(R.id.layout_sticker_title);
        mLayoutStickerContent = (LinearLayout) view.findViewById(R.id.layout_sticker_content);
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

}
