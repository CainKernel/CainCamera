package com.cgfay.picker.fragment;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import android.view.View;

import com.cgfay.picker.scanner.ImageDataScanner;
import com.cgfay.scan.R;

/**
 * 图片选择列表
 */
public class ImageDataFragment extends MediaDataFragment {

    @Override
    protected void initView(@NonNull View rootView) {
        super.initView(rootView);
        mMultiSelect = true;
        if (mMediaDataAdapter != null) {
            mMediaDataAdapter.setShowCheckbox(true);
        }
    }

    @Override
    protected void initDataProvider() {
        if (mDataScanner == null) {
            mDataScanner = new ImageDataScanner(mContext, LoaderManager.getInstance(this), this);
            mDataScanner.setUserVisible(getUserVisibleHint());
            mMediaDataAdapter.setShowCheckbox(true);
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_image_list;
    }

    @Override
    protected int getMediaType() {
        return TypeImage;
    }

    @Override
    public String getTitle() {
        return "图片";
    }
}
