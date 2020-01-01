package com.cgfay.uitls.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cgfay.uitls.adapter.LocalMusicAdapter;
import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.scanner.LocalMusicScanner;
import com.cgfay.utilslibrary.R;

/**
 * 音乐选择页面
 */
public class MusicPickerFragment extends AppCompatDialogFragment implements LocalMusicScanner.MusicScanCallbacks,
        LocalMusicAdapter.OnMusicItemSelectedListener {

    public static final String TAG = "MusicPickerFragment";

    private FragmentActivity mActivity;

    private LocalMusicScanner mMusicScanner;
    private RecyclerView mRecyclerView;
    private LocalMusicAdapter mAdapter;
    private OnMusicSelectedListener mMusicSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity)context;
        } else if (getActivity() != null) {
            mActivity = getActivity();
        }
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.MusicDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_music_select, container, false);
        initView(view);
        return view;
    }

    private void initView(@NonNull View view) {
        mRecyclerView = view.findViewById(R.id.music_list);
        view.findViewById(R.id.iv_close).setOnClickListener(v -> {
            if (mMusicSelectedListener != null) {
                mMusicSelectedListener.onMusicSelectClose();
            }
        });
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
    public void onResume() {
        super.onResume();
        initDismissListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMusicScanner.destroy();
    }

    private void initDismissListener() {
        if (getDialog() != null) {
            getDialog().setOnDismissListener(dialog -> {
                closeFragment();
            });
        }
    }

    /**
     * 关闭当前页面
     */
    private void closeFragment() {
        if (getParentFragment() != null) {
            getParentFragment()
                    .getChildFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
        } else if (mActivity != null) {
            mActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
        }
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
    public void onMusicItemSelected(MusicData musicData) {
        if (mMusicSelectedListener != null) {
            mMusicSelectedListener.onMusicSelected(musicData);
        }
    }

    /**
     * 音乐选中监听器
     */
    public interface OnMusicSelectedListener {

        void onMusicSelectClose();

        void onMusicSelected(MusicData musicData);
    }

    /**
     * 添加音乐选中监听器
     * @param listener
     */
    public void addOnMusicSelectedListener(OnMusicSelectedListener listener) {
        mMusicSelectedListener = listener;
    }
}
