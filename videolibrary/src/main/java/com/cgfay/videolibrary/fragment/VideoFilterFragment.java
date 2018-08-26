package com.cgfay.videolibrary.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;
import com.cgfay.videolibrary.R;
import com.cgfay.videolibrary.adapter.VideoFilterAdapter;

/**
 * 滤镜页面
 */
public class VideoFilterFragment extends BaseVideoFilterFragment implements RecyclerView.OnItemTouchListener {

    private GestureDetectorCompat mGestureDetector;
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
        VideoFilterAdapter adapter = new VideoFilterAdapter(getActivity(), mGlFilterType, mFilterName);
        mFilterListView.setAdapter(adapter);
        mGestureDetector = new GestureDetectorCompat(mFilterListView.getContext(),
                new ItemTouchHelperGestureListener());
    }

    @Override
    public void onResume() {
        super.onResume();
        mFilterListView.addOnItemTouchListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mFilterListView.removeOnItemTouchListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mListener != null) {
                View child = mFilterListView.findChildViewUnder(e.getX(), e.getY());
                int position = mFilterListView.getChildAdapterPosition(child);
                mListener.onFilterSelected(getFilterType(position));
            }
            return super.onSingleTapUp(e);
        }
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
