package com.cgfay.imagelibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.cgfay.filterlibrary.glfilter.resource.FilterHelper;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.widget.GLImageSurfaceView;
import com.cgfay.imagelibrary.R;
import com.cgfay.imagelibrary.adapter.ImageFilterAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜编辑页面
 */
public class ImageFilterFragment extends Fragment implements View.OnClickListener {

    private View mContentView;

    private Button mBtnInternal;
    private Button mBtnCustomize;
    private Button mBtnCollection;
    private Button mBtnAdd;
    private Button mBtnSetting;

    private FrameLayout mLayoutFilterContent;
    private GLImageSurfaceView mCainImageView;
    private RecyclerView mFiltersView;
    private LinearLayoutManager mLayoutManager;

    private Activity mActivity;

    private Bitmap mBitmap;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_image_filter, container, false);
        return mContentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(mContentView);
    }

    /**
     * 初始化视图
     * @param view
     */
    private void initView(View view) {
        // 图片内容布局
        mCainImageView = (GLImageSurfaceView) view.findViewById(R.id.glImageView);
        if (mBitmap != null) {
            mCainImageView.setBitmap(mBitmap);
        }
        // 滤镜内容框
        mLayoutFilterContent = (FrameLayout) view.findViewById(R.id.layout_filter_content);
        mBtnInternal = (Button) view.findViewById(R.id.btn_internal);
        mBtnCustomize = (Button) view.findViewById(R.id.btn_customize);
        mBtnCollection = (Button) view.findViewById(R.id.btn_collection);
        mBtnAdd = (Button) view.findViewById(R.id.btn_add);
        mBtnSetting = (Button) view.findViewById(R.id.btn_setting);
        showFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCainImageView != null) {
            mCainImageView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCainImageView != null) {
            mCainImageView.onPause();
        }
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
        if (id == R.id.btn_internal) {

        } else if (id == R.id.btn_customize) {

        } else if (id == R.id.btn_collection) {

        } else if (id == R.id.btn_add) {

        } else if (id == R.id.btn_setting) {

        }
    }

    /**
     * 重置按钮颜色
     */
    private void resetButtonColor() {
        mBtnInternal.setTextColor(Color.WHITE);
        mBtnCustomize.setTextColor(Color.WHITE);
        mBtnCollection.setTextColor(Color.WHITE);
        mBtnAdd.setTextColor(Color.WHITE);
        mBtnSetting.setTextColor(Color.WHITE);
    }

    /**
     * 显示滤镜列表
     */
    private void showFilters() {
        resetButtonColor();
        mBtnInternal.setTextColor(Color.BLUE);
        if (mFiltersView == null) {
            mFiltersView = new RecyclerView(mActivity);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mFiltersView.setLayoutManager(mLayoutManager);
            ImageFilterAdapter adapter = new ImageFilterAdapter(mActivity, FilterHelper.getFilterList());
            mFiltersView.setAdapter(adapter);
            adapter.addOnFilterChangeListener(new ImageFilterAdapter.OnFilterChangeListener() {
                @Override
                public void onFilterChanged(final ResourceData resourceData) {
                    if (mCainImageView != null) {
                        mCainImageView.setFilter(resourceData);
                    }
                }
            });
        }
        if (mLayoutFilterContent != null) {
            mLayoutFilterContent.removeAllViews();
            mLayoutFilterContent.addView(mFiltersView);
        }
    }

    /**
     * 设置bitmap
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (mCainImageView != null) {
            mCainImageView.setBitmap(mBitmap);
        }
    }

    /**
     * 是否显示GLSurfaceView，解决多重fragment时显示问题
     * @param showing
     */
    public void showGLSurfaceView(boolean showing) {
        if (mCainImageView != null) {
            mCainImageView.setVisibility(showing ? View.VISIBLE : View.GONE);
        }
    }
}
