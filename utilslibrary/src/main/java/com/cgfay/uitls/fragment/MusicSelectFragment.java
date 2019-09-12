package com.cgfay.uitls.fragment;

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

import com.cgfay.uitls.adapter.LocalMusicAdapter;
import com.cgfay.uitls.bean.Music;
import com.cgfay.uitls.scanner.LocalMusicScanner;
import com.cgfay.utilslibrary.R;

/**
 * 音乐选择页面
 */
public class MusicSelectFragment extends Fragment implements LocalMusicScanner.MusicScanCallbacks,
        LocalMusicAdapter.OnMusicItemSelectedListener {

    private LocalMusicScanner mMusicScanner;
    private RecyclerView mRecyclerView;
    private LocalMusicAdapter mAdapter;
    private OnMusicSelectedListener mMusicSelectedListener;

    public MusicSelectFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_music_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.music_list);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new LocalMusicAdapter(null);
        mAdapter.setOnMusicSelectedListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mMusicScanner = new LocalMusicScanner(getActivity(), this);
        mMusicScanner.scanLocalMusic();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMusicScanner.destroy();
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
    public void onMusicItemSelected(Music music) {
        if (mMusicSelectedListener != null) {
            mMusicSelectedListener.onMusicSelected(music);
        }
    }

    /**
     * 音乐选中监听器
     */
    public interface OnMusicSelectedListener {

        void onMusicSelected(Music music);
    }

    /**
     * 添加音乐选中监听器
     * @param listener
     */
    public void addOnMusicSelectedListener(OnMusicSelectedListener listener) {
        mMusicSelectedListener = listener;
    }
}
