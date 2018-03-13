package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.cgfay.caincamera.R;

/**
 * 裁剪旋转编辑器
 * Created by Administrator on 2018/3/12.
 */

public class CropRotateEditer extends BaseEditer
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private RelativeLayout mLayoutCropRotate;

    private SeekBar mSeekBar;

    private Button mBtnRotate90;
    private Button mBtnFlip;
    private Button mBtnCustom;
    private Button mBtnOriginal;
    private Button mBtnOneToOne;
    private Button mBtnThreeToFour;
    private Button mBtnFourToThree;
    private Button mBtnNineToSixteen;
    private Button mBtnSixteenToNine;

    public CropRotateEditer(Context context, ImageEditManager manager) {
        super(context, manager);
    }


    @Override
    protected void initView() {
        mLayoutCropRotate = (RelativeLayout) mInflater
                .inflate(R.layout.view_image_edit_croprotate, null);
        mBtnRotate90 = (Button) mLayoutCropRotate.findViewById(R.id.btn_rotate_90);
        mBtnFlip = (Button) mLayoutCropRotate.findViewById(R.id.btn_flip);
        mBtnCustom = (Button) mLayoutCropRotate.findViewById(R.id.btn_custom);
        mBtnOriginal = (Button) mLayoutCropRotate.findViewById(R.id.btn_original);
        mBtnOneToOne = (Button) mLayoutCropRotate.findViewById(R.id.btn_one_to_one);
        mBtnThreeToFour = (Button) mLayoutCropRotate.findViewById(R.id.btn_three_to_four);
        mBtnFourToThree = (Button) mLayoutCropRotate.findViewById(R.id.btn_four_to_three);
        mBtnNineToSixteen = (Button) mLayoutCropRotate.findViewById(R.id.btn_nine_to_sixteen);
        mBtnSixteenToNine = (Button) mLayoutCropRotate.findViewById(R.id.btn_sixteen_to_nine);

        mBtnRotate90.setOnClickListener(this);
        mBtnFlip.setOnClickListener(this);
        mBtnCustom.setOnClickListener(this);
        mBtnOriginal.setOnClickListener(this);
        mBtnOneToOne.setOnClickListener(this);
        mBtnThreeToFour.setOnClickListener(this);
        mBtnFourToThree.setOnClickListener(this);
        mBtnNineToSixteen.setOnClickListener(this);
        mBtnSixteenToNine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rotate_90:
                break;

            case R.id.btn_flip:
                break;

            case R.id.btn_custom:
                break;

            case R.id.btn_original:
                break;

            case R.id.btn_one_to_one:
                break;

            case R.id.btn_three_to_four:
                break;

            case R.id.btn_four_to_three:
                break;

            case R.id.btn_nine_to_sixteen:
                break;

            case R.id.btn_sixteen_to_nine:
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

    public RelativeLayout getLayoutCropRotate() {
        return mLayoutCropRotate;
    }
}
