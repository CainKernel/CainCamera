package com.cgfay.video.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cgfay.video.R;

import java.util.ArrayList;

/**
 * 多段视频处理页面，音乐卡点、普通模式
 */
public class MultiVideoFragment extends Fragment {

    private static final String PATH = "video_path_list";

    private View mContentView;

    public static MultiVideoFragment newInstance(@NonNull ArrayList<String> pathList) {
        MultiVideoFragment fragment = new MultiVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(PATH, pathList);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_multi_video, container, false);
        initView(mContentView);
        return mContentView;
    }

    private void initView(@NonNull View rootView) {

    }


}
