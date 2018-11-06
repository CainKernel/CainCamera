package com.cgfay.videolibrary.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜特效基类
 */
public abstract class BaseVideoFilterFragment extends BaseVideoPageFragment {

    protected List<ResourceData> mFilterDataList;
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
     * 获取滤镜数据
     * @param position
     * @return
     */
    public ResourceData getFilterData(int position) {
        if (mFilterDataList.size() <= position) {
            return null;
        }
        return mFilterDataList.get(position);
    }

}
