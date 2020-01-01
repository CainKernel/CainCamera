package com.cgfay.picker.fragment;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import android.view.View;
import android.widget.TextView;

import com.cgfay.picker.scanner.VideoDataScanner;
import com.cgfay.scan.R;

/**
 * 视频选择列表
 */
public class VideoDataFragment extends MediaDataFragment {

    private TextView mMultiSelectView;

    @Override
    protected void initView(@NonNull View rootView) {
        super.initView(rootView);
        mMultiSelect = false;
        mMultiSelectView = rootView.findViewById(R.id.tv_multi_video);
        mMultiSelectView.setOnClickListener(v -> {
            processMultiSelectView();
        });
        if (!mMultiSelect) {
            mMultiSelectView.setText(R.string.video_multi_picker);
        } else {
            mMultiSelectView.setText(R.string.video_single_picker);
        }
    }

    private void processMultiSelectView() {
        mMultiSelect = !mMultiSelect;
        if (!mMultiSelect) {
            mMultiSelectView.setText(R.string.video_multi_picker);
            if (mMediaDataAdapter != null) {
                mMediaDataAdapter.setShowCheckbox(false);
                mMediaDataAdapter.notifyDataSetChanged();
            }
            mPresenter.clear();
            if (mSelectedChangeListener != null) {
                mSelectedChangeListener.onSelectedChange("");
            }
        } else {
            mMultiSelectView.setText(R.string.video_single_picker);
            if (mMediaDataAdapter != null) {
                mMediaDataAdapter.setShowCheckbox(true);
                mMediaDataAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_video_list;
    }

    @Override
    protected void initDataProvider() {
        if (mDataScanner == null) {
            mDataScanner = new VideoDataScanner(mContext, LoaderManager.getInstance(this), this);
            mDataScanner.setUserVisible(getUserVisibleHint());
            mMediaDataAdapter.setShowCheckbox(mMultiSelect);
        }
    }

    @Override
    protected int getMediaType() {
        return TypeVideo;
    }

    @Override
    public String getTitle() {
        return "视频";
    }
}
