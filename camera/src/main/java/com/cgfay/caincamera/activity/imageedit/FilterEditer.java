package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.adapter.EffectFilterAdapter;
import com.cgfay.cainfilter.camerarender.ColorFilterManager;
import com.cgfay.utilslibrary.AsyncRecyclerview;

/**
 * 特效编辑器
 * Created by Administrator on 2018/3/12.
 */

public class FilterEditer extends BaseEditer {

    private static final String TAG = "FilterEditer";
    private static final boolean VERBOSE = true;

    // 特效列表
    private AsyncRecyclerview mFilterListView;
    private LinearLayoutManager mFilterListManager;
    private int mColorIndex = 0;

    public FilterEditer(Context context) {
        super(context);
    }

    /**
     * 初始化特效列表
     */
    protected void initView() {
        // 滤镜列表
        mFilterListView = (AsyncRecyclerview) mInflater
                .inflate(R.layout.view_video_edit_filters, null);
        mFilterListManager = new LinearLayoutManager(mContext);
        mFilterListManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(mFilterListManager);
        // TODO 滤镜适配器
        EffectFilterAdapter adapter = new EffectFilterAdapter(mContext,
                ColorFilterManager.getInstance().getFilterType(),
                ColorFilterManager.getInstance().getFilterName());

        mFilterListView.setAdapter(adapter);
        adapter.addItemClickListener(new EffectFilterAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(int position) {
                mColorIndex = position;
                if (VERBOSE) {
                    Log.d("changeFilter", "index = " + mColorIndex + ", filter name = "
                            + ColorFilterManager.getInstance().getColorFilterName(mColorIndex));
                }
            }
        });
    }

    /**
     * 获取滤镜列表
     * @return
     */
    public AsyncRecyclerview getFilterListView() {
        return mFilterListView;
    }
}
