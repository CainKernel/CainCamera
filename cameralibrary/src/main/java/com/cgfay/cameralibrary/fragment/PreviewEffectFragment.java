package com.cgfay.cameralibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.adapter.PreviewBeautyAdapter;
import com.cgfay.cameralibrary.adapter.PreviewFilterAdapter;
import com.cgfay.cameralibrary.adapter.PreviewMakeupAdapter;
import com.cgfay.cameralibrary.engine.render.PreviewRenderer;
import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;

/**
 * 特效选择页面
 */
public class PreviewEffectFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "FilterEditedFragment";
    private static final boolean VERBOSE = true;

    // 标题选择索引，0表示美颜，1表轻美妆，3表示滤镜
    private int mTitleButtonIndex = 0;

    // 当前滤镜索引
    private int mCurrentFilterIndex = 0;

    // 内容显示列表
    private View mContentView;

    // 数值调整布局
    private LinearLayout mLayoutProgress;
    private TextView mTypeValueView;
    private SeekBar mValueSeekBar;

    // 美型
    private Button mBtnBeauty;
    // 美妆
    private Button mBtnMakeup;
    // 滤镜
    private Button mBtnFilter;

    // 内容栏
    private LinearLayout mLayoutContent;

    // 美颜列表
    private LinearLayout mLayoutBeauty;
    private RecyclerView mBeautyRecyclerView;
    private LinearLayoutManager mBeautyLayoutManager;
    private PreviewBeautyAdapter mBeautyAdapter;

    // 美妆列表
    private LinearLayout mLayoutMakeup;
    private RecyclerView mMakeupRecyclerView;
    private LinearLayoutManager mMakeupLayoutManager;
    private PreviewMakeupAdapter mMakeupAdapter;

    // 滤镜列表
    private LinearLayout mLayoutFilter;
    private Button mBtnLiquefaction;
    private Button mBtnVignette;
    private RecyclerView mFilterRecyclerView;
    private LinearLayoutManager mFilterLayoutManager;
    private PreviewFilterAdapter mFilterAdapter;

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
        mContentView = inflater.inflate(R.layout.fragment_filter_edit, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(mContentView);
    }

    /**
     * 初始化页面
     * @param view
     */
    private void initView(View view) {

        // 数值布局
        mLayoutProgress = (LinearLayout) view.findViewById(R.id.layout_progress);
        mTypeValueView = (TextView) view.findViewById(R.id.tv_type_value);
        mValueSeekBar = (SeekBar) view.findViewById(R.id.value_progress);
        mValueSeekBar.setMax(100);
        mValueSeekBar.setOnSeekBarChangeListener(this);

        // 内容栏
        mLayoutContent = (LinearLayout) view.findViewById(R.id.layout_content);

        // 标题按钮
        mBtnBeauty = (Button) view.findViewById(R.id.btn_preview_beauty);
        mBtnFilter = (Button) view.findViewById(R.id.btn_preview_filter);
        mBtnMakeup = (Button) view.findViewById(R.id.btn_preview_makeup);

        mBtnBeauty.setOnClickListener(this);
        mBtnFilter.setOnClickListener(this);
        mBtnMakeup.setOnClickListener(this);

        // 显示默认内容布局
        showContentLayout(mTitleButtonIndex);
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


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_preview_beauty) {            // 美型
            showContentLayout(0);
        } else if (id == R.id.btn_preview_makeup) {     // 美妆
            showContentLayout(1);
        } else if (id == R.id.btn_preview_filter) {     // 滤镜
            showContentLayout(2);
        } else if (id == R.id.btn_liquefaction) {   // 景深/液化
            processDepthBlur();
        } else if (id == R.id.btn_vignette) {   // 暗角
            processVignette();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (mTitleButtonIndex == 0) { // 美颜
                if (mBeautyAdapter.getSelectedPosition() == 0) {    // 磨皮
                    PreviewRenderer.getInstance().setBeautyIntensity(progress / 100.0f);
                } else if (mBeautyAdapter.getSelectedPosition() == 1) { // 美肤
                    PreviewRenderer.getInstance().setComplexionIntensity(progress / 100.0f);
                }
            } else if (mTitleButtonIndex == 1) { // 彩妆

            } else if (mTitleButtonIndex == 2) { // 滤镜

            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 显示内容布局
     * @param index
     */
    private void showContentLayout(int index) {
        mTitleButtonIndex = index;
        resetLayout();
        if (index == 0) {
            showBeautyLayout();
        } else if (index == 1) {
            showMakeupLayout();
        } else if (index == 2) {
            showFilterLayout();
        }
    }

    /**
     * 重置布局
     */
    private void resetLayout() {
        // 重置标题
        mBtnBeauty.setBackgroundColor(mTitleButtonIndex == 0 ? Color.DKGRAY : Color.TRANSPARENT);
        mBtnMakeup.setBackgroundColor(mTitleButtonIndex == 1 ? Color.DKGRAY : Color.TRANSPARENT);
        mBtnFilter.setBackgroundColor(mTitleButtonIndex == 2 ? Color.DKGRAY : Color.TRANSPARENT);
        mLayoutProgress.setVisibility(View.GONE);
    }

    /**
     * 处理景深/液化
     */
    private void processDepthBlur() {
        PreviewRenderer.getInstance().enableDepthBlur(!PreviewRenderer.getInstance().enableDepthBlur());
        mBtnLiquefaction.setBackgroundResource(PreviewRenderer.getInstance().enableDepthBlur()
                ? R.drawable.ic_camera_blur_selected
                : R.drawable.ic_camera_blur_normal);
    }

    /**
     * 处理暗角
     */
    private void processVignette() {
        PreviewRenderer.getInstance().enableVignette(!PreviewRenderer.getInstance().enableVignette());
        mBtnVignette.setBackgroundResource(PreviewRenderer.getInstance().enableVignette()
                ? R.drawable.ic_camera_vignette_selected
                : R.drawable.ic_camera_vignette_normal);
    }

    // -------------------------------------- 美颜(beauty) ----------------------------------------
    /**
     * 显示美颜视图布局
     */
    private void showBeautyLayout() {
        mLayoutProgress.setVisibility(View.VISIBLE);
        if (mLayoutBeauty == null) {
            mLayoutBeauty = (LinearLayout) mInflater.inflate(R.layout.view_preview_beauty, null);
            mBeautyRecyclerView = (RecyclerView) mLayoutBeauty.findViewById(R.id.preview_beauty_list);
            mBeautyLayoutManager = new LinearLayoutManager(mActivity);
            mBeautyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mBeautyRecyclerView.setLayoutManager(mBeautyLayoutManager);
            mBeautyAdapter = new PreviewBeautyAdapter(mActivity);
            mBeautyRecyclerView.setAdapter(mBeautyAdapter);
            mBeautyAdapter.addOnBeautySelectedListener(new PreviewBeautyAdapter.OnBeautySelectedListener() {
                @Override
                public void onBeautySelected(int position, String beautyName) {
                    Log.d(TAG, "onBeautySelected: position = " + position + ", name = " + beautyName);
                    processBeautyParam(position);
                }
            });
        }
        processBeautyParam(mBeautyAdapter.getSelectedPosition());

        mLayoutContent.removeAllViews();
        mLayoutContent.addView(mLayoutBeauty);
    }

    /**
     * 处理美颜参数
     */
    private void processBeautyParam(int position) {
        if (position == 0) { // 磨皮
            mValueSeekBar.setProgress((int)(PreviewRenderer.getInstance().getBeautyIntensity() * 100));
        } else if (position == 1) { // 肤色
            mValueSeekBar.setProgress((int) (PreviewRenderer.getInstance().getComplexionIntensity() * 100));
        }
    }

    // -------------------------------------- 美妆(makeup) ----------------------------------------
    /**
     * 显示美妆布局
     */
    private void showMakeupLayout() {
        if (mLayoutMakeup == null) {
            mLayoutMakeup = (LinearLayout) mInflater.inflate(R.layout.view_preview_makeup, null);
            mMakeupRecyclerView = (RecyclerView) mLayoutMakeup.findViewById(R.id.preview_makeup_list);
            mMakeupLayoutManager = new LinearLayoutManager(mActivity);
            mMakeupLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mMakeupRecyclerView.setLayoutManager(mMakeupLayoutManager);
            mMakeupAdapter = new PreviewMakeupAdapter(mActivity);
            mMakeupRecyclerView.setAdapter(mMakeupAdapter);
            mMakeupAdapter.addOnMakeupSelectedListener(new PreviewMakeupAdapter.OnMakeupSelectedListener() {
                @Override
                public void onMakeupSelected(int position, String makeupName) {
                    Log.d(TAG, "onMakeupSelected: position = " + position + ", name = " + makeupName);
                }
            });
        }
        mLayoutContent.removeAllViews();
        mLayoutContent.addView(mLayoutMakeup);
    }

    // -------------------------------------- 滤镜(filter) ----------------------------------------
    /**
     * 显示滤镜布局
     */
    private void showFilterLayout() {
        if (mLayoutFilter == null) {
            mLayoutFilter = (LinearLayout) mInflater.inflate(R.layout.view_preview_filter, null);
            mBtnLiquefaction = (Button) mLayoutFilter.findViewById(R.id.btn_liquefaction);
            mBtnLiquefaction.setBackgroundResource(PreviewRenderer.getInstance().enableDepthBlur()
                    ? R.drawable.ic_camera_blur_selected
                    : R.drawable.ic_camera_blur_normal);
            mBtnLiquefaction.setOnClickListener(this);
            mBtnVignette = (Button) mLayoutFilter.findViewById(R.id.btn_vignette);
            mBtnVignette.setBackgroundResource(PreviewRenderer.getInstance().enableVignette()
                    ? R.drawable.ic_camera_vignette_selected
                    : R.drawable.ic_camera_vignette_normal);
            mBtnVignette.setOnClickListener(this);
            mFilterRecyclerView = (RecyclerView) mLayoutFilter.findViewById(R.id.preview_filter_list);
            mFilterLayoutManager = new LinearLayoutManager(mActivity);
            mFilterLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mFilterRecyclerView.setLayoutManager(mFilterLayoutManager);
            mFilterAdapter = new PreviewFilterAdapter(mActivity,
                    GLImageFilterManager.getFilterTypes(),
                    GLImageFilterManager.getFilterNames());
            mFilterRecyclerView.setAdapter(mFilterAdapter);
            mFilterAdapter.setOnFilterChangeListener(new PreviewFilterAdapter.OnFilterChangeListener() {
                @Override
                public void onFilterChanged(GLImageFilterType type) {
                    PreviewRenderer.getInstance().changeFilterType(type);
                    mCurrentFilterIndex = mFilterAdapter.getSelectedPosition();
                }
            });
            if (mCurrentFilterIndex != mFilterAdapter.getSelectedPosition()) {
                scrollToCurrentFilter(mCurrentFilterIndex);
            }
        }
        mLayoutContent.removeAllViews();
        mLayoutContent.addView(mLayoutFilter);
    }

    /**
     * 滚动到选中的滤镜位置上
     * @param index
     */
    public void scrollToCurrentFilter(int index) {
        if (mFilterRecyclerView != null) {
            int firstItem = mFilterLayoutManager.findFirstVisibleItemPosition();
            int lastItem = mFilterLayoutManager.findLastVisibleItemPosition();
            if (index <= firstItem) {
                mFilterRecyclerView.scrollToPosition(index);
            } else if (index <= lastItem) {
                int top = mFilterRecyclerView.getChildAt(index - firstItem).getTop();
                mFilterRecyclerView.scrollBy(0, top);
            } else {
                mFilterRecyclerView.scrollToPosition(index);
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

}
