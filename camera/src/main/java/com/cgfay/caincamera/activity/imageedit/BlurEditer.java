package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.cgfay.caincamera.R;

/**
 * 虚化编辑器
 * Created by Administrator on 2018/3/12.
 */

public class BlurEditer extends BaseEditer implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private RelativeLayout mLayoutBlur;

    private SeekBar mSeekBar;
    private Button mBtnBlurType;

    public BlurEditer(Context context) {
        super(context);
    }

    @Override
    protected void initView() {
        mLayoutBlur = (RelativeLayout) mInflater.inflate(R.layout.view_image_edit_blur, null);
        mSeekBar = (SeekBar) mLayoutBlur.findViewById(R.id.sb_value);
        mSeekBar.setOnSeekBarChangeListener(this);
        mBtnBlurType = (Button) mLayoutBlur.findViewById(R.id.btn_blur_type);
        mBtnBlurType.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_blur_type:
                break;
        }
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

    public RelativeLayout getLayoutBlur() {
        return mLayoutBlur;
    }

}
