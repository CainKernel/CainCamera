package com.cgfay.videolibrary.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;
import com.cgfay.videolibrary.R;
import com.cgfay.videolibrary.adapter.VideoFilterAdapter;

/**
 * 滤镜页面
 */
public class VideoFilterFragment extends BaseVideoFilterFragment {

    // 当前滤镜索引
    private int mCurrentFilterIndex = 0;
    private VideoFilterAdapter mFilterAdapter;
    private OnFilterSelectListener mListener;

    public VideoFilterFragment() {
        super();
    }

    @Override
    protected void initFilters() {
        mGlFilterType.addAll(GLImageFilterManager.getFilterTypes());
        mFilterName.addAll(GLImageFilterManager.getFilterNames());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_filter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mFilterListView = (RecyclerView) view.findViewById(R.id.list_effect);
        mFilterLayoutManager = new LinearLayoutManager(getActivity());
        mFilterLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(mFilterLayoutManager);
        mFilterAdapter = new VideoFilterAdapter(getActivity(), mGlFilterType, mFilterName);
        mFilterListView.setAdapter(mFilterAdapter);
        mFilterAdapter.setOnFilterChangeListener(new VideoFilterAdapter.OnFilterChangeListener() {
            @Override
            public void onFilterChanged(GLImageFilterType type) {
                if (mListener != null) {
                    mListener.onFilterSelected(type);
                }
                mCurrentFilterIndex = mFilterAdapter.getSelectedPosition();
            }
        });
        if (mCurrentFilterIndex != mFilterAdapter.getSelectedPosition()) {
            scrollToCurrentFilter(mCurrentFilterIndex);
        }
    }

    /**
     * 滚动到选中的滤镜位置上
     * @param index
     */
    public void scrollToCurrentFilter(int index) {
        if (mFilterListView != null) {
            int firstItem = mFilterLayoutManager.findFirstVisibleItemPosition();
            int lastItem = mFilterLayoutManager.findLastVisibleItemPosition();
            if (index <= firstItem) {
                mFilterListView.scrollToPosition(index);
            } else if (index <= lastItem) {
                int top = mFilterListView.getChildAt(index - firstItem).getTop();
                mFilterListView.scrollBy(0, top);
            } else {
                mFilterListView.scrollToPosition(index);
            }
            mFilterAdapter.scrollToCurrentFilter(index);
        }
        mCurrentFilterIndex = index;
    }

    /**
     * 获取当前滤镜索引
     * @return
     */
    public int getCurrentFilterIndex() {
        return mFilterAdapter != null ? mFilterAdapter.getSelectedPosition() : 0;
    }

    /**
     * 滤镜选中监听器
     */
    public interface OnFilterSelectListener {
        void onFilterSelected(GLImageFilterType type);
    }

    /**
     * 添加滤镜选中监听器
     * @param listener
     */
    public void addFilterSelectListener(OnFilterSelectListener listener) {
        mListener = listener;
    }
}
