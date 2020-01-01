package com.cgfay.picker.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cgfay.picker.fragment.MediaDataFragment;

import java.util.List;

/**
 * 媒体列表Fragment适配器
 */
public class MediaDataPagerAdapter extends FragmentPagerAdapter {

    private List<MediaDataFragment> mFragments;

    public MediaDataPagerAdapter(FragmentManager fm, List<MediaDataFragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public MediaDataFragment getItem(int position) {
        if (mFragments.size() > position) {
            return mFragments.get(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getTitle();
    }
}

