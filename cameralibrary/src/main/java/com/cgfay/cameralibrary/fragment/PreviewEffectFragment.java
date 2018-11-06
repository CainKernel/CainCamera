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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.adapter.PreviewBeautyAdapter;
import com.cgfay.cameralibrary.adapter.PreviewFilterAdapter;
import com.cgfay.cameralibrary.adapter.PreviewMakeupAdapter;
import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.render.PreviewRenderer;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.resource.FilterHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

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
    private RelativeLayout mLayoutBeauty;
    private RecyclerView mBeautyRecyclerView;
    private LinearLayoutManager mBeautyLayoutManager;
    private PreviewBeautyAdapter mBeautyAdapter;
    private Button mBtnReset;

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

    // 相机参数
    private CameraParam mCameraParam;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mInflater = LayoutInflater.from(mActivity);
        mCameraParam = CameraParam.getInstance();
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
                processBeautyParam(mBeautyAdapter.getSelectedPosition(), progress);
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

    // -------------------------------------- 美颜(beauty) ----------------------------------------
    /**
     * 显示美颜视图布局
     */
    private void showBeautyLayout() {
        mLayoutProgress.setVisibility(View.VISIBLE);
        if (mLayoutBeauty == null) {
            mLayoutBeauty = (RelativeLayout) mInflater.inflate(R.layout.view_preview_beauty, null);
            mBeautyRecyclerView = (RecyclerView) mLayoutBeauty.findViewById(R.id.preview_beauty_list);
            mBeautyLayoutManager = new LinearLayoutManager(mActivity);
            mBeautyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mBeautyRecyclerView.setLayoutManager(mBeautyLayoutManager);
            mBeautyAdapter = new PreviewBeautyAdapter(mActivity);
            mBeautyRecyclerView.setAdapter(mBeautyAdapter);
            mBeautyAdapter.addOnBeautySelectedListener(new PreviewBeautyAdapter.OnBeautySelectedListener() {
                @Override
                public void onBeautySelected(int position, String beautyName) {
                    setSeekBarBeautyParam(position);
                }
            });
            mBtnReset = (Button) mLayoutBeauty.findViewById(R.id.btn_beauty_reset);
            mBtnReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCameraParam.beauty.reset();
                    setSeekBarBeautyParam(mBeautyAdapter.getSelectedPosition());
                }
            });
        }
        setSeekBarBeautyParam(mBeautyAdapter.getSelectedPosition());

        mLayoutContent.removeAllViews();
        mLayoutContent.addView(mLayoutBeauty);
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
            mValueSeekBar.setProgress((int)((1.0f + mCameraParam.beauty.eyeEnlargeIntensity) * 50));
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
            mCameraParam.beauty.eyeEnlargeIntensity = (progress - 50.0f) / 50.0f;
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
            mBtnLiquefaction.setBackgroundResource(mCameraParam.enableDepthBlur
                    ? R.drawable.ic_camera_blur_selected
                    : R.drawable.ic_camera_blur_normal);
            mBtnLiquefaction.setOnClickListener(this);
            mBtnVignette = (Button) mLayoutFilter.findViewById(R.id.btn_vignette);
            mBtnVignette.setBackgroundResource(mCameraParam.enableVignette
                    ? R.drawable.ic_camera_vignette_selected
                    : R.drawable.ic_camera_vignette_normal);
            mBtnVignette.setOnClickListener(this);
            mFilterRecyclerView = (RecyclerView) mLayoutFilter.findViewById(R.id.preview_filter_list);
            mFilterLayoutManager = new LinearLayoutManager(mActivity);
            mFilterLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mFilterRecyclerView.setLayoutManager(mFilterLayoutManager);
            mFilterAdapter = new PreviewFilterAdapter(mActivity,
                    FilterHelper.getFilterList());
            mFilterRecyclerView.setAdapter(mFilterAdapter);
            mFilterAdapter.setOnFilterChangeListener(new PreviewFilterAdapter.OnFilterChangeListener() {
                @Override
                public void onFilterChanged(ResourceData resourceData) {
                    if (!resourceData.name.equals("none")) {
                        String folderPath = FilterHelper.getFilterDirectory(mActivity) + File.separator + resourceData.unzipFolder;
                        DynamicColor color = null;
                        try {
                            color = ResourceJsonCodec.decodeFilterData(folderPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        PreviewRenderer.getInstance().changeDynamicFilter(color);
                    } else {
                        PreviewRenderer.getInstance().changeDynamicFilter(null);
                    }
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
