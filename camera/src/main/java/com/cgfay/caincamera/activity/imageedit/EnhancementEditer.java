package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.cgfay.caincamera.R;

/**
 * 夜景增强编辑器
 * Created by Administrator on 2018/3/12.
 */

public class EnhancementEditer extends BaseEditer implements SeekBar.OnSeekBarChangeListener {

    private LinearLayout mLayoutEnhancement;

    private SeekBar mSeekBar;

    public EnhancementEditer(Context context, ImageEditManager manager) {
        super(context, manager);
    }

    @Override
    protected void initView() {
        mLayoutEnhancement = (LinearLayout) mInflater
                .inflate(R.layout.view_image_edit_enhancement, null);

        mSeekBar = (SeekBar) mLayoutEnhancement.findViewById(R.id.sb_value);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public LinearLayout getLayoutEnhancement() {
        return mLayoutEnhancement;
    }
}
