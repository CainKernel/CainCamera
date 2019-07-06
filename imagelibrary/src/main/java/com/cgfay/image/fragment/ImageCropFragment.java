package com.cgfay.image.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.imagelibrary.R;
import com.cgfay.image.widget.CropCoverView;

/**
 * 裁剪页面
 */
public class ImageCropFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "ImageCropFragment";
    private static final boolean VERBOSE = true;

    // fragment 页面内容
    private View mContentView;
    // 内容布局
    private FrameLayout mLayoutContent;
    // 图片显示控件
    private ImageView mImageView;
    // 裁剪控件
    private CropCoverView mCropCoverView;

    // 裁剪类型
    private TextView mCropTypeView;
    // 裁剪值
    private SeekBar mCropProgress;
    // 水平
    private Button mBtnHorizontal;
    // 长宽比
    private Button mBtnRatio;
    // 旋转
    private Button mBtnRotate;
    // 翻转
    private Button mBtnFlip;
    // 纵向透视
    private Button mBtnVPerspective;
    // 横向透视
    private Button mBtnHPerspective;
    // 拉伸
    private Button mBtnStretch;

    private Activity mActivity;

    private Bitmap mBitmap;
    private int mImageWidth;
    private int mImageHeight;
    private int mViewWidth;
    private int mViewHeight;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_image_crop, container, false);
        return mContentView;
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

        mLayoutContent = (FrameLayout) view.findViewById(R.id.layout_content);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap);
        mCropCoverView = (CropCoverView) view.findViewById(R.id.image_cop_view);
        mCropTypeView = (TextView) view.findViewById(R.id.crop_type);
        mCropProgress = (SeekBar) view.findViewById(R.id.crop_progress);
        mCropProgress.setOnSeekBarChangeListener(this);

        mBtnHorizontal = (Button) view.findViewById(R.id.btn_horizontal);
        mBtnRatio = (Button) view.findViewById(R.id.btn_ratio);
        mBtnRotate = (Button) view.findViewById(R.id.btn_rotate);
        mBtnFlip = (Button) view.findViewById(R.id.btn_flip);
        mBtnVPerspective = (Button) view.findViewById(R.id.btn_vertical_perspective);
        mBtnHPerspective = (Button) view.findViewById(R.id.btn_horizontal_perspective);
        mBtnStretch = (Button) view.findViewById(R.id.btn_stretch);

        mBtnHorizontal.setOnClickListener(this);
        mBtnRatio.setOnClickListener(this);
        mBtnRotate.setOnClickListener(this);
        mBtnFlip.setOnClickListener(this);
        mBtnVPerspective.setOnClickListener(this);
        mBtnHPerspective.setOnClickListener(this);
        mBtnStretch.setOnClickListener(this);
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_horizontal) {
            mCropTypeView.setText(mActivity.getString(R.string.btn_horizontal));
        } else if (id == R.id.btn_ratio) {

        } else if (id == R.id.btn_rotate) {

        } else if (id == R.id.btn_flip) {

        } else if (id == R.id.btn_vertical_perspective) {

        } else if (id == R.id.btn_horizontal_perspective) {

        } else if (id == R.id.btn_stretch) {

        }
    }

    /**
     * 设置图片
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
    }
}
