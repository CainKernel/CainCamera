package com.cgfay.camera.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * View控件适配器
 */
public class EffectViewPagerAdapter extends PagerAdapter {

    private final List<View> mViewList;
    private final List<String> mTitleList;

    public EffectViewPagerAdapter(@NonNull List<View> viewList, List<String> titleList) {
        mViewList = viewList;
        mTitleList = titleList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position >= mTitleList.size()) {
            return "";
        }
        return mTitleList.get(position);
    }
}
