package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cgfay.caincamera.R;

/**
 * 抠图虚化编辑器
 * Created by Administrator on 2018/3/12.
 */

public class MatBlurEditer extends BaseEditer implements View.OnClickListener {

    private LinearLayout mLayoutMatblur;
    private Button mBtnMat;
    private Button mBtnPreview;
    private Button mBtnErase;

    public MatBlurEditer(Context context) {
        super(context);
    }

    @Override
    protected void initView() {
        mLayoutMatblur = (LinearLayout) mInflater.inflate(R.layout.view_image_edit_matblur, null);
        mBtnMat = (Button) mLayoutMatblur.findViewById(R.id.btn_mat);
        mBtnPreview = (Button) mLayoutMatblur.findViewById(R.id.btn_preview);
        mBtnErase = (Button) mLayoutMatblur.findViewById(R.id.btn_erase);

        mBtnMat.setOnClickListener(this);
        mBtnPreview.setOnClickListener(this);
        mBtnErase.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mat:
                break;

            case R.id.btn_preview:
                break;

            case R.id.btn_erase:
                break;
        }
    }

    public LinearLayout getLayoutMatblur() {
        return mLayoutMatblur;
    }
}
