package com.cgfay.camera.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.camera.adapter.EffectViewPagerAdapter;
import com.cgfay.cameralibrary.R;
import com.cgfay.camera.adapter.PreviewBeautyAdapter;
import com.cgfay.camera.adapter.PreviewFilterAdapter;
import com.cgfay.camera.adapter.PreviewMakeupAdapter;
import com.cgfay.camera.camera.CameraParam;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.MakeupHelper;
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 特效选择页面
 */
public class PreviewEffectFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    // 滤镜索引
    private int mFilterIndex = 0;

    // 内容显示列表
    private View mContentView;

    // 对比按钮
    private Button mBtnCompare;

    // 数值调整布局
    private LinearLayout mLayoutProgress;
    private TextView mTypeValueView;
    private SeekBar mValueSeekBar;

    // 特效类型
    private TabLayout mEffectTabLayout;
    // 特效ViewPager
    private ViewPager mEffectViewPager;
    // 特效页面列表
    private List<View> mEffectViewLists = new ArrayList<>();
    // 特效标题列表
    private List<String> mEffectTitleLists = new ArrayList<>();

    // 美颜列表
    private RecyclerView mBeautyRecyclerView;
    private LinearLayoutManager mBeautyLayoutManager;
    private PreviewBeautyAdapter mBeautyAdapter;
    private Button mBtnReset;

    // 美妆列表
    private RecyclerView mMakeupRecyclerView;
    private LinearLayoutManager mMakeupLayoutManager;
    private PreviewMakeupAdapter mMakeupAdapter;

    // 滤镜列表
    private Button mBtnLiquefaction;
    private Button mBtnVignette;
    private RecyclerView mFilterRecyclerView;
    private LinearLayoutManager mFilterLayoutManager;
    private PreviewFilterAdapter mFilterAdapter;

    // 布局管理器
    private LayoutInflater mInflater;
    private Activity mActivity;

    // 相机参数
    private CameraParam mCameraParam;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            mActivity = getActivity();
        }
        mInflater = LayoutInflater.from(context);
        mCameraParam = CameraParam.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_filter_edit, container, false);
        initView(mContentView);
        return mContentView;
    }

    /**
     * 初始化页面
     * @param view
     */
    private void initView(View view) {
        initCompareButton(view);
        initBeautyProgress(view);
        initViewList(view);
    }

    /**
     * 初始化比较滤镜
     * @param view
     */
    private void initCompareButton(@NonNull View view) {
        mBtnCompare = view.findViewById(R.id.btn_compare);
        mBtnCompare.setOnTouchListener((v, event)-> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mCompareEffectListener != null) {
                        mCompareEffectListener.onCompareEffect(true);
                    }
                    mBtnCompare.setBackgroundResource(R.drawable.ic_camera_compare_pressed);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mCompareEffectListener != null) {
                        mCompareEffectListener.onCompareEffect(false);
                    }
                    mBtnCompare.setBackgroundResource(R.drawable.ic_camera_compare_normal);
                    break;
            }
            return true;
        });
    }

    private void initBeautyProgress(@NonNull View view) {
        // 数值布局
        mLayoutProgress = view.findViewById(R.id.layout_progress);
        mTypeValueView = view.findViewById(R.id.tv_type_value);
        mValueSeekBar = view.findViewById(R.id.value_progress);
        mValueSeekBar.setMax(100);
        mValueSeekBar.setOnSeekBarChangeListener(this);
    }

    /**
     * 初始化列表
     * @param view
     */
    private void initViewList(@NonNull View view) {
        mEffectViewLists.clear();
        mEffectTitleLists.clear();

        addBeautyView();
        addMakeupView();
        addFilterView();

        mEffectViewPager = view.findViewById(R.id.vp_effect);
        mEffectTabLayout = view.findViewById(R.id.tl_effect_type);
        mEffectViewPager.setAdapter(new EffectViewPagerAdapter(mEffectViewLists, mEffectTitleLists));
        mEffectTabLayout.setupWithViewPager(mEffectViewPager);
        mEffectTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mLayoutProgress.setVisibility(tab.getPosition() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * 显示美颜视图布局
     */
    private void addBeautyView() {
        mLayoutProgress.setVisibility(View.VISIBLE);
        View beautyView = mInflater.inflate(R.layout.view_preview_beauty, null);
        mBeautyRecyclerView = beautyView.findViewById(R.id.preview_beauty_list);
        mBeautyLayoutManager = new LinearLayoutManager(mActivity);
        mBeautyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBeautyRecyclerView.setLayoutManager(mBeautyLayoutManager);
        mBeautyAdapter = new PreviewBeautyAdapter(mActivity);
        mBeautyRecyclerView.setAdapter(mBeautyAdapter);
        mBeautyAdapter.addOnBeautySelectedListener((position, beautyName) -> setSeekBarBeautyParam(position));
        mBtnReset = beautyView.findViewById(R.id.btn_beauty_reset);
        mBtnReset.setOnClickListener(v -> {
            mCameraParam.beauty.reset();
            setSeekBarBeautyParam(mBeautyAdapter.getSelectedPosition());
        });
        setSeekBarBeautyParam(mBeautyAdapter.getSelectedPosition());
        mEffectViewLists.add(beautyView);
        mEffectTitleLists.add(getResources().getString(R.string.tab_preview_beauty));
    }

    /**
     * 显示美妆布局
     */
    private void addMakeupView() {
        View makeupView = mInflater.inflate(R.layout.view_preview_makeup, null);
        mMakeupRecyclerView = makeupView.findViewById(R.id.preview_makeup_list);
        mMakeupLayoutManager = new LinearLayoutManager(mActivity);
        mMakeupLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mMakeupRecyclerView.setLayoutManager(mMakeupLayoutManager);
        mMakeupAdapter = new PreviewMakeupAdapter(mActivity);
        mMakeupRecyclerView.setAdapter(mMakeupAdapter);
        mMakeupAdapter.addOnMakeupSelectedListener((position, makeupName) -> {
            if (position == 0) {
                String folderPath = MakeupHelper.getMakeupDirectory(mActivity) + File.separator +
                        MakeupHelper.getMakeupList().get(1).unzipFolder;
                DynamicMakeup makeup = null;
                try {
                    makeup = ResourceJsonCodec.decodeMakeupData(folderPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mOnMakeupChangeListener != null) {
                    mOnMakeupChangeListener.onMakeupChange(makeup);
                }
            } else {
                if (mOnMakeupChangeListener != null) {
                    mOnMakeupChangeListener.onMakeupChange(null);
                }
            }
        });
        mEffectViewLists.add(makeupView);
        mEffectTitleLists.add(getResources().getString(R.string.tab_preview_makeup));
    }

    /**
     * 显示滤镜布局
     */
    private void addFilterView() {
        View filterView = mInflater.inflate(R.layout.view_preview_filter, null);
        mBtnLiquefaction = filterView.findViewById(R.id.btn_liquefaction);
        mBtnLiquefaction.setBackgroundResource(mCameraParam.enableDepthBlur
                ? R.drawable.ic_camera_blur_selected
                : R.drawable.ic_camera_blur_normal);
        mBtnLiquefaction.setOnClickListener(this);
        mBtnVignette = filterView.findViewById(R.id.btn_vignette);
        mBtnVignette.setBackgroundResource(mCameraParam.enableVignette
                ? R.drawable.ic_camera_vignette_selected
                : R.drawable.ic_camera_vignette_normal);
        mBtnVignette.setOnClickListener(this);
        mFilterRecyclerView = filterView.findViewById(R.id.preview_filter_list);
        mFilterLayoutManager = new LinearLayoutManager(mActivity);
        mFilterLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterRecyclerView.setLayoutManager(mFilterLayoutManager);
        mFilterAdapter = new PreviewFilterAdapter(mActivity,
                FilterHelper.getFilterList());
        mFilterRecyclerView.setAdapter(mFilterAdapter);
        mFilterAdapter.setOnFilterChangeListener(resourceData -> {
            if (mActivity == null) {
                return;
            }
            if (!resourceData.name.equals("none")) {
                String folderPath = FilterHelper.getFilterDirectory(mActivity) + File.separator + resourceData.unzipFolder;
                DynamicColor color = null;
                try {
                    color = ResourceJsonCodec.decodeFilterData(folderPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mOnFilterChangeListener != null) {
                    mOnFilterChangeListener.onFilterChange(color);
                }
            } else {
                if (mOnFilterChangeListener != null) {
                    mOnFilterChangeListener.onFilterChange(null);
                }
            }
            scrollToCurrentFilter(mFilterAdapter.getSelectedPosition());
        });
        scrollToCurrentFilter(mFilterIndex);
        mEffectViewLists.add(filterView);
        mEffectTitleLists.add(getResources().getString(R.string.tab_preview_filter));
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        mOnFilterChangeListener = null;
        mOnMakeupChangeListener = null;
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
        if (id == R.id.btn_liquefaction) {   // 景深/液化
            processDepthBlur();
        } else if (id == R.id.btn_vignette) {   // 暗角
            processVignette();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (mEffectTabLayout.getSelectedTabPosition() == 0) { // 美颜
                processBeautyParam(mBeautyAdapter.getSelectedPosition(), progress);
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
     * 处理景深/液化
     */
    private void processDepthBlur() {
        mCameraParam.enableDepthBlur = !mCameraParam.enableDepthBlur;
        mBtnLiquefaction.setBackgroundResource(mCameraParam.enableDepthBlur
                ? R.drawable.ic_camera_blur_selected
                : R.drawable.ic_camera_blur_normal);
    }

    /**
     * 处理暗角
     */
    private void processVignette() {
        mCameraParam.enableVignette = !mCameraParam.enableVignette;
        mBtnVignette.setBackgroundResource(mCameraParam.enableVignette
                ? R.drawable.ic_camera_vignette_selected
                : R.drawable.ic_camera_vignette_normal);
    }

    /**
     * 设置Seekbar对应的美颜参数值
     */
    private void setSeekBarBeautyParam(int position) {
        if (position == 0) {            // 磨皮
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.beautyIntensity * 100));
        } else if (position == 1) {     // 肤色
            mValueSeekBar.setProgress((int) (mCameraParam.beauty.complexionIntensity * 100));
        } else if (position == 2) {     // 瘦脸
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.faceLift * 100));
        } else if (position == 3) {     // 削脸
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.faceShave * 100));
        } else if (position == 4) {     // 小脸
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.faceNarrow * 100));
        } else if (position == 5) {     // 下巴
            mValueSeekBar.setProgress((int)((1.0f + mCameraParam.beauty.chinIntensity) * 50));
        } else if (position == 6) {     // 法令纹
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.nasolabialFoldsIntensity * 100));
        } else if (position == 7) {     // 额头
            mValueSeekBar.setProgress((int)((1.0f + mCameraParam.beauty.foreheadIntensity) * 50));
        } else if (position == 8) {     // 大眼
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.eyeEnlargeIntensity * 100));
        } else if (position == 9) {     // 眼距
            mValueSeekBar.setProgress((int)((1.0f + mCameraParam.beauty.eyeDistanceIntensity) * 50));
        } else if (position == 10) {    // 眼角
            mValueSeekBar.setProgress((int)((1.0f + mCameraParam.beauty.eyeCornerIntensity) * 50));
        } else if (position == 11) {    // 卧蚕
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.eyeFurrowsIntensity * 100));
        } else if (position == 12) {    // 眼袋
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.eyeBagsIntensity * 100));
        } else if (position == 13) {    // 亮眼
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.eyeBrightIntensity * 100));
        } else if (position == 14) {    // 瘦鼻
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.noseThinIntensity * 100));
        } else if (position == 15) {    // 鼻翼
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.alaeIntensity * 100));
        } else if (position == 16) {    // 长鼻
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.proboscisIntensity * 100));
        } else if (position == 17) {    // 嘴型
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.mouthEnlargeIntensity * 100));
        } else if (position == 18) {    // 美牙
            mValueSeekBar.setProgress((int)(mCameraParam.beauty.teethBeautyIntensity * 100));
        }
    }

    /**
     * 处理美颜参数
     * @param progress
     */
    private void processBeautyParam(int position, int progress) {
        if (position == 0) {            // 磨皮
            mCameraParam.beauty.beautyIntensity = progress / 100.0f;
        } else if (position == 1) {     // 肤色
            mCameraParam.beauty.complexionIntensity = progress / 100.0f;
        } else if (position == 2) {     // 瘦脸
            mCameraParam.beauty.faceLift = progress / 100.0f;
        } else if (position == 3) {     // 削脸
            mCameraParam.beauty.faceShave = progress / 100.0f;
        } else if (position == 4) {     // 小脸
            mCameraParam.beauty.faceNarrow = progress / 100.0f;
        } else if (position == 5) {     // 下巴
            mCameraParam.beauty.chinIntensity = (progress - 50.0f) / 50.0f;
        } else if (position == 6) {     // 法令纹
            mCameraParam.beauty.nasolabialFoldsIntensity = progress / 100.0f;
        } else if (position == 7) {     // 额头
            mCameraParam.beauty.foreheadIntensity = (progress - 50.0f) / 50.0f;
        } else if (position == 8) {     // 大眼
            mCameraParam.beauty.eyeEnlargeIntensity = progress / 100.0f;
        } else if (position == 9) {     // 眼距
            mCameraParam.beauty.eyeDistanceIntensity = (progress - 50.0f) / 50.0f;
        } else if (position == 10) {    // 眼角
            mCameraParam.beauty.eyeCornerIntensity = (progress - 50.0f) / 50.0f;
        } else if (position == 11) {    // 卧蚕
            mCameraParam.beauty.eyeFurrowsIntensity = progress / 100.0f;
        } else if (position == 12) {    // 眼袋
            mCameraParam.beauty.eyeBagsIntensity = progress / 100.0f;
        } else if (position == 13) {    // 亮眼
            mCameraParam.beauty.eyeBrightIntensity = progress / 100.0f;
        } else if (position == 14) {    // 瘦鼻
            mCameraParam.beauty.noseThinIntensity = progress / 100.0f;
        } else if (position == 15) {    // 鼻翼
            mCameraParam.beauty.alaeIntensity = progress / 100.0f;
        } else if (position == 16) {    // 长鼻
            mCameraParam.beauty.proboscisIntensity = progress / 100.0f;
        } else if (position == 17) {    // 嘴型
            mCameraParam.beauty.mouthEnlargeIntensity = progress / 100.0f;
        } else if (position == 18) {    // 美牙
            mCameraParam.beauty.teethBeautyIntensity = progress / 100.0f;
        }
    }

    /**
     * 滚动到选中的滤镜位置上
     * @param index
     */
    public void scrollToCurrentFilter(int index) {
        mFilterIndex = index;
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
    }

    /**
     * 比较监听器
     */
    public interface OnCompareEffectListener {

        void onCompareEffect(boolean compare);
    }

    /**
     * 添加比较回调监听
     * @param listener
     */
    public void addOnCompareEffectListener(OnCompareEffectListener listener) {
        mCompareEffectListener = listener;
    }

    private OnCompareEffectListener mCompareEffectListener;

    /**
     * 滤镜切换监听
     */
    public interface OnFilterChangeListener {

        /** 滤镜切换监听器 */
        void onFilterChange(DynamicColor color);
    }

    /**
     * 添加滤镜监听器
     * @param listener
     */
    public void addOnFilterChangeListener(OnFilterChangeListener listener) {
        mOnFilterChangeListener = listener;
    }

    private OnFilterChangeListener mOnFilterChangeListener;

    /**
     * 彩妆切换监听
     */
    public interface OnMakeupChangeListener {

        /** 彩妆切换监听器 */
        void onMakeupChange(DynamicMakeup makeup);
    }

    /**
     * 添加彩妆切换监听器
     * @param listener
     */
    public void addOnMakeupChangeListener(OnMakeupChangeListener listener) {
        mOnMakeupChangeListener = listener;
    }
    private OnMakeupChangeListener mOnMakeupChangeListener;
}
