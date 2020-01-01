package com.cgfay.image.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.filter.widget.GLImageSurfaceView;
import com.cgfay.imagelibrary.R;
import com.cgfay.image.activity.ImagePreviewActivity;
import com.cgfay.image.adapter.ImageFilterAdapter;
import com.cgfay.uitls.utils.BitmapUtils;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * 滤镜编辑页面
 */
public class ImageFilterFragment extends Fragment implements View.OnClickListener {

    private View mContentView;

    private Button mBtnInternal;
    private Button mBtnCustomize;
    private Button mBtnCollection;
    private Button mBtnSave;
    private Button mBtnAdd;
    private Button mBtnSetting;

    private FrameLayout mLayoutFilterContent;
    private GLImageSurfaceView mCainImageView;
    private RecyclerView mFiltersView;
    private LinearLayoutManager mLayoutManager;

    private Activity mActivity;
    private Handler mMainHandler;

    private Bitmap mBitmap;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mMainHandler = new Handler(Looper.getMainLooper());
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
        mCainImageView.setCaptureCallback(mCaptureCallback);
        if (mBitmap != null) {
            mCainImageView.setBitmap(mBitmap);
        }
        // 滤镜内容框
        mLayoutFilterContent = (FrameLayout) view.findViewById(R.id.layout_filter_content);
        mBtnInternal = (Button) view.findViewById(R.id.btn_internal);
        mBtnInternal.setOnClickListener(this);
        mBtnCustomize = (Button) view.findViewById(R.id.btn_customize);
        mBtnCustomize.setOnClickListener(this);
        mBtnCollection = (Button) view.findViewById(R.id.btn_collection);
        mBtnCollection.setOnClickListener(this);
        mBtnSave = (Button) view.findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(this);
        mBtnAdd = (Button) view.findViewById(R.id.btn_add);
        mBtnAdd.setOnClickListener(this);
        mBtnSetting = (Button) view.findViewById(R.id.btn_setting);
        mBtnSetting.setOnClickListener(this);
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

        } else if (id == R.id.btn_save) {
            if (mCainImageView != null) {
                mCainImageView.getCaptureFrame();
            }
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

    /**
     * 截屏回调
     */
    private GLImageSurfaceView.CaptureCallback mCaptureCallback = new GLImageSurfaceView.CaptureCallback() {
        @Override
        public void onCapture(final ByteBuffer buffer, final int width, final int height) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    String filePath = getDCIMImagePath(mActivity);
                    BitmapUtils.saveBitmap(filePath, buffer, width, height);
                    Log.d("hahaha", "run: " + filePath);
                    Intent intent = new Intent(mActivity, ImagePreviewActivity.class);
                    intent.putExtra(ImagePreviewActivity.PATH, filePath);
                    startActivity(intent);
                }
            });
        }
    };

    /**
     * 获取图片缓存绝对路径
     * @param context
     * @return
     */
    private static String getDCIMImagePath(Context context) {
        String directoryPath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        } else { // 使用内部存储缓存目录
            directoryPath = context.getCacheDir().getAbsolutePath();
        }
        String path = directoryPath + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "CainCamera_" + System.currentTimeMillis() + ".jpeg";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }
}
