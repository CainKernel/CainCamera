package com.cgfay.caincamera.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.adapter.MusicItemAdapter;
import com.cgfay.caincamera.bean.MusicItem;
import com.cgfay.caincamera.scanner.MusicItemScanner;


/**
 * 音乐选择页面
 */
public class MusicScanFragment extends Fragment implements MusicItemScanner.MusicScanCallbacks,
        MusicItemAdapter.OnMusicItemSelectedListener {

    private MusicItemScanner mMusicItemScanner;
    private RecyclerView mRecyclerView;
    private MusicItemAdapter mAdapter;
    private OnMusicSelectedListener mMusicSelectedListener;

    public MusicScanFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.music_list);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new MusicItemAdapter(null);
        mAdapter.setOnMusicSelectedListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mMusicItemScanner = new MusicItemScanner(getActivity(), this);
        mMusicItemScanner.scanLocalMusic();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMusicItemScanner.destroy();
    }

    @Override
    public void onMusicScanFinish(Cursor cursor) {
        mAdapter.setCursor(cursor);
    }

    @Override
    public void onMusicScanReset() {
        mAdapter.setCursor(null);
    }

    @Override
    public void onMusicItemSelected(MusicItem music) {
        if (mMusicSelectedListener != null) {
            mMusicSelectedListener.onMusicSelected(music);
        }
    }

    /**
     * 音乐选中监听器
     */
    public interface OnMusicSelectedListener {
        void onMusicSelected(MusicItem music);
    }

    /**
     * 添加音乐选中监听器
     * @param listener
     */
    public void addOnMusicSelectedListener(OnMusicSelectedListener listener) {
        mMusicSelectedListener = listener;
    }
}
