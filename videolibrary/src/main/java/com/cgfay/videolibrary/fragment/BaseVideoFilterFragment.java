package com.cgfay.videolibrary.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;

import java.util.ArrayList;

/**
 * 滤镜特效基类
 */
public abstract class BaseVideoFilterFragment extends BaseVideoPageFragment {

    protected ArrayList<GLImageFilterType> mGlFilterType = new ArrayList<>();
    protected ArrayList<String> mFilterName = new ArrayList<>();

    protected RecyclerView mFilterListView;
    protected LinearLayoutManager mFilterLayoutManager;

    public BaseVideoFilterFragment() {
        initFilters();
    }

    /**
     * 初始化滤镜组
     */
    protected abstract void initFilters();

    /**
     * 获取滤镜类型
     * @param position
     * @return
     */
    public GLImageFilterType getFilterType(int position) {
        if (mGlFilterType.size() <= position) {
            return GLImageFilterType.NONE;
        }
        return mGlFilterType.get(position);
    }

}
