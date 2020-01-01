package com.cgfay.camera.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cgfay.camera.adapter.PreviewResourceAdapter;
import com.cgfay.camera.adapter.RecyclerViewPagerAdapter;
import com.cgfay.cameralibrary.R;
import com.cgfay.filter.glfilter.resource.ResourceHelper;
import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.filter.glfilter.resource.bean.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 贴纸资源页面
 */
public class PreviewResourceFragment extends Fragment {

    public static final String TAG = "PreviewResourceFragment";

    // 无资源数据
    private static final ResourceData mNoneResource = new ResourceData("none",
            "assets://resource/none.zip", ResourceType.NONE,
            "none", "assets://thumbs/resource/none.png");

    private Context mContext;

    // 内容显示列表
    private View mContentView;

    private ImageView mResourceNone;
    private TabLayout mResourceTabLayout;
    private ViewPager mResourceViewPager;
    private List<RecyclerView> mResourceViewList = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_preview_resource, container, false);
        initView(mContentView);
        return mContentView;
    }

    private void initView(View view) {
        mResourceNone = view.findViewById(R.id.iv_resource_none);
        mResourceNone.setOnClickListener(v -> {
            resourceUnSelected();
            if (mOnResourceChangeListener != null) {
                mOnResourceChangeListener.onResourceChange(mNoneResource);
            }
        });
        mResourceViewPager = view.findViewById(R.id.vp_resource);
        mResourceTabLayout = view.findViewById(R.id.tl_resource_type);
        mResourceTabLayout.setupWithViewPager(mResourceViewPager);
        initResourceViewList();
    }

    /**
     * 初始化资源列表
     */
    private void initResourceViewList() {
        mResourceViewList.clear();

        RecyclerView recyclerView = new RecyclerView(mContext);
        GridLayoutManager manager = new GridLayoutManager(mContext, 5);
        recyclerView.setLayoutManager(manager);
        PreviewResourceAdapter adapter = new PreviewResourceAdapter(mContext, ResourceHelper.getResourceList());
        recyclerView.setAdapter(adapter);
        adapter.setOnResourceChangeListener(resourceData -> {
            if (mOnResourceChangeListener != null) {
                mOnResourceChangeListener.onResourceChange(resourceData);
            }
        });
        mResourceViewList.add(recyclerView);
        mResourceViewPager.setAdapter(new RecyclerViewPagerAdapter(mResourceViewList));
    }

    private void resourceUnSelected() {
        for(RecyclerView recyclerView : mResourceViewList) {
            if (recyclerView.getAdapter() instanceof PreviewResourceAdapter) {
                ((PreviewResourceAdapter) recyclerView.getAdapter()).unSelect();
            }
        }
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        mOnResourceChangeListener = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    /**
     * 资源切换监听器
     */
    public interface OnResourceChangeListener {

        /** 切换资源 */
        void onResourceChange(ResourceData data);
    }

    /**
     * 添加资源切换监听器
     * @param listener
     */
    public void addOnChangeResourceListener(OnResourceChangeListener listener) {
        mOnResourceChangeListener = listener;
    }

    private OnResourceChangeListener mOnResourceChangeListener;

}
